const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25610);
const PASS = process.argv[3] || 'f71rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f71-verify-out.txt';

const lines = [];
function log(s) { lines.push(s); console.log(s); }

function pkt(id, type, body) {
  const b = Buffer.from(body, 'utf8');
  const buf = Buffer.alloc(14 + b.length);
  buf.writeInt32LE(b.length + 10, 0);
  buf.writeInt32LE(id, 4);
  buf.writeInt32LE(type, 8);
  b.copy(buf, 12);
  buf.writeInt16LE(0, 12 + b.length);
  return buf;
}

class Rcon {
  constructor(sock) { this.sock = sock; this.acc = Buffer.alloc(0); this.pending = new Map(); this.id = 2; }
  static connect() {
    return new Promise((resolve, reject) => {
      const sock = net.connect(PORT, '127.0.0.1');
      const r = new Rcon(sock);
      let authed = false;
      sock.on('connect', () => sock.write(pkt(1, 3, PASS)));
      sock.on('error', reject);
      sock.on('data', (d) => {
        r.acc = Buffer.concat([r.acc, d]);
        while (r.acc.length >= 4) {
          const len = r.acc.readInt32LE(0);
          if (r.acc.length < 4 + len) break;
          const id = r.acc.readInt32LE(4);
          const body = r.acc.subarray(12, 4 + len - 2).toString('utf8');
          r.acc = r.acc.subarray(4 + len);
          if (id === 1 && !authed) { authed = true; resolve(r); continue; }
          const p = r.pending.get(id);
          if (p) { r.pending.delete(id); p(body); }
        }
      });
      setTimeout(() => reject(new Error('rcon auth timeout')), 15000);
    });
  }
  raw(str) {
    return new Promise((resolve) => {
      const id = this.id++;
      this.pending.set(id, resolve);
      this.sock.write(pkt(id, 2, str));
    });
  }
  close() { this.sock.end(); }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
let fails = 0;

async function cmd(r, c, echo) {
  const body = await r.raw(c);
  if (echo !== false) log('> ' + c + '\n    ' + JSON.stringify(body));
  return body;
}

async function expect(r, c, mustPass, label, echo) {
  const body = await cmd(r, c, echo !== false);
  const ok = mustPass ? /Test passed/i.test(body) : /Test failed/i.test(body);
  if (!ok) { fails++; log('  [FAIL] ' + label + ' :: 期望 ' + (mustPass ? 'Test passed' : 'Test failed') + '，实际 ' + JSON.stringify(body)); }
  else { log('  [PASS] ' + label); }
  return ok;
}

/** 八格燃料各不相同，用来同时验证「八格可混搭」（DESIGN §2.6 #2）。 */
const EIGHT = [
  'minecraft:wheat', 'minecraft:golden_apple', 'minecraft:crimson_fungus', 'farmstorm:molotov',
  'farmstorm:puffed_wheat', 'farmstorm:wild_apple', 'minecraft:carrot', 'farmstorm:lucky_berry'
];

/** kind: ok（正例）/ diamondCell（第 0 格换钻石）/ badCenter（中心不是工作台）。 */
function grid(kind) {
  const cells = [];
  let k = 0;
  for (let i = 0; i < 9; i++) {
    if (i === 4) { cells.push(kind === 'badCenter' ? 'minecraft:diamond_block' : 'minecraft:crafting_table'); }
    else if (kind === 'diamondCell' && i === 0) { cells.push('minecraft:diamond'); }
    else { cells.push(EIGHT[k++]); }
  }
  return cells;
}

async function craftTest(r, x, y, z, kind, label) {
  log('');
  log('## ' + label);
  // 没有客户端（headless）：crafting_shaped 的匹配与工作台走同一个 RecipeManager 判定，
  // 故用 crafter（9 格容器 + 红石上升沿）代替人工摆工作台；槽 0..8 就是 3x3 行优先格位。
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'setblock ' + x + ' ' + y + ' ' + z + ' minecraft:air');
  await cmd(r, 'setblock ' + x + ' ' + y + ' ' + z + ' minecraft:crafter[crafting=true]');
  const cells = grid(kind);
  for (let i = 0; i < 9; i++) {
    await cmd(r, 'item replace block ' + x + ' ' + y + ' ' + z + ' container.' + i + ' with ' + cells[i]);
  }
  await cmd(r, 'kill @e[type=minecraft:item]');
  await expect(r, 'execute if items block ' + x + ' ' + y + ' ' + z + ' container.4 ' +
      (kind === 'badCenter' ? 'minecraft:diamond_block' : 'minecraft:crafting_table'), true,
      label + '：中心格就位', false);
  await cmd(r, 'setblock ' + (x + 1) + ' ' + y + ' ' + z + ' minecraft:redstone_block');
  await sleep(900);
  await cmd(r, 'setblock ' + (x + 1) + ' ' + y + ' ' + z + ' minecraft:air');
  await sleep(500);
  await cmd(r, 'data get entity @e[type=minecraft:item,limit=1]');
  if (kind === 'ok') {
    await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:converter"}', true,
        label + '：产出 item 实体 id = farmstorm:converter');
    await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}', true, label + '：产出数量 = 1');
  } else {
    await expect(r, 'execute if entity @e[type=minecraft:item]', false, label + '：无任何 item 实体');
  }
}

(async () => {
  const r = await Rcon.connect();
  log('### RCON 已连接 127.0.0.1:' + PORT);
  log('');
  log('## 0. 清场与基线');
  await cmd(r, 'forceload add 96 96 112 112');             // V-16：forceload 用方块坐标
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');  // 清场
  await cmd(r, 'kill @e[type=minecraft:item]');
  await expect(r, 'execute if block 104 100 100 minecraft:air', true, '清场基线：104,100,100 为空');

  log('');
  log('## A. 验收 2：标签命中（/execute if items block ... #farmstorm:converter_fuel）');
  await cmd(r, 'setblock 100 100 100 minecraft:chest');
  const hits = ['minecraft:wheat', 'minecraft:golden_apple', 'minecraft:crimson_fungus',
                'farmstorm:puffed_wheat', 'farmstorm:molotov', 'farmstorm:wild_apple'];
  for (const it of hits) {
    await cmd(r, 'item replace block 100 100 100 container.0 with ' + it, false);
    await expect(r, 'execute if items block 100 100 100 container.0 #farmstorm:converter_fuel', true, '命中标签 ' + it);
  }
  const misses = ['minecraft:diamond', 'minecraft:enchanted_golden_apple', 'farmstorm:puffed_wheat_seeds',
                  'farmstorm:compressed_puffed_wheat', 'minecraft:apple', 'farmstorm:converter'];
  for (const it of misses) {
    await cmd(r, 'item replace block 100 100 100 container.0 with ' + it, false);
    await expect(r, 'execute if items block 100 100 100 container.0 #farmstorm:converter_fuel', false, '未入标签 ' + it);
  }

  await craftTest(r, 104, 100, 100, 'ok', 'B. 验收 3 正例：3x3（中心工作台 + 八格各 1 个燃料，八格混搭）→ 1 个转换器');
  await craftTest(r, 108, 100, 100, 'diamondCell', 'C. 验收 3 反例：把一格换成 minecraft:diamond → 无产物');
  await craftTest(r, 104, 100, 100, 'badCenter', 'D. 反例 2：中心放钻石块（不是工作台）→ 无产物');

  log('');
  log('## E. 收尾：清场');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');

  log('');
  log(fails === 0 ? '### 全部断言通过 (0 FAIL)' : '### 有 ' + fails + ' 条断言失败');
  fs.writeFileSync(OUTFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(fails === 0 ? 0 : 1);
})().catch((e) => { console.error('ERROR ' + e.message); fs.writeFileSync(OUTFILE, lines.join('\n') + '\nERROR ' + e.message + '\n', 'utf8'); process.exit(2); });

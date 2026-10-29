const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25612);
const PASS = process.argv[3] || 'f72rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f72-verify-out.txt';

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

/**
 * 把 cells 里 <= 9 个格位填好后，用红石上升沿让 crafter 合成一次。
 * slotted: [[slot, item], ...]，同一 slot 只写一次（一次给 count 个则用写满 ItemStack 的方式）。
 */
async function craftAttempt(r, x, y, z, slotted, label) {
  log('');
  log('## ' + label);
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'setblock ' + x + ' ' + y + ' ' + z + ' minecraft:air');
  await cmd(r, 'setblock ' + x + ' ' + y + ' ' + z + ' minecraft:crafter[crafting=true]');
  for (const [slot, item] of slotted) {
    await cmd(r, 'item replace block ' + x + ' ' + y + ' ' + z + ' container.' + slot + ' with ' + item, false);
  }
  // 复述容器实际内容（正例前先确认 9 格状态，避免被上一轮的残留污染）
  await cmd(r, 'data get block ' + x + ' ' + y + ' ' + z, true);
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'setblock ' + (x + 1) + ' ' + y + ' ' + z + ' minecraft:redstone_block');
  await sleep(900);
  await cmd(r, 'setblock ' + (x + 1) + ' ' + y + ' ' + z + ' minecraft:air');
  await sleep(600);
  await cmd(r, 'data get entity @e[type=minecraft:item,limit=1]');
}

(async () => {
  const r = await Rcon.connect();
  log('### RCON 已连接 127.0.0.1:' + PORT);
  log('');
  log('## 0. 清场与基线');
  await cmd(r, 'forceload add 96 96 112 112');                // V-16：forceload 用方块坐标
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await expect(r, 'execute if block 104 100 100 minecraft:air', true, '清场基线：104,100,100 为空');


  // ---- 验收 2：4 个膨胀麦，两种摆放位置各一次 ----
  await craftAttempt(r, 104, 100, 100, [[0, 'farmstorm:puffed_wheat'], [2, 'farmstorm:puffed_wheat'],
      [6, 'farmstorm:puffed_wheat'], [8, 'farmstorm:puffed_wheat']],
      'B. 验收 2-a：4 个膨胀麦放在 9 格的四角（0/2/6/8）→ 应得 1 个压缩膨胀麦');
  await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:compressed_puffed_wheat"}',
      true, 'B：产出 item 实体 id = farmstorm:compressed_puffed_wheat');
  await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}', true, 'B：产出数量 = 1');

  await craftAttempt(r, 108, 100, 100, [[1, 'farmstorm:puffed_wheat'], [3, 'farmstorm:puffed_wheat'],
      [5, 'farmstorm:puffed_wheat'], [7, 'farmstorm:puffed_wheat']],
      'C. 验收 2-b：4 个膨胀麦换一组位置（1/3/5/7，无序也成立）→ 应得 1 个压缩膨胀麦');
  await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:compressed_puffed_wheat"}',
      true, 'C：产出 item 实体 id = farmstorm:compressed_puffed_wheat');
  await expect(r, 'execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}', true, 'C：产出数量 = 1');

  // ---- 验收 3：3 个无产物 ----
  await craftAttempt(r, 100, 100, 100, [[0, 'farmstorm:puffed_wheat'], [4, 'farmstorm:puffed_wheat'],
      [8, 'farmstorm:puffed_wheat']], 'D. 验收 3-a：只放 3 个膨胀麦 → 无产物');
  await expect(r, 'execute if entity @e[type=minecraft:item]', false, 'D：无任何 item 实体');

  // ---- 验收 3：5 个无产物 ----
  await craftAttempt(r, 112, 100, 100, [[0, 'farmstorm:puffed_wheat'], [1, 'farmstorm:puffed_wheat'],
      [3, 'farmstorm:puffed_wheat'], [4, 'farmstorm:puffed_wheat'], [8, 'farmstorm:puffed_wheat']],
      'E. 验收 3-b：放 5 个膨胀麦 → 无产物');
  await expect(r, 'execute if entity @e[type=minecraft:item]', false, 'E：无任何 item 实体');

  // ---- 附加边界：4 个堆在同一个格子里不等于 4 份原料 ----
  log('');
  log('## F. 附加：4 个膨胀麦堆在同一格（container.4 一叠 4 个）→ 无产物');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'setblock 104 100 104 minecraft:air');
  await cmd(r, 'setblock 104 100 104 minecraft:crafter[crafting=true]');
  await cmd(r, 'item replace block 104 100 104 container.4 with farmstorm:puffed_wheat 4');
  await cmd(r, 'data get block 104 100 104');
  await cmd(r, 'setblock 105 100 104 minecraft:redstone_block');
  await sleep(900);
  await cmd(r, 'setblock 105 100 104 minecraft:air');
  await sleep(600);
  await cmd(r, 'data get entity @e[type=minecraft:item,limit=1]');
  await expect(r, 'execute if entity @e[type=minecraft:item]', false, 'F：无任何 item 实体');

  log('');
  log('## G. 收尾：清场');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');

  log('');
  log(fails === 0 ? '### 全部断言通过 (0 FAIL)' : '### 有 ' + fails + ' 条断言失败');
  fs.writeFileSync(OUTFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(fails === 0 ? 0 : 1);
})().catch((e) => { console.error('ERROR ' + e.message); fs.writeFileSync(OUTFILE, lines.join('\n') + '\nERROR ' + e.message + '\n', 'utf8'); process.exit(2); });

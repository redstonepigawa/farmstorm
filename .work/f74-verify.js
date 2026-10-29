const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25622);
const PASS = process.argv[3] || 'f74rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f74-verify-out.txt';

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
          if (p) {
            p.acc += body;
            if (body.length < 4096) { r.pending.delete(id); p.resolve(p.acc); }
          }
        }
      });
      setTimeout(() => reject(new Error('rcon auth timeout')), 15000);
    });
  }
  raw(str) {
    return new Promise((resolve) => {
      const id = this.id++;
      this.pending.set(id, { acc: '', resolve });
      this.sock.write(pkt(id, 2, str));
      setTimeout(() => { if (this.pending.delete(id)) resolve('<TIMEOUT>'); }, 60000);
    });
  }
  close() { this.sock.end(); }
}

let fails = 0;
function assert(cond, label, detail) {
  if (cond) { log('  [PASS] ' + label); }
  else { fails++; log('  [FAIL] ' + label + (detail ? ' :: ' + detail : '')); }
}

async function cmd(r, c, echo) {
  const body = await r.raw(c);
  if (echo !== false) {
    log('> ' + c);
    for (const line of body.split('\n')) { if (line.trim().length > 0) log('    | ' + line); }
  }
  return body;
}

function checkHay(hay, tag) {
  assert(/tag block=compressed_hay_block .*mineable\/pickaxe=true needs_diamond_tool=true requiresCorrectToolForDrops=true destroySpeed=30\.0/.test(hay),
    tag + ' 运行时标签：mineable/pickaxe + needs_diamond_tool 均 true、硬速 30.0');
  assert(/loottable key=farmstorm:blocks\/compressed_hay_block empty=false/.test(hay),
    tag + ' 战利品表已加载（empty=false）');
  assert(/gate tool=empty .*hasCorrectToolForDrops=false canHarvestBlock=false/.test(hay), tag + ' 门槛：空手 false');
  assert(/gate tool=minecraft:wooden_pickaxe .*hasCorrectToolForDrops=false canHarvestBlock=false .*perTick=0\.000667/.test(hay), tag + ' 门槛：木镐 false、0.000667/tick');
  assert(/gate tool=minecraft:stone_pickaxe .*hasCorrectToolForDrops=false canHarvestBlock=false .*perTick=0\.001333/.test(hay), tag + ' 门槛：石镐 false、0.001333/tick');
  assert(/gate tool=minecraft:iron_pickaxe .*hasCorrectToolForDrops=false canHarvestBlock=false .*perTick=0\.002000/.test(hay), tag + ' 门槛：铁镐 false、0.002000/tick');
  assert(/gate tool=minecraft:diamond_pickaxe .*hasCorrectToolForDrops=true canHarvestBlock=true .*perTick=0\.008889/.test(hay), tag + ' 门槛：钻石镐 true、0.008889/tick');
  assert(/gate tool=minecraft:netherite_pickaxe .*hasCorrectToolForDrops=true canHarvestBlock=true .*perTick=0\.010000/.test(hay), tag + ' 门槛：下界合金镐 true、0.010000/tick');

  assert(/hold tool=empty ticks=600 perTick=0\.000333 progressAt600=0\.200000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0/.test(hay),
    tag + ' 空手 30 秒：进度 0.200、方块未破、掉落 0');
  assert(/hold tool=minecraft:wooden_pickaxe ticks=600 perTick=0\.000667 progressAt600=0\.400000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0/.test(hay),
    tag + ' 木镐 30 秒：进度 0.400、方块未破、掉落 0');
  assert(/hold tool=minecraft:stone_pickaxe ticks=600 perTick=0\.001333 progressAt600=0\.800000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0/.test(hay),
    tag + ' 石镐 30 秒：进度 0.800、方块未破、掉落 0');

  assert(/digprep tool=minecraft:diamond_pickaxe .*canHarvestBlock=true perTick=0\.008889 needTicks=113 directDrops=\[farmstorm:compressed_hay_blockx1\]/.test(hay),
    tag + ' 钻石镐：战利品表直算得到本体 1 个');
  assert(/dig tool=empty stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true .*drops=total=0 \{\}/.test(hay),
    tag + ' 空手挖穿：方块被破坏、掉落 0');
  assert(/dig tool=minecraft:wooden_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true .*drops=total=0 \{\}/.test(hay),
    tag + ' 木镐挖穿：方块被破坏、掉落 0（工具门槛生效）');
  assert(/dig tool=minecraft:iron_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true .*drops=total=0 \{\}/.test(hay),
    tag + ' 铁镐挖穿：方块被破坏、掉落 0（needs_diamond_tool 卡住铁镐）');
  assert(/dig tool=minecraft:diamond_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=true stateAfter=minecraft:air broken=true .*drops=total=1 \{farmstorm:compressed_hay_block=1\}/.test(hay),
    tag + ' 钻石镐挖穿：方块被破坏、掉本体 1 个');
}

(async () => {
  const r = await Rcon.connect();
  log('### RCON connected 127.0.0.1:' + PORT);
  log('');

  log('## 0. 清场与基线（V-16 方块坐标 forceload / V-17 清场 + 基线断言）');
  await cmd(r, 'forceload add 96 96 128 128');
  await cmd(r, 'fill 96 96 96 128 112 128 minecraft:air');
  await cmd(r, 'kill @e[type=minecraft:item]');
  const b1 = await cmd(r, 'execute if block 104 100 104 minecraft:air');
  const b2 = await cmd(r, 'execute if block 110 100 110 minecraft:air');
  const b3 = await cmd(r, 'execute if block 114 100 114 minecraft:air');
  assert(/Test passed/i.test(b1), '基线：104,100,104 空');
  assert(/Test passed/i.test(b2), '基线：110,100,110 空');
  assert(/Test passed/i.test(b3), '基线：114,100,114 空');

  log('');
  log('## 1. 压缩干草块 · 第一次（工具门槛 + 30 秒 + 挖穿）');
  checkHay(await cmd(r, 'f74probe hay'), 'run1');

  log('');
  log('## 2. 压缩干草块 · 第二次（同一命令重复执行，验证可复现）');
  checkHay(await cmd(r, 'f74probe hay'), 'run2');

  log('');
  log('## 3. 桑吉尔蔗（部署方块口径）：玩家自挖掉自身');
  const cane = await cmd(r, 'f74probe cane');
  assert(/tag block=zangief_cane .*deployed_crops=true requiresCorrectToolForDrops=false destroySpeed=0\.5/.test(cane), '桑吉尔蔗：deployed_crops=true、无工具门槛');
  assert(/loottable key=farmstorm:blocks\/zangief_cane empty=false/.test(cane), '桑吉尔蔗：战利品表已加载（empty=false）');
  assert(/digprep tool=empty .*canHarvestBlock=true perTick=0\.066667 needTicks=15 directDrops=\[farmstorm:zangief_canex1\]/.test(cane), '桑吉尔蔗：战利品表直算得到自身 1 个');
  assert(/dig tool=empty .*stateAfter=minecraft:air broken=true .*drops=total=1 \{farmstorm:zangief_cane=1\}/.test(cane), '桑吉尔蔗：空手挖掉掉自身 1 个');

  log('');
  log('## 4. 对照组：同一套机具打原版石头，证明读数能看到掉落');
  const ctl = await cmd(r, 'f74probe control');
  assert(/dig tool=minecraft:wooden_pickaxe .*broken=true .*drops=total=1 \{minecraft:cobblestone=1\}/.test(ctl), '对照组：木镐挖石头掉圆石 1 个');

  log('');
  log('## 5. 收尾清场');
  await cmd(r, 'fill 96 96 96 128 112 128 minecraft:air');
  await cmd(r, 'kill @e[type=minecraft:item]');
  const after = await cmd(r, 'execute if block 104 100 104 minecraft:air');
  assert(/Test passed/i.test(after), '收尾：104,100,104 已清空');

  log('');
  log(fails === 0 ? '### 全部断言通过 (0 FAIL)' : '### 有 ' + fails + ' 条断言失败');
  fs.writeFileSync(OUTFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(fails === 0 ? 0 : 1);
})().catch((e) => { console.error('ERROR ' + e.message); fs.writeFileSync(OUTFILE, lines.join('\n') + '\nERROR ' + e.message + '\n', 'utf8'); process.exit(2); });

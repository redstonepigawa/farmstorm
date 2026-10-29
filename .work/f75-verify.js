
const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25632);
const PASS = process.argv[3] || 'f75rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f75-verify-out.txt';
const SERVERLOG = 'D:/project_farm/.work/f75-server.log';

const out = [];
function log(s) { out.push(s); console.log(s); }
function flush() { fs.writeFileSync(OUTFILE, out.join('\n') + '\n'); }

let pass = 0, fail = 0;
const failures = [];
function check(name, cond, detail) {
  if (cond) { pass++; log('  [PASS] ' + name + (detail ? '  (' + detail + ')' : '')); }
  else { fail++; failures.push(name + (detail ? ' :: ' + detail : '')); log('  [FAIL] ' + name + (detail ? '  (' + detail + ')' : '')); }
}
function eq(name, actual, expected) {
  const a = JSON.stringify(actual), e = JSON.stringify(expected);
  check(name, a === e, 'actual=' + a + ' expected=' + e);
}
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

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
          const type = r.acc.readInt32LE(8);
          const body = r.acc.subarray(12, 4 + len - 2).toString('utf8');
          r.acc = r.acc.subarray(4 + len);
          if (id === 1 && !authed) { authed = true; resolve(r); continue; }
          if (id === 2 && type === 2) { resolve(r); continue; }
          const p = r.pending.get(id);
          if (p) { p.acc += body; if (body.length < 4096) { r.pending.delete(id); p.resolve(p.acc); } }
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
      setTimeout(() => { if (this.pending.delete(id)) resolve('<TIMEOUT>'); }, 120000);
    });
  }
  close() { this.sock.end(); }
}

let rcon;
async function cmd(str) { return rcon.raw(str); }
function clean(s) { return (s || '').replace(/\r/g, '').replace(/\n+$/g, '').replace(/\n/g, ' / '); }
async function run(str) {
  const r = await cmd(str);
  log('> /' + str);
  log('    "' + clean(r) + '"');
  return r;
}
async function runSilent(str) { return rcon.raw(str); }

// ---- 方块读数 ----
async function isBlock(x, y, z, id) {
  const t = await rcon.raw('execute if block ' + x + ' ' + y + ' ' + z + ' ' + id);
  return t.startsWith('Test passed');
}
async function kindAt(x, y, z, ids) {
  for (const id of ids) if (await isBlock(x, y, z, id)) return id;
  return 'other';
}

const FLAMMABLE = ['minecraft:fire', 'minecraft:magma_block', 'minecraft:air'];

function disc4(cx, cz) {
  const cells = [];
  for (let dx = -4; dx <= 4; dx++) for (let dz = -4; dz <= 4; dz++) {
    if (dx * dx + dz * dz <= 16) cells.push([cx + dx, cz + dz]);
  }
  return cells;
}
function square9(cx, cz) {
  const cells = [];
  for (let dx = -4; dx <= 4; dx++) for (let dz = -4; dz <= 4; dz++) cells.push([cx + dx, cz + dz]);
  return cells;
}
function cube3(cx, cy, cz) {
  const cells = [];
  for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) for (let dz = -1; dz <= 1; dz++) {
    cells.push([cx + dx, cy + dy, cz + dz]);
  }
  return cells;
}

const MOL_CENTER = [100, 99, 116];   // 落点方块
const MOL_TOP = 100;                 // 火/岩浆写入层
const PUF_CENTER = [114, 99, 100];

async function scanFireLayer(cx, cz, y) {
  let fire = 0, magma = 0, air = 0, other = [];
  for (const [x, z] of disc4(cx, cz)) {
    const k = await kindAt(x, y, z, FLAMMABLE);
    if (k === 'minecraft:fire') fire++;
    else if (k === 'minecraft:magma_block') magma++;
    else if (k === 'minecraft:air') air++;
    else other.push(x + ',' + y + ',' + z + '=' + k);
  }
  return { fire, magma, air, other };
}

async function captureSet(cells, ids) {
  const map = new Map();
  for (const [x, y, z] of cells) map.set(x + ',' + y + ',' + z, await kindAt(x, y, z, ids));
  return map;
}

async function countItemEntities() {
  const t = await rcon.raw('execute if entity @e[type=minecraft:item]');
  if (t.startsWith('Test passed')) {
    const d = await rcon.raw('data get entity @e[type=minecraft:item,limit=1] Item');
    return { any: true, sample: clean(d).slice(0, 200) };
  }
  return { any: false, sample: '' };
}

function parseProbe(line) {
  const obj = {};
  const body = line.replace(/^.*\[F75PROBE\] /, '');
  const re = /([A-Za-z][A-Za-z0-9_.]*)=("[^"]*"|\S+)/g;
  let m;
  while ((m = re.exec(body))) obj[m[1]] = m[2];
  return obj;
}
function probeLine(text) {
  const m = /\[F75PROBE\] [^\n]*/.exec(text || '');
  return m ? m[0] : null;
}

function logLines(pattern) {
  const text = fs.readFileSync(SERVERLOG, 'utf8');
  return text.split(/\r?\n/).filter(l => l.includes(pattern));
}

// ================= 主流程 =================
(async () => {
  rcon = await Rcon.connect();
  log('=== F-75 出口回归 · 五步闭环 ===');
  log('RCON 127.0.0.1:' + PORT + '  输出 ' + OUTFILE);
  await run('gamerule sendCommandFeedback true');
  await run('gamerule doMobSpawning false');
  await run('gamerule randomTickSpeed 3');
  await run('difficulty peaceful');

  // ---------- S0 清场与基线 ----------
  log('\n--- S0 环境与基线 ---');
  await run('forceload add 96 96 128 128');
  await sleep(1200);                                   // 区块加载 + 冻结实体复活（V-17）
  await run('kill @e[type=minecraft:item]');
  await run('kill @e[type=minecraft:cow]');
  await sleep(400);
  await run('kill @e[type=minecraft:item]');
  await runSilent('kill @e[type=farmstorm:molotov]');
  await runSilent('kill @e[type=farmstorm:puffed_wheat]');
  await runSilent('kill @e[type=farmstorm:compressed_puffed_wheat]');
  await run('fill 96 96 96 128 112 128 minecraft:air');
  // 三块测试台（y=99 石板层）
  await run('fill 96 99 96 108 99 108 minecraft:stone');      // 合成 / 转换区
  await run('fill 96 99 112 104 99 120 minecraft:stone');     // 莫洛托夫台
  await run('fill 110 99 96 118 99 104 minecraft:stone');     // 膨胀麦台
  await sleep(300);
  const base1 = await run('execute if block 96 96 96 minecraft:air');
  const base2 = await run('execute if block 100 100 116 minecraft:air');
  const base3 = await run('execute if block 114 99 100 minecraft:stone');
  const base4 = await run('execute if block 100 99 100 minecraft:stone');
  check('基线：清场后 (96,96,96) 是空气', base1.startsWith('Test passed'));
  check('基线：莫洛托夫台 y=100 层是空气', base2.startsWith('Test passed'));
  check('基线：膨胀麦台 (114,99,100) 是石头', base3.startsWith('Test passed'));
  check('基线：合成区 (100,99,100) 是石头', base4.startsWith('Test passed'));
  const e0 = await countItemEntities();
  check('基线：世界里没有掉落物', !e0.any, e0.sample);

  // ---------- S1 做转换器 ----------
  log('\n--- S1 做转换器（crafter = 3×3 工作台等价路径）---');
  const st = await run('f75probe setup 102.5 100.0 102.5');
  check('S1 探针玩家就位', /setup name=F75Probe/.test(st) && /gameMode=SURVIVAL/.test(st), clean(st));
  const giveFuel = [
    ['minecraft:crafting_table', 1], ['minecraft:wheat', 1], ['minecraft:carrot', 1],
    ['minecraft:potato', 1], ['minecraft:beetroot', 1], ['minecraft:melon_slice', 1],
    ['minecraft:cactus', 1], ['minecraft:bamboo', 1], ['minecraft:sweet_berries', 1],
  ];
  const giveReplies = {};
  for (const [id, n] of giveFuel) {
    const r = await run('f75probe give ' + id + ' ' + n);
    giveReplies[id] = clean(r);
    check('S1 /give ' + id, /Gave \d+ \[/.test(clean(r)) || /Gave .* to F75Probe/.test(clean(r)), clean(r).slice(0, 160));
  }
  await run('setblock 100 100 100 minecraft:crafter');
  await run('item replace block 100 100 100 container.4 with minecraft:crafting_table');
  const fuelSlots = [0, 1, 2, 3, 5, 6, 7, 8];
  const fuelIds = ['minecraft:wheat', 'minecraft:carrot', 'minecraft:potato', 'minecraft:beetroot',
                   'minecraft:melon_slice', 'minecraft:cactus', 'minecraft:bamboo', 'minecraft:sweet_berries'];
  for (let i = 0; i < fuelSlots.length; i++) {
    await run('item replace block 100 100 100 container.' + fuelSlots[i] + ' with ' + fuelIds[i]);
  }
  await run('setblock 101 100 100 minecraft:redstone_block');
  await sleep(600);
  await run('setblock 101 100 100 minecraft:air');
  await sleep(500);
  await run('data get entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter"}},limit=1]');
  const crafted = await run('execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter"}}]');
  check('S1 合成产出 farmstorm:converter（item 实体）', crafted.startsWith('Test passed'), clean(crafted));
  const craftedCount = await run('execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter",count:1}}]');
  check('S1 转换器数量 = 1', craftedCount.startsWith('Test passed'), clean(craftedCount));
  await run('setblock 104 100 100 farmstorm:converter');
  const convBlock = await run('execute if block 104 100 100 farmstorm:converter');
  check('S1 /execute if block：转换器已放置', convBlock.startsWith('Test passed'), clean(convBlock));
  await run('kill @e[type=minecraft:item]');
  await sleep(200);

  // ---------- S2 换出三件新物品 ----------
  log('\n--- S2 换出三件新物品（真实 ConverterMenu 按钮）---');
  await run('f75probe xp 200');
  await run('f75probe give minecraft:nether_star 1');   // D-47 催化剂（先给星星）
  for (const id of ['minecraft:crimson_fungus', 'minecraft:sugar_cane', 'minecraft:golden_apple', 'minecraft:torchflower']) {
    await run('f75probe give ' + id + ' 1');
  }
  const c1 = await run('f75probe convert 104 100 100 minecraft:crimson_fungus minecraft:nether_star 1');
  const p1 = parseProbe(c1);
  check('S2 转换器 A：crimson_fungus → molotov（按钮返回 true）', p1.clickMenuButton === 'true', clean(c1).slice(0, 200));
  check('S2 转换器 A：canConvert=true 且 xpCost=3', p1.canConvertBefore === 'true' && p1.xpCost === '3', 'canConvert=' + p1.canConvertBefore + ' xpCost=' + p1.xpCost);
  check('S2 转换器 A：扣 3 点经验（totalExperience 200→197）', p1.marginXp === '200->197', p1.marginXp);
  check('S2 转换器 A：输入槽清空、催化剂槽仍是下界之星×1（该配方无催化剂）',
    p1.inputSlotAfter === 'empty' && /nether_star/.test(p1.catalystSlotAfter || ''), 'input=' + p1.inputSlotAfter + ' catalyst=' + p1.catalystSlotAfter);
  check('S2 转换器 A：背包出现 molotov', /farmstorm:molotov/.test(p1.inv || ''), (p1.inv || '').slice(0, 200));

  const c2 = await run('f75probe convert 104 100 100 minecraft:sugar_cane none 0');
  const p2 = parseProbe(c2);
  check('S2 转换器 B：sugar_cane → zangief_cane（按钮返回 true）', p2.clickMenuButton === 'true', clean(c2).slice(0, 200));
  check('S2 转换器 B：扣 3 点经验（197→194）', p2.marginXp === '197->194', p2.marginXp);
  check('S2 转换器 B：催化剂量未变（仍 ×1）', /nether_star/.test(p2.catalystSlotAfter || ''), p2.catalystSlotAfter);
  check('S2 转换器 B：背包出现 zangief_cane', /zangief_cane/.test(p2.inv || ''), (p2.inv || '').slice(0, 220));

  const c3 = await run('f75probe convert 104 100 100 minecraft:golden_apple none 0');
  const p3 = parseProbe(c3);
  check('S2 转换器 C：golden_apple → wild_apple（按钮返回 true）', p3.clickMenuButton === 'true', clean(c3).slice(0, 200));
  check('S2 转换器 C：扣 3 点经验（194→191）', p3.marginXp === '194->191', p3.marginXp);
  check('S2 转换器 C：背包出现 wild_apple', /farmstorm:wild_apple/.test(p3.inv || ''), (p3.inv || '').slice(0, 240));

  const c4 = await run('f75probe convert 104 100 100 farmstorm:wild_apple none 0');
  const p4 = parseProbe(c4);
  check('S2 转换器 D（反向）：wild_apple → golden_apple（双向可用）', p4.clickMenuButton === 'true', clean(c4).slice(0, 200));
  check('S2 转换器 D（反向）：反向同样扣 3 点经验（191→188）', p4.marginXp === '191->188', p4.marginXp);

  const c5 = await run('f75probe convert 104 100 100 minecraft:torchflower none 0');
  const p5 = parseProbe(c5);
  check('S2 补充：torchflower 正向转换消耗 1 个下界之星（D-47 催化剂槽路径）',
    p5.clickMenuButton === 'true' && p5.catalystSlotAfter === 'empty',
    'click=' + p5.clickMenuButton + ' catalystAfter=' + p5.catalystSlotAfter);
  const convStill = await run('execute if block 104 100 100 farmstorm:converter');
  check('S2 /execute if block：转换器仍在原位', convStill.startsWith('Test passed'), clean(convStill));
  await run('f75probe inv');

  // ---------- S3 4 合 1 ----------
  log('\n--- S3 4 合 1（4 × puffed_wheat → compressed_puffed_wheat）---');
  const g3 = await run('f75probe give farmstorm:puffed_wheat 4');
  check('S3 /give 4 × puffed_wheat', /Gave 4 \[/.test(clean(g3)), clean(g3).slice(0, 160));
  await run('setblock 100 100 104 minecraft:crafter');
  for (const slot of [0, 1, 2, 3]) await run('item replace block 100 100 104 container.' + slot + ' with farmstorm:puffed_wheat');
  await run('setblock 101 100 104 minecraft:redstone_block');
  await sleep(600);
  await run('setblock 101 100 104 minecraft:air');
  await sleep(500);
  await run('data get entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat"}},limit=1]');
  const cp = await run('execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat"}}]');
  check('S3 4 个膨胀麦 → 1 个压缩膨胀麦', cp.startsWith('Test passed'), clean(cp));
  const cpCount = await run('execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat",count:1}}]');
  check('S3 压缩膨胀麦数量 = 1', cpCount.startsWith('Test passed'), clean(cpCount));
  const crafterB = await run('execute if block 100 100 104 minecraft:crafter');
  check('S3 /execute if block：工作格方块是 crafter', crafterB.startsWith('Test passed'), clean(crafterB));
  await run('kill @e[type=minecraft:item]');
  // 反例：3 个 / 5 个无产物
  for (const slot of [0, 1, 2, 3, 4]) await run('item replace block 100 100 104 container.' + slot + ' with minecraft:air');
  for (const slot of [0, 1, 2]) await run('item replace block 100 100 104 container.' + slot + ' with farmstorm:puffed_wheat');
  await run('setblock 101 100 104 minecraft:redstone_block'); await sleep(500); await run('setblock 101 100 104 minecraft:air'); await sleep(400);
  const three = await countItemEntities();
  check('S3 反例：3 个膨胀麦无产物', !three.any, three.sample);
  for (const slot of [0, 1, 2, 3, 4]) await run('item replace block 100 100 104 container.' + slot + ' with minecraft:air');
  for (const slot of [0, 1, 2, 3, 4]) await run('item replace block 100 100 104 container.' + slot + ' with farmstorm:puffed_wheat');
  await run('setblock 101 100 104 minecraft:redstone_block'); await sleep(500); await run('setblock 101 100 104 minecraft:air'); await sleep(400);
  const five = await countItemEntities();
  check('S3 反例：5 个膨胀麦无产物', !five.any, five.sample);
  for (const slot of [0, 1, 2, 3, 4]) await run('item replace block 100 100 104 container.' + slot + ' with minecraft:air');

  // ---------- S4 投莫洛托夫点火 ----------
  log('\n--- S4 投莫洛托夫点火（默认 fireChance=85.0）---');
  const g4 = await run('f75probe give farmstorm:molotov 1');
  check('S4 /give farmstorm:molotov', /Gave 1 \[/.test(clean(g4)), clean(g4).slice(0, 160));
  await run('summon minecraft:cow 96.5 100.0 112.5 {NoAI:1b,Silent:1b}');   // 距落点 5.66 格：半径 4 之外，不会被自己点的火点到
  const h1 = await run('data get entity @e[type=minecraft:cow,limit=1] Health');
  const hpBefore = clean(h1);
  check('S4 对照牛已就位（Health 10.0）', /10\.0/.test(hpBefore), hpBefore);
  const e1 = await countItemEntities();
  check('S4 投掷前无掉落物', !e1.any, e1.sample);
  await run('summon farmstorm:molotov 100.5 104.0 116.5 {Motion:[0.0d,0.0d,0.0d]}');
  await sleep(1800);
  await runSilent('kill @e[type=farmstorm:molotov]');
  await sleep(200);
  const fireScan = await scanFireLayer(MOL_CENTER[0], MOL_CENTER[2], MOL_TOP);
  log('    [扫描] y=100 层半径 4 圆盘 49 格：火 ' + fireScan.fire + ' 岩浆 ' + fireScan.magma
      + ' 空气 ' + fireScan.air + ' 其它 ' + fireScan.other.length);
  check('S4 候选格全部被写入火/岩浆（49 格无遗漏）', fireScan.fire + fireScan.magma === 49 && fireScan.other.length === 0,
    'fire=' + fireScan.fire + ' magma=' + fireScan.magma + ' other=' + JSON.stringify(fireScan.other).slice(0, 120));
  // 平台 81 格逐格仍在
  let platformIntact = 0;
  for (const [x, z] of square9(MOL_CENTER[0], MOL_CENTER[2])) if (await isBlock(x, 99, z, 'minecraft:stone')) platformIntact++;
  check('S4 原方块一个都没少：y=99 平台 81 格全是石头', platformIntact === 81, 'stone=' + platformIntact + '/81');
  let nonCandidateAir = 0, nonCandidateBad = [];
  const disc = new Set(disc4(MOL_CENTER[0], MOL_CENTER[2]).map(c => c[0] + ',' + c[1]));
  for (const [x, z] of square9(MOL_CENTER[0], MOL_CENTER[2])) {
    if (disc.has(x + ',' + z)) continue;
    const k = await kindAt(x, MOL_TOP, z, ['minecraft:air', 'minecraft:fire', 'minecraft:magma_block']);
    if (k === 'minecraft:air') nonCandidateAir++; else nonCandidateBad.push(x + ',' + z + '=' + k);
  }
  check('S4 半径外 32 格一块没动（仍是空气）', nonCandidateAir === 32 && nonCandidateBad.length === 0,
    'air=' + nonCandidateAir + ' bad=' + JSON.stringify(nonCandidateBad).slice(0, 120));
  const h2 = await run('data get entity @e[type=minecraft:cow,limit=1] Health');
  check('S4 无爆炸伤害：牛 Health 投前投后相同', clean(h2) === hpBefore, clean(h2) + ' vs ' + hpBefore);
  const firePoint = await run('execute if block 100 100 116 minecraft:fire');
  const firePointMagma = await run('execute if block 100 100 116 minecraft:magma_block');
  check('S4 /execute if block：落点正上方那格确实是火或岩浆块', firePoint.startsWith('Test passed') || firePointMagma.startsWith('Test passed'),
    'fire=' + clean(firePoint) + ' magma=' + clean(firePointMagma));
  const candLine = logLines('莫洛托夫 落点').slice(-1)[0] || '';
  const candLine2 = logLines('莫洛托夫 候选').slice(-1)[0] || '';
  log('    服务端日志：' + candLine.trim());
  log('    服务端日志：' + candLine2.trim());
  check('S4 日志：落点威力 0（默认 molotov.triggerExplosionPower=0）', /威力 0$/.test(candLine.trim()), candLine.trim());
  check('S4 日志：候选 49 = 火 + 岩浆', /莫洛托夫 候选 49 火 (\d+) 岩浆 (\d+)/.test(candLine2.trim()), candLine2.trim());

  // ---------- S4b 20 投粗核（§11 #7 的 85 : 15） ----------
  log('\n--- S4b 连投 20 颗，火 : 岩浆块 ≈ 85 : 15 ---');
  let sumFire = 0, sumMagma = 0, sumCand = 0, badThrows = [];
  for (let i = 1; i <= 20; i++) {
    await runSilent('fill 96 100 112 104 104 120 minecraft:air');
    await sleep(120);
    await runSilent('summon farmstorm:molotov 100.5 104.0 116.5 {Motion:[0.0d,0.0d,0.0d]}');
    await sleep(1400);
    await runSilent('kill @e[type=farmstorm:molotov]');
    const s = await scanFireLayer(MOL_CENTER[0], MOL_CENTER[2], MOL_TOP);
    sumFire += s.fire; sumMagma += s.magma; sumCand += s.fire + s.magma;
    if (s.fire + s.magma !== 49 || s.other.length) badThrows.push(i + ':fire=' + s.fire + ',magma=' + s.magma + ',other=' + s.other.length);
    if (i % 5 === 0) log('    第 ' + i + ' 投：火 ' + s.fire + ' 岩浆 ' + s.magma);
  }
  log('    20 投合计：候选 ' + sumCand + ' 火 ' + sumFire + ' 岩浆 ' + sumMagma);
  const magmaPct = sumCand ? (sumMagma * 100 / sumCand) : 0;
  check('S4b 每投候选恰好 49 且火+岩浆=候选', badThrows.length === 0, JSON.stringify(badThrows).slice(0, 200));
  check('S4b 岩浆块占比落在 10 %–20 %', magmaPct >= 10 && magmaPct <= 20, magmaPct.toFixed(2) + '%（' + sumMagma + '/' + sumCand + '）');
  const throwLogs = logLines('莫洛托夫 候选').slice(-21);
  log('    日志最近 21 条「候选」行（首条为上面的单投）：');
  for (const l of throwLogs) log('      ' + l.trim());
  const parsed = throwLogs.slice(1).map(l => { const m = /候选 (\d+) 火 (\d+) 岩浆 (\d+)/.exec(l); return m ? [+m[1], +m[2], +m[3]] : null; }).filter(Boolean);
  const logSumF = parsed.reduce((a, p) => a + p[1], 0), logSumM = parsed.reduce((a, p) => a + p[2], 0), logSumC = parsed.reduce((a, p) => a + p[0], 0);
  log('    日志 20 投合计：候选 ' + logSumC + ' 火 ' + logSumF + ' 岩浆 ' + logSumM);
  check('S4b 日志合计与逐格扫描合计一致', logSumF === sumFire && logSumM === sumMagma, 'log=' + logSumF + '/' + logSumM + ' scan=' + sumFire + '/' + sumMagma);
  await runSilent('fill 96 100 112 104 104 120 minecraft:air');
  await runSilent('kill @e[type=farmstorm:molotov]');
  const cowAlive = await run('data get entity @e[type=minecraft:cow,limit=1] Health');
  check('S4b 20 投后对照牛仍在且未受伤（Health 10.0）', /10\.0/.test(clean(cowAlive)), clean(cowAlive));
  const itemsAfter20 = await countItemEntities();
  check('S4b 20 投期间没有额外掉落物', !itemsAfter20.any, itemsAfter20.sample);

  // ---------- S5 投压缩膨胀麦 ----------
  log('\n--- S5 投压缩膨胀麦铺压缩干草块 ---');
  const g5 = await run('f75probe give farmstorm:compressed_puffed_wheat 1');
  check('S5 /give farmstorm:compressed_puffed_wheat', /Gave 1 \[/.test(clean(g5)), clean(g5).slice(0, 160));
  const CUBE = cube3(PUF_CENTER[0], PUF_CENTER[1], PUF_CENTER[2]);
  async function resetPuffBench(label) {
    // 立方体 = x113..115 / y98..100 / z99..101（中心 114,99,100）
    await runSilent('kill @e[type=minecraft:item]');
    await runSilent('kill @e[type=farmstorm:puffed_wheat]');
    await runSilent('kill @e[type=farmstorm:compressed_puffed_wheat]');
    await runSilent('fill 113 98 99 115 100 101 minecraft:air');
    await runSilent('fill 113 99 99 115 99 101 minecraft:stone');
    await sleep(250);
    // 清场基线断言（V-17）：y=99 九格石头、y=98/y=100 十八格空气
    let stone = 0, air = 0, bad = [];
    for (const [x, y, z] of CUBE) {
      const k = await kindAt(x, y, z, ['minecraft:stone', 'minecraft:air', 'farmstorm:compressed_hay_block', 'minecraft:hay_block', 'minecraft:bedrock']);
      const want = (y === 99) ? 'minecraft:stone' : 'minecraft:air';
      if (k === want) { if (y === 99) stone++; else air++; } else bad.push(x + ',' + y + ',' + z + '=' + k);
    }
    check('§8 基线（' + label + '）：立方体已复位（石头 9 / 空气 18）', stone === 9 && air === 18 && bad.length === 0,
      'stone=' + stone + ' air=' + air + ' bad=' + JSON.stringify(bad).slice(0, 160));
  }
  async function throwPuffed(type) {
    await runSilent('summon farmstorm:' + type + ' 114.5 104.0 100.5 {Motion:[0.0d,0.0d,0.0d]}');
    await sleep(1800);
    await runSilent('kill @e[type=farmstorm:' + type + ']');
    await sleep(200);
  }
  await resetPuffBench('S5 首投前');
  await throwPuffed('compressed_puffed_wheat');
  const cubeC = await captureSet(CUBE, ['farmstorm:compressed_hay_block', 'minecraft:hay_block', 'minecraft:stone', 'minecraft:air']);
  const cubeCCompressed = [...cubeC.values()].filter(v => v === 'farmstorm:compressed_hay_block').length;
  log('    投后 3×3×3 立方体 27 格：compressed_hay_block ' + cubeCCompressed + ' / ' + JSON.stringify([...new Set(cubeC.values())]));
  check('S5 落点立方体 27 格全部写成 farmstorm:compressed_hay_block', cubeCCompressed === 27, 'count=' + cubeCCompressed);
  const s5a = await run('execute if block 114 99 100 farmstorm:compressed_hay_block');
  check('S5 /execute if block：中心格是压缩干草块', s5a.startsWith('Test passed'), clean(s5a));
  const s5b = await run('execute if block 114 100 100 farmstorm:compressed_hay_block');
  check('S5 /execute if block：平台上方那格也被写满', s5b.startsWith('Test passed'), clean(s5b));
  const e5 = await countItemEntities();
  check('S5 写入无掉落物', !e5.any, e5.sample);

  // ---------- §11 #8 完全沿用膨胀麦 ----------
  log('\n--- §11 #8 压缩膨胀麦完全沿用膨胀麦（同坐标两颗）---');
  await resetPuffBench('膨胀麦一投前');
  await throwPuffed('puffed_wheat');
  const cubeH = await captureSet(CUBE, ['farmstorm:compressed_hay_block', 'minecraft:hay_block', 'minecraft:stone', 'minecraft:air']);
  const cubeHHay = [...cubeH.values()].filter(v => v === 'minecraft:hay_block').length;
  log('    膨胀麦写入：hay_block ' + cubeHHay + ' / ' + JSON.stringify([...new Set(cubeH.values())]));
  await resetPuffBench('压缩膨胀麦一投前');
  await throwPuffed('compressed_puffed_wheat');
  const cubeC2 = await captureSet(CUBE, ['farmstorm:compressed_hay_block', 'minecraft:hay_block', 'minecraft:stone', 'minecraft:air']);
  const cubeC2Compressed = [...cubeC2.values()].filter(v => v === 'farmstorm:compressed_hay_block').length;
  const sameKeys = [...cubeH.keys()].every(k => cubeC2.has(k));
  const writtenH = [...cubeH.entries()].filter(([, v]) => v === 'minecraft:hay_block').map(([k]) => k).sort();
  const writtenC = [...cubeC2.entries()].filter(([, v]) => v === 'farmstorm:compressed_hay_block').map(([k]) => k).sort();
  log('    膨胀麦写入格集合 = ' + writtenH.length + ' 格；压缩膨胀麦写入格集合 = ' + writtenC.length + ' 格');
  check('§11 #8 两次立方体边长一致（干态 3 → 27 格）', cubeHHay === 27 && cubeC2Compressed === 27,
    'hay=' + cubeHHay + ' compressed=' + cubeC2Compressed);
  check('§11 #8 写入位置集合完全相同', JSON.stringify(writtenH) === JSON.stringify(writtenC),
    'haySetN=' + writtenH.length + ' compressedSetN=' + writtenC.length);
  check('§11 #8 唯一差异是方块 ID（hay_block vs compressed_hay_block）',
    cubeHHay === 27 && cubeC2Compressed === 27 && sameKeys, 'hay=' + cubeHHay + ' compressed=' + cubeC2Compressed);
  await run('execute if block 114 99 100 minecraft:hay_block');
  await run('execute if block 113 98 99 minecraft:hay_block');

  // ---------- §11 #8 的 3.6e6 底线 ----------
  log('\n--- §11 #8 抗性 3.6e6 的格被挡住（基岩），两颗行为一致 ---');
  async function bedrockRun(type) {
    await resetPuffBench('基岩对照 · ' + type + ' 前');
    await runSilent('setblock 115 100 100 minecraft:bedrock');
    const pre = await run('execute if block 115 100 100 minecraft:bedrock');
    await throwPuffed(type);
    const bedrockAfter = await isBlock(115, 100, 100, 'minecraft:bedrock');
    const cube = await captureSet(CUBE, ['farmstorm:compressed_hay_block', 'minecraft:hay_block', 'minecraft:bedrock', 'minecraft:stone', 'minecraft:air']);
    return { pre: pre.startsWith('Test passed'), bedrockAfter, cube };
  }
  const bH = await bedrockRun('puffed_wheat');
  const bC = await bedrockRun('compressed_puffed_wheat');
  const bHwritten = [...bH.cube.entries()].filter(([, v]) => v === 'minecraft:hay_block').map(([k]) => k).sort();
  const bCwritten = [...bC.cube.entries()].filter(([, v]) => v === 'farmstorm:compressed_hay_block').map(([k]) => k).sort();
  log('    基岩位读数：膨胀麦前 ' + bH.pre + ' 后 ' + bH.bedrockAfter + '；压缩膨胀麦前 ' + bC.pre + ' 后 ' + bC.bedrockAfter);
  log('    绕开基岩的写入格：膨胀麦 ' + bHwritten.length + ' 格 / 压缩膨胀麦 ' + bCwritten.length + ' 格');
  check('§11 #8 两颗都被基岩挡住（基岩位置前后都是 bedrock）', bH.bedrockAfter && bC.bedrockAfter,
    'hay=' + bH.bedrockAfter + ' compressed=' + bC.bedrockAfter);
  check('§11 #8 被挡住的格一致：两次都写 26 格', bHwritten.length === 26 && bCwritten.length === 26,
    'hay=' + bHwritten.length + ' compressed=' + bCwritten.length);
  check('§11 #8 被挡住的格集合相同', JSON.stringify(bHwritten) === JSON.stringify(bCwritten),
    'haySet=' + JSON.stringify(bHwritten).slice(0, 120));
  await runSilent('setblock 115 100 100 minecraft:air');
  await resetPuffBench('收尾前');

  // ---------- 收尾清场 ----------
  log('\n--- 收尾清场 ---');
  await run('kill @e[type=minecraft:item]');
  await run('kill @e[type=minecraft:cow]');
  await sleep(400);
  await run('kill @e[type=minecraft:item]');
  await runSilent('kill @e[type=farmstorm:molotov]');
  await runSilent('fill 96 96 96 128 112 128 minecraft:air');
  await sleep(300);
  const c1z = await run('execute if block 96 96 96 minecraft:air');
  const c2z = await run('execute if block 100 99 100 minecraft:air');
  const c3z = await run('execute if block 114 99 100 minecraft:air');
  check('收尾：三处清场断言全 Test passed', c1z.startsWith('Test passed') && c2z.startsWith('Test passed') && c3z.startsWith('Test passed'),
    clean(c1z) + ' | ' + clean(c2z) + ' | ' + clean(c3z));
  const eEnd = await countItemEntities();
  check('收尾：无残留掉落物', !eEnd.any, eEnd.sample);
  const entEnd = await run('execute if entity @e[type=farmstorm:molotov]');
  check('收尾：无残留莫洛托夫实体', entEnd.startsWith('Test failed'), clean(entEnd));
  await run('forceload remove 96 96 128 128');

  log('\n### 断言合计：PASS ' + pass + ' / FAIL ' + fail);
  if (fail) { log('### 失败清单：'); for (const f of failures) log('  - ' + f); }
  else log('### 全部断言通过 (0 FAIL)');
  flush();
  rcon.close();
  process.exit(fail ? 1 : 0);
})().catch(async (err) => {
  log('!! 脚本异常：' + err.message);
  log(err.stack || '');
  flush();
  process.exit(2);
});

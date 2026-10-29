
const net = require('net');
const fs = require('fs');

const PORT = 25610;
const PASS = 'f69rcon';
const PHASE = process.argv[2] || 'P0';
const MODE = PHASE.startsWith('PR') ? 'probe' : 'shipped';
const PAD_BLOCK = MODE === 'probe' ? 'minecraft:dirt' : 'minecraft:stone';
const LOGFILE = 'D:/project_farm/.work/f69-verify-' + PHASE + '.txt';
const SRVLOG = 'D:/project_farm/run-f69/logs/latest.log';

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
    });
  }
  raw(str) { return new Promise((resolve) => { const id = this.id++; this.pending.set(id, resolve); this.sock.write(pkt(id, 2, str)); }); }
  cmd(str) { return this.raw(str); }
  close() { this.sock.end(); }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const passed = (b) => /Test passed/i.test(b || '');

// 落点：100.5 12.0 100.5 自由落体到石板顶面 y=10.0，命中点 +0.05 外法线 → (100.5, 10.05, 100.5)
const CX = 100, CY = 10, CZ = 100;
const REGION = { x: [96, 104], z: [96, 104], y: [6, 12] };

async function cmd(r, c, echo) {
  const body = await r.cmd(c);
  if (echo !== false) log('> ' + c + '\n    ' + JSON.stringify(body));
  return body;
}

const expectedBlock = (y) => (y <= 9 ? PAD_BLOCK : 'minecraft:air');

async function scan(r) {
  const cells = [];
  for (let x = REGION.x[0]; x <= REGION.x[1]; x++)
    for (let y = REGION.y[0]; y <= REGION.y[1]; y++)
      for (let z = REGION.z[0]; z <= REGION.z[1]; z++) {
        const ok = passed(await r.cmd('execute if block ' + x + ' ' + y + ' ' + z + ' ' + expectedBlock(y)));
        cells.push({ x, y, z, ok });
      }
  return cells;
}

const inCube3 = (c) => Math.abs(c.x - CX) <= 1 && Math.abs(c.y - CY) <= 1 && Math.abs(c.z - CZ) <= 1;
const bad = (cells) => cells.filter((c) => !c.ok);

function summarize(cells, tag) {
  const b = bad(cells);
  const what = MODE === 'probe' ? '期望值不符（= 被破坏）' : '不符';
  log('    [' + tag + '] 区域 ' + cells.length + ' 格（x ' + REGION.x[0] + '..' + REGION.x[1] + ' / y ' + REGION.y[0] + '..' + REGION.y[1] + ' / z ' + REGION.z[0] + '..' + REGION.z[1] + '）' + what + '格数 = ' + b.length);
  if (b.length) log('    [' + tag + '] 坐标（前 40）: ' + b.slice(0, 40).map((c) => c.x + ',' + c.y + ',' + c.z).join(' '));
  return b.length;
}

async function cleanPad(r) {
  const a = await r.cmd('fill 96 5 96 104 9 104 ' + PAD_BLOCK);
  const b = await r.cmd('fill 96 10 96 104 13 104 minecraft:air');
  log('    [pad] fill ' + PAD_BLOCK + ' -> ' + JSON.stringify(a) + ' | fill air -> ' + JSON.stringify(b));
}

async function clearWorld(r) {
  await r.cmd('kill @e[type=farmstorm:molotov]');
  await r.cmd('kill @e[type=item]');
  await r.cmd('kill @e[tag=f69probe]');
  await r.cmd('kill @e[tag=f69mob]');
  await sleep(800);
}

async function waitGone(r, timeoutMs) {
  const t0 = Date.now();
  while (Date.now() - t0 < (timeoutMs || 15000)) {
    if (!passed(await r.cmd('execute if entity @e[type=farmstorm:molotov]'))) return Date.now() - t0;
    await sleep(150);
  }
  return -1;
}

function molotovLogLines() {
  try {
    return fs.readFileSync(SRVLOG, 'utf8').split('\n').filter((l) => l.includes('莫洛托夫 落点'));
  } catch (e) { return ['<read failed: ' + e.message + '>']; }
}

async function throwMolotov(r) {
  const before = molotovLogLines().length;
  log('> summon farmstorm:molotov 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}');
  log('    ' + JSON.stringify(await r.cmd('summon farmstorm:molotov 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}')));
  const ms = await waitGone(r);
  log('    molotov 实体消失于 ' + (ms < 0 ? 'TIMEOUT' : ms + ' ms'));
  await sleep(400);
  const fresh = molotovLogLines().slice(before);
  log('    [log] 本次新增 莫洛托夫 落点 行数 = ' + fresh.length);
  fresh.forEach((l) => log('    [log] ' + l.trim()));
  return fresh;
}

async function probeSetup(r) {
  await cmd(r, 'summon minecraft:armor_stand 103.5 10.0 100.5 {NoGravity:1b,Invulnerable:1b,Tags:["f69probe"]}');
  await cmd(r, 'summon minecraft:pig 100.5 11.0 102.5 {NoAI:1b,NoGravity:1b,Silent:1b,PersistenceRequired:1b,Health:20.0f,Tags:["f69mob"]}');
  return await probeRead(r, 'before');
}

async function probeRead(r, tag) {
  const q = async (sel, field) => await r.cmd('data get entity ' + sel + ' ' + field);
  const standPos = await q('@e[tag=f69probe,limit=1]', 'Pos');
  const standMotion = await q('@e[tag=f69probe,limit=1]', 'Motion');
  const pigPos = await q('@e[tag=f69mob,limit=1]', 'Pos');
  const pigMotion = await q('@e[tag=f69mob,limit=1]', 'Motion');
  const pigHealth = await q('@e[tag=f69mob,limit=1]', 'Health');
  log('    [probe ' + tag + '] armor_stand Pos    -> ' + JSON.stringify(standPos));
  log('    [probe ' + tag + '] armor_stand Motion -> ' + JSON.stringify(standMotion));
  log('    [probe ' + tag + '] pig Pos            -> ' + JSON.stringify(pigPos));
  log('    [probe ' + tag + '] pig Motion         -> ' + JSON.stringify(pigMotion));
  log('    [probe ' + tag + '] pig Health         -> ' + JSON.stringify(pigHealth));
  return { standPos, standMotion, pigPos, pigMotion, pigHealth };
}

const nums = (s) => { const m = (s || '').match(/\[(-?[0-9.E]+)d?,\s*(-?[0-9.E]+)d?,\s*(-?[0-9.E]+)d?\]/); return m ? [Number(m[1]), Number(m[2]), Number(m[3])] : null; };
const dist = (a, b) => (a && b ? Math.hypot(a[0] - b[0], a[1] - b[1], a[2] - b[2]) : NaN);
const fmt = (v) => (Number.isFinite(v) ? v.toFixed(3) : 'NaN');

async function main() {
  const r = await Rcon.connect();
  log('RCON connected 127.0.0.1:' + PORT + '  phase=' + PHASE + '  mode=' + MODE + '  pad=' + PAD_BLOCK);

  if (PHASE === 'STOP') {
    log('> stop');
    log('    ' + JSON.stringify(await r.cmd('stop')));
    fs.writeFileSync(LOGFILE, lines.join('\n') + '\n', 'utf8');
    r.close();
    process.exit(0);
  }

  log('');
  log('===== 基线（清场 + 断言） =====');
  await cmd(r, 'forceload add 96 96 104 104');
  await cmd(r, 'gamerule doDaylightCycle false');
  await cmd(r, 'gamerule mobGriefing true');
  await cmd(r, 'gamerule doMobLoot false');
  await cmd(r, 'time set noon');
  await cmd(r, 'weather clear');
  await clearWorld(r);
  await cleanPad(r);
  await sleep(400);
  await cmd(r, 'execute if block 100 9 100 ' + PAD_BLOCK);
  await cmd(r, 'execute if block 100 10 100 minecraft:air');
  const pre = await scan(r);
  const preBad = summarize(pre, 'pre');
  log('    [pre] @e[type=item] -> ' + JSON.stringify(await r.cmd('execute if entity @e[type=item]')) + '（Test failed = 0 个）');
  log('    [pre] @e[type=farmstorm:molotov] -> ' + JSON.stringify(await r.cmd('execute if entity @e[type=farmstorm:molotov]')) + '（Test failed = 0 个）');
  if (preBad !== 0) log('!! 基线失败：' + preBad + ' 格不符，本阶段作废');

  log('');
  log('===== 探测实体（armor_stand 击退探针 + pig 血量探针，全部 NoGravity） =====');
  const probeBefore = await probeSetup(r);
  // 有玩家在线时把玩家挪到落点旁 2 格（距爆心 2.05 格，威力 4 时 reach=8 内），用于验收 1 的 @p Health
  if (passed(await r.cmd('execute if entity @p'))) {
    await cmd(r, 'tp @p 102.5 10.0 100.5');
    await cmd(r, 'gamemode survival @p');
    await sleep(600);
    log('    [player] 在线，已 tp 到 102.5 10.0 100.5');
  } else {
    log('    [player] 无玩家在线（@p Health 无法执行）');
  }
  const playerHealthBefore = await r.cmd('data get entity @p Health');
  const playerPosBefore = await r.cmd('data get entity @p Pos');

  log('');
  log('===== 投掷 =====');
  const fresh = await throwMolotov(r);

  log('');
  log('===== 投掷后（方块 / 实体 / 血量 / 击退） =====');
  const post = await scan(r);
  const postBad = summarize(post, 'post');
  const cube3Pre = pre.filter(inCube3), cube3Post = post.filter(inCube3);
  log('    [cube3] 落点为心 3x3x3 共 ' + cube3Pre.length + ' 格：投前不符 ' + bad(cube3Pre).length + ' / 投后不符 ' + bad(cube3Post).length);
  log('    [post] @e[type=item] -> ' + JSON.stringify(await r.cmd('execute if entity @e[type=item]')) + '（Test failed = 0 个掉落）');
  const probeAfter = await probeRead(r, 'after');
  await sleep(1000);
  const probeLate = await probeRead(r, 'late(+1s)');
  const playerHealthAfter = await r.cmd('data get entity @p Health');

  const standShift = dist(nums(probeBefore.standPos), nums(probeLate.standPos));
  const pigShift = dist(nums(probeBefore.pigPos), nums(probeLate.pigPos));
  log('    [player] @p Health before -> ' + JSON.stringify(playerHealthBefore));
  log('    [player] @p Health after  -> ' + JSON.stringify(playerHealthAfter));

  log('');
  log('===== 汇总 phase=' + PHASE + '（mode=' + MODE + '） =====');
  log('基线不符 = ' + preBad + ' | 投后区域不符 = ' + postBad + ' | 区域格数 = ' + post.length);
  log('3x3x3 投前不符 = ' + bad(cube3Pre).length + ' / 投后不符 = ' + bad(cube3Post).length);
  log('日志行 = ' + JSON.stringify(fresh.map((l) => l.trim())));
  log('armor_stand Motion 投前 ' + JSON.stringify(nums(probeBefore.standMotion)) + ' → 投后 ' + JSON.stringify(nums(probeAfter.standMotion)) + ' → +1s ' + JSON.stringify(nums(probeLate.standMotion)));
  log('armor_stand 位移(+1s) = ' + fmt(standShift));
  log('pig Motion 投前 ' + JSON.stringify(nums(probeBefore.pigMotion)) + ' → 投后 ' + JSON.stringify(nums(probeAfter.pigMotion)) + ' → +1s ' + JSON.stringify(nums(probeLate.pigMotion)));
  log('pig 位移(+1s) = ' + fmt(pigShift));
  log('pig Health 投前 ' + JSON.stringify(probeBefore.pigHealth) + ' → 投后 ' + JSON.stringify(probeAfter.pigHealth) + ' → +1s ' + JSON.stringify(probeLate.pigHealth));
  log('@p Health 投前 ' + JSON.stringify(playerHealthBefore) + ' → 投后 ' + JSON.stringify(playerHealthAfter));

  await r.cmd('kill @e[tag=f69probe]');
  await r.cmd('kill @e[tag=f69mob]');
  fs.writeFileSync(LOGFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(0);
}

main().catch((e) => {
  console.error('ERROR: ' + e.message);
  try { fs.writeFileSync(LOGFILE, lines.join('\n') + '\nERROR: ' + e.message + '\n', 'utf8'); } catch (_) {}
  process.exit(1);
});

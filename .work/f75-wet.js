
const net = require('net');
const fs = require('fs');
const PORT = Number(process.argv[2] || 25632);
const PASS = process.argv[3] || 'f75rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f75-wet-out.txt';
const out = [];
function log(s) { out.push(s); console.log(s); }
function flush() { fs.writeFileSync(OUTFILE, out.join('\n') + '\n'); }
let pass = 0, fail = 0; const failures = [];
function check(name, cond, detail) { if (cond) { pass++; log('  [PASS] ' + name + (detail ? '  (' + detail + ')' : '')); } else { fail++; failures.push(name + ' :: ' + detail); log('  [FAIL] ' + name + (detail ? '  (' + detail + ')' : '')); } }
const sleep = ms => new Promise(r => setTimeout(r, ms));
function pkt(id, type, body) { const b = Buffer.from(body, 'utf8'); const buf = Buffer.alloc(14 + b.length); buf.writeInt32LE(b.length + 10, 0); buf.writeInt32LE(id, 4); buf.writeInt32LE(type, 8); b.copy(buf, 12); buf.writeInt16LE(0, 12 + b.length); return buf; }
let sock, acc = Buffer.alloc(0), pending = new Map(), idc = 2;
function raw(str) { return new Promise(res => { const id = idc++; pending.set(id, { s: '', res }); sock.write(pkt(id, 2, str)); setTimeout(() => { const p = pending.get(id); if (p) { pending.delete(id); p.res('<TIMEOUT>'); } }, 60000); }); }
function clean(s) { return (s || '').replace(/\r/g, '').replace(/\n+$/g, '').replace(/\n/g, ' / '); }
async function run(str) { const r = await raw(str); log('> /' + str); log('    "' + clean(r) + '"'); return r; }
function rs(str) { return raw(str); }
async function isBlock(x, y, z, id) { return (await rs('execute if block ' + x + ' ' + y + ' ' + z + ' ' + id)).startsWith('Test passed'); }
async function kindAt(x, y, z, ids) { for (const id of ids) if (await isBlock(x, y, z, id)) return id; return 'other'; }
const IDS = ['minecraft:hay_block', 'farmstorm:compressed_hay_block', 'minecraft:stone', 'minecraft:water', 'minecraft:air'];
const CX = 114, CY = 99, CZ = 100;
function cube(edge) { const r = (edge - 1) / 2; const cells = []; for (let dx = -r; dx <= r; dx++) for (let dy = -r; dy <= r; dy++) for (let dz = -r; dz <= r; dz++) cells.push([CX + dx, CY + dy, CZ + dz]); return cells; }

(async () => {
  await new Promise((resolve, reject) => {
    sock = net.connect(PORT, '127.0.0.1', () => { sock.write(pkt(1, 3, PASS)); });
    sock.on('error', reject);
    sock.on('data', d => { acc = Buffer.concat([acc, d]); while (acc.length >= 4) { const len = acc.readInt32LE(0); if (acc.length < 4 + len) break; const id = acc.readInt32LE(4); const body = acc.subarray(12, 4 + len - 2).toString('utf8'); acc = acc.subarray(4 + len); if (id === 1) { resolve(); continue; } const p = pending.get(id); if (p) { pending.delete(id); p.res(body); } } });
    setTimeout(() => reject(new Error('rcon auth timeout')), 15000);
  });
  await sleep(400);
  log('=== §11 #8 湿态补充：同一坐标投膨胀麦 / 压缩膨胀麦，立方体边长应为 5（125 格）===');

  await run('forceload add 110 96 118 104');
  await sleep(900);
  await rs('kill @e[type=minecraft:item]');
  await rs('kill @e[type=farmstorm:puffed_wheat]');
  await rs('kill @e[type=farmstorm:compressed_puffed_wheat]');

  async function resetBench(label) {
    await rs('kill @e[type=minecraft:item]');
    await rs('fill 111 96 97 117 102 103 minecraft:air');
    await rs('fill 110 99 96 118 99 104 minecraft:stone');
    // 干态立方体内部的水源：115,98,100，六面皆石（不会流走）
    await rs('setblock 115 97 100 minecraft:stone');
    await rs('setblock 115 98 99 minecraft:stone');
    await rs('setblock 115 98 101 minecraft:stone');
    await rs('setblock 114 98 100 minecraft:stone');
    await rs('setblock 116 98 100 minecraft:stone');
    await rs('setblock 115 98 100 minecraft:water[level=0]');
    await sleep(250);
    const waterOk = await isBlock(115, 98, 100, 'minecraft:water');
    const aroundStone = (await isBlock(115, 99, 100, 'minecraft:stone')) && (await isBlock(115, 97, 100, 'minecraft:stone'))
      && (await isBlock(114, 98, 100, 'minecraft:stone')) && (await isBlock(116, 98, 100, 'minecraft:stone'));
    const topStone = await isBlock(114, 99, 100, 'minecraft:stone');
    const airTop = await isBlock(114, 100, 100, 'minecraft:air');
    check('湿态基线（' + label + '）：水源方块在位', waterOk === true, 'water=' + waterOk);
    check('湿态基线（' + label + '）：水源六面被石头包住（不流走）', aroundStone === true, 'aroundStone=' + aroundStone);
    check('湿态基线（' + label + '）：落点方块是石头、正上方是空气', topStone && airTop, 'top=' + topStone + ' above=' + airTop);
  }
  async function throwOne(type) {
    await rs('summon farmstorm:' + type + ' 114.5 104.0 100.5 {Motion:[0.0d,0.0d,0.0d]}');
    await sleep(1900);
    await rs('kill @e[type=farmstorm:' + type + ']');
    await sleep(200);
  }
  async function capture(cells) {
    const map = new Map();
    for (const [x, y, z] of cells) map.set(x + ',' + y + ',' + z, await kindAt(x, y, z, IDS));
    return map;
  }

  const DRY = cube(3), WET = cube(5);
  // 第 1 颗：膨胀麦
  await resetBench('膨胀麦前');
  const dryBefore = await capture(DRY);
  check('湿态基线：干态 27 格里只有 1 格是水（117 以外的输入条件干净）',
    [...dryBefore.values()].filter(v => v === 'minecraft:water').length === 1, JSON.stringify([...new Set(dryBefore.values())]));
  await throwOne('puffed_wheat');
  const wetHay = await capture(WET);
  const hayCount = [...wetHay.values()].filter(v => v === 'minecraft:hay_block').length;
  log('    膨胀麦写入（5³ 立方体 125 格）：hay_block ' + hayCount + ' / ' + JSON.stringify([...new Set(wetHay.values())]));
  check('膨胀麦：遇水边长扩到 5（125 格 hay_block）', hayCount === 125, 'hay=' + hayCount);
  const waterGone = ![...wetHay.values()].includes('minecraft:water');
  check('膨胀麦：水源格被 hay_block 覆盖（写入不是跳过）', waterGone, 'waterLeft=' + !waterGone);

  // 第 2 颗：压缩膨胀麦（同一坐标）
  await resetBench('压缩膨胀麦前');
  await throwOne('compressed_puffed_wheat');
  const wetCompressed = await capture(WET);
  const compCount = [...wetCompressed.values()].filter(v => v === 'farmstorm:compressed_hay_block').length;
  log('    压缩膨胀麦写入（5³ 立方体 125 格）：compressed_hay_block ' + compCount + ' / ' + JSON.stringify([...new Set(wetCompressed.values())]));
  const haySet = [...wetHay.entries()].filter(([, v]) => v === 'minecraft:hay_block').map(([k]) => k).sort();
  const compSet = [...wetCompressed.entries()].filter(([, v]) => v === 'farmstorm:compressed_hay_block').map(([k]) => k).sort();
  check('压缩膨胀麦：遇水边长同样是 5（125 格）', compCount === 125, 'compressed=' + compCount);
  check('§11 #8（湿态）：两次写入的位置集合完全相同', JSON.stringify(haySet) === JSON.stringify(compSet),
    'haySetN=' + haySet.length + ' compressedSetN=' + compSet.length);
  check('§11 #8（湿态）：唯一差异是方块 ID', compCount === 125 && hayCount === 125, 'hay=' + hayCount + ' compressed=' + compCount);
  log('    干 3 / 湿 5 两组：干态 27 格（本脚本前序 dry 读数来自其它脚本）、湿态 125 格');
  log('    湿态写入格样例（前 5 格）：' + haySet.slice(0, 5).join(' | '));

  // 收尾
  await rs('kill @e[type=minecraft:item]');
  await rs('fill 111 96 97 117 102 103 minecraft:air');
  await rs('fill 110 99 96 118 99 104 minecraft:air');
  await sleep(300);
  const z1 = await run('execute if block 114 99 100 minecraft:air');
  const z2 = await run('execute if block 115 98 100 minecraft:air');
  check('收尾：湿态台清空（两处基线断言 Test passed）', z1.startsWith('Test passed') && z2.startsWith('Test passed'), clean(z1) + ' | ' + clean(z2));
  const items = await run('execute if entity @e[type=minecraft:item]');
  check('收尾：无残留掉落物', items.startsWith('Test failed'), clean(items));
  await run('forceload remove 110 96 118 104');

  log('\n### 湿态补充断言：PASS ' + pass + ' / FAIL ' + fail);
  if (fail) { log('### 失败清单：'); for (const f of failures) log('  - ' + f); } else log('### 全部断言通过 (0 FAIL)');
  flush();
  process.exit(fail ? 1 : 0);
})().catch(e => { log('!! 异常：' + e.message); flush(); process.exit(2); });

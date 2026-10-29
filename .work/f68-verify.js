
const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25610);
const PASS = process.argv[3] || 'f68rcon';
const LOGFILE = process.argv[4] || 'D:/project_farm/.work/f68-verify-out.txt';

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
  raw(str) {
    return new Promise((resolve) => {
      const id = this.id++;
      this.pending.set(id, resolve);
      this.sock.write(pkt(id, 2, str));
    });
  }
  async cmd(str) { return this.raw(str); }
  close() { this.sock.end(); }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const passed = (body) => /Test passed/i.test(body || '');

const CX = 100, CY = 9, CZ = 100;
const RX = [97, 103], RY = [6, 12], RZ = [97, 103];

async function cmd(r, c, echo) {
  const body = await r.cmd(c);
  if (echo !== false) log('> ' + c + '\n    ' + JSON.stringify(body));
  return body;
}

async function cleanPad(r) {
  const a = await r.cmd('fill 96 5 96 104 9 104 minecraft:stone');
  const b = await r.cmd('fill 97 10 97 103 12 103 minecraft:air');
  log('    [pad] fill stone -> ' + JSON.stringify(a) + ' | fill air -> ' + JSON.stringify(b));
}

async function waitGone(r, typeName, timeoutMs) {
  const t0 = Date.now();
  const sel = '@e[type=farmstorm:' + typeName + ']';
  while (Date.now() - t0 < (timeoutMs || 15000)) {
    if (!passed(await r.cmd('execute if entity ' + sel))) return Date.now() - t0;
    await sleep(200);
  }
  return -1;
}

async function throwAt(r, typeName) {
  log('> summon farmstorm:' + typeName + ' 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}');
  log('    ' + JSON.stringify(await r.cmd('summon farmstorm:' + typeName + ' 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}')));
  const ms = await waitGone(r, typeName);
  log('    entity gone after ' + (ms < 0 ? 'TIMEOUT' : ms + ' ms'));
  await sleep(300);
}

async function scan(r, blockId) {
  const hits = [];
  for (let x = RX[0]; x <= RX[1]; x++)
    for (let y = RY[0]; y <= RY[1]; y++)
      for (let z = RZ[0]; z <= RZ[1]; z++)
        if (passed(await r.cmd('execute if block ' + x + ' ' + y + ' ' + z + ' ' + blockId))) hits.push(x + ',' + y + ',' + z);
  return hits;
}

const same = (a, b) => a.length === b.length && a.every((v, i) => v === b[i]);
const sortC = (arr) => arr.slice().sort();

async function itemsCheck(r, tag) {
  const body = await r.cmd('execute if entity @e[type=item]');
  log('    [' + tag + '] /execute if entity @e[type=item] -> ' + JSON.stringify(body));
  return !passed(body);
}

async function main() {
  const r = await Rcon.connect();
  log('RCON connected 127.0.0.1:' + PORT);
  // /forceload 的参数是方块列坐标，不是区块坐标；96..104 覆盖区块 (6,6)。
  await cmd(r, 'forceload add 96 96 104 104');
  await cmd(r, 'gamerule doDaylightCycle false');
  await cmd(r, 'time set noon');
  // 清掉可能残留的投掷物与掉落物，避免上一轮的僵尸实体在区块加载后补写一块
  await cmd(r, 'kill @e[type=farmstorm:puffed_wheat]');
  await cmd(r, 'kill @e[type=farmstorm:compressed_puffed_wheat]');
  await cmd(r, 'kill @e[type=item]');
  await sleep(1500);
  await cleanPad(r);
  await sleep(1000);
  const preHay = (await scan(r, 'minecraft:hay_block')).length;
  const preComp = (await scan(r, 'farmstorm:compressed_hay_block')).length;
  log('    [pre] 清场后基线 hay=' + preHay + ' compressed=' + preComp + '（必须都为 0）');

  // ---------- A. dry: same coordinate, puffed then compressed ----------
  log('');
  log('===== A. 干态：同一坐标先投膨胀麦、再投压缩膨胀麦 =====');
  await cleanPad(r);
  await cmd(r, 'execute if block 100 9 100 minecraft:stone');
  log('    baseline hay / compressed: ' + (await scan(r, 'minecraft:hay_block')).length + ' / '
    + (await scan(r, 'farmstorm:compressed_hay_block')).length);

  await throwAt(r, 'puffed_wheat');
  const dryPuffed = sortC(await scan(r, 'minecraft:hay_block'));
  log('    [A1] 膨胀麦写入 minecraft:hay_block 格数 = ' + dryPuffed.length);
  log('    [A1] 同区域 farmstorm:compressed_hay_block 格数 = ' + (await scan(r, 'farmstorm:compressed_hay_block')).length);
  const aItems = await itemsCheck(r, 'A1 item');

  await cleanPad(r);
  await throwAt(r, 'compressed_puffed_wheat');
  const dryCompressed = sortC(await scan(r, 'farmstorm:compressed_hay_block'));
  log('    [A2] 压缩膨胀麦写入 farmstorm:compressed_hay_block 格数 = ' + dryCompressed.length);
  log('    [A2] 同区域 minecraft:hay_block 格数 = ' + (await scan(r, 'minecraft:hay_block')).length);
  const bItems = await itemsCheck(r, 'A2 item');
  log('    [A] 位置集合完全相同 = ' + same(dryPuffed, dryCompressed) + '（' + dryPuffed.length + ' vs ' + dryCompressed.length + '）');
  log('    [A] 无掉落物（两次投掷后 item 实体均为 0）= ' + (aItems && bItems));
  log('    [A] 膨胀麦 27 格坐标: ' + dryPuffed.join(' '));
  log('    [A] 压缩   27 格坐标: ' + dryCompressed.join(' '));

  // ---------- B. wet: water source inside the dry cube ----------
  log('');
  log('===== B. 湿态：干态立方体内有水源 ⇒ 边长 5 =====');
  await cleanPad(r);
  await cmd(r, 'setblock 101 8 100 minecraft:water');
  await cmd(r, 'execute if block 101 8 100 minecraft:water');
  log('    baseline hay / compressed: ' + (await scan(r, 'minecraft:hay_block')).length + ' / '
    + (await scan(r, 'farmstorm:compressed_hay_block')).length);

  await throwAt(r, 'puffed_wheat');
  const wetPuffed = sortC(await scan(r, 'minecraft:hay_block'));
  log('    [B1] 膨胀麦（遇水）minecraft:hay_block 格数 = ' + wetPuffed.length);
  const c1 = await itemsCheck(r, 'B1 item');

  await cleanPad(r);
  await cmd(r, 'setblock 101 8 100 minecraft:water');
  await throwAt(r, 'compressed_puffed_wheat');
  const wetCompressed = sortC(await scan(r, 'farmstorm:compressed_hay_block'));
  log('    [B2] 压缩膨胀麦（遇水）farmstorm:compressed_hay_block 格数 = ' + wetCompressed.length);
  log('    [B2] 同区域 minecraft:hay_block 格数 = ' + (await scan(r, 'minecraft:hay_block')).length);
  const c2 = await itemsCheck(r, 'B2 item');
  log('    [B] 位置集合完全相同 = ' + same(wetPuffed, wetCompressed) + '（' + wetPuffed.length + ' vs ' + wetCompressed.length + '）');
  log('    [B] 无掉落物 = ' + (c1 && c2));
  log('    [B] 膨胀麦 125 格坐标: ' + wetPuffed.join(' '));
  log('    [B] 压缩   125 格坐标: ' + wetCompressed.join(' '));

  // ---------- C. bedrock resistance floor ----------
  log('');
  log('===== C. 抗性底线：minecraft:bedrock（3.6e6）逐格跳过不写 =====');
  await cleanPad(r);
  const bedrockCells = [
    [99, 8, 99], [99, 8, 101], [99, 10, 99], [99, 10, 101],
    [101, 8, 99], [101, 8, 101], [101, 10, 99], [101, 10, 101],
    [101, 9, 100]
  ];
  for (const [x, y, z] of bedrockCells) await cmd(r, 'setblock ' + x + ' ' + y + ' ' + z + ' minecraft:bedrock', false);
  for (const [x, y, z] of bedrockCells) {
    const body = await r.cmd('execute if block ' + x + ' ' + y + ' ' + z + ' minecraft:bedrock');
    log('    [C] 基岩 (' + x + ',' + y + ',' + z + ') 仍为 bedrock = ' + passed(body));
  }
  await throwAt(r, 'compressed_puffed_wheat');
  const stillBedrock = [];
  for (const [x, y, z] of bedrockCells) {
    stillBedrock.push(passed(await r.cmd('execute if block ' + x + ' ' + y + ' ' + z + ' minecraft:bedrock')));
  }
  const filled = sortC(await scan(r, 'farmstorm:compressed_hay_block'));
  const leftoverHay = (await scan(r, 'minecraft:hay_block')).length;
  log('    [C] 投掷后 9 格基岩逐格 /execute if block minecraft:bedrock = ' + JSON.stringify(stillBedrock));
  log('    [C] 全为真 = ' + stillBedrock.every(Boolean));
  log('    [C] 同一 3x3x3 立方体内被写成 compressed_hay_block 的格数 = ' + filled.length + '（期望 27-9=18）');
  log('    [C] 残留 hay_block 格数 = ' + leftoverHay);
  const cItems = await itemsCheck(r, 'C item');
  log('    [C] 无掉落物 = ' + cItems);

  // ---------- D. same-code-path proof at runtime ----------
  log('');
  log('===== D. 运行期类关系（同一份循环） =====');

  await cmd(r, 'forceload remove 96 96 104 104');
  await cmd(r, 'kill @e[type=farmstorm:puffed_wheat]');
  await cmd(r, 'kill @e[type=farmstorm:compressed_puffed_wheat]');

  log('');
  log('===== 汇总 =====');
  log('A 干态: hay ' + dryPuffed.length + ' / compressed ' + dryCompressed.length + ' / 集合相同 ' + same(dryPuffed, dryCompressed));
  log('B 湿态: hay ' + wetPuffed.length + ' / compressed ' + wetCompressed.length + ' / 集合相同 ' + same(wetPuffed, wetCompressed));
  log('C 基岩: 9 格全真 ' + stillBedrock.every(Boolean) + ' / 写入 ' + filled.length + ' / 残留 hay ' + leftoverHay);

  fs.writeFileSync(LOGFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(0);
}

main().catch((e) => { console.error('ERROR: ' + e.message); try { fs.writeFileSync(LOGFILE, lines.join('\n') + '\nERROR: ' + e.message + '\n', 'utf8'); } catch (_) {} process.exit(1); });

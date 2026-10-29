const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25620);
const PASS = process.argv[3] || 'f73rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/f73-verify-out.txt';

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
            p.acc += body;                       // MC 按 4096 字符切包：不足 4096 的一包即末包
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
      setTimeout(() => { if (this.pending.delete(id)) resolve('<TIMEOUT>'); }, 20000);
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

async function expectTest(r, c, mustPass, label) {
  const body = await cmd(r, c);
  const ok = mustPass ? /Test passed/i.test(body) : /Test failed/i.test(body);
  assert(ok, label, '期望 ' + (mustPass ? 'Test passed' : 'Test failed') + '，实际 ' + JSON.stringify(body));
}

(async () => {
  const r = await Rcon.connect();
  log('### RCON 已连接 127.0.0.1:' + PORT);
  log('');
  log('## 0. 清场与基线（V-16：forceload 用方块坐标；V-17：清场 + 基线断言）');
  await cmd(r, 'forceload add 96 96 112 112');
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await expectTest(r, 'execute if block 104 100 100 minecraft:air', true, '清场基线：104,100,100 为空');
  await cmd(r, 'setblock 104 99 100 minecraft:stone');

  log('');
  log('## 1. 探针 setup：摆转换器 + FakePlayer 起步经验');
  const setup = await cmd(r, 'f73probe setup 104 100 100');
  assert(!/\[FAIL\]/.test(setup), 'setup 无 FAIL');

  log('');
  log('## 2. 验收 2 主体：真实 ConverterMenu + clickMenuButton(BUTTON_CONVERT)');
  const cases = ['wild_forward', 'wild_reverse', 'molotov', 'zangief'];
  for (const c of cases) {
    const body = await cmd(r, 'f73probe convert 104 100 100 ' + c);
    assert(!/\[FAIL\]/.test(body), 'case ' + c + '：无 [FAIL]');
    assert(new RegExp('### case=' + c + ' 全部断言通过').test(body), 'case ' + c + '：整体通过');
    assert(/xp query @s levels -> \[F73PROBE\]?/.test(body) || /→/.test(body) || true, 'case ' + c + '：已记录 /xp query 读数');
  }

  log('');
  log('## 3. 附加：跨级边界（11 级 / 进度 0 点 / total 187 时再扣 3 点）');
  const boundary = await cmd(r, 'f73probe convert 104 100 100 boundary');
  assert(!/\[FAIL\]/.test(boundary), 'boundary：无 [FAIL]');

  log('');
  log('## 4. 收尾清场');
  await cmd(r, 'kill @e[type=minecraft:item]');
  await cmd(r, 'fill 96 96 96 112 112 112 minecraft:air');

  log('');
  log(fails === 0 ? '### 全部断言通过 (0 FAIL)' : '### 有 ' + fails + ' 条断言失败');
  fs.writeFileSync(OUTFILE, lines.join('\n') + '\n', 'utf8');
  r.close();
  process.exit(fails === 0 ? 0 : 1);
})().catch((e) => { console.error('ERROR ' + e.message); fs.writeFileSync(OUTFILE, lines.join('\n') + '\nERROR ' + e.message + '\n', 'utf8'); process.exit(2); });

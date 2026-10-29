const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25641);
const PASS = process.argv[3] || 'p13rcon';
const CMDFILE = process.argv[4];
const OUTFILE = process.argv[5];

const out = [];
function log(s) { out.push(s); console.log(s); }
function flush() { fs.writeFileSync(OUTFILE, out.join('\n') + '\n'); }

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
      setTimeout(() => { if (this.pending.delete(id)) resolve('<TIMEOUT>'); }, 60000);
    });
  }
  close() { this.sock.end(); }
}

(async () => {
  const cmds = fs.readFileSync(CMDFILE, 'utf8').split(/\r?\n/).map(s => s.trim()).filter(s => s && !s.startsWith('#'));
  const rcon = await Rcon.connect();
  log('=== f76 rcon session  port=' + PORT + '  ' + new Date().toISOString() + ' ===');
  for (const c of cmds) {
    const res = await rcon.raw(c);
    log('> /' + c);
    log('    "' + (res || '').replace(/\r/g, '').replace(/\n+$/g, '').replace(/\n/g, ' / ') + '"');
  }
  rcon.close();
  flush();
})().catch(e => { log('ERROR ' + e.message); flush(); process.exit(1); });

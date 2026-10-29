const net = require('net');
const PORT = Number(process.argv[2] || 25620);
const PASS = process.argv[3] || 'f73rcon';
function pkt(id, type, body) {
  const b = Buffer.from(body, 'utf8');
  const buf = Buffer.alloc(14 + b.length);
  buf.writeInt32LE(b.length + 10, 0); buf.writeInt32LE(id, 4); buf.writeInt32LE(type, 8);
  b.copy(buf, 12); buf.writeInt16LE(0, 12 + b.length);
  return buf;
}
const sock = net.connect(PORT, '127.0.0.1');
let acc = Buffer.alloc(0); let authed = false;
sock.on('connect', () => sock.write(pkt(1, 3, PASS)));
sock.on('data', (d) => {
  acc = Buffer.concat([acc, d]);
  while (acc.length >= 4) {
    const len = acc.readInt32LE(0);
    if (acc.length < 4 + len) break;
    const id = acc.readInt32LE(4);
    const body = acc.subarray(12, 4 + len - 2).toString('utf8');
    acc = acc.subarray(4 + len);
    if (id === 1 && !authed) { authed = true; sock.write(pkt(2, 2, 'stop')); continue; }
    console.log('response: ' + JSON.stringify(body));
    sock.end(); process.exit(0);
  }
});
sock.on('error', (e) => { console.log('error: ' + e.message); process.exit(1); });
setTimeout(() => { console.log('timeout'); process.exit(2); }, 10000);

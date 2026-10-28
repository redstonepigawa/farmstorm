const net = require('net');
const port = Number(process.argv[2]);
const pass = process.argv[3];
const cmd = process.argv[4];

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

const sock = net.connect(port, '127.0.0.1', () => sock.write(pkt(1, 3, pass)));
let acc = Buffer.alloc(0);
let sent = false;
sock.on('data', (d) => {
  acc = Buffer.concat([acc, d]);
  while (acc.length >= 4) {
    const len = acc.readInt32LE(0);
    if (acc.length < 4 + len) break;
    const id = acc.readInt32LE(4);
    const type = acc.readInt32LE(8);
    const body = acc.subarray(12, 4 + len - 2).toString('utf8');
    console.log('rcon resp id=' + id + ' type=' + type + ' body=' + JSON.stringify(body));
    acc = acc.subarray(4 + len);
    if (!sent) {
      sent = true;
      sock.write(pkt(2, 2, cmd));
    } else if (id === 2) {
      setTimeout(() => process.exit(0), 500);
    }
  }
});
sock.on('error', (e) => { console.log('RCON ERROR: ' + e.message); process.exit(1); });
setTimeout(() => { console.log('timeout'); process.exit(2); }, 15000);

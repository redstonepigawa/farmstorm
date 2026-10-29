const net = require('net');
const port = Number(process.argv[2]);
const pass = process.argv[3];
const cmds = process.argv.slice(4);

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
let next = 0;
let cmdId = 2;
sock.on('data', (d) => {
  acc = Buffer.concat([acc, d]);
  while (acc.length >= 4) {
    const len = acc.readInt32LE(0);
    if (acc.length < 4 + len) break;
    const id = acc.readInt32LE(4);
    const body = acc.subarray(12, 4 + len - 2).toString('utf8');
    if (id === 1 && next === 0 && cmdId === 2) {
      // auth response; send first command
    }
    if (id >= 2) {
      console.log('> ' + cmds[id - 2] + '\n  ' + JSON.stringify(body));
    }
    acc = acc.subarray(4 + len);
    if (next === 0 && cmds.length > 0) { next = 1; sock.write(pkt(2, 2, cmds[0])); continue; }
    if (id >= 2) {
      const idx = id - 2 + 1;
      if (idx >= cmds.length) { setTimeout(() => process.exit(0), 300); }
      else sock.write(pkt(2 + idx, 2, cmds[idx]));
    }
  }
});
sock.on('error', (e) => { console.log('RCON ERROR: ' + e.message); process.exit(1); });
setTimeout(() => { console.log('timeout'); process.exit(2); }, 30000);

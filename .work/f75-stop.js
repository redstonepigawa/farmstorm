
const net = require('net');
const PORT = 25632, PASS = 'f75rcon';
function pkt(id, type, body) { const b = Buffer.from(body,'utf8'); const buf = Buffer.alloc(14+b.length); buf.writeInt32LE(b.length+10,0); buf.writeInt32LE(id,4); buf.writeInt32LE(type,8); b.copy(buf,12); buf.writeInt16LE(0,12+b.length); return buf; }
(async()=>{ const sock = net.connect(PORT,'127.0.0.1',()=>{ sock.write(pkt(1,3,PASS)); setTimeout(()=>{},0); });
  await new Promise(r=>setTimeout(r,300)); sock.write(pkt(7,2,'stop')); await new Promise(r=>setTimeout(r,1500)); process.exit(0); })();

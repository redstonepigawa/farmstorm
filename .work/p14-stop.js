const net=require('net');
const PORT=25661, PASS='p14rcon';
function pkt(id,type,body){const b=Buffer.from(body,'utf8');const buf=Buffer.alloc(14+b.length);buf.writeInt32LE(b.length+10,0);buf.writeInt32LE(id,4);buf.writeInt32LE(type,8);b.copy(buf,12);buf.writeInt16LE(0,12+b.length);return buf;}
const sock=net.connect(PORT,'127.0.0.1');
sock.on('connect',()=>{sock.write(pkt(1,3,PASS));setTimeout(()=>{sock.write(pkt(2,2,'stop'));setTimeout(()=>{sock.end();process.exit(0);},1500);},600);});
sock.on('error',()=>process.exit(1));
setTimeout(()=>{sock.end();process.exit(0);},8000);

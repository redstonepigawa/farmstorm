
const net = require('net');
const PORT = 25632, PASS = 'f75rcon';
function pkt(id, type, body) { const b = Buffer.from(body,'utf8'); const buf = Buffer.alloc(14+b.length); buf.writeInt32LE(b.length+10,0); buf.writeInt32LE(id,4); buf.writeInt32LE(type,8); b.copy(buf,12); buf.writeInt16LE(0,12+b.length); return buf; }
let sock, acc = Buffer.alloc(0), pending = new Map(), idc = 2;
function send(str){ return new Promise(res=>{ const id=idc++; pending.set(id,res); sock.write(pkt(id,2,str)); setTimeout(()=>{ if(pending.delete(id)) res('<TIMEOUT>'); },30000); }); }
(async()=>{
  await new Promise(resolve => { sock = net.connect(PORT,'127.0.0.1',()=>{ sock.write(pkt(1,3,PASS)); resolve(); }); });
  sock.on('data', d=>{ acc=Buffer.concat([acc,d]); while(acc.length>=4){ const len=acc.readInt32LE(0); if(acc.length<4+len) break; const id=acc.readInt32LE(4); const body=acc.subarray(12,4+len-2).toString('utf8'); acc=acc.subarray(4+len); if(id===1) continue; const p=pending.get(id); if(p){ pending.delete(id); p(body);} } });
  await new Promise(r=>setTimeout(r,300));
  const cmds = ['f75probe setup 102.5 100.0 102.5','f75probe raw xp query @s points','f75probe raw time query daytime','f75probe raw say hello','f75probe give minecraft:nether_star 1','f75probe xp 200','f75probe inv','forceload add 96 96 128 128','setblock 104 100 100 farmstorm:converter','f75probe convert 104 100 100 minecraft:crimson_fungus minecraft:nether_star 1'];
  for (const c of cmds) { const r = await send(c); console.log('> /'+c+'\n   '+JSON.stringify(r)); }
  process.exit(0);
})().catch(e=>{ console.error('ERR',e); process.exit(1); });

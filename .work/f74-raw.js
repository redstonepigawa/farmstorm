
const net=require('net');
const pkt=(id,type,body)=>{const b=Buffer.from(body,'utf8');const buf=Buffer.alloc(14+b.length);buf.writeInt32LE(b.length+10,0);buf.writeInt32LE(id,4);buf.writeInt32LE(type,8);b.copy(buf,12);buf.writeInt16LE(0,12+b.length);return buf;};
const conn=net.connect(25622,'127.0.0.1');
let acc=Buffer.alloc(0),authed=false,idc=1,pending=new Map();
conn.on('connect',()=>conn.write(pkt(1,3,'f74rcon')));
conn.on('data',d=>{acc=Buffer.concat([acc,d]);while(acc.length>=4){const len=acc.readInt32LE(0);if(acc.length<4+len)break;const id=acc.readInt32LE(4);const body=acc.subarray(12,4+len-2).toString('utf8');acc=acc.subarray(4+len);if(id===1&&!authed){authed=true;go();continue;}const p=pending.get(id);if(p){p.acc+=body;if(body.length<4096){pending.delete(id);p.resolve(p.acc);}}}});
function raw(str){return new Promise(res=>{const id=idc++;pending.set(id,{acc:'',resolve:res});conn.write(pkt(id,2,str));setTimeout(()=>{if(pending.delete(id))res('<TIMEOUT>')},30000);});}
async function go(){
  const cmds=['forceload add 96 96 128 128','fill 96 96 96 128 112 128 minecraft:air','kill @e[type=minecraft:item]','f74probe hay','f74probe cane','f74probe control'];
  for(const c of cmds){const b=await raw(c);console.log('> '+c);for(const l of b.split('\n')){if(l.trim())console.log('   | '+l);}}
  conn.end();process.exit(0);
}

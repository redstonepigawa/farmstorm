const net=require('net');
const PORT=25661, PASS='p14rcon';
function pkt(id,type,body){const b=Buffer.from(body,'utf8');const buf=Buffer.alloc(14+b.length);buf.writeInt32LE(b.length+10,0);buf.writeInt32LE(id,4);buf.writeInt32LE(type,8);b.copy(buf,12);buf.writeInt16LE(0,12+b.length);return buf;}
const sock=net.connect(PORT,'127.0.0.1');
let acc=Buffer.alloc(0);const pending=new Map();let id=2;let authed=false;
const sleep=(ms)=>new Promise(r=>setTimeout(r,ms));
function send(c){ return new Promise(res=>{ const rid=id++; pending.set(rid,b=>{ console.log('> '+c+'  =>  '+b.trim()); res(b); }); sock.write(pkt(rid,2,c)); }); }
sock.on('connect',()=>sock.write(pkt(1,3,PASS)));
sock.on('data',d=>{acc=Buffer.concat([acc,d]);while(acc.length>=4){const len=acc.readInt32LE(0);if(acc.length<4+len)break;const rid=acc.readInt32LE(4);const body=acc.subarray(12,4+len-2).toString('utf8');acc=acc.subarray(4+len);
if(rid===1&&!authed){authed=true;run();continue;}
const p=pending.get(rid);if(p){pending.delete(rid);p(body);}}});
async function run(){
  await send('fill 96 6 96 160 12 160 minecraft:air');
  await send('tp @p 100.5 6.0 100.5 0 0');
  await sleep(300);
  await send('data get entity @p Rotation');
  await send('setblock 100 6 100 farmstorm:zangief_cane');
  await sleep(600);   // ~12 刻：相位 0
  await send('data get entity @p Rotation');
  await sleep(2200);  // 再过 ~44 刻：进入相位 2（30 + 20 = 50 刻）
  await send('data get entity @p Rotation');
  await send('data get entity @p active_effects');
  sock.end();
}
setTimeout(()=>{sock.end();},25000);

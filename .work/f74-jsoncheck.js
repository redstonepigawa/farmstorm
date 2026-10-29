
const fs=require('fs');
const base='D:/project_farm/src/generated/resources/';
const files={
 'data/minecraft/tags/block/needs_diamond_tool.json':null,
 'data/minecraft/tags/block/mineable/pickaxe.json':null,
 'data/farmstorm/tags/block/deployed_crops.json':null,
 'data/farmstorm/loot_table/blocks/compressed_hay_block.json':null,
 'data/farmstorm/loot_table/blocks/zangief_cane.json':null,
};
for(const f of Object.keys(files)){
  const j=JSON.parse(fs.readFileSync(base+f,'utf8'));
  if(j.values) console.log(f+' -> values='+JSON.stringify(j.values)+' count='+j.values.length);
  else console.log(f+' -> pools='+j.pools.length+' item='+j.pools[0].entries[0].name+' rolls='+j.pools[0].rolls);
}
const ndt=JSON.parse(fs.readFileSync(base+'data/minecraft/tags/block/needs_diamond_tool.json','utf8'));
console.log('needs_diamond_tool exact match = '+(JSON.stringify(ndt.values)===JSON.stringify(['farmstorm:compressed_hay_block'])));

// 读 run-p14 世界里的玩家进度存档，列出 farmstorm:* 的授予情况（F-98 动态验收的读数工具）
const fs = require('fs');
const path = require('path');
const DIR = 'D:/project_farm/run-p14/world/advancements';
const files = fs.existsSync(DIR) ? fs.readdirSync(DIR).filter((f) => f.endsWith('.json')) : [];
if (files.length === 0) { console.log('(还没有玩家进度文件——玩家进过世界一次才会生成)'); process.exit(0); }
for (const f of files) {
  const j = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8'));
  const done = Object.keys(j).filter((k) => k.startsWith('farmstorm:')).sort();
  console.log('玩家存档 ' + f + '（' + done.length + ' 条 farmstorm 进度）');
  for (const k of done) {
    const criteria = Object.keys(j[k].criteria || {});
    console.log('  GRANTED  ' + k + '   criteria=' + criteria.join(','));
  }
  const wanted = ['farmstorm:first_molotov', 'farmstorm:zangief_hit', 'farmstorm:first_wild_hunter'];
  console.log('  三条新进度：' + wanted.map((w) => w.replace('farmstorm:', '') + '=' + (done.includes(w) ? 'YES' : 'no')).join('  '));
}

const fs = require('fs');
const out = [];
const serverLog = 'D:/project_farm/run-p14/logs/latest.log';
const lines = fs.readFileSync(serverLog, 'utf8').split('\n');
out.push('# 服务端日志摘录（' + serverLog + '）—— Phase 14 最终一轮');
for (const l of lines) {
  if (/桑吉尔蔗/.test(l) || /自检 部署行为/.test(l) || /自检 注册对象|自检 作物|自检 幸运/.test(l)) out.push(l.trim());
}
for (const f of ['D:/project_farm/.work/p14-client8.log', 'D:/project_farm/.work/p14-client10.log']) {
  if (!fs.existsSync(f)) continue;
  out.push('');
  out.push('# 客户端日志摘录（' + f + '）');
  for (const l of fs.readFileSync(f, 'utf8').split('\n')) {
    if (/P14 输入探针|桑吉尔蔗|取证 相机/.test(l)) out.push(l.trim());
  }
}
fs.writeFileSync('D:/project_farm/.work/p14-log-extract.txt', out.join('\n') + '\n', 'utf8');
console.log('lines=' + out.length);
console.log(out.filter(l => /落地耗时/.test(l)).slice(-6).join('\n'));

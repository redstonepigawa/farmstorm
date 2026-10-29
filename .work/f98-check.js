const fs = require('fs');
const path = require('path');
const DIR = 'D:/project_farm/src/generated/resources/data/farmstorm/advancement';
const LANG = 'D:/project_farm/src/generated/resources/assets/farmstorm/lang';
const files = fs.readdirSync(DIR).filter((f) => f.endsWith('.json')).sort();
console.log('advancement json 总数:', files.length);
const zh = JSON.parse(fs.readFileSync(path.join(LANG, 'zh_cn.json'), 'utf8'));
const en = JSON.parse(fs.readFileSync(path.join(LANG, 'en_us.json'), 'utf8'));
let bad = 0;
for (const f of files) {
  const j = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8'));
  const d = j.display || {};
  const titleKey = (d.title || {}).translate;
  const descKey = (d.description || {}).translate;
  const inZh = titleKey in zh && descKey in zh;
  const inEn = titleKey in en && descKey in en;
  if (!inZh || !inEn) { bad++; console.log('  LANG MISSING', f, titleKey, descKey); }
  if (f.startsWith('first_molotov') || f.startsWith('zangief_hit') || f.startsWith('first_wild_hunter')) {
    console.log('--- ' + f + ' ---');
    console.log('  parent      :', j.parent);
    console.log('  frame       :', d.frame);
    console.log('  show_toast  :', d.show_toast);
    console.log('  announce    :', d.announce_to_chat);
    console.log('  icon        :', (d.icon || {}).id);
    console.log('  title/desc  :', titleKey, '/', descKey);
    console.log('  zh          :', zh[titleKey], '/', zh[descKey]);
    console.log('  lang 两侧都有:', inZh && inEn);
    console.log('  criteria    :', Object.keys(j.criteria || {}).join(','));
  }
}
console.log('缺语言键的进度数:', bad);

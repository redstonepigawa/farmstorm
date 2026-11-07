// Convert 1.21.x datagen output to the 1.20.1 on-disk schema.
//   1) recipe  result: {id,count}  ->  {item,count}
//   2) advancement  display.icon: {id}  ->  {item}; drop sends_telemetry_event (unknown in 1.20.1)
import { readdirSync, readFileSync, writeFileSync, statSync } from "node:fs";
import { join } from "node:path";

const root = process.argv[2];
if (!root) { console.error("usage: node fix-generated-1201.mjs <generatedResourcesDir>"); process.exit(2); }

function walk(dir, acc = []) {
  for (const e of readdirSync(dir)) {
    const p = join(dir, e);
    if (statSync(p).isDirectory()) walk(p, acc);
    else if (p.endsWith(".json")) acc.push(p);
  }
  return acc;
}

let recipeFixed = 0, advFixed = 0, iconFixed = 0, skipped = 0;

for (const dataDir of readdirSync(root)) {
  const base = join(root, dataDir);
  if (!statSync(base).isDirectory()) continue;

  for (const [dir, kind] of [["recipes", "recipe"], ["advancements", "advancement"]]) {
    const d = join(base, dir);
    let files;
    try { files = walk(d); } catch { continue; }
    for (const f of files) {
      let obj;
      try { obj = JSON.parse(readFileSync(f, "utf8")); } catch { skipped++; continue; }
      let changed = false;

      if (kind === "recipe") {
        const r = obj.result;
        if (r && typeof r === "object" && !Array.isArray(r) && r.id !== undefined) {
          r.item = r.id;
          delete r.id;
          changed = true;
          recipeFixed++;
        }
      } else {
        const icon = obj.display && obj.display.icon;
        if (icon && typeof icon === "object" && icon.id !== undefined) {
          icon.item = icon.id;
          delete icon.id;
          changed = true;
          iconFixed++;
        }
        if (obj.sends_telemetry_event !== undefined) {
          delete obj.sends_telemetry_event;
          changed = true;
        }
        if (changed) advFixed++;
      }

      if (changed) writeFileSync(f, JSON.stringify(obj, null, 2) + "\n", "utf8");
    }
  }
}

console.log(JSON.stringify({ recipeFixed, iconFixed, advFixed, skipped }));

# -*- coding: utf-8 -*-
import json, collections, os
base = os.path.join("D:/project_farm/src/generated/resources/assets/farmstorm/lang") + os.sep
order = ["farmstorm.configuration","advancements.farmstorm","item.farmstorm","block.farmstorm",
         "tooltip.farmstorm","entity.farmstorm","gui.farmstorm","death.attack","effect.farmstorm",
         "subtitles.farmstorm","key.farmstorm","message.farmstorm","command.farmstorm",
         "container.farmstorm","itemGroup.farmstorm"]
def bucket(k):
    for o in order:
        if k.startswith(o):
            return o
    return "__OTHER__"
data = {}
for f in ("zh_cn.json","en_us.json"):
    d = json.load(open(base+f, encoding="utf-8"))
    data[f]=d
    pre = collections.Counter(bucket(k) for k in d)
    print(f, "keys=", len(d))
    vals = [pre.get(o,0) for o in order]
    print("  dist:", " + ".join(str(v) for v in vals), "=", sum(vals))
    for o,v in zip(order,vals):
        print("   ", o, v)
    oth = [k for k in d if bucket(k)=="__OTHER__"]
    print("  OTHER keys:", oth)
    print("  farmlandlimit:", sum(1 for k in d if "farmlandlimit" in k))
zh, en = data["zh_cn.json"], data["en_us.json"]
print("set(zh)==set(en):", set(zh)==set(en))
print("zh-only:", sorted(set(zh)-set(en)))
print("en-only:", sorted(set(en)-set(zh)))

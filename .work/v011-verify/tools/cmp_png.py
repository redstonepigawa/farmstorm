# -*- coding: utf-8 -*-
import hashlib, os, struct, json
src = "D:/project_farm/src/main/resources/assets/farmstorm/textures"
gen = "D:/project_farm/.work/v011-verify/gen-out/assets/farmstorm/textures"
def walk(root):
    out = {}
    for dp, dn, fn in os.walk(root):
        for f in fn:
            if f.endswith(".png"):
                p = os.path.join(dp, f)
                rel = os.path.relpath(p, root).replace("\\", "/")
                out[rel] = (hashlib.sha256(open(p,"rb").read()).hexdigest(), os.path.getsize(p))
    return out
a, b = walk(src), walk(gen)
print("src pngs:", len(a), " gen pngs:", len(b))
print("only in src:", sorted(set(a)-set(b)))
print("only in gen:", sorted(set(b)-set(a)))
diff = [k for k in sorted(set(a)&set(b)) if a[k][0]!=b[k][0]]
print("sha256 differs (%d):" % len(diff))
for k in diff: print("   ", k, a[k][1], "->", b[k][1])
def size(p):
    d = open(p,"rb").read(33)
    w,h = struct.unpack(">II", d[16:24]); return (w,h)
print("wild_apple src:", size(src+"/item/wild_apple.png"), a["item/wild_apple.png"][1], a["item/wild_apple.png"][0])
print("wild_apple gen:", size(gen+"/item/wild_apple.png"), b["item/wild_apple.png"][1], b["item/wild_apple.png"][0])
items_src = sorted(k for k in a if k.startswith("item/"))
print("item pngs: src=%d gen=%d, all equal=%s" % (len(items_src), len([k for k in b if k.startswith("item/")]), all(a[k][0]==b[k][0] for k in items_src)))

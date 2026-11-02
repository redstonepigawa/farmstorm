# -*- coding: utf-8 -*-
import sys, collections, importlib.util
spec = importlib.util.spec_from_file_location("nbt", r"D:/project_farm/.work/p29-nbt.py")
nbt = importlib.util.module_from_spec(spec); spec.loader.exec_module(nbt)

MASK26 = (1 << 26) - 1
def sx(v, bits):
    m = (1 << bits) - 1
    v &= m
    if v >= (1 << (bits - 1)):
        v -= (1 << bits)
    return v
def decode(v):
    return (sx(v >> 38, 26), sx(v & 0xFFF, 12), sx(v >> 12, 26))

base = "D:/project_farm/run-p31/world/data/"
d = nbt.load(base + "farmstorm_farmland.dat")["data"]
bl = d.get("bindings") or []
print("dataVersion:", d.get("dataVersion"), "bindings:", len(bl), "orphans:", len(d.get("orphans") or []))
print("binding keys:", sorted(bl[0].keys()))
bycore = collections.Counter()
for b in bl:
    bycore[(b.get("coreDimension"), decode(b["corePos"]))] += 1
print("by core:")
for k, v in sorted(bycore.items()):
    print("   ", k, "=", v)
pos = sorted(decode(b["pos"]) for b in bl)
print("all positions (%d):" % len(pos))
print("  ", pos)
print("owners:", set(b["owner"] for b in bl))
c = nbt.load(base + "farmstorm_outpost_core.dat")["data"]
for owner, o in c.get("owners", {}).items():
    print("core index:", o.get("name"), [(decode(e["pos"]), e["dimension"], e.get("gameTime")) for e in o["entries"]])

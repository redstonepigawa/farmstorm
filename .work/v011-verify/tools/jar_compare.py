# -*- coding: utf-8 -*-
import zipfile, hashlib, sys, os
j11 = r"D:\project_farm\dist\farmstorm-0.1.1+mc1.21.1.jar"
j10 = r"D:\project_farm\dist\farmstorm-0.1.0+mc1.21.1.jar"
def entries(p):
    with zipfile.ZipFile(p) as z:
        return z.namelist()
e11, e10 = entries(j11), entries(j10)
print("0.1.1 entries:", len(e11), " 0.1.0 entries:", len(e10))
targets = [
 "assets/farmstorm/textures/item/wild_apple.png",
 "com/dashpig/farmstorm/network/CoreListPayload.class",
 "com/dashpig/farmstorm/client/CoreHudRenderer.class",
 "com/dashpig/farmstorm/outpost/CoreChunkTickets.class",
 "com/dashpig/farmstorm/command/FarmstormCommands.class",
 "com/dashpig/farmstorm/client/FarmstormKeys.class",
 "com/dashpig/farmstorm/client/CoreHudDragScreen.class",
 "com/dashpig/farmstorm/client/CoreHudClientCommands.class",
 "com/dashpig/farmstorm/client/ClientCoreList.class",
 "com/dashpig/farmstorm/blockentity/OutpostCoreBlockEntity.class",
 "META-INF/neoforge.mods.toml",
]
for t in targets:
    print("  %-62s 0.1.1=%d  0.1.0=%d" % (t, e11.count(t), e10.count(t)))
print("png 0.1.1/0.1.0:", sum(1 for x in e11 if x.endswith(".png")), sum(1 for x in e10 if x.endswith(".png")))
print("json 0.1.1/0.1.0:", sum(1 for x in e11 if x.endswith(".json")), sum(1 for x in e10 if x.endswith(".json")))
print("ogg 0.1.1/0.1.0:", sum(1 for x in e11 if x.endswith(".ogg")), sum(1 for x in e10 if x.endswith(".ogg")))
s11, s10 = set(e11), set(e10)
added = sorted(s11 - s10)
removed = sorted(s10 - s11)
print("added entries (%d):" % len(added))
for a in added: print("   +", a)
print("removed entries (%d):" % len(removed))
for a in removed: print("   -", a)
print("added png:", [x for x in added if x.endswith('.png')])
print("added json:", [x for x in added if x.endswith('.json')])
print("added ogg:", [x for x in added if x.endswith('.ogg')])
with zipfile.ZipFile(j11) as z:
    print("--- neoforge.mods.toml ---")
    print(z.read("META-INF/neoforge.mods.toml").decode("utf-8"))
    wi = z.getinfo("assets/farmstorm/textures/item/wild_apple.png")
    data = z.read("assets/farmstorm/textures/item/wild_apple.png")
    import struct
    w, h = struct.unpack(">II", data[16:24])
    print("wild_apple.png: %d bytes, %dx%d, sha256=%s" % (len(data), w, h, hashlib.sha256(data).hexdigest()))

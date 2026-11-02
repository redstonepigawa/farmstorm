# -*- coding: utf-8 -*-
"""Craft v1-format FarmlandLedger fixture + OutpostCoreIndex fixture (Phase 31 evidence)."""
import gzip, struct, os, sys

UUID = "81b5a0ee-ef3e-349d-9a7e-1a5138d5583b"
NAME = "ALICE"

def pos_long(x, y, z):
    return ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF)

class W:
    def __init__(self):
        self.b = bytearray()
    def tag_header(self, t, name):
        self.b.append(t)
        n = name.encode("utf-8")
        self.b += struct.pack(">H", len(n)) + n
    def i(self, name, v):
        self.tag_header(3, name); self.b += struct.pack(">i", v)
    def l(self, name, v):
        self.tag_header(4, name); self.b += struct.pack(">q", v)
    def s(self, name, v):
        self.tag_header(8, name)
        n = v.encode("utf-8"); self.b += struct.pack(">H", len(n)) + n
    def compound(self, name):
        self.tag_header(10, name)
    def list_compound(self, name, n):
        self.tag_header(9, name); self.b.append(10); self.b += struct.pack(">i", n)
    def end(self):
        self.b.append(0)

def write_root(path, payload):
    w = W()
    w.compound("")
    w.i("DataVersion", 3955)
    w.tag_header(10, "data")
    payload(w)
    w.end()
    w.end()
    with gzip.open(path, "wb") as f:
        f.write(bytes(w.b))
    print("wrote", path, len(w.b), "raw bytes")

FR = "D:/project_farm/.work/v011-verify/fixtures/"
os.makedirs(FR, exist_ok=True)

xs = list(range(190, 210))
Y, Z = 98, 210

def farmland_v1(w):
    w.tag_header(10, "owners")
      # owners -> uuid -> {name, positions:[{pos}]}
    w.tag_header(10, UUID)
    w.s("name", NAME)
    w.list_compound("positions", len(xs))
    for x in xs:
        w.l("pos", pos_long(x, Y, Z))
        w.end()
    w.end()   # uuid
    w.end()   # owners

def core_index(w):
    w.tag_header(10, "owners")
    w.tag_header(10, UUID)
    w.s("name", NAME)
    w.list_compound("entries", 1)
    w.l("pos", pos_long(200, 99, 205))
    w.s("dimension", "minecraft:overworld")
    w.l("gameTime", 1000)
    w.end()
    w.end()
    w.end()

write_root(FR + "farmstorm_farmland_v1.dat", farmland_v1)
write_root(FR + "farmstorm_outpost_core.dat", core_index)
print("positions:", [(x, Y, Z) for x in xs])
print("core:", (200, 99, 205), "gameTime=1000")

# -*- coding: utf-8 -*-
from PIL import Image
import os
src = "D:/project_farm/src/main/resources/assets/farmstorm/textures/item/"
gen = "D:/project_farm/.work/v011-verify/gen-out/assets/farmstorm/textures/item/"
names = ["puffed_wheat","blast_carrot","ubi_server","toxic_ubi_server","power_beet","melon_slice_bomb",
         "melon_grenade","pumpkin_turret","zangief_cane","molotov","lucky_berry","compressed_puffed_wheat"]
print("%-26s %-10s %-10s %s" % ("file","src size","gen size","diff px / src extra-green px"))
for n in names:
    ps, pg = src+n+".png", gen+n+".png"
    if not (os.path.exists(ps) and os.path.exists(pg)):
        print(n, "missing"); continue
    a, b = Image.open(ps).convert("RGBA"), Image.open(pg).convert("RGBA")
    same_size = a.size == b.size
    diff = 0; green = 0
    if same_size:
        pa, pb = a.load(), b.load()
        for y in range(a.size[1]):
            for x in range(a.size[0]):
                if pa[x,y] != pb[x,y]:
                    diff += 1
                    r,g,bb,al = pa[x,y]
                    if al > 0 and g > 120 and r < 120 and bb < 120:
                        green += 1
    print("%-26s %-10s %-10s diff=%d extra-green=%d same_size=%s" % (n, a.size, b.size, diff, green, same_size))

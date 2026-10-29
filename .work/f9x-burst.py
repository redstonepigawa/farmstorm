from PIL import Image, ImageChops
import glob, os
files = sorted(glob.glob(r"D:\project_farm\run-p14c\screenshots\2026-10-29_20.51.5*.png"))
print("frames:", len(files))
base = Image.open(files[0]).convert("RGB")
for f in files[1:]:
    im = Image.open(f).convert("RGB")
    bbox = ImageChops.difference(base, im).getbbox()
    print(os.path.basename(f), "diff bbox:", bbox)
# 找所有帧里「绿色像素」最多的那一帧
best = None
for f in files:
    im = Image.open(f).convert("RGB")
    px = im.load()
    greens = 0
    for y in range(120, 340):
        for x in range(300, 620):
            r, g, b = px[x, y]
            if g > 110 and g > r + 40 and g > b + 40:
                greens += 1
    if best is None or greens > best[1]:
        best = (f, greens)
    print(os.path.basename(f), "green px in block region:", greens)
print("best:", best)

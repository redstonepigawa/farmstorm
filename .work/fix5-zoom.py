from PIL import Image
from pathlib import Path
names = ['fix5-shot-1-look-north','fix5-shot-2-look-east','fix5-shot-3-look-south','fix5-shot-4-look-west']
tiles = []
for n in names:
    im = Image.open(Path('.work/fix5-shots') / (n + '.png')).convert('RGB')
    w, h = im.size
    box = (int(w*0.30), int(h*0.18), int(w*0.70), int(h*0.72))
    tiles.append(im.crop(box).resize(((box[2]-box[0])*2, (box[3]-box[1])*2), Image.Resampling.NEAREST))
tw, th = tiles[0].size
sheet = Image.new('RGB', (tw*2, th*2), (0,0,0))
for i, t in enumerate(tiles):
    sheet.paste(t, ((i % 2) * tw, (i // 2) * th))
sheet.save('.work/fix5-shots/fix5-shots-zoom.png')
print('sheet', sheet.size)
for n in names:
    im = Image.open(Path('.work/fix5-shots') / (n + '.png')).convert('RGB')
    print(n, im.size, 'center px', im.getpixel((427, 190)))
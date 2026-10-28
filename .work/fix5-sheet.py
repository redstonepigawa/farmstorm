from PIL import Image
from pathlib import Path
root = Path('src/main/resources/assets/farmstorm/textures/block')
names = ['converter_side_wheat','converter_side_carrot','converter_side_potato','converter_side_beetroot','converter_top','converter_side','converter_front']
zoom = 10
sheet = Image.new('RGBA', (len(names)*16*zoom, 16*zoom), (40,40,40,255))
for i, n in enumerate(names):
    img = Image.open(root / (n + '.png')).convert('RGBA')
    print(n, img.size)
    sheet.alpha_composite(img.resize((16*zoom, 16*zoom), Image.Resampling.NEAREST), (i*16*zoom, 0))
Path('.work/fix5-shots').mkdir(parents=True, exist_ok=True)
sheet.save('.work/fix5-shots/fix5-block-sides-zoom.png')
print('sheet', sheet.size)
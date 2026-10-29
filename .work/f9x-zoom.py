from PIL import Image
src = Image.open(r"D:\project_farm\.work\f9x-1-inventory.png").convert("RGBA")
w, h = src.size
print("full:", src.size)
# 底部两行（快捷栏 + 背包首行）放大，便于肉眼核对图标
crop = src.crop((210, 360, 650, 480))
crop = crop.resize((crop.width * 4, crop.height * 4), Image.Resampling.NEAREST)
crop.save(r"D:\project_farm\.work\f9x-1-inventory-zoom.png")
print("zoom:", crop.size)

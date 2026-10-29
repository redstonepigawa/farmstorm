from PIL import Image
src = r"D:\project_farm\run-p14c\screenshots\2026-10-29_20.51.54.png"
img = Image.open(src).convert("RGB")
crop = img.crop((330, 150, 560, 300))
crop.resize((crop.width * 4, crop.height * 4), Image.Resampling.NEAREST).save(r"D:\project_farm\.work\f9x-8-particles-zoom.png")
print("zoom saved")

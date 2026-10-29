from PIL import Image
for tag, src in (("4a", r"D:\project_farm\.work\f9x-4a-break-particles.png"),
                 ("4b", r"D:\project_farm\.work\f9x-4b-break-particles.png")):
    img = Image.open(src).convert("RGBA")
    crop = img.crop((340, 150, 560, 320))
    crop.resize((crop.width * 3, crop.height * 3), Image.Resampling.NEAREST).save(r"D:\project_farm\.work\f9x-zoom-" + tag + ".png")
    print(tag, "ok")

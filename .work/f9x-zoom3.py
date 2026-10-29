from PIL import Image
for tag, src in (("invisible", r"D:\project_farm\.work\f9x-3-block-invisible.png"),
                 ("particles", r"D:\project_farm\.work\f9x-4-break-particles.png")):
    img = Image.open(src).convert("RGBA")
    crop = img.crop((360, 160, 560, 320))
    crop = crop.resize((crop.width * 3, crop.height * 3), Image.Resampling.NEAREST)
    out = r"D:\project_farm\.work\f9x-zoom-" + tag + ".png"
    crop.save(out)
    print(tag, crop.size, "->", out)

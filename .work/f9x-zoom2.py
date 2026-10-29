from PIL import Image
shot = Image.open(r"D:\project_farm\.work\f9x-2-hud-effect.png").convert("RGBA")
crop = shot.crop((790, 5, 854, 60))
crop.resize((crop.width * 6, crop.height * 6), Image.Resampling.NEAREST).save(r"D:\project_farm\.work\f9x-2-hud-icon-zoom.png")
print("zoom saved", crop.size)

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""FIX-3 几何校验：把 converter.png 里实际画出的槽位矩形找出来，与 ConverterMenu 的 Slot 坐标逐项比对。

判定方法：脚本给每个槽位画的是 18x18 的方框（左上暗边 0x37、右下亮边 0xFF、填色 0x8B、框内 16x16）。
所以「内容区左上角」= (框左上 + 1, 框左上 + 1)，与 Minecraft 的 Slot x/y 语义相同（Slot 坐标即 16x16 物品格的左上角）。
"""
import re
import sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
PNG = ROOT / "src/main/resources/assets/farmstorm/textures/gui/converter.png"
MENU = ROOT / "src/main/java/com/dashpig/farmstorm/menu/ConverterMenu.java"
SCREEN = ROOT / "src/main/java/com/dashpig/farmstorm/client/ConverterScreen.java"
GEN = ROOT / "tools/gen_textures.py"

FILL = (0x8B, 0x8B, 0x8B, 0xFF)
DARK = (0x37, 0x37, 0x37, 0xFF)
LIGHT = (0xFF, 0xFF, 0xFF, 0xFF)

def java_consts(path, names):
    text = path.read_text(encoding="utf-8")
    out = {}
    for n in names:
        m = re.search(r"\b" + n + r"\b\s*=\s*(-?\d+)", text)
        if not m:
            sys.exit("missing constant %s in %s" % (n, path.name))
        out[n] = int(m.group(1))
    return out

def py_consts(path, names):
    text = path.read_text(encoding="utf-8")
    out = {}
    for n in names:
        m = re.search(r"^" + n + r"\s*=\s*\(?(-?\d+)(?:,\s*(-?\d+))?\)?", text, re.M)
        if not m:
            sys.exit("missing constant %s in %s" % (n, path.name))
        out[n] = (int(m.group(1)), int(m.group(2))) if m.group(2) else int(m.group(1))
    return out

def find_slots(img):
    w, h = img.size
    px = img.load()
    found = []
    for y in range(1, h - 17):
        for x in range(1, w - 17):
            if px[x, y] != FILL or px[x - 1, y] != DARK or px[x, y - 1] != DARK:
                continue
            if px[x + 16, y] != LIGHT or px[x, y + 16] != LIGHT:
                continue
            ok = all(px[x + i, y + j] == FILL for j in range(16) for i in range(16))
            if ok:
                found.append((x, y))
    return found

def expected_slots(c):
    slots = [(c["INPUT_X"], c["INPUT_Y"]), (c["PREVIEW_X"], c["PREVIEW_Y"])]
    for row in range(3):
        for col in range(9):
            slots.append((c["INVENTORY_X"] + col * c["SLOT_SIZE"], c["INVENTORY_Y"] + row * c["SLOT_SIZE"]))
    for col in range(9):
        slots.append((c["INVENTORY_X"] + col * c["SLOT_SIZE"], c["HOTBAR_Y"]))
    return slots

def main():
    menu = java_consts(MENU, ["INPUT_X", "INPUT_Y", "PREVIEW_X", "PREVIEW_Y",
                              "INVENTORY_X", "INVENTORY_Y", "HOTBAR_Y", "SLOT_SIZE"])
    screen = java_consts(SCREEN, ["IMAGE_WIDTH", "IMAGE_HEIGHT"])
    gen = py_consts(GEN, ["GUI_INPUT_SLOT", "GUI_PREVIEW_SLOT", "GUI_INVENTORY_ORIGIN",
                          "GUI_HOTBAR_ORIGIN", "GUI_SLOT_PITCH", "GUI_SLOT_SIZE"])
    img = Image.open(PNG).convert("RGBA")
    print("贴图        : %s  %dx%d" % (PNG.name, img.size[0], img.size[1]))
    print("Screen 常量 : IMAGE_WIDTH=%d IMAGE_HEIGHT=%d" % (screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]))
    print("Menu 常量   : INPUT=(%d,%d) PREVIEW=(%d,%d) INV=(%d,%d) HOTBAR_Y=%d PITCH=%d"
          % (menu["INPUT_X"], menu["INPUT_Y"], menu["PREVIEW_X"], menu["PREVIEW_Y"],
             menu["INVENTORY_X"], menu["INVENTORY_Y"], menu["HOTBAR_Y"], menu["SLOT_SIZE"]))
    print("生成器常量  : INPUT=%s PREVIEW=%s INV=%s HOTBAR=%s PITCH=%d SIZE=%d"
          % (gen["GUI_INPUT_SLOT"], gen["GUI_PREVIEW_SLOT"], gen["GUI_INVENTORY_ORIGIN"],
             gen["GUI_HOTBAR_ORIGIN"], gen["GUI_SLOT_PITCH"], gen["GUI_SLOT_SIZE"]))

    problems = []
    if img.size != (screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]):
        problems.append("贴图尺寸 %s != Screen 的 %dx%d" % (img.size, screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]))
    gen_expected = [(gen["GUI_INPUT_SLOT"][0], gen["GUI_INPUT_SLOT"][1]), (gen["GUI_PREVIEW_SLOT"][0], gen["GUI_PREVIEW_SLOT"][1])]
    for row in range(3):
        for col in range(9):
            gen_expected.append((gen["GUI_INVENTORY_ORIGIN"][0] + col * gen["GUI_SLOT_PITCH"],
                                 gen["GUI_INVENTORY_ORIGIN"][1] + row * gen["GUI_SLOT_PITCH"]))
    for col in range(9):
        gen_expected.append((gen["GUI_HOTBAR_ORIGIN"][0] + col * gen["GUI_SLOT_PITCH"], gen["GUI_HOTBAR_ORIGIN"][1]))

    found = sorted(find_slots(img))
    want = sorted(expected_slots(menu))
    print("\n贴图上找到 %d 个槽位矩形，Menu 声明 %d 个 Slot" % (len(found), len(want)))
    if len(found) != len(want):
        problems.append("槽位数量不一致：贴图 %d / Menu %d" % (len(found), len(want)))

    def label(p):
        if p == (menu["INPUT_X"], menu["INPUT_Y"]):
            return "输入槽"
        if p == (menu["PREVIEW_X"], menu["PREVIEW_Y"]):
            return "预览槽"
        return ""

    print("\n%-4s %-10s %-10s %-10s %s" % ("#", "贴图(x,y)", "Menu(x,y)", "偏差", "槽位"))
    worst = 0
    for i in range(max(len(found), len(want))):
        f = found[i] if i < len(found) else None
        w = want[i] if i < len(want) else None
        d = "—" if (f is None or w is None) else ("一致" if f == w else "(dx=%+d, dy=%+d)" % (w[0] - f[0], w[1] - f[1]))
        if f is not None and w is not None:
            worst = max(worst, abs(w[0] - f[0]), abs(w[1] - f[1]))
        print("%-4d %-10s %-10s %-10s %s" % (i, f, w, d, label(w) if w else label(f)))
    if worst:
        problems.append("槽位坐标最大偏差 %d 像素" % worst)
    else:
        print("\n[OK] 全部 %d 个槽位坐标逐像素一致（最大偏差 0）" % len(want))

    if sorted(found) != sorted(gen_expected):
        problems.append("贴图实际槽位 != 生成器声明的槽位（PNG 与脚本已不同步，需重跑脚本）")
    else:
        print("[OK] 贴图实际槽位与 tools/gen_textures.py 的常量逐像素一致")

    # 6 参 blit 的后果（256x256 UV 假设）：只采样贴图左上 176*176/256 x 166*166/256 的区域，再拉伸铺满 176x166
    sx = screen["IMAGE_WIDTH"] * screen["IMAGE_WIDTH"] / 256.0
    sy = screen["IMAGE_HEIGHT"] * screen["IMAGE_HEIGHT"] / 256.0
    print("\n若用 6 参 blit(RL, x, y, 0, 0, 176, 166)（内部按 256x256 算 UV）：")
    print("  实际被采样并放大的源区域 = (0,0)-(%.1f,%.1f)，放大倍率 %.3fx / %.3fx"
          % (sx, sy, 256.0 / screen["IMAGE_WIDTH"], 256.0 / screen["IMAGE_HEIGHT"]))
    for name, p in (("输入槽", (menu["INPUT_X"], menu["INPUT_Y"])), ("预览槽", (menu["PREVIEW_X"], menu["PREVIEW_Y"])),
                    ("背包首格", (menu["INVENTORY_X"], menu["INVENTORY_Y"])), ("快捷栏首格", (menu["INVENTORY_X"], menu["HOTBAR_Y"]))):
        bx, by = p[0] - 1, p[1] - 1
        print("  %s 画在 (%.0f,%.0f)，而物品格在 (%d,%d) —— 错位 dx=%+.0f dy=%+.0f"
              % (name, bx * 256.0 / 176, by * 256.0 / 166, p[0], p[1],
                 bx * 256.0 / 176 - p[0], by * 256.0 / 166 - p[1]))

    if problems:
        print("\nFAIL:")
        for p in problems:
            print("  - " + p)
        return 1
    print("\nFAIL 0")
    return 0

if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""FIX-4 几何校验：converter.png 里画出来的槽位/按钮矩形，与 Java 侧的常量逐像素比对。

判定方法（与 tools/gen_textures.py 的画法一一对应）：
  * 槽位：18x18 方框，左上暗边 0x37、右下亮边 0xFF、填色 0x8B，框内 16x16 内容区；
    内容区左上的 (x, y) 就是 Minecraft 的 Slot 坐标语义。
  * 催化剂槽的空槽提示是内容区正中的菱形轮廓（|dx|+|dy| = 3，白色），只此一处例外。
  * 按钮：矩形底 (x0..x1, y0..y1)，上/左暗边、下/右亮边、内部填色。
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
EDGE_DARK = (0x55, 0x55, 0x55, 0xFF)
EDGE_LIGHT = (0xFF, 0xFF, 0xFF, 0xFF)
MARK_RADIUS = 3
SLOT_SIZE = 16


def java_ints(path, names):
    text = path.read_text(encoding="utf-8")
    out = {}
    for name in names:
        m = re.search(r"\b" + name + r"\b\s*=\s*(-?\d+)", text)
        if not m:
            sys.exit("missing constant %s in %s" % (name, path.name))
        out[name] = int(m.group(1))
    return out


def py_tuples(path, names):
    text = path.read_text(encoding="utf-8")
    out = {}
    for name in names:
        m = re.search(r"^" + name + r"\s*=\s*\(([^)\n]*)\)", text, re.M)
        if not m:
            sys.exit("missing constant %s in %s" % (name, path.name))
        out[name] = tuple(int(part.strip()) for part in m.group(1).split(",") if part.strip())
    return out


def is_mark(slot, point):
    """内容区里属于「菱形提示」的像素（只允许出现在催化剂槽）。"""
    return abs(point[0] - (slot[0] + SLOT_SIZE // 2)) + abs(point[1] - (slot[1] + SLOT_SIZE // 2)) == MARK_RADIUS


def find_slots(img, catalyst_slot):
    width, height = img.size
    px = img.load()
    found = []
    for y in range(1, height - 17):
        for x in range(1, width - 17):
            if px[x - 1, y] != DARK or px[x, y - 1] != DARK:
                continue
            if px[x + SLOT_SIZE, y] != LIGHT or px[x, y + SLOT_SIZE] != LIGHT:
                continue
            bad = 0
            for j in range(SLOT_SIZE):
                for i in range(SLOT_SIZE):
                    value = px[x + i, y + j]
                    if value == FILL:
                        continue
                    if (x, y) == catalyst_slot and value == LIGHT and is_mark((x, y), (x + i, y + j)):
                        continue
                    bad += 1
            if bad == 0:
                found.append((x, y))
    return found


def expected_slots(c):
    slots = [(c["CATALYST_X"], c["CATALYST_Y"]), (c["INPUT_X"], c["INPUT_Y"]), (c["PREVIEW_X"], c["PREVIEW_Y"])]
    for row in range(3):
        for column in range(9):
            slots.append((c["INVENTORY_X"] + column * c["SLOT_SIZE"], c["INVENTORY_Y"] + row * c["SLOT_SIZE"]))
    for column in range(9):
        slots.append((c["INVENTORY_X"] + column * c["SLOT_SIZE"], c["HOTBAR_Y"]))
    return slots


def gen_slots(g):
    slots = [tuple(g["GUI_CATALYST_SLOT"]), tuple(g["GUI_INPUT_SLOT"]), tuple(g["GUI_PREVIEW_SLOT"])]
    for row in range(g["GUI_INVENTORY_ROWS"]):
        for column in range(g["GUI_INVENTORY_COLUMNS"]):
            slots.append((g["GUI_INVENTORY_ORIGIN"][0] + column * g["GUI_SLOT_PITCH"],
                          g["GUI_INVENTORY_ORIGIN"][1] + row * g["GUI_SLOT_PITCH"]))
    for column in range(g["GUI_INVENTORY_COLUMNS"]):
        slots.append((g["GUI_HOTBAR_ORIGIN"][0] + column * g["GUI_SLOT_PITCH"], g["GUI_HOTBAR_ORIGIN"][1]))
    return slots


def check_button(img, rect):
    px = img.load()
    x0, y0, x1, y1 = rect
    problems = []
    if x1 <= x0 or y1 <= y0:
        return ["按钮矩形退化：%s" % (rect,)]
    if px[x1 - 1, y0] != EDGE_DARK or px[x0, y1 - 1] != EDGE_DARK:
        problems.append("按钮上/左边框不是暗色")
    if px[x1, y1] != EDGE_LIGHT or px[x0 + 1, y1] != EDGE_LIGHT:
        problems.append("按钮下/右边框不是亮色")
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            if px[x, y] != FILL:
                problems.append("按钮内部 (%d,%d) 不是填充色 %s" % (x, y, px[x, y]))
                return problems
    return problems


def main():
    menu = java_ints(MENU, ["INPUT_X", "INPUT_Y", "CATALYST_X", "CATALYST_Y", "PREVIEW_X", "PREVIEW_Y",
                            "INVENTORY_X", "INVENTORY_Y", "HOTBAR_Y", "SLOT_SIZE"])
    screen = java_ints(SCREEN, ["IMAGE_WIDTH", "IMAGE_HEIGHT", "BUTTON_X", "BUTTON_Y",
                                "BUTTON_WIDTH", "BUTTON_HEIGHT", "CONTENT_MARGIN"])
    gen = py_tuples(GEN, ["GUI_CATALYST_SLOT", "GUI_INPUT_SLOT", "GUI_PREVIEW_SLOT", "GUI_INVENTORY_ORIGIN",
                          "GUI_HOTBAR_ORIGIN", "GUI_SIZE", "GUI_BUTTON"])
    gen_nums = java_ints(GEN, ["GUI_SLOT_PITCH", "GUI_SLOT_SIZE", "GUI_DIVIDER_Y",
                               "GUI_INVENTORY_DIVIDER_Y", "GUI_INVENTORY_ROWS", "GUI_INVENTORY_COLUMNS"])

    gen_all = {**gen, **gen_nums}
    img = Image.open(PNG).convert("RGBA")
    print("贴图        : %s  %dx%d" % (PNG.name, img.size[0], img.size[1]))
    print("Screen 常量 : IMAGE=%dx%d BUTTON=(x=%d,y=%d,w=%d,h=%d) titleLabelY 之下 inventoryLabelY=%d"
          % (screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"], screen["BUTTON_X"], screen["BUTTON_Y"],
             screen["BUTTON_WIDTH"], screen["BUTTON_HEIGHT"], screen["IMAGE_HEIGHT"] - 94))
    print("Menu 常量   : CATALYST=(%d,%d) INPUT=(%d,%d) PREVIEW=(%d,%d) INV=(%d,%d) HOTBAR_Y=%d PITCH=%d SIZE=%d"
          % (menu["CATALYST_X"], menu["CATALYST_Y"], menu["INPUT_X"], menu["INPUT_Y"],
             menu["PREVIEW_X"], menu["PREVIEW_Y"], menu["INVENTORY_X"], menu["INVENTORY_Y"],
             menu["HOTBAR_Y"], menu["SLOT_SIZE"], SLOT_SIZE))
    print("生成器常量  : CATALYST=%s INPUT=%s PREVIEW=%s INV=%s HOTBAR=%s PITCH=%d SIZE=%d DIV=%d/%d BUTTON=%s"
          % (gen["GUI_CATALYST_SLOT"], gen["GUI_INPUT_SLOT"], gen["GUI_PREVIEW_SLOT"], gen["GUI_INVENTORY_ORIGIN"],
             gen["GUI_HOTBAR_ORIGIN"], gen_nums["GUI_SLOT_PITCH"], gen_nums["GUI_SLOT_SIZE"],
             gen_nums["GUI_DIVIDER_Y"], gen_nums["GUI_INVENTORY_DIVIDER_Y"], gen["GUI_BUTTON"]))

    problems = []
    if img.size != (screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]):
        problems.append("贴图尺寸 %s != Screen 的 %dx%d" % (img.size, screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]))
    if tuple(gen["GUI_SIZE"]) != (screen["IMAGE_WIDTH"], screen["IMAGE_HEIGHT"]):
        problems.append("生成器 GUI_SIZE %s != Screen 的 %dx%d" % (gen["GUI_SIZE"], screen["IMAGE_WIDTH"],
                                                                    screen["IMAGE_HEIGHT"]))

    want = sorted(expected_slots(menu))
    gen_want = sorted(gen_slots(gen_all))
    if want != gen_want:
        problems.append("Menu 常量与生成器常量不一致：%s" % sorted(set(want) ^ set(gen_want)))

    catalyst_slot = (menu["CATALYST_X"], menu["CATALYST_Y"])
    found = sorted(find_slots(img, catalyst_slot))
    print("\n贴图上找到 %d 个槽位矩形，Menu 声明 %d 个 Slot" % (len(found), len(want)))
    if found != want:
        problems.append("贴图槽位与 Menu 的 Slot 坐标不一致：只在一侧出现的 %s"
                        % sorted(set(found) ^ set(want)))
    names = {catalyst_slot: "催化剂槽", (menu["INPUT_X"], menu["INPUT_Y"]): "输入槽",
             (menu["PREVIEW_X"], menu["PREVIEW_Y"]): "预览槽"}
    for slot in [catalyst_slot, (menu["INPUT_X"], menu["INPUT_Y"]), (menu["PREVIEW_X"], menu["PREVIEW_Y"])]:
        mark = "命中" if slot in found else "缺失"
        print("  %-6s 贴图=%s Menu=%s %s  内容区 %s" % (names[slot], "找到" if slot in found else "—", slot,
                                                     mark, "含下界之星菱形提示" if slot == catalyst_slot else "纯填色"))
    print("[OK] 全部 %d 个槽位坐标逐像素一致（最大偏差 0）" % len(want) if found == want else "槽位不一致")

    if sorted(found) == gen_want:
        print("[OK] 贴图实际槽位与 tools/gen_textures.py 的常量逐像素一致")
    else:
        problems.append("贴图实际槽位 != 生成器声明的槽位（PNG 与脚本已不同步，需重跑脚本）")

    java_rect = (screen["BUTTON_X"], screen["BUTTON_Y"],
                 screen["BUTTON_X"] + screen["BUTTON_WIDTH"] - 1,
                 screen["BUTTON_Y"] + screen["BUTTON_HEIGHT"] - 1)
    print("\n按钮矩形：Java=(%d,%d)-(%d,%d)  生成器=%s"
          % (java_rect[0], java_rect[1], java_rect[2], java_rect[3], gen["GUI_BUTTON"]))
    if java_rect != tuple(gen["GUI_BUTTON"]):
        problems.append("按钮矩形：Java %s != 生成器 %s" % (java_rect, gen["GUI_BUTTON"]))
    else:
        print("[OK] 按钮矩形与 ConverterScreen 的 bounds 逐像素重合")
    problems.extend(check_button(img, tuple(gen["GUI_BUTTON"])))

    button = tuple(gen["GUI_BUTTON"])
    slot_bottom = menu["INPUT_Y"] + SLOT_SIZE
    if button[1] <= slot_bottom:
        problems.append("按钮顶边 %d 压在槽位带上（槽位内容区底边 %d）" % (button[1], slot_bottom))
    if button[3] >= gen_nums["GUI_INVENTORY_DIVIDER_Y"]:
        problems.append("按钮底边 %d 越过物品栏分隔线 %d" % (button[3], gen_nums["GUI_INVENTORY_DIVIDER_Y"]))
    label_y = screen["IMAGE_HEIGHT"] - 94
    if button[3] >= label_y:
        problems.append("按钮底边 %d 压到物品栏标签 y=%d" % (button[3], label_y))
    for slot in [(menu["CATALYST_X"], menu["CATALYST_Y"]), (menu["INPUT_X"], menu["INPUT_Y"]),
                 (menu["PREVIEW_X"], menu["PREVIEW_Y"])]:
        overlap = not (slot[0] + SLOT_SIZE < button[0] or slot[0] > button[2]
                       or slot[1] + SLOT_SIZE < button[1] or slot[1] > button[3])
        if overlap:
            problems.append("槽位 %s 与按钮矩形重叠" % (slot,))
    print("[OK] 按钮 (y=%d..%d) 在槽位带 (内容区底边 %d) 之下、物品栏分隔线 y=%d 与标签 y=%d 之上；三个槽位不与按钮重叠"
          % (button[1], button[3], slot_bottom, gen_nums["GUI_INVENTORY_DIVIDER_Y"], label_y))

    if problems:
        print("\nFAIL:")
        for problem in problems:
            print("  - " + problem)
        return 1
    print("\nFAIL 0")
    return 0


if __name__ == "__main__":
    sys.exit(main())

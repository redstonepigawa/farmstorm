#!/usr/bin/env python3
r"""「农场风云 / Farmstorm」材质生成器（F-51 / TECH §6.5 的 87 张 + FIX-5 的 4 张转换器作物侧面 = 91 张；
V-23 追加 4 张物品图标与 2 张方块，v0.1.0 · F-164 追加压缩麦顶 / 底 2 张 —— 共 99 张，材料全部程序生成）。

产出的 99 张材质（DEC D-25 的占位稿，作者审校后可替换）：
  1. textures/item/<26 个 id>.png            —— 15 个作物 + 6 个播种物 + 转换器 + V-23 的 4 个，16x16；
     v0.1.1 · D-125：plan0.0.1.txt 第 8 条「右下角叠绿色 +」这条规则全局作废，生成器不再叠任何角标；
     唯一例外是狂野苹果（v0.1.1 · D-126：源图 64x64 原样直通，不再进 16 网格取色）；
  2. textures/block/<base>_stage{0..3}.png   —— 14 个作物 × 4 阶段 = 56 张，16x16；
     <base> 取法见 TECH §6.3：作物方块 ID 去掉末尾的 _crop / _stem / _stalk / _block / _bush；
  3. textures/block/<id>.png                 —— 5 个部署方块 + zangief_cane + compressed_hay_block
     + converter_{top,side,front} + converter_side_{wheat,carrot,potato,beetroot}（FIX-5：工作台侧面叠作物图标）
     + compressed_hay_block_{top,bottom}（v0.1.0 · F-164 的顶 / 底切面）= 16 张；
  4. textures/gui/converter.png              —— 176x166 容器背景（面板 + 标题/背包刻线 + 输入→预览箭头 + 槽位），
     每个槽位方框的「内容区左上角」与 ConverterMenu 的 Slot 坐标逐像素相等。

0.0.3 追加的 8 张里，本脚本只出 5 张（`--additions`，与上面 99 张分开放）：`vibration_bean`
（16x16，作者没有给它的贴图素材，只能程序绘制）+ 4 张 `friend_chives_stage0..3`（v0.1.0 · F-162 起按
23 号 §3.4 的四阶段形态表绘制，不再是洋红 #D81E8A 占位）。另外 3 张凡有源图可转的一律走 tools/convert_media.ps1：
`mob_effect/q_bounce.png`（preview.jpg）、`item/friend_chives.png` 与 `item/chive_seeds.png`
（作者 1254x1254 原画，路径见 plan0.0.3.txt）—— 有源转码就不手绘，避免两套图源打架。

用法::

    python -X utf8 tools/gen_textures.py --list
    python -X utf8 tools/gen_textures.py --vanilla-assets <jar 或解包资源根> --out src/main/resources --dry-run
    python -X utf8 tools/gen_textures.py --vanilla-assets <jar 或解包资源根> --out src/main/resources

    # 0.0.3 追加里没有源图可转的 5 张（1 张程序绘制物品图标 + 4 张朋友的韭阶段贴图），不读原版贴图：
    python -X utf8 tools/gen_textures.py --additions --out src/main/resources

    --vanilla-assets 接受两种形态：
      * 客户端 jar（本机 = D:\minecraft\.minecraft\versions\26.3-NeoForge_26.3.0.99\26.3-NeoForge_26.3.0.99.jar），
        脚本用标准库 zipfile 直接读其中的 assets/minecraft/textures/{item,block}/*.png，不解包、不落地临时文件；
      * 解包后的资源根目录，其下可为 minecraft/textures/… 或 assets/minecraft/textures/…。
    --out  输出根目录（**资源根**，不是 assets 目录）：写到 <out>/assets/farmstorm/textures/{item,block,gui}/…，
           因此常规调用就是 --out src/main/resources。
    --dry-run 只解析并打印将写入的路径与源材质状态，不建目录、不写文件。

回退规则（任务书 §2）：每个目标带一条候选链——主源（原版同名贴图）缺失时用链表里的次级原版
贴图；整条链都缺失时才生成纯色占位色块，并把该目标登记进「找不到」清单，绝不中断整批生成。
结尾按「原版命中 / 退回占位 / 找不到」三类分别列出目标清单，供作者在换成真 1.21.1 资源后核对。

依赖：Python 3.9+ 与 Pillow 9.1+。
"""

from __future__ import annotations

import argparse
import os
import shutil
import sys
import zipfile
from dataclasses import dataclass, field
from functools import partial
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw

TEXTURE_SIZE = 16
MOD_ID = "farmstorm"
ASSETS_DIR = "assets"

PLACEHOLDER_COLOR = (0xD8, 0x1E, 0x8A, 0xFF)

# 阶段贴图按阶段从小到大等比缩小（NEAREST，底边居中），
# 让原版只有单一贴图的作物（西瓜/南瓜茎、仙人掌）四个阶段也能看出生长差异。
STAGE_SCALES = (10, 12, 14, 16)

LAYER_BLOCK = "block"

KIND_ICON = "icon"
KIND_STAGE = "stage"
KIND_BLOCK = "block"
KIND_GUI = "gui"
# 0.0.3 追加：没有原版来源、纯程序绘制的物品图标（--additions 模式专用）。
KIND_DRAWN = "drawn"

STATUS_VANILLA = "原版命中"
STATUS_SUBSTITUTE = "退回占位（主源缺失，改用次级原版贴图）"
STATUS_MISSING = "找不到（无任何候选，生成纯色占位色块）"

GUI_SIZE = (176, 166)
GUI_BG = (0xC6, 0xC6, 0xC6, 0xFF)
GUI_EDGE_LIGHT = (0xFF, 0xFF, 0xFF, 0xFF)
GUI_EDGE_DARK = (0x55, 0x55, 0x55, 0xFF)
GUI_SLOT_FILL = (0x8B, 0x8B, 0x8B, 0xFF)
GUI_SLOT_EDGE_DARK = (0x37, 0x37, 0x37, 0xFF)
GUI_SLOT_EDGE_LIGHT = (0xFF, 0xFF, 0xFF, 0xFF)
# 与 ConverterMenu 的槽位常量逐字一致：catalyst(44,22) / input(80,22) / preview(116,22) / 背包(8,84) 9x3 / 快捷栏(8,142)
GUI_CATALYST_SLOT = (44, 22)
GUI_INPUT_SLOT = (80, 22)
GUI_PREVIEW_SLOT = (116, 22)
GUI_INVENTORY_ORIGIN = (8, 84)
GUI_HOTBAR_ORIGIN = (8, 142)
GUI_INVENTORY_ROWS = 3
GUI_INVENTORY_COLUMNS = 9
GUI_SLOT_PITCH = 18
GUI_SLOT_SIZE = 16
# 标题区（titleLabelY=6）与背包区文案（inventoryLabelY=72）各压一条刻线，把文案带与槽位带分开。
GUI_DIVIDER_Y = 18
GUI_INVENTORY_DIVIDER_Y = 66
# 催化剂槽(44,22)→输入槽(80,22) 的空当是 61..78、输入槽→预览槽(116,22) 的空当是 97..114，各画一个箭头。
GUI_CATALYST_ARROW = (62, 30, 76)
GUI_ARROW = (99, 30, 113)
# 转换按钮的矩形底，与 ConverterScreen 的 BUTTON_X/Y/WIDTH/HEIGHT 逐像素一致：(x0,y0,x1,y1)，含 1 像素边框。
GUI_BUTTON = (8, 42, 167, 59)

# 0.0.3 追加里唯一还是程序绘制的那张（震动豆）：调色板集中在这里，改配色不用翻画法。
# 取值向作者原画（friend_chives / chive_seeds 的 1254x1254 素材）看齐：作者那两张的实色主体
# 都很深（韭菜 #0D2C12~#123715、种子 #141314~#2F2F34），靠少量亮部提结构，所以这里的豆荚
# 也用「近黑描边 + 暗棕主体 + 克制的亮豆 + 冷灰震纹」，不用高饱和橙棕。
CLEAR = (0x00, 0x00, 0x00, 0x00)
POD_DARK = (0x12, 0x0C, 0x07, 0xFF)
POD_MID = (0x2E, 0x1C, 0x0E, 0xFF)
POD_BEAN = (0x55, 0x37, 0x1A, 0xFF)
POD_HI = (0x76, 0x51, 0x27, 0xFF)
POD_STEM = (0x3F, 0x7A, 0x33, 0xFF)
VIB_WAVE = (0xA9, 0xB6, 0xBC, 0xFF)

# --- v0.1.0 · 压缩麦三张（F-164 / 23 号 §4.6）---
# 9 档灰阶的黄褐色板（亮 → 暗）。与「H 42° / S 0.55、L 沿用灰阶」的公式在 9 个灰阶上逐档相等，
# 但 #4B4B4B 那一档公式会因浮点取整给出 #745C22，故这里直接用文档的实测色值当唯一事实源。
HAY_RAMP: tuple[tuple[int, int, int], ...] = (
    (0xDA, 0xBF, 0x80), (0xD6, 0xB7, 0x70), (0xCC, 0xA6, 0x4E), (0xBF, 0x96, 0x37), (0xAE, 0x89, 0x32),
    (0x9D, 0x7B, 0x2D), (0x96, 0x76, 0x2C), (0x85, 0x69, 0x27), (0x74, 0x5B, 0x22),
)
# 现侧贴图读出来的 9 档灰阶（23 号 §4.6 表的第一列，亮 → 暗），与 HAY_RAMP 一一对应。
HAY_GRAYS: tuple[int, ...] = (0xAD, 0xA3, 0x8D, 0x7B, 0x70, 0x65, 0x61, 0x56, 0x4B)
HAY_TONE: dict[int, tuple[int, int, int]] = dict(zip(HAY_GRAYS, HAY_RAMP))
# 顶面 5 档（#9D7B2D ~ #DABF80）；底面「同图案整体压暗一档」= 每个下标沿 9 档下移 3 级（#745B22 ~ #BF9637）。
HAY_TOP_LEVELS: tuple[int, ...] = (0, 1, 2, 4, 5)
HAY_BOTTOM_LEVELS: tuple[int, ...] = (3, 4, 5, 7, 8)
# 底面捆绳落在中心 4x4 区域（左含右不含）。
HAY_ROPE_BOX: tuple[int, int, int, int] = (6, 6, 10, 10)

# --- v0.1.0 · 朋友们韭的四阶段（F-162 / 23 号 §3.4）---
# 4 个绿直接沿用 power_beet 已入库的绿族，亮度升序：茎 < 叶基 < 叶中 < 叶尖。
CHIVE_COLORS: dict[str, tuple[int, int, int, int]] = {
    "a": (0x26, 0x63, 0x25, 0xFF),
    "b": (0x2B, 0x70, 0x2A, 0xFF),
    "c": (0x4A, 0x8F, 0x28, 0xFF),
    "d": (0x55, 0xAB, 0x2D, 0xFF),
}
# 23 号 §3.4 的目标不透明像素数（6 / 14 / 40 / 90），画完逐张断言。
CHIVE_OPAQUE: tuple[int, ...] = (6, 14, 40, 90)

# --- 狂野苹果（v0.1.0 · F-163；v0.1.1 · D-126 改为 64x64 源图原样直通）---
# 作者给的 64x64 RGBA / 6,946 B 源图。**不进 jar**：这里只存路径，生成时读一次。
WILD_APPLE_SOURCE = Path(r"C:\Users\DASHpig\Downloads\wild_apple_64x64.png")
WILD_APPLE_SOURCE_ENV = "FARMSTORM_WILD_APPLE_SOURCE"


@dataclass(frozen=True)
class SourceRef:
    """一条原版贴图引用：图层（item / block）+ 贴图名（不含扩展名）。"""

    layer: str
    name: str

    def label(self) -> str:
        return f"{self.layer}/{self.name}"


@dataclass(frozen=True)
class Spec:
    """一张待生成材质的目标描述。"""

    rel_path: str
    kind: str
    zh_name: str
    candidates: tuple[SourceRef, ...] = ()
    fit: int = TEXTURE_SIZE
    tint: tuple[int, int, int] | None = None
    overlay: SourceRef | None = None
    # TECH §6.5：压缩干草块必须是「原版干草块侧面的灰度版」——R=G=B 的纯灰度，
    # 不是「降低饱和度」（那会留色偏），所以走 convert("L") 再回 RGBA。
    monochrome: bool = False
    # v0.1.0 · F-164：灰度结果再按 23 号 §4.6 的 9 档映射表换成干草黄褐（在 monochrome 之后应用）。
    hay_tone: bool = False
    # 0.0.3 追加：程序绘制图标取哪个画法（DRAWERS 的键）；None 表示走原版来源解析。
    drawer: str | None = None


@dataclass(frozen=True)
class Resolved:
    """一次源解析的结果：图（或 None）+ 命中的是哪条候选 + 主源名字（供缺失时报告）。"""

    image: Image.Image | None
    status: str
    used: str
    primary: str


@dataclass
class Report:
    """三类清单 + 计数（局部对象，非全局状态）。"""

    buckets: dict[str, list[tuple[str, str, str]]] = field(
        default_factory=lambda: {STATUS_VANILLA: [], STATUS_SUBSTITUTE: [], STATUS_MISSING: []}
    )
    planned: int = 0
    written: int = 0
    procedural: int = 0

    def record(self, spec: Spec, resolved: Resolved) -> None:
        # GUI 背景与 0.0.3 追加的程序绘制图标都是程序画的，不参与「原版命中 / 退回 / 找不到」的三类统计。
        if spec.kind == KIND_GUI or spec.drawer is not None:
            self.procedural += 1
            return
        self.buckets[resolved.status].append(
            (spec.rel_path, resolved.used, resolved.primary)
        )


def refs(*entries: tuple[str, str]) -> tuple[SourceRef, ...]:
    """把 (图层, 贴图名) 序列转成候选链。"""
    return tuple(SourceRef(layer, name) for layer, name in entries)


def one(name: str, layer: str = LAYER_BLOCK) -> tuple[SourceRef, ...]:
    """单候选链。"""
    return (SourceRef(layer, name),)


def stage_set(a: str, b: str, c: str, d: str, layer: str = LAYER_BLOCK) -> tuple[tuple[SourceRef, ...], ...]:
    """四个阶段的单候选链（原版有完整阶段贴图时用）。"""
    return tuple(one(name, layer) for name in (a, b, c, d))


# --- 22 张物品图标（15 作物 + 6 播种物 + 转换器）---
# 原版 1.21.4 起方块类物品不再单独出 item 贴图（melon / pumpkin / cactus / torchflower / *_mushroom），
# 因此这些条目把 block 图层放在次级候选，主源仍按「原版同名 item 贴图」书写，便于作者核对。
ICONS: tuple[tuple[str, str, tuple[tuple[str, str], ...]], ...] = (
    ("puffed_wheat", "膨胀麦", (("item", "wheat"),)),
    ("blast_carrot", "胡罗贝", (("item", "carrot"),)),
    ("ubi_server", "育碧服务器", (("item", "potato"),)),
    ("toxic_ubi_server", "毒育碧服务器", (("item", "poisonous_potato"),)),
    ("power_beet", "甜菜！", (("item", "beetroot"),)),
    ("melon_slice_bomb", "瓜片子", (("item", "melon_slice"),)),
    ("melon_grenade", "瓜！", (("item", "melon"), ("block", "melon_side"))),
    ("pumpkin_turret", "man瓜", (("item", "pumpkin"), ("block", "pumpkin_side"))),
    ("awakened_torchflower", "激活的火把花", (("item", "torchflower"), ("block", "torchflower"))),
    ("awakened_pitcher_plant", "激活的瓶子草", (("item", "pitcher_plant"),)),
    ("fang_trap_cactus", "赢过输过没死过", (("item", "cactus"), ("block", "cactus_side"))),
    ("bamboo_firecracker", "爆竹", (("item", "bamboo"),)),
    ("red_mushroom_puff", "？！菇菇！？", (("item", "red_mushroom"), ("block", "red_mushroom"))),
    ("brown_mushroom_puff", "？！棕色菇菇色棕！？", (("item", "brown_mushroom"), ("block", "brown_mushroom"))),
    ("lucky_berry", "幸运浆果", (("item", "sweet_berries"),)),
    ("puffed_wheat_seeds", "膨胀麦种子", (("item", "wheat_seeds"),)),
    ("power_beet_seeds", "甜菜！种子", (("item", "beetroot_seeds"),)),
    ("melon_grenade_seeds", "瓜！种子", (("item", "melon_seeds"),)),
    ("pumpkin_turret_seeds", "man瓜种子", (("item", "pumpkin_seeds"),)),
    ("awakened_torchflower_seeds", "激活的火把花种子", (("item", "torchflower_seeds"),)),
    ("awakened_pitcher_plant_pod", "激活的瓶子草种荚", (("item", "pitcher_pod"),)),
    ("converter", "转换器", (("item", "converter"), ("block", "crafting_table_front"))),
    # V-23：F-65 注册的 4 个新作物物品没有图标，客户端 datagen 从 F-65 起跑不通。
    # 源贴图是临时起手，最终外观由 F-95 审定（届时只改这一张表即可）。
    # 原型是绯红菌（转换配方的原料也是 minecraft:crimson_fungus），不是药水瓶。
    ("molotov", "莫洛托夫！", (("item", "crimson_fungus"), ("block", "crimson_fungus"))),
    ("zangief_cane", "桑吉尔蔗", (("item", "sugar_cane"),)),
    ("wild_apple", "狂野苹果", (("item", "apple"),)),
    ("compressed_puffed_wheat", "压缩膨胀麦", (("block", "hay_block_side"),)),
)

# 需要走 HAY_TONE 映射表的方块贴图（v0.1.0 · F-164 的压缩麦侧面）。
HAY_TONE_BLOCKS = frozenset({"compressed_hay_block"})

# --- 14 个作物 × 4 阶段（方块 ID 去掉后缀后的 <base>）---
STAGE_CROPS: tuple[tuple[str, str, tuple[tuple[SourceRef, ...], ...], tuple[int, int, int] | None], ...] = (
    ("puffed_wheat", "膨胀麦", stage_set("wheat_stage0", "wheat_stage3", "wheat_stage5", "wheat_stage7"), None),
    ("blast_carrot", "胡罗贝", stage_set("carrots_stage0", "carrots_stage1", "carrots_stage2", "carrots_stage3"), None),
    ("ubi_server", "育碧服务器", stage_set("potatoes_stage0", "potatoes_stage1", "potatoes_stage2", "potatoes_stage3"), None),
    # 毒育碧没有原版对应物：复用马铃薯阶段贴图并整体染色，保证与育碧、与原版都能区分。
    ("toxic_ubi_server", "毒育碧服务器",
     (refs(("block", "poisonous_potato_stage0"), ("block", "potatoes_stage0")),
      refs(("block", "poisonous_potato_stage1"), ("block", "potatoes_stage1")),
      refs(("block", "poisonous_potato_stage2"), ("block", "potatoes_stage2")),
      refs(("block", "poisonous_potato_stage3"), ("block", "potatoes_stage3"))),
     (0xA0, 0xD0, 0x40)),
    ("power_beet", "甜菜！", stage_set("beetroots_stage0", "beetroots_stage1", "beetroots_stage2", "beetroots_stage3"), None),
    ("melon_grenade", "瓜！", stage_set("melon_stem", "melon_stem", "melon_stem", "melon_stem"), None),
    ("pumpkin_turret", "man瓜", stage_set("pumpkin_stem", "pumpkin_stem", "pumpkin_stem", "pumpkin_stem"), None),
    ("awakened_torchflower", "激活的火把花",
     (one("torchflower_crop_stage0"), one("torchflower_crop_stage0"),
      one("torchflower_crop_stage1"), one("torchflower_crop_stage1")), None),
    ("awakened_pitcher_plant", "激活的瓶子草",
     (refs(("block", "pitcher_crop_bottom_stage_1"), ("block", "pitcher_crop_bottom")),
      refs(("block", "pitcher_crop_bottom_stage_2"), ("block", "pitcher_crop_bottom")),
      refs(("block", "pitcher_crop_bottom_stage_3"), ("block", "pitcher_crop_top")),
      refs(("block", "pitcher_crop_top_stage_4"), ("block", "pitcher_crop_top"))), None),
    ("fang_trap_cactus", "赢过输过没死过", stage_set("cactus_side", "cactus_side", "cactus_side", "cactus_side"), None),
    ("bamboo_firecracker", "爆竹",
     stage_set("bamboo_stage0", "bamboo_singleleaf", "bamboo_small_leaves", "bamboo_large_leaves"), None),
    ("red_mushroom_puff", "？！菇菇！？",
     stage_set("red_mushroom", "red_mushroom", "red_mushroom_block", "red_mushroom_block"), None),
    ("brown_mushroom_puff", "？！棕色菇菇色棕！？",
     stage_set("brown_mushroom", "brown_mushroom", "brown_mushroom_block", "brown_mushroom_block"), None),
    ("lucky_berry", "幸运浆果",
     stage_set("sweet_berry_bush_stage0", "sweet_berry_bush_stage1", "sweet_berry_bush_stage2", "sweet_berry_bush_stage3"),
     None),
)

# --- 5 个部署方块 + 3 张转换器面 ---
# FIX-5 起模型只引用 converter_top 与四个作物侧面；converter_side / converter_front 仍照旧生成，
# 保留给「以后想换回旧外观」的场合（与 R-42 同口径），不删条目以免输出集合缺文件。
BLOCK_TEXTURES: tuple[tuple[str, str, tuple[tuple[str, str], ...], bool], ...] = (
    ("blast_carrot", "部署·胡罗贝", (("block", "carrots_stage3"),), False),
    ("pumpkin_turret", "部署·man瓜", (("block", "carved_pumpkin"), ("block", "pumpkin_side")), False),
    ("awakened_torchflower", "部署·激活的火把花", (("block", "torchflower"),), False),
    ("awakened_pitcher_plant", "部署·激活的瓶子草",
     (("item", "pitcher_plant"), ("block", "pitcher_crop_top")), False),
    ("fang_trap_cactus", "部署·赢过输过没死过", (("block", "cactus_side"),), False),
    # V-23：F-65 注册了这两个方块却没建外观，导致 runData --all 从 F-65 起就抛「贴图不存在」。
    # 陷阱类部署方块在世界里不渲染几何，这张 PNG 只作破坏粒子；压缩干草块则是 cubeAll 的正式贴图。
    ("zangief_cane", "部署·桑吉尔蔗（仅作破坏粒子）", (("block", "sugar_cane"),), False),
    # 第 4 个元素 = 是否灰度化。取值链是「原版 hay_block_side → L 通道（9 档灰阶）→ HAY_TONE 换干草黄褐」：
    # 先灰度这一步不能省，23 号 §4.6 的映射表就是按这 9 个灰阶写的（v0.1.0 · F-164）。
    ("compressed_hay_block", "压缩干草块", (("block", "hay_block_side"),), True),
    # v0.1.0 · F-164：落地后顶 / 底各一张 16x16 切面，由 BLOCK_DRAWERS 画（不读原版像素，无随机数）；
    # 候选链仍写 hay_block_side，标明三张同属一套黄褐色板（23 号 §4.6 的六面分配表）。
    ("compressed_hay_block_top", "压缩干草块·顶面（束纹 + 描边）", (("block", "hay_block_side"),), False),
    ("compressed_hay_block_bottom", "压缩干草块·底面（压暗一档 + 十字捆绳）", (("block", "hay_block_side"),), False),
    # 顶面兼底面：原版 crafting_table 模型的 up / down 都用 crafting_table_top，这里照抄同一个口径。
    ("converter_top", "转换器·顶面（兼底面）", (("block", "crafting_table_top"),), False),
    ("converter_side", "转换器·净工作台侧面（模型已不引用）", (("block", "crafting_table_side"),), False),
    ("converter_front", "转换器·正面（模型已不引用）", (("block", "furnace_front"),), False),
)

# --- 转换器四个作物侧面（FIX-5）：原版 crafting_table_side 作底，中央叠一个作物图标 ---
# 顺序即 vanilla 朝向口径（north / east / south / west），与 ModBlockStateProvider 的 CONVERTER_FACE_TEXTURES 一一对应。
CONVERTER_SIDE_OVERLAYS: tuple[tuple[str, str, tuple[str, str]], ...] = (
    ("wheat", "小麦", ("item", "wheat")),
    ("carrot", "胡萝卜", ("item", "carrot")),
    ("potato", "马铃薯", ("item", "potato")),
    ("beetroot", "甜菜根", ("item", "beetroot")),
)
# 图标等比缩到 10px 内（16x16 里留 3px 边距），外加 1px 深棕描边 —— 深色木纹上仍能读出轮廓。
CONVERTER_OVERLAY_EXTENT = 10
CONVERTER_OVERLAY_OUTLINE = (0x1E, 0x14, 0x0B, 0xFF)
CONVERTER_OVERLAY_NEIGHBORS = (
    (-1, -1), (0, -1), (1, -1), (-1, 0), (1, 0), (-1, 1), (0, 1), (1, 1),
)


def compose_crop_side(
    base: Image.Image, overlay: Image.Image, extent: int = CONVERTER_OVERLAY_EXTENT
) -> Image.Image:
    """把作物图标居中叠到方块底图上：裁掉透明边 → 等比缩到 extent 内（NEAREST）→ 1px 暗描边。"""
    canvas = base.convert("RGBA")
    if canvas.size != (TEXTURE_SIZE, TEXTURE_SIZE):
        canvas = canvas.resize((TEXTURE_SIZE, TEXTURE_SIZE), Image.Resampling.NEAREST)

    sprite = overlay.convert("RGBA")
    box = sprite.getchannel("A").getbbox()
    if box is not None:
        sprite = sprite.crop(box)
    scale = extent / max(sprite.width, sprite.height)
    width = min(extent, max(1, round(sprite.width * scale)))
    height = min(extent, max(1, round(sprite.height * scale)))

    layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    layer.alpha_composite(sprite.resize((width, height), Image.Resampling.NEAREST),
                          ((TEXTURE_SIZE - width) // 2, (TEXTURE_SIZE - height) // 2))

    # 描边只落在图标的「透明」邻域上，不盖图标本身：8 邻域手写膨胀，避免为一格描边引入新依赖。
    alpha = layer.getchannel("A").load()
    outline = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    outline_pixels = outline.load()
    for y in range(TEXTURE_SIZE):
        for x in range(TEXTURE_SIZE):
            if alpha[x, y]:
                continue
            for dx, dy in CONVERTER_OVERLAY_NEIGHBORS:
                nx, ny = x + dx, y + dy
                if 0 <= nx < TEXTURE_SIZE and 0 <= ny < TEXTURE_SIZE and alpha[nx, ny]:
                    outline_pixels[x, y] = CONVERTER_OVERLAY_OUTLINE
                    break

    canvas.alpha_composite(outline)
    canvas.alpha_composite(layer)
    return canvas


def build_specs() -> tuple[Spec, ...]:
    """汇总全部 91 条目标（顺序：item 22 → stage 56 → block 12 → gui 1）。"""
    specs: list[Spec] = []

    for item_id, zh_name, chain in ICONS:
        specs.append(
            Spec(
                rel_path=f"textures/item/{item_id}.png",
                kind=KIND_ICON,
                zh_name=zh_name,
                candidates=refs(*chain),
                drawer=item_id if item_id in ICON_DRAWERS else None,
            )
        )

    for base, zh_name, stage_chain, tint in STAGE_CROPS:
        for index, candidates in enumerate(stage_chain):
            specs.append(
                Spec(
                    rel_path=f"textures/block/{base}_stage{index}.png",
                    kind=KIND_STAGE,
                    zh_name=f"{zh_name} 阶段 {index}",
                    candidates=candidates,
                    fit=STAGE_SCALES[index],
                    tint=tint,
                )
            )

    for name, zh_name, chain, monochrome in BLOCK_TEXTURES:
        specs.append(
            Spec(
                rel_path=f"textures/block/{name}.png",
                kind=KIND_BLOCK,
                zh_name=zh_name,
                candidates=refs(*chain),
                monochrome=monochrome,
                hay_tone=name in HAY_TONE_BLOCKS,
                drawer=name if name in BLOCK_DRAWERS else None,
            )
        )

    for suffix, zh_name, overlay in CONVERTER_SIDE_OVERLAYS:
        specs.append(
            Spec(
                rel_path=f"textures/block/converter_side_{suffix}.png",
                kind=KIND_BLOCK,
                zh_name=f"转换器·{zh_name}侧面",
                candidates=one("crafting_table_side"),
                overlay=SourceRef(*overlay),
            )
        )

    specs.append(Spec(rel_path="textures/gui/converter.png", kind=KIND_GUI, zh_name="转换器 GUI"))
    return tuple(specs)


class AssetSource:
    """原版资源读取器：客户端 jar（zipfile）或解包后的资源目录，两种形态对外接口一致。"""

    def __init__(self, root: Path, warnings: list[str]) -> None:
        self.root = root
        self.warnings = warnings
        self._zip: zipfile.ZipFile | None = None
        if root.is_file() and zipfile.is_zipfile(root):
            self._zip = zipfile.ZipFile(root)
        elif not root.is_dir():
            warnings.append(f"--vanilla-assets 既不是目录也不是可读 jar：{root}")

    def describe(self) -> str:
        return "jar" if self._zip is not None else "dir"

    def close(self) -> None:
        if self._zip is not None:
            self._zip.close()

    def _zip_entry(self, ref: SourceRef) -> str | None:
        assert self._zip is not None
        entry = f"assets/minecraft/textures/{ref.layer}/{ref.name}.png"
        try:
            self._zip.getinfo(entry)
        except KeyError:
            return None
        return entry

    def _dir_candidates(self, ref: SourceRef) -> tuple[Path, ...]:
        tail = (ref.layer, f"{ref.name}.png")
        return (
            self.root.joinpath("minecraft", "textures", *tail),
            self.root.joinpath("assets", "minecraft", "textures", *tail),
            self.root.joinpath("textures", *tail),
        )

    def load(self, ref: SourceRef) -> Image.Image | None:
        """读取一张原版贴图并转成 RGBA；不存在或无法解析时返回 None。"""
        try:
            if self._zip is not None:
                entry = self._zip_entry(ref)
                if entry is None:
                    return None
                with Image.open(BytesIO(self._zip.read(entry))) as image:
                    return image.convert("RGBA")
            for path in self._dir_candidates(ref):
                if path.is_file():
                    with Image.open(path) as image:
                        return image.convert("RGBA")
            return None
        except (OSError, zipfile.BadZipFile) as exc:
            self.warnings.append(f"原版贴图无法读取：{ref.label()}（{exc}）")
            return None

    def resolve(self, spec: Spec) -> Resolved:
        """按候选链解析：命中第一条就返回；全链缺失返回 None 图 + 「找不到」；带 overlay 的目标把叠加层合进主图。"""
        primary = spec.candidates[0].label() if spec.candidates else "-"
        for index, ref in enumerate(spec.candidates):
            image = self.load(ref)
            if image is not None:
                status = STATUS_VANILLA if index == 0 else STATUS_SUBSTITUTE
                used = ref.label()
                if spec.overlay is not None:
                    overlay = self.load(spec.overlay)
                    if overlay is None:
                        self.warnings.append(
                            f"叠加层缺失：{spec.overlay.label()}（{spec.rel_path} 只用底图）"
                        )
                    else:
                        image = compose_crop_side(image, overlay)
                        used = f"{used} + {spec.overlay.label()}"
                return Resolved(image, status, used, primary)
        return Resolved(None, STATUS_MISSING, "纯色占位色块", primary)


class NullSource:
    """--additions 用的空源：那 5 张不读任何原版贴图（1 张程序绘制 + 4 张纯色占位）。"""

    def __init__(self) -> None:
        self.warnings: list[str] = []

    def describe(self) -> str:
        return "程序绘制 / 占位，不读原版贴图"

    def close(self) -> None:
        return None

    def resolve(self, spec: Spec) -> Resolved:
        if spec.drawer is not None:
            return Resolved(None, STATUS_MISSING, "程序绘制", "-")
        return Resolved(None, STATUS_MISSING, "纯色占位色块", "-")


def make_placeholder() -> Image.Image:
    """源材质缺失时使用的纯色占位图。"""
    return Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), PLACEHOLDER_COLOR)


def apply_tint(image: Image.Image, tint: tuple[int, int, int]) -> Image.Image:
    """按通道相乘染色，保留 alpha（用于没有原版对应物的变体作物）。"""
    red, green, blue, alpha = image.split()
    red = red.point(lambda value: value * tint[0] // 255)
    green = green.point(lambda value: value * tint[1] // 255)
    blue = blue.point(lambda value: value * tint[2] // 255)
    return Image.merge("RGBA", (red, green, blue, alpha))


def hay_tone_from_level(level: int) -> tuple[int, int, int]:
    """映射表之外的灰阶兜底：H 42° / S 0.55、L 沿用灰阶（23 号 §4.6 的改色规则）。"""
    lightness = level / 255
    chroma = (1 - abs(2 * lightness - 1)) * 0.55
    second = chroma * (1 - abs(42 / 60 % 2 - 1))
    offset = lightness - chroma / 2
    return (
        min(255, round((chroma + offset) * 255)),
        min(255, round((second + offset) * 255)),
        min(255, round(offset * 255)),
    )


def apply_hay_tone(image: Image.Image) -> Image.Image:
    """把灰度图按 23 号 §4.6 的 9 档映射表换成干草黄褐，像素布局与 alpha 原样保留。"""
    toned = image.convert("RGBA")
    pixels = toned.load()
    for y in range(toned.height):
        for x in range(toned.width):
            red, green, blue, alpha = pixels[x, y]
            level = (299 * red + 587 * green + 114 * blue) // 1000
            pixels[x, y] = HAY_TONE.get(level, hay_tone_from_level(level)) + (alpha,)
    return toned


def fit_bottom_center(base: Image.Image, extent: int) -> Image.Image:
    """把底图等比缩放成 extent x extent 并底边居中贴进 16x16 透明画布。"""
    canvas = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    scaled = base.convert("RGBA")
    if scaled.size != (extent, extent):
        scaled = scaled.resize((extent, extent), Image.Resampling.NEAREST)
    canvas.alpha_composite(scaled, ((TEXTURE_SIZE - extent) // 2, TEXTURE_SIZE - extent))
    return canvas


def draw_gui_background() -> Image.Image:
    """程序绘制 176x166 的容器背景：面板斜边 + 标题/背包刻线 + 两条箭头 + 转换按钮底 + 全部槽位。"""
    image = Image.new("RGBA", GUI_SIZE, GUI_BG)
    draw = ImageDraw.Draw(image)
    width, height = GUI_SIZE

    draw.line(((0, 0), (width - 1, 0)), fill=GUI_EDGE_LIGHT)
    draw.line(((0, 0), (0, height - 1)), fill=GUI_EDGE_LIGHT)
    draw.line(((width - 1, 0), (width - 1, height - 1)), fill=GUI_EDGE_DARK)
    draw.line(((0, height - 1), (width - 1, height - 1)), fill=GUI_EDGE_DARK)

    for divider_y in (GUI_DIVIDER_Y, GUI_INVENTORY_DIVIDER_Y):
        draw.line(((1, divider_y), (width - 2, divider_y)), fill=GUI_EDGE_DARK)
        draw.line(((1, divider_y + 1), (width - 2, divider_y + 1)), fill=GUI_EDGE_LIGHT)

    for arrow_x0, arrow_y, arrow_x1 in (GUI_CATALYST_ARROW, GUI_ARROW):
        draw.line(((arrow_x0, arrow_y), (arrow_x1 - 3, arrow_y)), fill=GUI_EDGE_DARK)
        draw.polygon(((arrow_x1 - 4, arrow_y - 4), (arrow_x1, arrow_y), (arrow_x1 - 4, arrow_y + 4)),
                     fill=GUI_EDGE_DARK)

    # 转换按钮的矩形底：位置与 ConverterScreen 的按钮 bounds 重合，运行时原版按钮精灵盖在同一块上，
    # 所以这里的描边只是让独立贴图也能看出「哪儿可点」，不会和控件叠出双边框。
    button_x0, button_y0, button_x1, button_y1 = GUI_BUTTON
    draw.rectangle((button_x0, button_y0, button_x1, button_y1), fill=GUI_SLOT_FILL)
    draw.line(((button_x0, button_y0), (button_x1, button_y0)), fill=GUI_EDGE_DARK)
    draw.line(((button_x0, button_y0), (button_x0, button_y1)), fill=GUI_EDGE_DARK)
    draw.line(((button_x0, button_y1), (button_x1, button_y1)), fill=GUI_EDGE_LIGHT)
    draw.line(((button_x1, button_y0), (button_x1, button_y1)), fill=GUI_EDGE_LIGHT)

    slots = [GUI_CATALYST_SLOT, GUI_INPUT_SLOT, GUI_PREVIEW_SLOT]
    base_x, base_y = GUI_INVENTORY_ORIGIN
    for row in range(GUI_INVENTORY_ROWS):
        for column in range(GUI_INVENTORY_COLUMNS):
            slots.append((base_x + column * GUI_SLOT_PITCH, base_y + row * GUI_SLOT_PITCH))
    hotbar_x, hotbar_y = GUI_HOTBAR_ORIGIN
    for column in range(GUI_INVENTORY_COLUMNS):
        slots.append((hotbar_x + column * GUI_SLOT_PITCH, hotbar_y))

    for x, y in slots:
        left, top = x - 1, y - 1
        right, bottom = x + GUI_SLOT_SIZE, y + GUI_SLOT_SIZE
        draw.rectangle((left, top, right, bottom), fill=GUI_SLOT_FILL)
        draw.line(((left, top), (right, top)), fill=GUI_SLOT_EDGE_DARK)
        draw.line(((left, top), (left, bottom)), fill=GUI_SLOT_EDGE_DARK)
        draw.line(((left, bottom), (right, bottom)), fill=GUI_SLOT_EDGE_LIGHT)
        draw.line(((right, top), (right, bottom)), fill=GUI_SLOT_EDGE_LIGHT)

    # 催化剂槽的空槽提示：正中的菱形轮廓（|dx|+|dy| = 3），暗示这里要放下界之星。
    center_x, center_y = GUI_CATALYST_SLOT[0] + GUI_SLOT_SIZE // 2, GUI_CATALYST_SLOT[1] + GUI_SLOT_SIZE // 2
    for offset_y in range(-3, 4):
        for offset_x in range(-3, 4):
            if abs(offset_x) + abs(offset_y) == 3:
                draw.point((center_x + offset_x, center_y + offset_y), fill=GUI_SLOT_EDGE_LIGHT)
    return image


def draw_vibration_bean() -> Image.Image:
    """16x16「震动豆」物品图标：豆荚 + 三粒豆 + 右侧震纹（D-90；§1.4(d) 第 8 张）。

    作者只给了朋友的韭与韭菜种子的原画（plan0.0.3.txt），震动豆没有贴图素材，因此这一张
    仍是程序绘制；画法与配色按作者那两张的口径来 —— 近黑描边、暗主体、少量亮部，
    而不是高饱和色块，这样三张物品图标并排时不显得是两个人画的。
    """
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), CLEAR)
    draw = ImageDraw.Draw(image)
    # 荚：外层近黑描边 + 内层暗棕主体
    draw.ellipse((1, 4, 12, 13), fill=POD_DARK)
    draw.ellipse((2, 5, 11, 12), fill=POD_MID)
    # 三粒豆：竖条 + 每粒顶部一点亮部
    for left in (3, 6, 9):
        draw.ellipse((left, 6, left + 1, 11), fill=POD_BEAN)
        draw.point((left, 7), fill=POD_HI)
    # 荚面上沿一道高光
    draw.line(((3, 5), (8, 5)), fill=POD_HI)
    # 蒂：一段暗绿，与作者韭菜原画的绿色同族
    draw.line(((4, 4), (2, 1)), fill=POD_STEM, width=2)
    # 震纹：右侧三道，用种子原画那套冷灰亮部色
    for top, right in ((4, 14), (7, 15), (10, 14)):
        draw.line(((13, top), (right, top - 1)), fill=VIB_WAVE)
    return image


# --- v0.1.0 · F-162：朋友的韭四阶段像素表（23 号 §3.4 的形态表）---
# '.' 透明，其余字母见 CHIVE_COLORS。四张的共同骨架是「底部中央 1 px 深色茎 + 中央一根向上抽薹的叶
# + 左右各若干根竖向叶片」；阶段推进只加叶片高度与叶片根数，色相不动，所以远处看是「同一株长大了」。
# 高度 4 / 8 / 12 / 16 行分别对齐 SHAPE_BY_AGE 的四级碰撞箱，不透明像素数见 CHIVE_OPAQUE。
CHIVE_STAGE_ART: tuple[tuple[str, ...], ...] = (
    (
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        ".......a........",
        ".......a........",
        "......bab.......",
        ".......a........",
    ),
    (
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        ".......c........",
        ".......c........",
        ".......b........",
        ".......bc.......",
        ".......ab.......",
        ".......a........",
        "......ba........",
        "......babb......",
    ),
    (
        "................",
        "................",
        "................",
        "................",
        ".......d........",
        ".......c........",
        ".......c........",
        ".......c........",
        "......db........",
        "......dbd.......",
        ".....dcac.......",
        ".....ccac.......",
        ".....ccacd......",
        "....cccacc......",
        "....bbbabb......",
        "....bbbabb......",
    ),
    (
        "......dd........",
        "......ddd.......",
        "......dcd.......",
        ".....ddcd.......",
        ".....ddcd.......",
        ".....ddcdd......",
        ".....dcbcd......",
        ".....ccbcd......",
        "....dccacc......",
        "....dccacc......",
        "....dccaccd.....",
        "...dcccaccd.....",
        "...dcccaccd.....",
        "...ccccaccc.....",
        "...bbbbabbb.....",
        "...bbbbabbb.....",
    ),
)


def draw_chive_stage(index: int) -> Image.Image:
    """按 CHIVE_STAGE_ART[index] 取色画一张 16x16 植株（纯查表，无随机数）。"""
    art = CHIVE_STAGE_ART[index]
    if len(art) != TEXTURE_SIZE or any(len(row) != TEXTURE_SIZE for row in art):
        raise ValueError(f"朋友的韭阶段 {index} 的像素表不是 {TEXTURE_SIZE}x{TEXTURE_SIZE}")
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), CLEAR)
    opaque = 0
    for y, row in enumerate(art):
        for x, symbol in enumerate(row):
            color = CHIVE_COLORS.get(symbol)
            if color is None:
                continue
            image.putpixel((x, y), color)
            opaque += 1
    if opaque != CHIVE_OPAQUE[index]:
        raise ValueError(f"朋友的韭阶段 {index} 的不透明像素 {opaque}，应为 {CHIVE_OPAQUE[index]}")
    return image


# --- v0.1.0 · F-164：压缩麦的顶 / 底两张切面 ---
def draw_hay_cross_section(levels: tuple[int, ...]) -> Image.Image:
    """16x16 干草捆切面：四周 1 px 描边（本面最暗一档）+ 内部每 2 行一组的横向束纹（亮档 / 暗档交替）。"""
    ramp = [HAY_RAMP[level] for level in levels]
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), ramp[-1] + (0xFF,))
    for y in range(1, TEXTURE_SIZE - 1):
        pair = (y - 1) // 2
        bright, dark = (ramp[0], ramp[1]) if pair % 2 == 0 else (ramp[2], ramp[3])
        color = bright if (y - 1) % 2 == 0 else dark
        for x in range(1, TEXTURE_SIZE - 1):
            image.putpixel((x, y), color + (0xFF,))
    return image


def draw_hay_top() -> Image.Image:
    """顶面：秸秆横截面，5 档 #9D7B2D ~ #DABF80（23 号 §4.6 的六面分配表）。"""
    return draw_hay_cross_section(HAY_TOP_LEVELS)


def draw_hay_bottom() -> Image.Image:
    """底面：同图案整体压暗一档 + 中心 4x4 区域 2 px 宽的十字捆绳，5 档 #745B22 ~ #BF9637。"""
    image = draw_hay_cross_section(HAY_BOTTOM_LEVELS)
    rope = HAY_RAMP[HAY_BOTTOM_LEVELS[-1]] + (0xFF,)
    left, top, right, bottom = HAY_ROPE_BOX
    center_x = (left + right) // 2
    center_y = (top + bottom) // 2
    for x in (center_x - 1, center_x):
        for y in range(top, bottom):
            image.putpixel((x, y), rope)
    for y in (center_y - 1, center_y):
        for x in range(left, right):
            image.putpixel((x, y), rope)
    return image


# --- 狂野苹果：v0.1.0 · F-163 走 16 网格取色，v0.1.1 · D-126 改为源图原样直通 ---
def wild_apple_source() -> Path:
    """狂野苹果源图路径；FARMSTORM_WILD_APPLE_SOURCE 可指向别处的同素材。"""
    override = os.environ.get(WILD_APPLE_SOURCE_ENV)
    return Path(override) if override else WILD_APPLE_SOURCE


def wild_apple_source_size() -> tuple[int, int] | None:
    """源图的实际尺寸；源图缺失时返回 None（此时由 compose() 退回候选链的 16x16 产物）。"""
    path = wild_apple_source()
    if not path.is_file():
        return None
    with Image.open(path) as raw:
        return raw.size


def draw_wild_apple() -> Image.Image | None:
    """狂野苹果：源图原样直通（D-126）——不降采样、不进 16 网格取色、不量化、不动 alpha。

    这张图标就是要「源图本身的样子」，所以解出来就是产物；写盘那一步由 RAW_COPY_SOURCES
    走原字节拷贝（Pillow 的 save() 必然重编码，字节会变）。源图缺失时返回 None，由 compose() 退回候选链。
    """
    path = wild_apple_source()
    if not path.is_file():
        print(f"[警告] 狂野苹果源图不存在：{path}（退回原版 item/apple）", file=sys.stderr)
        return None
    with Image.open(path) as raw:
        return raw.convert("RGBA")


ITEM_DRAWERS = {
    "vibration_bean": draw_vibration_bean,
}

# F-162：四张阶段贴图按 CHIVE_STAGE_ART 画，不再落 make_placeholder()（23 号 §3.5）。
STAGE_DRAWERS = {
    f"friend_chives_stage{index}": partial(draw_chive_stage, index)
    for index in range(len(CHIVE_STAGE_ART))
}

# F-164：压缩麦顶 / 底两张切面。
BLOCK_DRAWERS = {
    "compressed_hay_block_top": draw_hay_top,
    "compressed_hay_block_bottom": draw_hay_bottom,
}

# F-163：要读作者源图的物品图标；源图缺失时 compose() 退回候选链。
ICON_DRAWERS = {
    "wild_apple": draw_wild_apple,
}

DRAWERS = {**ITEM_DRAWERS, **STAGE_DRAWERS, **BLOCK_DRAWERS, **ICON_DRAWERS}

# F-179 · D-126：这一类目标的验收口径是「与源图逐字节相同」，而 Pillow 的 save() 必然重编码
# （同一张 64x64 RGBA 会被写成 7,172 B），所以写盘时走原字节拷贝；值是「取源图路径」的函数。
RAW_COPY_SOURCES = {
    "wild_apple": wild_apple_source,
}

# 0.0.3 追加的 4 张朋友的韭阶段贴图：v0.1.0 起由 STAGE_DRAWERS 按阶段绘制（F-162）；
# PLACEHOLDER_COLOR / make_placeholder() 仍留给其它「源材质缺失」的项（23 号 §3.5）。
ADDITION_PLACEHOLDER_STAGES = 4


def build_additions_specs() -> tuple[Spec, ...]:
    """0.0.3 追加里有「源可转」之外的 5 张（1 张程序绘制物品图标 + 4 张洋红占位阶段贴图）。

    另外 3 张都在 tools/convert_media.ps1：mob_effect/q_bounce.png（preview.jpg）与
    item/friend_chives.png、item/chive_seeds.png（作者 1254x1254 原画）——凡是有源图可转的
    都走那条管线，本脚本只负责没有源可转的那几张。
    """
    specs: list[Spec] = []
    for item_id, zh_name in (
        ("vibration_bean", "震动豆"),
    ):
        specs.append(
            Spec(
                rel_path=f"textures/item/{item_id}.png",
                kind=KIND_DRAWN,
                zh_name=f"{zh_name}（程序绘制）",
                drawer=item_id,
            )
        )
    for index in range(ADDITION_PLACEHOLDER_STAGES):
        specs.append(
            Spec(
                rel_path=f"textures/block/friend_chives_stage{index}.png",
                kind=KIND_BLOCK,
                zh_name=f"朋友的韭 阶段 {index}（按 23 号 §3.4 绘制）",
                drawer=f"friend_chives_stage{index}",
            )
        )
    return tuple(specs)


def compose(spec: Spec, resolved: Resolved) -> Image.Image:
    """按目标类型合成最终图（阶段缩放 / 方块原样 / GUI 程序绘制 / 专用画法）。"""
    if spec.kind == KIND_GUI:
        return draw_gui_background()

    base = DRAWERS[spec.drawer]() if spec.drawer is not None else resolved.image
    if base is None:
        base = make_placeholder()
    if spec.tint is not None:
        base = apply_tint(base, spec.tint)
    if spec.monochrome:
        # TECH §6.5 指定写法：L 通道取亮度再回 RGBA ⇒ R=G=B（色相 0），尺寸与 alpha 不变。
        base = base.convert("RGBA").convert("L").convert("RGBA")
    if spec.hay_tone:
        base = apply_hay_tone(base)
    if spec.kind == KIND_STAGE:
        return fit_bottom_center(base, spec.fit)
    canvas = base.convert("RGBA")
    size = expected_size(spec)
    if canvas.size != size:
        canvas = canvas.resize(size, Image.Resampling.NEAREST)
    return canvas


def expected_size(spec: Spec) -> tuple[int, int]:
    """该目标的期望尺寸（v0.1.1 · A-32：断言保留，只放开期望值）。

    GUI 是 176x166；走原字节直通的那张取源图实际尺寸（源图缺失则退回 16x16，与候选链产物一致）；
    其余一律 16x16。
    """
    if spec.kind == KIND_GUI:
        return GUI_SIZE
    if spec.drawer in RAW_COPY_SOURCES:
        size = wild_apple_source_size()
        if size is not None:
            return size
    return (TEXTURE_SIZE, TEXTURE_SIZE)


def output_path(out_dir: Path, spec: Spec) -> Path:
    """生成材质的完整路径：<out>/assets/farmstorm/<rel_path>。"""
    return out_dir.joinpath(ASSETS_DIR, MOD_ID, *spec.rel_path.split("/"))


def print_listing(specs: tuple[Spec, ...]) -> None:
    """打印内置目标表（路径 / 类型 / 中文名 / 候选链）。"""
    counts: dict[str, int] = {}
    for spec in specs:
        counts[spec.kind] = counts.get(spec.kind, 0) + 1
    print(
        f"{MOD_ID} 材质目标表：共 {len(specs)} 张"
        f"（item {counts.get(KIND_ICON, 0)} / stage {counts.get(KIND_STAGE, 0)}"
        f" / block {counts.get(KIND_BLOCK, 0)} / gui {counts.get(KIND_GUI, 0)}）"
    )
    print(f"输出路径：<out>/{ASSETS_DIR}/{MOD_ID}/textures/{{item,block,gui}}/…")
    print()
    print(f"{'kind':<6} {'rel_path':<46} {'candidates':<40} 中文名")
    print("-" * 118)
    for spec in specs:
        chain = " → ".join(ref.label() for ref in spec.candidates) or "（程序绘制）"
        if spec.tint is not None:
            chain = f"{chain} [tint]"
        if spec.monochrome:
            chain = f"{chain} [gray]"
        if spec.hay_tone:
            chain = f"{chain} [干草黄褐]"
        if spec.drawer is not None:
            chain = f"{chain} [专用画法]"
        print(f"{spec.kind:<6} {spec.rel_path:<46} {chain:<40} {spec.zh_name}")


def build_parser() -> argparse.ArgumentParser:
    """构造命令行解析器。"""
    parser = argparse.ArgumentParser(
        prog="gen_textures.py",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=(
            "生成「农场风云 / Farmstorm」的 99 张材质（TECH §6.5 的 87 张 + FIX-5 的 4 张转换器作物侧面\n"
            "+ V-23 的 4 张物品图标与 2 张方块 + v0.1.0 · F-164 的压缩麦顶 / 底 2 张）：\n"
            "26 张物品图标 + 56 张作物阶段 + 16 张方块（含转换器顶面与四个作物侧面、压缩麦顶 / 底切面）\n"
            "+ 1 张 176x166 GUI。\n"
            "原版贴图缺失时按候选链回退，最终回退为纯色占位色块，不中断整批生成。"
        ),
        epilog=(
            "示例：\n"
            "  python -X utf8 tools/gen_textures.py --list\n"
            "  python -X utf8 tools/gen_textures.py --vanilla-assets D:\\minecraft\\.minecraft\\versions\\<ver>\\<ver>.jar"
            " --out src\\main\\resources --dry-run\n"
            "  python -X utf8 tools/gen_textures.py --vanilla-assets <jar 或资源根> --out src\\main\\resources\n"
            "\n"
            "源材质：<jar 内的 assets/minecraft/textures/{item,block}/<名字>.png>，\n"
            "        或 <资源根>/minecraft/textures/{item,block}/<名字>.png（也认 assets/minecraft/textures/…）。\n"
            "输出：  <out>/assets/farmstorm/textures/item/<id>.png 等（<out> 是资源根）。\n"
            "依赖：Python 3.9+ 与 Pillow 9.1+。"
        ),
    )
    parser.add_argument(
        "--vanilla-assets",
        type=Path,
        metavar="PATH",
        help="原版资源来源：客户端 jar，或解包后的资源根目录（其下应有 minecraft/textures/）",
    )
    parser.add_argument(
        "--out",
        type=Path,
        metavar="DIR",
        help="输出资源根目录；脚本写入 <out>/assets/farmstorm/textures/{item,block,gui}/…",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="只解析并打印将写入的路径与源材质状态，不创建目录、不写文件",
    )
    parser.add_argument(
        "--additions",
        action="store_true",
        help="只生成 0.0.3 追加里无源可转的 5 张（1 张程序绘制物品图标 + 4 张朋友的韭阶段贴图），不读原版贴图；只需 --out",
    )
    parser.add_argument(
        "--list",
        dest="list_items",
        action="store_true",
        help="打印内置目标表（99 条：26 item + 56 stage + 16 block + 1 gui）后退出",
    )
    return parser


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    """解析命令行参数；非 --list 模式要求给出 --vanilla-assets 与 --out。"""
    parser = build_parser()
    args = parser.parse_args(argv)
    if args.list_items:
        return args

    required = [("--out", args.out)]
    if not args.additions:
        required.insert(0, ("--vanilla-assets", args.vanilla_assets))
    missing = [flag for flag, value in required if value is None]
    if missing:
        parser.error("缺少必需参数：{}（或改用 --list 只看目标表）".format("、".join(missing)))
    return args


def print_buckets(report: Report, specs: tuple[Spec, ...]) -> None:
    """按「原版命中 / 退回占位 / 找不到」三类打印清单（GUI 背景与专用画法单列）。"""
    for status in (STATUS_VANILLA, STATUS_SUBSTITUTE, STATUS_MISSING):
        entries = report.buckets[status]
        print(f"\n{status}：{len(entries)} 张")
        for rel_path, used, primary in entries:
            if status == STATUS_VANILLA:
                print(f"  {rel_path:<48} <- {used}")
            else:
                print(f"  {rel_path:<48} <- {used}（主源 {primary} 缺失）")
    print(f"\n程序绘制 / 源图重采样：{report.procedural} 张")
    for spec in specs:
        if spec.kind == KIND_GUI or spec.drawer is not None:
            print(f"  {spec.rel_path:<48} {spec.zh_name}")


def print_additions_summary(report: Report) -> None:
    """--additions 的小结：程序绘制几张 + 占位几张（不涉及原版贴图，故不分三类）。"""
    print(f"\n程序绘制贴图：{report.procedural} 张")
    placeholders = report.buckets[STATUS_MISSING]
    print(f"纯色占位贴图：{len(placeholders)} 张")
    for rel_path, _used, _primary in placeholders:
        print(f"  {rel_path:<48} 纯色占位")


def main(argv: list[str] | None = None) -> int:
    """命令行入口。"""
    args = parse_args(argv)
    additions = args.additions
    specs = build_additions_specs() if additions else build_specs()

    if args.list_items:
        print_listing(specs)
        return 0

    warnings: list[str] = []
    source = NullSource() if additions else AssetSource(args.vanilla_assets, warnings)
    report = Report()

    try:
        for spec in specs:
            resolved = source.resolve(spec)
            report.record(spec, resolved)
            target = output_path(args.out, spec)

            if args.dry_run:
                if spec.drawer is not None:
                    action = "程序绘制"
                else:
                    action = "纯色占位色块" if resolved.image is None else f"取自 {resolved.used}"
                if spec.tint is not None:
                    action += " + 染色"
                if spec.monochrome:
                    action += " + 灰度化"
                size = expected_size(spec)
                print(f"[dry-run] {spec.rel_path:<46} => {target}（{action}，期望 {size[0]}x{size[1]}）")
                report.planned += 1
                continue

            image = compose(spec, resolved)
            size = expected_size(spec)
            if image.size != size:
                raise SystemExit(f"尺寸断言失败：{target} 生成 {image.size}，要求 {size}")
            target.parent.mkdir(parents=True, exist_ok=True)
            try:
                source_path = RAW_COPY_SOURCES.get(spec.drawer)
                if source_path is not None:
                    shutil.copyfile(source_path(), target)
                else:
                    image.save(target)
            except OSError as exc:
                raise SystemExit(f"写入失败：{target}：{exc}") from exc
            report.written += 1
            print(f"[完成] {spec.rel_path:<46} {image.size[0]}x{image.size[1]}")
    finally:
        source.close()

    for warning in warnings:
        print(f"[警告] {warning}", file=sys.stderr)

    if additions:
        print_additions_summary(report)
    else:
        print_buckets(report, specs)
    if args.dry_run:
        print(
            f"\n[dry-run] 共 {len(specs)} 条：将写入 {report.planned} 个，"
            f"源 {source.describe()}（未写任何文件）"
        )
    else:
        print(f"\n完成：共 {len(specs)} 条，写入 {report.written} 个（源 {source.describe()}）")
    return 0


if __name__ == "__main__":
    sys.exit(main())

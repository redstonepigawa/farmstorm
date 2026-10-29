#!/usr/bin/env python3
r"""「农场风云 / Farmstorm」材质生成器（F-51 / TECH §6.5 的 87 张 + FIX-5 的 4 张转换器作物侧面 = 91 张，材料全部程序生成）。

产出的 91 张材质（DEC D-25 的占位稿，作者审校后可替换）：
  1. textures/item/<22 个 id>.png            —— 15 个作物 + 6 个播种物 + 转换器，16x16；
     15 个作物与 6 个播种物图标右下角叠绿色「+」（plan0.0.1.txt 第 8 条，区分原版与改良）；转换器图标不加；
  2. textures/block/<base>_stage{0..3}.png   —— 14 个作物 × 4 阶段 = 56 张，16x16；
     <base> 取法见 TECH §6.3：作物方块 ID 去掉末尾的 _crop / _stem / _stalk / _block / _bush；
  3. textures/block/<id>.png                 —— 5 个部署方块 + converter_{top,side,front}
     + converter_side_{wheat,carrot,potato,beetroot}（FIX-5：工作台侧面叠作物图标）= 12 张；
  4. textures/gui/converter.png              —— 176x166 容器背景（面板 + 标题/背包刻线 + 输入→预览箭头 + 槽位），
     每个槽位方框的「内容区左上角」与 ConverterMenu 的 Slot 坐标逐像素相等。

用法::

    python -X utf8 tools/gen_textures.py --list
    python -X utf8 tools/gen_textures.py --vanilla-assets <jar 或解包资源根> --out src/main/resources --dry-run
    python -X utf8 tools/gen_textures.py --vanilla-assets <jar 或解包资源根> --out src/main/resources

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
import sys
import zipfile
from dataclasses import dataclass, field
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw

TEXTURE_SIZE = 16
MOD_ID = "farmstorm"
ASSETS_DIR = "assets"

PLUS_SIZE = 7
PLUS_ARM = 3
PLUS_MARGIN = 0
PLUS_COLOR = (0x3F, 0xA3, 0x4D, 0xFF)

PLACEHOLDER_COLOR = (0xD8, 0x1E, 0x8A, 0xFF)

# 阶段贴图按阶段从小到大等比缩小（NEAREST，底边居中），
# 让原版只有单一贴图的作物（西瓜/南瓜茎、仙人掌）四个阶段也能看出生长差异。
STAGE_SCALES = (10, 12, 14, 16)

LAYER_BLOCK = "block"

KIND_ICON = "icon"
KIND_STAGE = "stage"
KIND_BLOCK = "block"
KIND_GUI = "gui"

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
    add_plus: bool = False
    fit: int = TEXTURE_SIZE
    tint: tuple[int, int, int] | None = None
    overlay: SourceRef | None = None


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
        # GUI 背景是程序绘制的，不参与「原版命中 / 退回 / 找不到」的三类统计。
        if spec.kind == KIND_GUI:
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
    ("molotov", "莫洛托夫！", (("item", "splash_potion"),)),
    ("zangief_cane", "桑吉尔蔗", (("item", "sugar_cane"),)),
    ("wild_apple", "狂野苹果", (("item", "apple"),)),
    ("compressed_puffed_wheat", "压缩膨胀麦", (("block", "hay_block_side"),)),
)

# 转换器不是改良作物，图标不加「+」（plan0.0.1.txt 第 8 条只要求改良作物）。
NO_PLUS_ICONS = frozenset({"converter"})

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
BLOCK_TEXTURES: tuple[tuple[str, str, tuple[tuple[str, str], ...]], ...] = (
    ("blast_carrot", "部署·胡罗贝", (("block", "carrots_stage3"),)),
    ("pumpkin_turret", "部署·man瓜", (("block", "carved_pumpkin"), ("block", "pumpkin_side"))),
    ("awakened_torchflower", "部署·激活的火把花", (("block", "torchflower"),)),
    ("awakened_pitcher_plant", "部署·激活的瓶子草",
     (("item", "pitcher_plant"), ("block", "pitcher_crop_top"))),
    ("fang_trap_cactus", "部署·赢过输过没死过", (("block", "cactus_side"),)),
    # V-23：F-65 注册了这两个方块却没建外观，导致 runData --all 从 F-65 起就抛「贴图不存在」。
    # 陷阱类部署方块在世界里不渲染几何，这张 PNG 只作破坏粒子；压缩干草块则是 cubeAll 的正式贴图。
    ("zangief_cane", "部署·桑吉尔蔗（仅作破坏粒子）", (("block", "sugar_cane"),)),
    ("compressed_hay_block", "压缩干草块", (("block", "hay_block_side"),)),
    # 顶面兼底面：原版 crafting_table 模型的 up / down 都用 crafting_table_top，这里照抄同一个口径。
    ("converter_top", "转换器·顶面（兼底面）", (("block", "crafting_table_top"),)),
    ("converter_side", "转换器·净工作台侧面（模型已不引用）", (("block", "crafting_table_side"),)),
    ("converter_front", "转换器·正面（模型已不引用）", (("block", "furnace_front"),)),
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
                add_plus=item_id not in NO_PLUS_ICONS,
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

    for name, zh_name, chain in BLOCK_TEXTURES:
        specs.append(
            Spec(
                rel_path=f"textures/block/{name}.png",
                kind=KIND_BLOCK,
                zh_name=zh_name,
                candidates=refs(*chain),
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


def build_marker(size: int = PLUS_SIZE, arm: int = PLUS_ARM) -> Image.Image:
    """渲染 size x size 的绿色「+」标记：居中的实心十字，臂宽 arm。

    不描 1px 描边——7x7 十字若做完整八邻域描边会被撑成实心方块，物品栏里读不出「+」；
    也不缩到 1px 臂——那样在 16x16 图标里太淡。7x7 + 3px 臂是能一眼看出「+」的最简解。
    """
    if size < arm + 2 or arm < 1:
        raise ValueError(f"十字标记参数非法：size={size} arm={arm}")
    start = (size - arm) // 2
    marker = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(size):
        for x in range(size):
            if start <= x < start + arm or start <= y < start + arm:
                marker.putpixel((x, y), PLUS_COLOR)
    return marker


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


def fit_bottom_center(base: Image.Image, extent: int) -> Image.Image:
    """把底图等比缩放成 extent x extent 并底边居中贴进 16x16 透明画布。"""
    canvas = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    scaled = base.convert("RGBA")
    if scaled.size != (extent, extent):
        scaled = scaled.resize((extent, extent), Image.Resampling.NEAREST)
    canvas.alpha_composite(scaled, ((TEXTURE_SIZE - extent) // 2, TEXTURE_SIZE - extent))
    return canvas


def compose_icon(base: Image.Image, marker: Image.Image) -> Image.Image:
    """把标记叠到底图右下角，返回 16x16 RGBA 物品图标。"""
    left = TEXTURE_SIZE - marker.width - PLUS_MARGIN
    top = TEXTURE_SIZE - marker.height - PLUS_MARGIN
    if left < 0 or top < 0:
        raise ValueError(f"标记 {marker.size} 无法放进 {TEXTURE_SIZE}x{TEXTURE_SIZE} 画布")

    canvas = base.convert("RGBA")
    if canvas.size != (TEXTURE_SIZE, TEXTURE_SIZE):
        canvas = canvas.resize((TEXTURE_SIZE, TEXTURE_SIZE), Image.Resampling.NEAREST)
    canvas.alpha_composite(marker, (left, top))
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


def compose(spec: Spec, resolved: Resolved, marker: Image.Image) -> Image.Image:
    """按目标类型合成最终图（图标叠「+」/ 阶段缩放 / 方块原样 / GUI 程序绘制）。"""
    if spec.kind == KIND_GUI:
        return draw_gui_background()

    base = resolved.image if resolved.image is not None else make_placeholder()
    if spec.tint is not None:
        base = apply_tint(base, spec.tint)
    if spec.kind == KIND_STAGE:
        return fit_bottom_center(base, spec.fit)
    if spec.add_plus:
        return compose_icon(base, marker)
    canvas = base.convert("RGBA")
    if canvas.size != (TEXTURE_SIZE, TEXTURE_SIZE):
        canvas = canvas.resize((TEXTURE_SIZE, TEXTURE_SIZE), Image.Resampling.NEAREST)
    return canvas


def expected_size(spec: Spec) -> tuple[int, int]:
    return GUI_SIZE if spec.kind == KIND_GUI else (TEXTURE_SIZE, TEXTURE_SIZE)


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
        print(f"{spec.kind:<6} {spec.rel_path:<46} {chain:<40} {spec.zh_name}")


def build_parser() -> argparse.ArgumentParser:
    """构造命令行解析器。"""
    parser = argparse.ArgumentParser(
        prog="gen_textures.py",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=(
            "生成「农场风云 / Farmstorm」的 91 张占位材质（TECH §6.5 的 87 张 + FIX-5 的 4 张转换器作物侧面）：\n"
            "22 张物品图标 + 56 张作物阶段 + 12 张方块（含转换器顶面与四个作物侧面）+ 1 张 176x166 GUI。\n"
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
        "--list",
        dest="list_items",
        action="store_true",
        help="打印内置目标表（97 条：26 item + 56 stage + 14 block + 1 gui）后退出",
    )
    return parser


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    """解析命令行参数；非 --list 模式要求给出 --vanilla-assets 与 --out。"""
    parser = build_parser()
    args = parser.parse_args(argv)
    if args.list_items:
        return args

    missing = [
        flag
        for flag, value in (
            ("--vanilla-assets", args.vanilla_assets),
            ("--out", args.out),
        )
        if value is None
    ]
    if missing:
        parser.error("缺少必需参数：{}（或改用 --list 只看目标表）".format("、".join(missing)))
    return args


def print_buckets(report: Report) -> None:
    """按「原版命中 / 退回占位 / 找不到」三类打印清单（GUI 背景单列，程序绘制）。"""
    for status in (STATUS_VANILLA, STATUS_SUBSTITUTE, STATUS_MISSING):
        entries = report.buckets[status]
        print(f"\n{status}：{len(entries)} 张")
        for rel_path, used, primary in entries:
            if status == STATUS_VANILLA:
                print(f"  {rel_path:<48} <- {used}")
            else:
                print(f"  {rel_path:<48} <- {used}（主源 {primary} 缺失）")
    print("\n程序绘制（无原版来源）：%d 张 —— textures/gui/converter.png" % report.procedural)


def main(argv: list[str] | None = None) -> int:
    """命令行入口。"""
    args = parse_args(argv)
    specs = build_specs()

    if args.list_items:
        print_listing(specs)
        return 0

    warnings: list[str] = []
    source = AssetSource(args.vanilla_assets, warnings)
    marker = build_marker()
    report = Report()

    try:
        for spec in specs:
            resolved = source.resolve(spec)
            report.record(spec, resolved)
            target = output_path(args.out, spec)

            if args.dry_run:
                action = "纯色占位色块" if resolved.image is None else f"取自 {resolved.used}"
                if spec.add_plus:
                    action += " + 绿色「+」"
                if spec.tint is not None:
                    action += " + 染色"
                print(f"[dry-run] {spec.rel_path:<46} => {target}（{action}）")
                report.planned += 1
                continue

            image = compose(spec, resolved, marker)
            size = expected_size(spec)
            if image.size != size:
                raise SystemExit(f"尺寸断言失败：{target} 生成 {image.size}，要求 {size}")
            target.parent.mkdir(parents=True, exist_ok=True)
            try:
                image.save(target)
            except OSError as exc:
                raise SystemExit(f"写入失败：{target}：{exc}") from exc
            report.written += 1
            print(f"[完成] {spec.rel_path:<46} {image.size[0]}x{image.size[1]}")
    finally:
        source.close()

    for warning in warnings:
        print(f"[警告] {warning}", file=sys.stderr)

    print_buckets(report)
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

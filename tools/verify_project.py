#!/usr/bin/env python3
r"""Farmstorm v0.1.1 静态验收器。

期望值全部从上游文档现算，不手抄：
  - config 键集  <- docs/zh/01-DESIGN.md §3.8（117）+ docs/zh/12-TECH-0.0.2.md §8.1（22）
                   + docs/zh/18-TECH-0.0.3.md §8.1（v0.0.3 的 19 行，F-104）
                   + docs/zh/24-TECH-0.1.0.md §8.1（v0.1.0 的 12 行，F-142）
                   + docs/zh/30-TECH-0.1.1.md §4.1（v0.1.1 的 58 组 / 166 键，F-177）
                   + docs/zh/36-TECH-0.2.0.md §4.1.1（v0.2.0 的 11 行：增 10 / 删 1，F-243），并与
                   run/config/farmstorm-server.toml 逐键比对。18 / 24 / 30 号各含「改既有默认值」的行
                   （v0.0.3 是 fang_trap_cactus 的三个键、v0.1.0 是 melon_grenade.horizontalJitter、
                   v0.1.1 是 pumpkin_turret 的三行），键名与前面的清单重名，按集合去重后是
                   167 个键；再加 36 号 §4.1.1 的 10 个增键、扣掉两个删键（`REMOVED_KEYS` 的
                   farmland.limitPerPlayer 与 §4.1.1 第 1 行的 outpost_core.farmlandBonus）= 175 个键
  - 97 个注册 ID  <- docs/zh/36-TECH-0.2.0.md §1.2 的 v0.2.0 列逐类小计（12 类，相加 97；
                   基线 93 见 24-TECH-0.1.0.md §2.1，本版 +4）

用法：python -X utf8 tools/verify_project.py [--root <仓库根>]
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import datetime
from pathlib import Path

FAIL: list[str] = []
WARN: list[str] = []


def ok(m: str) -> None:
    print(f"  [OK]   {m}")


def fail(m: str) -> None:
    FAIL.append(m)
    print(f"  [FAIL] {m}")


def warn(m: str) -> None:
    WARN.append(m)
    print(f"  [WARN] {m}")


def stale(m: str) -> None:
    FAIL.append(m)
    print(f"  [STALE] {m}")


def mtime(p: Path) -> str:
    return datetime.fromtimestamp(p.stat().st_mtime).strftime("%Y-%m-%d %H:%M:%S")


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8")


def expected_config_keys(design: str) -> list[str]:
    start = design.index("#### 3.8.1")
    end = design.index("#### 3.8.6")
    section = design[start:end]
    keys = [m.group(0)[len("farmstorm."):]
            for m in re.finditer(r"farmstorm\.[a-z][a-zA-Z0-9_]*(?:\.[a-zA-Z0-9_]+)*", section)]
    seen = set(keys)
    for crop, effect in re.findall(
            r"^\|\s*\`([a-z_]+)\`\s*\|\s*\`([a-z_]+)\`[^|]*\|\s*\d+\s*\|\s*\d+\s*\|",
            section, re.MULTILINE):
        for suffix in ("level", "durationTicks"):
            k = f"{crop}.{effect}.{suffix}"
            if k not in seen:
                seen.add(k)
                keys.append(k)
    return keys


def expected_config_keys_v002(tech: str) -> list[str]:
    start = tech.index("### 8.1")
    end = tech.index("### 8.2")
    section = tech[start:end]
    return [m.group(1) for m in re.finditer(
        r"^\|\s*\d+\s*\|\s*`([A-Za-z0-9_.]+)`\s*\|", section, re.MULTILINE)]


def expected_config_keys_v003(tech: str) -> list[str]:
    start = tech.index("### 8.1")
    end = tech.index("### 8.2")
    section = tech[start:end]
    return [m.group(1) for m in re.finditer(
        r"^\|\s*\d+\s*\|\s*`([A-Za-z0-9_.]+)`\s*\|", section, re.MULTILINE)]


def expected_config_keys_v010(tech4: Path) -> tuple[list[str], list[str]]:
    """24-TECH §8.1 的 12 行：返回（「新」列的 11 个键名，「改」列的 1 个既有键名）。

    解析失败一律带（文件名 + 表格行号）抛出：静默跳过会让期望集悄悄退回 155，比报错更糟。
    """
    text = read(tech4)
    head = re.search(r"^### 8\.1\b.*$", text, re.MULTILINE)
    if not head:
        seen = [f"第 {text[: m.start()].count(chr(10)) + 1} 行 {m.group(0)[:24]}"
                for m in re.finditer(r"^### 8\.[^\n]*", text, re.MULTILINE)]
        raise ValueError(f"{tech4.name}：找不到「### 8.1」的表格标题"
                         f"（全文 {len(text.splitlines())} 行；现有的 8.x 标题：{seen or '无'}，表格格式变了？）")
    base_line = text[:head.start()].count("\n") + 1
    tail = re.search(r"^### 8\.2\b", text[head.start():], re.MULTILINE)
    if not tail:
        raise ValueError(f"{tech4.name}:{base_line}：§8.1 之后找不到「### 8.2」，无法界定表格范围")
    fresh: list[tuple[int, str]] = []
    changed: list[tuple[int, str]] = []
    for offset, line in enumerate(text[head.start():head.start() + tail.start()].splitlines()):
        row = re.match(r"^\|\s*(\d+)\s*\|\s*`([A-Za-z0-9_.]+)`\s*\|", line)
        if not row:
            continue
        lineno = base_line + offset
        cols = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cols) != 8:
            raise ValueError(f"{tech4.name}:{lineno}：§8.1 的表行应 8 列，实际 {len(cols)} 列：{line.strip()}")
        flag = cols[6].strip("*").strip()
        if "新" in flag:
            fresh.append((lineno, row.group(2)))
        elif "改" in flag:
            changed.append((lineno, row.group(2)))
        else:
            raise ValueError(f"{tech4.name}:{lineno}：§8.1 第 7 列既不是「新」也不是「改」：{cols[6]!r}")
    rows = fresh + changed
    if len(rows) != 12:
        raise ValueError(f"{tech4.name}:{base_line}：§8.1 从第 {base_line} 行起只解析出 {len(rows)} 行"
                         f"（新 {len(fresh)} + 改 {len(changed)}，行号 {[n for n, _ in rows]}），文档口径是 12 行")
    if len(fresh) != 11:
        raise ValueError(f"{tech4.name}:{base_line}：§8.1 的「新」列应 11 行、实际 {len(fresh)} 行"
                         f"（行号 {[n for n, _ in fresh]}）：{[k for _, k in fresh]}")
    return [k for _, k in fresh], [k for _, k in changed]


def expected_config_keys_v012(tech6: Path) -> tuple[list[str], list[str]]:
    """36-TECH §4.1.1 的 11 行 →（增键列表，删键列表）。

    表列是「序号 | 完整键路径 | 类型 | 默认 | 值域 | 动作 | 决策号」：键取第 2 列反引号内的
    `<组>.<键>`，动作取第 6 列的「增」/「删」。增删**从文档读**，不在本文件里抄第二份清单
    （抄一份答案会让「读文档现算」退化成「对答案」，A-66 的原始缺陷原地复活）。

    解析失败一律带（文件名 + 表行号）抛出：静默跳过会让期望集悄悄停在 166，比报错更糟。
    """
    text = read(tech6)
    head = re.search(r"^#### 4\.1\.1\b.*$", text, re.MULTILINE)
    if not head:
        raise ValueError(f"{tech6.name}：找不到「#### 4.1.1」的表格标题（表格格式变了？）")
    base_line = text[:head.start()].count("\n") + 1
    tail = re.search(r"^#### 4\.1\.2\b", text[head.start():], re.MULTILINE)
    if not tail:
        raise ValueError(f"{tech6.name}:{base_line}：§4.1.1 之后找不到「#### 4.1.2」，无法界定表格范围")
    added: list[tuple[int, str]] = []
    removed: list[tuple[int, str]] = []
    for offset, line in enumerate(text[head.start():head.start() + tail.start()].splitlines()):
        row = re.match(r"^\|\s*\d+\s*\|\s*`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`\s*\|", line)
        if not row:
            continue
        lineno = base_line + offset
        cols = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cols) != 7:
            raise ValueError(f"{tech6.name}:{lineno}：§4.1.1 的表行应 7 列"
                             f"（序号/键路径/类型/默认/值域/动作/决策号），实际 {len(cols)} 列：{line.strip()[:70]}")
        act = cols[5].strip("*").strip()
        if act.startswith("增"):
            added.append((lineno, row.group(1)))
        elif act.startswith("删"):
            removed.append((lineno, row.group(1)))
        else:
            raise ValueError(f"{tech6.name}:{lineno}：§4.1.1 第 6 列既不是「增」也不是「删」：{cols[5]!r}")
    if len(added) + len(removed) != 11:
        raise ValueError(f"{tech6.name}:{base_line}：§4.1.1 从第 {base_line} 行起只解析出"
                         f"{len(added) + len(removed)} 行（增 {len(added)} / 删 {len(removed)}，"
                         f"行号 {[n for n, _ in added + removed]}），文档口径是 11 行")
    if len(added) != 10 or len(removed) != 1:
        raise ValueError(f"{tech6.name}:{base_line}：§4.1.1 应是「增 10 / 删 1」，实际"
                         f"「增 {len(added)} / 删 {len(removed)}」")
    return [k for _, k in added], [k for _, k in removed]


# 本版从期望集里剔除的键（30 号 §2.3 的删键登记 / D-139：整组删 farmland）。
# 只许在 main() 的并集表达式旁边扣一次——散成两处，下一个删键的人就会漏扣一处。
REMOVED_KEYS = {"farmland.limitPerPlayer"}


def expected_config_keys_v011(tech5: Path) -> tuple[list[str], int]:
    """30-TECH §4.1 的 58 行数据表 →（`<组>.<键>` 列表，组数）。

    表列是「序号 | 组 | 键数 | 键（逐键点名）| 标记」：组取第 2 列（嵌套组形如
    `blast_carrot.nausea`），键取第 4 列反引号内的全部键名，拼成 `<组>.<键>`。行内的
    「（**改** 1 → 3）」这类注解写在反引号外，由反引号解析天然跳过，不需要额外分支。
    §4.1 里另有两张表（被删的那一组、新增键的三个属性）没有「序号」列，因此不构成数据行。

    解析失败一律带（文件名 + 表行号）抛出：静默跳过会让期望集悄悄缩小，比报错更糟。
    """
    text = read(tech5)
    head = re.search(r"^### 4\.1\b.*$", text, re.MULTILINE)
    if not head:
        seen = [f"第 {text[: m.start()].count(chr(10)) + 1} 行 {m.group(0)[:24]}"
                for m in re.finditer(r"^### 4\.[^\n]*", text, re.MULTILINE)]
        raise ValueError(f"{tech5.name}：找不到「### 4.1」的表格标题"
                         f"（全文 {len(text.splitlines())} 行；现有的 4.x 标题：{seen or '无'}，表格格式变了？）")
    base_line = text[:head.start()].count("\n") + 1
    tail = re.search(r"^### 4\.2\b", text[head.start():], re.MULTILINE)
    if not tail:
        raise ValueError(f"{tech5.name}:{base_line}：§4.1 之后找不到「### 4.2」，无法界定表格范围")
    rows: list[tuple[int, str, list[str]]] = []
    for offset, line in enumerate(text[head.start():head.start() + tail.start()].splitlines()):
        if not re.match(r"^\|\s*\d+\s*\|", line):
            continue
        lineno = base_line + offset
        cols = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cols) != 5:
            raise ValueError(f"{tech5.name}:{lineno}：§4.1 的数据行应 5 列（序号/组/键数/键/标记），"
                             f"实际 {len(cols)} 列：{line.strip()[:70]}")
        group = re.fullmatch(r"`([a-z][a-z0-9_]*(?:\.[a-z0-9_]+)*)`", cols[1])
        if not group:
            raise ValueError(f"{tech5.name}:{lineno}：§4.1 的「组」列应是单个反引号内的小写路径"
                             f"（形如 `blast_carrot.nausea`），实际是 {cols[1]!r}")
        keys = re.findall(r"`([A-Za-z0-9_]+)`", cols[3])
        declared = cols[2].strip("*").strip()
        if not declared.isdigit() or int(declared) != len(keys):
            raise ValueError(f"{tech5.name}:{lineno}：§4.1「{group.group(1)}」的键数列是 {cols[2]!r}，"
                             f"但「键」列只解析出 {len(keys)} 个反引号键名：{keys}")
        rows.append((lineno, group.group(1), keys))
    pairs = [f"{g}.{k}" for _, g, ks in rows for k in ks]
    if len(rows) != 58:
        raise ValueError(f"{tech5.name}:{base_line}：§4.1 从第 {base_line} 行起只解析出 {len(rows)} 个组"
                         f"（前几个组行号 {[n for n, _, _ in rows][:6]}），文档口径是 58 组")
    if len(pairs) != 166:
        raise ValueError(f"{tech5.name}:{base_line}：§4.1 从第 {base_line} 行起的 {len(rows)} 组合计"
                         f"{len(pairs)} 个键，文档口径是 166 键")
    dup = sorted({k for k in pairs if pairs.count(k) > 1})
    if dup:
        where = [f"第 {n} 行（{g}）" for n, g, ks in rows if any(f"{g}.{k}" in dup for k in ks)]
        raise ValueError(f"{tech5.name}:{base_line}：§4.1 解析出 {len(dup)} 个重复键名 {dup[:5]}"
                         f"（相关行 {where[:5]}），组 + 键两列拼出的键集必须两两不同")
    return pairs, len(rows)


def toml_keys(text: str) -> set[str]:
    out: set[str] = set()
    section = ""
    for raw in text.splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("[") and line.endswith("]"):
            section = line[1:-1].strip()
            continue
        m = re.match(r"^([A-Za-z0-9_.-]+)\s*=", line)
        if m:
            out.add(f"{section}.{m.group(1)}" if section else m.group(1))
    return out


def doc_key_variants(k: str) -> list[str]:
    """文档的「完整键路径」→ 落盘可能出现的拼法（第一个永远原样）。

    落盘键名由 `b.push(组)` + `defineInRange(键)` 两段决定，所以文档写
    `trade.common.min` 的键，落盘是 `[trade]` 段的 `commonMin`。这里只做
    **组名之后的点去掉**（`a.b.c` → `a.bC`，首段原样、其余段首字母大写）这一条通用形状归一，组名不动、
    不引入任何逐键白名单——白名单会让工具从「读文档现算」退化回「对答案」。
    """
    parts = k.split(".")
    if len(parts) < 3:
        return [k]
    return [k, parts[0] + "." + parts[1] + "".join(p[:1].upper() + p[1:] for p in parts[2:])]


CROP_IDS = [
    "puffed_wheat", "blast_carrot", "ubi_server", "toxic_ubi_server", "power_beet",
    "melon_slice_bomb", "melon_grenade", "pumpkin_turret", "awakened_torchflower",
    "awakened_pitcher_plant", "fang_trap_cactus", "bamboo_firecracker",
    "red_mushroom_puff", "brown_mushroom_puff", "lucky_berry",
    # v0.0.3 的 3 条新档案（18-TECH §2.2 #1~#11 的作物面）
    "friend_chives", "point_defense_fungus", "vibration_bean",
]

EXPECTED = {
    "items": CROP_IDS + [
        "molotov", "zangief_cane", "wild_apple", "compressed_puffed_wheat",
        "puffed_wheat_seeds", "power_beet_seeds", "melon_grenade_seeds",
        "pumpkin_turret_seeds", "awakened_torchflower_seeds",
        "awakened_pitcher_plant_pod", "converter", "compressed_hay_block",
        "chive_seeds", "outpost_core",  # v0.1.0 的 BlockItem（24-TECH §1.4 G1）
        "cultivator",  # v0.2.0 的培养器方块物品（36-TECH §1.2）
    ],
    "blocks": [
        "puffed_wheat_crop", "blast_carrot_crop", "ubi_server_crop", "toxic_ubi_server_crop",
        "power_beet_crop", "melon_grenade_stem", "pumpkin_turret_stem",
        "awakened_torchflower_crop", "awakened_pitcher_plant_crop", "fang_trap_cactus_block",
        "bamboo_firecracker_stalk", "red_mushroom_puff_block", "brown_mushroom_puff_block",
        "lucky_berry_bush",
        "blast_carrot", "pumpkin_turret", "awakened_torchflower", "awakened_pitcher_plant",
        "fang_trap_cactus", "converter", "zangief_cane", "compressed_hay_block",
        "friend_chives_crop", "point_defense_fungus", "vibration_bean",
        "outpost_core",  # v0.1.0 的据点核心方块（24-TECH §1.4 G1）
        "cultivator",  # v0.2.0 的培养器方块（36-TECH §1.2）
    ],
    "block_entity_type": [
        "blast_carrot", "pumpkin_turret", "awakened_torchflower",
        "awakened_pitcher_plant", "fang_trap_cactus", "converter",
        "outpost_core",  # v0.1.0 新建的 BlockEntityType（24-TECH §2.2 注 1）
        "cultivator",  # v0.2.0 的培养器 BlockEntityType（36-TECH §1.2）
    ],
    "entity_type": [
        "puffed_wheat", "ubi_server", "toxic_ubi_server", "melon_slice_bomb", "melon_grenade",
        "bamboo_firecracker", "red_mushroom_puff", "brown_mushroom_puff", "turret_bullet",
        "molotov", "compressed_puffed_wheat", "friend_chives", "vibration_bean",
    ],
    "mob_effect": ["wild_hunter", "zangief_piledriver", "q_bounce", "confusion"],
    "sound_event": ["wild_hunter_loop", "zangief_piledriver", "friend_chives"],
    "recipe_type": ["converting"],
    "recipe_serializer": ["converting"],
    "creative_mode_tab": ["main"],
    "damage_type": ["void_bolt", "crop_blast", "zangief_slam"],
    "data_component_type": ["temporary_enchantments"],
    "menu_type": ["converter", "outpost_core"],  # v0.2.0 的核心存储菜单（36-TECH §1.2）
}

# 36-TECH-0.2.0 §1.2 的 v0.2.0 列：12 类逐类小计，相加 97（基线 93 + 本版 4）。
# ID 清单少一个、多一个都在这里露出来。
EXPECTED_COUNTS = {
    "items": 33,
    "blocks": 27,
    "block_entity_type": 8,
    "entity_type": 13,
    "mob_effect": 4,
    "sound_event": 3,
    "recipe_type": 1,
    "recipe_serializer": 1,
    "creative_mode_tab": 1,
    "damage_type": 3,
    "data_component_type": 1,
    "menu_type": 2,
}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=str(Path(__file__).resolve().parent.parent))
    args = ap.parse_args()
    root = Path(args.root)
    java = root / "src/main/java/com/dashpig/farmstorm"
    all_java = "\n".join(read(f) for f in java.rglob("*.java")) if java.exists() else ""

    print("== 1. config：文档键集 vs 生成的 toml ==")
    design = read(root / "docs/zh/01-DESIGN.md")
    tech = read(root / "docs/zh/12-TECH-0.0.2.md")
    tech3 = read(root / "docs/zh/18-TECH-0.0.3.md")
    tech4 = root / "docs/zh/24-TECH-0.1.0.md"
    tech5 = root / "docs/zh/30-TECH-0.1.1.md"
    tech6 = root / "docs/zh/36-TECH-0.2.0.md"
    base = expected_config_keys(design)
    added = expected_config_keys_v002(tech)
    added3 = expected_config_keys_v003(tech3)
    if len(added3) != 19:
        fail(f"18-TECH §8.1 解析出 {len(added3)} 行，文档口径是 19 行（表格格式变了？）")
    # 两份 TECH 的行里各有若干行是「改既有默认值」——键名与前面的清单重名，不是新键。
    # 因此期望集是四个清单的并集（集合去重），重复行数现算、不写死：
    # v0.0.3 是 3 行（139 + 16 = 155），v0.1.0 再加 1 行（155 + 11 = 166）。
    try:
        added4, changed4 = expected_config_keys_v010(tech4)
    except (ValueError, OSError) as exc:
        fail(f"24-TECH §8.1 解析失败，期望键集无法现算：{exc}")
        added4 = None
    try:
        added5, groups5 = expected_config_keys_v011(tech5)
    except (ValueError, OSError) as exc:
        fail(f"30-TECH §4.1 解析失败，期望键集无法现算：{exc}")
        added5 = None
    try:
        added6, removed6 = expected_config_keys_v012(tech6)
    except (ValueError, OSError) as exc:
        fail(f"36-TECH §4.1.1 解析失败，期望键集无法现算：{exc}")
        added6 = None
    if added4 is None or added5 is None or added6 is None:
        exp_ok, exp = False, set()
    else:
        before6 = set(base) | set(added) | set(added3) | set(added4) | set(added5)
        overlap = set(added3) & (set(base) | set(added))
        overlap4 = set(added4) & (set(base) | set(added) | set(added3))
        overlap5 = set(added5) & (set(base) | set(added) | set(added3) | set(added4))
        overlap6 = set(added6) & before6
        union = before6 | set(added6)
        # 已删键在并集旁边扣一次（REMOVED_KEYS 的唯一用处），不散落到解析函数里；
        # v0.2.0 的第 2 个删键由 36-TECH §4.1.1 现算（removed6），不写死在这里。
        gone = REMOVED_KEYS | set(removed6)
        exp_ok, exp = True, union - gone
        print(f"  30-TECH §4.1：{groups5} 组 / {len(added5)} 键"
              f"（v0.1.1 的 58 行数据表现算，其中 {len(overlap5)} 个键名已在前四份清单里）")
        print(f"  36-TECH §4.1.1：{len(added6) + len(removed6)} 行（增 {len(added6)} / 删 {len(removed6)}；"
              f"v0.2.0 的 11 行数据表现算，其中 {len(overlap6)} 个键名与前面的清单重名）")
        print(f"  剔除已删键 {len(gone)} 个：{'、'.join(sorted(gone))}")
        print(f"  文档口径键数: {len(exp)}（01-DESIGN §3.8 {len(base)} + 12-TECH §8.1 {len(added)}"
              f" + 18-TECH §8.1 {len(added3)} 行"
              f" + 24-TECH §8.1 {len(added4) + len(changed4)} 行（新 {len(added4)} + 改 {len(changed4)}）"
              f" + 30-TECH §4.1 {len(added5)} 键 + 36-TECH §4.1.1 {len(added6)} 键；其中 24 / 18 号另有"
              f" {len(overlap) + len(overlap4)} 行是改既有默认值、§4.1 另有 {len(overlap5)} 个键名与"
              f"前面的清单重名；按并集去重后 {len(union)}，再扣掉 {len(gone)} 个已删键 = {len(exp)}）")
    toml = root / "run/config/farmstorm-server.toml"
    if not exp_ok:
        print("  生成 toml 键数: 跳过（期望集没算出来，见上面的 [FAIL]）")
    elif toml.exists():
        got = toml_keys(read(toml))
        print(f"  生成 toml 键数: {len(got)}")
        # 文档的「完整键路径」是逻辑路径，落盘键名另由 b.push(组) + defineInRange(键) 决定：
        # 36 号 §4.1.1 写 trade.common.min，落盘是 [trade] 段的 commonMin（同 6 个键）。
        # 归一后才匹配上的键单独 WARN 登记，不静默吞掉；其它键仍逐字比对。
        spelled = {k: doc_key_variants(k) for k in exp}
        accepted = {v for vs in spelled.values() for v in vs}
        only_variant = sorted(k for k in exp if k not in got and any(v in got for v in spelled[k]))
        missing = sorted(k for k in exp if not any(v in got for v in spelled[k]))
        extra = sorted(k for k in got if k not in accepted)
        if only_variant:
            warn(f"{len(only_variant)} 个键在文档里是逻辑路径、落盘是组内拼写（形状归一后才对上，已登记为文档-源码偏差）: "
                 f"{[k + ' -> ' + spelled[k][-1] for k in only_variant[:6]]}")
        if missing or extra:
            config_java = root / "src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java"
            # 判据是源码里有没有键字面量；mtime 只进提示文字，不参与判定
            literal = (set(re.findall(r'"([A-Za-z0-9_.]+)"', read(config_java)))
                       if config_java.exists() else set())

            def in_source(k: str) -> bool:
                return k in literal or k.rsplit(".", 1)[-1] in literal

            # 源码有而 toml 没有 ⇒ 产物没重生成；源码没有而 toml 有 ⇒ 旧键名残留
            stale_keys = [k for k in missing if in_source(k)] + [k for k in extra if not in_source(k)]
            real_missing = [k for k in missing if not in_source(k)]
            real_extra = [k for k in extra if in_source(k)]
            if stale_keys:
                stamp = f"mtime {mtime(toml)}"
                if config_java.exists() and toml.stat().st_mtime < config_java.stat().st_mtime:
                    stamp += f"，早于 config/FarmstormConfig.java 的 {mtime(config_java)}"
                stale(f"run/config/farmstorm-server.toml 过期（{stamp}）："
                      f"缺 {len(missing)} 个键、多 {len(extra)} 个键，重新生成即可消除"
                      f"（缺 {missing[:3]}，多 {extra[:3]}）；"
                      f"跑一次 .tools\\gw.cmd runServer 让它重新生成后再验")
            if real_missing:
                fail(f"toml 缺 {len(real_missing)} 个键且源码里也没有（文档/代码不一致 MISMATCH）: {real_missing[:8]}")
            if real_extra:
                fail(f"toml 多 {len(real_extra)} 个键且源码里存在（文档漏记 MISMATCH）: {real_extra[:8]}")
        else:
            ok(f"{len(exp)} 个配置键与文档逐字一致（toml {len(got)} 键）")
    else:
        warn("run/config/farmstorm-server.toml 不存在（先跑一次 runServer）")

    print("== 2. 97 个注册 ID 逐字核对（全源码树） ==")
    total = 0
    for group, ids in EXPECTED.items():
        missed = [i for i in ids if f'"{i}"' not in all_java]
        if missed:
            fail(f"{group} 缺 {len(missed)} 个 ID: {missed[:6]}")
        else:
            ok(f"{group}: {len(ids)} 个 ID 齐全")
        expected = EXPECTED_COUNTS[group]
        if len(ids) != expected:
            fail(f"{group}: 清单 {len(ids)} 个 ID，但 36-TECH §1.2 的 v0.2.0 列是 {expected} 个")
        total += len(ids)
    print(f"  已核 registry 对象: {total} / 97")

    print("== 3. 语言文件键集一致 ==")
    gen = root / "src/generated/resources"
    zh, en = gen / "assets/farmstorm/lang/zh_cn.json", gen / "assets/farmstorm/lang/en_us.json"
    if zh.exists() and en.exists():
        a, b = set(json.loads(read(zh))), set(json.loads(read(en)))
        if a != b:
            fail(f"lang 键集不一致：仅中文 {sorted(a - b)[:5]}，仅英文 {sorted(b - a)[:5]}")
        else:
            ok(f"lang 键集一致，共 {len(a)} 键")
    else:
        warn("语言文件尚未生成（DataGen 前）")

    print("== 4. 禁令扫描 ==")
    bad = 0
    for f in java.rglob("*.java"):
        body = read(f)
        for pat, label in (
            (r"@Mixin\b", "Mixin 注解"),
            (r"\bmixins\.json\b", "mixin 配置"),
            (r"TODO\(api-unverified\)|//\s*TODO\b", "TODO 占位"),
            (r"printStackTrace|System\.out\.println", "调试残留"),
            (r"\[PROBE\]|\[probe\]", "临时探针日志"),
            (r"catch\s*\(\s*Exception\s+\w+\s*\)\s*\{\s*\}", "空 catch"),
        ):
            if re.search(pat, body):
                fail(f"{f.relative_to(root)}: 命中 {label}")
                bad += 1
    if bad == 0:
        ok("无 Mixin / TODO / 调试残留 / 空 catch")

    print()
    print(f"结论：FAIL {len(FAIL)} 项，WARN {len(WARN)} 项")
    return 1 if FAIL else 0


if __name__ == "__main__":
    sys.exit(main())

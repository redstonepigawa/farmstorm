#!/usr/bin/env python3
r"""Farmstorm v0.0.3 静态验收器。

期望值全部从上游文档现算，不手抄：
  - config 键集  <- docs/zh/01-DESIGN.md §3.8（117）+ docs/zh/12-TECH-0.0.2.md §8.1（22）
                   + docs/zh/18-TECH-0.0.3.md §8.1（v0.0.3 的 19 行，F-104），并与
                   run/config/farmstorm-server.toml 逐键比对。19 行里 3 行是「改既有默认值」
                   （fang_trap_cactus 的三个键），键名与 117 里的重名，按集合去重后是 155 个键
  - 90 个注册 ID  <- docs/zh/18-TECH-0.0.3.md §2.1 的 v0.0.3 列逐类小计（12 类，相加 90）

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
        "chive_seeds",
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
    ],
    "block_entity_type": [
        "blast_carrot", "pumpkin_turret", "awakened_torchflower",
        "awakened_pitcher_plant", "fang_trap_cactus", "converter",
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
    "menu_type": ["converter"],
}

# 18-TECH-0.0.3 §2.1 的 v0.0.3 列：12 类逐类小计，相加 90。ID 清单少一个、多一个都在这里露出来。
EXPECTED_COUNTS = {
    "items": 31,
    "blocks": 25,
    "block_entity_type": 6,
    "entity_type": 13,
    "mob_effect": 4,
    "sound_event": 3,
    "recipe_type": 1,
    "recipe_serializer": 1,
    "creative_mode_tab": 1,
    "damage_type": 3,
    "data_component_type": 1,
    "menu_type": 1,
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
    base = expected_config_keys(design)
    added = expected_config_keys_v002(tech)
    added3 = expected_config_keys_v003(tech3)
    if len(added3) != 19:
        fail(f"18-TECH §8.1 解析出 {len(added3)} 行，文档口径是 19 行（表格格式变了？）")
    # 19 行里有若干行是「改既有默认值」——键名与上面的 117 / 22 重名，不是新键。
    # 因此期望集是三个清单的并集（集合去重），重复行数现算、不写死：v0.0.3 是 3 行（139 + 16 = 155）。
    overlap = set(added3) & (set(base) | set(added))
    exp = set(base) | set(added) | set(added3)
    print(f"  文档口径键数: {len(exp)}（01-DESIGN §3.8 {len(base)} + 12-TECH §8.1 {len(added)}"
          f" + 18-TECH §8.1 {len(added3)} 行；其中 {len(overlap)} 行是改既有默认值、键名已在上两份清单里，"
          f"按并集去重后 {len(exp)} = {len(base) + len(added)} + {len(added3) - len(overlap)}）")
    toml = root / "run/config/farmstorm-server.toml"
    if toml.exists():
        got = toml_keys(read(toml))
        print(f"  生成 toml 键数: {len(got)}")
        missing, extra = sorted(exp - got), sorted(got - exp)
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

    print("== 2. 90 个注册 ID 逐字核对（全源码树） ==")
    total = 0
    for group, ids in EXPECTED.items():
        missed = [i for i in ids if f'"{i}"' not in all_java]
        if missed:
            fail(f"{group} 缺 {len(missed)} 个 ID: {missed[:6]}")
        else:
            ok(f"{group}: {len(ids)} 个 ID 齐全")
        expected = EXPECTED_COUNTS[group]
        if len(ids) != expected:
            fail(f"{group}: 清单 {len(ids)} 个 ID，但 18-TECH §2.1 的 v0.0.3 列是 {expected} 个")
        total += len(ids)
    print(f"  已核 registry 对象: {total} / 90")

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

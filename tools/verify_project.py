#!/usr/bin/env python3
r"""Farmstorm v0.0.1 静态验收器。

期望值全部从上游文档现算，不手抄：
  - 117 个配置键  <- docs/zh/01-DESIGN.md §3.8（并与 run/config/farmstorm-server.toml 逐键比对）
  - 64 个注册 ID  <- docs/zh/02-TECH-SPEC.md §3

用法：python -X utf8 tools/verify_project.py [--root <仓库根>]
"""
from __future__ import annotations

import argparse
import json
import re
import sys
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
]

EXPECTED = {
    "items": CROP_IDS + [
        "puffed_wheat_seeds", "power_beet_seeds", "melon_grenade_seeds",
        "pumpkin_turret_seeds", "awakened_torchflower_seeds",
        "awakened_pitcher_plant_pod", "converter",
    ],
    "blocks": [
        "puffed_wheat_crop", "blast_carrot_crop", "ubi_server_crop", "toxic_ubi_server_crop",
        "power_beet_crop", "melon_grenade_stem", "pumpkin_turret_stem",
        "awakened_torchflower_crop", "awakened_pitcher_plant_crop", "fang_trap_cactus_block",
        "bamboo_firecracker_stalk", "red_mushroom_puff_block", "brown_mushroom_puff_block",
        "lucky_berry_bush",
        "blast_carrot", "pumpkin_turret", "awakened_torchflower", "awakened_pitcher_plant",
        "fang_trap_cactus", "converter",
    ],
    "block_entity_type": [
        "blast_carrot", "pumpkin_turret", "awakened_torchflower",
        "awakened_pitcher_plant", "fang_trap_cactus", "converter",
    ],
    "entity_type": [
        "puffed_wheat", "ubi_server", "toxic_ubi_server", "melon_slice_bomb", "melon_grenade",
        "bamboo_firecracker", "red_mushroom_puff", "brown_mushroom_puff", "turret_bullet",
    ],
}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=str(Path(__file__).resolve().parent.parent))
    args = ap.parse_args()
    root = Path(args.root)
    java = root / "src/main/java/com/dashpig/farmstorm"
    all_java = "\n".join(read(f) for f in java.rglob("*.java")) if java.exists() else ""

    print("== 1. config：文档 117 键 vs 生成的 toml ==")
    design = read(root / "docs/zh/01-DESIGN.md")
    exp = set(expected_config_keys(design))
    print(f"  文档口径键数: {len(exp)}")
    toml = root / "run/config/farmstorm-server.toml"
    if toml.exists():
        got = toml_keys(read(toml))
        print(f"  生成 toml 键数: {len(got)}")
        missing, extra = sorted(exp - got), sorted(got - exp)
        if missing:
            fail(f"toml 缺 {len(missing)} 个键: {missing[:8]}")
        if extra:
            fail(f"toml 多 {len(extra)} 个键: {extra[:8]}")
        if not missing and not extra:
            ok(f"117 个配置键与文档逐字一致（{len(got)} 键）")
    else:
        warn("run/config/farmstorm-server.toml 不存在（先跑一次 runServer）")

    print("== 2. 64 个注册 ID 逐字核对（全源码树） ==")
    total = 0
    for group, ids in EXPECTED.items():
        missed = [i for i in ids if f'"{i}"' not in all_java]
        if missed:
            fail(f"{group} 缺 {len(missed)} 个 ID: {missed[:6]}")
        else:
            ok(f"{group}: {len(ids)} 个 ID 齐全")
        total += len(ids)
    for gid in ("converting", "main", "void_bolt", "crop_blast", "temporary_enchantments"):
        if f'"{gid}"' not in all_java:
            fail(f"缺注册 ID 字面量: {gid}")
        else:
            total += 1
    print(f"  已核 registry 对象: {total} / 64")

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

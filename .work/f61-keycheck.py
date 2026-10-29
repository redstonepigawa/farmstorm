import re, pathlib

root = pathlib.Path(__file__).resolve().parent.parent
brief = (root / '.work/v002-BRIEF.md').read_text(encoding='utf-8').splitlines()
toml = (root / 'run-f61/config/farmstorm-server.toml').read_text(encoding='utf-8').splitlines()

rows = []
for line in brief:
    m = re.match(r'^\|\s*(\d+)\s*\|\s*`([a-z_]+\.[a-zA-Z0-9_]+)`\s*\|\s*([a-z]+)\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|', line)
    if m:
        idx, key, typ, dflt, rng = m.groups()
        dn = re.search(r'-?\d+(?:\.\d+)?', dflt.replace('*', ''))
        rr = re.search(r'(-?\d+(?:\.\d+)?)\s*[–\-]\s*(-?\d+(?:\.\d+)?)', rng)
        rows.append((int(idx), key, typ, float(dn.group(0)), (float(rr.group(1)), float(rr.group(2)))))

# parse toml: value + "# Range:" comment bound to the key
sec = ''
data = {}
pending_range = None
for raw in toml:
    s = raw.strip()
    if not s:
        continue
    if s.startswith('[') and s.endswith(']'):
        sec = s[1:-1]
        continue
    if s.startswith('#'):
        mr = re.search(r'#\s*Range:\s*(-?[\d.]+)\s*~\s*(-?[\d.]+)', s)
        if mr:
            pending_range = (float(mr.group(1)), float(mr.group(2)))
        continue
    m = re.match(r'^([A-Za-z0-9_.\-]+)\s*=\s*(.*)$', s)
    if m:
        data[f'{sec}.{m.group(1)}'] = (m.group(2).strip(), pending_range)
        pending_range = None

print(f'BRIEF §8 rows parsed: {len(rows)}')
bad = 0
for idx, key, typ, dflt, rng in rows:
    tv, trng = data.get(key, (None, None))
    dok = tv is not None and abs(float(tv) - dflt) < 1e-9
    rok = trng == rng
    if not (dok and rok):
        bad += 1
    print(f'{idx:>2} {key:<38} toml={tv!s:<8} brief={dflt!s:<8} default={"OK" if dok else "MISMATCH"}'
          f'  range toml={trng} brief={rng} {"OK" if rok else "MISMATCH"}')
print(f'22 键齐备: {len([1 for r in rows if r[1] in data])}/{len(rows)}；default+range 不一致: {bad}')
print('toml 里 molotov/wild_apple/wild_hunter/zangief_cane 之外的同名键:',
      sorted(k for k in data if k.split('.')[0] in ('molotov', 'wild_apple', 'wild_hunter', 'zangief_cane')) == sorted(r[1] for r in rows))

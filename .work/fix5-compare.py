import re, sys
from pathlib import Path

def parse_space(p):
    out = {}
    for line in Path(p).read_text(encoding='utf-8').splitlines():
        m = re.match(r'^(.*?)\s+(\d+)\s+([0-9A-Fa-f]{32})$', line.strip())
        if m:
            out[m.group(1).strip()] = (int(m.group(2)), m.group(3).upper())
    return out

def parse_pipe(p):
    out = {}
    for line in Path(p).read_text(encoding='utf-8').splitlines():
        parts = line.strip().split('|')
        if len(parts) == 3:
            out[parts[0]] = (int(parts[1]), parts[2].upper())
    return out

before = parse_space('.work/fix5-md5-before.txt')
after = parse_pipe('.work/fix5-md5-b.txt')
print('before entries', len(before), 'after entries', len(after))
changed = [k for k in before if k in after and before[k][1] != after[k][1]]
gone = [k for k in before if k not in after]
added = [k for k in after if k not in before]
print('CHANGED', len(changed))
for k in sorted(changed): print('  CHG', k, before[k][1], '->', after[k][1])
print('GONE', len(gone))
for k in sorted(gone): print('  GONE', k)
print('ADDED', len(added))
for k in sorted(added): print('  ADD', k, after[k][0], 'B', after[k][1])
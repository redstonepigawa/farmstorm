import json
from pathlib import Path
from PIL import Image

gen = Path('src/generated/resources/assets/farmstorm')
assets = Path('src/main/resources/assets')

tex_refs = 0
missing_tex = []
missing_parent = []
foreign = []
for m in sorted((gen / 'models').rglob('*.json')):
    rel = m.relative_to(gen / 'models').as_posix()
    data = json.loads(m.read_text(encoding='utf-8'))
    parent = data.get('parent')
    if isinstance(parent, str) and parent.startswith('farmstorm:'):
        target = gen / 'models' / (parent[len('farmstorm:'):] + '.json')
        if not target.is_file():
            missing_parent.append(rel + ' -> ' + parent)
    for key, value in (data.get('textures') or {}).items():
        if not isinstance(value, str) or value.startswith('#'):
            continue
        tex_refs += 1
        ns, _, path = value.partition(':') if ':' in value else ('minecraft', '', value)
        if ns != 'farmstorm':
            foreign.append(rel + ' ' + key + '=' + value)
            continue
        png = assets / 'farmstorm' / 'textures' / (path + '.png')
        if not png.is_file():
            missing_tex.append(rel + ' ' + key + '=' + value)

print('texture refs examined =', tex_refs)
print('dangling texture refs =', len(missing_tex))
for x in missing_tex: print('  MISSING-PNG:', x)
print('dangling parent models =', len(missing_parent))
for x in missing_parent: print('  MISSING-MODEL:', x)
print('non-farmstorm texture refs =', len(foreign))
for x in foreign: print('  FOREIGN-NS:', x)
print('RESULT:', 'CLEAN' if not (missing_tex or missing_parent or foreign) else 'BROKEN')

print()
blockdir = assets / 'farmstorm' / 'textures' / 'block'
conv = sorted(blockdir.glob('converter*.png'))
print('textures/block/converter*.png =', len(conv))
for p in conv:
    with Image.open(p) as im:
        print('  %-32s %dx%d  %d B' % (p.name, im.width, im.height, p.stat().st_size))
total = len(list((assets / 'farmstorm' / 'textures').rglob('*.png')))
print('farmstorm textures total =', total)
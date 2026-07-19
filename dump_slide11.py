import zipfile
import re
from pathlib import Path
from xml.etree import ElementTree as ET

p = Path('QUIZ_PLATFORM.pptx')
output = Path('slide11_dump.txt')
with zipfile.ZipFile(p, 'r') as z:
    data = z.read('ppt/slides/slide11.xml')
root = ET.fromstring(data)
NS = {'a':'http://schemas.openxmlformats.org/drawingml/2006/main', 'p':'http://schemas.openxmlformats.org/presentationml/2006/main'}
lines = []
for i, sp in enumerate(root.findall('.//p:sp', NS), start=1):
    texts = [t.text or '' for t in sp.findall('.//a:t', NS)]
    lines.append(f'shape {i}:')
    lines.append('\n'.join(texts) or '<empty>')
    lines.append('-' * 40)
output.write_text('\n'.join(lines), encoding='utf-8')
print('Wrote', output)

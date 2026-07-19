import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET

p = Path('QUIZ_PLATFORM.pptx')
if not p.exists():
    raise SystemExit('QUIZ_PLATFORM.pptx not found')

with zipfile.ZipFile(p, 'r') as z:
    data = z.read('ppt/slides/slide11.xml')
root = ET.fromstring(data)
NS = {
    'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
    'p': 'http://schemas.openxmlformats.org/presentationml/2006/main'
}
for i, sp in enumerate(root.findall('.//p:sp', NS), start=1):
    texts = [t.text or '' for t in sp.findall('.//a:t', NS)]
    print('shape', i)
    print('\n'.join(texts) or '<empty>')
    print('-----')

for i, txBody in enumerate(root.findall('.//a:txBody', NS), start=1):
    paragraphs = []
    for p_elem in txBody.findall('a:p', NS):
        paragraphs.append(''.join(t.text or '' for t in p_elem.findall('.//a:t', NS)))
    print('txBody', i)
    print('\n'.join(paragraphs) or '<empty>')
    print('-----')

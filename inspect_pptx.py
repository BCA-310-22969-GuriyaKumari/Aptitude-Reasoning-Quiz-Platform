import zipfile
import re
from pathlib import Path
from xml.etree import ElementTree as ET

p = Path('QUIZ_PLATFORM.pptx')
if not p.exists():
    raise SystemExit('QUIZ_PLATFORM.pptx not found')

with zipfile.ZipFile(p, 'r') as z:
    slides = [n for n in z.namelist() if n.startswith('ppt/slides/slide')]
    slides.sort(key=lambda s: int(re.search(r'slide(\d+)', s).group(1)))
    print('slides:', slides)
    for slide in slides:
        data = z.read(slide)
        tree = ET.fromstring(data)
        texts = [t.text or '' for t in tree.findall('.//{http://schemas.openxmlformats.org/drawingml/2006/main}t')]
        print('\n===', slide, '===')
        print('\n'.join(texts))

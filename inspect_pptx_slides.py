import zipfile
import re
from pathlib import Path
from xml.etree import ElementTree as ET

p = Path('QUIZ_PLATFORM.pptx')
with zipfile.ZipFile(p, 'r') as z:
    slides = [n for n in z.namelist() if n.startswith('ppt/slides/slide')]
    slides.sort(key=lambda s: int(re.search(r'slide(\d+)', s).group(1)))
    target = {'ppt/slides/slide5.xml': 'Process Logic', 'ppt/slides/slide10.xml': 'ER Diagram', 'ppt/slides/slide11.xml': 'Interface'}
    for slide in slides:
        if slide in target:
            data = z.read(slide)
            root = ET.fromstring(data)
            NS = {'a':'http://schemas.openxmlformats.org/drawingml/2006/main', 'p':'http://schemas.openxmlformats.org/presentationml/2006/main'}
            print('===', slide, '===')
            for i, sp in enumerate(root.findall('.//p:sp', NS), start=1):
                texts = [t.text or '' for t in sp.findall('.//a:t', NS)]
                if texts:
                    print('shape', i, repr('\n'.join(texts)))
            print('-----')

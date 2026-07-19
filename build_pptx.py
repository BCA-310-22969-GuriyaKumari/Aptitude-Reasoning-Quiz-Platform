import zipfile
import shutil
from pathlib import Path
from xml.etree import ElementTree as ET

def add_text_to_slide(root, text_to_add, ns):
    """Add text to an existing text box or create new paragraph"""
    text_bodies = root.findall('.//a:txBody', ns)
    if text_bodies:
        last_txbody = text_bodies[-1]
        p_elem = ET.Element('{http://schemas.openxmlformats.org/drawingml/2006/main}p')
        r_elem = ET.SubElement(p_elem, '{http://schemas.openxmlformats.org/drawingml/2006/main}r')
        rPr = ET.SubElement(r_elem, '{http://schemas.openxmlformats.org/drawingml/2006/main}rPr')
        rPr.set('lang', 'en-US')
        rPr.set('dirty', '0')
        t_elem = ET.SubElement(r_elem, '{http://schemas.openxmlformats.org/drawingml/2006/main}t')
        t_elem.text = text_to_add
        last_txbody.append(p_elem)
        return True
    return False

pptx_path = Path('QUIZ_PLATFORM.pptx')
backup_path = Path('QUIZ_PLATFORM_backup.pptx')
shutil.copy(pptx_path, backup_path)
print(f'Backed up to {backup_path}')

extract_dir = Path('pptx_temp')
if extract_dir.exists():
    shutil.rmtree(extract_dir)
with zipfile.ZipFile(pptx_path, 'r') as z:
    z.extractall(extract_dir)

NS = {
    'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
    'p': 'http://schemas.openxmlformats.org/presentationml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
}

slide5_path = extract_dir / 'ppt/slides/slide5.xml'
tree5 = ET.parse(slide5_path)
root5 = tree5.getroot()
add_text_to_slide(root5, 'Admin Panel Flow:', NS)
add_text_to_slide(root5, 'Admin Login → Dashboard → Manage Questions → View Results', NS)
add_text_to_slide(root5, 'Includes: Add/Edit/Delete Questions, Monitor Performance', NS)
tree5.write(slide5_path, encoding='utf-8', xml_declaration=True)
print('Updated Slide 5: Process Logic')

slide10_path = extract_dir / 'ppt/slides/slide10.xml'
tree10 = ET.parse(slide10_path)
root10 = tree10.getroot()
add_text_to_slide(root10, 'Admin Role:', NS)
add_text_to_slide(root10, 'Manages Question entities', NS)
add_text_to_slide(root10, 'Reviews Result entities', NS)
add_text_to_slide(root10, 'Authentication protects admin operations', NS)
tree10.write(slide10_path, encoding='utf-8', xml_declaration=True)
print('Updated Slide 10: ER Diagram')

slide11_path = extract_dir / 'ppt/slides/slide11.xml'
tree11 = ET.parse(slide11_path)
root11 = tree11.getroot()
add_text_to_slide(root11, 'Admin Interface Screens:', NS)
add_text_to_slide(root11, '• Admin Login Page', NS)
add_text_to_slide(root11, '• Dashboard (Questions, Results, Stats)', NS)
add_text_to_slide(root11, '• Question Management (Add/Edit/Delete)', NS)
add_text_to_slide(root11, '• Results Viewer (Student Performance)', NS)
tree11.write(slide11_path, encoding='utf-8', xml_declaration=True)
print('Updated Slide 11: Interface')

output_pptx = Path('QUIZ_PLATFORM_UPDATED.pptx')
if output_pptx.exists():
    output_pptx.unlink()

def zip_directory(folder_path, zip_path):
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in (folder_path).walk():
            for file in files:
                file_path = root / file
                arcname = file_path.relative_to(folder_path)
                zipf.write(file_path, arcname)

zip_directory(extract_dir, output_pptx)
print(f'Created updated PPTX: {output_pptx}')

shutil.rmtree(extract_dir)
print('Done!')

import zipfile
import shutil
from pathlib import Path
from xml.etree import ElementTree as ET
from lxml import etree

# Define the input and output paths
pptx_path = Path('QUIZ_PLATFORM.pptx')
pptx_backup = Path('QUIZ_PLATFORM_BACKUP.pptx')
pptx_output = Path('QUIZ_PLATFORM_UPDATED.pptx')

# Backup the original
shutil.copy(pptx_path, pptx_backup)
print(f'✓ Backup created: {pptx_backup}')

# Define namespaces
NS = {
    'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
    'p': 'http://schemas.openxmlformats.org/presentationml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
}

# Register namespaces to preserve prefixes
for prefix, uri in NS.items():
    ET.register_namespace(prefix, uri)

# Extract PPTX
temp_dir = Path('pptx_temp')
if temp_dir.exists():
    shutil.rmtree(temp_dir)
with zipfile.ZipFile(pptx_path, 'r') as z:
    z.extractall(temp_dir)
print(f'✓ PPTX extracted to {temp_dir}')

# Define slide updates
updates = {
    'ppt/slides/slide5.xml': {
        'title': 'Process Logic',
        'append_text': '\n\nAdmin Workflow:\nAdmin login → Dashboard access → Question management (Add/Edit/Delete) → Database update → Results review'
    },
    'ppt/slides/slide10.xml': {
        'title': 'ER Diagram',
        'append_text': '\n\nAdmin Management Layer:\nAdmin panel manages Question and Result entities\nDirect database CRUD operations for questions\nReal-time result monitoring and reporting'
    },
    'ppt/slides/slide11.xml': {
        'title': 'Interface',
        'append_text': '\n\nAdmin Panel:\n- Admin Login Page\n- Admin Dashboard (Questions & Results Overview)\n- Question Management (Add/Edit/Delete)\n- Results Viewer'
    }
}

# Update each slide
for slide_path, content_info in updates.items():
    slide_file = temp_dir / slide_path
    if not slide_file.exists():
        print(f'⚠ Slide not found: {slide_file}')
        continue
    
    # Parse with lxml for better namespace handling
    tree = etree.parse(str(slide_file))
    root = tree.getroot()
    
    # Find the main text body (usually the last txBody in the slide)
    txbodies = root.findall('.//a:txBody', NS)
    
    if txbodies:
        # Get the last txBody (main content area)
        main_txbody = txbodies[-1]
        
        # Create a new paragraph with the appended text
        p_elem = etree.SubElement(main_txbody, '{%s}p' % NS['a'])
        
        # Create a run (text element)
        r_elem = etree.SubElement(p_elem, '{%s}r' % NS['a'])
        
        # Create text properties (copy from last run if available)
        runs = main_txbody.findall('.//a:r', NS)
        if runs:
            last_run_props = runs[-1].find('a:rPr', NS)
            if last_run_props is not None:
                rPr = etree.SubElement(r_elem, '{%s}rPr' % NS['a'])
                for attr, val in last_run_props.attrib.items():
                    rPr.set(attr, val)
                # Copy child elements
                for child in last_run_props:
                    rPr.append(etree.fromstring(etree.tostring(child)))
        else:
            # Create default text properties
            rPr = etree.SubElement(r_elem, '{%s}rPr' % NS['a'])
            rPr.set('lang', 'en-US')
            rPr.set('sz', '1800')
        
        # Add the text
        t_elem = etree.SubElement(r_elem, '{%s}t' % NS['a'])
        t_elem.text = content_info['append_text']
        
        # Save the modified slide
        tree.write(str(slide_file), encoding='utf-8', xml_declaration=True, pretty_print=True)
        print(f'✓ Updated slide: {slide_path}')
    else:
        print(f'⚠ No text body found in {slide_path}')

# Repackage the PPTX
with zipfile.ZipFile(pptx_output, 'w', zipfile.ZIP_DEFLATED) as zipf:
    for file in temp_dir.rglob('*'):
        if file.is_file():
            arcname = file.relative_to(temp_dir)
            zipf.write(file, arcname)

print(f'✓ PPTX updated and saved: {pptx_output}')

# Cleanup
shutil.rmtree(temp_dir)
print('✓ Temporary files cleaned up')

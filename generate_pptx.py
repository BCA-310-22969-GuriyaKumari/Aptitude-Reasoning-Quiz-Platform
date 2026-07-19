#!/usr/bin/env python3
import zipfile
import shutil
import os
from pathlib import Path
from xml.etree import ElementTree as ET

try:
    os.chdir(r'e:\BCA Final Year Project\spring-boot-quiz-app-main')
    print("Working directory:", os.getcwd())
    
    # Backup
    pptx_path = 'QUIZ_PLATFORM.pptx'
    if os.path.exists(pptx_path):
        shutil.copy(pptx_path, 'QUIZ_PLATFORM_backup.pptx')
        print("✓ Backup created")
    else:
        print("ERROR: QUIZ_PLATFORM.pptx not found")
        exit(1)
    
    # Extract
    if os.path.exists('pptx_temp'):
        shutil.rmtree('pptx_temp')
    
    with zipfile.ZipFile(pptx_path, 'r') as z:
        z.extractall('pptx_temp')
    print("✓ Extracted PPTX")
    
    # Register namespace
    ET.register_namespace('a', 'http://schemas.openxmlformats.org/drawingml/2006/main')
    ET.register_namespace('p', 'http://schemas.openxmlformats.org/presentationml/2006/main')
    ET.register_namespace('r', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
    
    NS = {
        'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
        'p': 'http://schemas.openxmlformats.org/presentationml/2006/main'
    }
    
    # Helper function
    def add_text(tree_path, text_items):
        tree = ET.parse(tree_path)
        root = tree.getroot()
        text_bodies = root.findall('.//a:txBody', NS)
        if text_bodies:
            for text in text_items:
                p = ET.Element('{http://schemas.openxmlformats.org/drawingml/2006/main}p')
                r = ET.SubElement(p, '{http://schemas.openxmlformats.org/drawingml/2006/main}r')
                rPr = ET.SubElement(r, '{http://schemas.openxmlformats.org/drawingml/2006/main}rPr')
                rPr.set('lang', 'en-US')
                rPr.set('dirty', '0')
                t = ET.SubElement(r, '{http://schemas.openxmlformats.org/drawingml/2006/main}t')
                t.text = text
                text_bodies[-1].append(p)
        tree.write(tree_path, encoding='utf-8', xml_declaration=True)
    
    # Update slides
    add_text('pptx_temp/ppt/slides/slide5.xml', [
        'Admin Panel Flow:',
        'Admin Login → Dashboard → Manage Questions → View Results'
    ])
    print("✓ Slide 5 updated")
    
    add_text('pptx_temp/ppt/slides/slide10.xml', [
        'Admin Role: Manages Question & Result Entities',
        'Authentication protects admin operations'
    ])
    print("✓ Slide 10 updated")
    
    add_text('pptx_temp/ppt/slides/slide11.xml', [
        'Admin Interface Screens:',
        'Admin Login • Dashboard • Question Manager • Results Viewer'
    ])
    print("✓ Slide 11 updated")
    
    # Repackage
    output = 'QUIZ_PLATFORM_UPDATED.pptx'
    if os.path.exists(output):
        os.remove(output)
    
    def zipdir(path, ziph):
        for root, dirs, files in os.walk(path):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, path)
                ziph.write(file_path, arcname)
    
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as zipf:
        zipdir('pptx_temp', zipf)
    print("✓ PPTX repackaged")
    
    # Cleanup
    shutil.rmtree('pptx_temp')
    
    print(f"\n✅ SUCCESS! Updated PPT created: {output}")
    print(f"Location: {os.path.abspath(output)}")
    
except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()

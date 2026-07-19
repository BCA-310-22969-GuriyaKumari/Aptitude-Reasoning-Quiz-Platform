import zipfile
import shutil
import os
from pathlib import Path

# Paths
pptx_path = 'QUIZ_PLATFORM.pptx'
output_path = 'QUIZ_PLATFORM_UPDATED.pptx'
backup_path = 'QUIZ_PLATFORM_BACKUP.pptx'
temp_extract = 'pptx_extracted'

# Create backup
if not os.path.exists(backup_path):
    shutil.copy(pptx_path, backup_path)

# Extract
if os.path.exists(temp_extract):
    shutil.rmtree(temp_extract)
os.makedirs(temp_extract)

with zipfile.ZipFile(pptx_path, 'r') as z:
    z.extractall(temp_extract)

print('Extracted PPTX')

# Read slides
slides_to_update = ['ppt/slides/slide5.xml', 'ppt/slides/slide10.xml', 'ppt/slides/slide11.xml']
append_content = {
    'ppt/slides/slide5.xml': 'Admin Workflow: Login - Dashboard - Question Mgmt (Add/Edit/Delete) - Database Update - Results Review',
    'ppt/slides/slide10.xml': 'Admin Management: Question and Result entities managed by Admin panel with direct CRUD operations',
    'ppt/slides/slide11.xml': 'Admin Panel Screens: Login Page, Dashboard, Question Manager, Results Viewer'
}

for slide in slides_to_update:
    slide_path = os.path.join(temp_extract, slide)
    if os.path.exists(slide_path):
        with open(slide_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        text_to_add = append_content.get(slide, '')
        
        # Simple approach: add text to the end of txBody
        if '</a:txBody>' in content:
            # Find last txBody and add paragraph before closing tag
            insert_pos = content.rfind('</a:txBody>')
            if insert_pos > 0:
                new_para = f'<a:p><a:r><a:t>{text_to_add}</a:t></a:r></a:p>'
                content = content[:insert_pos] + new_para + content[insert_pos:]
        
        with open(slide_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f'Updated: {slide}')

# Repackage
if os.path.exists(output_path):
    os.remove(output_path)

with zipfile.ZipFile(output_path, 'w', zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk(temp_extract):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, temp_extract)
            z.write(file_path, arcname)

print(f'Created: {output_path}')

# Cleanup
shutil.rmtree(temp_extract)
print('Done!')

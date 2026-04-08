import os
import re

html_dir = r"c:\Users\Charlene C. Dilig\OneDrive\Documents\Github\payroll\src\main\resources\templates\html"

count = 0
for file in os.listdir(html_dir):
    if not file.endswith(".html"):
        continue
    filepath = os.path.join(html_dir, file)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # We match <nav ... </nav> and replace it.
    new_content = re.sub(
        r'<nav[^>]*>.*?</nav>',
        '<nav th:replace="~{html/fragments/nav :: sidebar}"></nav>',
        content,
        flags=re.DOTALL
    )
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {file}")
        count += 1
print(f"Total files updated: {count}")

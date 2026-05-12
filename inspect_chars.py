file_path = r"C:\Users\Charlene C. Dilig\OneDrive\Documents\Github\payroll\src\main\resources\templates\html\addEmployee.html"

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if i in [121, 122, 123, 124, 125, 126, 127, 128, 129, 130]:
        print(f"Line {i+1}: {repr(line)}")

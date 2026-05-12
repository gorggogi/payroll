file_path = r"C:\Users\Charlene C. Dilig\OneDrive\Documents\Github\payroll\src\main\resources\templates\html\addEmployee.html"

with open(file_path, 'rb') as f:
    data = bytearray(f.read())

# Byte-level replacements (exact sequences found in the file)
replacements = [
    # Line 122: em-dash-like garble + right-single-quote -> " - '"
    (b'\xc3\xa2\xe2\x82\xac\xe2\x80\x9d', b" - '"),
    # Line 122: division-sign garble -> "/"
    (b'\xc3\x83\xc2\xb7', b'/'),
    # Line 123: em-dash-like garble + right-single-quote -> " - '"
    (b'\xc3\xa2\xe2\x82\xac\xe2\x80\x9d', b" - '"),
    # Line 123: broken 52Ã— -> "52x"
    (b'\xc3\x83', b''),
    # Line 123: division-sign garble -> "/"
    (b'\xc3\x83\xc2\xb7', b'/'),
    # Line 124: em-dash-like garble + right-single-quote -> " - '"
    (b'\xc3\xa2\xe2\x82\xac\xe2\x80\x9d', b" - '"),
    # Line 125: em-dash-like garble + right-single-quote -> " - '"
    (b'\xc3\xa2\xe2\x82\xac\xe2\x80\x9d', b" - '"),
    # Line 127: arrow garble + right-single-quote -> " -> '"
    (b'\xc3\xa2\xe2\x80\xa0\xe2\x80\x99', b" -> '"),
]

changed = True
iterations = 0
while changed and iterations < 10:
    changed = False
    iterations += 1
    for old, new in replacements:
        if old in data:
            data = data.replace(old, new)
            changed = True
            print(f"Replaced {old} -> {new}")

with open(file_path, 'wb') as f:
    f.write(data)

print("\nVerification (lines 122-131):")
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines[121:131], start=122):
    print(f"Line {i}: {line.rstrip()}")

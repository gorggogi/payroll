file_path = r"C:\Users\Charlene C. Dilig\OneDrive\Documents\Github\payroll\src\main\resources\templates\html\addEmployee.html"

with open(file_path, 'rb') as f:
    data = f.read()

# Find all non-ASCII bytes and show context
i = 0
lines = data.split(b'\n')
print(f"Total lines: {len(lines)}")

# Look at lines 121-131 (0-indexed)
for idx in range(121, 132):
    if idx < len(lines):
        line = lines[idx]
        print(f"\nLine {idx+1} (len={len(line)}):")
        # Show hex of non-ASCII portions
        j = 0
        while j < len(line):
            c = line[j]
            if c > 127:
                # Show the sequence
                seq = []
                k = j
                while k < len(line) and line[k] > 127:
                    seq.append(f"{line[k]:02x}")
                    k += 1
                print(f"  Non-ASCII at byte {j}: {' '.join(seq)}")
                j = k
            else:
                j += 1
        # Show the actual line text
        try:
            text = line.decode('utf-8')
        except:
            text = line.decode('utf-8', errors='replace')
        print(f"  Text: {repr(text[:100])}")

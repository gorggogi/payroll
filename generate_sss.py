import decimal

sql = "TRUNCATE TABLE ssstable;\n"
sql += "INSERT INTO ssstable (rangeFrom, rangeTo, employeeShare, employerShare, effectiveYear) VALUES\n"

msc = 4000
range_from = 0.0
range_to = 4249.99

values = []
# Minimum bracket (below 4250 -> MSC 4000)
emp_share = msc * 0.05
er_share = msc * 0.095
values.append(f"({range_from}, {range_to}, {emp_share:.2f}, {er_share:.2f}, 2024)")
values.append(f"({range_from}, {rangeTo}, {emp_share:.2f}, {er_share:.2f}, 2025)")

range_from = 4250.0

for i in range(1, 62):
    msc = 4000 + (i * 500)
    if msc > 35000:
        break
    
    range_to = range_from + 499.99
    if msc == 35000:
        range_to = 999999.99 # Maximum bracket

    emp_share = msc * 0.05
    er_share = msc * 0.095
    
    values.append(f"({range_from:.2f}, {range_to:.2f}, {emp_share:.2f}, {er_share:.2f}, 2024)")
    values.append(f"({range_from:.2f}, {range_to:.2f}, {emp_share:.2f}, {er_share:.2f}, 2025)")
    
    range_from = range_to + 0.01

sql += ",\n".join(values) + ";\n"

with open('populate_statutory_tables.sql', 'w') as f:
    f.write(sql)

const fs = require('fs');

let sql = "-- ============================================\n";
sql += "-- 2024, 2025, 2026 SSS CONTRIBUTION TABLE\n";
sql += "-- 2024: Total 14% (4.5% EE, 9.5% ER), Max MSC 30,000\n";
sql += "-- 2025+: Total 15% (5.0% EE, 10.0% ER), Min MSC 5,000, Max MSC 35,000\n";
sql += "-- ============================================\n";
sql += "TRUNCATE TABLE ssstable;\n";
sql += "INSERT INTO ssstable (rangeFrom, rangeTo, employeeShare, employerShare, effectiveYear) VALUES\n";

let values = [];

// === 2024 BRACKETS ===
// 4.5% EE, 9.5% ER. Minimum MSC 4,000; Maximum 30,000.
let rangeFrom24 = 0.00;
let rangeTo24 = 4249.99;
let msc24 = 4000;

values.push(`(${rangeFrom24.toFixed(2)}, ${rangeTo24.toFixed(2)}, ${(msc24 * 0.045).toFixed(2)}, ${(msc24 * 0.095).toFixed(2)}, 2024)`);

rangeFrom24 = 4250.00;
for (let i = 1; i <= 52; i++) {
    msc24 = 4000 + (i * 500);
    if (msc24 > 30000) break;
    
    rangeTo24 = rangeFrom24 + 499.99;
    if (msc24 === 30000) {
        rangeTo24 = 999999.99; // Maximum Bracket
    }
    
    values.push(`(${rangeFrom24.toFixed(2)}, ${rangeTo24.toFixed(2)}, ${(msc24 * 0.045).toFixed(2)}, ${(msc24 * 0.095).toFixed(2)}, 2024)`);
    rangeFrom24 = rangeTo24 + 0.01;
}

// === 2025 & 2026 BRACKETS ===
// 5.0% EE, 10.0% ER. Minimum MSC 5,000; Maximum 35,000.
for (let year of [2025, 2026]) {
    let rangeFrom25 = 0.00;
    let rangeTo25 = 5249.99;
    let msc25 = 5000;
    
    values.push(`(${rangeFrom25.toFixed(2)}, ${rangeTo25.toFixed(2)}, ${(msc25 * 0.05).toFixed(2)}, ${(msc25 * 0.10).toFixed(2)}, ${year})`);
    
    rangeFrom25 = 5250.00;
    for (let i = 1; i <= 60; i++) {
        msc25 = 5000 + (i * 500);
        if (msc25 > 35000) break;
        
        rangeTo25 = rangeFrom25 + 499.99;
        if (msc25 === 35000) {
            rangeTo25 = 999999.99; // Maximum Bracket
        }
        
        values.push(`(${rangeFrom25.toFixed(2)}, ${rangeTo25.toFixed(2)}, ${(msc25 * 0.05).toFixed(2)}, ${(msc25 * 0.10).toFixed(2)}, ${year})`);
        rangeFrom25 = rangeTo25 + 0.01;
    }
}

sql += values.join(",\n") + ";\n\n";

// === TAX BRACKETS ===
sql += "-- ============================================\n";
sql += "-- 2024, 2025, 2026 PURE COMPENSATION BIR TAX TABLE (Monthly, TRAIN Law Phase 2)\n";
sql += "-- ============================================\n";
sql += "TRUNCATE TABLE taxtable;\n";
sql += "INSERT INTO taxtable (compensationFrom, compensationTo, taxRate, additionalTax, effectiveYear) VALUES\n";

let taxValues = [];
for (let year of [2024, 2025, 2026]) {
    taxValues.push(`(0.00, 20833.32, 0.00, 0.00, ${year})`);
    taxValues.push(`(20833.33, 33333.32, 15.00, 0.00, ${year})`);
    taxValues.push(`(33333.33, 66666.66, 20.00, 1875.00, ${year})`);
    taxValues.push(`(66666.67, 166666.66, 25.00, 8541.67, ${year})`);
    taxValues.push(`(166666.67, 666666.66, 30.00, 33541.67, ${year})`);
    taxValues.push(`(666666.67, 99999999.99, 35.00, 183541.67, ${year})`);
}

sql += taxValues.join(",\n") + ";\n";

fs.writeFileSync('populate_statutory_tables.sql', sql);
console.log('Successfully generated populate_statutory_tables.sql with REAL-LIFE brackets!');

package digital8.payroll.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import digital8.payroll.entities.PagibigTable;
import digital8.payroll.entities.PhilhealthTable;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.TaxTable;
import digital8.payroll.repositories.PagibigTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.TaxTableRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PremiumTableService {

    @Autowired private SssTableRepository sssRepo;
    @Autowired private TaxTableRepository taxRepo;
    @Autowired private PhilhealthTableRepository philhealthRepo;
    @Autowired private PagibigTableRepository pagibigRepo;

    // --- SSS ---
    public List<SssTable> getSssTablesByYear(Integer year) {
        return sssRepo.findByEffectiveYearOrderByRangeFromAsc(year);
    }
    public SssTable saveSssTable(SssTable table) {
        return sssRepo.save(table);
    }
    public void saveAllSssTables(List<SssTable> tables) {
        sssRepo.saveAll(tables);
    }
    public void deleteSssTable(Integer id) {
        sssRepo.deleteById(id);
    }
    public void deleteAllSssTables(List<Integer> ids) {
        sssRepo.deleteAllById(ids);
    }

    // --- TAX ---
    public List<TaxTable> getTaxTablesByYearAndFrequency(Integer year, String frequency) {
        return taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year, frequency);
    }
    public List<TaxTable> getTaxTablesByYear(Integer year) {
        // Fallback or generic query if we don't filter by freq in UI
        return taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(year, "MONTHLY"); 
    }
    public TaxTable saveTaxTable(TaxTable table) {
        return taxRepo.save(table);
    }
    public void saveAllTaxTables(List<TaxTable> tables) {
        taxRepo.saveAll(tables);
    }
    public void deleteTaxTable(Integer id) {
        taxRepo.deleteById(id);
    }
    public void deleteAllTaxTables(List<Integer> ids) {
        taxRepo.deleteAllById(ids);
    }

    // --- PHILHEALTH (Flat Rate) ---
    // Since HR wants a single flat rate, we maintain ONE row per year in the DB.
    // 'employeeShare' will hold the percentage (e.g. 2.50)
    public PhilhealthTable getPhilhealthRateByYear(Integer year) {
        List<PhilhealthTable> list = philhealthRepo.findByEffectiveYearOrderByRangeFromAsc(year);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public PhilhealthTable savePhilhealthRate(Integer year, BigDecimal rate) {
        List<PhilhealthTable> list = philhealthRepo.findByEffectiveYearOrderByRangeFromAsc(year);
        PhilhealthTable record;
        if (list != null && !list.isEmpty()) {
            record = list.get(0);
        } else {
            record = new PhilhealthTable();
            record.setEffectiveYear(year);
            // Ignore range since it's a flat rate
            record.setRangeFrom(BigDecimal.ZERO);
            record.setRangeTo(new BigDecimal("9999999"));
        }
        record.setEmployeeShare(rate);
        record.setEmployerShare(rate);
        return philhealthRepo.save(record);
    }

    // --- PAG-IBIG (Flat Rate) ---
    // Single row per year. 'employeeShare' will hold the percentage (e.g. 2.00)
    public PagibigTable getPagibigRateByYear(Integer year) {
        List<PagibigTable> list = pagibigRepo.findByEffectiveYearOrderByRangeFromAsc(year);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public PagibigTable savePagibigRate(Integer year, BigDecimal rate) {
        List<PagibigTable> list = pagibigRepo.findByEffectiveYearOrderByRangeFromAsc(year);
        PagibigTable record;
        if (list != null && !list.isEmpty()) {
            record = list.get(0);
        } else {
            record = new PagibigTable();
            record.setEffectiveYear(year);
            // Ignore range since it's a flat rate
            record.setRangeFrom(BigDecimal.ZERO);
            record.setRangeTo(new BigDecimal("9999999"));
        }
        record.setEmployeeShare(rate);
        record.setEmployerShare(rate);
        return pagibigRepo.save(record);
    }

    // ==================== COPY YEAR ====================

    @Transactional
    public int copySssYear(int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }
        List<SssTable> existing = sssRepo.findByEffectiveYearOrderByRangeFromAsc(targetYear);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("SSS data already exists for " + targetYear + ". Delete it first or choose another year.");
        }
        List<SssTable> source = sssRepo.findByEffectiveYearOrderByRangeFromAsc(sourceYear);
        int created = 0;
        for (SssTable src : source) {
            SssTable row = new SssTable();
            row.setRangeFrom(src.getRangeFrom());
            row.setRangeTo(src.getRangeTo());
            row.setEmployeeShare(src.getEmployeeShare());
            row.setEmployerShare(src.getEmployerShare());
            row.setEffectiveYear(targetYear);
            sssRepo.save(row);
            created++;
        }
        return created;
    }

    @Transactional
    public int copyTaxYear(int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }
        // Check both frequencies
        List<TaxTable> existingSemi = taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(targetYear, "SEMI_MONTHLY");
        List<TaxTable> existingMonthly = taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(targetYear, "MONTHLY");
        if (!existingSemi.isEmpty() || !existingMonthly.isEmpty()) {
            throw new IllegalArgumentException("Tax data already exists for " + targetYear + ". Delete it first or choose another year.");
        }
        List<TaxTable> sourceSemi = taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(sourceYear, "SEMI_MONTHLY");
        List<TaxTable> sourceMonthly = taxRepo.findByEffectiveYearAndPayFrequencyOrderByCompensationFromAsc(sourceYear, "MONTHLY");
        int created = 0;
        for (TaxTable src : sourceSemi) {
            TaxTable row = new TaxTable();
            row.setCompensationFrom(src.getCompensationFrom());
            row.setCompensationTo(src.getCompensationTo());
            row.setAdditionalTax(src.getAdditionalTax());
            row.setTaxRate(src.getTaxRate());
            row.setPayFrequency(src.getPayFrequency());
            row.setEffectiveYear(targetYear);
            taxRepo.save(row);
            created++;
        }
        for (TaxTable src : sourceMonthly) {
            TaxTable row = new TaxTable();
            row.setCompensationFrom(src.getCompensationFrom());
            row.setCompensationTo(src.getCompensationTo());
            row.setAdditionalTax(src.getAdditionalTax());
            row.setTaxRate(src.getTaxRate());
            row.setPayFrequency(src.getPayFrequency());
            row.setEffectiveYear(targetYear);
            taxRepo.save(row);
            created++;
        }
        return created;
    }

    @Transactional
    public int copyPhilhealthYear(int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }
        PhilhealthTable src = getPhilhealthRateByYear(sourceYear);
        if (src == null) {
            throw new IllegalArgumentException("No PhilHealth data found for " + sourceYear + ".");
        }
        PhilhealthTable existing = getPhilhealthRateByYear(targetYear);
        if (existing != null) {
            throw new IllegalArgumentException("PhilHealth data already exists for " + targetYear + ".");
        }
        PhilhealthTable row = new PhilhealthTable();
        row.setRangeFrom(src.getRangeFrom());
        row.setRangeTo(src.getRangeTo());
        row.setEmployeeShare(src.getEmployeeShare());
        row.setEmployerShare(src.getEmployerShare());
        row.setEffectiveYear(targetYear);
        philhealthRepo.save(row);
        return 1;
    }

    @Transactional
    public int copyPagibigYear(int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }
        PagibigTable src = getPagibigRateByYear(sourceYear);
        if (src == null) {
            throw new IllegalArgumentException("No Pag-IBIG data found for " + sourceYear + ".");
        }
        PagibigTable existing = getPagibigRateByYear(targetYear);
        if (existing != null) {
            throw new IllegalArgumentException("Pag-IBIG data already exists for " + targetYear + ".");
        }
        PagibigTable row = new PagibigTable();
        row.setRangeFrom(src.getRangeFrom());
        row.setRangeTo(src.getRangeTo());
        row.setEmployeeShare(src.getEmployeeShare());
        row.setEmployerShare(src.getEmployerShare());
        row.setEffectiveYear(targetYear);
        pagibigRepo.save(row);
        return 1;
    }

    // ==================== IMPORT CSV ====================

    @Transactional
    public int importSssCsv(InputStream csvStream, int effectiveYear) throws IOException {
        int created = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first && line.toLowerCase().contains("range_from")) {
                    first = false;
                    continue;
                }
                first = false;
                String[] cols = parseCsvLine(line);
                if (cols.length < 4) continue;

                SssTable row = new SssTable();
                row.setRangeFrom(new BigDecimal(cols[0].trim()));
                row.setRangeTo(new BigDecimal(cols[1].trim()));
                row.setEmployeeShare(new BigDecimal(cols[2].trim()));
                row.setEmployerShare(new BigDecimal(cols[3].trim()));
                row.setEffectiveYear(effectiveYear);
                sssRepo.save(row);
                created++;
            }
        }
        return created;
    }

    @Transactional
    public int importTaxCsv(InputStream csvStream, int effectiveYear) throws IOException {
        int created = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first && line.toLowerCase().contains("pay_frequency")) {
                    first = false;
                    continue;
                }
                first = false;
                String[] cols = parseCsvLine(line);
                if (cols.length < 5) continue;

                TaxTable row = new TaxTable();
                row.setPayFrequency(cols[0].trim().toUpperCase());
                row.setCompensationFrom(new BigDecimal(cols[1].trim()));
                String compTo = cols[2].trim();
                row.setCompensationTo(compTo.isEmpty() ? null : new BigDecimal(compTo));
                row.setAdditionalTax(new BigDecimal(cols[3].trim()));
                row.setTaxRate(new BigDecimal(cols[4].trim()));
                row.setEffectiveYear(effectiveYear);
                taxRepo.save(row);
                created++;
            }
        }
        return created;
    }

    /** Simple CSV line parser that respects quoted fields (same as HolidayAdminService). */
    private String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(String[]::new);
    }
}

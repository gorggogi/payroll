package digital8.payroll.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digital8.payroll.entities.PagibigTable;
import digital8.payroll.entities.PhilhealthTable;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.TaxTable;
import digital8.payroll.repositories.PagibigTableRepository;
import digital8.payroll.repositories.PhilhealthTableRepository;
import digital8.payroll.repositories.SssTableRepository;
import digital8.payroll.repositories.TaxTableRepository;

import java.math.BigDecimal;
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
}

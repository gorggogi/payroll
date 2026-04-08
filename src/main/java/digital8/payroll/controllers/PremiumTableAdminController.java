package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import digital8.payroll.entities.PagibigTable;
import digital8.payroll.entities.PhilhealthTable;
import digital8.payroll.entities.SssTable;
import digital8.payroll.entities.TaxTable;
import digital8.payroll.services.PremiumTableService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/admin/tables/premium")
public class PremiumTableAdminController {

    @Autowired
    private PremiumTableService premiumTableService;

    @GetMapping
    public String renderPremiumTables(@RequestParam(name = "year", required = false) Integer year,
                                      @RequestParam(name = "tab", required = false, defaultValue = "sss") String tab,
                                      Model model) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        model.addAttribute("currentYear", year);
        model.addAttribute("activeTab", tab);

        // Pre-load all tables for the given year so JS client-side tabbing works
        model.addAttribute("sssList", premiumTableService.getSssTablesByYear(year));
        
        model.addAttribute("taxMonthlyList", premiumTableService.getTaxTablesByYearAndFrequency(year, "MONTHLY"));
        model.addAttribute("taxSemiList", premiumTableService.getTaxTablesByYearAndFrequency(year, "SEMI_MONTHLY"));

        PhilhealthTable ph = premiumTableService.getPhilhealthRateByYear(year);
        model.addAttribute("philhealthRate", ph != null && ph.getEmployeeShare() != null ? ph.getEmployeeShare() : new BigDecimal("2.50"));

        PagibigTable pg = premiumTableService.getPagibigRateByYear(year);
        model.addAttribute("pagibigRate", pg != null && pg.getEmployeeShare() != null ? pg.getEmployeeShare() : new BigDecimal("2.00"));

        return "html/premiumTables";
    }

    // --- SSS ---
    @PostMapping("/sss/save")
    public String saveSss(@ModelAttribute SssTable sss, RedirectAttributes redirectAttributes) {
        premiumTableService.saveSssTable(sss);
        redirectAttributes.addAttribute("year", sss.getEffectiveYear());
        redirectAttributes.addAttribute("tab", "sss");
        return "redirect:/admin/tables/premium";
    }

    @PostMapping("/sss/delete/{id}")
    public String deleteSss(@PathVariable Integer id, @RequestParam("year") Integer year, RedirectAttributes redirectAttributes) {
        premiumTableService.deleteSssTable(id);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "sss");
        return "redirect:/admin/tables/premium";
    }

    @PostMapping("/sss/save-bulk")
    @ResponseBody
    public ResponseEntity<?> saveBulkSss(@RequestBody List<SssTable> tables) {
        premiumTableService.saveAllSssTables(tables);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sss/delete-bulk")
    @ResponseBody
    public ResponseEntity<?> deleteBulkSss(@RequestBody List<Integer> ids) {
        premiumTableService.deleteAllSssTables(ids);
        return ResponseEntity.ok().build();
    }

    // --- TAX ---
    @PostMapping("/tax/save")
    public String saveTax(@ModelAttribute TaxTable tax, RedirectAttributes redirectAttributes) {
        premiumTableService.saveTaxTable(tax);
        redirectAttributes.addAttribute("year", tax.getEffectiveYear());
        redirectAttributes.addAttribute("tab", "tax");
        return "redirect:/admin/tables/premium";
    }

    @PostMapping("/tax/delete/{id}")
    public String deleteTax(@PathVariable Integer id, @RequestParam("year") Integer year, RedirectAttributes redirectAttributes) {
        premiumTableService.deleteTaxTable(id);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "tax");
        return "redirect:/admin/tables/premium";
    }

    @PostMapping("/tax/save-bulk")
    @ResponseBody
    public ResponseEntity<?> saveBulkTax(@RequestBody List<TaxTable> tables) {
        premiumTableService.saveAllTaxTables(tables);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tax/delete-bulk")
    @ResponseBody
    public ResponseEntity<?> deleteBulkTax(@RequestBody List<Integer> ids) {
        premiumTableService.deleteAllTaxTables(ids);
        return ResponseEntity.ok().build();
    }

    // --- PHILHEALTH ---
    @PostMapping("/philhealth/save")
    public String savePhilhealthRate(@RequestParam("effectiveYear") Integer year, 
                                     @RequestParam("rate") BigDecimal rate, 
                                     RedirectAttributes redirectAttributes) {
        premiumTableService.savePhilhealthRate(year, rate);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "philhealth");
        return "redirect:/admin/tables/premium";
    }

    // --- PAG-IBIG ---
    @PostMapping("/pagibig/save")
    public String savePagibigRate(@RequestParam("effectiveYear") Integer year, 
                                  @RequestParam("rate") BigDecimal rate, 
                                  RedirectAttributes redirectAttributes) {
        premiumTableService.savePagibigRate(year, rate);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "pagibig");
        return "redirect:/admin/tables/premium";
    }
}

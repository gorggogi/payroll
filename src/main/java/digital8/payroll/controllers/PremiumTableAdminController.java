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
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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

    // ==================== COPY YEAR ====================

    @PostMapping("/copy-year")
    public String copyYear(@RequestParam Integer sourceYear,
                           @RequestParam Integer targetYear,
                           @RequestParam(required = false, defaultValue = "sss") String tab,
                           RedirectAttributes redirectAttributes) {

        List<String> successes = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // SSS
        try {
            int count = premiumTableService.copySssYear(sourceYear, targetYear);
            successes.add("SSS: copied " + count + " brackets.");
        } catch (IllegalArgumentException e) {
            errors.add("SSS: " + e.getMessage());
        }

        // Tax
        try {
            int count = premiumTableService.copyTaxYear(sourceYear, targetYear);
            successes.add("Tax: copied " + count + " brackets.");
        } catch (IllegalArgumentException e) {
            errors.add("Tax: " + e.getMessage());
        }

        // PhilHealth
        try {
            int count = premiumTableService.copyPhilhealthYear(sourceYear, targetYear);
            successes.add("PhilHealth: copied " + count + " record(s).");
        } catch (IllegalArgumentException e) {
            errors.add("PhilHealth: " + e.getMessage());
        }

        // Pag-IBIG
        try {
            int count = premiumTableService.copyPagibigYear(sourceYear, targetYear);
            successes.add("Pag-IBIG: copied " + count + " record(s).");
        } catch (IllegalArgumentException e) {
            errors.add("Pag-IBIG: " + e.getMessage());
        }

        if (!successes.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", String.join(" ", successes));
        }
        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", String.join(" ", errors));
        }

        redirectAttributes.addAttribute("year", targetYear);
        redirectAttributes.addAttribute("tab", tab);
        return "redirect:/admin/tables/premium";
    }

    // ==================== IMPORT CSV ====================

    @PostMapping("/sss/import")
    public String importSssCsv(@RequestParam("file") MultipartFile file,
                               @RequestParam("effectiveYear") Integer year,
                               RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please upload a CSV file.");
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("tab", "sss");
            return "redirect:/admin/tables/premium";
        }
        try {
            int created = premiumTableService.importSssCsv(file.getInputStream(), year);
            redirectAttributes.addFlashAttribute("successMessage", "Imported " + created + " SSS brackets from CSV.");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "SSS import failed: " + e.getMessage());
        }
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "sss");
        return "redirect:/admin/tables/premium";
    }

    @PostMapping("/tax/import")
    public String importTaxCsv(@RequestParam("file") MultipartFile file,
                               @RequestParam("effectiveYear") Integer year,
                               RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please upload a CSV file.");
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("tab", "tax");
            return "redirect:/admin/tables/premium";
        }
        try {
            int created = premiumTableService.importTaxCsv(file.getInputStream(), year);
            redirectAttributes.addFlashAttribute("successMessage", "Imported " + created + " Tax brackets from CSV.");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tax import failed: " + e.getMessage());
        }
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("tab", "tax");
        return "redirect:/admin/tables/premium";
    }
}

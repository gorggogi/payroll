package digital8.payroll.controllers;

import digital8.payroll.entities.Holiday;
import digital8.payroll.entities.Users;
import digital8.payroll.services.HolidayAdminService;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/admin/holidays")
public class HolidayAdminController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HolidayAdminController.class);

    private final HolidayAdminService holidayAdminService;

    public HolidayAdminController(HolidayAdminService holidayAdminService) {
        this.holidayAdminService = holidayAdminService;
    }

    @GetMapping
    public String page(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String type,
            Authentication authentication,
            Model model) {

        guardAdmin(authentication);

        // Identify and log all unclassified holidays so they can be assigned a type
        List<Holiday> unclassified = holidayAdminService.list(null, "UNCLASSIFIED");
        if (!unclassified.isEmpty()) {
            log.warn("Found {} unclassified holidays that need type mapping:", unclassified.size());
            for (Holiday h : unclassified) {
                log.warn(" - {} ({})", h.getHolidayName(), h.getHolidayDate());
            }
        }

        int currentYear = Year.now().getValue();
        Integer selectedYear = (year != null) ? year : currentYear;

        List<Holiday> holidays = holidayAdminService.list(selectedYear, type);

        model.addAttribute("holidays", holidays);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedType", type);

        model.addAttribute("holidayTypes", List.of(
                Holiday.TYPE_REGULAR,
                Holiday.TYPE_SPECIAL_NON_WORKING,
                Holiday.TYPE_SPECIAL_WORKING));
        model.addAttribute("yearOptions", List.of(currentYear + 1, currentYear, currentYear - 1));

        return "html/holidaysAdmin";
    }

    @PostMapping
    public String create(
            @RequestParam String holidayName,
            @RequestParam LocalDate holidayDate,
            @RequestParam(required = false) String holidayType,
            Authentication authentication,
            RedirectAttributes ra) {

        guardAdmin(authentication);

        try {
            holidayAdminService.create(holidayName, holidayDate, holidayType);
            ra.addFlashAttribute("successMessage", "Holiday created.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/holidays";
    }

    @PostMapping("/{id}/update")
    public String update(
            @PathVariable Integer id,
            @RequestParam String holidayName,
            @RequestParam LocalDate holidayDate,
            @RequestParam(required = false) String holidayType,
            Authentication authentication,
            RedirectAttributes ra) {

        guardAdmin(authentication);

        try {
            holidayAdminService.update(id, holidayName, holidayDate, holidayType);
            ra.addFlashAttribute("successMessage", "Holiday updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/holidays";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, Authentication authentication, RedirectAttributes ra) {
        guardAdmin(authentication);
        try {
            holidayAdminService.delete(id);
            ra.addFlashAttribute("successMessage", "Holiday deleted.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/holidays";
    }

    private void guardAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Users user)
                || user.getRole() == null
                || !"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }

    @PostMapping("/copy-year")
    public String copyYear(
            @RequestParam Integer sourceYear,
            @RequestParam Integer targetYear,
            Authentication authentication,
            RedirectAttributes ra) {

        guardAdmin(authentication);

        try {
            int created = holidayAdminService.copyYear(sourceYear, targetYear);
            ra.addFlashAttribute("successMessage",
                    "Copied " + created + " holidays from " + sourceYear + " to " + targetYear + ".");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/holidays?year=" + targetYear;
    }

    @PostMapping("/import")
    public String importCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes ra) {

        guardAdmin(authentication);

        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Please upload a CSV file.");
            return "redirect:/admin/holidays";
        }

        try {
            int created = holidayAdminService.importCsv(file.getInputStream());
            ra.addFlashAttribute("successMessage", "Imported " + created + " holidays from CSV.");
        } catch (IllegalArgumentException | IOException ex) {
            ra.addFlashAttribute("errorMessage", "Import failed: " + ex.getMessage());
        }

        return "redirect:/admin/holidays";
    }

    @PostMapping("/sync-google")
    public String syncGoogle(
            @RequestParam Integer year,
            @RequestParam(defaultValue = "false") boolean dryRun,
            Authentication authentication,
            RedirectAttributes ra) {

        guardAdmin(authentication);

        try {
            HolidayAdminService.SyncResult result = holidayAdminService.syncFromGoogle(year, dryRun);
            ra.addFlashAttribute("successMessage",
                    "Google sync complete. Created=" + result.created()
                            + ", Updated=" + result.updated()
                            + ", Skipped=" + result.skipped()
                            + ", Unmapped=" + result.unmapped());
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/holidays?year=" + year;
    }
}
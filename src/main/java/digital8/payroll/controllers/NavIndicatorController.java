package digital8.payroll.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import digital8.payroll.entities.Users;
import digital8.payroll.services.LeaveService;

/**
 * API for nav badge indicators. Returns a map of indicator key → show (boolean).
 * Add new keys here when you add new nav indicators (e.g. deductions).
 * Frontend uses data-nav-indicator="&lt;key&gt;" on the span to drive visibility.
 */
@RestController
public class NavIndicatorController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/api/nav-indicators")
    @ResponseBody
    public Map<String, Boolean> getNavIndicators(Authentication authentication) {
        Map<String, Boolean> out = new LinkedHashMap<>();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Users)) {
            out.put("leave", false);
            // out.put("deductions", false);  // add when needed
            return out;
        }

        Users user = (Users) authentication.getPrincipal();

        // Leave: admin = pending count, employee = new responded count
        boolean leave = false;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            leave = leaveService.getPendingCount() > 0;
        } else if (user.getEmployee() != null) {
            long count = leaveService.getNewRespondedCountForEmployee(
                user.getEmployee().getEmployeeId(),
                user.getLastLeaveViewedAt()
            );
            leave = count > 0;
        }
        out.put("leave", leave);

        // Deductions (example for future): out.put("deductions", deductionService.hasUnread(...));

        return out;
    }
}

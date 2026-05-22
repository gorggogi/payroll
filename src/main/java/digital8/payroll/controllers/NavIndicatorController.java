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
import digital8.payroll.services.UndertimeService;

/**
 * API for nav badge indicators. Returns a map of indicator key → show (boolean).
 * Add new keys here when you add new nav indicators (e.g. deductions).
 * Frontend uses data-nav-indicator="&lt;key&gt;" on the span to drive visibility.
 */
@RestController
public class NavIndicatorController {

    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private UndertimeService undertimeService;

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

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Integer employeeId = user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null;

        boolean teamPending = false;
        boolean selfResponded = false;

        if (employeeId != null) {
            selfResponded = (leaveService.getNewRespondedCountForEmployee(
                    employeeId,
                    user.getLastLeaveViewedAt()
            ) + undertimeService.getNewRespondedCountForEmployee(
                    employeeId,
                    user.getLastLeaveViewedAt()
            )) > 0;
        }

        if (isAdmin) {
            teamPending = (leaveService.getPendingCountExcludingEmployee(employeeId) + 
                           undertimeService.getPendingCountExcludingEmployee(employeeId)) > 0;
        }

        // For admins:
        // - "leave" drives the main Leave nav item:
        //      lights up when there are team pending requests OR this admin's own
        //      requests have been approved/rejected.
        // - "leaveSelf" drives the My Leave link (their own approvals/rejections)
        // For employees:
        // - "leave" is their own approvals/rejections
        out.put("leave", isAdmin ? (teamPending || selfResponded) : selfResponded);
        out.put("leaveSelf", selfResponded);

        // Deductions (example for future): out.put("deductions", deductionService.hasUnread(...));

        return out;
    }
}

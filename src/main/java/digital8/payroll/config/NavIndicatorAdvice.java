package digital8.payroll.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import digital8.payroll.entities.Users;
import digital8.payroll.services.LeaveService;

@ControllerAdvice
public class NavIndicatorAdvice {

    @Autowired LeaveService leaveService;

    @ModelAttribute("pendingLeaveCount")
    public long addPendingLeaveCount(Authentication auth){
        if (auth == null || !auth.isAuthenticated()) return 0L;

        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) return 0L;

        if (!(auth.getPrincipal() instanceof Users)) return 0L;
        Users user = (Users) auth.getPrincipal();
        Integer adminEmployeeId = user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null;

        return leaveService.getPendingCountExcludingEmployee(adminEmployeeId);
    }

    @ModelAttribute("respondedLeaveCount")
    public long addRespondedLeaveCount(Authentication auth){    
        if (auth == null || !(auth.getPrincipal() instanceof Users)) return 0L;
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) return 0L;

        Users user = (Users) auth.getPrincipal();

        if (user.getEmployee() == null) return 0L;

        LocalDateTime lastViewed = user.getLastLeaveViewedAt();

        return leaveService.getNewRespondedCountForEmployee(user.getEmployee().getEmployeeId(), lastViewed);

    }

}
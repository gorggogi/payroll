package digital8.payroll.controllers;

import digital8.payroll.entities.Users;
import digital8.payroll.services.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/employee/leave")
    public String employeeLeavePage(Model model, Authentication authentication) {
      
        Users user = (Users) authentication.getPrincipal();
        Integer employeeId = user.getEmployee().getEmployeeId();
        String employeeName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();

        model.addAttribute("employeeName", employeeName);
        model.addAttribute("leaveBalances", leaveService.getEmployeeLeaveBalance(employeeId));
        model.addAttribute("leaveTypes", leaveService.getAllLeaveTypes());
        model.addAttribute("leaveRequests", leaveService.getEmployeeLeaveRequests(employeeId));

        return "html/leaveEmployee";
    }

    @PostMapping("/employee/leave/request")
    public String submitLeaveRequest(
            @RequestParam Integer leaveTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String reason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Users user = (Users) authentication.getPrincipal();
            Integer employeeId = user.getEmployee().getEmployeeId();

            leaveService.submitLeaveRequest(employeeId, leaveTypeId, startDate, endDate, reason);

            redirectAttributes.addFlashAttribute("successMessage", "Leave request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit leave request: " + e.getMessage());
        }

        return "redirect:/employee/leave";
    }

    @GetMapping("/admin/leave")
    public String adminLeavePage(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            Model model, Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        Integer adminId = user.getUserId();
        model.addAttribute("emp_id", adminId);
        String fullName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();
        model.addAttribute("employeeName", fullName);
        model.addAttribute("pendingRequests", leaveService.getPendingLeaveRequests());
        model.addAttribute("pendingCount", leaveService.getPendingCount());
        model.addAttribute("allRequests", leaveService.getAllLeaveRequests(filter, search));
        model.addAttribute("filter", filter);
        model.addAttribute("search", search);
        return "html/leaveAdmin";
    }

    @PostMapping("/admin/leave/approve/{id}")
    public String approveLeaveRequest(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Users user = (Users) authentication.getPrincipal();
            Integer adminId = user.getUserId();

            leaveService.approveLeaveRequest(id, adminId);

            redirectAttributes.addFlashAttribute("successMessage", "Leave request approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve leave request: " + e.getMessage());
        }

        return "redirect:/admin/leave";
    }

    @PostMapping("/admin/leave/reject/{id}")
    public String rejectLeaveRequest(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Users user = (Users) authentication.getPrincipal();
            Integer adminId = user.getUserId();

            leaveService.rejectLeaveRequest(id, adminId);

            redirectAttributes.addFlashAttribute("successMessage", "Leave request rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject leave request: " + e.getMessage());
        }

        return "redirect:/admin/leave";
    }
}

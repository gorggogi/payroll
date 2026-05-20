package digital8.payroll.controllers;

import digital8.payroll.entities.Users;
import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.services.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Controller
public class LeaveController {

    private static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    @Autowired
    private LeaveService leaveService;
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private digital8.payroll.services.EmployeeService employeeService;

    @GetMapping("/employee/leave")
    public String employeeLeavePage(Model model, Authentication authentication) {
      
        Users user = (Users) authentication.getPrincipal();
        Integer employeeId = user.getEmployee().getEmployeeId();
        String employeeName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();

        model.addAttribute("emp_id", employeeId);
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("leaveBalances", leaveService.getEmployeeLeaveBalance(employeeId));
        model.addAttribute("leaveTypes", leaveService.getAllLeaveTypes());
        model.addAttribute("leaveRequests", leaveService.getEmployeeLeaveRequests(employeeId));
        model.addAttribute("activeEmployees", employeeService.filterEmployees(null, null, null, "Active", null, null, null, null, null, null));
        user.setLastLeaveViewedAt(LocalDateTime.now());
        usersRepository.save(user);

        return "html/leaveEmployee";
    }

    @GetMapping("/admin/my-leave")
    public String adminSelfLeavePage(Model model, Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        Integer employeeId = user.getEmployee().getEmployeeId();
        String employeeName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();

        model.addAttribute("emp_id", user.getUserId());
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("leaveBalances", leaveService.getEmployeeLeaveBalance(employeeId));
        model.addAttribute("leaveTypes", leaveService.getAllLeaveTypes());
        model.addAttribute("leaveRequests", leaveService.getEmployeeLeaveRequests(employeeId));
        model.addAttribute("activeEmployees", employeeService.filterEmployees(null, null, null, "Active", null, null, null, null, null, null));
        user.setLastLeaveViewedAt(LocalDateTime.now());
        usersRepository.save(user);

        return "html/leaveAdminSelf";
    }

    @PostMapping("/employee/leave/request")
    public String submitLeaveRequest(
            @RequestParam Integer leaveTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String reason,
            @RequestParam(value = "reliever", required = false) String reliever,
            @RequestParam(value = "attachment", required = false) org.springframework.web.multipart.MultipartFile attachment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {


        try {
            log.info("[LeaveController] submitLeaveRequest called with leaveTypeId={}, startDate={}, endDate={}, reason={}, reliever={}", leaveTypeId, startDate, endDate, reason, reliever);
            Users user = (Users) authentication.getPrincipal();
            Integer employeeId = user.getEmployee().getEmployeeId();

            String attachmentPath = null;
            if (attachment != null && !attachment.isEmpty()) {
                // Always use project root for uploads
                String projectRoot = System.getProperty("user.dir");
                java.nio.file.Path uploadDir = java.nio.file.Paths.get(projectRoot, "uploads", "leave_attachments");
                java.nio.file.Files.createDirectories(uploadDir);
                String fileName = System.currentTimeMillis() + "_" + attachment.getOriginalFilename();
                java.nio.file.Path filePath = uploadDir.resolve(fileName);
                attachment.transferTo(filePath.toFile());
                attachmentPath = "uploads/leave_attachments/" + fileName;
            }

            log.info("[LeaveController] Calling leaveService.submitLeaveRequest with employeeId={}, leaveTypeId={}, startDate={}, endDate={}, reason={}, reliever={}, attachmentPath={}", employeeId, leaveTypeId, startDate, endDate, reason, reliever, attachmentPath);
            leaveService.submitLeaveRequest(employeeId, leaveTypeId, startDate, endDate, reason, reliever, attachmentPath);
            log.info("[LeaveController] leaveService.submitLeaveRequest completed");

            redirectAttributes.addFlashAttribute("successMessage", "Leave request submitted successfully!");
        } catch (Exception e) {
            log.error("[LeaveController] Exception in submitLeaveRequest", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit leave request: " + e.getMessage());
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin ? "redirect:/admin/my-leave" : "redirect:/employee/leave";
    }

    @GetMapping("/admin/leave")
    public String adminLeavePage(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            Model model, Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        Integer adminUserId = user.getUserId();
        Integer adminEmployeeId = user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null;
        model.addAttribute("emp_id", adminUserId);
        String fullName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();
        model.addAttribute("employeeName", fullName);
        model.addAttribute("pendingRequests", leaveService.getPendingLeaveRequestsExcludingEmployee(adminEmployeeId));
        model.addAttribute("pendingCount", leaveService.getPendingCountExcludingEmployee(adminEmployeeId));
        model.addAttribute("allRequests", leaveService.getAllLeaveRequestsExcludingEmployee(adminEmployeeId, filter, search));
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

    @PutMapping("/api/employee/leave/{id}")
    @ResponseBody
    public ResponseEntity<?> updateLeaveRequest(
            @PathVariable Integer id,
            @RequestParam Integer leaveTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String reason,
            Authentication authentication) {
        
        try {
            Users user = (Users) authentication.getPrincipal();
            Integer employeeId = user.getEmployee().getEmployeeId();
            
            leaveService.updateLeaveRequest(id, employeeId, leaveTypeId, startDate, endDate, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave request updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
}

package digital8.payroll.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.services.EmployeeService;
import digital8.payroll.services.UserService;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/employees")
public class AdminEmployeeApiController {

    @Autowired
    private UserService userService;

    // @Autowired
    // private EmployeeService employeeService;

    @PostMapping("/{employeeId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Integer employeeId, @RequestBody Map<String, String> body) {
        String newPassword = body != null ? body.get("newPassword") : null;
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        boolean done = userService.resetPasswordByEmployeeId(employeeId, newPassword);
        if (!done) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // // TODO: Remove after one-time use
    // @PostMapping("/backfill-accounts")
    // public ResponseEntity<?> backfillUserAccounts() {
    //     int created = employeeService.createMissingUserAccounts();
    //     return ResponseEntity.ok(Map.of("message", created + " user accounts created"));
    // }
}

package digital8.payroll.controllers;

import digital8.payroll.entities.PayrollItems;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.PayrollItemsRepository;
import digital8.payroll.services.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollApiController {

    @Autowired
    private PayrollItemsRepository payrollItemsRepository;
    @Autowired
    private PayrollService payrollService;

    @GetMapping("/{empId}")
    public ResponseEntity<List<PayrollItems>> getPayrollByEmpId(
            @PathVariable Integer empId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String month,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Users) {
            Users user = (Users) authentication.getPrincipal();
            boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
            if (!isAdmin && (user.getEmployee() == null || !empId.equals(user.getEmployee().getEmployeeId()))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
            }
        }

        List<PayrollItems> computed = payrollService.computePayroll(empId, period, month);
        if (computed != null && !computed.isEmpty()) {
            return ResponseEntity.ok(computed);
        }
        List<PayrollItems> items = payrollItemsRepository.findByEmployeeId(empId);
        if (items == null || items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(items);
    }
}

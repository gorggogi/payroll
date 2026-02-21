package digital8.payroll.controllers;

import digital8.payroll.entities.PayrollItems;
import digital8.payroll.repositories.PayrollItemsRepository;
import digital8.payroll.services.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class payrollController {
    @Autowired
    private PayrollItemsRepository payrollRepository;

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/{empId}")
    public ResponseEntity<List<PayrollItems>> getPayrollByEmpId(
            @PathVariable Integer empId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String month) {

        List<PayrollItems> computed = payrollService.computePayroll(empId, period, month);
        if (computed != null && !computed.isEmpty()) {
            return ResponseEntity.ok(computed);
        }

        // fallback to stored items
        List<PayrollItems> items = payrollRepository.findByEmployeeId(empId);
        if (items == null || items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(items);
    }
}

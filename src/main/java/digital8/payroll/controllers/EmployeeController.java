package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<Employees>> getAllEmployees(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

             List<Employees> employees = employeeService.filterEmployees(
                    null, null, null, null, null, null, null, null, sortBy, direction
                );
                return ResponseEntity.ok(employees);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Employees>> filterEmployees(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer positionId,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String payType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {

        List<Employees> employees = employeeService.filterEmployees(
            searchQuery,
            departmentId,
            positionId,
            employmentStatus,
            employmentType,
            payType,
            minSalary,
            maxSalary,
            sortBy,
            direction
        );
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employees> getEmployeeById(@PathVariable Integer id) {
        Optional<Employees> emp = employeeService.getEmployeeById(id);
        return emp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employees> updateEmployee(@PathVariable Integer id, @RequestBody Employees updated) {
        Optional<Employees> saved = employeeService.updateEmployee(id, updated);
        return saved.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}
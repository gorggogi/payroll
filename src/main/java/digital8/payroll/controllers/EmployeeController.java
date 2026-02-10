package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping ("api/employees")

public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<Employees>> getAllEmployees(@RequestParam (required = false) String sortBy , @RequestParam (required = false) String direction){

        List<Employees> employees = employeeService.getAllEmployees(sortBy, direction);
        return ResponseEntity.ok(employees);
        
    }
    
}

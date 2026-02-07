package digital8.payroll.controllers;

import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping ("api/employees")

public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<Employees>> getAllEmployees(
        @RequestParam(defaultValue = "lastName") String sortBy,
        @RequestParam(defaultValue = "asc") String direction
    ) {

        Sort sort;

        if (direction.equalsIgnoreCase("desc")){
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        List<Employees> employees = employeeRepository.findAll(sort);

        return ResponseEntity.ok(employees);

    }
    
    
    
    
}

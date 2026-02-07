package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/test")

public class testController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/employee/{employeeNumber}")
    public Employees testFindByEmployeeNumber (@PathVariable String employeeNumber){
        return employeeRepository.findByEmployeeNumber(employeeNumber);
    }
    
    

    
}

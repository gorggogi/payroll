package digital8.payroll.controllers;

import digital8.payroll.entities.Departments;
import digital8.payroll.repositories.DepartmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/departments")
public class DepartmentsController {

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @GetMapping
    public ResponseEntity<List<Departments>> getAll() {
        return ResponseEntity.ok(departmentsRepository.findAll());
    }
}

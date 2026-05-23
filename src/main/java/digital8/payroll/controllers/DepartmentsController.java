package digital8.payroll.controllers;

import digital8.payroll.entities.Departments;
import digital8.payroll.repositories.DepartmentsRepository;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/departments")
public class DepartmentsController {

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<Departments>> getAll() {
        return ResponseEntity.ok(departmentsRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Departments> createDepartment(@RequestBody Departments department) {
        return ResponseEntity.ok(departmentsRepository.save(department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Integer id) {
        if (employeeRepository.existsByDepartment_DepartmentId(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot delete department because there are employees assigned to it.");
        }
        departmentsRepository.deleteById(id);
        return ResponseEntity.ok("Department deleted successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Integer id, @RequestBody Departments updatedDept) {
        return departmentsRepository.findById(id).map(dept -> {
            dept.setDepartmentName(updatedDept.getDepartmentName());
            departmentsRepository.save(dept);
            return ResponseEntity.ok(dept);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

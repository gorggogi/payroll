package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.specifications.EmployeeSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employees> filterEmployees(
        String searchQuery,
        Integer departmentId,
        Integer positionId,
        String employmentStatus,
        String employmentType,
        String payType,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String sortBy,
        String direction) {
    
    System.out.println("========== FILTER EMPLOYEES DEBUG ==========");
    System.out.println("Received sortBy: '" + sortBy + "'");
    System.out.println("Received direction: '" + direction + "'");
    System.out.println("Received employmentStatus: '" + employmentStatus + "'");
    
 
    if (sortBy == null || sortBy.isEmpty()) {
        sortBy = "lastName";
    }
    if (direction == null || direction.isEmpty()) {
        direction = "asc";
    }

    System.out.println("After defaults - sortBy: '" + sortBy + "'");
    System.out.println("After defaults - direction: '" + direction + "'");

    Sort sort;
    if ("desc".equalsIgnoreCase(direction)) {
        sort = Sort.by(Sort.Direction.DESC, sortBy);
        System.out.println("Created DESCENDING sort for: " + sortBy);
    } else {
        sort = Sort.by(Sort.Direction.ASC, sortBy);
        System.out.println("Created ASCENDING sort for: " + sortBy);
    }

    Specification<Employees> spec = EmployeeSpecifications.filterBy(
        searchQuery,
        departmentId,
        positionId,
        employmentStatus,
        employmentType,
        payType,
        minSalary,
        maxSalary
    );

    List<Employees> results = employeeRepository.findAll(spec, sort);
    System.out.println("Query returned " + results.size() + " results");

    if (!results.isEmpty()) {
        System.out.println("First 3 employees (to verify sort):");
        for (int i = 0; i < Math.min(3, results.size()); i++) {
            Employees emp = results.get(i);
            System.out.println("  " + (i+1) + ". " + emp.getLastName() + ", " + emp.getFirstName());
        }
    }
    System.out.println("============================================");
    
    return results;
    }
}



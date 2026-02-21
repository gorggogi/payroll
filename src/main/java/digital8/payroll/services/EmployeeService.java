package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.specifications.EmployeeSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import digital8.payroll.repositories.DepartmentsRepository;
import digital8.payroll.repositories.PositionsRepository;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private PositionsRepository positionsRepository;

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

    public Optional<Employees> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employees> createEmployee(Employees emp) {
        if (emp.getEmail() != null && employeeRepository.existsByEmail(emp.getEmail())) {
            return Optional.empty();
        }
        if (emp.getEmployeeNumber() == null || emp.getEmployeeNumber().isBlank()) {
            emp.setEmployeeNumber(generateEmployeeNumber());
        } else if (employeeRepository.existsByEmployeeNumber(emp.getEmployeeNumber())) {
            return Optional.empty();
        }
        if (emp.getDepartment() != null && emp.getDepartment().getDepartmentId() != null) {
            departmentsRepository.findById(emp.getDepartment().getDepartmentId()).ifPresent(emp::setDepartment);
        }
        if (emp.getPosition() != null && emp.getPosition().getPositionId() != null) {
            positionsRepository.findById(emp.getPosition().getPositionId()).ifPresent(emp::setPosition);
        }
        Employees saved = employeeRepository.save(emp);
        return Optional.of(saved);
    }

    private String generateEmployeeNumber() {
        return employeeRepository.findFirstByOrderByEmployeeIdDesc()
                .map(e -> "EMP" + String.format("%05d", e.getEmployeeId() + 1))
                .orElse("EMP00001");
    }

    public Optional<Employees> updateEmployee(Integer id, Employees updated) {
        Optional<Employees> existingOpt = employeeRepository.findById(id);
        if (!existingOpt.isPresent()) {
            return Optional.empty();
        }
        Employees existing = existingOpt.get();

        // Update basic fields
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getMiddleName() != null) existing.setMiddleName(updated.getMiddleName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getContactNumber() != null) existing.setContactNumber(updated.getContactNumber());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getDateHired() != null) existing.setDateHired(updated.getDateHired());
        if (updated.getEmploymentStatus() != null) existing.setEmploymentStatus(updated.getEmploymentStatus());
        if (updated.getEmploymentType() != null) existing.setEmploymentType(updated.getEmploymentType());
        if (updated.getPayType() != null) existing.setPayType(updated.getPayType());
        if (updated.getBasicSalary() != null) existing.setBasicSalary(updated.getBasicSalary());
        if (updated.getTin() != null) existing.setTin(updated.getTin());
        if (updated.getSssNumber() != null) existing.setSssNumber(updated.getSssNumber());
        if (updated.getPhilhealthNumber() != null) existing.setPhilhealthNumber(updated.getPhilhealthNumber());
        if (updated.getPagibigNumber() != null) existing.setPagibigNumber(updated.getPagibigNumber());
        if (updated.getBank_Account() != null) existing.setBank_Account(updated.getBank_Account());

        // Update department if provided
        if (updated.getDepartment() != null && updated.getDepartment().getDepartmentId() != null) {
            departmentsRepository.findById(updated.getDepartment().getDepartmentId()).ifPresent(existing::setDepartment);
        }

        // Update position if provided
        if (updated.getPosition() != null && updated.getPosition().getPositionId() != null) {
            positionsRepository.findById(updated.getPosition().getPositionId()).ifPresent(existing::setPosition);
        }

        Employees saved = employeeRepository.save(existing);
        return Optional.of(saved);
    }
}



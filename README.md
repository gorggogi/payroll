# payroll

package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // ============================================
    // SIMPLE FILTERS
    // ============================================
    
    public List<Employees> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public List<Employees> getEmployeesByStatus(String status) {
        return employeeRepository.findByEmploymentStatus(status);
    }
    
    public List<Employees> getEmployeesByType(String type) {
        return employeeRepository.findByEmploymentType(type);
    }
    
    public List<Employees> getEmployeesByDepartment(Integer departmentId) {
        return employeeRepository.findByDepartment_DepartmentId(departmentId);
    }
    
    public List<Employees> getEmployeesByPosition(Integer positionId) {
        return employeeRepository.findByPosition_PositionId(positionId);
    }
    
    public List<Employees> getEmployeesByPayType(String payType) {
        return employeeRepository.findByPayType(payType);
    }
    
    // ============================================
    // ADVANCED FILTER
    // ============================================
    
    public List<Employees> filterEmployees(
            Integer departmentId,
            Integer positionId,
            String employmentStatus,
            String employmentType,
            String payType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            LocalDate hiredAfter,
            LocalDate hiredBefore
    ) {
        List<Employees> employees = employeeRepository.findAll();
        
        if (departmentId != null) {
            employees = employees.stream()
                .filter(emp -> emp.getDepartment() != null && 
                              emp.getDepartment().getDepartmentId().equals(departmentId))
                .collect(Collectors.toList());
        }
        
        if (positionId != null) {
            employees = employees.stream()
                .filter(emp -> emp.getPosition() != null && 
                              emp.getPosition().getPositionId().equals(positionId))
                .collect(Collectors.toList());
        }
        
        if (employmentStatus != null && !employmentStatus.isEmpty()) {
            employees = employees.stream()
                .filter(emp -> employmentStatus.equalsIgnoreCase(emp.getEmploymentStatus()))
                .collect(Collectors.toList());
        }
        
        if (employmentType != null && !employmentType.isEmpty()) {
            employees = employees.stream()
                .filter(emp -> employmentType.equalsIgnoreCase(emp.getEmploymentType()))
                .collect(Collectors.toList());
        }
        
        if (payType != null && !payType.isEmpty()) {
            employees = employees.stream()
                .filter(emp -> payType.equalsIgnoreCase(emp.getPayType()))
                .collect(Collectors.toList());
        }
        
        if (minSalary != null) {
            employees = employees.stream()
                .filter(emp -> emp.getBasicSalary() != null && 
                              emp.getBasicSalary().compareTo(minSalary) >= 0)
                .collect(Collectors.toList());
        }
        
        if (maxSalary != null) {
            employees = employees.stream()
                .filter(emp -> emp.getBasicSalary() != null && 
                              emp.getBasicSalary().compareTo(maxSalary) <= 0)
                .collect(Collectors.toList());
        }
        
        if (hiredAfter != null) {
            employees = employees.stream()
                .filter(emp -> emp.getDateHired() != null && 
                              emp.getDateHired().isAfter(hiredAfter))
                .collect(Collectors.toList());
        }
        
        if (hiredBefore != null) {
            employees = employees.stream()
                .filter(emp -> emp.getDateHired() != null && 
                              emp.getDateHired().isBefore(hiredBefore))
                .collect(Collectors.toList());
        }
        
        return employees;
    }
}
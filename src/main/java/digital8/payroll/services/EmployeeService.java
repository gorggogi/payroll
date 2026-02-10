package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employees> getAllEmployees(String sortBy, String direction) {

        if (sortBy == null || sortBy.isEmpty()){
            sortBy = "lastName";
        }

        if (direction == null || direction.isEmpty()){
            direction = "asc";
        }

        Sort sort;

        if (direction.equalsIgnoreCase("desc")){
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        return employeeRepository.findAll(sort);
    }

    public List<Employees> getAllEmployees(){
        return getAllEmployees(null, null);
    }
    
    public List<Employees> getEmployeesByStatus (String status){
        return employeeRepository.findByEmploymentStatus(status);
    }
}

package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.EmployeeDeductions;
import java.util.List;

@Repository
public interface EmployeeDeductionsRepository extends JpaRepository<EmployeeDeductions, Integer> {
    List<EmployeeDeductions> findByEmployeeId(Integer employeeId);
}

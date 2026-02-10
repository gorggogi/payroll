package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Departments;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Positions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, Integer>, JpaSpecificationExecutor<Employees> {

    Employees findByEmployeeNumber(String employeeNumber);
    Employees findByEmail(String email);
    boolean existsByEmployeeNumber(String employeeNumber);
    boolean existsByEmail(String email);

    // Optional: 
    List<Employees> findByDepartment(Departments department);
    List<Employees> findByDepartment_DepartmentId(Integer departmentId);
    List<Employees> findByPosition(Positions position);
    List<Employees> findByPosition_PositionId(Integer positionId);
    List<Employees> findByEmploymentStatus(String employmentStatus);
    List<Employees> findByEmploymentType(String employmentType);
    List<Employees> findByDepartmentAndEmploymentStatus(Departments department, String employmentStatus);
    List<Employees> findByDepartment_DepartmentIdAndEmploymentStatus(Integer departmentId, String employmentStatus);
    List<Employees> findByEmploymentStatusAndPayType(String employmentStatus, String payType);
    List<Employees> findByDateHiredBetween(LocalDate startDate, LocalDate endDate);
    List<Employees> findByPayType(String payType);
    List<Employees> findByBasicSalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);
    List<Employees> findByBasicSalaryGreaterThanEqual(BigDecimal minSalary);
    List<Employees> findByBasicSalaryLessThanEqual(BigDecimal maxSalary);

}
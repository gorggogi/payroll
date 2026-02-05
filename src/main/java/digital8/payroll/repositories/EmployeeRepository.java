package digital8.payroll.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Employees;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, Integer>{

    Employees findByEmployeeNumber(String employeeNumber); // 1.
    Employees findByEmail(String email); // 2.
    boolean existsByEmployeeNumber(String employeeNumber); // 3.
    boolean existsByEmail(String email); // 4.


    List<Employees> findByDepartmentId(Integer departmentId); // 5.

    List<Employees> findByPositionId(Integer positionId); // 6.

    List<Employees> findByEmploymentStatus(String employmentStatus); // 7. 

    List<Employees> findByEmploymentType(String employmentType); // 9.

    List<Employees> findByDepartmentIdAndEmploymentStatus(Integer departmentId, String employmentStatus); // 10.

    List<Employees> findByEmploymentStatusAndPayType(String employmentStatus, String payType); // 11.

 


    
}

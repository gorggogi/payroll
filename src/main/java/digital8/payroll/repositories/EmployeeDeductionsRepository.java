package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.EmployeeDeductions;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeDeductionsRepository extends JpaRepository<EmployeeDeductions, Integer> {
    List<EmployeeDeductions> findByEmployeeId(Integer employeeId);

    /**
     * Returns recurring deductions whose active window overlaps the given period.
     * Use this in payroll computation to avoid loading the full deduction history.
     */
    @Query("SELECT ed FROM EmployeeDeductions ed WHERE ed.employeeId = :empId " +
           "AND ed.isRecurring = true " +
           "AND ed.startDate <= :periodEnd " +
           "AND (ed.endDate IS NULL OR ed.endDate >= :periodStart)")
    List<EmployeeDeductions> findActiveRecurringByEmployee(
        @Param("empId") Integer employeeId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd);
}

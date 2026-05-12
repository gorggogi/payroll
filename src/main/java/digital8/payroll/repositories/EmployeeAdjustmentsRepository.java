package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.EmployeeAdjustments;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeAdjustmentsRepository extends JpaRepository<EmployeeAdjustments, Integer> {
    List<EmployeeAdjustments> findByEmployeeId(Integer employeeId);

    /**
     * Returns recurring adjustments whose active window overlaps the given period.
     * Use this in payroll computation to avoid loading the full adjustment history.
     */
    @Query("SELECT ea FROM EmployeeAdjustments ea WHERE ea.employeeId = :empId " +
           "AND ea.isRecurring = true " +
           "AND ea.startDate <= :periodEnd " +
           "AND (ea.endDate IS NULL OR ea.endDate >= :periodStart)")
    List<EmployeeAdjustments> findActiveRecurringByEmployee(
        @Param("empId") Integer employeeId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd);
}

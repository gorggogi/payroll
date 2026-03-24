package digital8.payroll.repositories;

import digital8.payroll.entities.EmployeeScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeScheduleAssignmentRepository extends JpaRepository<EmployeeScheduleAssignment, Integer> {

    long countByTemplateId(Integer templateId);

    Optional<EmployeeScheduleAssignment> findByEmployeeIdAndScheduleYearAndScheduleMonth(
            Integer employeeId, int scheduleYear, int scheduleMonth);

    void deleteByEmployeeIdAndScheduleYearAndScheduleMonth(Integer employeeId, int scheduleYear, int scheduleMonth);

    @Query("SELECT a.employeeId FROM EmployeeScheduleAssignment a WHERE a.templateId = :tid AND a.scheduleYear = :y AND a.scheduleMonth = :m")
    List<Integer> findEmployeeIdsByTemplateAndMonth(
            @Param("tid") Integer templateId, @Param("y") int scheduleYear, @Param("m") int scheduleMonth);
}

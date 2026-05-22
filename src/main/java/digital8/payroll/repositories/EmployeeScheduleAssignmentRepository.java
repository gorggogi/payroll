package digital8.payroll.repositories;

import digital8.payroll.entities.EmployeeScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EmployeeScheduleAssignmentRepository extends JpaRepository<EmployeeScheduleAssignment, Integer> {

    long countByTemplateId(Integer templateId);

    Optional<EmployeeScheduleAssignment> findByEmployeeIdAndScheduleYearAndScheduleMonth(
            Integer employeeId, int scheduleYear, int scheduleMonth);

    @Query(value = """
            SELECT a.*
            FROM employee_schedule_assignment a
            JOIN weekly_schedule_template wst ON a.template_id = wst.template_id
            WHERE a.employeeId = :eid
              AND (a.schedule_year < :y OR (a.schedule_year = :y AND a.schedule_month <= :m))
              AND wst.indefinite = TRUE
            ORDER BY a.schedule_year DESC, a.schedule_month DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<EmployeeScheduleAssignment> findLatestAssignmentOnOrBefore(
            @Param("eid") Integer employeeId, @Param("y") int scheduleYear, @Param("m") int scheduleMonth);

    @Transactional
    void deleteByEmployeeIdAndScheduleYearAndScheduleMonth(Integer employeeId, int scheduleYear, int scheduleMonth);

    @Transactional
    void deleteByEmployeeId(Integer employeeId);

    @Query("SELECT a.employeeId FROM EmployeeScheduleAssignment a WHERE a.templateId = :tid AND a.scheduleYear = :y AND a.scheduleMonth = :m")
    List<Integer> findEmployeeIdsByTemplateAndMonth(
            @Param("tid") Integer templateId, @Param("y") int scheduleYear, @Param("m") int scheduleMonth);
}

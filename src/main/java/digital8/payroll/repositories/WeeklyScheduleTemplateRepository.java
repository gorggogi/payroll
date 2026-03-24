package digital8.payroll.repositories;

import digital8.payroll.entities.WeeklyScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyScheduleTemplateRepository extends JpaRepository<WeeklyScheduleTemplate, Integer> {

    List<WeeklyScheduleTemplate> findByScheduleYearAndScheduleMonthOrderByTemplateNameAsc(int scheduleYear, int scheduleMonth);
}

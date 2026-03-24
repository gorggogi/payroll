package digital8.payroll.repositories;

import digital8.payroll.entities.WeeklyScheduleTemplateDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyScheduleTemplateDayRepository extends JpaRepository<WeeklyScheduleTemplateDay, Integer> {

    List<WeeklyScheduleTemplateDay> findByTemplateIdOrderByDayOfWeekAsc(Integer templateId);

    Optional<WeeklyScheduleTemplateDay> findByTemplateIdAndDayOfWeek(Integer templateId, int dayOfWeek);

    void deleteByTemplateIdAndDayOfWeek(Integer templateId, int dayOfWeek);
}

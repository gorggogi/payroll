package digital8.payroll.repositories;

import digital8.payroll.entities.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    Optional<Holiday> findByHolidayDate(LocalDate holidayDate);

    boolean existsByHolidayDate(LocalDate holidayDate);

    List<Holiday> findByHolidayDateBetweenOrderByHolidayDateAsc(
            LocalDate startInclusive, LocalDate endInclusive);

    List<Holiday> findAllByOrderByHolidayDateAsc();

    boolean existsByHolidayDateAndHolidayIdNot(
            LocalDate holidayDate, Integer holidayId);
}

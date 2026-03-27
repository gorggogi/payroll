package digital8.payroll.repositories;

import digital8.payroll.entities.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    Optional<Holiday> findByCountryCodeAndHolidayDateAndActiveTrue(String countryCode, LocalDate holidayDate);

    boolean existsByCountryCodeAndHolidayDateAndActiveTrue(String countryCode, LocalDate holidayDate);

    List<Holiday> findByCountryCodeAndActiveTrueAndHolidayDateBetweenOrderByHolidayDateAsc(
            String countryCode, LocalDate startInclusive, LocalDate endInclusive);

    List<Holiday> findByCountryCodeAndHolidayDateBetweenOrderByHolidayDateAsc(
            String countryCode, LocalDate start, LocalDate end);

    List<Holiday> findByCountryCodeAndActiveOrderByHolidayDateAsc(
            String countryCode, boolean active);

    List<Holiday> findByCountryCodeOrderByHolidayDateAsc(String countryCode);

    boolean existsByCountryCodeAndHolidayDateAndActiveTrueAndHolidayIdNot(
            String countryCode, LocalDate holidayDate, Integer holidayId);

    Optional<Holiday> findByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
}

package digital8.payroll.services;

import digital8.payroll.entities.Holiday;
import digital8.payroll.repositories.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HolidayCalendarService {

    private final HolidayRepository holidayRepository;

    public HolidayCalendarService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public List<Holiday> activeHolidaysInRange(String countryCode, LocalDate start, LocalDate end) {
        return holidayRepository.findByCountryCodeAndActiveTrueAndHolidayDateBetweenOrderByHolidayDateAsc(
                countryCode, start, end);
    }

    @Transactional(readOnly = true)
    public Optional<Holiday> findActiveHolidayOn(String countryCode, LocalDate date) {
        return holidayRepository.findByCountryCodeAndHolidayDateAndActiveTrue(countryCode, date);
    }

    @Transactional(readOnly = true)
    public boolean isActiveHoliday(String countryCode, LocalDate date) {
        return holidayRepository.existsByCountryCodeAndHolidayDateAndActiveTrue(countryCode, date);
    }
}
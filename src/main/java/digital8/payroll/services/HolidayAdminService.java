package digital8.payroll.services;

import digital8.payroll.entities.Holiday;
import digital8.payroll.repositories.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class HolidayAdminService {

    private final HolidayRepository holidayRepository;

    public HolidayAdminService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public List<Holiday> list(Integer year, String type) {
        List<Holiday> base;
        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            base = holidayRepository.findByHolidayDateBetweenOrderByHolidayDateAsc(start, end);
        } else {
            base = holidayRepository.findAllByOrderByHolidayDateAsc();
        }

        if (type != null && !type.isBlank()) {
            String t = type.trim().toUpperCase();
            if ("UNCLASSIFIED".equals(t)) {
                base = base.stream()
                        .filter(h -> h.getHolidayType() == null || h.getHolidayType().isBlank())
                        .toList();
            } else {
                base = base.stream()
                        .filter(h -> t.equalsIgnoreCase(h.getHolidayType()))
                        .toList();
            }
        }
        return base;
    }

    @Transactional
    public Holiday create(String holidayName, LocalDate holidayDate, String holidayType) {
        String type = normalizeTypeOrNull(holidayType);

        if (holidayRepository.existsByHolidayDate(holidayDate)) {
            throw new IllegalArgumentException("A holiday already exists on that date.");
        }

        Holiday h = new Holiday();
        h.setHolidayName(holidayName.trim());
        h.setHolidayDate(holidayDate);
        h.setHolidayType(type);
        return holidayRepository.save(h);
    }

    @Transactional
    public Holiday update(Integer id, String holidayName, LocalDate holidayDate, String holidayType) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));

        String type = normalizeTypeOrNull(holidayType);

        if (holidayRepository.existsByHolidayDateAndHolidayIdNot(holidayDate, id)) {
            throw new IllegalArgumentException("Another holiday already exists on that date.");
        }

        h.setHolidayName(holidayName.trim());
        h.setHolidayDate(holidayDate);
        h.setHolidayType(type);

        return holidayRepository.save(h);
    }

    @Transactional
    public void delete(Integer id) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));
        holidayRepository.delete(h);
    }

    private String normalizeType(String holidayType) {
        String type = holidayType == null ? "" : holidayType.trim().toUpperCase();
        if (!Holiday.TYPE_REGULAR.equals(type)
                && !Holiday.TYPE_SPECIAL_NON_WORKING.equals(type)
                && !Holiday.TYPE_SPECIAL_WORKING.equals(type)) {
            throw new IllegalArgumentException("Invalid holiday type.");
        }
        return type;
    }

    private String normalizeTypeOrNull(String holidayType) {
        if (holidayType == null || holidayType.isBlank())
            return null;
        return normalizeType(holidayType);
    }

    @Transactional
    public int copyYear(int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }

        LocalDate sourceStart = LocalDate.of(sourceYear, 1, 1);
        LocalDate sourceEnd = LocalDate.of(sourceYear, 12, 31);

        List<Holiday> sourceRows = holidayRepository
                .findByHolidayDateBetweenOrderByHolidayDateAsc(sourceStart, sourceEnd);

        int created = 0;
        for (Holiday src : sourceRows) {
            LocalDate targetDate = src.getHolidayDate().withYear(targetYear);

            boolean exists = holidayRepository.existsByHolidayDate(targetDate);
            if (exists)
                continue;

            Holiday h = new Holiday();
            h.setHolidayName(src.getHolidayName());
            h.setHolidayDate(targetDate);
            h.setHolidayType(src.getHolidayType());
            holidayRepository.save(h);
            created++;
        }

        return created;
    }

    @Transactional
    public int importCsv(InputStream csvStream) throws IOException {
        int created = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;

                if (first && line.toLowerCase().contains("holiday_name")) {
                    first = false;
                    continue;
                }
                first = false;

                String[] cols = parseCsvLine(line);
                if (cols.length < 3)
                    continue;

                String holidayName = cols[0].trim();
                LocalDate holidayDate = LocalDate.parse(cols[1].trim());
                String holidayType = normalizeTypeOrNull(cols[2].trim());

                if (holidayName.isBlank())
                    continue;
                if (holidayRepository.existsByHolidayDate(holidayDate))
                    continue;

                Holiday h = new Holiday();
                h.setHolidayName(holidayName);
                h.setHolidayDate(holidayDate);
                h.setHolidayType(holidayType);

                holidayRepository.save(h);
                created++;
            }
        }

        return created;
    }

    private String[] parseCsvLine(String line) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(String[]::new);
    }
}

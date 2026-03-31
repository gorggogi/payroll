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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayAdminService {

    private final HolidayRepository holidayRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(HolidayAdminService.class);

    private final String googleBaseUrl;
    private final String googleApiKey;
    private final String googleCalendarId;
    private final String googleTimeZone;

    public HolidayAdminService(
            HolidayRepository holidayRepository,
            @org.springframework.beans.factory.annotation.Value("${google.calendar.api.base-url}") String googleBaseUrl,
            @org.springframework.beans.factory.annotation.Value("${google.calendar.api.key}") String googleApiKey,
            @org.springframework.beans.factory.annotation.Value("${google.calendar.calendar-id}") String googleCalendarId,
            @org.springframework.beans.factory.annotation.Value("${google.calendar.time-zone}") String googleTimeZone) {
        this.holidayRepository = holidayRepository;
        this.googleBaseUrl = googleBaseUrl;
        this.googleApiKey = googleApiKey;
        this.googleCalendarId = googleCalendarId;
        this.googleTimeZone = googleTimeZone;
    }

    @Transactional(readOnly = true)
    public List<Holiday> list(String countryCode, Integer year, String type) {
        String cc = (countryCode == null || countryCode.isBlank()) ? Holiday.COUNTRY_PH
                : countryCode.trim().toUpperCase();

        List<Holiday> base;
        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            base = holidayRepository.findByCountryCodeAndHolidayDateBetweenOrderByHolidayDateAsc(cc, start, end);
        } else {
            base = holidayRepository.findByCountryCodeOrderByHolidayDateAsc(cc);
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
    public Holiday create(String countryCode, String holidayName, LocalDate holidayDate, String holidayType,
            String sourceNote) {
        String cc = normalizeCountry(countryCode);
        String type = normalizeTypeOrNull(holidayType);

        if (holidayRepository.existsByCountryCodeAndHolidayDateAndActiveTrue(cc, holidayDate)) {
            throw new IllegalArgumentException("Active holiday already exists on that date.");
        }

        Holiday h = new Holiday();
        h.setCountryCode(cc);
        h.setHolidayName(holidayName.trim());
        h.setHolidayDate(holidayDate);
        h.setHolidayType(type);
        h.setActive(true);
        h.setSourceNote(normalizeSourceNoteOrNull(sourceNote));
        return holidayRepository.save(h);
    }

    @Transactional
    public Holiday update(Integer id, String holidayName, LocalDate holidayDate, String holidayType,
            String sourceNote) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));

        String cc = h.getCountryCode();
        String type = normalizeTypeOrNull(holidayType);

        if (holidayRepository.existsByCountryCodeAndHolidayDateAndActiveTrueAndHolidayIdNot(cc, holidayDate, id)) {
            throw new IllegalArgumentException("Another holiday already exists on that date.");
        }

        h.setHolidayName(holidayName.trim());
        h.setHolidayDate(holidayDate);
        h.setHolidayType(type);
        h.setSourceNote(normalizeSourceNoteOrNull(sourceNote));

        return holidayRepository.save(h);
    }

    @Transactional
    public void delete(Integer id) {
        Holiday h = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));
        holidayRepository.delete(h);
    }

    private String normalizeCountry(String countryCode) {
        return (countryCode == null || countryCode.isBlank()) ? Holiday.COUNTRY_PH : countryCode.trim().toUpperCase();
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

    private String normalizeSourceNoteOrNull(String sourceNote) {
        if (sourceNote == null)
            return null;
        String trimmed = sourceNote.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public int copyYear(String countryCode, int sourceYear, int targetYear) {
        if (sourceYear == targetYear) {
            throw new IllegalArgumentException("Source year and target year cannot be the same.");
        }

        String cc = normalizeCountry(countryCode);
        LocalDate sourceStart = LocalDate.of(sourceYear, 1, 1);
        LocalDate sourceEnd = LocalDate.of(sourceYear, 12, 31);

        List<Holiday> sourceRows = holidayRepository
                .findByCountryCodeAndHolidayDateBetweenOrderByHolidayDateAsc(cc, sourceStart, sourceEnd);

        int created = 0;
        for (Holiday src : sourceRows) {
            LocalDate targetDate = src.getHolidayDate().withYear(targetYear);

            // Skip if an active holiday already exists for target date
            boolean existsActive = holidayRepository.existsByCountryCodeAndHolidayDateAndActiveTrue(cc, targetDate);
            if (existsActive)
                continue;

            Holiday h = new Holiday();
            h.setCountryCode(cc);
            h.setHolidayName(src.getHolidayName());
            h.setHolidayDate(targetDate);
            h.setHolidayType(src.getHolidayType());
            h.setActive(true);
            h.setSourceNote(src.getSourceNote());
            holidayRepository.save(h);
            created++;
        }

        return created;
    }

    @Transactional
    public int importCsv(String countryCode, InputStream csvStream) throws IOException {
        String cc = normalizeCountry(countryCode);
        int created = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;

                // Skip header row once
                if (first && line.toLowerCase().contains("holiday_name")) {
                    first = false;
                    continue;
                }
                first = false;

                // Expected: holiday_name,holiday_date,holiday_type[,source_note]
                String[] cols = parseCsvLine(line);
                if (cols.length < 3)
                    continue;

                String holidayName = cols[0].trim();
                LocalDate holidayDate = LocalDate.parse(cols[1].trim()); // yyyy-MM-dd
                String holidayType = normalizeTypeOrNull(cols[2].trim());
                String sourceNote = (cols.length >= 4) ? cols[3].trim() : null;

                if (holidayName.isBlank())
                    continue;
                if (holidayRepository.existsByCountryCodeAndHolidayDateAndActiveTrue(cc, holidayDate))
                    continue;

                Holiday h = new Holiday();
                h.setCountryCode(cc);
                h.setHolidayName(holidayName);
                h.setHolidayDate(holidayDate);
                h.setHolidayType(holidayType);
                h.setActive(true);
                h.setSourceNote(sourceNote == null || sourceNote.isBlank() ? null : sourceNote);

                holidayRepository.save(h);
                created++;
            }
        }

        return created;
    }

    public record SyncResult(int created, int updated, int reactivated, int skipped, int unmapped) {
    }

    @Transactional
    public SyncResult syncFromGoogle(String countryCode, int year, boolean dryRun) {
        if (googleApiKey == null || googleApiKey.isBlank()) {
            throw new IllegalArgumentException("Google API key is not configured.");
        }

        String cc = normalizeCountry(countryCode);

        String timeMin = year + "-01-01T00:00:00+08:00";
        String timeMax = year + "-12-31T23:59:59+08:00";

        String encodedCalendarId = URLEncoder.encode(googleCalendarId, StandardCharsets.UTF_8);
        String url = googleBaseUrl + "/calendars/" + encodedCalendarId + "/events"
                + "?key=" + URLEncoder.encode(googleApiKey, StandardCharsets.UTF_8)
                + "&timeMin=" + URLEncoder.encode(timeMin, StandardCharsets.UTF_8)
                + "&timeMax=" + URLEncoder.encode(timeMax, StandardCharsets.UTF_8)
                + "&singleEvents=true"
                + "&orderBy=startTime"
                + "&timeZone=" + URLEncoder.encode(googleTimeZone, StandardCharsets.UTF_8);

        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new IllegalArgumentException("Google API error: HTTP " + resp.statusCode());
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode items = root.path("items");

            int created = 0, updated = 0, reactivated = 0, skipped = 0, unmapped = 0;
            Set<String> unmappedTitles = new HashSet<>();

            for (JsonNode item : items) {
                String summary = item.path("summary").asText("").trim();
                if (summary.isBlank()) {
                    skipped++;
                    continue;
                }

                LocalDate date = extractEventDate(item);
                if (date == null) {
                    skipped++;
                    continue;
                }

                String mappedType = mapGoogleTitleToType(summary);
                if (mappedType == null) {
                    unmapped++;
                    unmappedTitles.add(summary);
                    log.warn("Unmapped Google holiday title for year {}: '{}'", year, summary);
                }

                var existingOpt = holidayRepository.findByCountryCodeAndHolidayDate(cc, date);

                if (existingOpt.isEmpty()) {
                    if (!dryRun) {
                        Holiday h = new Holiday();
                        h.setCountryCode(cc);
                        h.setHolidayName(summary);
                        h.setHolidayDate(date);
                        h.setHolidayType(mappedType);
                        h.setActive(true);
                        h.setSourceNote("Google sync " + LocalDate.now());
                        holidayRepository.save(h);
                    }
                    created++;
                    continue;
                }

                Holiday existing = existingOpt.get();
                boolean wasInactive = !existing.isActive();
                String syncNote = "Google sync " + LocalDate.now();
                boolean changed = false;

                if (!summary.equals(existing.getHolidayName()))
                    changed = true;
                if (!existing.isActive())
                    changed = true;

                // only treat type as changed if mappedType is known and different
                if (mappedType != null && !mappedType.equals(existing.getHolidayType()))
                    changed = true;

                if (!syncNote.equals(existing.getSourceNote()))
                    changed = true;

                if (changed) {
                    if (!dryRun) {
                        existing.setHolidayName(summary);
                        existing.setActive(true);
                        existing.setSourceNote(syncNote);

                        // do not overwrite a manually assigned type with null
                        if (mappedType != null) {
                            existing.setHolidayType(mappedType);
                        }

                        holidayRepository.save(existing);
                    }
                    if (wasInactive)
                        reactivated++;
                    else
                        updated++;
                } else {
                    skipped++;
                }
            }
            if (!unmappedTitles.isEmpty()) {
                log.warn("Google sync year {} unmapped titles: {}", year, unmappedTitles);
            }
            return new SyncResult(created, updated, reactivated, skipped, unmapped);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to sync Google calendar: " + e.getMessage(), e);
        }
    }

    private LocalDate extractEventDate(JsonNode item) {
        String allDay = item.path("start").path("date").asText("");
        if (!allDay.isBlank())
            return LocalDate.parse(allDay);

        String dateTime = item.path("start").path("dateTime").asText("");
        if (!dateTime.isBlank())
            return OffsetDateTime.parse(dateTime).toLocalDate();

        return null;
    }

    /**
     * Map Google holiday titles to your payroll holiday types.
     * Expand this over time to reduce "unmapped" results.
     */
    private String mapGoogleTitleToType(String title) {
        String t = title.toLowerCase();

        if (t.contains("new year")
                || t.contains("maundy thursday")
                || t.contains("good friday")
                || t.contains("araw ng kagitingan")
                || t.contains("labor day")
                || t.contains("independence day")
                || t.contains("national heroes day")
                || t.contains("bonifacio day")
                || t.contains("christmas day")
                || t.contains("rizal day")) {
            return Holiday.TYPE_REGULAR;
        }

        if (t.contains("special working")
                || t.contains("edsa people power")
                || t.contains("people power revolution")) {
            return Holiday.TYPE_SPECIAL_WORKING;
        }

        if (t.contains("chinese new year")
                || t.contains("black saturday")
                || t.contains("ninoy aquino day")
                || t.contains("all saints")
                || t.contains("all souls")
                || t.contains("immaculate conception")
                || t.contains("christmas eve")
                || t.contains("last day of the year")) {
            return Holiday.TYPE_SPECIAL_NON_WORKING;
        }

        return null; // unknown -> report as unmapped
    }

    /**
     * Minimal CSV parser supporting quoted values and escaped quotes ("").
     */
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
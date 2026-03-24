package digital8.payroll.services;

import digital8.payroll.entities.Attendance;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.OvertimeRequest;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.AttendanceRepository;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.OvertimeRequestRepository;
import digital8.payroll.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OvertimeRequestService {

    @Autowired
    private OvertimeRequestRepository overtimeRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UsersRepository usersRepository;

    public List<OvertimeRequest> listForEmployee(Integer employeeId) {
        return overtimeRequestRepository.findByEmployee_EmployeeIdOrderByRequestedAtDesc(employeeId);
    }

    public List<OvertimeRequest> listForEmployeeInMonth(Integer employeeId, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return overtimeRequestRepository.findByEmployee_EmployeeIdAndWorkDateBetween(employeeId, from, to).stream()
                .sorted((a, b) -> {
                    LocalDateTime ar = a.getRequestedAt();
                    LocalDateTime br = b.getRequestedAt();
                    if (ar == null && br == null) return 0;
                    if (ar == null) return 1;
                    if (br == null) return -1;
                    return br.compareTo(ar);
                })
                .toList();
    }

    /**
     * Latest overtime request per work date in the calendar month (by requestedAt).
     */
    public Map<LocalDate, OvertimeRequest> latestRequestByWorkDateForMonth(Integer employeeId, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<OvertimeRequest> list =
                overtimeRequestRepository.findByEmployee_EmployeeIdAndWorkDateBetween(employeeId, from, to);
        Map<LocalDate, OvertimeRequest> map = new HashMap<>();
        for (OvertimeRequest o : list) {
            LocalDate d = o.getWorkDate();
            if (d == null) {
                continue;
            }
            OvertimeRequest cur = map.get(d);
            if (cur == null) {
                map.put(d, o);
                continue;
            }
            LocalDateTime a = o.getRequestedAt();
            LocalDateTime b = cur.getRequestedAt();
            if (a != null && (b == null || a.isAfter(b))) {
                map.put(d, o);
            }
        }
        return map;
    }

    public List<OvertimeRequest> listPendingExcludingEmployee(Integer excludeEmployeeId) {
        List<OvertimeRequest> pending = overtimeRequestRepository.findByStatusOrderByRequestedAtAsc("Pending");
        if (excludeEmployeeId == null) {
            return pending;
        }
        return pending.stream()
                .filter(r -> r.getEmployee() == null
                        || !excludeEmployeeId.equals(r.getEmployee().getEmployeeId()))
                .toList();
    }

    public List<OvertimeRequest> listPendingExcludingEmployeeInMonth(Integer excludeEmployeeId, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return overtimeRequestRepository.findByStatusOrderByRequestedAtAsc("Pending").stream()
                .filter(r -> r.getWorkDate() != null)
                .filter(r -> !r.getWorkDate().isBefore(from) && !r.getWorkDate().isAfter(to))
                .filter(r -> excludeEmployeeId == null
                        || r.getEmployee() == null
                        || !excludeEmployeeId.equals(r.getEmployee().getEmployeeId()))
                .toList();
    }

    public Map<Integer, String> approverNameByRequestId(List<OvertimeRequest> requests) {
        Map<Integer, String> map = new HashMap<>();
        if (requests == null || requests.isEmpty()) {
            return map;
        }
        Set<Integer> userIds = new HashSet<>();
        for (OvertimeRequest r : requests) {
            if (r.getApprovedByUserId() != null) {
                userIds.add(r.getApprovedByUserId());
            }
        }
        if (userIds.isEmpty()) {
            return map;
        }
        Map<Integer, String> userNameById = new HashMap<>();
        for (Integer userId : userIds) {
            Users u = usersRepository.findById(userId).orElse(null);
            if (u == null) {
                continue;
            }
            if (u.getEmployee() != null) {
                String fn = u.getEmployee().getFirstName() != null ? u.getEmployee().getFirstName() : "";
                String ln = u.getEmployee().getLastName() != null ? u.getEmployee().getLastName() : "";
                String full = (fn + " " + ln).trim();
                userNameById.put(userId, full.isEmpty() ? ("User #" + userId) : full);
            } else {
                userNameById.put(userId, u.getEmail() != null ? u.getEmail() : ("User #" + userId));
            }
        }
        for (OvertimeRequest r : requests) {
            if (r.getOvertimeRequestId() == null) {
                continue;
            }
            String name = r.getApprovedByUserId() != null ? userNameById.get(r.getApprovedByUserId()) : null;
            if (name != null) {
                map.put(r.getOvertimeRequestId(), name);
            }
        }
        return map;
    }

    @Transactional
    public OvertimeRequest submit(
            Integer employeeId,
            LocalDate workDate,
            LocalTime overtimeIn,
            LocalTime overtimeOut,
            String reason) {

        Employees emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (workDate == null) {
            throw new RuntimeException("Work date is required.");
        }
        if (overtimeIn == null || overtimeOut == null) {
            throw new RuntimeException("Overtime in and out are required.");
        }
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Reason is required.");
        }
        BigDecimal hours = computeHours(overtimeIn, overtimeOut);
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total hours must be greater than zero.");
        }

        OvertimeRequest r = new OvertimeRequest();
        r.setEmployee(emp);
        r.setWorkDate(workDate);
        r.setOvertimeIn(overtimeIn);
        r.setOvertimeOut(overtimeOut);
        r.setTotalHours(hours);
        r.setReason(reason.trim());
        r.setStatus("Pending");
        r.setRequestedAt(LocalDateTime.now());
        return overtimeRequestRepository.save(r);
    }

    @Transactional
    public void approve(Integer requestId, Integer adminUserId) {
        OvertimeRequest r = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!"Pending".equals(r.getStatus())) {
            throw new RuntimeException("Request is not pending.");
        }
        Users admin = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));
        if (admin.getEmployee() != null
                && r.getEmployee() != null
                && admin.getEmployee().getEmployeeId().equals(r.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot approve your own overtime request.");
        }

        Integer empId = r.getEmployee().getEmployeeId();
        LocalDate d = r.getWorkDate();
        Attendance att = attendanceRepository.findAttendanceOnDate(empId, d)
                .orElseThrow(() -> new RuntimeException(
                        "No daily time record for " + d + ". Add attendance for that date first."));

        att.setOvertime_hours(r.getTotalHours().setScale(2, RoundingMode.HALF_UP));
        attendanceRepository.save(att);

        r.setStatus("Approved");
        r.setApprovedByUserId(adminUserId);
        r.setRespondedAt(LocalDateTime.now());
        overtimeRequestRepository.save(r);
    }

    @Transactional
    public void reject(Integer requestId, Integer adminUserId) {
        OvertimeRequest r = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!"Pending".equals(r.getStatus())) {
            throw new RuntimeException("Request is not pending.");
        }
        Users admin = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));
        if (admin.getEmployee() != null
                && r.getEmployee() != null
                && admin.getEmployee().getEmployeeId().equals(r.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot reject your own overtime request.");
        }

        r.setStatus("Rejected");
        r.setApprovedByUserId(adminUserId);
        r.setRespondedAt(LocalDateTime.now());
        overtimeRequestRepository.save(r);
    }

    private static BigDecimal normalizeHours(BigDecimal totalHours) {
        if (totalHours == null) {
            return BigDecimal.ZERO;
        }
        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal computeHours(LocalTime in, LocalTime out) {
        long minutes = ChronoUnit.MINUTES.between(in, out);
        if (minutes <= 0) {
            minutes += 24L * 60L;
        }
        BigDecimal raw = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return normalizeHours(raw);
    }
}

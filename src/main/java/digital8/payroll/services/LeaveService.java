package digital8.payroll.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.LeaveBalance;
import digital8.payroll.entities.LeaveBalanceId;
import digital8.payroll.entities.LeaveRequests;
import digital8.payroll.entities.LeaveTypes;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.LeaveBalanceRepository;
import digital8.payroll.repositories.LeaveRequestRepository;
import digital8.payroll.repositories.LeaveTypesRepository;
import digital8.payroll.repositories.UsersRepository;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;
    @Autowired
    private LeaveTypesRepository leaveTypesRepository;
    @Autowired
    private EmployeeRepository employeesRepository;
    @Autowired
    private UsersRepository usersRepository;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LeaveService.class);

    public List<LeaveTypes> getAllLeaveTypes(){
        return leaveTypesRepository.findAll();
    }

    public List<LeaveBalance> getEmployeeLeaveBalance(Integer employeeId){
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequests> getEmployeeLeaveRequests(Integer employeeId){
        return leaveRequestRepository.findByEmployee_EmployeeIdOrderByRequestedDateDesc(employeeId);
    }

    public LeaveRequests submitLeaveRequest(Integer employeeId, Integer leaveTypeId, LocalDate startDate, LocalDate endDate, String reason, String reliever, String attachmentPath) {
        log.info("[LeaveService] submitLeaveRequest called with employeeId={}, leaveTypeId={}, startDate={}, endDate={}, reason={}, reliever={}, attachmentPath={}", employeeId, leaveTypeId, startDate, endDate, reason, reliever, attachmentPath);
        Employees employee = employeesRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveTypes leaveType = leaveTypesRepository.findById(leaveTypeId)
            .orElseThrow(() -> new RuntimeException("Leave Type not found"));

        LeaveRequests request = new LeaveRequests();
        request.setEmployee(employee);
        request.setLeaveType(leaveType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason);
        request.setReliever(reliever);
        request.setAttachmentPath(attachmentPath);
        request.setStatus("Pending");
        request.setRequestedDate(LocalDate.now());

        LeaveRequests saved = leaveRequestRepository.save(request);
        log.info("[LeaveService] leaveRequestRepository.save returned: {}", saved != null ? saved.getLeaveRequestId() : null);
        return saved;
    }

    public List<LeaveRequests> getPendingLeaveRequests(){
        return leaveRequestRepository.findByStatusOrderByRequestedDateAsc("Pending");
    }

    public long getPendingCount(){
        return leaveRequestRepository.countByStatus("Pending");
    }

    public List<LeaveRequests> getPendingLeaveRequestsExcludingEmployee(Integer employeeId) {
        if (employeeId == null) {
            return getPendingLeaveRequests();
        }
        return leaveRequestRepository.findByStatusAndEmployee_EmployeeIdNotOrderByRequestedDateAsc("Pending", employeeId);
    }

    public long getPendingCountExcludingEmployee(Integer employeeId) {
        if (employeeId == null) {
            return getPendingCount();
        }
        return leaveRequestRepository.countByStatusAndEmployee_EmployeeIdNot("Pending", employeeId);
    }

    public List<LeaveRequests> getAllLeaveRequests(String filter, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        if (hasSearch) {
            String status = (filter != null && !filter.isEmpty() && !filter.equals("all"))
                    ? filter.substring(0, 1).toUpperCase() + filter.substring(1)
                    : null;
            if (status != null) {
                return leaveRequestRepository.searchByKeywordAndStatusOrderByRequestedDateDesc(search.trim(), status);
            }
            return leaveRequestRepository.searchByKeywordOrderByRequestedDateDesc(search.trim());
        }
        if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
            String status = filter.substring(0, 1).toUpperCase() + filter.substring(1);
            return leaveRequestRepository.findByStatusOrderByRequestedDateDesc(status);
        }
        return leaveRequestRepository.findAllByOrderByRequestedDateDesc();
    }

    public List<LeaveRequests> getAllLeaveRequestsExcludingEmployee(Integer employeeId, String filter, String search) {
        List<LeaveRequests> base = getAllLeaveRequests(filter, search);
        if (employeeId == null) {
            return base;
        }
        return base.stream()
                .filter(req -> req.getEmployee() == null ||
                        !employeeId.equals(req.getEmployee().getEmployeeId()))
                .toList();
    }

    // UPDATED: Now returns a native Java Map that Thymeleaf understands directly
    public Map<String, List<Map<String, Object>>> getCalendarLeaveData(Integer excludeEmployeeId) {
        List<LeaveRequests> requests;
        if (excludeEmployeeId != null) {
            requests = leaveRequestRepository.findAll().stream()
                .filter(req -> req.getEmployee() == null || !excludeEmployeeId.equals(req.getEmployee().getEmployeeId()))
                .toList();
        } else {
            requests = leaveRequestRepository.findAll();
        }

        Map<String, List<Map<String, Object>>> leaveMap = new HashMap<>();

        for (LeaveRequests req : requests) {
            if (!"Approved".equalsIgnoreCase(req.getStatus())) continue;

            LocalDate current = req.getStartDate();
            LocalDate end = req.getEndDate();

            if (current == null || end == null) continue;

            while (!current.isAfter(end)) {
                String dateStr = current.toString(); 

                leaveMap.computeIfAbsent(dateStr, k -> new ArrayList<>());

                Map<String, Object> dto = new HashMap<>();
                dto.put("name", req.getEmployee().getFirstName() + " " + req.getEmployee().getLastName());
                dto.put("empId", "EMP-" + req.getEmployee().getEmployeeId());
                dto.put("type", req.getLeaveType().getLeaveName());
                dto.put("reason", req.getReason());
                dto.put("status", req.getStatus());

                leaveMap.get(dateStr).add(dto);

                current = current.plusDays(1);
            }
        }

        return leaveMap;
    }

    public void approveLeaveRequest(Integer requestId, Integer adminId){
        LeaveRequests request = leaveRequestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

        Users admin = usersRepository.findById(adminId)
        .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getEmployee() != null &&
            admin.getEmployee().getEmployeeId().equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot approve your own leave request.");
        }

        request.setStatus("Approved");
        request.setApproved_by(adminId);
        request.setRespondedAt(LocalDateTime.now());

        leaveRequestRepository.save(request);

        LeaveBalanceId balanceId = new LeaveBalanceId(
            request.getEmployee().getEmployeeId(),
            request.getLeaveType().getLeaveTypeId()
        );

        LeaveBalance balance = leaveBalanceRepository.findById(balanceId)
        .orElseThrow(() -> new RuntimeException("Leave balance not found"));

        BigDecimal currentBalance = balance.getBalance();
        BigDecimal daysRequested = new BigDecimal(request.getTotalDays());
        BigDecimal newBalance = currentBalance.subtract(daysRequested);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new RuntimeException("Insufficient leave balance");
        }

        balance.setBalance(newBalance);
        leaveBalanceRepository.save(balance);
    }

    public void rejectLeaveRequest(Integer requestId, Integer adminId){
        LeaveRequests request = leaveRequestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

        Users admin = usersRepository.findById(adminId)
        .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getEmployee() != null &&
            admin.getEmployee().getEmployeeId().equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot reject your own leave request.");
        }

        request.setStatus("Rejected");
        request.setApproved_by(adminId);
        request.setRespondedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }

    public long getNewRespondedCountForEmployee(Integer employeeId, LocalDateTime lastViewedAt){
        return leaveRequestRepository.countNewRespondedForEmployee(employeeId, lastViewedAt);
    }

    public LeaveRequests updateLeaveRequest(Integer requestId, Integer employeeId, Integer leaveTypeId, LocalDate startDate, LocalDate endDate, String reason, String attachmentPath) {
        LeaveRequests request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!employeeId.equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("Unauthorized: You cannot edit another employee's leave request");
        }

        if (!"Pending".equals(request.getStatus())) {
            throw new RuntimeException("Cannot edit leave request with status: " + request.getStatus());
        }

        LeaveTypes leaveType = leaveTypesRepository.findById(leaveTypeId)
            .orElseThrow(() -> new RuntimeException("Leave Type not found"));
        request.setLeaveType(leaveType);

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason cannot be empty");
        }

        if (reason.trim().length() < 10) {
            throw new RuntimeException("Reason must be at least 10 characters");
        }

        if (reason.trim().length() > 500) {
            throw new RuntimeException("Reason cannot exceed 500 characters");
        }

        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason.trim());
        
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            request.setAttachmentPath(attachmentPath);
        }

        return leaveRequestRepository.save(request);
    }
}
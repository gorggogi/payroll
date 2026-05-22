package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.UndertimeRequest;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.UndertimeRequestRepository;
import digital8.payroll.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UndertimeService {

    @Autowired
    private UndertimeRequestRepository undertimeRequestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UsersRepository usersRepository;

    public UndertimeRequest submitUndertimeRequest(Integer employeeId, LocalDate requestDate,
                                                    BigDecimal totalHours, String reason, String reliever,
                                                    String attachmentPath) {
        Employees employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        UndertimeRequest req = new UndertimeRequest();
        req.setEmployee(employee);
        req.setRequestDate(requestDate);
        req.setTotalHours(totalHours);
        req.setReason(reason);
        req.setReliever(reliever);
        req.setAttachmentPath(attachmentPath);
        req.setStatus("Pending");
        req.setRequestedAt(LocalDateTime.now());

        return undertimeRequestRepository.save(req);
    }

    public List<UndertimeRequest> getEmployeeUndertimeRequests(Integer employeeId) {
        return undertimeRequestRepository.findByEmployee_EmployeeIdOrderByRequestedAtDesc(employeeId);
    }

    public List<UndertimeRequest> getPendingUndertimeRequests() {
        return undertimeRequestRepository.findByStatusOrderByRequestedAtAsc("Pending");
    }

    public long getPendingCount() {
        return undertimeRequestRepository.countByStatus("Pending");
    }

    public List<UndertimeRequest> getPendingUndertimeRequestsExcludingEmployee(Integer employeeId) {
        if (employeeId == null) {
            return getPendingUndertimeRequests();
        }
        return undertimeRequestRepository.findByStatusAndEmployee_EmployeeIdNotOrderByRequestedAtAsc("Pending", employeeId);
    }

    public long getPendingCountExcludingEmployee(Integer employeeId) {
        if (employeeId == null) {
            return getPendingCount();
        }
        return undertimeRequestRepository.countByStatusAndEmployee_EmployeeIdNot("Pending", employeeId);
    }

    public List<UndertimeRequest> getAllUndertimeRequests(String filter, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        if (hasSearch) {
            String status = (filter != null && !filter.isEmpty() && !filter.equals("all"))
                    ? filter.substring(0, 1).toUpperCase() + filter.substring(1)
                    : null;
            if (status != null) {
                return undertimeRequestRepository.searchByKeywordAndStatusOrderByRequestedAtDesc(search.trim(), status);
            }
            return undertimeRequestRepository.searchByKeywordOrderByRequestedAtDesc(search.trim());
        }
        if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
            String status = filter.substring(0, 1).toUpperCase() + filter.substring(1);
            return undertimeRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        }
        return undertimeRequestRepository.findAllByOrderByRequestedAtDesc();
    }

    public List<UndertimeRequest> getAllUndertimeRequestsExcludingEmployee(Integer employeeId, String filter, String search) {
        List<UndertimeRequest> base = getAllUndertimeRequests(filter, search);
        if (employeeId == null) {
            return base;
        }
        return base.stream()
                .filter(req -> req.getEmployee() == null ||
                        !employeeId.equals(req.getEmployee().getEmployeeId()))
                .toList();
    }

    public void approveUndertimeRequest(Integer requestId, Integer adminId) {
        UndertimeRequest request = undertimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Undertime request not found"));

        Users admin = usersRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getEmployee() != null &&
                admin.getEmployee().getEmployeeId().equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot approve your own undertime request.");
        }

        request.setStatus("Approved");
        request.setApprovedByUserId(adminId);
        request.setRespondedAt(LocalDateTime.now());
        undertimeRequestRepository.save(request);
    }

    public void rejectUndertimeRequest(Integer requestId, Integer adminId, String denialReason) {
        UndertimeRequest request = undertimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Undertime request not found"));

        Users admin = usersRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getEmployee() != null &&
                admin.getEmployee().getEmployeeId().equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("You cannot reject your own undertime request.");
        }

        request.setStatus("Rejected");
        request.setApprovedByUserId(adminId);
        request.setDenialReason(denialReason);
        request.setRespondedAt(LocalDateTime.now());
        undertimeRequestRepository.save(request);
    }

    public long getNewRespondedCountForEmployee(Integer employeeId, LocalDateTime lastViewedAt) {
        return undertimeRequestRepository.countNewRespondedForEmployee(employeeId, lastViewedAt);
    }

    public UndertimeRequest updateUndertimeRequest(Integer requestId, Integer employeeId, LocalDate requestDate, BigDecimal totalHours, String reason, String attachmentPath) {
        UndertimeRequest request = undertimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Undertime request not found"));

        if (!employeeId.equals(request.getEmployee().getEmployeeId())) {
            throw new RuntimeException("Unauthorized: You cannot edit another employee's undertime request");
        }

        if (!"Pending".equals(request.getStatus())) {
            throw new RuntimeException("Cannot edit undertime request with status: " + request.getStatus());
        }

        request.setRequestDate(requestDate);
        request.setTotalHours(totalHours);
        request.setReason(reason);
        
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            request.setAttachmentPath(attachmentPath);
        }

        return undertimeRequestRepository.save(request);
    }
}

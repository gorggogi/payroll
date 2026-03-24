package digital8.payroll.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "overtime_request")
public class OvertimeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "overtime_request_id", nullable = false, unique = true)
    private Integer overtimeRequestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employeeId", nullable = false)
    private Employees employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "overtime_in", nullable = false)
    private LocalTime overtimeIn;

    @Column(name = "overtime_out", nullable = false)
    private LocalTime overtimeOut;

    @Column(name = "total_hours", nullable = false)
    private BigDecimal totalHours;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "approved_by_user_id")
    private Integer approvedByUserId;

    public Integer getOvertimeRequestId() {
        return overtimeRequestId;
    }

    public void setOvertimeRequestId(Integer overtimeRequestId) {
        this.overtimeRequestId = overtimeRequestId;
    }

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getOvertimeIn() {
        return overtimeIn;
    }

    public void setOvertimeIn(LocalTime overtimeIn) {
        this.overtimeIn = overtimeIn;
    }

    public LocalTime getOvertimeOut() {
        return overtimeOut;
    }

    public void setOvertimeOut(LocalTime overtimeOut) {
        this.overtimeOut = overtimeOut;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Integer getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Integer approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
}

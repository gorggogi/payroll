package digital8.payroll.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "undertime_requests")
public class UndertimeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "undertime_request_id", nullable = false, unique = true)
    private Integer undertimeRequestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employeeId", nullable = false)
    private Employees employee;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "total_hours", nullable = false)
    private BigDecimal totalHours;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "reliever", nullable = true, length = 255)
    private String reliever;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "approved_by_user_id")
    private Integer approvedByUserId;

    @Column(name = "denial_reason", nullable = true, length = 500)
    private String denialReason;

    @Column(name = "attachment_path", length = 255)
    private String attachmentPath;

    // Getters and setters

    public Integer getUndertimeRequestId() {
        return undertimeRequestId;
    }

    public void setUndertimeRequestId(Integer undertimeRequestId) {
        this.undertimeRequestId = undertimeRequestId;
    }

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
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

    public String getReliever() {
        return reliever;
    }

    public void setReliever(String reliever) {
        this.reliever = reliever;
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

    public String getDenialReason() {
        return denialReason;
    }

    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}

package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table (name="leaveRequests")
public class LeaveRequests{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer leaveRequestId;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "employeeId", nullable = false)
    private Employees employee; 

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "leaveTypeId", nullable = false)
    private LeaveTypes leaveType;

    @Column (nullable = false, unique = false)
    private LocalDate startDate;

    @Column (nullable = false, unique = false)
    private LocalDate endDate;

    @Column (nullable = false, length = 500)
    private String reason;

    @Column (nullable = false, unique = false)
    private String status;

    @Column (nullable = true, unique = false)
    private Integer approved_by;

    @Column (nullable = false, unique = false)
    private LocalDate requestedDate;

    @Transient
    public Integer getTotalDays() {
        if (startDate != null && endDate != null){
            return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }

        return 0;
    }

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
    }
    public Integer getLeaveRequestId() {
        return leaveRequestId;
    }

    public void setLeaveRequestId(Integer leaveRequestId) {
        this.leaveRequestId = leaveRequestId;
    }

    public LeaveTypes getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveTypes leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public Integer getApproved_by() {
        return approved_by;
    }

    public void setApproved_by(Integer approved_by) {
        this.approved_by = approved_by;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

}

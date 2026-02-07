package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table (name="leaveRequests")
public class LeaveRequests{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer leaveRequestId;

    @Column (nullable = false, unique = false)
    private Integer employeeId;

    @Column (nullable = false, unique = false)
    private Integer leaveTypeId;

    @Column (nullable = false, unique = false)
    private LocalDate startDate;

    @Column (nullable = false, unique = false)
    private LocalDate endDate;

    @Column (nullable = false, unique = false)
    private String status;

    @Column (nullable = false, unique = false)
    private Integer approved_by;

    public Integer getLeaveRequestId() {
        return leaveRequestId;
    }

    public void setLeaveRequestId(Integer leaveRequestId) {
        this.leaveRequestId = leaveRequestId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Integer leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
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

}

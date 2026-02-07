package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table (name="leaveBalance")
@IdClass(LeaveBalanceId.class)
public class LeaveBalance{
    @Id
    @Column (nullable = false)
    private Integer employeeId;

    @Id
    @Column (nullable = false)
    private Integer leaveTypeId;

    @Column (nullable = false)
    private BigDecimal balance;

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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}

class LeaveBalanceId implements Serializable{
    private Integer employeeId;
    private Integer leaveTypeId;

    public LeaveBalanceId() {}

    public LeaveBalanceId(Integer employeeId, Integer leaveTypeId) {
        this.employeeId = employeeId;
        this.leaveTypeId = leaveTypeId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveBalanceId that = (LeaveBalanceId) o;
        return employeeId.equals(that.employeeId) && leaveTypeId.equals(that.leaveTypeId);
    }

    @Override
    public int hashCode() {
        return employeeId.hashCode() + leaveTypeId.hashCode();
    }
}

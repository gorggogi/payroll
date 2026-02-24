package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "employeeId", insertable = false, updatable = false)
    private Employees employee;

    @Id
    @Column (nullable = false)
    private Integer leaveTypeId;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "leaveTypeId", insertable = false, updatable = false)
    private LeaveTypes leaveType;

    @Column (nullable = false)
    private BigDecimal balance;

    public BigDecimal getRemainingDays(){
        return balance;
    }

    public BigDecimal getTotalDays(){
        return new BigDecimal("15");
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

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
    }

    public LeaveTypes getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveTypes leaveType) {
        this.leaveType = leaveType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}



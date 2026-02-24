package digital8.payroll.entities;

import java.io.Serializable;

public class LeaveBalanceId implements Serializable{
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

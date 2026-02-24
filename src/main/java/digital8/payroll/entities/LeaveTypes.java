package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table (name = "leaveType")
public class LeaveTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column (nullable = false, unique = true)
    private Integer leaveTypeId;

    @Column (nullable = false, unique = false)
    private String leaveName;

    @Column (nullable = false, unique = false)
    private Boolean withPay;

    public Boolean getWithPay() {
        return withPay;
    }

    public void setWithPay(Boolean withPay) {
        this.withPay = withPay;
    }

    public Integer getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Integer leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }
   
    
}

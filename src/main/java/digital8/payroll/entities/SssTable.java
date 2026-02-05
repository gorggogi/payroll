package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table (name="sssTable")
public class SssTable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer sssId;

    @Column (nullable = false, unique = false)
    private BigDecimal rangeFrom;

    @Column (nullable = false, unique = false)
    private BigDecimal rangeTo;

    @Column (nullable = false, unique = false)
    private BigDecimal employeeShare;

    @Column (nullable = false, unique = false)
    private BigDecimal employerShare;

    @Column (nullable = false, unique = false)
    private Integer effectiveYear;

    public Integer getSssId() {
        return sssId;
    }

    public void setSssId(Integer sssId) {
        this.sssId = sssId;
    }

    public BigDecimal getRangeFrom() {
        return rangeFrom;
    }

    public void setRangeFrom(BigDecimal rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public BigDecimal getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(BigDecimal rangeTo) {
        this.rangeTo = rangeTo;
    }

    public BigDecimal getEmployeeShare() {
        return employeeShare;
    }

    public void setEmployeeShare(BigDecimal employeeShare) {
        this.employeeShare = employeeShare;
    }

    public BigDecimal getEmployerShare() {
        return employerShare;
    }

    public void setEmployerShare(BigDecimal employerShare) {
        this.employerShare = employerShare;
    }

    public Integer getEffectiveYear() {
        return effectiveYear;
    }

    public void setEffectiveYear(Integer effectiveYear) {
        this.effectiveYear = effectiveYear;
    }

}

package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table (name="payroll")
public class Payroll{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer payrollId;

    @Column (nullable = false, unique = false)
    private LocalDate payPeriodStart;

    @Column (nullable = false, unique = false)
    private LocalDate payPeriodEnd;

    @Column (nullable = false, unique = false)
    private String payrollType;

    @Column (nullable = false, unique = false)
    private String status;

    @Column (nullable = false, unique = false)
    private LocalDate dateProcessed;

    @Column (nullable = false, unique = false)
    private LocalDate dateReleased;

    public Integer getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(Integer payrollId) {
        this.payrollId = payrollId;
    }

    public LocalDate getPayPeriodStart() {
        return payPeriodStart;
    }

    public void setPayPeriodStart(LocalDate payPeriodStart) {
        this.payPeriodStart = payPeriodStart;
    }

    public LocalDate getPayPeriodEnd() {
        return payPeriodEnd;
    }

    public void setPayPeriodEnd(LocalDate payPeriodEnd) {
        this.payPeriodEnd = payPeriodEnd;
    }

    public String getPayrollType() {
        return payrollType;
    }

    public void setPayrollType(String payrollType) {
        this.payrollType = payrollType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDateProcessed() {
        return dateProcessed;
    }

    public void setDateProcessed(LocalDate dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public LocalDate getDateReleased() {
        return dateReleased;
    }

    public void setDateReleased(LocalDate dateReleased) {
        this.dateReleased = dateReleased;
    }

}

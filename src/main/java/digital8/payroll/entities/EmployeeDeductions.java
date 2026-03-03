package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table (name="employeedeductions")
public class EmployeeDeductions{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer employeeDeductionId;

    @Column (nullable = false, unique = false)
    private Integer employeeId; // foreign key

    @Column (nullable = false, unique = false)
    private Integer deductionId; // foreign key

    @Column (nullable = false, unique = false)
    private BigDecimal amount;

    @Column (nullable = false, unique = false)
    private Boolean isRecurring;

    @Column (nullable = false, unique = false)
    private LocalDate startDate;

    @Column (nullable = false, unique = false)
    private LocalDate endDate;

    public Integer getEmployeeDeductionId() {
        return employeeDeductionId;
    }

    public void setEmployeeDeductionId(Integer employeeDeductionId) {
        this.employeeDeductionId = employeeDeductionId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getDeductionId() {
        return deductionId;
    }

    public void setDeductionId(Integer deductionId) {
        this.deductionId = deductionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
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

}

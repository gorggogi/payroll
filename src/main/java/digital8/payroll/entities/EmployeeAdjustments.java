package digital8.payroll.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Employee assignment — mirrors the `EmployeeDeductions` entity. */
@Entity
@Table(name = "employeeadjustments")
public class EmployeeAdjustments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Integer employeeAdjustmentId;

    @Column(nullable = true)
    private Integer employeeId;       // FK → employees

    @Column(nullable = true)
    private Integer adjustmentId;     // FK → adjustments (type catalog)

    @Column(nullable = true)
    private BigDecimal amount;

    @Column(nullable = true)
    private Boolean isRecurring;

    @Column(nullable = true)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    /** "SEMI_1", "SEMI_2", or "BOTH" — mirrors deductionCutoff */
    @Column(nullable = false)
    private String applyOnCutoff = "BOTH";

    public Integer getEmployeeAdjustmentId() { return employeeAdjustmentId; }
    public void setEmployeeAdjustmentId(Integer employeeAdjustmentId) { this.employeeAdjustmentId = employeeAdjustmentId; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public Integer getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(Integer adjustmentId) { this.adjustmentId = adjustmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getApplyOnCutoff() { return applyOnCutoff; }
    public void setApplyOnCutoff(String applyOnCutoff) { this.applyOnCutoff = applyOnCutoff; }
}

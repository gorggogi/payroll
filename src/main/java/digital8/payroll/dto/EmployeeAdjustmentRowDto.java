package digital8.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Row DTO for the adjustments table — mirrors EmployeeDeductionRowDto. */
public class EmployeeAdjustmentRowDto {
    private Integer employeeAdjustmentId;
    private Integer employeeId;
    private String  employeeName;
    private Integer adjustmentId;
    private String  adjustmentName;
    private String  adjustmentType;   // "Earnings" or "Deduction"
    private BigDecimal amount;
    private Boolean recurring;
    private LocalDate startDate;
    private LocalDate endDate;
    private String  applyOnCutoff;    // "SEMI_1", "SEMI_2", "BOTH"

    public Integer getEmployeeAdjustmentId() { return employeeAdjustmentId; }
    public void setEmployeeAdjustmentId(Integer id) { this.employeeAdjustmentId = id; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Integer getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(Integer adjustmentId) { this.adjustmentId = adjustmentId; }

    public String getAdjustmentName() { return adjustmentName; }
    public void setAdjustmentName(String adjustmentName) { this.adjustmentName = adjustmentName; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getRecurring() { return recurring; }
    public void setRecurring(Boolean recurring) { this.recurring = recurring; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getApplyOnCutoff() { return applyOnCutoff; }
    public void setApplyOnCutoff(String applyOnCutoff) { this.applyOnCutoff = applyOnCutoff; }
}

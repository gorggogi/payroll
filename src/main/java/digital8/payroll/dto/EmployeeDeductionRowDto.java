package digital8.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeDeductionRowDto {
    private Integer employeeDeductionId;
    private Integer employeeId;
    private String employeeName;
    private Integer deductionId;
    private String deductionName;
    private String deductionType;
    private BigDecimal amount;
    private Boolean recurring;
    private LocalDate startDate;
    private LocalDate endDate;

    public Integer getEmployeeDeductionId() { return employeeDeductionId; }
    public void setEmployeeDeductionId(Integer employeeDeductionId) { this.employeeDeductionId = employeeDeductionId; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Integer getDeductionId() { return deductionId; }
    public void setDeductionId(Integer deductionId) { this.deductionId = deductionId; }
    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }
    public String getDeductionType() { return deductionType; }
    public void setDeductionType(String deductionType) { this.deductionType = deductionType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Boolean getRecurring() { return recurring; }
    public void setRecurring(Boolean recurring) { this.recurring = recurring; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    private String deductionCutoff;
    public String getDeductionCutoff() { return deductionCutoff; }
    public void setDeductionCutoff(String deductionCutoff) { this.deductionCutoff = deductionCutoff; }
}

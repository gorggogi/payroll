package digital8.payroll.dto;

import java.math.BigDecimal;

public class DeductionBreakdownItem {
    private String deductionName;
    private BigDecimal amount;

    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

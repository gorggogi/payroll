package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table (name="payrollItems")
public class PayrollItems{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer payrollItemId;

    @Column (nullable = false, unique = false)
    private Integer payrollId;

    @Column (nullable = false, unique = false)
    private Integer employeeId;

    @Column (nullable = false, unique = false)
    private BigDecimal basicPay;

    @Column (nullable = false, unique = false)
    private BigDecimal overtimePay;

    @Column (nullable = false, unique = false)
    private BigDecimal holidayPay;

    @Column (nullable = false, unique = false)
    private BigDecimal allowances;

    @Column (nullable = false, unique = false)
    private BigDecimal grossPay;

    @Column (nullable = false, unique = false)
    private BigDecimal sss;

    @Column (nullable = false, unique = false)
    private BigDecimal philhealth;

    @Column (nullable = false, unique = false)
    private BigDecimal pagibig;

    @Column (nullable = false, unique = false)
    private BigDecimal tax;

    @Column (nullable = false, unique = false)
    private BigDecimal otherDeductions;

    @Column (nullable = false, unique = false)
    private BigDecimal totalDeductions;

    @Column (nullable = false, unique = false)
    private BigDecimal netPay;

    // --- New fields for redesigned payroll computation ---

    @Column (nullable = true)
    private BigDecimal dailyRate;

    @Column (nullable = true)
    private BigDecimal hourlyRate;

    @Column (nullable = true)
    private BigDecimal perMinuteRate;

    @Column (nullable = true)
    private BigDecimal totalWorkedHours;

    @Column (nullable = true)
    private BigDecimal totalOtHours;

    @Column (nullable = true)
    private Integer lateUndertimeMinutes;

    @Column (nullable = true)
    private BigDecimal lateUndertimeDeduction;

    @Column (nullable = true)
    private BigDecimal cashAdvance;

    @Column (nullable = true)
    private BigDecimal adjustmentEarnings;

    @Column (nullable = true)
    private BigDecimal adjustmentDeductions;

    @Column (nullable = true)
    private BigDecimal totalEarnings;

    @Column (nullable = true)
    private BigDecimal serviceFee;

    @Column (nullable = true)
    private BigDecimal semiMonthlyContributions;

    @Column (nullable = true)
    private String employmentType;

    public Integer getPayrollItemId() {
        return payrollItemId;
    }

    public void setPayrollItemId(Integer payrollItemId) {
        this.payrollItemId = payrollItemId;
    }

    public Integer getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(Integer payrollId) {
        this.payrollId = payrollId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getBasicPay() {
        return basicPay;
    }

    public void setBasicPay(BigDecimal basicPay) {
        this.basicPay = basicPay;
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = overtimePay;
    }

    public BigDecimal getHolidayPay() {
        return holidayPay;
    }

    public void setHolidayPay(BigDecimal holidayPay) {
        this.holidayPay = holidayPay;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }

    public BigDecimal getSss() {
        return sss;
    }

    public void setSss(BigDecimal sss) {
        this.sss = sss;
    }

    public BigDecimal getPhilhealth() {
        return philhealth;
    }

    public void setPhilhealth(BigDecimal philhealth) {
        this.philhealth = philhealth;
    }

    public BigDecimal getPagibig() {
        return pagibig;
    }

    public void setPagibig(BigDecimal pagibig) {
        this.pagibig = pagibig;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    // --- Getters/Setters for new fields ---

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getPerMinuteRate() { return perMinuteRate; }
    public void setPerMinuteRate(BigDecimal perMinuteRate) { this.perMinuteRate = perMinuteRate; }

    public BigDecimal getTotalWorkedHours() { return totalWorkedHours; }
    public void setTotalWorkedHours(BigDecimal totalWorkedHours) { this.totalWorkedHours = totalWorkedHours; }

    public BigDecimal getTotalOtHours() { return totalOtHours; }
    public void setTotalOtHours(BigDecimal totalOtHours) { this.totalOtHours = totalOtHours; }

    public Integer getLateUndertimeMinutes() { return lateUndertimeMinutes; }
    public void setLateUndertimeMinutes(Integer lateUndertimeMinutes) { this.lateUndertimeMinutes = lateUndertimeMinutes; }

    public BigDecimal getLateUndertimeDeduction() { return lateUndertimeDeduction; }
    public void setLateUndertimeDeduction(BigDecimal lateUndertimeDeduction) { this.lateUndertimeDeduction = lateUndertimeDeduction; }

    public BigDecimal getCashAdvance() { return cashAdvance; }
    public void setCashAdvance(BigDecimal cashAdvance) { this.cashAdvance = cashAdvance; }

    public BigDecimal getAdjustmentEarnings() { return adjustmentEarnings; }
    public void setAdjustmentEarnings(BigDecimal adjustmentEarnings) { this.adjustmentEarnings = adjustmentEarnings; }

    public BigDecimal getAdjustmentDeductions() { return adjustmentDeductions; }
    public void setAdjustmentDeductions(BigDecimal adjustmentDeductions) { this.adjustmentDeductions = adjustmentDeductions; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public BigDecimal getSemiMonthlyContributions() { return semiMonthlyContributions; }
    public void setSemiMonthlyContributions(BigDecimal semiMonthlyContributions) { this.semiMonthlyContributions = semiMonthlyContributions; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

}

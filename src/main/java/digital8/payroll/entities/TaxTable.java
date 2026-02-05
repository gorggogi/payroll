package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table (name="taxTable")
public class TaxTable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer taxId;

    @Column (nullable = false, unique = false)
    private BigDecimal compensationFrom;

    @Column (nullable = false, unique = false)
    private BigDecimal compensationTo;

    @Column (nullable = false, unique = false)
    private BigDecimal taxRate;

    @Column (nullable = false, unique = false)
    private BigDecimal additionalTax;

    @Column (nullable = false, unique = false)
    private Integer effectiveYear;

    public Integer getTaxId() {
        return taxId;
    }

    public void setTaxId(Integer taxId) {
        this.taxId = taxId;
    }

    public BigDecimal getCompensationFrom() {
        return compensationFrom;
    }

    public void setCompensationFrom(BigDecimal compensationFrom) {
        this.compensationFrom = compensationFrom;
    }

    public BigDecimal getCompensationTo() {
        return compensationTo;
    }

    public void setCompensationTo(BigDecimal compensationTo) {
        this.compensationTo = compensationTo;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getAdditionalTax() {
        return additionalTax;
    }

    public void setAdditionalTax(BigDecimal additionalTax) {
        this.additionalTax = additionalTax;
    }

    public Integer getEffectiveYear() {
        return effectiveYear;
    }

    public void setEffectiveYear(Integer effectiveYear) {
        this.effectiveYear = effectiveYear;
    }

}

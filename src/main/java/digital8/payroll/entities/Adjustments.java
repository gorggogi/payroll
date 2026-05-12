package digital8.payroll.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

/** Type catalog — mirrors the `deductions` entity. */
@Entity
@Table(name = "adjustments")
public class Adjustments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Integer adjustmentId;

    @Column(nullable = true)
    private String adjustmentName;

    /** "Earnings" or "Deduction" */
    @Column(nullable = true)
    private String adjustmentType;

    public Integer getAdjustmentId() { return adjustmentId; }
    public void setAdjustmentId(Integer adjustmentId) { this.adjustmentId = adjustmentId; }

    public String getAdjustmentName() { return adjustmentName; }
    public void setAdjustmentName(String adjustmentName) { this.adjustmentName = adjustmentName; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
}

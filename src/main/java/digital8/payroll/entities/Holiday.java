package digital8.payroll.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "holiday")
public class Holiday {


    public static final String TYPE_REGULAR = "REGULAR";
    public static final String TYPE_SPECIAL_NON_WORKING = "SPECIAL_NON_WORKING";
    public static final String TYPE_SPECIAL_WORKING = "SPECIAL_WORKING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id", nullable = false)
    private Integer holidayId;



    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", nullable = true, length = 32)
    private String holidayType;





    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getHolidayId() { 
    return holidayId; 
    }

    public void setHolidayId(Integer holidayId) { 
    this.holidayId = holidayId; 
    }



    public String getHolidayName() { 
    return holidayName; 
    }

    public void setHolidayName(String holidayName) { 
    this.holidayName = holidayName; 
    }

    public LocalDate getHolidayDate() { 
    return holidayDate; 
    }

    public void setHolidayDate(LocalDate holidayDate) { 
    this.holidayDate = holidayDate; 
    }

    public String getHolidayType() { 
    return holidayType; 
    }

    public void setHolidayType(String holidayType) { 
    this.holidayType = holidayType; 
    }





    public LocalDateTime getCreatedAt() { 
    return createdAt; 
    }

    public void setCreatedAt(LocalDateTime createdAt) { 
    this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
    return updatedAt; 
    }

    public void setUpdatedAt(LocalDateTime updatedAt) { 
    this.updatedAt = updatedAt; 
    }
}
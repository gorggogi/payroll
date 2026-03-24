package digital8.payroll.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weekly_schedule_template")
public class WeeklyScheduleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", nullable = false)
    private Integer templateId;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "schedule_year", nullable = false)
    private int scheduleYear;

    @Column(name = "schedule_month", nullable = false)
    private int scheduleMonth;

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getScheduleYear() {
        return scheduleYear;
    }

    public void setScheduleYear(int scheduleYear) {
        this.scheduleYear = scheduleYear;
    }

    public int getScheduleMonth() {
        return scheduleMonth;
    }

    public void setScheduleMonth(int scheduleMonth) {
        this.scheduleMonth = scheduleMonth;
    }
}

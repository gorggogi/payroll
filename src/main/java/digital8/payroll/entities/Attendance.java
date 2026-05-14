package digital8.payroll.entities;

import digital8.payroll.HourFormatUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table (name="attendance")
public class Attendance{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (nullable = false, unique = true)
    private Integer attendanceId;

    @Column (nullable = false, unique = false)
    private Integer employeeId; // foreign key

    @Column (nullable = false, unique = false)
    private  LocalDate attendance_date;

    @Column (nullable = false, unique = false)
    private LocalTime time_in;

    @Column (nullable = false, unique = false)
    private LocalTime time_out;

    @Column (nullable = false, unique = false)
    private BigDecimal work_hours;

    @Column (nullable = false, unique = false)
    private Integer late_minutes;

    @Column (nullable = false, unique = false)
    private BigDecimal overtime_hours;

    @Column (nullable = false, unique = false)
    private Integer undertime_minutes;

    @Column (nullable = false, unique = false)
    private String status;

    public Integer getUndertime_minutes() {
        return undertime_minutes;
    }

    public void setUndertime_minutes(Integer undertime_minutes) {
        this.undertime_minutes = undertime_minutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Integer attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAttendance_date() {
        return attendance_date;
    }

    public void setAttendance_date(LocalDate attendance_date) {
        this.attendance_date = attendance_date;
    }

    public LocalTime getTime_in() {
        return time_in;
    }

    public void setTime_in(LocalTime time_in) {
        this.time_in = time_in;
    }

    public LocalTime getTime_out() {
        return time_out;
    }

    public void setTime_out(LocalTime time_out) {
        this.time_out = time_out;
    }

    public BigDecimal getWork_hours() {
        return work_hours;
    }

    public void setWork_hours(BigDecimal work_hours) {
        this.work_hours = work_hours;
    }

    public Integer getLate_minutes() {
        return late_minutes;
    }

    public void setLate_minutes(Integer late_minutes) {
        this.late_minutes = late_minutes;
    }

    public BigDecimal getOvertime_hours() {
        return overtime_hours;
    }

    public void setOvertime_hours(BigDecimal overtime_hours) {
        this.overtime_hours = overtime_hours;
    }

    public String getWorkHoursDisplay() {
        return HourFormatUtils.formatHours(work_hours);
    }

    public String getOvertimeHoursDisplay() {
        return HourFormatUtils.formatHours(overtime_hours);
    }

    public String getTotalHoursWithOvertimeDisplay() {
        BigDecimal work = work_hours != null ? work_hours : BigDecimal.ZERO;
        BigDecimal overtime = overtime_hours != null ? overtime_hours : BigDecimal.ZERO;
        return HourFormatUtils.formatHours(work.add(overtime));
    }

}
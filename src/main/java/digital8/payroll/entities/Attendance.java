package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
    private Integer employeeId;

    @Column (nullable = false, unique = false)
    private  LocalDate attendance_date;

    @Column (nullable = false, unique = false)
    private LocalTime time_in;

    @Column (nullable = false, unique = false)
    private LocalTime time_out;

    @Column (nullable = false, unique = false)
    private Integer work_hours;

    @Column (nullable = false, unique = false)
    private Integer late_minutes;

    @Column (nullable = false, unique = false)
    private Integer overtime_hours;

    @Column (nullable = false, unique = false)
    private Integer undertime_minutes;

    @Column (nullable = false, unique = false)
    private String status;

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

    public Integer getWork_hours() {
        return work_hours;
    }

    public void setWork_hours(Integer work_hours) {
        this.work_hours = work_hours;
    }

    public Integer getLate_minutes() {
        return late_minutes;
    }

    public void setLate_minutes(Integer late_minutes) {
        this.late_minutes = late_minutes;
    }

    public Integer getOvertime_hours() {
        return overtime_hours;
    }

    public void setOvertime_hours(Integer overtime_hours) {
        this.overtime_hours = overtime_hours;
    }

 

}
package digital8.payroll.dto;

import digital8.payroll.entities.WeeklyScheduleTemplateDay;

import java.time.LocalTime;

/** One row for Sun–Sat grid (display order); dayOfWeek is Java 1=Mon..7=Sun. */
public class WeeklyScheduleRowDto {

    private final int dayOfWeek;
    private final String dayLabel;
    private boolean restDay;
    private LocalTime timeIn;
    private LocalTime timeOut;

    public WeeklyScheduleRowDto(int dayOfWeek, String dayLabel) {
        this.dayOfWeek = dayOfWeek;
        this.dayLabel = dayLabel;
    }

    public static WeeklyScheduleRowDto fromTemplateDay(int dayOfWeek, String dayLabel, WeeklyScheduleTemplateDay slot) {
        WeeklyScheduleRowDto row = new WeeklyScheduleRowDto(dayOfWeek, dayLabel);
        if (slot != null) {
            row.setRestDay(slot.isRestDay());
            row.setTimeIn(slot.getTimeIn());
            row.setTimeOut(slot.getTimeOut());
        }
        return row;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public boolean isRestDay() {
        return restDay;
    }

    public void setRestDay(boolean restDay) {
        this.restDay = restDay;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalTime timeIn) {
        this.timeIn = timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }
}

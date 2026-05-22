package digital8.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class HourFormatUtils {
    private HourFormatUtils() {
    }

    public static String formatHours(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return "0:00";
        }
        long totalMinutes = hours.multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        long wholeHours = totalMinutes / 60;
        long remainingMinutes = Math.abs(totalMinutes % 60);
        return wholeHours + ":" + String.format(Locale.ENGLISH, "%02d", remainingMinutes);
    }

    public static String formatHoursWhole(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return "0";
        }
        return hours.setScale(0, RoundingMode.DOWN).toPlainString();
    }
}

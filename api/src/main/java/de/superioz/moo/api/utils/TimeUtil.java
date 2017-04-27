package de.superioz.moo.api.utils;

import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.util.Validation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {

    /**
     * Get the dateFormat from given timestamp in milliseconds and the pattern
     *
     * @param stamp   The timestamp (System.currentTimeMillis())
     * @param pattern The pattern (e.g.: 'dd.MM.yyyy HH:mm:ss.SSS')
     * @return The format as string
     */
    public static String getFormat(long stamp, String pattern) {
        return new SimpleDateFormat(pattern).format(new Date(stamp));
    }

    public static String getFormat(long stamp) {
        return getFormat(stamp, "dd.MM.yyyy HH:mm:ss.SSS");
    }

    /**
     * Gets the duration and timeunit from string (like 1d or 5h)
     *
     * @param str The string
     * @return The pair with both values
     */
    public static Pair<Integer, TimeUnit> getTime(String str) {
        if(!Validation.TIME.matches(str)) return null;
        String timeUnitStr = str.substring(str.length() - 1, str.length());
        int duration = Integer.parseInt(str.substring(0, str.length() - 1));

        TimeUnit unit = null;
        switch(timeUnitStr) {
            case "d":
                unit = TimeUnit.DAYS;
                break;
            case "h":
                unit = TimeUnit.HOURS;
                break;
            case "m":
                unit = TimeUnit.MINUTES;
                break;
            case "s":
                unit = TimeUnit.SECONDS;
                break;
        }
        return new Pair<>(duration, unit);
    }

}

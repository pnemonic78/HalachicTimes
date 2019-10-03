package com.github.times;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ZmanimHelper {
    private ZmanimHelper() {
    }

    private static SimpleDateFormat dateFormat;

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS zzz}"
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    public static String formatDateTime(Date time) {
        SimpleDateFormat dateFormat = ZmanimHelper.dateFormat;
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz", Locale.US);
            ZmanimHelper.dateFormat = dateFormat;
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    public static String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }
}

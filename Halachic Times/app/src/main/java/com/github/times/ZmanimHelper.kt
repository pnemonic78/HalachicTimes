package com.github.times

import com.github.times.ZmanimItem.Companion.NEVER
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ZmanimHelper {
    private val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz", Locale.US)
    }

    /**
     * Format the date and time with seconds.
     *
     * The pattern is "`yyyy-MM-dd HH:mm:ss.SSS zzz`"
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    fun formatDateTime(time: Date): String {
        return dateFormat.format(time)
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    fun formatDateTime(time: Long): String {
        if (time == NEVER) return "never"
        return formatDateTime(Date(time))
    }
}
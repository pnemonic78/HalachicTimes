/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.times

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.github.times.databinding.TimesItemBinding
import com.github.times.preference.ZmanimPreferences
import com.github.util.LocaleUtils.getDefaultLocale
import com.github.util.LocaleUtils.isLocaleRTL
import com.github.util.TimeUtils.roundUp
import com.github.widget.ArrayAdapter
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.text.Format
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Adapter for halachic times list.
 *
 *
 * See also Wikipedia article on [Zmanim](http://en.wikipedia.org/wiki/Zmanim).
 *
 * @author Moshe Waisberg
 */
open class ZmanimAdapter<VH : ZmanViewHolder> @JvmOverloads constructor(
    protected val context: Context,
    protected val settings: ZmanimPreferences,
    protected val listener: OnZmanItemClickListener? = null
) : ArrayAdapter<ZmanimItem, VH>(R.layout.times_item) {

    /**
     * The calendar for the relevant day that the adapter is populated.
     */
    var calendar: ComplexZmanimCalendar = ComplexZmanimCalendar().apply {
        setShaahZmanisType(settings.hourType)
        isUseElevation = settings.isUseElevation
    }

    private val now = System.currentTimeMillis()
    /** Whether to use Israel holiday scheme or not. */
    var inIsrael = false
    private val summaries: Boolean = settings.isSummaries
    private val showElapsed: Boolean = settings.isPast
    private val timeFormat: Format
    private val timeFormatSeasonalHour: Format
    private val timeFormatGranularity: Long
    private val comparator: Comparator<ZmanimItem?> by lazy { ZmanimComparator() }

    protected val hebrewDateFormatter: HebrewDateFormatter by lazy {
        HebrewDateFormatter().apply {
            isHebrewFormat = true
            isUseFinalFormLetters = settings.isYearFinalForm
        }
    }

    private val monthNames: Array<String> by lazy { context.resources.getStringArray(R.array.hebrew_months) }
    private val monthDayYear: String by lazy { context.getString(com.github.lib.R.string.month_day_year) }
    private val omerFormat: String by lazy { context.getString(R.string.omer_format) }
    private val emphasisScale: Float = settings.emphasisScale

    /**
     * The candles data.
     */
    var candles = CandleData()

    init {
        val time24 = DateFormat.is24HourFormat(context)
        val locale = getDefaultLocale(context)
        if (settings.isSeconds) {
            val pattern = if (time24) {
                context.getString(com.github.lib.R.string.twenty_four_hour_time_format)
            } else {
                context.getString(com.github.lib.R.string.twelve_hour_time_format)
            }
            timeFormat = SimpleDateFormat(pattern, locale)
            timeFormatGranularity = DateUtils.SECOND_IN_MILLIS
            timeFormatSeasonalHour = SimpleDateFormat(context.getString(R.string.hour_format_seconds), locale)
        } else {
            timeFormat = DateFormat.getTimeFormat(context)
            timeFormatGranularity = DateUtils.MINUTE_IN_MILLIS
            timeFormatSeasonalHour = SimpleDateFormat(context.getString(R.string.hour_format), locale)
        }
    }

    /**
     * Compare two time items.
     */
    protected class ZmanimComparator : Comparator<ZmanimItem?> {
        override fun compare(o1: ZmanimItem?, o2: ZmanimItem?): Int {
            if (o1 === o2) return 0
            if (o1 == null) return -1
            if (o2 == null) return +1
            return o1.compareTo(o2)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createArrayViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
        fieldId: Int
    ): VH {
        val binding = TimesItemBinding.inflate(inflater, parent, false)
        return ZmanItemViewHolder(binding, summaries, emphasisScale, listener) as VH
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId    the title label id.
     * @param summaryId  the summary label id.
     * @param time       the time in milliseconds.
     * @param jewishDate the Jewish date.
     * @param remote     hide elapsed times for remote view?
     */
    fun add(
        @StringRes titleId: Int,
        @StringRes summaryId: Int,
        time: Long?,
        jewishDate: JewishDate?,
        remote: Boolean = false
    ) = add(
        titleId,
        if (summaryId == 0) null else context.getText(summaryId),
        time ?: ZmanimItem.NEVER,
        jewishDate,
        remote
    )

    /**
     * Adds the item to the array for a valid date.
     *
     * @param titleId    the row layout id.
     * @param summary    the summary label.
     * @param time       the time in milliseconds.
     * @param jewishDate the Jewish date.
     * @param remote     hide elapsed times for remote view?
     * @param hour       format as hour?
     */
    fun add(
        @StringRes titleId: Int,
        summary: CharSequence?,
        time: Long?,
        jewishDate: JewishDate?,
        remote: Boolean,
        hour: Boolean = (titleId == R.string.hour)
    ) {
        if ((time == null) || (time == ZmanimItem.NEVER)) {
            return
        }
        val now = now
        val title: CharSequence = context.getString(titleId)

        ZmanimItem(titleId, title, time).apply {
            this.summary = summary
            this.jewishDate = jewishDate
            this.isEmphasis = settings.isEmphasis(titleId)
            this.timeLabel = if (hour) {
                timeFormatSeasonalHour.format(roundUp(time, DateUtils.SECOND_IN_MILLIS))
            } else {
                timeFormat.format(roundUp(time, timeFormatGranularity))
            }
            this.isElapsed = if (remote) (time < now) else !(showElapsed || hour) && (time < now)
            add(this)
        }
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId   the title label id.
     * @param summaryId the summary label id.
     * @param time      the time in milliseconds.
     */
    fun addHour(
        @StringRes titleId: Int,
        @StringRes summaryId: Int,
        time: Long,
        remote: Boolean = false
    ) = add(
        titleId,
        if (summaryId == SUMMARY_NONE) null else context.getText(summaryId),
        time + DateUtils.DAY_IN_MILLIS,
        null,
        remote,
        true
    )

    /**
     * Sort the times from oldest to newest.
     */
    fun sort() {
        sort(comparator)
    }

    /**
     * Format the Hebrew date.
     *
     * @param context    the context.
     * @param jewishDate the date.
     * @return the formatted date.
     */
    fun formatDate(context: Context, jewishDate: JewishDate): CharSequence {
        val jewishYear = jewishDate.jewishYear
        var jewishMonth = jewishDate.jewishMonth
        if (jewishMonth == JewishDate.ADAR && jewishDate.isJewishLeapYear) {
            jewishMonth = ADAR_I // return "Adar I", not just "Adar".
        }
        val jewishDay = jewishDate.jewishDayOfMonth

        val yearStr: String
        val monthStr = monthNames[jewishMonth - 1]
        val dayStr: String
        val dayPadded: String
        if (isLocaleRTL(context)) {
            val formatter = hebrewDateFormatter
            yearStr = formatter.formatHebrewNumber(jewishYear)
            dayStr = formatter.formatHebrewNumber(jewishDay)
            dayPadded = dayStr
        } else {
            yearStr = jewishYear.toString()
            dayStr = jewishDay.toString()
            dayPadded = if (jewishDay < 10) "0$dayStr" else dayStr
        }
        return monthDayYear.replace(YEAR_VAR, yearStr)
            .replace(MONTH_VAR, monthStr)
            .replace(DAY_VAR, dayStr)
            .replace(DAY_PAD_VAR, dayPadded)
    }

    /**
     * Get the Jewish calendar.
     *
     * @return the calendar - `null` if date is invalid.
     */
    val jewishCalendar: JewishCalendar?
        get() {
            val gcal = calendar.calendar
            if (gcal[Calendar.ERA] < GregorianCalendar.AD) {
                // Avoid future "IllegalArgumentException".
                return null
            }
            return JewishCalendar(gcal).apply {
                this.inIsrael = this@ZmanimAdapter.inIsrael
            }
        }

    /**
     * Format the number of omer days.
     *
     * @param context the context.
     * @param days    the number of days.
     * @return the formatted count.
     */
    fun formatOmer(context: Context, days: Int): CharSequence? {
        if (days <= 0) {
            return null
        }
        var suffix = settings.omerSuffix ?: ZmanimPreferences.Values.OMER_L
        if (suffix.isNullOrEmpty()) {
            return null
        }
        if (ZmanimPreferences.Values.OMER_B == suffix) {
            suffix = context.getString(R.string.omer_b)
        } else if (ZmanimPreferences.Values.OMER_L == suffix) {
            suffix = context.getString(R.string.omer_l)
        }
        val format: String
        if (days == 33) {
            format = context.getString(R.string.omer_33)
            return String.format(format, suffix)
        }
        format = omerFormat
        val dayStr: String = if (isLocaleRTL(context)) {
            val formatter = hebrewDateFormatter
            formatter.formatHebrewNumber(days)
        } else {
            days.toString()
        }
        return String.format(format, dayStr, suffix)
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position) ?: return position.toLong()
        return item.titleId.toLong()
    }

    fun getItemById(id: Int): ZmanimItem? {
        val count = itemCount
        for (i in 0 until count) {
            val item = getItem(i) ?: continue
            if (item.titleId == id) {
                return item
            }
        }
        return null
    }

    /**
     * Get the number of candles for tonight.
     *
     * @return the candles count.
     */
    val candlesCount: Int
        get() = candles.countTomorrow

    /**
     * Get the number of candles for last night.
     *
     * @return the candles count.
     */
    val candlesTodayCount: Int
        get() = candles.countToday

    /**
     * Get the special day.
     *
     * @return the holiday.
     */
    val holidayToday: Int
        get() = candles.holidayToday

    /**
     * Get tomorrow's special day.
     *
     * @return the holiday.
     */
    val holidayTomorrow: Int
        get() = candles.holidayTomorrow

    /**
     * Get the omer.
     *
     * @return the omer.
     */
    val dayOfOmerToday: Int
        get() = candles.omerToday

    /**
     * Get tomorrow's omer.
     *
     * @return the omer.
     */
    val dayOfOmerTomorrow: Int
        get() = candles.omerTomorrow

    companion object {
        /**
         * No summary.
         */
        const val SUMMARY_NONE = 0

        /**
         * The day of the month as a decimal number (range 01 to 31).
         */
        private const val DAY_PAD_VAR = "%d"

        /**
         * The day of the month as a decimal number (range 1 to 31).
         */
        private const val DAY_VAR = "%-e"

        /**
         * The full month name according to the current locale.
         */
        private const val MONTH_VAR = "%B"

        /**
         * The year as a decimal number including the century.
         */
        private const val YEAR_VAR = "%Y"

        /**
         * Value of the month field indicating Adar I, the leap (intercalary or embolismic) thirteenth (Undecimber) numeric
         * month of the year added in Jewish [leap year][JewishDate.isJewishLeapYear]). The leap years are years 3, 6, 8, 11,
         * 14, 17 and 19 of a 19 year cycle. With the year starting at [JewishDate.TISHREI], it would actually be the 7th month
         * of the year.
         */
        protected const val ADAR_I = JewishDate.ADAR_II + 1
    }
}
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
package net.sf.times;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.preference.ZmanimPreferences;
import net.sf.util.LocaleUtils;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

/**
 * Adapter for halachic times list.
 * <p/>
 * See also Wikipedia article on <a
 * href="http://en.wikipedia.org/wiki/Zmanim">Zmanim</a>.
 *
 * @author Moshe Waisberg
 */
public class ZmanimAdapter extends ArrayAdapter<ZmanimItem> {

    protected static final int CANDLES_MASK = ZmanimPopulater.CANDLES_MASK;
    protected static final int HOLIDAY_MASK = ZmanimPopulater.HOLIDAY_MASK;

    /** No summary. */
    protected static final int SUMMARY_NONE = 0;

    /** The day of the month as a decimal number (range 01 to 31). */
    private static final String DAY_PAD_VAR = "%d";
    /** The day of the month as a decimal number (range 1 to 31). */
    private static final String DAY_VAR = "%-e";
    /** The full month name according to the current locale. */
    private static final String MONTH_VAR = "%B";
    /** The year as a decimal number including the century. */
    private static final String YEAR_VAR = "%Y";

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    /**
     * Value of the month field indicating Adar I, the leap (intercalary or embolismic) thirteenth (Undecimber) numeric
     * month of the year added in Jewish {@link JewishDate#isJewishLeapYear() leap year}). The leap years are years 3, 6, 8, 11,
     * 14, 17 and 19 of a 19 year cycle. With the year starting at {@link JewishDate#TISHREI}, it would actually be the 7th month
     * of the year.
     */
    protected static final int ADAR_I = JewishDate.ADAR_II + 1;

    protected final LayoutInflater inflater;
    protected final ZmanimPreferences settings;
    protected ComplexZmanimCalendar calendar;
    protected boolean inIsrael;
    protected long now = System.currentTimeMillis();
    protected boolean summaries;
    protected boolean showElapsed;
    private Format timeFormat;
    private Format timeFormatSeasonalHour;
    private Comparator<ZmanimItem> comparator;
    private HebrewDateFormatter hebrewDateFormatter;
    private String[] monthNames;
    private String monthDayYear;
    private String omerFormat;
    private float emphasisScale;

    /**
     * Time row item.
     */
    protected static class ZmanimItem implements Comparable<ZmanimItem> {

        /** The title id. */
        public final int titleId;
        /** The summary. */
        public CharSequence summary;
        /** The time. */
        public long time = NEVER;
        /** The time label. */
        public CharSequence timeLabel;
        /** Has the time elapsed? */
        public boolean elapsed;
        /** Emphasize? */
        public boolean emphasis;

        /** Creates a new row item. */
        public ZmanimItem(int titleId) {
            this.titleId = titleId;
        }

        @Override
        public int compareTo(ZmanimItem that) {
            long t1 = this.time;
            long t2 = that.time;
            if (t1 != t2)
                return (t1 < t2) ? -1 : +1;
            return this.titleId - that.titleId;
        }

        /**
         * Is the item empty?
         *
         * @return {@code true} if empty.
         */
        public boolean isEmpty() {
            return elapsed || (time == NEVER) || (timeLabel == null);
        }
    }

    /**
     * Compare two time items.
     */
    protected static class ZmanimComparator implements Comparator<ZmanimItem> {
        @Override
        public int compare(ZmanimItem lhs, ZmanimItem rhs) {
            return lhs.compareTo(rhs);
        }
    }

    /**
     * Creates a new adapter.
     *
     * @param context
     *         the context.
     * @param settings
     *         the application settings.
     */
    public ZmanimAdapter(Context context, ZmanimPreferences settings) {
        super(context, R.layout.times_item);
        this.inflater = LayoutInflater.from(context);
        this.calendar = new ComplexZmanimCalendar();
        calendar.setShaahZmanisType(settings.getHourType());
        this.settings = settings;
        this.summaries = settings.isSummaries();
        this.showElapsed = settings.isPast();
        this.emphasisScale = settings.getEmphasisScale();

        boolean time24 = DateFormat.is24HourFormat(context);
        String patternSeasonalHour;

        if (settings.isSeconds()) {
            String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
            this.timeFormat = new SimpleDateFormat(pattern, Locale.getDefault());

            patternSeasonalHour = context.getString(R.string.hour_format_seconds);
        } else {
            this.timeFormat = DateFormat.getTimeFormat(context);

            patternSeasonalHour = context.getString(R.string.hour_format);
        }
        this.timeFormatSeasonalHour = new SimpleDateFormat(patternSeasonalHour, Locale.getDefault());
    }

    /**
     * Get the calendar.
     *
     * @return the calendar.
     */
    public ComplexZmanimCalendar getCalendar() {
        return calendar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = getViewHolder(position, convertView);
        if (holder == null) {
            holder = createViewHolder(position, convertView, parent);
        }
        bindViewHolder(position, holder);
        return holder.itemView;
    }

    /**
     * Get the view holder.
     *
     * @param position
     *         the row index.
     * @param convertView
     *         the view.
     * @return the view holder.
     */
    @Nullable
    protected ViewHolder getViewHolder(int position, View convertView) {
        return (convertView != null) ? (ViewHolder) convertView.getTag() : null;
    }

    /**
     * Create a view holder.
     *
     * @param position
     *         the row index.
     * @param convertView
     *         the view.
     * @param parent
     *         the parent view.
     * @return the item view.
     */
    protected ViewHolder createViewHolder(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.times_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Bind the item to the view.
     *
     * @param position
     *         the row index.
     * @param holder
     *         the view holder.
     * @return the item view.
     */
    protected void bindViewHolder(int position, ViewHolder holder) {
        holder.itemView.setTag(holder);
        holder.bind(getItem(position), summaries, emphasisScale);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time.
     */
    public void add(int titleId, int summaryId, Long time) {
        add(titleId, summaryId, time, false);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time.
     * @param remote
     *         hide elapsed times for remote view?
     */
    public void add(int titleId, int summaryId, Long time, boolean remote) {
        add(titleId, summaryId, time == null ? NEVER : time, remote);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time in milliseconds.
     */
    public void add(int titleId, int summaryId, long time) {
        add(titleId, summaryId, time, false);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time in milliseconds.
     * @param remote
     *         hide elapsed times for remote view?
     */
    public void add(int titleId, int summaryId, long time, boolean remote) {
        add(titleId, (summaryId == 0) ? null : getContext().getText(summaryId), time, remote);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summary
     *         the summary label.
     * @param time
     *         the time
     * @param remote
     *         hide elapsed times for remote view?
     */
    public void add(int titleId, CharSequence summary, Long time, boolean remote) {
        add(titleId, summary, time == null ? NEVER : time, remote);
    }

    /**
     * Adds the item to the array for a valid date.
     *
     * @param titleId
     *         the row layout id.
     * @param summary
     *         the summary label.
     * @param time
     *         the time in milliseconds.
     * @param remote
     *         hide elapsed times for remote view?
     */
    public void add(int titleId, CharSequence summary, long time, boolean remote) {
        add(titleId, summary, time, remote, titleId == R.string.hour);
    }

    /**
     * Adds the item to the array for a valid date.
     *
     * @param titleId
     *         the row layout id.
     * @param summary
     *         the summary label.
     * @param time
     *         the time in milliseconds.
     * @param remote
     *         hide elapsed times for remote view?
     * @param hour
     *         format as hour?
     */
    public void add(int titleId, CharSequence summary, long time, boolean remote, boolean hour) {
        if (time == NEVER) {
            return;
        }

        ZmanimItem item = new ZmanimItem(titleId);
        item.summary = summary;
        item.time = time;
        item.emphasis = settings.isEmphasis(titleId);
        item.timeLabel = hour ? timeFormatSeasonalHour.format(time) : timeFormat.format(time);
        item.elapsed = remote ? (time < now) : !(showElapsed || hour) && (time < now);

        add(item);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time in milliseconds.
     */
    public void addHour(int titleId, int summaryId, long time) {
        addHour(titleId, summaryId, time, false);
    }

    /**
     * Adds the item to the array for a valid time.
     *
     * @param titleId
     *         the title label id.
     * @param summaryId
     *         the summary label id.
     * @param time
     *         the time in milliseconds.
     */
    public void addHour(int titleId, int summaryId, long time, boolean remote) {
        add(titleId, (summaryId == SUMMARY_NONE) ? null : getContext().getText(summaryId), time, remote, true);
    }

    /**
     * Sort the times from oldest to newest.
     */
    protected void sort() {
        if (comparator == null) {
            comparator = new ZmanimComparator();
        }
        sort(comparator);
    }

    /**
     * View holder for zman row item.
     *
     * @author Moshe W
     */
    protected static class ViewHolder {

        public final View itemView;
        public final TextView title;
        public final TextView summary;
        public final TextView time;

        public ViewHolder(View view) {
            this.itemView = view;
            this.title = (TextView) view.findViewById(android.R.id.title);
            this.summary = (TextView) view.findViewById(android.R.id.summary);
            this.time = (TextView) view.findViewById(R.id.time);
        }

        public void bind(ZmanimItem item, boolean summaries, float emphasisScale) {
            boolean enabled = !item.elapsed;

            itemView.setEnabled(enabled);
            itemView.setTag(R.id.time, item);

            title.setText(item.titleId);
            title.setEnabled(enabled);
            if (item.emphasis) {
                title.setTypeface(title.getTypeface(), Typeface.BOLD);
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * emphasisScale);
            }

            if (summary != null) {
                summary.setText(item.summary);
                summary.setEnabled(enabled);
                if (!summaries || (item.summary == null))
                    summary.setVisibility(View.GONE);
            }

            time.setText(item.timeLabel);
            time.setEnabled(enabled);
            if (item.emphasis) {
                time.setTypeface(time.getTypeface(), Typeface.BOLD);
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, time.getTextSize() * emphasisScale);
            }
        }
    }

    /**
     * Format the Hebrew date.
     *
     * @param context
     *         the context.
     * @param jewishDate
     *         the date.
     * @return the formatted date.
     */
    public CharSequence formatDate(Context context, JewishDate jewishDate) {
        int jewishDay = jewishDate.getJewishDayOfMonth();
        int jewishMonth = jewishDate.getJewishMonth();
        int jewishYear = jewishDate.getJewishYear();
        if ((jewishMonth == JewishDate.ADAR) && jewishDate.isJewishLeapYear()) {
            jewishMonth = ADAR_I; // return "Adar I", not just "Adar".
        }

        String[] monthNames = this.monthNames;
        if (monthNames == null) {
            monthNames = context.getResources().getStringArray(R.array.hebrew_months);
            this.monthNames = monthNames;
        }
        String format = monthDayYear;
        if (format == null) {
            format = context.getString(R.string.month_day_year);
            monthDayYear = format;
        }

        String yearStr;
        String monthStr = monthNames[jewishMonth - 1];
        String dayStr;
        String dayPadded;

        if (LocaleUtils.isLocaleRTL(getContext())) {
            HebrewDateFormatter formatter = getHebrewDateFormatter();

            yearStr = formatter.formatHebrewNumber(jewishYear);
            dayStr = formatter.formatHebrewNumber(jewishDay);
            dayPadded = dayStr;
        } else {
            yearStr = String.valueOf(jewishYear);
            dayStr = String.valueOf(jewishDay);
            dayPadded = (jewishDay < 10) ? "0" + dayStr : dayStr;
        }

        String formatted = format.replaceAll(YEAR_VAR, yearStr);
        formatted = formatted.replaceAll(MONTH_VAR, monthStr);
        formatted = formatted.replaceAll(DAY_VAR, dayStr);
        formatted = formatted.replaceAll(DAY_PAD_VAR, dayPadded);

        return formatted;
    }

    /**
     * Set the calendar for the relevant day that the adapter is populated.
     *
     * @param calendar
     *         the calendar.
     */
    public void setCalendar(ComplexZmanimCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Sets whether to use Israel holiday scheme or not.
     *
     * @param inIsrael
     *         set to {@code true} for calculations for Israel.
     */
    public void setInIsrael(boolean inIsrael) {
        this.inIsrael = inIsrael;
    }

    /**
     * Get the Jewish calendar.
     *
     * @return the calendar - {@code null} if date is invalid.
     */
    public JewishCalendar getJewishCalendar() {
        Calendar gcal = getCalendar().getCalendar();
        if (gcal.get(Calendar.YEAR) <= 1) {
            // Avoid future "IllegalArgumentException".
            return null;
        }
        JewishCalendar jcal = new JewishCalendar(gcal);
        jcal.setInIsrael(inIsrael);
        return jcal;
    }

    /**
     * Format the number of omer days.
     *
     * @param context
     *         the context.
     * @param days
     *         the number of days.
     * @return the formatted count.
     */
    public CharSequence formatOmer(Context context, int days) {
        if (days <= 0) {
            return null;
        }
        String suffix = settings.getOmerSuffix();
        if (TextUtils.isEmpty(suffix)) {
            return null;
        }
        if (ZmanimPreferences.OMER_B.equals(suffix)) {
            suffix = context.getString(R.string.omer_b);
        } else if (ZmanimPreferences.OMER_L.equals(suffix)) {
            suffix = context.getString(R.string.omer_l);
        }

        String format;
        if (days == 33) {
            format = context.getString(R.string.omer_33);
            return String.format(format, suffix);
        }

        format = omerFormat;
        if (format == null) {
            format = context.getString(R.string.omer_format);
            omerFormat = format;
        }

        String dayStr;

        if (LocaleUtils.isLocaleRTL(getContext())) {
            HebrewDateFormatter formatter = getHebrewDateFormatter();

            dayStr = formatter.formatHebrewNumber(days);
        } else {
            dayStr = String.valueOf(days);
        }

        return String.format(format, dayStr, suffix);
    }

    protected HebrewDateFormatter getHebrewDateFormatter() {
        HebrewDateFormatter formatter = hebrewDateFormatter;
        if (formatter == null) {
            formatter = new HebrewDateFormatter();
            formatter.setHebrewFormat(true);
            hebrewDateFormatter = formatter;
        }
        return formatter;
    }
}

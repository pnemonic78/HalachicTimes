/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
        public int titleId;
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
        public ZmanimItem() {
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
            return elapsed || (time == ZmanimAdapter.NEVER) || (timeLabel == null);
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
        return createViewFromResource(position, convertView, parent, R.layout.times_item);
    }

    /**
     * Bind the item to the view.
     *
     * @param position
     *         the row index.
     * @param convertView
     *         the view.
     * @param parent
     *         the parent view.
     * @param resource
     *         the resource layout.
     * @return the item view.
     */
    protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        ZmanimItem item = getItem(position);
        boolean enabled = !item.elapsed;

        View view = convertView;
        ViewHolder holder;
        TextView title;
        TextView summary;
        TextView time;

        if (view == null) {
            view = inflater.inflate(resource, parent, false);

            title = (TextView) view.findViewById(android.R.id.title);
            summary = (TextView) view.findViewById(android.R.id.summary);
            time = (TextView) view.findViewById(R.id.time);

            holder = new ViewHolder(title, summary, time);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            title = holder.title;
            summary = holder.summary;
            time = holder.time;
        }
        view.setEnabled(enabled);
        view.setTag(R.id.time, item);

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

        return view;
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
    public void add(int titleId, int summaryId, Date time) {
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
    public void add(int titleId, int summaryId, Date time, boolean remote) {
        add(titleId, summaryId, time == null ? NEVER : time.getTime(), remote);
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
    public void add(int titleId, CharSequence summary, Date time, boolean remote) {
        add(titleId, summary, time == null ? NEVER : time.getTime(), remote);
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
        add(titleId, summary, time, remote, (titleId == R.string.hour));
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
        ZmanimItem item = new ZmanimItem();
        item.titleId = titleId;
        item.summary = summary;
        item.time = time;
        item.emphasis = settings.isEmphasis(titleId);

        if (time == NEVER) {
            item.timeLabel = null;
            item.elapsed = true;
        } else {
            item.timeLabel = hour ? timeFormatSeasonalHour.format(time) : timeFormat.format(time);
            item.elapsed = remote ? (time < now) : (showElapsed || (titleId == R.string.hour)) ? false : (time < now);
        }

        if (time != NEVER) {
            add(item);
        }
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
    private static class ViewHolder {

        public final TextView title;
        public final TextView summary;
        public final TextView time;

        public ViewHolder(TextView title, TextView summary, TextView time) {
            this.title = title;
            this.summary = summary;
            this.time = time;
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
            jewishMonth = 14; // return Adar I, not Adar in a leap year
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

        if (ZmanimLocations.isLocaleRTL()) {
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
     * Set the calendar.
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
     * @return the calendar.
     */
    public JewishCalendar getJewishCalendar() {
        Calendar gcal = getCalendar().getCalendar();
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

        if (ZmanimLocations.isLocaleRTL()) {
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

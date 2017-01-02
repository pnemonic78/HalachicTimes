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
package net.sf.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Preference that shows a time picker.
 * <p>
 * The preference value is stored in the ISO 8601 format {@code hh:mm}
 * </p>
 *
 * @author Moshe Waisberg
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Times">ISO 8601 - Wikipedia</a>
 */
public class TimePreference extends DialogPreference {

    private static final String TAG = "TimePreference";

    /**
     * ISO 8601 time format.
     */
    protected static final String PATTERN = "HH:mm";

    private TimePicker picker;
    private String value;
    private Calendar time;
    private static final java.text.DateFormat formatIso = new SimpleDateFormat(PATTERN, Locale.US);
    private java.text.DateFormat formatPretty;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        formatPretty = DateFormat.getTimeFormat(context);
    }

    @Override
    protected View onCreateDialogView() {
        Context context = getContext();
        picker = new TimePicker(context);
        picker.setIs24HourView(DateFormat.is24HourFormat(context));

        return picker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (time != null) {
            picker.setCurrentHour(time.get(Calendar.HOUR_OF_DAY));
            picker.setCurrentMinute(time.get(Calendar.MINUTE));
        }
    }

    /**
     * Saves the time to the {@link SharedPreferences}.
     *
     * @param timeString The chosen time. Can be {@code null}.
     */
    public void setTime(String timeString) {
        final boolean wasBlocking = shouldDisableDependents();

        this.value = timeString;
        this.time = parseTime(timeString);

        persistString(timeString);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Saves the time to the {@link SharedPreferences}.
     *
     * @param time The chosen time. Can be {@code null}.
     */
    public void setTime(Calendar time) {
        final boolean wasBlocking = shouldDisableDependents();

        this.value = (time != null) ? formatIso.format(time.getTime()) : null;
        this.time = time;

        persistString(value);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Gets the time from the {@link SharedPreferences}.
     *
     * @return The current preference value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the time from the {@link SharedPreferences}.
     *
     * @return The current preference value.
     */
    public Calendar getTime() {
        return time;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            time.set(Calendar.MINUTE, picker.getCurrentMinute());
            String value = formatIso.format(time.getTime());

            if (callChangeListener(value)) {
                setTime(time);
            }
        }
    }

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setTime(restoreValue ? getPersistedString(value) : (String) defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(value) || super.shouldDisableDependents();
    }

    /**
     * Returns the {@link TimePicker} widget that will be shown in the dialog.
     *
     * @return The {@link TimePicker} widget that will be shown in the dialog.
     */
    public TimePicker getTimePicker() {
        return picker;
    }

    /**
     * Format the time as per user's locale.
     *
     * @return the formatted time.
     */
    public String formatTime() {
        return (time != null) ? formatPretty.format(time.getTime()) : null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which == DialogInterface.BUTTON_NEUTRAL) {
            String value = null;//"off"
            if (callChangeListener(value)) {
                setTime(value);
            }
        }
    }

    /**
     * Parse the time value.
     *
     * @param timeString the time in ISO 8601 format.
     * @return the time - {@code null} otherwise.
     */
    public static Calendar parseTime(String timeString) {
        if (!TextUtils.isEmpty(timeString)) {
            try {
                Date date = formatIso.parse(timeString);
                Calendar time = Calendar.getInstance();
                time.setTime(date);
                return time;
            } catch (ParseException e) {
                Log.e(TAG, "parseTime: " + e.getLocalizedMessage(), e);
            }
        }
        return null;
    }
}

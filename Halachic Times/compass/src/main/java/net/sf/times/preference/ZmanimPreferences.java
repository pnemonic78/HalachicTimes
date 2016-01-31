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
package net.sf.times.preference;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;

import net.sf.preference.RingtonePreference;
import net.sf.preference.SeekBarDialogPreference;
import net.sf.preference.TimePreference;
import net.sf.times.compass.R;
import net.sf.times.ZmanimApplication;
import net.sf.times.location.AddressProvider;

import java.util.Calendar;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferences extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String REMINDER_SUNDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.SUNDAY;
    private static final String REMINDER_MONDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.MONDAY;
    private static final String REMINDER_TUESDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.TUESDAY;
    private static final String REMINDER_WEDNESDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.WEDNESDAY;
    private static final String REMINDER_THURSDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.THURSDAY;
    private static final String REMINDER_FRIDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.FRIDAY;
    private static final String REMINDER_SATURDAY_SUFFIX = ZmanimSettings.REMINDER_SUFFIX + ".day." + Calendar.SATURDAY;

    private SeekBarDialogPreference candlesSeek;
    private SeekBarDialogPreference shabbathSeek;
    private ZmanimSettings settings;
    private Preference clearHistory;
    private RingtonePreference reminderRingtonePreference;
    private Runnable remindRunner;

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferences() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen screen = getPreferenceScreen();
        screen.setTitle(getTitle());
        replaceScreen(screen, "general", R.xml.general_preferences);
        replaceScreen(screen, "appearance", R.xml.appearance_preferences);
        PreferenceScreen screenZmanim = replaceScreen(screen, "zmanim", R.xml.zmanim_preferences);
        replaceScreen(screen, "privacy", R.xml.privacy_preferences);
        replaceScreen(screen, "about", R.xml.about_preferences);

        replaceScreen(screenZmanim, "hour_screen", R.xml.zman_hour_preferences);
        replaceScreen(screenZmanim, "dawn_screen", R.xml.zman_dawn_preferences);
        replaceScreen(screenZmanim, "tallis_screen", R.xml.zman_tallis_preferences);
        replaceScreen(screenZmanim, "sunrise_screen", R.xml.zman_sunrise_preferences);
        replaceScreen(screenZmanim, "shema_screen", R.xml.zman_shema_preferences);
        replaceScreen(screenZmanim, "prayers_screen", R.xml.zman_prayers_preferences);
        replaceScreen(screenZmanim, "midday_screen", R.xml.zman_midday_preferences);
        replaceScreen(screenZmanim, "earliest_mincha_screen", R.xml.zman_earliest_mincha_preferences);
        replaceScreen(screenZmanim, "mincha_screen", R.xml.zman_mincha_preferences);
        replaceScreen(screenZmanim, "plug_hamincha_screen", R.xml.zman_plug_hamincha_preferences);
        replaceScreen(screenZmanim, "candles_screen", R.xml.zman_candles_preferences);
        replaceScreen(screenZmanim, "sunset_screen", R.xml.zman_sunset_preferences);
        replaceScreen(screenZmanim, "twilight_screen", R.xml.zman_twilight_preferences);
        replaceScreen(screenZmanim, "nightfall_screen", R.xml.zman_nightfall_preferences);
        replaceScreen(screenZmanim, "shabbath_screen", R.xml.zman_shabbath_preferences);
        replaceScreen(screenZmanim, "midnight_screen", R.xml.zman_midnight_preferences);
        replaceScreen(screenZmanim, "levana_earliest_screen", R.xml.zman_levana_earliest_preferences);
        replaceScreen(screenZmanim, "levana_latest_screen", R.xml.zman_levana_latest_preferences);
        replaceScreen(screenZmanim, "burn_chametz_screen", R.xml.zman_chametz_preferences);

        reminderRingtonePreference = initRingtone(ZmanimSettings.KEY_REMINDER_RINGTONE);
        initList(ZmanimSettings.KEY_REMINDER_STREAM);

        initList(ZmanimSettings.KEY_COORDS_FORMAT);
        initList(ZmanimSettings.KEY_THEME);

        candlesSeek = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
        candlesSeek.setSummaryFormat(R.plurals.candles_summary);
        candlesSeek.setOnPreferenceChangeListener(this);
        onSeekPreferenceChange(candlesSeek, null);

        shabbathSeek = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_SHABBATH_ENDS_MINUTES);
        shabbathSeek.setSummaryFormat(R.plurals.shabbath_ends_summary);
        shabbathSeek.setOnPreferenceChangeListener(this);
        onSeekPreferenceChange(shabbathSeek, null);

        initList(ZmanimSettings.KEY_OPINION_HOUR);
        initList(ZmanimSettings.KEY_OPINION_DAWN);
        initList(ZmanimSettings.KEY_OPINION_TALLIS);
        initList(ZmanimSettings.KEY_OPINION_SUNRISE);
        initList(ZmanimSettings.KEY_OPINION_SHEMA);
        initList(ZmanimSettings.KEY_OPINION_TFILA);
        initList(ZmanimSettings.KEY_OPINION_NOON);
        initList(ZmanimSettings.KEY_OPINION_EARLIEST_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_PLUG_MINCHA);
        initList(ZmanimSettings.KEY_OPINION_SUNSET);
        initList(ZmanimSettings.KEY_OPINION_CANDLES_CHANUKKA);
        initList(ZmanimSettings.KEY_OPINION_TWILIGHT);
        initList(ZmanimSettings.KEY_OPINION_NIGHTFALL);
        initList(ZmanimSettings.KEY_OPINION_SHABBATH_ENDS_MINUTES);
        initList(ZmanimSettings.KEY_OPINION_MIDNIGHT);
        initList(ZmanimSettings.KEY_OPINION_EARLIEST_LEVANA);
        initList(ZmanimSettings.KEY_OPINION_LATEST_LEVANA);
        initList(ZmanimSettings.KEY_OPINION_BURN);
        initList(ZmanimSettings.KEY_OPINION_OMER);

        initList(ZmanimSettings.KEY_REMINDER_DAWN);
        initList(ZmanimSettings.KEY_REMINDER_TALLIS);
        initList(ZmanimSettings.KEY_REMINDER_SUNRISE);
        initList(ZmanimSettings.KEY_REMINDER_SHEMA);
        initList(ZmanimSettings.KEY_REMINDER_TFILA);
        initList(ZmanimSettings.KEY_REMINDER_NOON);
        initList(ZmanimSettings.KEY_REMINDER_EARLIEST_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_PLUG_MINCHA);
        initList(ZmanimSettings.KEY_REMINDER_CANDLES);
        initList(ZmanimSettings.KEY_REMINDER_SUNSET);
        initList(ZmanimSettings.KEY_REMINDER_TWILIGHT);
        initList(ZmanimSettings.KEY_REMINDER_NIGHTFALL);
        initList(ZmanimSettings.KEY_REMINDER_MIDNIGHT);

        initTime(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
        initTime(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

        initReminderDays(ZmanimSettings.KEY_REMINDER_DAWN);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TALLIS);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SUNRISE);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SHEMA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TFILA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_NOON);
        initReminderDays(ZmanimSettings.KEY_REMINDER_EARLIEST_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_PLUG_MINCHA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_CANDLES);
        initReminderDays(ZmanimSettings.KEY_REMINDER_SUNSET);
        initReminderDays(ZmanimSettings.KEY_REMINDER_TWILIGHT);
        initReminderDays(ZmanimSettings.KEY_REMINDER_NIGHTFALL);
        initReminderDays(ZmanimSettings.KEY_REMINDER_MIDNIGHT);
        initReminderDays(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
        initReminderDays(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

        clearHistory = findPreference("clear_history");
        clearHistory.setOnPreferenceClickListener(this);

        Preference version = findPreference("about.version");
        try {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            // Never should happen with our own package!
        }

        // Other preferences that affect the app widget.
        findPreference(ZmanimSettings.KEY_PAST).setOnPreferenceChangeListener(this);
        findPreference(ZmanimSettings.KEY_SECONDS).setOnPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    protected ListPreference initList(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if ((pref != null) && (pref instanceof ListPreference)) {
            ListPreference list = (ListPreference) pref;
            list.setOnPreferenceChangeListener(this);
            onListPreferenceChange(list, list.getValue());
            return list;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    protected RingtonePreference initRingtone(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if ((pref != null) && (pref instanceof RingtonePreference)) {
            RingtonePreference ring = (RingtonePreference) pref;
            ring.setOnPreferenceChangeListener(this);
            onRingtonePreferenceChange(ring, ring.getValue());
            return ring;
        }
        return null;
    }

    protected TimePreference initTime(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if ((pref != null) && (pref instanceof TimePreference)) {
            TimePreference time = (TimePreference) pref;
            time.setNeutralButtonText(R.string.off);
            time.setOnPreferenceChangeListener(this);
            onTimePreferenceChange(time, time.getValue());
            return time;
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == candlesSeek) {
            return onSeekPreferenceChange(candlesSeek, newValue);
        }
        if (preference == shabbathSeek) {
            return onSeekPreferenceChange(shabbathSeek, newValue);
        }
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference) preference;
            return onCheckBoxPreferenceChange(checkBox, newValue);
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            onListPreferenceChange(list, newValue);
            return false;
        }
        if (preference instanceof RingtonePreference) {
            RingtonePreference ring = (RingtonePreference) preference;
            return onRingtonePreferenceChange(ring, newValue);
        }
        if (preference instanceof TimePreference) {
            TimePreference time = (TimePreference) preference;
            return onTimePreferenceChange(time, newValue);
        }
        return true;
    }

    private boolean onSeekPreferenceChange(SeekBarDialogPreference preference, Object newValue) {
        int minutes = preference.getProgress();
        Resources res = getResources();
        CharSequence summary = null;
        if (preference == candlesSeek) {
            summary = res.getQuantityString(R.plurals.candles_summary, minutes, minutes);
        } else if (preference == shabbathSeek) {
            summary = res.getQuantityString(R.plurals.shabbath_ends_summary, minutes, minutes);
        }
        preference.setSummary(summary);
        return true;
    }

    /**
     * Called when a list preference has probably changed its value.
     * <br>Updates the summary to the new value.
     *
     * @param preference
     *         the  preference.
     * @param newValue
     *         the possibly new value.
     */
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();
        String value = (newValue == null) ? null : newValue.toString();
        //Set the value for the summary.
        preference.setValue(value);
        updateSummary(preference, value);

        if (!oldValue.equals(newValue)) {
            String key = preference.getKey();

            if (ZmanimSettings.KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
                int audioStreamType = TextUtils.isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
                int ringType;
                if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                    ringType = RingtoneManager.TYPE_NOTIFICATION;
                } else {
                    ringType = RingtoneManager.TYPE_ALARM;
                }
                reminderRingtonePreference.setRingtoneType(ringType);
            } else if (key.endsWith(ZmanimSettings.REMINDER_SUFFIX)) {
                // Explicitly disable dependencies?
                preference.notifyDependencyChange(preference.shouldDisableDependents());
            }
        }
    }

    /**
     * Called when a ringtone preference has changed its value.
     * <br>Updates the summary to the new ringtone title.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onRingtonePreferenceChange(RingtonePreference preference, Object newValue) {
        String value = (newValue == null) ? null : newValue.toString();
        updateSummary(preference, value);
        return true;
    }

    /**
     * Called when a check box preference has changed its value.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onCheckBoxPreferenceChange(CheckBoxPreference preference, Object newValue) {
        return true;
    }

    /**
     * Called when a time preference has probably changed its value.
     * <br>Updates the summary to the new value.
     *
     * @param preference
     *         the  preference.
     * @param newValue
     *         the possibly new value.
     */
    protected boolean onTimePreferenceChange(TimePreference preference, Object newValue) {
        String value = (newValue == null) ? null : newValue.toString();
        //Set the value for the summary.
        preference.setTime(value);
        updateSummary(preference, value);
        return true;
    }

    /**
     * Update the summary that was selected from the list.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(ListPreference preference, String newValue) {
        CharSequence[] values = preference.getEntryValues();
        CharSequence[] entries = preference.getEntries();
        int length = values.length;

        for (int i = 0; i < length; i++) {
            if (newValue.equals(values[i])) {
                preference.setSummary(entries[i]);
                return;
            }
        }
        preference.setSummary(null);
    }

    /**
     * Update the summary that was selected from the list.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(RingtonePreference preference, String newValue) {
        preference.setSummary(preference.getRingtoneTitle(newValue));
    }

    /**
     * Update the summary that was selected from the time picker.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(TimePreference preference, String newValue) {
        String summary = preference.formatTime();
        if (summary != null) {
            preference.setSummary(summary);
        } else {
            preference.setSummary(R.string.off);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == clearHistory) {
            preference.setEnabled(false);
            deleteHistory();
            preference.setEnabled(true);
            return true;
        }
        return false;
    }

    /**
     * Clear the history of addresses.
     */
    private void deleteHistory() {
        ZmanimApplication app = (ZmanimApplication) getApplication();
        AddressProvider provider = app.getAddresses();
        provider.deleteAddresses();
    }

    protected void initReminderDays(String reminderKey) {
        if (TextUtils.isEmpty(reminderKey)) {
            return;
        }

        initReminderDay(reminderKey + ZmanimSettings.REMINDER_SUNDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_MONDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_TUESDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_WEDNESDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_THURSDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_FRIDAY_SUFFIX);
        initReminderDay(reminderKey + ZmanimSettings.REMINDER_SATURDAY_SUFFIX);
    }

    protected CheckBoxPreference initReminderDay(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            CheckBoxPreference checkBox = (CheckBoxPreference) pref;
            checkBox.setOnPreferenceChangeListener(this);
            return checkBox;
        }
        return null;
    }

    private PreferenceScreen replaceScreen(PreferenceScreen parent, String key, int xmlId) {
        PreferenceScreen current = getPreferenceScreen();
        PreferenceScreen screen = (PreferenceScreen) parent.findPreference(key);
        setPreferenceScreen(screen);
        addPreferencesFromResource(xmlId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            int count = screen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                screen.getPreference(i).setFragment(null);
            }
        }
        setPreferenceScreen((current != null) ? current : parent);
        return screen;
    }
}

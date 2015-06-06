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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;
import net.sf.times.ZmanimApplication;
import net.sf.times.ZmanimReminder;
import net.sf.times.ZmanimWidget;
import net.sf.times.location.AddressProvider;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferences extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private SeekBarDialogPreference candles;
    private ZmanimSettings settings;
    private ZmanimReminder reminder;
    private Preference clearHistory;

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferences() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

        initList(ZmanimSettings.KEY_REMIDER_STREAM);

        candles = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
        candles.setOnPreferenceChangeListener(this);
        onCandlesPreferenceChange(candles, null);

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
        initList(ZmanimSettings.KEY_OPINION_TWILIGHT);
        initList(ZmanimSettings.KEY_OPINION_NIGHTFALL);
        initList(ZmanimSettings.KEY_OPINION_MIDNIGHT);
        initList(ZmanimSettings.KEY_OPINION_EARLIEST_LEVANA);
        initList(ZmanimSettings.KEY_OPINION_LATEST_LEVANA);

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
        initList(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
        initList(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

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
    private void initList(String name) {
        ListPreference list = (ListPreference) findPreference(name);
        list.setOnPreferenceChangeListener(this);
        onListPreferenceChange(list, list.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == candles) {
            onCandlesPreferenceChange(preference, newValue);
            return true;
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            onListPreferenceChange(list, newValue);
            notifyAppWidgets();
            return false;
        }
        notifyAppWidgets();
        return true;
    }

    private void onCandlesPreferenceChange(Preference preference, Object newValue) {
        Resources res = getResources();
        int minutes = candles.getProgress();
        CharSequence summary = res.getQuantityString(R.plurals.candles_summary, minutes, minutes);
        candles.setSummary(summary);
    }

    private void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();
        String value = newValue.toString();
        updateSummary(preference, value);

        if (!oldValue.equals(newValue)) {
            if (preference.getKey().endsWith(ZmanimSettings.REMINDER_SUFFIX)) {
                if (settings == null)
                    settings = new ZmanimSettings(this);
                if (reminder == null)
                    reminder = new ZmanimReminder(this);
                reminder.remind(settings);
            }
        }
    }

    /**
     * Find the summary that was selected from the list.
     *
     * @param preference
     *         the preference.
     * @param newValue
     *         the new value.
     */
    private void updateSummary(ListPreference preference, String newValue) {
        preference.setValue(newValue);

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

    private void notifyAppWidgets() {
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final Class<?> clazz = ZmanimWidget.class;
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;

        Intent intent = new Intent(context, ZmanimWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
}

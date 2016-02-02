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
package net.sf.times.compass.preference;

import android.content.pm.PackageManager.NameNotFoundException;
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

import net.sf.times.compass.CompassApplication;
import net.sf.times.compass.R;
import net.sf.times.location.AddressProvider;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class Compass10PreferenceActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private Preference clearHistory;

    /**
     * Constructs a new preferences.
     */
    public Compass10PreferenceActivity() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen screen = getPreferenceScreen();
        screen.setTitle(getTitle());
        replaceScreen(screen, "appearance", R.xml.appearance_preferences);
        replaceScreen(screen, "privacy", R.xml.privacy_preferences);
        replaceScreen(screen, "about", R.xml.about_preferences);

        initList(CompassSettings.KEY_COORDS_FORMAT);
        initList(CompassSettings.KEY_THEME);

        clearHistory = findPreference("clear_history");
        clearHistory.setOnPreferenceClickListener(this);

        Preference version = findPreference("about.version");
        try {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            // Never should happen with our own package!
        }
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference) preference;
            return onCheckBoxPreferenceChange(checkBox, newValue);
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            onListPreferenceChange(list, newValue);
            return false;
        }
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
        String value = (newValue == null) ? null : newValue.toString();
        //Set the value for the summary.
        preference.setValue(value);
        updateSummary(preference, value);
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
        CompassApplication app = (CompassApplication) getApplication();
        AddressProvider provider = app.getAddresses();
        provider.deleteAddresses();
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

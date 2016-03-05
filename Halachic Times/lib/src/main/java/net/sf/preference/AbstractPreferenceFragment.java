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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import net.sf.lib.R;

/**
 * This fragment shows the preferences for a header.
 */
public abstract class AbstractPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int xmlId = getPreferencesXml();
        PreferenceManager.setDefaultValues(getActivity(), xmlId, false);
        addPreferencesFromResource(xmlId);
    }

    protected abstract int getPreferencesXml();

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
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference) preference;
            return onCheckBoxPreferenceChange(checkBox, newValue);
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            onListPreferenceChange(list, newValue);
            notifyPreferenceChanged();
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
        notifyPreferenceChanged();
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
        return false;
    }

    /**
     * Notification that the preference has changed and should do something external.
     */
    protected void notifyPreferenceChanged() {
    }

    protected void validateIntent(String key) {
        validateIntent(findPreference(key));
    }

    protected void validateIntent(Preference preference) {
        Intent intent = preference.getIntent();
        if (intent == null) {
            return;
        }
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, 0);
        if (info == null) {
            preference.setIntent(null);
        }
    }
}

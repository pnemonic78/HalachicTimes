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
package com.github.preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.TextUtils;

import com.github.lib.R;
import com.github.util.LogUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.O;

/**
 * This fragment shows the preferences for a header.
 */
public abstract class AbstractPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = "AbstractPreferenceFrag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int xmlId = getPreferencesXml();
        PreferenceManager.setDefaultValues(getActivity(), xmlId, false);
        addPreferencesFromResource(xmlId);
        addChangeListeners(getPreferenceScreen());
    }

    protected abstract int getPreferencesXml();

    protected Preference initPreference(String key) {
        Preference pref;

        pref = initList(key);
        if (pref != null) {
            return pref;
        }
        pref = initRingtone(key);
        if (pref != null) {
            return pref;
        }
        pref = initTime(key);
        if (pref != null) {
            return pref;
        }

        return null;
    }

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
        notifyPreferenceChanged();
        if (preference instanceof SwitchPreference) {
            SwitchPreference checkBox = (SwitchPreference) preference;
            return onCheckBoxPreferenceChange(checkBox, newValue);
        }
        if (preference instanceof ListPreference) {
            ListPreference list = (ListPreference) preference;
            return onListPreferenceChange(list, newValue);
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

    /**
     * Called when a list preference has probably changed its value.
     * <br>Updates the summary to the new value.
     *
     * @param preference the  preference.
     * @param newValue   the possibly new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onListPreferenceChange(ListPreference preference, Object newValue) {
        String value = (newValue == null) ? null : newValue.toString();
        //Set the value for the summary.
        preference.setValue(value);
        updateSummary(preference, value);
        return false;
    }

    /**
     * Called when a ringtone preference has changed its value.
     * <br>Updates the summary to the new ringtone title.
     *
     * @param preference the preference.
     * @param newValue   the new value.
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
     * @param preference the preference.
     * @param newValue   the new value.
     * @return {@code true} if the user value should be set as the preference value (and persisted).
     */
    protected boolean onCheckBoxPreferenceChange(SwitchPreference preference, Object newValue) {
        return true;
    }

    /**
     * Called when a time preference has probably changed its value.
     * <br>Updates the summary to the new value.
     *
     * @param preference the  preference.
     * @param newValue   the possibly new value.
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
     * @param preference the preference.
     * @param newValue   the new value.
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
     * @param preference the preference.
     * @param newValue   the new value.
     */
    private void updateSummary(RingtonePreference preference, String newValue) {
        preference.setSummary(preference.getRingtoneTitle(newValue));
    }

    /**
     * Update the summary that was selected from the time picker.
     *
     * @param preference the preference.
     * @param newValue   the new value.
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
        final Intent intent = preference.getIntent();
        if (intent == null) {
            return;
        }
        final Context context = preference.getContext();
        PackageManager pm = context.getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, MATCH_DEFAULT_ONLY);
        if (info != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Error launching intent: " + intent, e);
                    }
                    return true;
                }
            });
        } else {
            preference.setIntent(null);
        }
    }

    protected void addChangeListeners(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        if (preference instanceof PreferenceGroup) {
            addChangeListeners((PreferenceGroup) preference);
        }
    }

    protected void addChangeListeners(PreferenceGroup group) {
        final int count = group.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            addChangeListeners(group.getPreference(i));
        }
    }

    public boolean removePreference(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            if (VERSION.SDK_INT >= O) {
                return pref.getParent().removePreference(pref);
            }
            return findPreferenceParent(key).removePreference(pref);
        }

        return false;
    }

    @Nullable
    public PreferenceGroup findPreferenceParent(CharSequence key) {
        return findPreferenceParent(key, getPreferenceScreen());
    }

    @Nullable
    protected PreferenceGroup findPreferenceParent(CharSequence key, PreferenceGroup parent) {
        final int preferenceCount = parent.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = parent.getPreference(i);
            final String curKey = preference.getKey();

            if (curKey != null && curKey.equals(key)) {
                return parent;
            }

            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup returnedParent = findPreferenceParent(key, (PreferenceGroup) preference);
                if (returnedParent != null) {
                    return returnedParent;
                }
            }
        }

        return null;
    }

    @Nullable
    protected CharSequence findEntry(@NonNull ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index >= 0) {
            return preference.getEntries()[index];
        }
        return null;
    }
}

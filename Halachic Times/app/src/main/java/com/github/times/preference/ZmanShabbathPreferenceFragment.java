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
package com.github.times.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.github.preference.NumberPickerPreference;
import com.github.times.R;

import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER;
import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES;
import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_NIGHTFALL;
import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_SUNSET;
import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_TWILIGHT;

/**
 * This fragment shows the preferences for the Shabbath Ends screen.
 */
public class ZmanShabbathPreferenceFragment extends ZmanPreferenceFragment {

    private ListPreference afterPreference;
    private ListPreference sunsetPreference;
    private ListPreference twilightPreference;
    private ListPreference nightfallPreference;
    private NumberPickerPreference minutesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sunsetPreference = addDefaultOption(KEY_OPINION_SHABBATH_ENDS_SUNSET);
        twilightPreference = addDefaultOption(KEY_OPINION_SHABBATH_ENDS_TWILIGHT);
        nightfallPreference = addDefaultOption(KEY_OPINION_SHABBATH_ENDS_NIGHTFALL);
        minutesPreference = (NumberPickerPreference) findPreference(KEY_OPINION_SHABBATH_ENDS_MINUTES);
        afterPreference = initList(KEY_OPINION_SHABBATH_ENDS_AFTER);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();

        if (KEY_OPINION_SHABBATH_ENDS_MINUTES.equals(key)) {
            int shabbathAfterId = getPreferences().toId(afterPreference.getValue());
            String shabbathAfterName = getString(shabbathAfterId);

            int value = (int) newValue;
            minutesPreference.setSummary(getResources().getQuantityString(R.plurals.shabbath_ends_summary, value, value, shabbathAfterName));
        }

        return super.onPreferenceChange(preference, newValue);
    }

    @Override
    protected boolean onListPreferenceChange(ListPreference preference, Object newValue) {
        final String key = preference.getKey();

        if (KEY_OPINION_SHABBATH_ENDS_AFTER.equals(key) && (sunsetPreference != null)) {
            int shabbathAfterId = getPreferences().toId(newValue.toString());
            String shabbathAfterName = getString(shabbathAfterId);

            switch (shabbathAfterId) {
                case R.string.sunset:
                    sunsetPreference.setEnabled(true);
                    twilightPreference.setEnabled(false);
                    nightfallPreference.setEnabled(false);
                    break;
                case R.string.twilight:
                    sunsetPreference.setEnabled(false);
                    twilightPreference.setEnabled(true);
                    nightfallPreference.setEnabled(false);
                    break;
                case R.string.nightfall:
                    sunsetPreference.setEnabled(false);
                    twilightPreference.setEnabled(false);
                    nightfallPreference.setEnabled(true);
                    break;
                default:
                    sunsetPreference.setEnabled(false);
                    twilightPreference.setEnabled(false);
                    nightfallPreference.setEnabled(false);
                    break;
            }

            int value = minutesPreference.getValue();
            minutesPreference.setSummary(getResources().getQuantityString(R.plurals.shabbath_ends_summary, value, value, shabbathAfterName));
        }

        return super.onListPreferenceChange(preference, newValue);
    }

    private ListPreference addDefaultOption(String key) {
        ListPreference preference = (ListPreference) findPreference(key);
        return addDefaultOption(preference);
    }

    private ListPreference addDefaultOption(ListPreference preference) {
        final Context context = preference.getContext();

        CharSequence[] oldValues = preference.getEntryValues();
        final int oldSize = oldValues.length;
        final int newSize = oldSize + 1;
        CharSequence[] newValues = new CharSequence[newSize];
        System.arraycopy(oldValues, 0, newValues, 1, oldSize);
        CharSequence defaultValue = context.getText(R.string.opinion_value_none);
        newValues[0] = defaultValue;
        preference.setEntryValues(newValues);

        CharSequence[] oldEntries = preference.getEntries();
        CharSequence[] newEntries = new CharSequence[newSize];
        System.arraycopy(oldEntries, 0, newEntries, 1, oldSize);
        CharSequence defaultEntry = context.getText(R.string.none);
        newEntries[0] = defaultEntry;
        preference.setEntries(newEntries);

        onListPreferenceChange(preference, preference.getValue());

        return preference;
    }
}

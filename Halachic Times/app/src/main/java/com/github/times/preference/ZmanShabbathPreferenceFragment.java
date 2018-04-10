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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.github.preference.NumberPickerPreference;
import com.github.times.R;

import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER;
import static com.github.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES;

/**
 * This fragment shows the preferences for the Shabbath Ends screen.
 */
public class ZmanShabbathPreferenceFragment extends ZmanPreferenceFragment {

    private ListPreference afterPreference;
    private NumberPickerPreference minutesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        afterPreference = (ListPreference) findPreference(KEY_OPINION_SHABBATH_ENDS_AFTER);
        minutesPreference = (NumberPickerPreference) findPreference(KEY_OPINION_SHABBATH_ENDS_MINUTES);

        onPreferenceChange(minutesPreference, minutesPreference.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_OPINION_SHABBATH_ENDS_AFTER.equals(preference.getKey())) {
            int shabbathAfterId = getPreferences().toId(newValue.toString());
            String shabbathAfterName = getString(shabbathAfterId);

            int value = minutesPreference.getValue();
            minutesPreference.setSummary(getResources().getQuantityString(R.plurals.shabbath_ends_summary, value, value, shabbathAfterName));
        } else if (KEY_OPINION_SHABBATH_ENDS_MINUTES.equals(preference.getKey())) {
            int shabbathAfterId = getPreferences().toId(afterPreference.getValue());
            String shabbathAfterName = getString(shabbathAfterId);

            int value = (int) newValue;
            minutesPreference.setSummary(getResources().getQuantityString(R.plurals.shabbath_ends_summary, value, value, shabbathAfterName));
        }

        return super.onPreferenceChange(preference, newValue);
    }
}

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
package net.sf.times.preference;

import android.os.Bundle;
import android.preference.Preference;

import net.sf.preference.NumberPickerPreference;
import net.sf.times.R;

import static net.sf.times.preference.ZmanimPreferences.KEY_OPINION_CANDLES;

/**
 * This fragment shows the preferences for the Candles zman screen.
 */
public class ZmanCandlesPreferenceFragment extends ZmanPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NumberPickerPreference candles = (NumberPickerPreference) findPreference(KEY_OPINION_CANDLES);
        candles.setOnPreferenceChangeListener(this);
        onPreferenceChange(candles, candles.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_OPINION_CANDLES.equals(preference.getKey())) {
            preference.setSummary(getResources().getQuantityString(R.plurals.candles_summary, (Integer) newValue, newValue));
        }

        return super.onPreferenceChange(preference, newValue);
    }
}

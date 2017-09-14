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
import android.preference.ListPreference;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;

import static net.sf.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER;
import static net.sf.times.preference.ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES;

/**
 * This fragment shows the preferences for the Candles zman screen.
 */
public class ZmanShabbathPreferenceFragment extends ZmanPreferenceFragment {

    private ListPreference after;
    private SeekBarDialogPreference seek;
    private String shabbathAfterName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        after = (ListPreference) findPreference(KEY_OPINION_SHABBATH_ENDS_AFTER);
        int shabbathAfter = getSettings().toId(after.getValue());
        shabbathAfterName = getString(shabbathAfter);

        seek = (SeekBarDialogPreference) findPreference(KEY_OPINION_SHABBATH_ENDS_MINUTES);
        seek.setSummaryFormat(R.plurals.shabbath_ends_summary, shabbathAfterName);
        seek.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        super.onListPreferenceChange(preference, newValue);
        if ((preference == after) && (newValue != null)) {
            // Update "seek" summary.
            int shabbathAfter = getSettings().toId(newValue.toString());
            shabbathAfterName = getString(shabbathAfter);
            seek.setSummaryFormat(R.plurals.shabbath_ends_summary, shabbathAfterName);
        }
    }
}

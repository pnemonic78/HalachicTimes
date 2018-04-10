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
package com.github.times.compass.preference;

import android.os.Bundle;
import android.preference.Preference;

import net.sf.preference.AbstractPreferenceFragment;
import com.github.times.compass.lib.R;
import com.github.times.location.AddressProvider;
import com.github.times.location.LocationApplication;

/**
 * This fragment shows the preferences for the Privacy and Security header.
 */
public class PrivacyPreferenceFragment extends AbstractPreferenceFragment {

    private Preference clearHistory;

    @Override
    protected int getPreferencesXml() {
        return R.xml.privacy_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clearHistory = findPreference("clear_history");
        clearHistory.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == clearHistory) {
            preference.setEnabled(false);
            deleteHistory();
            preference.setEnabled(true);
            return true;
        }
        return super.onPreferenceClick(preference);
    }

    /**
     * Clear the history of addresses.
     */
    private void deleteHistory() {
        LocationApplication app = (LocationApplication) getActivity().getApplication();
        AddressProvider provider = app.getAddresses();
        provider.deleteAddresses();
        provider.deleteElevations();
        provider.deleteCities();
    }
}

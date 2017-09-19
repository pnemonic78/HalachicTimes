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

import net.sf.times.R;

import static net.sf.times.location.LocationPreferences.KEY_COORDS_FORMAT;
import static net.sf.times.preference.ZmanimPreferences.KEY_EMPHASIS_SCALE;
import static net.sf.times.preference.ZmanimPreferences.KEY_THEME;
import static net.sf.times.preference.ZmanimPreferences.KEY_THEME_COMPASS;

/**
 * This fragment shows the preferences for the Appearance header.
 */
public class AppearancePreferenceFragment extends net.sf.preference.AbstractPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.appearance_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList(KEY_COORDS_FORMAT);
        initList(KEY_THEME);
        initList(KEY_THEME_COMPASS);
        initList(KEY_EMPHASIS_SCALE);
    }
}

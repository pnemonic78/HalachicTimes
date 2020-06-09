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

import com.github.preference.PreferenceActivity;
import com.github.times.compass.lib.R;

import java.util.List;

import static com.github.preference.ThemePreferences.KEY_THEME;
import static com.github.times.compass.preference.CompassPreferences.KEY_THEME_COMPASS;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class CompassPreferenceActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean shouldRestartParentActivityForUi(String key) {
        return KEY_THEME.equals(key) || KEY_THEME_COMPASS.equals(key);
    }
}

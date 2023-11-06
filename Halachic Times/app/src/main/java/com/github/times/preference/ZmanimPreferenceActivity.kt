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

import static com.github.preference.LocalePreferences.KEY_LOCALE;
import static com.github.preference.ThemePreferences.KEY_THEME;
import static com.github.times.compass.preference.CompassPreferences.KEY_THEME_COMPASS;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.preference.LocalePreferences;
import com.github.preference.PreferenceActivity;
import com.github.times.compass.preference.MainPreferencesFragment;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferenceActivity extends PreferenceActivity {

    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);

        applyOverrideConfiguration(context.getResources().getConfiguration());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        localeCallbacks.onPreCreate(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected PreferenceFragmentCompat createMainFragment() {
        return new MainPreferencesFragment();
    }

    @Override
    protected boolean shouldRestartParentActivityForUi(String key) {
        return KEY_THEME.equals(key)
                || KEY_THEME_COMPASS.equals(key)
                || KEY_LOCALE.equals(key);
    }
}

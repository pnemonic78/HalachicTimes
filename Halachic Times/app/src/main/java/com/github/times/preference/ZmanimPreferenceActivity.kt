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
package com.github.times.preference

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.preference.LocalePreferences
import com.github.preference.PreferenceActivity
import com.github.preference.ThemePreferences
import com.github.times.compass.preference.CompassPreferences
import com.github.times.compass.preference.MainPreferencesFragment

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
open class ZmanimPreferenceActivity : PreferenceActivity() {
    private lateinit var localeCallbacks: LocaleCallbacks<LocalePreferences>

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
        applyOverrideConfiguration(context.resources.configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        localeCallbacks.onPreCreate(this)
        super.onCreate(savedInstanceState)
    }

    override fun createMainFragment(): PreferenceFragmentCompat {
        return MainPreferencesFragment()
    }

    override fun shouldRestartParentActivityForUi(key: String?): Boolean {
        return ThemePreferences.KEY_THEME == key
            || CompassPreferences.KEY_THEME_COMPASS == key
            || LocalePreferences.KEY_LOCALE == key
    }
}
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
package com.github.times.location

import android.content.Context
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.preference.LocalePreferences
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences

/**
 * Add a location by specifying its coordinates.
 *
 * @author Moshe Waisberg
 */
class ZmanimAddLocationActivity : AddLocationActivity<ZmanimPreferences>() {
    private lateinit var localeCallbacks: LocaleCallbacks<LocalePreferences>

    /** The preferences.  */
    private val preferences: ZmanimPreferences by lazy { SimpleZmanimPreferences(this) }

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
        applyOverrideConfiguration(context.resources.configuration)
    }

    override fun onPreCreate() {
        super.onPreCreate()
        localeCallbacks.onPreCreate(this)
    }

    override fun createThemeCallbacks(context: Context): ThemeCallbacks<ZmanimPreferences> {
        return SimpleThemeCallbacks(context, preferences)
    }
}
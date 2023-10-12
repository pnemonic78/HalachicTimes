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

import android.app.Activity
import android.content.Context
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.preference.LocalePreferences
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
class LocationActivity : LocationTabActivity<ZmanimPreferences>() {
    private var localeCallbacks: LocaleCallbacks<LocalePreferences>? = null

    /** The preferences.  */
    private var _preferences: ZmanimPreferences? = null
    val preferences: ZmanimPreferences
        get() {
            var preferences = _preferences
            if (preferences == null) {
                preferences = SimpleZmanimPreferences(this)
                this._preferences = preferences
            }
            return preferences
        }

    override fun attachBaseContext(newBase: Context) {
        val localeCallbacks: LocaleCallbacks<LocalePreferences> = LocaleHelper(newBase)
        this.localeCallbacks = localeCallbacks
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
        applyOverrideConfiguration(context.resources.configuration)
    }

    override fun onPreCreate() {
        super.onPreCreate()
        localeCallbacks?.onPreCreate(this)
    }

    override fun getAddLocationActivityClass(): Class<out Activity> {
        return ZmanimAddLocationActivity::class.java
    }

    override fun createThemeCallbacks(context: Context): ThemeCallbacks<ZmanimPreferences> {
        return SimpleThemeCallbacks(context, preferences)
    }
}
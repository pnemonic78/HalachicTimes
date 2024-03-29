/*
 * Copyright 2021, Moshe Waisberg
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
import com.github.preference.ThemePreferences.Values.THEME_DARK
import com.github.preference.ThemePreferences.Values.THEME_LIGHT
import com.github.times.compass.lib.R
import com.github.times.compass.preference.SimpleCompassPreferences
import com.github.times.preference.ZmanimPreferences.Values.THEME_WHITE

/**
 * Zmanim compass preferences implementation.
 *
 * @author Moshe Waisberg
 */
class ZmanimCompassPreferences(context: Context) : SimpleCompassPreferences(context) {

    init {
        SimpleZmanimPreferences.init(context)
    }

    override fun getTheme(value: String?): Int {
        return when (value) {
            THEME_DARK -> R.style.Theme_CompassApp_Dark
            THEME_LIGHT -> R.style.Theme_CompassApp_Light
            THEME_WHITE -> R.style.Theme_CompassApp_Light
            else -> R.style.Theme_CompassApp_DayNight
        }
    }
}
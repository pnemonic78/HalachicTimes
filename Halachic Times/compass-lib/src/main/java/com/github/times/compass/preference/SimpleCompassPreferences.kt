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
package com.github.times.compass.preference

import android.content.Context
import com.github.preference.SimpleThemePreferences
import com.github.preference.ThemePreferences.Values.THEME_DARK
import com.github.preference.ThemePreferences.Values.THEME_LIGHT
import com.github.times.compass.lib.R
import com.github.times.compass.preference.CompassPreferences.Values.BEARING_DEFAULT
import com.github.times.compass.preference.CompassPreferences.Values.BEARING_GREAT_CIRCLE
import com.github.times.compass.preference.CompassPreferences.Values.BEARING_RHUMB_LINE
import com.github.times.compass.preference.CompassPreferences.Values.SUMMARIES_DEFAULT
import com.github.times.compass.preference.CompassPreferences.Values.THEME_CLASSIC
import com.github.times.compass.preference.CompassPreferences.Values.THEME_COMPASS_DEFAULT
import com.github.times.compass.preference.CompassPreferences.Values.THEME_GOLD
import com.github.times.compass.preference.CompassPreferences.Values.THEME_ORIGINAL
import com.github.times.compass.preference.CompassPreferences.Values.THEME_SILVER

/**
 * Simple compass preferences implementation.
 *
 * @author Moshe Waisberg
 */
open class SimpleCompassPreferences(context: Context) : SimpleThemePreferences(context),
    ThemeCompassPreferences {

    init {
        init(context)
    }

    override val compassThemeValue: String?
        get() = preferences.getString(CompassPreferences.KEY_THEME_COMPASS, THEME_COMPASS_DEFAULT)

    override fun getCompassTheme(value: String?): Int {
        return when (value) {
            THEME_GOLD -> R.style.Theme_Compass_Gold
            THEME_SILVER -> R.style.Theme_Compass_Silver
            THEME_CLASSIC -> R.style.Theme_Compass_Classic
            else -> R.style.Theme_Compass_Original
        }
    }

    override val compassTheme: Int
        get() = getCompassTheme(compassThemeValue)

    override val bearing: String
        get() = preferences.getString(CompassPreferences.KEY_COMPASS_BEARING, BEARING_DEFAULT)
            .orEmpty()

    override val isSummariesVisible: Boolean
        get() = preferences.getBoolean(CompassPreferences.KEY_SUMMARIES, SUMMARIES_DEFAULT)

    override fun getTheme(value: String?): Int {
        return when (value) {
            THEME_DARK -> R.style.Theme_CompassApp_Dark
            THEME_LIGHT -> R.style.Theme_CompassApp_Light
            else -> R.style.Theme_CompassApp_DayNight
        }
    }

    companion object {
        /**
         * Initialize. Should be called only once when application created.
         *
         * @param context the context.
         */
        fun init(context: Context) {
            val res = context.resources
            SUMMARIES_DEFAULT =
                res.getBoolean(com.github.times.common.R.bool.summaries_visible_defaultValue)
            BEARING_DEFAULT = res.getString(R.string.compass_bearing_defaultValue)
            BEARING_GREAT_CIRCLE = res.getString(R.string.compass_bearing_value_circle)
            BEARING_RHUMB_LINE = res.getString(R.string.compass_bearing_value_rhumb)
            THEME_COMPASS_DEFAULT = res.getString(R.string.compass_theme_defaultValue)
            THEME_CLASSIC = res.getString(R.string.compass_theme_value_classic)
            THEME_GOLD = res.getString(R.string.compass_theme_value_gold)
            THEME_ORIGINAL = res.getString(R.string.compass_theme_value_original)
            THEME_SILVER = res.getString(R.string.compass_theme_value_silver)
        }
    }
}
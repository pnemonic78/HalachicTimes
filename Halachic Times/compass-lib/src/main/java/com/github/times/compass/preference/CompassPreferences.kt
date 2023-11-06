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

import androidx.annotation.StyleRes

/**
 * Compass preferences.
 *
 * @author Moshe Waisberg
 */
interface CompassPreferences {
    object Values {
        /** Default summaries hidden.  */
        @JvmField
        var SUMMARIES_DEFAULT = false

        /** The default bearing.  */
        @JvmField
        var BEARING_DEFAULT: String? = null

        /** Calculates the bearing for a Great Circle (shortest distance).  */
        @JvmField
        var BEARING_GREAT_CIRCLE: String? = null

        /** Calculates the bearing for a Rhumb Line (constant angle).  */
        @JvmField
        var BEARING_RHUMB_LINE: String? = null

        /** Original theme.  */
        @JvmField
        var THEME_ORIGINAL: String? = null

        /** Gold theme.  */
        @JvmField
        var THEME_GOLD: String? = null

        /** Silver theme.  */
        @JvmField
        var THEME_SILVER: String? = null

        /** Classic theme.  */
        @JvmField
        var THEME_CLASSIC: String? = null

        /** Default theme.  */
        @JvmField
        var THEME_COMPASS_DEFAULT: String? = null
    }

    /**
     * Get the theme value.
     *
     * @return the theme value.
     */
    val compassThemeValue: String?

    /**
     * Get the theme.
     *
     * @param value the theme value.
     * @return the theme resource id.
     * @see .getCompassThemeValue
     */
    @StyleRes
    fun getCompassTheme(value: String?): Int

    @get:StyleRes
    val compassTheme: Int

    /**
     * Get the type of bearing for calculating compass direction.
     *
     * @return the bearing type - either [Values.BEARING_GREAT_CIRCLE] or [Values.BEARING_RHUMB_LINE].
     */
    val bearing: String

    /**
     * Are summaries visible?
     *
     * @return `true` to show summaries.
     */
    val isSummariesVisible: Boolean

    companion object {
        /** Preference name for the compass bearing type.  */
        const val KEY_COMPASS_BEARING = "compass.bearing"

        /** Preference name for showing summaries.  */
        const val KEY_SUMMARIES = "summaries.visible"

        /** Preference name for the compass theme.  */
        const val KEY_THEME_COMPASS = "theme.compass"
    }
}
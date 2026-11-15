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
import com.github.preference.SimplePreferences
import com.github.times.location.LocationPreferences.Companion.KEY_COORDS_ELEVATION
import com.github.times.location.LocationPreferences.Companion.KEY_COORDS_FORMAT
import com.github.times.location.LocationPreferences.Values.Companion.ELEVATION_VISIBLE_DEFAULT
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_DECIMAL
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_DEFAULT
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_NONE
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_SEXAGESIMAL

/**
 * Simple location preferences implementation.
 *
 * @author Moshe Waisberg
 */
class SimpleLocationPreferences(context: Context) : SimplePreferences(context),
    LocationPreferences {

    init {
        init(context)
    }

    /**
     * Are coordinates visible?
     *
     * @return `true` to show coordinates.
     */
    override val isCoordinatesVisible: Boolean
        get() = FORMAT_NONE != coordinatesFormat

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    override val coordinatesFormat: String
        get() = preferences.getString(
            KEY_COORDS_FORMAT,
            FORMAT_DEFAULT
        ).orEmpty()

    override val isElevationVisible: Boolean
        get() = preferences.getBoolean(
            KEY_COORDS_ELEVATION,
            ELEVATION_VISIBLE_DEFAULT
        )

    companion object {
        /**
         * Initialize. Should be called only once when application created.
         *
         * @param context The context.
         */
        fun init(context: Context) {
            val res = context.resources
            FORMAT_DEFAULT =
                res.getString(R.string.coords_format_defaultValue)
            FORMAT_NONE =
                res.getString(R.string.coords_format_value_none)
            FORMAT_DECIMAL =
                res.getString(R.string.coords_format_value_decimal)
            FORMAT_SEXAGESIMAL =
                res.getString(R.string.coords_format_value_sexagesimal)
            ELEVATION_VISIBLE_DEFAULT =
                res.getBoolean(R.bool.coords_elevation_visible_defaultValue)
        }
    }
}
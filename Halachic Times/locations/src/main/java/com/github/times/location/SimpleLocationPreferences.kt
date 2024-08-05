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
import android.location.Location
import com.github.preference.SimplePreferences
import com.github.times.location.LocationPreferences.Companion.KEY_COORDS_ELEVATION
import com.github.times.location.LocationPreferences.Companion.KEY_COORDS_FORMAT
import com.github.times.location.LocationPreferences.Companion.KEY_ELEVATION
import com.github.times.location.LocationPreferences.Companion.KEY_LATITUDE
import com.github.times.location.LocationPreferences.Companion.KEY_LONGITUDE
import com.github.times.location.LocationPreferences.Companion.KEY_PROVIDER
import com.github.times.location.LocationPreferences.Companion.KEY_TIME
import com.github.times.location.LocationPreferences.Values.Companion.ELEVATION_VISIBLE_DEFAULT
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_DECIMAL
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_DEFAULT
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_NONE
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_SEXAGESIMAL
import timber.log.Timber

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

    override var location: Location?
        get() {
            if (!preferences.contains(KEY_LATITUDE)
                || !preferences.contains(KEY_LONGITUDE)
            ) {
                return null
            }
            val latitude: Double
            val longitude: Double
            val elevation: Double
            try {
                latitude = preferences.getString(KEY_LATITUDE, "0")!!.toDouble()
                longitude =
                    preferences.getString(KEY_LONGITUDE, "0")!!.toDouble()
                elevation =
                    preferences.getString(KEY_ELEVATION, "0")!!.toDouble()
            } catch (nfe: NumberFormatException) {
                Timber.e(nfe)
                return null
            }
            val provider = preferences.getString(KEY_PROVIDER, "")
            val location = Location(provider)
            location.latitude = latitude
            location.longitude = longitude
            location.altitude = elevation
            location.time = preferences.getLong(KEY_TIME, 0L)
            return location
        }
        set(value) = putLocation(value)

    private fun putLocation(location: Location?) {
        val editor = preferences.edit()
        if (location != null) {
            editor.putString(KEY_PROVIDER, location.provider)
                .putString(KEY_LATITUDE, location.latitude.toString())
                .putString(KEY_LONGITUDE, location.longitude.toString())
                .putString(
                    KEY_ELEVATION,
                    (if (location.hasAltitude()) location.altitude else 0.0).toString()
                )
                .putLong(KEY_TIME, location.time)
        } else {
            editor.remove(KEY_PROVIDER)
                .remove(KEY_LATITUDE)
                .remove(KEY_LONGITUDE)
                .remove(KEY_ELEVATION)
                .remove(KEY_TIME)
        }
        editor.apply()
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
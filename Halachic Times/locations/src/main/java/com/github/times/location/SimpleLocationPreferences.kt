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
            if (!preferences.contains(LocationPreferences.KEY_LATITUDE)
                || !preferences.contains(LocationPreferences.KEY_LONGITUDE)
            ) {
                return null
            }
            val latitude: Double
            val longitude: Double
            val elevation: Double
            try {
                latitude = preferences.getString(LocationPreferences.KEY_LATITUDE, "0")!!.toDouble()
                longitude =
                    preferences.getString(LocationPreferences.KEY_LONGITUDE, "0")!!.toDouble()
                elevation =
                    preferences.getString(LocationPreferences.KEY_ELEVATION, "0")!!.toDouble()
            } catch (nfe: NumberFormatException) {
                Timber.e(nfe)
                return null
            }
            val provider = preferences.getString(LocationPreferences.KEY_PROVIDER, "")
            val location = Location(provider)
            location.latitude = latitude
            location.longitude = longitude
            location.altitude = elevation
            location.time = preferences.getLong(LocationPreferences.KEY_TIME, 0L)
            return location
        }
        set(value) = putLocation(value)

    private fun putLocation(location: Location?) {
        val editor = preferences.edit()
        if (location != null) {
            editor.putString(LocationPreferences.KEY_PROVIDER, location.provider)
                .putString(
                    LocationPreferences.KEY_LATITUDE,
                    location.latitude.toString()
                )
                .putString(
                    LocationPreferences.KEY_LONGITUDE,
                    location.longitude.toString()
                )
                .putString(
                    LocationPreferences.KEY_ELEVATION,
                    (if (location.hasAltitude()) location.altitude else 0.0).toString()
                )
                .putLong(LocationPreferences.KEY_TIME, location.time)
        } else {
            editor.remove(LocationPreferences.KEY_PROVIDER)
                .remove(LocationPreferences.KEY_LATITUDE)
                .remove(LocationPreferences.KEY_LONGITUDE)
                .remove(LocationPreferences.KEY_ELEVATION)
                .remove(LocationPreferences.KEY_TIME)
        }
        editor.apply()
    }

    /**
     * Are coordinates visible?
     *
     * @return `true` to show coordinates.
     */
    override val isCoordinatesVisible: Boolean
        get() = LocationPreferences.Values.FORMAT_NONE != coordinatesFormat

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    override val coordinatesFormat: String
        get() = preferences.getString(
            LocationPreferences.KEY_COORDS_FORMAT,
            LocationPreferences.Values.FORMAT_DEFAULT
        ).orEmpty()

    override val isElevationVisible: Boolean
        get() = preferences.getBoolean(
            LocationPreferences.KEY_COORDS_ELEVATION,
            LocationPreferences.Values.ELEVATION_VISIBLE_DEFAULT
        )

    companion object {
        /**
         * Initialize. Should be called only once when application created.
         *
         * @param context The context.
         */
        fun init(context: Context) {
            val res = context.resources
            LocationPreferences.Values.FORMAT_DEFAULT =
                res.getString(R.string.coords_format_defaultValue)
            LocationPreferences.Values.FORMAT_NONE =
                res.getString(R.string.coords_format_value_none)
            LocationPreferences.Values.FORMAT_DECIMAL =
                res.getString(R.string.coords_format_value_decimal)
            LocationPreferences.Values.FORMAT_SEXAGESIMAL =
                res.getString(R.string.coords_format_value_sexagesimal)
            LocationPreferences.Values.ELEVATION_VISIBLE_DEFAULT =
                res.getBoolean(R.bool.coords_elevation_visible_defaultValue)
        }
    }
}
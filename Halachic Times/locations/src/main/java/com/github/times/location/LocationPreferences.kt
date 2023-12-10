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

import android.location.Location

/**
 * Location preferences.
 *
 * @author Moshe Waisberg
 */
interface LocationPreferences {
    open class Values {
        companion object {
            /** Default coordinates format.  */
            @JvmField
            var FORMAT_DEFAULT: String? = null

            /** Format the coordinates in decimal notation.  */
            @JvmField
            var FORMAT_NONE: String? = null

            /** Format the coordinates in decimal notation.  */
            @JvmField
            var FORMAT_DECIMAL: String? = null

            /** Format the coordinates in sexagesimal notation.  */
            @JvmField
            var FORMAT_SEXAGESIMAL: String? = null

            @JvmField
            var ELEVATION_VISIBLE_DEFAULT = false
        }
    }

    /**
     * Get the location.
     *
     * @return the location - `null` otherwise.
     */
    val location: Location?

    /**
     * Set the location.
     */
    fun putLocation(location: Location?)

    /**
     * Are coordinates visible?
     *
     * @return `true` to show coordinates.
     */
    val isCoordinatesVisible: Boolean

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    val coordinatesFormat: String

    /**
     * Are coordinates with elevation (altitude) visible?
     *
     * @return `true` to show coordinates with elevation.
     */
    val isElevationVisible: Boolean

    companion object {
        /** Preference name for the latitude.  */
        const val KEY_LATITUDE = "location.latitude"

        /** Preference name for the longitude.  */
        const val KEY_LONGITUDE = "location.longitude"

        /** Preference key for the elevation / altitude.  */
        const val KEY_ELEVATION = "location.altitude"

        /** Preference name for the location provider.  */
        const val KEY_PROVIDER = "location.provider"

        /** Preference name for the location time.  */
        const val KEY_TIME = "location.time"

        /** Preference name for the co-ordinates format.  */
        const val KEY_COORDS_FORMAT = "coords.format"

        /** Preference name for the co-ordinates with elevation/altitude.  */
        const val KEY_COORDS_ELEVATION = "coords.elevation"
    }
}
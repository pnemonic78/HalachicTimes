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
import com.kosherjava.zmanim.util.GeoLocation
import java.util.TimeZone
import kotlin.math.max

/**
 * Locations provider.
 *
 * @author Moshe Waisberg
 */
class ZmanimLocations(context: Context) : LocationsProvider(context) {
    /**
     * Get the location.
     *
     * @param timeZone the time zone.
     * @return the location - `null` otherwise.
     */
    fun getGeoLocation(timeZone: TimeZone): GeoLocation? {
        val location = getLocation() ?: return null
        val locationName = location.provider
        val latitude = location.latitude
        val longitude = location.longitude
        val elevation =
            if (location.hasAltitude()) max(GEOLOCATION_ELEVATION_MIN, location.altitude) else 0.0
        return GeoLocation(locationName, latitude, longitude, elevation, timeZone)
    }

    /**
     * Get the location for the time zone.
     *
     * @return the location - `null` otherwise.
     */
    val geoLocation: GeoLocation?
        get() = getGeoLocation(timeZone)

    companion object {
        //FIXME GEOLOCATION_ELEVATION_MIN = ELEVATION_MIN;
        private const val GEOLOCATION_ELEVATION_MIN = 0.0
    }
}
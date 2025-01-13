/*
 * Copyright 2025, Moshe Waisberg
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
package com.github.times.location.android

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.github.times.location.AddressResponseParser
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * built-in geocoder.
 *
 * @author Moshe Waisberg
 */
class AndroidGeocoder(private val context: Context, locale: Locale) : GeocoderBase(locale) {

    private val geocoder = Geocoder(context)

    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address> {
        return geocoder.getFromLocation(latitude, longitude, maxResults) ?: emptyList()
    }

    override fun createAddressResponseParser(): AddressResponseParser {
        throw NotImplementedError("Nothing to do here!")
    }

    override fun getElevation(latitude: Double, longitude: Double): Location? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val location = Location("")
            location.latitude = latitude
            location.longitude = longitude
            location.altitude = 0.0

            val altitudeConverter = android.location.altitude.AltitudeConverter()
            altitudeConverter.addMslAltitudeToLocation(context, location)
            if (location.hasMslAltitude()) {
                location.altitude = -location.mslAltitudeMeters
                return location
            }
        }
        return null
    }

    override fun createElevationResponseParser(): ElevationResponseParser {
        throw NotImplementedError("Nothing to do here!")
    }

}
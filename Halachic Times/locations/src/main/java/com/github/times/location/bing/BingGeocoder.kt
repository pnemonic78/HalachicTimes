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
package com.github.times.location.bing

import android.location.Address
import android.location.Location
import com.github.times.location.AddressResponseParser
import com.github.times.location.BuildConfig
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import java.io.IOException
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Microsoft Bing API.
 *
 *
 * [http://msdn.
 * microsoft.com/en-us/library/ff701710.aspx](http://msdn.microsoft.com/en-us/library/ff701710.aspx)
 *
 * @author Moshe Waisberg
 */
class BingGeocoder(locale: Locale) : GeocoderBase(locale) {

    @Throws(IOException::class)
    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address> {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (API_KEY.isEmpty()) return emptyList()
        val queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, language, API_KEY)
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults)
    }

    override fun createAddressResponseParser(): AddressResponseParser {
        return BingAddressResponseParser()
    }

    @Throws(IOException::class)
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (API_KEY.isEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY)
        return getJsonElevationFromURL(latitude, longitude, queryUrl)
    }

    @Throws(LocationException::class)
    override fun createElevationResponseParser(): ElevationResponseParser {
        return BingElevationResponseParser()
    }

    companion object {
        /**
         * URL that accepts latitude and longitude coordinates as parameters.
         */
        private const val URL_LATLNG =
            "https://dev.virtualearth.net/REST/v1/Locations/%f,%f?o=json&c=%s&key=%s"

        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.
         */
        private const val URL_ELEVATION =
            "https://dev.virtualearth.net/REST/v1/Elevation/List?o=json&points=%f,%f&key=%s"

        /**
         * Bing API key.
         */
        private val API_KEY = decodeApiKey(BuildConfig.BING_API_KEY)
    }
}
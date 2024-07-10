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
package com.github.times.location.geonames

import android.location.Address
import android.location.Location
import com.github.times.location.AddressResponseParser
import com.github.times.location.BuildConfig
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import com.github.times.location.TextElevationResponseParser
import java.io.IOException
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * GeoNames WebServices API.
 *
 * [http://www.geonames.org/export/web-services.html](http://www.geonames.org/export/web-services.html)
 *
 * @author Moshe Waisberg
 */
class GeoNamesGeocoder(locale: Locale) : GeocoderBase(locale) {

    @Throws(IOException::class)
    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address> {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (USERNAME.isEmpty()) return emptyList()
        val queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, language, USERNAME)
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults)
    }

    @Throws(LocationException::class)
    override fun createAddressResponseParser(): AddressResponseParser {
        return GeoNamesAddressResponseParser()
    }

    @Throws(IOException::class)
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (USERNAME.isNullOrEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_ELEVATION_SRTM3, latitude, longitude, USERNAME)
        return getTextElevationFromURL(latitude, longitude, queryUrl)
    }

    @Throws(LocationException::class)
    override fun createElevationResponseParser(): ElevationResponseParser {
        return TextElevationResponseParser()
    }

    companion object {
        /**
         * URL that accepts latitude and longitude coordinates as parameters.
         */
        private const val URL_LATLNG =
            "https://secure.geonames.org/findNearbyJSON?lat=%f&lng=%f&lang=%s&username=%s"

        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.<br></br>
         * Uses Shuttle Radar Topography Mission (SRTM) elevation data.
         */
        private const val URL_ELEVATION_SRTM3 =
            "https://secure.geonames.org/srtm3?lat=%f&lng=%f&username=%s"

        /**
         * GeoNames user name.
         */
        private val USERNAME = decodeApiKey(BuildConfig.GEONAMES_USERNAME)
    }
}
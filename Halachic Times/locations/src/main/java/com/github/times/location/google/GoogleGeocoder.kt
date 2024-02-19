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
package com.github.times.location.google

import android.location.Address
import android.location.Location
import com.github.times.location.AddressResponseParser
import com.github.times.location.BuildConfig
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import com.github.times.location.bing.BingGeocoder
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Google Geocoding API.
 *
 *
 * [http://
 * code.google.com/apis/maps/documentation/geocoding/](http://code.google.com/apis/maps/documentation/geocoding/)
 *
 * @author Moshe Waisberg
 */
class GoogleGeocoder(locale: Locale) : GeocoderBase(locale) {
    @Throws(IOException::class)
    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address>? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (API_KEY.isNullOrEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, language, API_KEY)
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults)
    }

    @Throws(IOException::class)
    override fun getFromLocationName(locationName: String, maxResults: Int): List<Address>? {
        if (API_KEY.isNullOrEmpty()) return null
        val queryUrl = String.format(
            Locale.US,
            URL_ADDRESS,
            URLEncoder.encode(locationName, StandardCharsets.UTF_8.name()),
            language,
            API_KEY
        )
        return getJsonAddressesFromURL(0.0, 0.0, queryUrl, maxResults)
    }

    @Throws(IOException::class)
    override fun getFromLocationName(
        locationName: String, maxResults: Int,
        lowerLeftLatitude: Double, lowerLeftLongitude: Double,
        upperRightLatitude: Double, upperRightLongitude: Double
    ): List<Address>? {
        require(lowerLeftLatitude in LATITUDE_MIN..LATITUDE_MAX) { "lowerLeftLatitude == $lowerLeftLatitude" }
        require(lowerLeftLongitude in LONGITUDE_MIN..LONGITUDE_MAX) { "lowerLeftLongitude == $lowerLeftLongitude" }
        require(upperRightLatitude in LATITUDE_MIN..LATITUDE_MAX) { "upperRightLatitude == $upperRightLatitude" }
        require(upperRightLongitude in LONGITUDE_MIN..LONGITUDE_MAX) { "upperRightLongitude == $upperRightLongitude" }
        if (API_KEY.isNullOrEmpty()) return null
        val queryUrl = String.format(
            Locale.US,
            URL_ADDRESS_BOUNDED,
            URLEncoder.encode(locationName, StandardCharsets.UTF_8.name()),
            lowerLeftLatitude,
            lowerLeftLongitude,
            upperRightLatitude,
            upperRightLongitude,
            language,
            API_KEY
        )
        val latitude = (lowerLeftLatitude + upperRightLatitude) * 0.5
        val longitude = (lowerLeftLongitude + upperRightLongitude) * 0.5
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults)
    }

    @Throws(LocationException::class)
    override fun createAddressResponseParser(): AddressResponseParser {
        return GoogleAddressResponseParser()
    }

    @Throws(IOException::class)
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (API_KEY.isNullOrEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY)
        return getJsonElevationFromURL(latitude, longitude, queryUrl)
    }

    @Throws(LocationException::class)
    override fun createElevationResponseParser(): ElevationResponseParser {
        return GoogleElevationResponseParser()
    }

    companion object {
        /**
         * URL that accepts latitude and longitude coordinates as parameters.
         */
        private const val URL_LATLNG =
            "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=%s&key=%s&sensor=true"

        /**
         * URL that accepts an address as parameters.
         */
        private const val URL_ADDRESS =
            "https://maps.googleapis.com/maps/api/geocode/json?address=%s&language=%s&key=%s&sensor=true"

        /**
         * URL that accepts a bounded address as parameters.
         */
        private const val URL_ADDRESS_BOUNDED =
            "https://maps.googleapis.com/maps/api/geocode/json?address=%s&bounds=%f,%f|%f,%f&language=%s&key=%s&sensor=true"

        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.
         */
        private const val URL_ELEVATION =
            "https://maps.googleapis.com/maps/api/elevation/json?locations=%f,%f&key=%s"

        /**
         * Google API key.
         */
        private val API_KEY = decodeApiKey(BuildConfig.GOOGLE_API_KEY)
    }
}
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

import android.location.Address
import android.location.Location
import android.location.Location.distanceBetween
import android.util.Base64
import androidx.annotation.FloatRange
import com.github.net.HTTPReader
import com.github.net.HTTPReader.read
import com.github.util.LocaleUtils
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding.
 * @param locale the locale.
 * @author Moshe Waisberg
 */
abstract class GeocoderBase(protected val locale: Locale) {

    /**
     * Get a parser for addresses.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    @get:Throws(LocationException::class)
    internal val addressResponseParser: AddressResponseParser by lazy { createAddressResponseParser() }

    /**
     * Get a parser for elevations.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    @get:Throws(LocationException::class)
    internal val elevationResponseParser: ElevationResponseParser by lazy { createElevationResponseParser() }

    /**
     * Returns an array of Addresses that are known to describe the area
     * immediately surrounding the given latitude and longitude.
     *
     * @param latitude   the latitude a point for the search.
     * @param longitude  the longitude a point for the search.
     * @param maxResults maximum number of addresses to return. Smaller numbers (1 to
     * 5) are recommended.
     * @return a list of addresses. Returns empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    abstract fun getFromLocation(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        maxResults: Int
    ): List<Address>

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     *
     *
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName a user-supplied description of a location.
     * @param maxResults   max number of addresses to return. Smaller numbers (1 to 5)
     * are recommended.
     * @return a list of addresses. Returns empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    open fun getFromLocationName(locationName: String, maxResults: Int): List<Address> {
        return emptyList()
    }

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     *
     *
     * You may specify a bounding box for the search results by including the
     * Latitude and Longitude of the Lower Left point and Upper Right point of
     * the box.
     *
     *
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName        a user-supplied description of a location.
     * @param maxResults          max number of addresses to return. Smaller numbers (1 to 5)
     * are recommended.
     * @param lowerLeftLatitude   the latitude of the lower left corner of the bounding box.
     * @param lowerLeftLongitude  the longitude of the lower left corner of the bounding box.
     * @param upperRightLatitude  the latitude of the upper right corner of the bounding box.
     * @param upperRightLongitude the longitude of the upper right corner of the bounding box.
     * @return a list of addresses. Returns empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    open fun getFromLocationName(
        locationName: String,
        maxResults: Int,
        @FloatRange(from = -90.0, to = 90.0) lowerLeftLatitude: Double,
        @FloatRange(from = -180.0, to = 180.0) lowerLeftLongitude: Double,
        @FloatRange(from = -90.0, to = 90.0) upperRightLatitude: Double,
        @FloatRange(from = -180.0, to = 180.0) upperRightLongitude: Double
    ): List<Address?> {
        require(lowerLeftLatitude in LATITUDE_MIN..LATITUDE_MAX) { "lowerLeftLatitude == $lowerLeftLatitude" }
        require(lowerLeftLongitude in LONGITUDE_MIN..LONGITUDE_MAX) { "lowerLeftLongitude == $lowerLeftLongitude" }
        require(upperRightLatitude in LATITUDE_MIN..LATITUDE_MAX) { "upperRightLatitude == $upperRightLatitude" }
        require(upperRightLongitude in LONGITUDE_MIN..LONGITUDE_MAX) { "upperRightLongitude == $upperRightLongitude" }
        return emptyList()
    }

    /**
     * Get the address by parsing the XML results.
     *
     * @param latitude   the requested latitude.
     * @param longitude  the requested longitude.
     * @param queryUrl   the URL.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    protected fun getJsonAddressesFromURL(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        queryUrl: String,
        maxResults: Int
    ): List<Address> {
        val url = URL(queryUrl)
        read(url, HTTPReader.CONTENT_JSON).use { data ->
            return parseAddresses(
                data,
                latitude,
                longitude,
                locale,
                maxResults
            )
        }
    }

    /**
     * Parse the JSON response for addresses.
     *
     * @param data       the JSON data.
     * @param latitude   the requested latitude.
     * @param longitude  the requested longitude.
     * @param locale     the locale.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    protected fun parseAddresses(
        data: InputStream?,
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        locale: Locale,
        maxResults: Int
    ): List<Address> {
        // Minimum length for either "<>" or "{}"
        if (data == null || data.available() <= 2) {
            return emptyList()
        }
        val parser = addressResponseParser
        return parser.parse(data, latitude, longitude, maxResults, locale)
    }

    /**
     * Create a parser for addresses.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    @Throws(LocationException::class)
    protected abstract fun createAddressResponseParser(): AddressResponseParser

    /**
     * Get the ISO 639 language code.
     *
     * @return the language code.
     */
    protected val language: String
        get() {
            val language = locale.language
            return when (language) {
                "in" -> "id"
                LocaleUtils.ISO639_HEBREW_JAVA -> LocaleUtils.ISO639_HEBREW_JAVA
                LocaleUtils.ISO639_YIDDISH_JAVA -> LocaleUtils.ISO639_YIDDISH
                else -> language
            }
        }

    /**
     * Get the location with elevation.
     *
     * @param latitude  the latitude a point for the search.
     * @param longitude the longitude a point for the search.
     * @return the location - `null` otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    abstract fun getElevation(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double
    ): Location?

    /**
     * Parse the XML response for an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the XML data.
     * @return the location - `null` otherwise.
     * @throws LocationException if a location error occurs.
     * @throws IOException       if an I/O error occurs.
     */
    @Throws(LocationException::class, IOException::class)
    protected fun parseElevation(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        data: InputStream?
    ): Location? {
        // Minimum length for either "<>" or "{}"
        if (data == null || data.available() <= 2) {
            return null
        }
        val parser = elevationResponseParser
        val results = parser.parse(data, latitude, longitude, 1)
        return if (results.isEmpty()) null else results[0]
    }

    /**
     * Get the elevation by parsing the plain text result.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - `null` otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    @Throws(IOException::class)
    protected fun getTextElevationFromURL(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        queryUrl: String
    ): Location? {
        val url = URL(queryUrl)
        read(url).use { data -> return parseElevation(latitude, longitude, data) }
    }

    /**
     * Get the elevation by parsing the JSON results.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - `null` otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    protected fun getJsonElevationFromURL(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        queryUrl: String
    ): Location? {
        val url = URL(queryUrl)
        read(url, HTTPReader.CONTENT_JSON).use { data ->
            return parseElevation(latitude, longitude, data)
        }
    }

    /**
     * Create a handler to parse elevations.
     *
     * @return the handler.
     * @throws LocationException if a location error occurs.
     */
    @Throws(LocationException::class)
    protected abstract fun createElevationResponseParser(): ElevationResponseParser

    companion object {
        /**
         * The "user pick a city" location provider.
         */
        const val USER_PROVIDER = "user"

        /**
         * Maximum radius to consider a location near the same street.
         */
        internal const val SAME_STREET = 250f // 250 metres.

        /**
         * Maximum radius to consider a location near the same neighbourhood.
         */
        internal const val SAME_HOOD = 1000f // 1 kilometre.

        /**
         * Maximum radius to consider a location near the same city.
         *
         * New York city, USA, is <tt>8,683 km<sup>2</sup></tt>, thus radius is
         * about <tt>37.175 km</tt>.<br></br>
         * Johannesburg/East Rand, ZA, is <tt>2,396 km<sup>2</sup></tt>, thus radius
         * is about <tt>19.527 km</tt>..<br></br>
         * Cape Town, ZA, is <tt>686 km<sup>2</sup></tt>, thus radius is about
         * <tt>10.449 km</tt>.
         */
        internal const val SAME_CITY = 15000f // 15 kilometres.

        /**
         * Maximum radius to consider a location near the same plateau with similar terrain.
         */
        internal const val SAME_PLATEAU = 150000f // 150 kilometres.

        /**
         * Maximum radius to consider a location near the same planet.
         */
        internal const val SAME_PLANET = 6000000f // 6000 kilometres.

        const val LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN
        const val LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX
        const val LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN
        const val LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX

        internal fun decodeApiKey(encoded: String): String {
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            return String(bytes, StandardCharsets.UTF_8)
        }

        /**
         * Find the nearest address.
         *
         * @param latitude  the latitude.
         * @param longitude  the longitude.
         * @param addresses the list of addresses.
         * @param radius    the maximum radius.
         * @return the best address - `null` otherwise.
         */
        fun findNearestAddress(
            latitude: Double,
            longitude: Double,
            addresses: List<Address>,
            radius: Float
        ): Address? {
            if (addresses.isEmpty()) {
                return null
            }

            var distanceMin = radius
            var candidate: Address? = null
            val distances = FloatArray(1)
            for (a in addresses) {
                if (!a.hasLatitude() || !a.hasLongitude()) continue
                distanceBetween(latitude, longitude, a.latitude, a.longitude, distances)
                if (distances[0] <= distanceMin) {
                    distanceMin = distances[0]
                    candidate = a
                }
            }
            return candidate
        }
    }
}
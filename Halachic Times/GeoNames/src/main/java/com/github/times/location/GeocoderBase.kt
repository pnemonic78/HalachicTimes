package com.github.times.location

import com.github.location.Location
import com.github.location.LocationException
import com.github.net.HTTPReader
import java.io.IOException
import java.io.InputStream
import java.net.URL

abstract class GeocoderBase {

    /**
     * Get a parser for elevations.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    @get:Throws(LocationException::class)
    internal val elevationResponseParser: ElevationResponseParser by lazy { createElevationResponseParser() }

    /**
     * Get the location with elevation.
     *
     * @param latitude  the latitude a point for the search.
     * @param longitude the longitude a point for the search.
     * @return the location - `null` otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     * @throws LocationException if a location error occurs.
     */
    @Throws(IOException::class, LocationException::class)
    abstract fun getElevation(latitude: Double, longitude: Double): Location?

    /**
     * Get the elevation by parsing the plain text result.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - `null` otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     * @throws LocationException if a location error occurs.
     */
    @Throws(IOException::class, LocationException::class)
    protected fun getTextElevationFromURL(
        latitude: Double,
        longitude: Double,
        queryUrl: String
    ): Location? {
        val url = URL(queryUrl)
        HTTPReader.read(url).use { data ->
            return parseElevation(latitude, longitude, data)
        }
    }

    /**
     * Get the elevation by parsing the JSON results.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - `null` otherwise.
     * @throws IOException if an I/O error occurs.
     * @throws LocationException if a location error occurs.
     */
    @Throws(IOException::class, LocationException::class)
    protected fun getJsonElevationFromURL(
        latitude: Double,
        longitude: Double,
        queryUrl: String
    ): Location? {
        val url = URL(queryUrl)
        HTTPReader.read(url, HTTPReader.CONTENT_JSON).use { data ->
            return parseElevation(latitude, longitude, data)
        }
    }

    /**
     * Parse the XML response for an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the XML data.
     * @return the location - `null` otherwise.
     * @throws IOException       if an I/O error occurs.
     * @throws LocationException if a location error occurs.
     */
    @Throws(IOException::class, LocationException::class)
    protected fun parseElevation(
        latitude: Double,
        longitude: Double,
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
     * Create a handler to parse elevations.
     *
     * @return the handler.
     * @throws LocationException if a location error occurs.
     */
    @Throws(LocationException::class)
    protected abstract fun createElevationResponseParser(): ElevationResponseParser

    companion object {
        const val USER_PROVIDER = "user"
    }
}
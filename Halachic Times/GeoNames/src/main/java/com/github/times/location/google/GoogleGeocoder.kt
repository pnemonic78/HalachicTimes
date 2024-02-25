package com.github.times.location.google

import com.github.location.Location
import com.github.location.Location.Companion.LATITUDE_MAX
import com.github.location.Location.Companion.LATITUDE_MIN
import com.github.location.Location.Companion.LONGITUDE_MAX
import com.github.location.Location.Companion.LONGITUDE_MIN
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import java.util.Locale

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Google Geocoding API.
 *
 * [geocoding](https://developers.google.com/maps/documentation/geocoding)
 *
 * @author Moshe Waisberg
 */
class GoogleGeocoder : GeocoderBase() {
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (API_KEY.isNullOrEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY)
        return getJsonElevationFromURL(latitude, longitude, queryUrl)
    }

    override fun createElevationResponseParser(): ElevationResponseParser {
        return GoogleElevationResponseParser()
    }

    companion object {
        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.
         */
        private const val URL_ELEVATION =
            "https://maps.googleapis.com/maps/api/elevation/json?locations=%f,%f&key=%s"

        /**
         * Google API key.
         */
        private val API_KEY = System.getProperty("GOOGLE_API_KEY")
    }
}
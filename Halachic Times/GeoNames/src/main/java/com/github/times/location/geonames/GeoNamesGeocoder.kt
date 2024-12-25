package com.github.times.location.geonames

import com.github.location.Location
import com.github.location.Location.Companion.LATITUDE_MAX
import com.github.location.Location.Companion.LATITUDE_MIN
import com.github.location.Location.Companion.LONGITUDE_MAX
import com.github.location.Location.Companion.LONGITUDE_MIN
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.TextElevationResponseParser
import java.util.Locale

class GeoNamesGeocoder : GeocoderBase() {
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        if (USERNAME.isNullOrEmpty()) return null
        val queryUrl = String.format(Locale.US, URL_ELEVATION_SRTM3, latitude, longitude, USERNAME)
        return getTextElevationFromURL(latitude, longitude, queryUrl)
    }

    override fun createElevationResponseParser(): ElevationResponseParser {
        return TextElevationResponseParser()
    }

    companion object {
        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.<br/>
         * Uses Shuttle Radar Topography Mission (SRTM) elevation data.
         */
        private const val URL_ELEVATION_SRTM3 =
            "https://secure.geonames.org/srtm3?lat=%f&lng=%f&username=%s"

        /**
         * GeoNames user name.
         */
        private val USERNAME = System.getProperty("GEONAMES_USERNAME")
    }
}
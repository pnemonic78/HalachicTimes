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
import com.github.io.StreamUtils.toString
import java.io.IOException
import java.io.InputStream
import timber.log.Timber

/**
 * Handler for parsing the textual elevation response.
 *
 * @author Moshe Waisberg
 */
class TextElevationResponseParser : ElevationResponseParser() {
    @Throws(LocationException::class, IOException::class)
    override fun parse(
        data: InputStream,
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Location> {
        val text = toString(data)
        if (text.isEmpty()) {
            throw LocationException("empty elevation")
        }
        val results = mutableListOf<Location>()
        val location = toLocation(text, latitude, longitude)
        if (location != null) {
            results.add(location)
        }
        return results
    }

    @Throws(LocationException::class)
    private fun toLocation(response: String, latitude: Double, longitude: Double): Location? {
        return try {
            val elevation = response.toDouble()
            if (elevation <= ELEVATION_LOWEST_SURFACE) {
                Timber.w("elevation too low: %s", response)
                return null
            }
            if (elevation >= ELEVATION_SPACE) {
                Timber.w("elevation too high: %s", response)
                return null
            }
            Location(GeocoderBase.USER_PROVIDER).apply {
                this.time = System.currentTimeMillis()
                this.latitude = latitude
                this.longitude = longitude
                this.altitude = elevation
            }
        } catch (e: NumberFormatException) {
            Timber.e(e, "Bad elevation: [$response] at $latitude,$longitude")
            throw LocationException(e)
        }
    }

    companion object {
        /**
         * Lowest possible natural elevation on the surface of the earth.
         */
        private const val ELEVATION_LOWEST_SURFACE = ZmanimLocation.ELEVATION_MIN

        /**
         * Highest possible natural elevation from the surface of the earth.
         */
        private const val ELEVATION_SPACE = ZmanimLocation.ELEVATION_MAX
    }
}
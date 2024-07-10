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
import androidx.annotation.FloatRange

/**
 * Location formatter.
 *
 * @author Moshe Waisberg
 */
interface LocationFormatter {
    /**
     * Format the coordinates.
     *
     * @param location
     * the location.
     * @return the coordinates text.
     */
    fun formatCoordinates(location: Location): String

    /**
     * Format the coordinates.
     *
     * @param address
     * the address.
     * @return the coordinates text.
     */
    fun formatCoordinates(address: Address): String

    /**
     * Format the coordinates.
     *
     * @param latitude
     * the latitude.
     * @param longitude
     * the longitude.
     * @param elevation
     * the elevation or altitude.
     * @return the coordinates text.
     */
    fun formatCoordinates(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        elevation: Double
    ): String

    /**
     * Format a latitude coordinate.
     *
     * @param latitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLatitude(@FloatRange(from = -90.0, to = 90.0) latitude: Double): String

    /**
     * Format a latitude coordinate using the decimal notation.
     *
     * @param latitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLatitudeDecimal(@FloatRange(from = -90.0, to = 90.0) latitude: Double): String

    /**
     * Format a latitude coordinate using the sexagesimal notation.
     *
     * @param latitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLatitudeSexagesimal(@FloatRange(from = -90.0, to = 90.0) latitude: Double): String

    /**
     * Format a longitude coordinate.
     *
     * @param longitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLongitude(@FloatRange(from = -180.0, to = 180.0) longitude: Double): String

    /**
     * Format a longitude coordinate using the decimal notation.
     *
     * @param longitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLongitudeDecimal(@FloatRange(from = -180.0, to = 180.0) longitude: Double): String

    /**
     * Format a longitude coordinate using the sexagesimal notation.
     *
     * @param longitude
     * the coordinate.
     * @return the coordinate text.
     */
    fun formatLongitudeSexagesimal(@FloatRange(from = -180.0, to = 180.0) longitude: Double): String

    /**
     * Format an elevation.
     *
     * @param elevation
     * the elevation.
     * @return the elevation text.
     */
    fun formatElevation(elevation: Double): String

    /**
     * Format a azimuth (bearing or yaw or compass angle).
     *
     * @param azimuth
     * the azimuth, in degrees.
     * @return the azimuth text.
     */
    fun formatBearing(azimuth: Double): String

    /**
     * Format a azimuth (bearing or yaw or compass angle) using the decimal notation.
     *
     * @param azimuth
     * the azimuth, in degrees.
     * @return the azimuth text.
     */
    fun formatBearingDecimal(azimuth: Double): String

    /**
     * Format a azimuth (bearing or yaw or compass angle) using the sexagesimal notation.
     *
     * @param azimuth
     * the azimuth, in degrees.
     * @return the azimuth text.
     */
    fun formatBearingSexagesimal(azimuth: Double): String

    /**
     * Parse a latitude.
     * @return the latitude, in degrees - `Double.NaN` otherwise.
     */
    fun parseLatitude(coordinate: String): Double

    /**
     * Parse a longitude.
     * @return the longitude, in degrees - `Double.NaN` otherwise.
     */
    fun parseLongitude(coordinate: String): Double
}
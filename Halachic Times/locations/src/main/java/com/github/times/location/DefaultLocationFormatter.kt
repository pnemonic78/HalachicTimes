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

import android.content.Context
import android.location.Address
import android.location.Location
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_SEXAGESIMAL
import com.github.util.getDefaultLocale
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Default location formatter.
 *
 * @author Moshe Waisberg
 */
open class DefaultLocationFormatter(
    /**
     * The context.
     */
    context: Context,
    /**
     * The format notation.
     *
     * @see com.github.times.location.LocationPreferences.Values.FORMAT_DECIMAL
     * @see com.github.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL
     */
    protected val notation: String,
    protected val isElevationVisible: Boolean
) : LocationFormatter {
    /**
     * The coordinates format for decimal format.
     */
    private val formatDecimal: String = context.getString(R.string.location_decimal)

    /**
     * The coordinates format for decimal format with elevation.
     */
    private val formatDecimalElevation: String =
        context.getString(R.string.location_decimal_with_elevation)

    /**
     * The coordinates format for sexagesimal format.
     */
    private val formatSexagesimal: String = context.getString(R.string.location_sexagesimal)

    /**
     * The coordinates format for sexagesimal format with elevation.
     */
    private val formatSexagesimalElevation: String =
        context.getString(R.string.location_sexagesimal_with_elevation)

    /**
     * The elevation format.
     */
    private val formatElevation: String = context.getString(R.string.location_elevation)

    /**
     * The bearing/yaw format for decimal format.
     */
    private val formatBearingDecimal: DecimalFormat = DecimalFormat("###.#\u00B0")

    override fun formatCoordinates(location: Location): String {
        val latitude = location.latitude
        val longitude = location.longitude
        val elevation = location.altitude
        return formatCoordinates(latitude, longitude, elevation)
    }

    override fun formatCoordinates(address: Address): String {
        val latitude = address.latitude
        val longitude = address.longitude
        var elevation = 0.0
        if (address is ZmanimAddress) {
            if (address.hasElevation()) {
                elevation = address.elevation
            }
        }
        return formatCoordinates(latitude, longitude, elevation)
    }

    override fun formatCoordinates(latitude: Double, longitude: Double, elevation: Double): String {
        val elevated = isElevationVisible && !elevation.isNaN()
        return if (FORMAT_SEXAGESIMAL == notation) {
            formatCoordinatesSexagesimal(latitude, longitude, elevation, elevated)
        } else {
            formatCoordinatesDecimal(latitude, longitude, elevation, elevated)
        }
    }

    protected fun formatCoordinatesDecimal(
        latitude: Double,
        longitude: Double,
        elevation: Double,
        withElevation: Boolean
    ): String {
        val latitudeText: CharSequence = formatLatitudeDecimal(latitude)
        val longitudeText: CharSequence = formatLongitudeDecimal(longitude)

        if (withElevation) {
            val elevationText: CharSequence = formatElevation(elevation)
            return String.format(
                Locale.US,
                formatDecimalElevation,
                latitudeText,
                longitudeText,
                elevationText
            )
        }
        return String.format(Locale.US, formatDecimal, latitudeText, longitudeText)
    }

    protected fun formatCoordinatesSexagesimal(
        latitude: Double,
        longitude: Double,
        elevation: Double,
        withElevation: Boolean
    ): String {
        val latitudeText: CharSequence = formatLatitudeSexagesimal(latitude)
        val longitudeText: CharSequence = formatLongitudeSexagesimal(longitude)
        val elevationText: CharSequence = formatElevation(elevation)

        if (withElevation) {
            return String.format(
                Locale.US,
                formatSexagesimalElevation,
                latitudeText,
                longitudeText,
                elevationText
            )
        }
        return String.format(Locale.US, formatSexagesimal, latitudeText, longitudeText)
    }

    override fun formatLatitude(latitude: Double): String {
        return if (FORMAT_SEXAGESIMAL == notation) {
            formatLatitudeSexagesimal(latitude)
        } else {
            formatLatitudeDecimal(latitude)
        }
    }

    override fun formatLatitudeDecimal(latitude: Double): String {
        return Location.convert(latitude, Location.FORMAT_DEGREES)
    }

    override fun formatLatitudeSexagesimal(latitude: Double): String {
        return Location.convert(abs(latitude), Location.FORMAT_SECONDS)
    }

    override fun formatLongitude(longitude: Double): String {
        return if (FORMAT_SEXAGESIMAL == notation) {
            formatLongitudeSexagesimal(longitude)
        } else {
            formatLongitudeDecimal(longitude)
        }
    }

    override fun formatLongitudeDecimal(longitude: Double): String {
        return Location.convert(longitude, Location.FORMAT_DEGREES)
    }

    override fun formatLongitudeSexagesimal(longitude: Double): String {
        return Location.convert(abs(longitude), Location.FORMAT_SECONDS)
    }

    override fun formatElevation(elevation: Double): String {
        return String.format(Locale.US, formatElevation, elevation)
    }

    /**
     * Get the locale for formatting.
     *
     * @return the context's locale.
     */
    protected val locale: Locale = context.getDefaultLocale()

    override fun formatBearing(azimuth: Double): String {
        return if (FORMAT_SEXAGESIMAL == notation) {
            formatBearingSexagesimal(azimuth)
        } else {
            formatBearingDecimal(azimuth)
        }
    }

    override fun formatBearingDecimal(azimuth: Double): String {
        return formatBearingDecimal.format((azimuth + 360) % 360)
    }

    override fun formatBearingSexagesimal(azimuth: Double): String {
        return formatBearingDecimal.format((azimuth + 360) % 360)
    }

    override fun parseLatitude(coordinate: String): Double {
        if (coordinate.isEmpty()) return Double.NaN
        try {
            val value = Location.convert(coordinate)
            if (value < LocationsProvider.LATITUDE_MIN) return Double.NaN
            if (value > LocationsProvider.LATITUDE_MAX) return Double.NaN
            return value
        } catch (ignored: IllegalArgumentException) {
        }
        return Double.NaN
    }

    override fun parseLongitude(coordinate: String): Double {
        if (coordinate.isEmpty()) return Double.NaN
        try {
            val value = Location.convert(coordinate)
            if (value < LocationsProvider.LONGITUDE_MIN) return Double.NaN
            if (value > LocationsProvider.LONGITUDE_MAX) return Double.NaN
            return value
        } catch (ignored: IllegalArgumentException) {
        }
        return Double.NaN
    }
}
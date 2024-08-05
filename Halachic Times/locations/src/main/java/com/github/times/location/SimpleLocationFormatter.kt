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
import com.github.times.location.LocationPreferences.Values.Companion.FORMAT_SEXAGESIMAL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Simple location formatter.
 *
 * @author Moshe Waisberg
 */
class SimpleLocationFormatter(context: Context, notation: String, isElevationVisible: Boolean) :
    DefaultLocationFormatter(context, notation, isElevationVisible) {

    private val symbolNorth: String = context.getString(R.string.north)
    private val symbolSouth: String = context.getString(R.string.south)
    private val symbolEast: String = context.getString(R.string.east)
    private val symbolWest: String = context.getString(R.string.west)

    /**
     * The bearing/yaw format for decimal format.
     */
    private val formatBearingDecimal: String = context.getString(R.string.bearing_decimal)

    /**
     * The bearing/yaw format for sexagesimal format.
     */
    private val formatBearingSexagesimal: String = context.getString(R.string.bearing_sexagesimal)

    constructor(context: Context, preferences: LocationPreferences) : this(
        context,
        preferences.coordinatesFormat,
        preferences.isElevationVisible
    )

    override fun formatLatitudeSexagesimal(latitude: Double): String {
        var coordinate = latitude
        val symbol = if (coordinate >= 0) symbolNorth else symbolSouth
        coordinate = abs(coordinate)
        val degrees = floor(coordinate)
        coordinate -= degrees
        coordinate *= 60.0
        val minutes = floor(coordinate)
        coordinate -= minutes
        coordinate *= 60.0
        val seconds = coordinate
        return String.format(
            locale,
            PATTERN_SEXAGESIMAL,
            degrees.toInt(),
            minutes.toInt(),
            seconds,
            symbol
        )
    }

    override fun formatLongitudeSexagesimal(longitude: Double): String {
        var coordinate = longitude
        val symbol = if (coordinate >= 0) symbolEast else symbolWest
        coordinate = abs(coordinate)
        val degrees = floor(coordinate)
        coordinate -= degrees
        coordinate *= 60.0
        val minutes = floor(coordinate)
        coordinate -= minutes
        coordinate *= 60.0
        val seconds = coordinate
        return String.format(
            locale,
            PATTERN_SEXAGESIMAL,
            degrees.toInt(),
            minutes.toInt(),
            seconds,
            symbol
        )
    }

    override fun formatBearingDecimal(azimuth: Double): String {
        val angle = (azimuth + 360) % 360
        val bearing = 90 - abs(angle % 180 - 90)
        val symbolLatitude = if (angle > 90 && angle < 270) symbolSouth else symbolNorth
        val symbolLongitude = if (angle in 0.0..180.0) symbolEast else symbolWest
        return String.format(locale, formatBearingDecimal, symbolLatitude, bearing, symbolLongitude)
    }

    override fun formatBearingSexagesimal(azimuth: Double): String {
        val angle = (azimuth + 360) % 360
        val bearing = 90 - abs(angle % 180 - 90)
        val symbolLatitude = if (angle > 90 && angle < 270) symbolSouth else symbolNorth
        val symbolLongitude = if (angle in 0.0..180.0) symbolEast else symbolWest
        var coordinate = bearing
        coordinate = abs(coordinate)
        val degrees = floor(coordinate)
        coordinate -= degrees
        coordinate *= 60.0
        val minutes = floor(coordinate)
        coordinate -= minutes
        coordinate *= 60.0
        val seconds = floor(coordinate)
        val bearingText = String.format(
            locale,
            PATTERN_SEXAGESIMAL_ROUND,
            degrees.toInt(),
            minutes.toInt(),
            seconds.toInt(),
            symbolLongitude
        )
        return String.format(Locale.US, formatBearingSexagesimal, symbolLatitude, bearingText)
    }

    override fun parseLatitude(coordinate: String): Double {
        return if (FORMAT_SEXAGESIMAL == notation) {
            parseLatitudeSexagesimal(coordinate)
        } else super.parseLatitude(coordinate)
    }

    override fun parseLongitude(coordinate: String): Double {
        return if (FORMAT_SEXAGESIMAL == notation) {
            parseLongitudeSexagesimal(coordinate)
        } else super.parseLongitude(coordinate)
    }

    private fun parseLatitudeSexagesimal(latitude: String): Double {
        var coordinate = latitude
        var isNorth = true
        if (coordinate.endsWith(symbolNorth)) {
            coordinate = coordinate.substring(0, coordinate.length - symbolNorth.length)
        } else if (coordinate.endsWith(symbolSouth)) {
            isNorth = false
            coordinate = coordinate.substring(0, coordinate.length - symbolSouth.length)
        } else if (coordinate.endsWith(symbolEast)) {
            return Double.NaN
        } else if (coordinate.endsWith(symbolWest)) {
            return Double.NaN
        }
        try {
            var indexDegrees = coordinate.indexOf(SYMBOL_DEGREES)
            if (indexDegrees <= 0) indexDegrees = coordinate.length
            val degrees = coordinate.substring(0, indexDegrees)
            var value = degrees.toInt(10).toDouble()
            if (value < ZmanimLocation.LATITUDE_MIN) return Double.NaN
            if (value > ZmanimLocation.LATITUDE_MAX) return Double.NaN
            indexDegrees++ // length(SYMBOL_MINUTES);
            if (indexDegrees < coordinate.length) {
                coordinate = coordinate.substring(indexDegrees)
                var indexMinutes = coordinate.indexOf(SYMBOL_MINUTES)
                if (indexMinutes <= 0) indexMinutes = coordinate.indexOf(SYMBOL_MINUTES_ASCII)
                if (indexMinutes <= 0) indexMinutes = coordinate.length
                val minutes = coordinate.substring(0, indexMinutes)
                val mins = minutes.toInt(10).toDouble()
                value += mins / 60.0
                if (value < ZmanimLocation.LATITUDE_MIN) return Double.NaN
                if (value > ZmanimLocation.LATITUDE_MAX) return Double.NaN
                indexMinutes++ // length(SYMBOL_MINUTES);
                if (indexMinutes < coordinate.length) {
                    coordinate = coordinate.substring(indexMinutes)
                    var indexSeconds = coordinate.indexOf(SYMBOL_SECONDS)
                    if (indexSeconds <= 0) indexSeconds = coordinate.indexOf(SYMBOL_SECONDS_ASCII)
                    if (indexSeconds <= 0) indexSeconds = coordinate.length
                    val seconds = coordinate.substring(0, indexSeconds)
                    val secs = seconds.toDouble()
                    value += secs / 3600.0
                    if (value < ZmanimLocation.LATITUDE_MIN) return Double.NaN
                    if (value > ZmanimLocation.LATITUDE_MAX) return Double.NaN
                }
            }
            return if (isNorth) value else -value
        } catch (ignore: NumberFormatException) {
        }
        return Double.NaN
    }

    private fun parseLongitudeSexagesimal(longitude: String): Double {
        var coordinate = longitude
        var isEast = true
        if (coordinate.endsWith(symbolEast)) {
            coordinate = coordinate.substring(0, coordinate.length - symbolEast.length)
        } else if (coordinate.endsWith(symbolWest)) {
            isEast = false
            coordinate = coordinate.substring(0, coordinate.length - symbolWest.length)
        } else if (coordinate.endsWith(symbolNorth)) {
            return Double.NaN
        } else if (coordinate.endsWith(symbolSouth)) {
            return Double.NaN
        }
        try {
            var indexDegrees = coordinate.indexOf(SYMBOL_DEGREES)
            if (indexDegrees <= 0) indexDegrees = coordinate.length
            val degrees = coordinate.substring(0, indexDegrees)
            var value = degrees.toInt(10).toDouble()
            if (value < ZmanimLocation.LONGITUDE_MIN) return Double.NaN
            if (value > ZmanimLocation.LONGITUDE_MAX) return Double.NaN
            indexDegrees++ // length(SYMBOL_MINUTES);
            if (indexDegrees < coordinate.length) {
                coordinate = coordinate.substring(indexDegrees)
                var indexMinutes = coordinate.indexOf(SYMBOL_MINUTES)
                if (indexMinutes <= 0) indexMinutes = coordinate.indexOf(SYMBOL_MINUTES_ASCII)
                if (indexMinutes <= 0) indexMinutes = coordinate.length
                val minutes = coordinate.substring(0, indexMinutes)
                val mins = minutes.toInt(10).toDouble()
                value += mins / 60.0
                if (value < ZmanimLocation.LONGITUDE_MIN) return Double.NaN
                if (value > ZmanimLocation.LONGITUDE_MAX) return Double.NaN
                indexMinutes++ // length(SYMBOL_MINUTES);
                if (indexMinutes < coordinate.length) {
                    coordinate = coordinate.substring(indexMinutes)
                    var indexSeconds = coordinate.indexOf(SYMBOL_SECONDS)
                    if (indexSeconds <= 0) indexSeconds = coordinate.indexOf(SYMBOL_SECONDS_ASCII)
                    if (indexSeconds <= 0) indexSeconds = coordinate.length
                    val seconds = coordinate.substring(0, indexSeconds)
                    val secs = seconds.toDouble()
                    value += secs / 3600.0
                    if (value < ZmanimLocation.LONGITUDE_MIN) return Double.NaN
                    if (value > ZmanimLocation.LONGITUDE_MAX) return Double.NaN
                }
            }
            return if (isEast) value else -value
        } catch (ignore: NumberFormatException) {
        }
        return Double.NaN
    }

    companion object {
        private const val SYMBOL_DEGREES = '\u00B0'
        private const val SYMBOL_MINUTES = '\u2032'
        private const val SYMBOL_MINUTES_ASCII = '\''
        private const val SYMBOL_SECONDS = '\u2033'
        private const val SYMBOL_SECONDS_ASCII = '"'

        /**
         * [ISO-6709](http://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_.28Annex_D.29)
         */
        private const val PATTERN_SEXAGESIMAL = "%1$02d\u00B0%2$02d\u2032%3$02.3f\u2033%4\$s"
        private const val PATTERN_SEXAGESIMAL_ROUND = "%1$02d\u00B0%2$02d\u2032%3$02d\u2033%4\$s"
    }
}
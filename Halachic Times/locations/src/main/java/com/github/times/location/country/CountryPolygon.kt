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
package com.github.times.location.country

import android.graphics.Point
import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Country borders as a simplified polygon.<br/>
 * Loosely based on `java.awt.Polygon`.
 *
 * `android.graphics.Region` has problem with large areas.
 *
 * @author Moshe Waisberg
 */
class CountryPolygon @JvmOverloads constructor(
    @JvmField
    val countryCode: String,
    /**
     * The total number of points.
     */
    npoints: Int = MIN_LENGTH,
    /**
     * The array of latitudes (Y coordinates).
     */
    latitudes: IntArray = IntArray(npoints),
    /**
     * The array of longitudes (X coordinates).
     */
    longitudes: IntArray = IntArray(npoints),
) {
    /**
     * The total number of points.
     */
    @JvmField
    var npoints: Int = npoints
    //TODO private set

    /**
     * The array of latitudes (Y coordinates).
     */
    @JvmField
    var latitudes: IntArray = latitudes
    //TODO private set

    /**
     * The array of longitudes (X coordinates).
     */
    @JvmField
    var longitudes: IntArray = longitudes
    //TODO private set

    private var minLatitude = Int.MAX_VALUE
    private var minLongitude = Int.MAX_VALUE
    private var maxLatitude = Int.MIN_VALUE
    private var maxLongitude = Int.MIN_VALUE

    init {
        this.npoints = npoints
        this.latitudes = latitudes
        this.longitudes = longitudes
        require(npoints > 0)
        require(npoints <= latitudes.size)
        require(npoints <= longitudes.size)
    }

    /**
     * Tests if the specified coordinates are inside the bounding box of the
     * country.
     *
     * @param latitude  the latitude to be tested.
     * @param longitude the longitude to be tested.
     * @return `true` if the specified coordinates are inside the country
     * boundary; `false` otherwise.
     */
    fun containsBox(latitude: Int, longitude: Int): Boolean {
        return (latitude in minLatitude..maxLatitude) && (longitude in minLongitude..maxLongitude)
    }

    /**
     * Tests if the specified country is inside the bounding box of this
     * country.
     *
     * @param other the other country to be tested.
     * @return `true` if the specified country is inside this country
     * boundary; `false` otherwise.
     */
    fun containsBox(other: CountryPolygon): Boolean {
        return other.minLatitude >= minLatitude
            && other.minLongitude >= minLongitude
            && other.maxLatitude <= maxLatitude
            && other.maxLongitude <= maxLongitude
    }

    /**
     * Tests if the specified coordinates are inside the borders of the country.
     * Uses the ray casting algorithm.
     *
     * @param latitude  the latitude to be tested.
     * @param longitude the longitude to be tested.
     * @return `true` if the specified coordinates are inside the country
     * boundary; `false` otherwise.
     */
    fun contains(latitude: Int, longitude: Int): Boolean {
        var oddNodes = false
        var latI: Int
        var lngI: Int
        var latJ: Int
        var lngJ: Int
        var j = npoints - 1
        for (i in 0 until npoints) {
            latI = latitudes[i]
            lngI = longitudes[i]
            latJ = latitudes[j]
            lngJ = longitudes[j]
            if ((latitude in (latI + 1)..latJ || latitude in (latJ + 1)..latI) && (lngI <= longitude || lngJ <= longitude)) {
                if (lngI + (((latitude - latI) * (lngJ - lngI)) / (latJ - latI)) < longitude) {
                    oddNodes = !oddNodes
                }
            }
            j = i
        }
        return oddNodes
    }

    /**
     * Appends the specified coordinates to this country.
     * @param latitude  the specified latitude (Y coordinate).
     * @param longitude the specified longitude (X coordinate).
     */
    fun addPoint(latitude: Int, longitude: Int) {
        val newIndex = npoints
        if (npoints >= latitudes.size || npoints >= longitudes.size) {
            var newLength = npoints shl 1
            // Make sure that newLength will be greater than MIN_LENGTH and
            // aligned to the power of 2
            if (newLength < MIN_LENGTH) {
                newLength = MIN_LENGTH
            } else if (newLength and (newLength - 1) != 0) {
                newLength = Integer.highestOneBit(newLength)
            }
            val oldLatitudes = latitudes
            val oldLongitudes = longitudes
            latitudes = IntArray(newLength)
            longitudes = IntArray(newLength)
            System.arraycopy(oldLatitudes, 0, latitudes, 0, npoints)
            System.arraycopy(oldLongitudes, 0, longitudes, 0, npoints)
        }
        latitudes[newIndex] = latitude
        longitudes[newIndex] = longitude
        npoints++
        updateBounds(latitude, longitude)
    }

    /**
     * Update the rectangular boundary.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     */
    private fun updateBounds(latitude: Int, longitude: Int) {
        minLatitude = min(minLatitude, latitude)
        minLongitude = min(minLongitude, longitude)
        maxLatitude = max(maxLatitude, latitude)
        maxLongitude = max(maxLongitude, longitude)
    }

    /**
     * Find the minimum distance to any of the borders.
     *
     * @param latitude  the latitude of the point.
     * @param longitude the longitude of the point.
     * @return the distance.
     */
    fun minimumDistanceToBorders(latitude: Int, longitude: Int): Double {
        var j = npoints - 1
        var minimum = Double.MAX_VALUE
        var d: Double
        for (i in 0 until npoints) {
            d = pointToLineDistance(
                latitudes[i].toDouble(),
                longitudes[i].toDouble(),
                latitudes[j].toDouble(),
                longitudes[j].toDouble(),
                latitude.toDouble(),
                longitude.toDouble()
            )
            if (d < minimum) minimum = d
            j = i
        }
        return minimum
    }

    override fun toString(): String {
        val buf = StringBuilder(countryCode)
            .append('[')
        for (i in 0 until npoints) {
            if (i > 0) buf.append(',')
            buf.append('(')
                .append(latitudes[i])
                .append(',')
                .append(longitudes[i])
                .append(')')
        }
        buf.append(']')
        return buf.toString()
    }

    companion object {
        /**
         * Factor to convert a fixed-point integer to double.
         */
        const val RATIO = 1e+5

        /**
         * Default length for latitudes and longitudes.
         */
        private const val MIN_LENGTH = 8

        /**
         * Calculate the distance from a point to a line.
         *
         * @param a a point on the line.
         * @param b another point on the line.
         * @param p the point, not on the line.
         * @return the distance.
         * @see [Distance
         * from a point to a line](http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line)
         */
        @JvmStatic
        fun pointToLineDistance(a: Point, b: Point, p: Point): Double {
            return pointToLineDistance(
                a.x.toDouble(),
                a.y.toDouble(),
                b.x.toDouble(),
                b.y.toDouble(),
                p.x.toDouble(),
                p.y.toDouble()
            )
        }

        /**
         * Calculate the distance from a point to a line.
         *
         * @param a a point on the line.
         * @param b another point on the line.
         * @param p the point, not on the line.
         * @return the distance.
         * @see [Distance
         * from a point to a line](http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line)
         */
        @JvmStatic
        fun pointToLineDistance(a: PointF, b: PointF, p: PointF): Double {
            return pointToLineDistance(
                a.x.toDouble(),
                a.y.toDouble(),
                b.x.toDouble(),
                b.y.toDouble(),
                p.x.toDouble(),
                p.y.toDouble()
            )
        }

        /**
         * Calculate the distance from a point to a line.
         *
         * @param ax X coordinate of a point on the line.
         * @param ay Y coordinate of a point on the line.
         * @param bx X coordinate of another point on the line.
         * @param by Y coordinate of another point on the line.
         * @param px X coordinate of the point, not on the line.
         * @param py Y coordinate of the point, not on the line.
         * @return the distance.
         * @see [Distance
         * from a point to a line](http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line)
         */
        @JvmStatic
        fun pointToLineDistance(
            ax: Double,
            ay: Double,
            bx: Double,
            by: Double,
            px: Double,
            py: Double
        ): Double {
            val dxAB = bx - ax
            val dyAB = by - ay
            val normalLength = hypot(dxAB, dyAB)
            return abs((px - ax) * dyAB - (py - ay) * dxAB) / normalLength
        }
    }
}
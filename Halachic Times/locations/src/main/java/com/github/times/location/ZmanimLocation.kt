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
import android.os.Parcel
import android.os.Parcelable
import com.github.math.toDegrees
import com.github.math.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ln
import kotlin.math.tan

/**
 * Location that is partially stored in the local database.
 *
 * @author Moshe Waisberg
 */
class ZmanimLocation : Location {
    var id: Long = 0

    /**
     * Constructs a new location.
     *
     * @param provider the name of the provider that generated this location.
     */
    constructor(provider: String) : super(provider)

    /**
     * Construct a new location that is copied from an existing one.
     *
     * @param location the source location.
     */
    constructor(location: Location) : super(location)

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the loxodrome path between this
     * location and the given location. The constant bearing path is defined
     * using the Rhumb line.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    fun angleTo(dest: Location): Float {
        return angleTo(dest.latitude, dest.longitude)
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the loxodrome path between this
     * location and the given location. The constant bearing path is defined
     * using the Rhumb line.
     *
     * @param latitude  the destination latitude, in degrees.
     * @param longitude the destination longitude, in degrees.
     * @return the bearing in degrees.
     */
    fun angleTo(latitude: Double, longitude: Double): Float {
        return computeRhumbBearing(this.latitude, this.longitude, latitude, longitude).toFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(id)
    }

    companion object {
        /**
         * Minimum valid latitude.
         */
        const val LATITUDE_MIN = -90.0

        /**
         * Maximum valid latitude.
         */
        const val LATITUDE_MAX = 90.0

        /**
         * Minimum valid longitude.
         */
        const val LONGITUDE_MIN = -180.0

        /**
         * Maximum valid longitude.
         */
        const val LONGITUDE_MAX = 180.0

        /**
         * Lowest possible natural elevation on the surface of the earth.
         */
        const val ELEVATION_MIN = -500.0

        /**
         * Highest possible natural elevation from the surface of the earth.
         */
        const val ELEVATION_MAX = 100_000.0

        /**
         * Double subtraction error.
         */
        private const val EPSILON = 1e-6
        private const val RADIANS_180 = Math.PI
        private const val RADIANS_360 = RADIANS_180 * 2
        private const val RADIANS_45 = RADIANS_180 / 4

        /**
         * Returns the approximate initial bearing in degrees East of true
         * North when traveling along the loxodrome path between this
         * location and the given location. The constant bearing path is defined
         * using the Rhumb line.
         *
         * @param location    the initial location.
         * @param destination the destination location.
         * @return the bearing in degrees.
         */
        @JvmStatic
        fun angleTo(location: Location, destination: Location): Float {
            return computeRhumbBearing(
                location.latitude,
                location.longitude,
                destination.latitude,
                destination.longitude
            ).toFloat()
        }

        /**
         * Computes the azimuth angle (clockwise from North) of a Rhumb line (a line of constant heading) between two
         * locations.
         * This method uses a spherical model, not elliptical.
         *
         * @param latitude1  the starting latitude, in degrees.
         * @param longitude1 the starting longitude, in degrees.
         * @param latitude2  the destination longitude, in degrees.
         * @param longitude2 the destination latitude, in degrees.
         * @return teh bearing in degrees.
         */
        @JvmStatic
        private fun computeRhumbBearing(
            latitude1: Double,
            longitude1: Double,
            latitude2: Double,
            longitude2: Double
        ): Double {
            val lat1 = latitude1.toRadians()
            val lng1 = longitude1.toRadians()
            val lat2 = latitude2.toRadians()
            val lng2 = longitude2.toRadians()

            val phi1 = tan(RADIANS_45 + (lat1 / 2))
            val phi2 = tan(RADIANS_45 + (lat2 / 2))
            val dPhi = ln(phi2 / phi1)
            var dLon = lng2 - lng1

            // if dLon over 180° take shorter Rhumb line across the anti-meridian:
            if (abs(dLon) > RADIANS_180) {
                dLon = if (dLon > 0) -(RADIANS_360 - dLon) else (RADIANS_360 + dLon)
            }
            var azimuth = atan2(dLon, dPhi)
            if (azimuth < 0) {
                azimuth += RADIANS_360
            }
            return azimuth.toDegrees()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<ZmanimLocation> =
            object : Parcelable.Creator<ZmanimLocation> {
                override fun createFromParcel(source: Parcel): ZmanimLocation {
                    val l = Location.CREATOR.createFromParcel(source)
                    return ZmanimLocation(l).apply {
                        id = source.readLong()
                    }
                }

                override fun newArray(size: Int): Array<ZmanimLocation?> {
                    return arrayOfNulls(size)
                }
            }

        /**
         * Compare two locations by latitude and longitude only.
         *
         * @param l1 the first location.
         * @param l2 the second location.
         * @return the comparison as per [Comparable].
         */
        @JvmStatic
        fun compare(l1: Location?, l2: Location?): Int {
            if (l1 === l2) return 0
            if (l1 == null) return -1
            if (l2 == null) return 1

            val lat1 = l1.latitude
            val lat2 = l2.latitude
            val latD = lat1 - lat2
            if (latD >= EPSILON) return 1
            if (latD <= -EPSILON) return -1

            val lng1 = l1.longitude
            val lng2 = l2.longitude
            val lngD = lng1 - lng2
            if (lngD >= EPSILON) return 1
            if (lngD <= -EPSILON) return -1

            return 0
        }

        /**
         * Compare two locations by latitude and then longitude, and then altitude, and then time.
         *
         * @param l1 the first location.
         * @param l2 the second location.
         * @return the comparison as per [Comparable].
         */
        @JvmStatic
        fun compareAll(l1: Location?, l2: Location?): Int {
            if (l1 === l2) return 0
            if (l1 == null) return -1
            if (l2 == null) return 1

            val c = compare(l1, l2)
            if (c != 0) return c

            val ele1 = if (l1.hasAltitude()) l1.altitude else 0.0
            val ele2 = if (l2.hasAltitude()) l2.altitude else 0.0
            val eleD = ele1 - ele2
            if (eleD >= EPSILON) return 1
            if (eleD <= -EPSILON) return -1

            val t1 = l1.time
            val t2 = l2.time
            return t1.compareTo(t2)
        }

        @JvmStatic
        fun toDecimal(degrees: Int, minutes: Int, seconds: Double): Double {
            return degrees + minutes / 60.0 + seconds / 3600.0
        }

        /**
         * Is the location valid?
         *
         * @param location the location to check.
         * @return `false` if location is invalid.
         */
        @JvmStatic
        fun isValid(location: Location?): Boolean {
            if (location == null) return false
            val latitude = location.latitude
            if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX) return false
            val longitude = location.longitude
            if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX) return false
            val elevation = location.altitude
            return elevation in ELEVATION_MIN..ELEVATION_MAX
        }

        @JvmStatic
        fun distanceBetween(startLocation: Location, endLocation: Location): Double {
            val distances = FloatArray(1)
            distanceBetween(startLocation, endLocation, distances)
            return distances[0].toDouble()
        }

        @JvmStatic
        fun distanceBetween(startLocation: Location, endLocation: Location, distances: FloatArray) =
            distanceBetween(
                startLocation.latitude,
                startLocation.longitude,
                endLocation.latitude,
                endLocation.longitude,
                distances
            )
    }
}
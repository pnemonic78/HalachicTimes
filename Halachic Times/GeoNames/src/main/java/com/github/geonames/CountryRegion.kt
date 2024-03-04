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
package com.github.geonames

import com.github.geonames.util.LocaleUtils.ISO639_ISRAEL
import com.github.geonames.util.LocaleUtils.ISO639_PALESTINE
import com.vividsolutions.jts.geom.Geometry
import java.awt.Polygon
import java.awt.geom.Point2D
import java.io.IOException
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.round
import org.geotools.geojson.geom.GeometryJSON

/**
 * Country region.
 *
 * @author Moshe Waisberg
 */
class CountryRegion(countryCode: String, geometry: Geometry) {
    val countryCode: String = if (ISO639_PALESTINE == countryCode) {
        ISO639_ISRAEL
    } else {
        countryCode
    }

    val boundary: Polygon = geometry.boundary.toPolygon(FACTOR_TO_INT)
    val centroid: Point2D = geometry.centroid.toPoint2D() * FACTOR_TO_INT
    val boundaries: List<Polygon>

    init {
        val num = geometry.numGeometries
        val geometries = mutableListOf<Polygon>()
        for (n in 0 until num) {
            geometries.add(geometry.getGeometryN(n).toPolygon(FACTOR_TO_INT))
        }
        boundaries = geometries
    }

    private fun addPoint(x: Double, y: Double) {
        boundary.addPoint(x.toInt(), y.toInt())
    }

    /**
     * Find the main vertices that represent the border.
     *
     * @param vertexCount the number of vertices.
     * @return an array of indexes.
     */
    fun findMainVertices(vertexCount: Int): IntArray {
        // TODO("find point closest to the 'ray'")
        val indexes = IntArray(vertexCount) { -1 }
        var r = 0
        val n = boundary.npoints
        val xpoints = boundary.xpoints
        val ypoints = boundary.ypoints
        val cx = centroid.x
        val cy = centroid.y

        val sweepAngle = (2 * PI) / vertexCount
        var angleStart = 0.0
        var angleEnd: Double
        var x: Double
        var y: Double
        var a: Double
        var d: Double
        var farIndex: Int
        var farDist: Double

        for (v in 0 until vertexCount) {
            angleEnd = angleStart + sweepAngle
            farDist = Double.MIN_VALUE
            farIndex = -1

            for (i in 0 until n) {
                x = xpoints[i].toDouble()
                y = ypoints[i].toDouble()
                a = atan2(y - cy, x - cx) + PI
                if (angleStart <= a && a < angleEnd) {
                    d = Point2D.distanceSq(cx, cy, x, y)
                    if (farDist < d) {
                        farDist = d
                        farIndex = i
                    }
                }
            }

            if (farIndex >= 0) indexes[r++] = farIndex
            angleStart += sweepAngle
        }
        if (r < vertexCount) {
            when (r) {
                0 -> {
                    addPoint(xpoints[0] - CITY_BOUNDARY, ypoints[0] - CITY_BOUNDARY)
                    addPoint(xpoints[0] + CITY_BOUNDARY, ypoints[0] + CITY_BOUNDARY)
                    addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(vertexCount)
                }

                1 -> {
                    addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(vertexCount)
                }
            }
        }
        return indexes
    }

    companion object {
        /** Factor to convert coordinate value to a fixed-point integer.  */
        internal const val FACTOR_TO_INT = 1e+5

        /**
         * Factor to convert coordinate value to a fixed-point integer for city
         * limits.
         */
        private const val CITY_BOUNDARY = 1e+4

        @JvmStatic
        @Throws(IOException::class)
        fun toRegion(countryCode: String, geoShape: GeoShape): CountryRegion {
            val json = GeometryJSON()
            val geometry = json.read(geoShape.geoJSON)
            geoShape.geometry = geometry
            return CountryRegion(countryCode, geometry)
        }

        fun toFixedPoint(degrees: Double): Double {
            return round(degrees * FACTOR_TO_INT)
        }

        fun toFixedPointInt(degrees: Double): Int {
            return toFixedPoint(degrees).toInt()
        }
    }
}

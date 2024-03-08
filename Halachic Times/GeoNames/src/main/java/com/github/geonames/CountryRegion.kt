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
import com.github.lang.toDoubleArray
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
    val areas: List<Double>
    val centroids: List<Point2D>
    val maxAreaIndex: Int

    init {
        val areas = mutableListOf<Double>()
        val centroids = mutableListOf<Point2D>()
        val num = geometry.numGeometries
        val geometries = mutableListOf<Polygon>()
        var maxAreaIndex = -1
        var maxArea = 0.0

        for (n in 0 until num) {
            val geometryN = geometry.getGeometryN(n)
            val areaN = geometryN.area
            geometries.add(geometryN.toPolygon(FACTOR_TO_INT))
            areas.add(areaN)
            centroids.add(geometryN.centroid.toPoint2D() * FACTOR_TO_INT)

            if (areaN > maxArea) {
                maxArea = areaN
                maxAreaIndex = n
            }
        }
        this.boundaries = geometries
        this.areas = areas
        this.centroids = centroids
        this.maxAreaIndex = maxAreaIndex
    }

    /**
     * Find the main vertices that represent the border.
     *
     * @param boundary the boundary with vertices.
     * @param vertexCount the number of vertices.
     * @return an array of indexes.
     */
    fun findMainVertices(boundary: Polygon, vertexCount: Int): IntArray {
        val indexes = IntArray(vertexCount) { -1 }
        val n = boundary.npoints
        val xpoints = boundary.xpoints.toDoubleArray()
        val ypoints = boundary.ypoints.toDoubleArray()
        val cx = centroid.x
        val cy = centroid.y
        val angles = DoubleArray(n)
        var x: Double
        var y: Double

        for (i in 0 until n) {
            x = xpoints[i]
            y = ypoints[i]
            angles[i] = atan2(y - cy, x - cx) + PI
        }

        val sweepAngle = (2 * PI) / vertexCount
        var angleStart = 0.0
        var angleEnd: Double
        var angleRange: OpenEndRange<Double>
        var a: Double
        var d: Double
        var i = 0
        var farIndex: Int
        var farDist: Double

        for (v in 0 until vertexCount) {
            angleEnd = angleStart + sweepAngle
            angleRange = angleStart.rangeUntil(angleEnd)
            farDist = 0.0
            farIndex = -1

            for (i in 0 until n) {
                a = angles[i]
                if (a in angleRange) {
                    x = xpoints[i]
                    y = ypoints[i]
                    d = Point2D.distanceSq(cx, cy, x, y)
                    if (farDist < d) {
                        farDist = d
                        farIndex = i
                    }
                }
            }

            if (farIndex >= 0) indexes[i++] = farIndex
            angleStart += sweepAngle
        }
        if (i < vertexCount) {
            when (i) {
                0 -> {
                    val boundarySmall = Polygon()
                    boundarySmall.addPoint(xpoints[0] - CITY_BOUNDARY, ypoints[0] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[0] + CITY_BOUNDARY, ypoints[0] + CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(boundarySmall, vertexCount)
                }

                1 -> {
                    val boundarySmall = Polygon()
                    boundarySmall.addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(boundarySmall, vertexCount)
                }
            }
        }
        return indexes
    }

    companion object {
        /** Factor to convert coordinate value to a fixed-point integer.  */
        internal const val FACTOR_TO_INT = 1e+5

        /** The number of main vertices per region border.  */
        const val VERTICES_COUNT = 16

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

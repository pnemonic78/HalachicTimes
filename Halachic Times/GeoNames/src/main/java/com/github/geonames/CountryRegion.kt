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
import java.io.IOException
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

    val geometries: List<CountryGeometry>
    val maxAreaIndex: Int

    init {
        val num = geometry.numGeometries
        val geometriesCloses = mutableListOf<Geometry>()
        for (n in 0 until num) {
            val geometryN = geometry.getGeometryN(n).close()
            geometriesCloses.add(geometryN)
        }
        val geometries = merge(geometriesCloses).map { CountryGeometry.toFixedInt(it) }
        this.geometries = geometries
        this.maxAreaIndex = findMaxArea(geometries)
    }

    private fun findMaxArea(geometries: List<CountryGeometry>): Int {
        val lastIndex = geometries.lastIndex
        var maxAreaIndex = -1
        var maxArea = 0.0
        for (n in 0..lastIndex) {
            val geometryN = geometries[n]
            val areaN = geometryN.area
            if (areaN > maxArea) {
                maxArea = areaN
                maxAreaIndex = n
            }
        }
        return maxAreaIndex
    }

    /** Merge neighbouring geometries. */
    private fun merge(
        geometries: List<Geometry>,
        distanceNeighbour: Double = DISTANCE_NEIGHBOUR
    ): List<Geometry> {
        if (geometries.size <= 1) return geometries

        val merged = mutableListOf<Geometry?>()
        merged.addAll(geometries.sortedByDescending { it.area })
        val lastIndex = merged.lastIndex

        for (i in 0..lastIndex) {
            var geoI = merged[i] ?: continue
            val envelopeI = geoI.envelope
            for (j in (i + 1)..lastIndex) {
                val geoJ = merged[j] ?: continue
                val envelopeJ = geoJ.envelope
                try {
                    if (envelopeI.distance(envelopeJ) <= distanceNeighbour) {
                        geoI = geoI.join(geoJ)
                        merged[i] = geoI
                        merged[j] = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return merged.filterNotNull()
            .map { it.convexHull() }
    }

    companion object {
        /** Factor to convert coordinate value to a fixed-point integer.  */
        internal const val FACTOR_TO_INT = CountryGeometry.FACTOR_TO_INT

        private const val DISTANCE_NEIGHBOUR = 5.0

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

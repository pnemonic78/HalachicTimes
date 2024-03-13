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
        val geometries = mutableListOf<CountryGeometry>()
        for (n in 0 until num) {
            val geometryN = geometry.getGeometryN(n)
            val countryGeometry = CountryGeometry(geometryN.close())
            geometries.add(countryGeometry)
        }
        this.geometries = merge(geometries)
        this.maxAreaIndex = findMaxArea(this.geometries)
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
    private fun merge(geometries: List<CountryGeometry>): List<CountryGeometry> {
        val merged = mutableListOf<Geometry?>()
        merged.addAll(geometries.sortedByDescending { it.area }.map { it.geometry })
        val lastIndex = merged.lastIndex

        for (i in 0..lastIndex) {
            var geoI = merged[i] ?: continue
            val envelopeI = geoI.envelope
            for (j in (i + 1)..lastIndex) {
                val geoJ = merged[j] ?: continue
                val envelopeJ = geoJ.envelope
                try {
                    if (envelopeI.distance(envelopeJ) <= DISTANCE_NEIGHBOUR) {
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
            .map { CountryGeometry(it.convexHull(), 1.0) }
    }

    companion object {
        /** Factor to convert coordinate value to a fixed-point integer.  */
        internal const val FACTOR_TO_INT = CountryGeometry.FACTOR_TO_INT

        private const val DISTANCE_NEIGHBOUR = 5 * FACTOR_TO_INT

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

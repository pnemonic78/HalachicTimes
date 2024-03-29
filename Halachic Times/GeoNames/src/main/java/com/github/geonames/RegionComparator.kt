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

/**
 * Region comparator.
 *
 * @author Moshe Waisberg
 */
class RegionComparator : Comparator<CountryRegion> {
    override fun compare(region1: CountryRegion, region2: CountryRegion): Int {
        val name1 = region1.countryCode
        val name2 = region2.countryCode
        val name = name1.compareTo(name2)
        if (name != 0) return name

        val geometries1 = region1.geometries
        val geometries2 = region2.geometries
        val size1 = geometries1.size
        val size2 = geometries2.size
        val sizeC = size1.compareTo(size2)
        if (sizeC != 0) return sizeC

        for (i in 0 until size1) {
            val c = compareGeometry(geometries1[i], geometries2[i])
            if (c != 0) return c
        }

        return 0
    }

    private fun compareGeometry(g1: CountryGeometry, g2: CountryGeometry): Int {
        val area1 = g1.area
        val area2 = g2.area
        val areaC = area1.compareTo(area2)
        if (areaC != 0) return areaC

        val npoints1 = g1.boundary.npoints
        val npoints2 = g2.boundary.npoints
        val npointsC = npoints1.compareTo(npoints2)
        if (npointsC != 0) return npointsC

        var x1: Int
        var x2: Int
        var xC: Int
        for (i in 0 until npoints1) {
            x1 = g1.boundary.xpoints[i]
            x2 = g2.boundary.xpoints[i]
            xC = x1.compareTo(x2)
            if (xC != 0) return xC
        }

        var y1: Int
        var y2: Int
        var yC: Int
        for (i in 0 until npoints1) {
            y1 = g1.boundary.ypoints[i]
            y2 = g2.boundary.ypoints[i]
            yC = y1.compareTo(y2)
            if (yC != 0) return yC
        }

        return 0
    }
}

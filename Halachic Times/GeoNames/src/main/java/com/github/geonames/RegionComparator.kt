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

        val npoints1 = region1.boundary.npoints
        val npoints2 = region2.boundary.npoints
        val npoints = npoints1.compareTo(npoints2)
        if (npoints != 0) return npoints

        var x1: Int
        var x2: Int
        var x: Int
        for (i in 0 until npoints1) {
            x1 = region1.boundary.xpoints[i]
            x2 = region2.boundary.xpoints[i]
            x = x1.compareTo(x2)
            if (x != 0) return x
        }

        var y1: Int
        var y2: Int
        var y: Int
        for (i in 0 until npoints1) {
            y1 = region1.boundary.ypoints[i]
            y2 = region2.boundary.ypoints[i]
            y = y1.compareTo(y2)
            if (y != 0) return y
        }
        return 0
    }
}

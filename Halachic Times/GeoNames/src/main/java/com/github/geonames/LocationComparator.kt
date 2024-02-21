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
 * Location comparator.
 *
 * @author Moshe Waisberg
 */
class LocationComparator : Comparator<GeoNamesToponym> {
    override fun compare(geo1: GeoNamesToponym, geo2: GeoNamesToponym): Int {
        // West < East
        val lng1 = geo1.longitude
        val lng2 = geo2.longitude
        val lngCompare = lng1.compareTo(lng2)
        if (lngCompare != 0) return lngCompare

        // North < South
        val lat1 = geo1.latitude
        val lat2 = geo2.latitude
        val latCompare = lat1.compareTo(lat2)
        if (latCompare != 0) return latCompare

        val ele1 = geo1.grossElevation ?: 0
        val ele2 = geo2.grossElevation ?: 0
        val eleCompare = ele1.compareTo(ele2)
        if (eleCompare != 0) return eleCompare

        val name1 = geo1.name
        val name2 = geo2.name
        val nameCompare = name1.compareTo(name2)
        if (nameCompare != 0) return nameCompare

        val id1 = geo1.geoNameId
        val id2 = geo2.geoNameId
        return id1.compareTo(id2)
    }

    companion object {
        /**
         * ISO 639 code for Norwegian Bokmål.
         */
        const val ISO_639_NB = "nb"

        /**
         * ISO 639 code for Norwegian.
         */
        const val ISO_639_NO = "no"
    }
}

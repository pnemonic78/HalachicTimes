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
package org.geonames

import com.github.util.LocaleUtils.ISO3166_ISRAEL
import com.github.util.LocaleUtils.ISO3166_PALESTINE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GeoNames toponym.
 *
 * @author Moshe Waisberg
 */
@Serializable
open class Toponym {
    @SerialName("geonameId")
    var geoNameId: GeoNameId = 0

    @SerialName("toponymName")
    var toponymName: String? = null
        get() = field ?: name

    @SerialName("name")
    var name: String = ""

    /**
     * The ASCII name.
     */
    @SerialName("asciiName")
    var asciiName: String? = null

    @SerialName("alternateNames")
    var alternateNames: List<AlternateName>? = null

    @SerialName("continentCode")
    var continentCode: String? = null

    @SerialName("countryCode")
    var countryCode: String? = null
        set(value) {
            field = if (ISO3166_PALESTINE == value) {
                ISO3166_ISRAEL
            } else {
                value
            }
        }

    @SerialName("countryId")
    var countryId: GeoNameId? = null

    @SerialName("countryName")
    var countryName: String? = null

    @SerialName("population")
    var population: Long? = null

    @SerialName("elevation")
    var elevation: Int? = null
        get() = field ?: elevationSRTM ?: elevationAsterGDEM

    /**
     * The GDEM elevation.
     * ASTER Global Digital Elevation Model.
     */
    @SerialName("astergdem")
    var elevationAsterGDEM: Int? = null

    /**
     * The SRTM3 elevation.
     * Shuttle Radar Topography Mission.
     */
    @SerialName("srtm3")
    var elevationSRTM: Int? = null

    @SerialName("fcl")
    var featureClass: FeatureClass? = null

    @SerialName("fclName")
    var featureClassName: String? = null

    @SerialName("fcode")
    var featureCode: String? = null

    @SerialName("fcodeName")
    var featureCodeName: String? = null

    @SerialName("lat")
    var latitude: Double = 0.0

    @SerialName("lng")
    var longitude: Double = 0.0

    @SerialName("adminCode1")
    var adminCode1: String? = null

    @SerialName("adminId1")
    var adminId1: GeoNameId? = null

    @SerialName("adminName1")
    var adminName1: String? = null

    @SerialName("adminCode2")
    var adminCode2: String? = null

    @SerialName("adminId2")
    var adminId2: GeoNameId? = null

    @SerialName("adminName2")
    var adminName2: String? = null

    @SerialName("adminCode3")
    var adminCode3: String? = null

    @SerialName("adminId3")
    var adminId3: GeoNameId? = null

    @SerialName("adminName3")
    var adminName3: String? = null

    @SerialName("adminCode4")
    var adminCode4: String? = null

    @SerialName("adminId4")
    var adminId4: GeoNameId? = null

    @SerialName("adminName4")
    var adminName4: String? = null

    @SerialName("adminCode5")
    var adminCode5: String? = null

    @SerialName("adminId5")
    var adminId5: GeoNameId? = null

    @SerialName("adminName5")
    var adminName5: String? = null

    @SerialName("adminTypeName")
    var adminTypeName: String? = null

    @SerialName("timezone")
    var timeZone: TimeZone? = null

    @SerialName("bbox")
    var boundingBox: BoundingBox? = null

    @SerialName("wikipediaURL")
    var wikipediaURL: String? = null
}
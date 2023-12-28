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
package com.github.times.location.geonames

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.geonames.BoundingBox
import org.geonames.FeatureClass
import org.geonames.Timezone

/**
 * GeoNames toponym.
 *
 * @author Moshe Waisberg
 */
@Serializable
class Toponym {
    @SerialName("geonameId")
    var geoNameId: Long = 0

    @SerialName("toponymName")
    var toponymName: String? = null

    @SerialName("name")
    var name: String? = null

    @SerialName("alternateNames")
    var alternateNames: String? = null

    @SerialName("continentCode")
    var continentCode: String? = null

    @SerialName("countryCode")
    var countryCode: String? = null

    @SerialName("countryId")
    var countryId: String? = null

    @SerialName("countryName")
    var countryName: String? = null

    @SerialName("population")
    var population: Long? = null

    @SerialName("elevation")
    var elevation: Int? = null

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

    @SerialName("adminName1")
    var adminName1: String? = null

    @SerialName("adminCode2")
    var adminCode2: String? = null

    @SerialName("adminName2")
    var adminName2: String? = null

    @SerialName("adminCode3")
    var adminCode3: String? = null

    @SerialName("adminName3")
    var adminName3: String? = null

    @SerialName("adminCode4")
    var adminCode4: String? = null

    @SerialName("adminName4")
    var adminName4: String? = null

    @SerialName("adminCode5")
    var adminCode5: String? = null

    @SerialName("adminName5")
    var adminName5: String? = null

    @SerialName("timezone")
    var timezone: Timezone? = null

    @SerialName("bbox")
    var boundingBox: BoundingBox? = null
}
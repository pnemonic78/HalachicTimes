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

import com.google.gson.annotations.SerializedName
import org.geonames.BoundingBox
import org.geonames.FeatureClass
import org.geonames.Timezone

/**
 * GeoNames toponym.
 *
 * @author Moshe Waisberg
 */
class Toponym {
    @SerializedName("geonameId")
    var geoNameId: Long = 0

    @SerializedName("name")
    var name: String? = null

    @SerializedName("alternateNames")
    var alternateNames: String? = null

    @SerializedName("continentCode")
    var continentCode: String? = null

    @SerializedName("countryCode")
    var countryCode: String? = null

    @SerializedName("countryName")
    var countryName: String? = null

    @SerializedName("population")
    var population: Long? = null

    @SerializedName("elevation")
    var elevation: Int? = null

    @SerializedName("fcl")
    var featureClass: FeatureClass? = null

    @SerializedName("fclName")
    var featureClassName: String? = null

    @SerializedName("fcode")
    var featureCode: String? = null

    @SerializedName("fCodeName")
    var featureCodeName: String? = null

    @SerializedName("lat")
    var latitude: Double = 0.0

    @SerializedName("lng")
    var longitude: Double = 0.0

    @SerializedName("adminCode1")
    var adminCode1: String? = null

    @SerializedName("adminName1")
    var adminName1: String? = null

    @SerializedName("adminCode2")
    var adminCode2: String? = null

    @SerializedName("adminName2")
    var adminName2: String? = null

    @SerializedName("adminCode3")
    var adminCode3: String? = null

    @SerializedName("adminName3")
    var adminName3: String? = null

    @SerializedName("adminCode4")
    var adminCode4: String? = null

    @SerializedName("adminName4")
    var adminName4: String? = null

    @SerializedName("adminCode5")
    var adminCode5: String? = null

    @SerializedName("adminName5")
    var adminName5: String? = null

    @SerializedName("timezone")
    var timezone: Timezone? = null

    @SerializedName("bbox")
    var boundingBox: BoundingBox? = null
}
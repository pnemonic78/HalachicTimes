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
package com.github.times.location.bing

import com.google.gson.annotations.SerializedName

/**
 * Address object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
class BingAddress {
    @SerializedName("addressLine")
    var addressLine: String? = null

    @SerializedName("adminDistrict")
    var adminDistrict: String? = null

    @SerializedName("adminDistrict2")
    var adminDistrict2: String? = null

    @SerializedName("countryRegion")
    var countryRegion: String? = null

    @SerializedName("formattedAddress")
    var formattedAddress: String? = null

    @SerializedName("locality")
    var locality: String? = null

    @SerializedName("postalCode")
    var postalCode: String? = null
}
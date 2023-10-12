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
 * Resource object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
class BingResource {
    @SerializedName("__type")
    var type: String? = null

    @SerializedName("bbox")
    var boundingBox: Array<Double>? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("point")
    var point: BingPoint? = null

    @SerializedName("address")
    var address: BingAddress? = null

    @SerializedName("confidence")
    var confidence: String? = null

    @SerializedName("elevations")
    var elevations: Array<Double>? = null

    @SerializedName("entityType")
    var entityType: String? = null

    @SerializedName("geocodePoints")
    var geocodePoints: List<Any>? = null

    @SerializedName("matchCodes")
    var matchCodes: List<Any>? = null

    @SerializedName("zoomLevel")
    var zoomLevel: Double? = null
}
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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resource object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
@Serializable
class BingResource {
    @SerialName("__type")
    var type: String? = null

    @SerialName("bbox")
    var boundingBox: List<Double>? = null

    @SerialName("name")
    var name: String? = null

    @SerialName("point")
    var point: BingPoint? = null

    @SerialName("address")
    var address: BingAddress? = null

    @SerialName("confidence")
    var confidence: String? = null

    @SerialName("elevations")
    var elevations: List<Double>? = null

    @SerialName("entityType")
    var entityType: String? = null

    @SerialName("geocodePoints")
    var geocodePoints: List<BingPoint>? = null

    @SerialName("matchCodes")
    var matchCodes: List<String>? = null

    @SerialName("zoomLevel")
    var zoomLevel: Double? = null
}
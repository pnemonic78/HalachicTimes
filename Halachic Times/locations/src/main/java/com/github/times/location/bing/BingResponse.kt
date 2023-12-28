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

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root object for Bing address JSON response.
 *
 * @author Moshe Waisberg
 */
@Serializable
class BingResponse {
    @SerialName("authenticationResultCode")
    var authenticationResultCode: String? = null

    @SerialName("brandLogoUri")
    var brandLogoUri: String? = null

    @SerialName("copyright")
    var copyright: String? = null

    @SerialName("resourceSets")
    var resourceSets: List<ResourceSet>? = null

    @SerialName("statusCode")
    var statusCode = 0

    @SerialName("statusDescription")
    var statusDescription: String? = null

    @SerialName("traceId")
    var traceId: String? = null

    @Serializable
    class ResourceSet {
        @SerialName("estimatedTotal")
        var estimatedTotal = 0

        @SerialName("resources")
        var resources: List<BingResource>? = null
    }

    companion object {
        const val STATUS_OK = 200
    }
}
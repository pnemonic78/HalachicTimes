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
package com.github.times.location.google

import com.github.times.location.google.json.GeocodingResultSerializer
import com.google.maps.errors.ApiException
import com.google.maps.internal.ApiResponse
import com.google.maps.model.GeocodingResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Geocoder response.
 *
 * @author Moshe Waisberg
 */
@Serializable
class GeocodingResponse : ApiResponse<List<GeocodingResult>> {
    @SerialName("status")
    var status: String? = null
    @SerialName("errorMessage")
    var errorMessage: String? = null
    @SerialName("results")
    var results: List<@Serializable(GeocodingResultSerializer::class) GeocodingResult>? = null

    override fun successful(): Boolean {
        return "OK" == status || "ZERO_RESULTS" == status
    }

    override fun getResult(): List<GeocodingResult> {
        return results ?: emptyList()
    }

    override fun getError(): ApiException? {
        return if (successful()) {
            null
        } else {
            ApiException.from(status, errorMessage)
        }
    }
}
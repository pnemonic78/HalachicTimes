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

import com.github.times.location.google.json.ElevationResultSerializer
import com.google.maps.errors.ApiException
import com.google.maps.internal.ApiResponse
import com.google.maps.model.ElevationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Elevation response.
 *
 * @author Moshe Waisberg
 */
@Serializable
class ElevationResponse : ApiResponse<ElevationResult> {
    @SerialName("status")
    var status: String? = null

    @SerialName("errorMessage")
    var errorMessage: String? = null

    @SerialName("results")
    var results: List<@Serializable(ElevationResultSerializer::class) ElevationResult>? = null

    override fun successful(): Boolean {
        return "OK" == status
    }

    override fun getResult(): ElevationResult? {
        return results?.get(0)
    }

    override fun getError(): ApiException? {
        return if (successful()) {
            null
        } else {
            ApiException.from(status, errorMessage)
        }
    }
}
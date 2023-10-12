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

import com.google.gson.annotations.SerializedName
import com.google.maps.errors.ApiException
import com.google.maps.internal.ApiResponse
import com.google.maps.model.ElevationResult

/**
 * Elevation response.
 *
 * @author Moshe Waisberg
 */
class ElevationResponse : ApiResponse<ElevationResult> {
    @SerializedName("status")
    var status: String? = null
    @SerializedName("errorMessage")
    var errorMessage: String? = null
    @SerializedName("results")
    var results: List<ElevationResult>? = null

    override fun successful(): Boolean {
        return "OK" == status
    }

    override fun getResult(): ElevationResult {
        return results!![0]
    }

    override fun getError(): ApiException? {
        return if (successful()) {
            null
        } else {
            ApiException.from(status, errorMessage)
        }
    }
}
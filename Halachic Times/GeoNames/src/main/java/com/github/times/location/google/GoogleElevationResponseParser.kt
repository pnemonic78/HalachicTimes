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

import com.github.json.JsonIgnore
import com.github.location.Location
import com.github.location.LocationException
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.google.maps.model.ElevationResult
import java.io.IOException
import java.io.InputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream

/**
 * Handler for parsing an elevation from a Google XML response.
 *
 * @author Moshe Waisberg
 */
internal class GoogleElevationResponseParser : ElevationResponseParser() {
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(LocationException::class, IOException::class)
    override fun parse(
        data: InputStream,
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Location> {
        return try {
            val response = JsonIgnore.decodeFromStream<ElevationResponse>(data)
            val results = mutableListOf<Location>()
            handleResponse(response, results, maxResults)
            results
        } catch (e: IllegalArgumentException) {
            throw LocationException(e)
        } catch (e: SerializationException) {
            throw LocationException(e)
        } catch (e: RuntimeException) {
            throw LocationException(e)
        }
    }

    @Throws(LocationException::class)
    private fun handleResponse(
        response: ElevationResponse?,
        results: MutableList<Location>,
        maxResults: Int
    ) {
        if (response == null) return
        if (!response.successful()) {
            throw LocationException(response.errorMessage)
        }
        val geocoderResult = response.result ?: return
        val location = toLocation(geocoderResult)
        results.add(location)
    }

    private fun toLocation(response: ElevationResult): Location {
        val location = response.location
        return Location(GeocoderBase.USER_PROVIDER).apply {
            latitude = location.lat
            longitude = location.lng
            altitude = response.elevation
            accuracy = response.resolution.toFloat()
        }
    }
}
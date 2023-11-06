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

import android.location.Location
import android.net.Uri
import com.github.json.UriAdapter
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import com.github.times.location.ZmanimLocation
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * Handler for parsing the Bing response for elevations.
 *
 * @author Moshe Waisberg
 */
class BingElevationResponseParser : ElevationResponseParser() {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriAdapter())
        .create()

    @Throws(LocationException::class, IOException::class)
    override fun parse(
        data: InputStream, latitude: Double, longitude: Double, maxResults: Int
    ): List<Location> {
        return try {
            val reader: Reader = InputStreamReader(data, StandardCharsets.UTF_8)
            val response = gson.fromJson(reader, BingResponse::class.java)
            val results = mutableListOf<Location>()
            handleResponse(response, results, latitude, longitude, maxResults)
            results
        } catch (e: JsonIOException) {
            throw IOException(e)
        } catch (e: JsonSyntaxException) {
            throw LocationException(e)
        }
    }

    @Throws(LocationException::class)
    private fun handleResponse(
        response: BingResponse?,
        results: MutableList<Location>,
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ) {
        if (response == null) return
        if (response.statusCode != BingResponse.STATUS_OK) {
            throw LocationException(response.statusDescription)
        }
        val resourceSets = response.resourceSets
        if (resourceSets.isNullOrEmpty()) {
            return
        }
        val resourceSet = resourceSets[0]
        val resources = resourceSet.resources
        if (resources.isNullOrEmpty()) {
            return
        }
        var location: Location?
        val size = maxResults.coerceAtMost(resources.size)
        for (resource in resources) {
            location = toLocation(resource, latitude, longitude)
            if (location != null) {
                results.add(location)
                if (results.size >= size) {
                    return
                }
            }
        }
    }

    private fun toLocation(response: BingResource, latitude: Double, longitude: Double): Location? {
        val result: Location = ZmanimLocation(GeocoderBase.USER_PROVIDER).apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        val point = response.point
        if (point != null) {
            val coordinates = point.coordinates
            if ((coordinates != null) && coordinates.size >= 2) {
                result.latitude = coordinates[0]
                result.longitude = coordinates[1]
            }
        }
        val elevations = response.elevations
        if (elevations.isNullOrEmpty()) {
            return null
        }
        result.altitude = elevations[0]
        return result
    }
}
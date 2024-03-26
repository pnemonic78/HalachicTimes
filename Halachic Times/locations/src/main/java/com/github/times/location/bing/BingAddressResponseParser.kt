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

import android.location.Address
import com.github.json.JsonIgnore
import com.github.times.location.AddressResponseParser
import com.github.times.location.LocationException
import com.github.times.location.ZmanimAddress
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.min
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream

/**
 * Handler for parsing the JSON response for addresses.
 *
 * @author Moshe Waisberg
 */
class BingAddressResponseParser : AddressResponseParser() {
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(LocationException::class, IOException::class)
    override fun parse(
        data: InputStream,
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        locale: Locale
    ): List<Address> {
        return try {
            val response = JsonIgnore.decodeFromStream<BingResponse>(data)
            val results = mutableListOf<Address>()
            handleResponse(response, results, maxResults, locale)
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
        response: BingResponse,
        results: MutableList<Address>,
        maxResults: Int,
        locale: Locale
    ) {
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
        var resource: BingResource
        var address: Address?
        val size = min(resources.size, maxResults)
        for (i in 0 until size) {
            resource = resources[i]
            address = toAddress(resource, locale)
            if (address != null) {
                results.add(address)
            }
        }
    }

    private fun toAddress(response: BingResource, locale: Locale): Address? {
        val result: Address = ZmanimAddress(locale)
        result.featureName = response.name
        val point = response.point ?: return null
        val coordinates = point.coordinates ?: return null
        if (coordinates.size < 2) {
            return null
        }
        result.latitude = coordinates[0]
        result.longitude = coordinates[1]
        val bingAddress = response.address ?: return null
        if (!bingAddress.addressLine.isNullOrEmpty()) {
            result.setAddressLine(0, bingAddress.addressLine)
        }
        result.adminArea = bingAddress.adminDistrict
        result.subAdminArea = bingAddress.adminDistrict2
        result.countryName = bingAddress.countryRegion
        result.locality = bingAddress.locality
        result.postalCode = bingAddress.postalCode
        val formatted = bingAddress.formattedAddress
        if (formatted != null && formatted == response.name) {
            result.featureName = null
        }
        return result
    }
}
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

import android.location.Address
import com.github.times.location.AddressResponseParser
import com.github.times.location.LocationException
import com.github.times.location.ZmanimAddress
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.maps.model.AddressComponentType
import com.google.maps.model.GeocodingResult
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Handler for parsing the JSON response for addresses.
 *
 * @author Moshe Waisberg
 */
internal class GoogleAddressResponseParser : AddressResponseParser() {
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(AddressComponentType::class.java, AddressComponentTypeAdapter())
        .create()

    @Throws(LocationException::class, IOException::class)
    override fun parse(
        data: InputStream,
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        locale: Locale
    ): List<Address> {
        return try {
            val reader: Reader = InputStreamReader(data, StandardCharsets.UTF_8)
            val response = gson.fromJson(reader, GeocodingResponse::class.java)
            val results: MutableList<Address> = ArrayList(maxResults)
            handleResponse(response, results, maxResults, locale)
            results
        } catch (e: JsonIOException) {
            throw IOException(e)
        } catch (e: JsonSyntaxException) {
            throw LocationException(e)
        }
    }

    @Throws(LocationException::class)
    private fun handleResponse(
        response: GeocodingResponse?,
        results: MutableList<Address>,
        maxResults: Int,
        locale: Locale
    ) {
        if (response == null) return
        if (!response.successful()) {
            throw LocationException(response.errorMessage)
        }
        val responseResults = response.results
        if (responseResults.isNullOrEmpty()) {
            return
        }
        var geocoderResult: GeocodingResult
        var address: Address?
        val size = responseResults.size.coerceAtMost(maxResults)
        for (i in 0 until size) {
            geocoderResult = responseResults[i]
            address = toAddress(geocoderResult, locale)
            if (address != null) {
                results.add(address)
            }
        }
    }

    private fun toAddress(response: GeocodingResult, locale: Locale): Address? {
        val location = response.geometry.location
        val result = ZmanimAddress(locale).apply {
            latitude = location.lat
            longitude = location.lng
        }
        var longName: String
        var shortName: String
        var addressComponentType: AddressComponentType
        var hasResult = false
        val components = response.addressComponents
        for (component in components) {
            longName = component.longName
            shortName = component.shortName
            if (component.types.isNullOrEmpty()) {
                continue
            }
            addressComponentType = component.types[0]

            when (addressComponentType) {
                AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1 -> {
                    result.adminArea = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2 -> {
                    result.subAdminArea = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.COUNTRY -> {
                    result.countryCode = shortName
                    result.countryName = longName
                    hasResult = true
                }

                AddressComponentType.LOCALITY -> {
                    result.locality = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.NATURAL_FEATURE -> {
                    result.featureName = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.POSTAL_CODE -> {
                    result.postalCode = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.PREMISE -> {
                    result.premises = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                AddressComponentType.ROUTE -> {
                    result.setAddressLine(
                        2,
                        if (shortName.isNullOrEmpty()) longName else shortName
                    )
                    hasResult = true
                }

                AddressComponentType.STREET_ADDRESS -> {
                    result.setAddressLine(1, if (shortName.isNullOrEmpty()) longName else shortName)
                    hasResult = true
                }

                AddressComponentType.STREET_NUMBER -> {
                    result.setAddressLine(0, if (shortName.isNullOrEmpty()) longName else shortName)
                    hasResult = true
                }

                AddressComponentType.SUBLOCALITY -> {
                    result.subLocality = if (shortName.isNullOrEmpty()) longName else shortName
                    hasResult = true
                }

                else -> Unit
            }
        }
        return if (hasResult) result else null
    }
}
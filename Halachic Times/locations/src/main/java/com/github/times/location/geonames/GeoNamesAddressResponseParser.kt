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
package com.github.times.location.geonames

import android.location.Address
import android.net.Uri
import com.github.json.UriAdapter
import com.github.times.location.AddressResponseParser
import com.github.times.location.LocationException
import com.github.times.location.ZmanimAddress
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.geonames.BoundingBox
import org.geonames.Timezone

/**
 * Handler for parsing the GeoNames response for addresses.
 *
 * @author Moshe Waisberg
 */
class GeoNamesAddressResponseParser : AddressResponseParser() {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriAdapter())
        .registerTypeAdapter(BoundingBox::class.java, GeoNamesBoxTypeAdapter())
        .registerTypeAdapter(Timezone::class.java, GeoNamesTimezoneAdapter())
        .setDateFormat("yyyy-MM-dd HH:mm")
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
            val response = gson.fromJson(reader, GeoNamesResponse::class.java)
            val results = mutableListOf<Address>()
            handleResponse(latitude, longitude, response, results, maxResults, locale)
            results
        } catch (e: JsonIOException) {
            throw IOException(e)
        } catch (e: JsonSyntaxException) {
            throw LocationException(e)
        } catch (e: RuntimeException) {
            throw LocationException(e)
        }
    }

    @Throws(LocationException::class)
    private fun handleResponse(
        latitude: Double,
        longitude: Double,
        response: GeoNamesResponse?,
        results: MutableList<Address>,
        maxResults: Int,
        locale: Locale
    ) {
        if (response == null) return
        val records = response.records
        var address: Address?
        if (records.isNullOrEmpty()) {
            val ocean = response.ocean
            if (ocean != null) {
                address = toAddress(ocean, locale, latitude, longitude)
                results.add(address)
                return
            }
            // No result found!
            val status = response.status
            if (status != null) {
                throw LocationException(status.message)
            }
            return
        }
        var toponym: Toponym?
        val size = records.size.coerceAtMost(maxResults)
        for (i in 0 until size) {
            toponym = records[i]
            address = toAddress(toponym, locale)
            results.add(address)
        }
    }

    private fun toAddress(response: Toponym, locale: Locale): Address =
        ZmanimAddress(locale).apply {
            latitude = response.latitude
            longitude = response.longitude
            elevation = response.elevation?.toDouble() ?: 0.0
            featureName = response.name
            adminArea = response.adminName1
            countryCode = response.countryCode
            countryName = response.countryName
            subAdminArea = response.adminName2
        }

    private fun toAddress(
        response: Ocean,
        locale: Locale,
        latitude: Double,
        longitude: Double
    ): Address = ZmanimAddress(locale).apply {
        this.latitude = latitude
        this.longitude = longitude
        elevation = 0.0
        featureName = response.name
        setFormatted(response.name)
    }
}
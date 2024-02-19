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
package com.github.times.location.country

import android.location.Address
import android.os.Parcel
import android.os.Parcelable
import com.github.times.location.ZmanimAddress
import com.github.times.location.country.CountryPolygon.Companion.toFixedPoint
import java.util.Locale
import kotlin.math.round

/**
 * Country that is stored in the application binary.
 *
 * @author Moshe Waisberg
 */
class Country : ZmanimAddress {
    constructor(locale: Locale) : super(locale)
    constructor(address: Address) : super(address)
    constructor(address: ZmanimAddress) : super(address)

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Country> = object : Parcelable.Creator<Country> {
            override fun createFromParcel(source: Parcel): Country {
                val a = ZmanimAddress.CREATOR.createFromParcel(source)
                return Country(a)
            }

            override fun newArray(size: Int): Array<Country?> {
                return arrayOfNulls(size)
            }
        }

        fun generateCountryId(country: Country): Long {
            return generateCountryId(country.latitude, country.longitude)
        }

        fun generateCountryId(latitude: Double, longitude: Double): Long {
            val fixedPointLatitude = toFixedPoint(latitude).toLong() and 0x7FFFFFFFL
            val fixedPointLongitude = toFixedPoint(longitude).toLong() and 0xFFFFFFFFL
            return -((fixedPointLatitude shl 31) or fixedPointLongitude)
        }
    }
}
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
package com.github.times.location

import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.github.os.getParcelableCompat
import com.github.util.StringUtils
import java.util.Locale

/**
 * Address that is stored in the local database.
 *
 * @author Moshe Waisberg
 */
open class ZmanimAddress : Address, Comparable<ZmanimAddress> {
    var id: Long = 0

    /** The elevation, in metres. */
    var elevation = 0.0
        get() {
            if (hasElevation) {
                return field
            }
            throw IllegalStateException()
        }
        set(value) {
            field = value
            hasElevation = true
        }
    private var hasElevation = false
    var isFavorite = false

    /** The formatted address. */
    private var _formatted: String? = null

    val formatted: String get() = getFormattedAddress()

    /**
     * Constructs a new address.
     *
     * @param locale the locale.
     */
    constructor(locale: Locale) : super(locale)

    /**
     * Constructs a new address.
     *
     * @param address the source address.
     */
    constructor(address: Address) : super(address.locale) {
        var index = 0
        val maxIndex = address.maxAddressLineIndex
        for (i in 0..maxIndex) {
            val line = address.getAddressLine(index)
            if (line.isNullOrEmpty()) continue
            setAddressLine(index++, line)
        }
        adminArea = address.adminArea
        countryCode = address.countryCode
        countryName = address.countryName
        extras = address.extras
        featureName = address.featureName
        latitude = address.latitude
        locality = address.locality
        longitude = address.longitude
        phone = address.phone
        postalCode = address.postalCode
        premises = address.premises
        subAdminArea = address.subAdminArea
        subLocality = address.subLocality
        subThoroughfare = address.subThoroughfare
        thoroughfare = address.thoroughfare
        url = address.url
    }

    /**
     * Constructs a new address.
     *
     * @param address the source address.
     */
    constructor(address: ZmanimAddress) : this(address as Address) {
        this.id = address.id
        if (address.hasElevation()) {
            elevation = address.elevation
        }
        setFormatted(address.formatted)
        this.isFavorite = address.isFavorite
    }

    /**
     * Returns true if an elevation has been assigned to this Address, false
     * otherwise.
     */
    fun hasElevation(): Boolean {
        return hasElevation
    }

    private fun getFormattedAddress(): String {
        var f = _formatted
        if (f == null) {
            f = format()
            _formatted = f
        }
        return f
    }

    fun setFormatted(value: String?) {
        _formatted = value
    }

    /**
     * Format the address.
     *
     * @return the formatted address.
     */
    protected fun format(): String {
        val formatted = extras?.getString(KEY_FORMATTED)
        if (formatted != null) return formatted

        val buf = StringBuilder()
        val feature: String? = featureName
        val premises: String? = premises
        val thoroughfare: String? = thoroughfare
        val subloc: String? = subLocality
        val locality: String? = locality
        val subadmin: String? = subAdminArea
        val admin: String? = adminArea
        val country: String? = countryName
        val addressLinesCount = maxAddressLineIndex + 1
        var address: String?

        if (!feature.isNullOrEmpty()) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(feature)
        }
        if (!premises.isNullOrEmpty()
            && premises != feature
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(premises)
        }
        if (!thoroughfare.isNullOrEmpty()
            && thoroughfare != feature
            && thoroughfare != premises
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(thoroughfare)
        }
        if (addressLinesCount >= 0) {
            for (i in 0 until addressLinesCount) {
                address = getAddressLine(i)
                if (!address.isNullOrEmpty()
                    && (thoroughfare == null || !address.contains(thoroughfare))
                    && (premises == null || !address.contains(premises))
                    && (subloc == null || !address.contains(subloc))
                    && (locality == null || !address.contains(locality))
                    && (subadmin == null || !address.contains(subadmin))
                    && (admin == null || !address.contains(admin))
                    && (country == null || !address.contains(country))
                ) {
                    if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
                    buf.append(address)
                }
            }
        }
        if (!subloc.isNullOrEmpty()
            && subloc != thoroughfare
            && subloc != feature
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(subloc)
        }
        if (!locality.isNullOrEmpty()
            && locality != subloc
            && locality != feature
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(locality)
        }
        if (!subadmin.isNullOrEmpty()
            && subadmin != locality
            && subadmin != subloc
            && subadmin != feature
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(subadmin)
        }
        if (!admin.isNullOrEmpty()
            && admin != subadmin
            && admin != locality
            && admin != subloc
            && admin != feature
        ) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(admin)
        }
        if (!country.isNullOrEmpty() && country != feature) {
            if (buf.isNotEmpty()) buf.append(ADDRESS_SEPARATOR)
            buf.append(country)
        }
        if (buf.isEmpty()) {
            val locale = locale
            return locale.getDisplayCountry(locale)
        }
        return buf.toString()
    }

    override fun compareTo(other: ZmanimAddress): Int {
        var c = compare(this, other)
        if (c != 0) return c

        val format1 = formatted
        val format2 = other.formatted
        c = StringUtils.compareTo(format1, format2, ignoreCase = true)
        if (c != 0) return c

        val id1 = id
        val id2 = other.id
        return id1.compareTo(id2)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is ZmanimAddress) {
            return compareTo(other) == 0
        }
        return super.equals(other)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(id)
        if (hasElevation) {
            parcel.writeInt(1)
            parcel.writeDouble(elevation)
        } else {
            parcel.writeInt(0)
        }
        parcel.writeString(formatted)
        parcel.writeInt(if (isFavorite) 1 else 0)
    }

    override fun setCountryCode(countryCode: String?) {
        var cc = countryCode
        if (ISO639_PALESTINE == cc) {
            cc = ISO639_ISRAEL
            val locale = Locale(locale.language, cc)
            countryName = locale.getDisplayCountry(locale)
        }
        super.setCountryCode(cc)
    }

    override fun setCountryName(countryName: String?) {
        if (getCountryName() != null) return
        super.setCountryName(countryName)
    }

    override fun toString(): String {
        val formatted = this.formatted
        if (formatted.isEmpty()) return super.toString()
        return super.toString() + "[" + formatted + "]"
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        /**
         * Key to store the formatted address, instead of formatting it ourselves
         * elsewhere.
         */
        const val KEY_FORMATTED = "formatted_address"

        /**
         * ISO 639 country code for Israel.
         */
        const val ISO639_ISRAEL = "IL"

        /**
         * ISO 639 country code for Palestine.
         */
        const val ISO639_PALESTINE = "PS"

        /**
         * Address field separator.
         */
        private const val ADDRESS_SEPARATOR = ", "

        /**
         * Double subtraction error.
         */
        private const val EPSILON = 1e-6

        /**
         * Compare two addresses by latitude and longitude only.
         *
         * @param a1 the first address.
         * @param a2 the second address.
         * @return the comparison as per [Comparable].
         */
        fun compare(a1: Address?, a2: Address?): Int {
            if (a1 === a2) {
                return 0
            }
            if (a1 == null) {
                return -1
            }
            if (a2 == null) {
                return 1
            }

            val lat1 = a1.latitude
            val lat2 = a2.latitude
            val latD = lat1 - lat2
            if (latD >= EPSILON) return 1
            if (latD <= -EPSILON) return -1

            val lng1 = a1.longitude
            val lng2 = a2.longitude
            val lngD = lng1 - lng2
            if (lngD >= EPSILON) return 1
            if (lngD <= -EPSILON) return -1

            // Don't compare elevation.
            return 0
        }

        @JvmField
        val CREATOR: Parcelable.Creator<ZmanimAddress> =
            object : Parcelable.Creator<ZmanimAddress> {
                override fun createFromParcel(source: Parcel): ZmanimAddress {
                    val address = Address.CREATOR.createFromParcel(source)
                    return ZmanimAddress(address).apply {
                        id = source.readLong()
                        hasElevation = source.readInt() != 0
                        if (hasElevation) {
                            elevation = source.readDouble()
                        }
                        setFormatted(source.readString())
                        isFavorite = source.readInt() != 0
                    }
                }

                override fun newArray(size: Int): Array<ZmanimAddress?> {
                    return arrayOfNulls(size)
                }
            }

        fun isValid(address: ZmanimAddress?): Boolean {
            if (address == null) return false
            if (!address.hasLatitude()) return false
            val latitude = address.latitude
            if (latitude < ZmanimLocation.LATITUDE_MIN || latitude > ZmanimLocation.LATITUDE_MAX) return false
            if (!address.hasLongitude()) return false
            val longitude = address.longitude
            if (longitude < ZmanimLocation.LONGITUDE_MIN || longitude > ZmanimLocation.LONGITUDE_MAX) return false
            if (address.hasElevation()) {
                val elevation = address.elevation
                return elevation in ZmanimLocation.ELEVATION_MIN..ZmanimLocation.ELEVATION_MAX
            }
            return true
        }
    }
}

fun Bundle.put(key: String, address: Address) {
    putParcelable(key, address)
}

fun Intent.put(key: String, address: Address): Intent {
    putExtra(key, address)
    return this
}

fun Bundle.getAddress(key: String): ZmanimAddress? {
    return getParcelableCompat(key, ZmanimAddress::class.java)
}

fun Intent.getAddress(key: String): ZmanimAddress? {
    return getParcelableCompat(key, ZmanimAddress::class.java)
}
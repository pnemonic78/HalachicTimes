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
package com.github.geonames

import org.geonames.InsufficientStyleException
import org.geonames.Toponym

/**
 * GeoNames data record.
 *
 * @author Moshe Waisberg
 */
open class GeoNamesRecord : Toponym() {
    /**
     * The ASCII name.
     */
    var asciiName: String? = null

    /**
     * The alternate country codes. A comma-separated list of codes.
     */
    var alternateCountryCodes: String? = null

    /**
     * The SRTM3 elevation.
     */
    var digitalElevation: Int? = null
        private set

    /**
     * The modification.
     */
    var modification: String? = null

    /**
     * Set the country code.
     *
     * @param countryCode the country code.
     */
    override fun setCountryCode(countryCode: String) {
        var code = countryCode
        if (CountryInfo.ISO639_PALESTINE == code) {
            code = CountryInfo.ISO639_ISRAEL
        }
        super.setCountryCode(code)
    }

    /**
     * Set the SRTM3 elevation.
     *
     * @param dem the elevation.
     */
    fun setDigitalElevation(dem: Int) {
        digitalElevation = dem
    }

    override fun hashCode(): Int {
        return geoNameId
    }

    val grossElevation: Int?
        get() {
            var elevation: Int? = null
            try {
                elevation = getElevation()
            } catch (ignore: InsufficientStyleException) {
            }
            if (elevation == null) {
                elevation = digitalElevation
            }
            return elevation
        }
}

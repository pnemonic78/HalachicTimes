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

import org.geonames.GeoNameId

/**
 * Country information.
 *
 * @author Moshe Waisberg
 */
data class CountryInfo(
    var iso: String? = null,
    var iso3: String? = null,
    var isoNumeric: Int = 0,
    var fips: String? = null,
    var country: String? = null,
    var capital: String? = null,
    var area: Double = 0.0,
    var population: Long = 0,
    var continent: String? = null,
    var tld: String? = null,
    var currencyCode: String? = null,
    var currencyName: String? = null,
    var phone: String? = null,
    var postalCodeFormat: String? = null,
    var postalCodeRegex: String? = null,
    var languages: List<String>? = null,
    var geoNameId: GeoNameId = 0,
    var neighbours: List<String>? = null,
    var equivalentFipsCode: String? = null
)
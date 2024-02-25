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
package org.geonames

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Alternate name.
 *
 * @author Moshe Waisberg
 */
@Serializable
data class AlternateName(

    var alternateNameId: Int = 0,

    var geoNameId: GeoNameId = 0,

    /** alternate name or name variant */
    @SerialName("name")
    var name: String = "",

    /** iso 639 language code 2- or 3-character */
    @SerialName("lang")
    var language: String = "",

    /** if this alternate name is an official/preferred name */
    @SerialName("isPreferredName")
    var isPreferred: Boolean = false,

    /** if this is a short name like 'California' for 'State of California' */
    @SerialName("isShortName")
    var isShort: Boolean = false,

    /** if this alternate name is a colloquial or slang term */
    @SerialName("isColloquial")
    var isColloquial: Boolean = false,

    /** if this alternate name is historic and was used in the past */
    @SerialName("isHistoric")
    var isHistoric: Boolean = false
) {
    companion object {

        /**
         * Abbreviation
         */
        const val TYPE_ABBR = "abbr"

        /**
         * Airport codes
         */
        const val TYPE_FAAC = "faac"

        /**
         * French Revolution names
         */
        const val TYPE_FR_1793 = "fr_1793"

        /**
         * Airport codes
         */
        const val TYPE_IATA = "iata"

        /**
         * Airport codes
         */
        const val TYPE_ICAO = "icao"

        /**
         * URL pointing to a website.
         */
        const val TYPE_LINK = "link"

        /**
         * Phonetics
         */
        const val TYPE_PHON = "phon"

        /**
         * Pinyin
         */
        const val TYPE_PINY = "piny"

        /**
         * Postal codes
         */
        const val TYPE_POST = "post"

        /**
         * Airport codes
         */
        const val TYPE_TCID = "tcid"

        /**
         * UN/LOCODE
         * "United Nations Code for Trade and Transport Locations"
         */
        const val TYPE_UNLC = "unlc"

        /**
         * Wikidata id
         */
        const val TYPE_WKDT = "wkdt"
    }
}

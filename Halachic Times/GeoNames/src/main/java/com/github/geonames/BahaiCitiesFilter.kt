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

/**
 * Name filter for Bahai cities.
 *
 * @author Moshe Waisberg
 */
class BahaiCitiesFilter : ToponymFilter {

    override fun accept(toponym: GeoNamesToponym): Boolean {
        return CITIES.binarySearch(toponym.geoNameId) >= 0
    }

    override fun replaceLocation(toponym: GeoNamesToponym) = Unit

    companion object {
        private const val ADDIS_ABABA = 344979
        private const val ISTANBUL = 745044
        private const val KIEV = 703448
        private const val OSLO = 3143244
        private const val PARIS = 2988507
        private const val RIO_DE_JANEIRO = 3451190
        private const val VIENNA = 2761369

        /**
         * List of Bahai cities' GeoName IDs.
         */
        private val CITIES = intArrayOf(
            295721,  // Acre, Israel
            ADDIS_ABABA,  // Addis Ababa, Ethiopia
            2759794,  // Amsterdam, Netherlands
            5879400,  // Anchorage, United States
            3041563,  // Andorra la Vella, Andorra
            1070940,  // Antananarivo, Madagascar
            4035413,  // Apia, Samoa
            3439389,  // Asunción, Paraguay
            2193733,  // Auckland, New Zealand
            98182,  // Baghdad, Iraq
            587084,  // Baku, Azerbaijan
            1831797,  // Battambang, Cambodia
            2655984,  // Belfast, Ireland
            88319,  // Benghazi, Libya
            2950159,  // Berlin, Germany
            2661552,  // Bern, Switzerland
            3688689,  // Bogota, Colombia
            3060972,  // Bratislava, Slovakia
            2260535,  // Brazzaville, Congo
            2800866,  // Brussels, Belgium
            3054643,  // Budapest, Hungary
            3435910,  // Buenos Aires, Argentina
            360630,  // Cairo, Egypt
            3369157,  // Cape Town, South Africa
            3646738,  // Caracas, Venezuela
            2553604,  // Casablanca, Morocco
            2618425,  // Copenhagen, Denmark
            2253354,  // Dakar, Senegal
            160196,  // Dodoma, Tanzania
            4963642,  // Eliot (Maine), United States
            933773,  // Gaborone, Botswana
            2648579,  // Glasgow, Scotland
            3598132,  // Guatemala City, Guatemala
            294801,  // Haifa, Israel
            658225,  // Helsinki, Finland
            1819729,  // Hong Kong, China
            ISTANBUL,  // Istanbul, Turkey
            1642911,  // Jakarta, Indonesia
            281184,  // Jerusalem, Israel
            993800,  // Johannesburg, South Africa
            1138958,  // Kabul, Afghanistan
            232422,  // Kampala, Uganda
            1174872,  // Karachi, Pakistan
            KIEV,  // Kiev, Ukraine
            202061,  // Kigali, Rwanda
            3489854,  // Kingston, Jamaica
            3911925,  // La Paz, Bolivia
            927967,  // Lilongwe, Malawi
            3936456,  // Lima, Peru
            2229411,  // Limbe, Cameroon
            2267057,  // Lisbon, Portugal
            2643743,  // London, United Kingdom
            5368361,  // Los Angeles, United States
            1821274,  // Macau, China
            3117735,  // Madrid, Spain
            3617763,  // Managua, Nicaragua
            1701668,  // Manila, Philippines
            3530597,  // Mexico City, Mexico
            3441575,  // Montevideo, Uruguay
            524901,  // Moscow, Russia
            1275339,  // Mumbai, India
            184745,  // Nairobi, Kenya
            2427123,  // N'Djamena, Chad
            1273294,  // New Delhi, India
            5128581,  // New York, United States
            OSLO,  // Oslo, Norway
            3703443,  // Panama City, Panama
            PARIS,  // Paris, France
            3718426,  // Port-au-Prince, Haiti
            3652462,  // Quito, Ecuador
            3413829,  // Reykjavík, Iceland
            RIO_DE_JANEIRO,  // Rio de Janeiro, Brazil
            3169070,  // Rome, Italy
            3621841,  // San José, Costa Rica
            3583361,  // San Salvador, El Salvador
            3871336,  // Santiago, Chile
            3492908,  // Santo Domingo, Dominican Republic
            3191281,  // Sarajevo, Bosnia and Herzegovina
            1796236,  // Shanghai, China
            2673730,  // Stockholm, Sweden
            2198148,  // Suva, Fiji
            2147714,  // Sydney, Australia
            1668341,  // Taipei, Taiwan, China
            3600949,  // Tegucigalpa, Honduras
            293397,  // Tel Aviv, Israel
            112931,  // Tehran, Iran
            1850147,  // Tokyo, Japan
            6167865,  // Toronto, Canada
            2464470,  // Tunis, Tunisia
            6173331,  // Vancouver, Canada
            VIENNA,  // Vienna, Austria
            756135,  // Warsaw, Poland
            4916732,  // Wilmette (Illinois), United States
            1298824,  // Yangon, Myanmar
            2220957,  // Yaoundé, Cameroon
            616052
        ).sorted()
    }
}

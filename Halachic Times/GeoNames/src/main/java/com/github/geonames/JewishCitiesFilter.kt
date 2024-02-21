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
 * Name filter for Jewish cities.
 *
 * @author Moshe Waisberg
 */
class JewishCitiesFilter : NameFilter {

    override fun accept(name: GeoNamesToponym): Boolean {
        return CITIES.binarySearch(name.geoNameId) >= 0
    }

    override fun replaceLocation(name: GeoNamesToponym) {
        when (name.geoNameId) {
            ADDIS_ABABA -> {
                name.latitude = 9.0350628
                name.longitude = 38.7486724
            }
            ANTWERP -> {
                name.latitude = 51.2199612
                name.longitude = 4.3861885
            }
            ISTANBUL -> {
                name.latitude = 41.0128072
                name.longitude = 28.9550702
            }
            KIEV -> {
                name.latitude = 50.446306
                name.longitude = 30.5180833
            }
            OSLO -> {
                name.latitude = 59.9119497
                name.longitude = 10.7313994
            }
            PARIS -> {
                name.latitude = 48.8657367
                name.longitude = 2.3382167
            }
            PORTLAND -> {
                name.latitude = 45.4275604
                name.longitude = -122.814655
            }
            RIO_DE_JANEIRO -> {
                name.latitude = -22.9041251
                name.longitude = -43.5734578
            }
            VIENNA -> {
                name.latitude = 48.2108685
                name.longitude = 16.3550599
            }
        }
    }

    companion object {
        private const val ADDIS_ABABA = 344979
        private const val ANTWERP = 2803138
        private const val ISTANBUL = 745044
        private const val KIEV = 703448
        private const val OSLO = 3143244
        private const val PARIS = 2988507
        private const val PORTLAND = 5746545
        private const val RIO_DE_JANEIRO = 3451190
        private const val VIENNA = 2761369

        /**
         * List of Jewish cities' GeoName IDs.
         */
        private val CITIES = intArrayOf(
            295721,  // Acre, Israel
            ADDIS_ABABA,  // Addis Ababa, Ethiopia
            295740,  // Afula, Israel
            2759794,  // Amsterdam, Netherlands
            5879400,  // Anchorage, United States
            ANTWERP,  // Antwerp, Belgium
            295657,  // Arad, Israel
            8199394,  // Ariel, Israel
            295629,  // Ashdod, Israel
            295620,  // Ashkelon / Ashqelon, Israel
            4180439,  // Atlanta, United States
            2193733,  // Auckland, New Zealand
            295584,  // Azor, Israel
            4347778,  // Baltimore, United States
            295548,  // Bat Yam, Israel
            295435,  // Beit She’an, Israel
            295530,  // Be'er Sheva, Israel
            295432,  // Beit Shemesh, Israel
            2655984,  // Belfast, Ireland
            2950159,  // Berlin, Germany
            2661552,  // Bern, Switzerland
            295514,  // Bnei Brak, Israel
            3688689,  // Bogota, Colombia
            4930956,  // Boston, United States
            3054643,  // Budapest, Hungary
            3435910,  // Buenos Aires, Argentina
            3369157,  // Cape Town, South Africa
            2553604,  // Casablanca, Morocco
            4887398,  // Chicago, United States
            5150529,  // Cleveland, OH, United States
            2618425,  // Copenhagen, Denmark
            4684888,  // Dallas, United States
            5419384,  // Denver, United States
            4990729,  // Detroit, United States
            295328,  // Dimona, Israel
            1007311,  // Durban, South Africa
            295277,  // Eilat, Israel
            295122,  // Even Yehuda, Israel
            295080,  // Gan Yavne, Israel
            295089,  // Ganei Tikva, Israel
            295064,  // Gedera, Israel
            2660646,  // Geneve, Switzerland
            294981,  // Givat Shmuel, Israel
            294999,  // Givatayim, Israel
            294946,  // Hadera, Israel
            294801,  // Haifa, Israel
            658225,  // Helsinki, Finland
            294778,  // Herzliya, Israel
            294760,  // Hod HaSharon, Israel
            294751,  // Holon, Israel
            4699066,  // Houston, United States
            ISTANBUL,  // Istanbul, Turkey
            293253,  // Jaffa, Israel
            281184,  // Jerusalem, Israel
            993800,  // Johannesburg, South Africa
            294577,  // Karmiel, Israel
            294514,  // Kfar Saba, Israel
            294492,  // Kfar Yona, Israel
            KIEV,  // Kiev, Ukraine
            5475433,  // Las Vegas, United States
            2267057,  // Lisbon, Portugal
            294421,  // Lod, Israel
            2643743,  // London, United Kingdom
            5368361,  // Los Angeles, United States
            2996944,  // Lyon, France
            3117735,  // Madrid, Spain
            2643123,  // Manchester, United Kingdom
            2995469,  // Marseille, France
            2158177,  // Melbourne, Australia
            294245,  // Mevasseret Zion, Israel
            3530597,  // Mexico City, Mexico
            4164138,  // Miami, United States
            294210,  // Migdal Ha‘Emeq, Israel
            282926,  // Modiin, Israel
            8199378,  // Modiin Ilit, Israel
            6077243,  // Montreal, Canada
            524901,  // Moscow, Russia
            1275339,  // Mumbai, India
            2867714,  // Munich, Germany
            294117,  // Nahariya, Israel
            294098,  // Nazareth, Israel
            294078,  // Nesher, Israel
            294074,  // Ness Ziona, Israel
            294071,  // Netanya, Israel
            294068,  // Netivot, Israel
            1273294,  // New Delhi, India
            5128581,  // New York, United States
            293992,  // Ofaqim, Israel
            293962,  // Or Yehuda, Israel
            OSLO,  // Oslo, Norway
            PARIS,  // Paris, France
            2063523,  // Perth, Australia
            293918,  // Petah Tiqwa, Israel
            4560349,  // Philadelphia, United States
            5308655,  // Phoenix, United States
            5206379,  // Pittsburgh, United States
            PORTLAND,  // Portland, Oregon, United States
            443093,  // Qatsrin, Israel
            293845,  // Qiryat Ata, Israel
            293844,  // Qiryat Bialik, Israel
            293842,  // Qiryat Gat, Israel
            293831,  // Qiryat Mozqin, Israel
            11049562,  // Qiryat Ono, Israel
            293825,  // Qiryat Shemona, Israel
            293822,  // Qiryat Yam, Israel
            293807,  // Raanana, Israel
            293788,  // Ramat Gan, Israel
            293783,  // Ramat HaSharon, Israel
            293725,  // Rehovot, Israel
            RIO_DE_JANEIRO,  // Rio de Janeiro, Brazil
            293703,  // Rishon LeZion, Israel
            3169070,  // Rome, Italy
            293690,  // Rosh Ha‘Ayin, Israel
            293100,  // Safed / Zefat, Israel
            3191281,  // Sarajevo, Bosnia and Herzegovina
            5809844,  // Seattle, United States
            293619,  // Sederot, Israel
            4407066,  // St Louis, United States
            498817,  // St Petersburg, Russia
            5391811,  // San Diego, United States
            5391959,  // San Francisco, United States
            3448439,  // Sao Paulo, Brazil
            8428283,  // Shoham, Israel
            2673730,  // Stockholm, Sweden
            2147714,  // Sydney, Australia
            293426,  // Tamra, Israel
            293397,  // Tel Aviv, Israel
            293322,  // Tiberias, Israel
            295127,  // Tirah, Israel
            293308,  // Tirat Karmel, Israel
            1850147,  // Tokyo, Japan
            6167865,  // Toronto, Canada
            6173331,  // Vancouver, Canada
            VIENNA,  // Vienna, Austria
            756135,  // Warsaw, Poland
            4140963,  // Washington D.C., United States
            293222,  // Yavne, Israel
            293207,  // Yehud, Israel
            10227184,  // Yehud Monosson, Israel
            2657896  // Zurich, Switzerland
        ).sorted()
    }
}

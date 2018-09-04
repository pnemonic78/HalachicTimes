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
package com.github.geonames;

import java.util.Arrays;

/**
 * Name filter for Jewish cities.
 *
 * @author Moshe Waisberg
 */
public class JewishCitiesFilter implements NameFilter {

    private static final long ADDIS_ABABA = 344979;
    private static final long ANTWERP = 2803138;
    private static final long ISTANBUL = 745044;
    private static final long KIEV = 703448;
    private static final long OSLO = 3143244;
    private static final long PARIS = 2988507;
    private static final long PORTLAND = 5746545;
    private static final long RIO_DE_JANEIRO = 3451190;
    private static final long VIENNA = 2761369;

    /**
     * List of Jewish cities' GeoName IDs.
     */
    private static final long[] CITIES = {
            // Acre, Israel
            295721,
            // Addis Ababa, Ethiopia
            ADDIS_ABABA,
            // Afula, Israel
            295740,
            // Amsterdam, Netherlands
            2759794,
            // Anchorage, United States
            5879400,
            // Antwerp, Belgium
            ANTWERP,
            // Arad, Israel
            295657,
            // Ariel, Israel
            8199394,
            // Ashdod, Israel
            295629,
            // Ashkelon / Ashqelon, Israel
            295620,
            // Atlanta, United States
            4180439,
            // Auckland, New Zealand
            2193733,
            // Azor, Israel
            295584,
            // Baltimore, United States
            4347778,
            // Bat Yam, Israel
            295548,
            // Beit She’an, Israel
            295435,
            // Be'er Sheva, Israel
            295530,
            // Beit Shemesh, Israel
            295432,
            // Belfast, Ireland
            2655984,
            // Berlin, Germany
            2950159,
            // Bern, Switzerland
            2661552,
            // Bnei Brak, Israel
            295514,
            // Bogota, Colombia
            3688689,
            // Boston, United States
            4930956,
            // Budapest, Hungary
            3054643,
            // Buenos Aires, Argentina
            3435910,
            // Cape Town, South Africa
            3369157,
            // Casablanca, Morocco
            2553604,
            // Chicago, United States
            4887398,
            // Cleveland, OH, United States
            5150529,
            // Copenhagen, Denmark
            2618425,
            // Dallas, United States
            4684888,
            // Denver, United States
            5419384,
            // Detroit, United States
            4990729,
            // Dimona, Israel
            295328,
            // Durban, South Africa
            1007311,
            // Eilat, Israel
            295277,
            // Even Yehuda, Israel
            295122,
            // Gan Yavne, Israel
            295080,
            // Ganei Tikva, Israel
            295089,
            // Gedera, Israel
            295064,
            // Geneve, Switzerland
            2660646,
            // Givat Shmuel, Israel
            294981,
            // Givatayim, Israel
            294999,
            // Hadera, Israel
            294946,
            // Haifa, Israel
            294801,
            // Helsinki, Finland
            658225,
            // Herzliya, Israel
            294778,
            // Hod HaSharon, Israel
            294760,
            // Holon, Israel
            294751,
            // Houston, United States
            4699066,
            // Istanbul, Turkey
            ISTANBUL,
            // Jaffa, Israel
            293253,
            // Jerusalem, Israel
            281184,
            // Johannesburg, South Africa
            993800,
            // Karmiel, Israel
            294577,
            // Kfar Saba, Israel
            294514,
            // Kfar Yona, Israel
            294492,
            // Kiev, Ukraine
            KIEV,
            // Las Vegas, United States
            5475433,
            // Lisbon, Portugal
            2267057,
            // Lod, Israel
            294421,
            // London, United Kingdom
            2643743,
            // Los Angeles, United States
            5368361,
            // Lyon, France
            2996944,
            // Madrid, Spain
            3117735,
            // Manchester, United Kingdom
            2643123,
            // Marseille, France
            2995469,
            // Melbourne, Australia
            2158177,
            // Mevasseret Zion, Israel
            294245,
            // Mexico City, Mexico
            3530597,
            // Miami, United States
            4164138,
            // Migdal Ha‘Emeq, Israel
            294210,
            // Modiin, Israel
            282926,
            // Modiin Ilit, Israel
            8199378,
            // Montreal, Canada
            6077243,
            // Moscow, Russia
            524901,
            // Mumbai, India
            1275339,
            // Munich, Germany
            2867714,
            // Nahariya, Israel
            294117,
            // Nazareth, Israel
            294098,
            // Nesher, Israel
            294078,
            // Ness Ziona, Israel
            294074,
            // Netanya, Israel
            294071,
            // Netivot, Israel
            294068,
            // New Delhi, India
            1273294,
            // New York, United States
            5128581,
            // Ofaqim, Israel
            293992,
            // Or Yehuda, Israel
            293962,
            // Oslo, Norway
            OSLO,
            // Paris, France
            PARIS,
            // Perth, Australia
            2063523,
            // Petah Tiqwa, Israel
            293918,
            // Philadelphia, United States
            4560349,
            // Phoenix, United States
            5308655,
            // Pittsburgh, United States
            5206379,
            // Portland, Oregon, United States
            PORTLAND,
            // Qiryat Ata, Israel
            293845,
            // Qiryat Bialik, Israel
            293844,
            // Qiryat Gat, Israel
            293842,
            // Qiryat Mozqin, Israel
            293831,
            // Qiryat Ono, Israel
            11049562,
            // Qiryat Shemona, Israel
            293825,
            // Qiryat Yam, Israel
            293822,
            // Raanana, Israel
            293807,
            // Ramat Gan, Israel
            293788,
            // Ramat HaSharon, Israel
            293783,
            // Rehovot, Israel
            293725,
            // Rio de Janeiro, Brazil
            RIO_DE_JANEIRO,
            // Rishon LeZion, Israel
            293703,
            // Rome, Italy
            3169070,
            // Rosh Ha‘Ayin, Israel
            293690,
            // Safed / Zefat, Israel
            293100,
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Seattle, United States
            5809844,
            // Sederot, Israel
            293619,
            // St Louis, United States
            4407066,
            // St Petersburg, Russia
            498817,
            // San Diego, United States
            5391811,
            // San Francisco, United States
            5391959,
            // Sao Paulo, Brazil
            3448439,
            // Shoham, Israel
            8428283,
            // Stockholm, Sweden
            2673730,
            // Sydney, Australia
            2147714,
            // Tamra, Israel
            293426,
            // Tel Aviv, Israel
            293397,
            // Tiberias, Israel
            293322,
            // Tirah, Israel
            295127,
            // Tirat Karmel, Israel
            293308,
            // Tokyo, Japan
            1850147,
            // Toronto, Canada
            6167865,
            // Vancouver, Canada
            6173331,
            // Vienna, Austria
            VIENNA,
            // Warsaw, Poland
            756135,
            // Washington D.C., United States
            4140963,
            // Yavne, Israel
            293222,
            // Yehud, Israel
            293207,
            // Yehud Monosson, Israel
            10227184,
            // Zurich, Switzerland
            2657896
    };

    public JewishCitiesFilter() {
        Arrays.sort(CITIES);
    }

    @Override
    public boolean accept(GeoName name) {
        return Arrays.binarySearch(CITIES, name.getGeoNameId()) >= 0;
    }

    @Override
    public void replaceLocation(GeoName name) {
        final long id = name.getGeoNameId();

        if (id == ADDIS_ABABA) {
            name.setLatitude(9.0350628);
            name.setLongitude(38.7486724);
        } else if (id == ANTWERP) {
            name.setLatitude(51.2199612);
            name.setLongitude(4.3861885);
        } else if (id == ISTANBUL) {
            name.setLatitude(41.0128072);
            name.setLongitude(28.9550702);
        } else if (id == KIEV) {
            name.setLatitude(50.446306);
            name.setLongitude(30.5180833);
        } else if (id == OSLO) {
            name.setLatitude(59.9119497);
            name.setLongitude(10.7313994);
        } else if (id == PARIS) {
            name.setLatitude(48.8657367);
            name.setLongitude(2.3382167);
        } else if (id == PORTLAND) {
            name.setLatitude(45.4275604);
            name.setLongitude(-122.814655);
        } else if (id == RIO_DE_JANEIRO) {
            name.setLatitude(-22.9041251);
            name.setLongitude(-43.5734578);
        } else if (id == VIENNA) {
            name.setLatitude(48.2108685);
            name.setLongitude(16.3550599);
        }
    }
}

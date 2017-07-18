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
package net.sf.geonames;

import java.util.Arrays;

/**
 * Name filter for Bahai cities.
 *
 * @author Moshe Waisberg
 */
public class BahaiCitiesFilter implements NameFilter {

    /**
     * List of Bahai cities' GeoName IDs.
     */
    private static final long[] CITIES = {
            // Acre, Israel
            295721,
            // Amsterdam, Netherlands
            2759794,
            // Anchorage, United States
            5879400,
            // Andorra la Vella, Andorra
            3041563,
            // Antananarivo, Madagascar
            1070940,
            // Apia, Samoa
            4035413,
            // Asunción, Paraguay
            3439389,
            // Auckland, New Zealand
            2193733,
            // Baghdad, Iraq
            98182,
            // Baku, Azerbaijan
            587084,
            // Battambang, Cambodia
            1831797,
            // Belfast, Ireland
            2655984,
            // Benghazi, Libya
            88319,
            // Berlin, Germany
            2950159,
            // Bern, Switzerland
            2661552,
            // Bogota, Colombia
            3688689,
            // Bratislava, Slovakia
            3060972,
            // Brazzaville, Congo
            2260535,
            // Brussels, Belgium
            2800866,
            // Budapest, Hungary
            3054643,
            // Buenos Aires, Argentina
            3435910,
            // Cairo, Egypt
            360630,
            // Caracas, Venezuela
            3646738,
            // Casablanca, Morocco
            2553604,
            // Copenhagen, Denmark
            2618425,
            // Dakar, Senegal
            2253354,
            // Dodoma, Tanzania
            160196,
            // Eliot (Maine), United States
            4963642,
            // Gaborone, Botswana
            933773,
            // Glasgow, Scotland
            2648579,
            // Guatemala City, Guatemala
            3598132,
            // Haifa, Israel
            294801,
            // Helsinki, Finland
            658225,
            // Hong Kong, China
            1819729,
            // Istanbul, Turkey
            745044,
            // Jakarta, Indonesia
            1642911,
            // Johannesburg, South Africa
            993800,
            // Kabul, Afghanistan
            1138958,
            // Kampala, Uganda
            232422,
            // Karachi, Pakistan
            1174872,
            // Kiev, Ukraine
            703448,
            // Kigali, Rwanda
            202061,
            // Kingston, Jamaica
            3489854,
            // La Paz, Bolivia
            3911925,
            // Lilongwe, Malawi
            927967,
            // Lima, Peru
            3936456,
            // Limbe, Cameroon
            2229411,
            // Lisbon, Portugal
            2267057,
            // London, United Kingdom
            2643743,
            // Macau, China
            1821274,
            // Madrid, Spain
            3117735,
            // Managua, Nicaragua
            3617763,
            // Manila, Philippines
            1701668,
            // Mexico City, Mexico
            3530597,
            // Montevideo, Uruguay
            3441575,
            // Moscow, Russia
            524901,
            // Mumbai, India
            1275339,
            // Nairobi, Kenya
            184745,
            // N'Djamena, Chad
            2427123,
            // New Delhi, India
            1273294,
            // Oslo, Norway
            3143244,
            // Panama City, Panama
            3703443,
            // Paris, France
            2988507,
            // Port-au-Prince, Haiti
            3718426,
            // Quito, Ecuador
            3652462,
            // Reykjavík, Iceland
            3413829,
            // Rio de Janeiro, Brazil
            3451190,
            // Rome, Italy
            3169070,
            // San José, Costa Rica
            3621841,
            // San Salvador, El Salvador
            3583361,
            // Santiago, Chile
            3871336,
            // Santo Domingo, Dominican Republic
            3492908,
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Shanghai, China
            1796236,
            // Stockholm, Sweden
            2673730,
            // Suva, Fiji
            2198148,
            // Sydney, Australia
            2147714,
            // Taipei, Taiwan, China
            1668341,
            // Tegucigalpa, Honduras
            3600949,
            // Tehran, Iran
            112931,
            // Tokyo, Japan
            1850147,
            // Toronto, Canada
            6167865,
            // Tunis, Tunisia
            2464470,
            // Vancouver, Canada
            6173331,
            // Vienna, Austria
            2761369,
            // Warsaw, Poland
            756135,
            // Wilmette (Illinois), United States
            4916732,
            // Yangon, Myanmar
            1298824,
            // Yaoundé, Cameroon
            2220957,
            // Yerevan, Armenia
            616052,
    };

    public BahaiCitiesFilter() {
        Arrays.sort(CITIES);
    }

    @Override
    public boolean accept(GeoName name) {
        return Arrays.binarySearch(CITIES, name.getGeoNameId()) >= 0;
    }

    @Override
    public void replaceLocation(GeoName name) {
    }
}

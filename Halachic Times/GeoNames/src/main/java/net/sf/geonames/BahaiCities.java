/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.geonames;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BahaiCities extends JewishCities {

    /**
     * List of Bahai cities' GeoName IDs.
     */
    private static final long[] BAHAI_CITIES = {
            // Kabul, Afghanistan
            1138958,
            // Andorra la Vella, Andorra
            3041563,
            // Buenos Aires, Argentina
            3435910,
            // Yerevan, Armenia
            616052,
            // Sydney, Australia
            2147714,
            // Vienna, Austria
            2761369,
            // Baku, Azerbaijan
            587084,
            // Brussels, Belgium
            2800866,
            // La Paz, Bolivia
            3911925,
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Gaborone, Botswana
            933773,
            // Rio de Janeiro, Brazil
            3451190,
            // Battambang, Cambodia
            1831797,
            // Limbe, Cameroon
            2229411,
            // Yaoundé, Cameroon
            2220957,
            // Toronto, Canada
            6167865,
            // Vancouver, Canada
            6173331,
            // N'Djamena, Chad
            2427123,
            // Santiago, Chile
            3871336,
            // Hong Kong, China
            1819729,
            // Macau, China
            1821274,
            // Shanghai, China
            1796236,
            // Taiwan, China
            1813416,
            // Bogota, Colombia
            3688689,
            // Brazzaville, Congo
            2260535,
            // San José, Costa Rica
            3621841,
            // Copenhagen, Denmark
            2618425,
            // Santo Domingo, Dominican Republic
            3492908,
            // Quito, Ecuador
            3652462,
            // Cairo, Egypt
            360630,
            // San Salvador, El Salvador
            3583361,
            // Suva, Fiji
            2198148,
            // Helsinki, Finland
            658225,
            // Paris, France
            2988507,
            // Berlin, Germany
            2950159,
            // Guatemala City, Guatemala
            3598132,
            // Port-au-Prince, Haiti
            3718426,
            // Tegucigalpa, Honduras
            3600949,
            // Budapest, Hungary
            3054643,
            // Reykjavík, Iceland
            3413829,
            // Mumbai, India
            1275339,
            // New Delhi, India
            1273294,
            // Jakarta, Indonesia
            1642911,
            // Tehran, Iran
            112931,
            // Baghdad, Iraq
            98182,
            // Belfast, Ireland
            2655984,
            // Acre, Israel
            295721,
            // Haifa, Israel
            294801,
            // Rome, Italy
            3169070,
            // Kingston, Jamaica
            3489854,
            // Tokyo, Japan
            1850147,
            // Nairobi, Kenya
            184745,
            // Benghazi, Libya
            88319,
            // Antananarivo, Madagascar
            1070940,
            // Lilongwe, Malawi
            927967,
            // Mexico City, Mexico
            3530597,
            // Casablanca, Morocco
            2553604,
            // Yangon, Myanmar
            1298824,
            // Amsterdam, Netherlands
            2759794,
            // Auckland, New Zealand
            2193733,
            // Managua, Nicaragua
            3617763,
            // Oslo, Norway
            3143244,
            // Karachi, Pakistan
            1174872,
            // Panama City, Panama
            3703443,
            // Asunción, Paraguay
            3439389,
            // Lima, Peru
            3936456,
            // Manila, Philippines
            1701668,
            // Warsaw, Poland
            756135,
            // Lisbon, Portugal
            2267057,
            // Moscow, Russia
            524901,
            // Kigali, Rwanda
            202061,
            // Apia, Samoa
            4035413,
            // Glasgow, Scotland
            2648579,
            // Dakar, Senegal
            2253354,
            // Bratislava, Slovakia
            3060972,
            // Johannesburg, South Africa
            993800,
            // Madrid, Spain
            3117735,
            // Stockholm, Sweden
            2673730,
            // Bern, Switzerland
            2661552,
            // Dodoma, Tanzania
            160196,
            // Tunis, Tunisia
            2464470,
            // Istanbul, Turkey
            745044,
            // Kampala, Uganda
            232422,
            // Kiev, Ukraine
            703448,
            // London, United Kingdom
            2643741,
            // Anchorage, United States
            5879400,
            // Eliot (Maine), United States
            4963642,
            // Wilmette (Illinois), United States
            4916732,
            // Montevideo, Uruguay
            3441575,
            // Caracas, Venezuela
            3646738,
    };

    public BahaiCities() {
        super();
        Arrays.sort(BAHAI_CITIES);
        setModuleName("compass-bahai");
    }

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        BahaiCities cities = new BahaiCities();
        Collection<GeoName> names;
        Collection<GeoName> filtered;
        try {
            names = cities.loadNames(res);
            filtered = cities.filterBahaiCities(names);
            cities.populateElevations(filtered);
            cities.toAndroidXML(filtered, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<GeoName> filterBahaiCities(Collection<GeoName> names) {
        Collection<GeoName> cities = new ArrayList<GeoName>();

        long nameId;
        for (GeoName name : names) {
            nameId = name.getGeoNameId();

            for (long id : BAHAI_CITIES) {
                if (id == nameId) {
                    cities.add(name);
                    continue;
                }
            }
        }

        return cities;
    }
}

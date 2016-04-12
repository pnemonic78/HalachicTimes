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
            // Andorra la Vella, Andorra
            // Buenos Aires, Argentina
            3435910,
            // Yerevan, Armenia
            // Sydney, Australia
            2147714,
            // Vienna, Austria
            2761369,
            // Baku, Azerbaijan
            // Brussels, Belgium
            2800866,
            // La Paz, Bolivia
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Gaborone, Botswana
            // Rio de Janeiro, Brazil
            3451190,
            // Battambang, Cambodia
            // Limbe, Cameroon
            // Yaoundé, Cameroon
            // Toronto, Canada
            6167865,
            // Vancouver, Canada
            6173331,
            // N’Djamena, Chad
            // Santiago, Chile
            // Hong Kong, China
            // Macao, China
            // Shanghai, China
            // Taiwan, China
            // Bogota, Colombia
            3688689,
            // Norte del Cauca, Colombia
            // Brazzaville, Congo
            // San José, Costa Rica
            // Copenhagen, Denmark
            // Santo Domingo, Dominican Republic
            // Quito, Ecuador
            // Cairo, Egypt
            // San Salvador, El Salvador
            // Suva, Fiji
            // Helsinki, Finland
            658225,
            // Paris, France
            2988507,
            // Berlin, Germany
            2950159,
            // Guatemala City, Guatemala
            // Port-au-Prince, Haiti
            // Tegucigalpa, Honduras
            // Budapest, Hungary
            3054643,
            // Reykjavík, Iceland
            // Mumbai, India
            1275339,
            // New Delhi, India
            // Jakarta, Indonesia
            // Tehran, Iran
            // Tehran, Iran
            // Baghdad, Iraq
            // Belfast, Ireland
            // Acre, Israel
            // Haifa, Israel
            294801,
            // Rome, Italy
            3169070,
            // Kingston, Jamaica
            // Tokyo, Japan
            // Matunda Soy, Kenya
            // Benghazi, Libya
            // Antananarivo, Madagascar
            // Lilongwe, Malawi
            // Mexico City, Mexico
            3530597,
            // Casablanca, Morocco
            2553604,
            // Yangon, Myanmar
            // Amsterdam, Netherlands
            2759794,
            // Auckland, New Zealand
            2193733,
            // Managua, Nicaragua
            // Oslo, Norway
            // Karachi, Pakistan
            // Panama City, Panama
            // Asunción, Paraguay
            // Lima, Peru
            // Manila, Philippines
            // Warsaw, Poland
            // Lisbon, Portugal
            // Moscow, Russia
            524901,
            // Kigali, Rwanda
            // Apia, Samoa
            // Glasgow, Scotland
            // Dakar, Senegal
            // Bratislava, Slovakia
            // Johannesburg, South Africa
            993800,
            // Madrid, Spain
            3117735,
            // Stockholm, Sweden
            2673730,
            // Bern, Switzerland
            // Dodoma, Tanzania
            // Tunis, Tunisia
            // Istanbul, Turkey
            745044,
            // Kampala, Uganda
            // Kiev, Ukraine
            703448,
            // London, United Kingdom
            2643741,
            // Anchorage, United States
            // Eliot (Maine), United States
            // Wilmette (Illinois), United States
            // Montevideo, Uruguay
            // Tanna, Vanuatu
            // Caracas, Venezuela
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

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

import java.util.Arrays;

/**
 * Name filter for Jewish cities.
 *
 * @author Moshe Waisberg
 */
public class JewishCitiesFilter implements NameFilter {

    /**
     * List of Jewish cities' GeoName IDs.
     */
    private static final long[] CITIES = {
            // Addis Ababa, Ethiopia
            344979,
            // Amsterdam, Netherlands
            2759794,
            // Anchorage, United States
            5879400,
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
            // Baltimore, United States
            4347778,
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
            // Bogota, Colombia
            3688689,
            // Boston, United States
            4930956,
            // Brussels, Belgium
            2800866,
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
            // Durban, South Africa
            1007311,
            // Eilat, Israel
            295277,
            // Geneve, Switzerland
            2660646,
            // Hadera, Israel
            294946,
            // Haifa, Israel
            294801,
            // Helsinki, Finland
            658225,
            // Houston, United States
            4699066,
            // Istanbul, Turkey
            745044,
            // Jerusalem, Israel
            281184,
            // Johannesburg, South Africa
            993800,
            // Kiev, Ukraine
            703448,
            // Las Vegas, United States
            5475433,
            // Lisbon, Portugal
            2267057,
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
            // Mexico City, Mexico
            3530597,
            // Miami, United States
            4164138,
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
            // New Delhi, India
            1273294,
            // New York, United States
            5128581,
            // Nice, France
            2990440,
            // Oslo, Norway
            3143244,
            // Paris, France
            2988507,
            // Perth, Australia
            2063523,
            // Philadelphia, United States
            4560349,
            // Phoenix, United States
            5308655,
            // Pittsburgh, United States
            5206379,
            // Portland, United States
            4975802,
            // Rio de Janeiro, Brazil
            3451190,
            // Rome, Italy
            3169070,
            // Safed / Zefat, Israel
            293100,
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Seattle, United States
            5809844,
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
            // Stockholm, Sweden
            2673730,
            // Sydney, Australia
            2147714,
            // Tel Aviv, Israel
            293397,
            // Tiberias, Israel
            293322,
            // Tokyo, Japan
            1850147,
            // Toronto, Canada
            6167865,
            // Vancouver, Canada
            6173331,
            // Vienna, Austria
            2761369,
            // Warsaw, Poland
            756135,
            // Washington D.C., United States
            4140963,
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
}

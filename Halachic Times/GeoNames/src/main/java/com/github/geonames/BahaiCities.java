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

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * Bahai cities for Android application resources.
 *
 * @author Moshe Waisberg
 */
public class BahaiCities extends JewishCities {

    protected static final String[] LANGUAGES = {null, "ar", "bg", "cs", "da", "de", "el", "es", "et", "fa", "fi", "fr", "he", "hi", "hu", "it", "iw", "lt", "nb", "no", "nl", "pl", "pt", "ro", "ru", "sv", "tr", "uk"};

    public BahaiCities() {
        super();
        setModuleName("compass-bahai");
    }

    public static void main(String[] args) throws Exception {
        String pathCities = "GeoNames/dump/cities1000.zip";
        if (args.length > 0) {
            pathCities = args[0];
        }
        String pathNames = "GeoNames/dump/alternateNamesV2.zip";
        if (args.length > 1) {
            pathNames = args[1];
        }
        BahaiCities cities = new BahaiCities();
        Collection<GeoNamesToponym> names = cities.loadNames(new File(pathCities), new BahaiCitiesFilter(), "cities1000.txt");

        cities.populateElevations(names);
        cities.populateAlternateNames(new File(pathNames), names, "alternateNamesV2.txt");

        InputStream googleNames = cities.getClass().getResourceAsStream("/googleNames.txt");
        cities.populateAlternateNames(googleNames, names);

        for (String lang : LANGUAGES) {
            cities.writeAndroidXML(names, lang);
        }
    }
}

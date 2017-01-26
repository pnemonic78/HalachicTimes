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
        String pathCities = "GeoNames/res/cities1000.txt";
        String pathNames = "GeoNames/res/alternateNames.txt";
        String pathNames2 = "GeoNames/res/googleNames.txt";
        BahaiCities cities = new BahaiCities();
        Collection<GeoName> names;

        names = cities.loadNames(new File(pathCities), new BahaiCitiesFilter());
        cities.populateElevations(names);
        cities.populateAlternateNames(new File(pathNames), names);
        cities.populateAlternateNames(new File(pathNames2), names);
        for (String lang : LANGUAGES) {
            cities.writeAndroidXML(names, lang);
        }
    }
}

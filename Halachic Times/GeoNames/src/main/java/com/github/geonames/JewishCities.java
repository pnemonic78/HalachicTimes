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

import org.geonames.InsufficientStyleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Jewish cities for Android application resources.
 *
 * @author Moshe Waisberg
 */
public class JewishCities extends Cities {

    protected static final String[] LANGUAGES = {null, "bg", "cs", "da", "de", "es", "et", "fi", "fr", "he", "hu", "it", "iw", "lt", "nb", "no", "nl", "pl", "pt", "ro", "ru", "sv", "uk"};

    public static void main(String[] args) throws Exception {
        String pathCities = "GeoNames/dump/cities1000.zip";
        if (args.length > 0) {
            pathCities = args[0];
        }
        String pathNames = "GeoNames/dump/alternateNamesV2.zip";
        if (args.length > 1) {
            pathNames = args[1];
        }
        JewishCities cities = new JewishCities();
        Collection<GeoNamesToponym> names = cities.loadNames(new File(pathCities), new JewishCitiesFilter(), "cities1000.txt");

        cities.populateElevations(names);
        cities.populateAlternateNames(new File(pathNames), names, "alternateNamesV2.txt");

        InputStream googleNames = cities.getClass().getResourceAsStream("/googleNames.txt");
        cities.populateAlternateNames(googleNames, names);

        for (String lang : LANGUAGES) {
            cities.writeAndroidXML(names, lang);
        }
    }

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param names
     *         the list of names.
     * @param language
     *         the language code.
     * @throws ParserConfigurationException
     *         if a DOM error occurs.
     * @throws TransformerException
     *         if a DOM error occurs.
     */
    @Override
    public void writeAndroidXML(Collection<GeoNamesToponym> names, String language) throws ParserConfigurationException, TransformerException, InsufficientStyleException {
        List<GeoNamesToponym> sorted;
        if (names instanceof List)
            sorted = (List<GeoNamesToponym>) names;
        else
            sorted = new ArrayList<>(names);
        Collections.sort(sorted, new LocationComparator());

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element resources = doc.createElement(ANDROID_ELEMENT_RESOURCES);
        resources.appendChild(doc.createComment(HEADER));
        doc.appendChild(resources);

        Element citiesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        citiesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities");
        resources.appendChild(citiesElement);

        Element countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);

        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);

        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(longitudesElement);

        Element elevationsElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        elevationsElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_elevations");
        elevationsElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(elevationsElement);

        Element zonesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_time_zones");
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(zonesElement);

        Element city, country, latitude, longitude, elevation, zone;
        String name;
        String language2 = getLanguageCode(language);

        for (GeoNamesToponym place : sorted) {
            name = place.getName(language2);
            if (name == null) {
                name = place.getName();
                System.err.println("Unknown translation! id: " + place.getGeoNameId() + " language: " + language2 + " name: [" + place.getName() + "]");
            }

            city = doc.createElement(ANDROID_ELEMENT_ITEM);
            city.setTextContent(escape(name));
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(place.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(Integer.toString((int) (place.getLatitude() * CountryRegion.FACTOR_TO_INT)));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(Integer.toString((int) (place.getLongitude() * CountryRegion.FACTOR_TO_INT)));
            elevation = doc.createElement(ANDROID_ELEMENT_ITEM);
            elevation.setTextContent(Integer.toString(place.getGrossElevation()));
            zone = doc.createElement(ANDROID_ELEMENT_ITEM);
            zone.setTextContent(place.getTimezone().getTimezoneId());

            citiesElement.appendChild(city);
            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
            elevationsElement.appendChild(elevation);
            zonesElement.appendChild(zone);
        }

        File file;
        if (language == null)
            file = new File(getModulePath(), "values/cities.xml");
        else
            file = new File(getModulePath(), "values-" + language + "/cities.xml");
        file.getParentFile().mkdirs();

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(S_KEY_INDENT_AMOUNT, "4");
        transformer.transform(src, result);
    }

    protected String escape(String text) {
        text = text.replaceAll("(['\"])", "\\\\$1");
        return text;
    }

    protected String getLanguageCode(String language) {
        if (language == null) {
            return null;
        }
        String language2 = new Locale(language).getLanguage();
        if (LocationComparator.ISO_639_NB.equals(language2)) {
            language2 = LocationComparator.ISO_639_NO;
        }
        return language2;
    }
}

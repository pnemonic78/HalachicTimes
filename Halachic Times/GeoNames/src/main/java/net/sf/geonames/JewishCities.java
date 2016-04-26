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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    protected static final String[] LANGUAGES = {null/*, "ar", "bg", "cs", "da", "de", "el", "es", "et", "fa", "fi", "fr", "hi", "hu", "it", "iw", "lt", "nb", "nl", "pl", "pt", "ro", "ru", "sv", "tr", "uk"*/};

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        JewishCities cities = new JewishCities();
        Collection<GeoName> names;

        try {
            names = cities.loadNames(res, new JewishCitiesFilter());
            cities.populateAlternateNames(names);
            cities.populateElevations(names);
            for (String lang : LANGUAGES) {
                cities.toAndroidXML(names, lang);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public void toAndroidXML(Collection<GeoName> names, String language) throws ParserConfigurationException, TransformerException {
        List<GeoName> sorted;
        if (names instanceof List)
            sorted = (List<GeoName>) names;
        else
            sorted = new ArrayList<GeoName>(names);
        Collections.sort(sorted, new LocationComparator());

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        File file;
        if (language == null)
            file = new File(getModulePath(), "values/cities.xml");
        else
            file = new File(getModulePath(), "values-" + language + "/cities.xml");
        file.getParentFile().mkdirs();

        Element resources = doc.createElement(ANDROID_ELEMENT_RESOURCES);
        doc.appendChild(doc.createComment("Generated from geonames.org data."));
        doc.appendChild(resources);

        Element citiesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        citiesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities");
        resources.appendChild(citiesElement);

        Element countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);

        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);

        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(longitudesElement);

        Element elevationsElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
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

        for (GeoName place : sorted) {
            city = doc.createElement(ANDROID_ELEMENT_ITEM);
            city.setTextContent(escape(place.getName()));
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(place.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(String.valueOf(place.getLatitude()));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(String.valueOf(place.getLongitude()));
            elevation = doc.createElement(ANDROID_ELEMENT_ITEM);
            elevation.setTextContent(String.valueOf(place.getGrossElevation()));
            zone = doc.createElement(ANDROID_ELEMENT_ITEM);
            zone.setTextContent(place.getTimeZone());

            citiesElement.appendChild(city);
            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
            elevationsElement.appendChild(elevation);
            zonesElement.appendChild(zone);
        }

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        transformer.transform(src, result);
    }

    protected String escape(String text) {
        text = text.replaceAll("(['\"])", "\\\\$1");
        return text;
    }
}

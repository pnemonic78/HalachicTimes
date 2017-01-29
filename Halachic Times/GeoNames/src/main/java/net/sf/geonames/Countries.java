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

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * Countries.
 *
 * @author Moshe Waisberg
 */
public class Countries extends Cities {

    /** The number of main vertices per region border. */
    private static final int VERTICES_COUNT = 16;

    /**
     * Constructs a new countries.
     */
    public Countries() {
        super();
    }

    public static void main(String[] args) throws Exception {
        File countryInfoFile = new File("GeoNames/res/countryInfo.txt");
        File shapesFile = new File("GeoNames/res/shapes_simplified_low.txt");

        Countries countries = new Countries();
        Collection<CountryInfo> names = countries.loadInfo(countryInfoFile);
        Collection<GeoShape> shapes = countries.loadShapes(shapesFile);
        Collection<CountryRegion> regions = countries.toRegions(names, shapes);

        countries.writeAndroidXML(regions);
    }

    /**
     * Transform the list of names to a list of country regions.
     *
     * @param names
     *         the list of places.
     * @return the list of regions.
     */
    public Collection<CountryRegion> toRegions(Collection<GeoName> names) {
        Map<String, CountryRegion> regions = new TreeMap<String, CountryRegion>();
        String countryCode;
        CountryRegion region;

        for (GeoName name : names) {
            countryCode = name.getCountryCode();

            if (!regions.containsKey(countryCode)) {
                region = new CountryRegion(countryCode);
                regions.put(countryCode, region);
            } else
                region = regions.get(countryCode);
            region.addLocation(name.getLatitude(), name.getLongitude());
        }

        return regions.values();
    }

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param countries
     *         the list of countries.
     * @throws ParserConfigurationException
     *         if a DOM error occurs.
     * @throws TransformerException
     *         if a DOM error occurs.
     */
    public void writeAndroidXML(Collection<CountryRegion> countries) throws ParserConfigurationException, TransformerException {
        List<CountryRegion> sorted = null;
        if (countries instanceof List)
            sorted = (List<CountryRegion>) countries;
        else
            sorted = new ArrayList<CountryRegion>(countries);
        Collections.sort(sorted, new RegionComparator());

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element resources = doc.createElement(ANDROID_ELEMENT_RESOURCES);
        resources.appendChild(doc.createComment(HEADER));
        doc.appendChild(resources);

        Element countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(countriesElement);
        Element verticesCountElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "vertices_count");
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(verticesCountElement);
        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(latitudesElement);
        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(longitudesElement);

        Element country, latitude, longitude, verticesCount;
        int[] pointIndexes;
        int pointIndex;
        int pointCount = 0;

        for (CountryRegion region : sorted) {
            pointIndexes = region.findMainVertices(VERTICES_COUNT);

            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(region.getCountryCode());
            countriesElement.appendChild(country);

            pointCount = 0;
            for (int i = 0; i < VERTICES_COUNT; i++) {
                pointIndex = pointIndexes[i];

                if (pointIndex < 0)
                    break;

                latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
                latitude.setTextContent(Integer.toString(region.ypoints[pointIndex]));
                latitudesElement.appendChild(latitude);
                longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
                longitude.setTextContent(Integer.toString(region.xpoints[pointIndex]));
                longitudesElement.appendChild(longitude);
                pointCount++;
            }

            verticesCount = doc.createElement(ANDROID_ELEMENT_ITEM);
            verticesCount.setTextContent(Integer.toString(pointCount));
            verticesCountElement.appendChild(verticesCount);

        }

        File file = new File(getModulePath(), "values/countries.xml");
        file.getParentFile().mkdirs();

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        TransformerFactory xformerFactory = TransformerFactory.newInstance();
        Transformer transformer = xformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
        transformer.transform(src, result);
    }

    /**
     * Load the list of countries.
     *
     * @param file
     *         the country info file.
     * @return the list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<CountryInfo> loadInfo(File file) throws IOException {
        return geoNames.parseCountries(file);
    }

    /**
     * Load the list of shapes.
     *
     * @param file
     *         the shapes file.
     * @return the list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoShape> loadShapes(File file) throws IOException {
        return geoNames.parseShapes(file);
    }

    /**
     * Transform the list of countries to a list of country regions.
     *
     * @param names
     *         the list of countries.
     * @param shapes
     *         the list of country shapes.
     * @return the list of regions.
     */
    public Collection<CountryRegion> toRegions(Collection<CountryInfo> names, Collection<GeoShape> shapes) throws IOException {
        List<CountryRegion> regions = new ArrayList<>(names.size());
        Long geonameId;
        CountryRegion region;
        Map<Long, GeoShape> shapesById = new HashMap<>();
        GeoShape shape;

        for (GeoShape s : shapes) {
            shapesById.put(s.getGeoNameId(), s);
        }

        for (CountryInfo name : names) {
            geonameId = name.getGeoNameId();
            shape = shapesById.get(geonameId);
            if (shape != null) {
                region = CountryRegion.toRegion(name.getIso(), shape);
                if (region != null) {
                    regions.add(region);
                }
            }
        }

        return regions;
    }
}

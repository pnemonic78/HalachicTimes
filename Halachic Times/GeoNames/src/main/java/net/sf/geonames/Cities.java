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

import net.sf.net.HTTPReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
 * Cities.
 *
 * @author Moshe
 */
public class Cities implements NameFilter{

    public static final String ANDROID_ATTRIBUTE_NAME = "name";
    public static final String ANDROID_ATTRIBUTE_TRANSLATABLE = "translatable";
    public static final String ANDROID_ELEMENT_RESOURCES = "resources";
    public static final String ANDROID_ELEMENT_STRING_ARRAY = "string-array";
    public static final String ANDROID_ELEMENT_INTEGER_ARRAY = "integer-array";
    public static final String ANDROID_ELEMENT_ITEM = "item";

    /** GeoNames user name. */
    private static final String USERNAME = "";

    private static final String TAG_ELEVATION_STATUS = "status";
    private static final String TAG_ELEVATION_RESULT = "result";
    private static final String TAG_ELEVATION_ELEVATION = "elevation";

    /**
     * URL that returns the attribute of the geoNames feature with the given geonameId as JSON document.
     */
    private static final String URL_GEONAME_GET = "http://api.geonames.org/getJSON?geonameId=%d&username=%s";

    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION_GOOGLE = "http://maps.googleapis.com/maps/api/elevation/xml?locations=%f,%f";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Aster Global Digital Elevation Model data.
     */
    private static final String URL_ELEVATION_AGDEM = "http://api.geonames.org/astergdem?lat=%f&lng=%f&username=%s";

    private static final String APP_RES = "/src/main/res";

    private String moduleName;

    /**
     * Constructs a new cities.
     */
    public Cities() {
        setModuleName("locations");
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    protected File getModulePath() {
        return new File(getModuleName(), APP_RES);
    }

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        System.out.println(res);
        System.out.println(res.getAbsolutePath());
        Cities cities = new Cities();
        Collection<GeoName> names;
        Collection<GeoName> cityNames;
        Collection<GeoName> capitals;
        try {
            names = cities.loadNames(res, null);
            cityNames = cities.filterCity(names);
            capitals = cities.filterCapitals(cityNames);
            cities.toAndroidXML(capitals, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the list of names.
     *
     * @param file
     *         the geonames file.
     * @param filter
     *         the name filter.
     * @return the sorted list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> loadNames(File file, NameFilter filter) throws IOException {
        GeoNames parser = new GeoNames();
        return parser.parseCSV(file, filter);
    }

    /**
     * Filter the list of names to find only cities.
     *
     * @param names
     *         the list of all names.
     * @return the list of cities.
     */
    public Collection<GeoName> filterCity(Collection<GeoName> names) {
        Collection<GeoName> cities = new ArrayList<GeoName>();

        for (GeoName name : names) {
            if (GeoName.FEATURE_P.equals(name.getFeatureClass())) {
                cities.add(name);
            }
        }

        return cities;
    }

    /**
     * Filter the list of names to find only capital cities.
     *
     * @param names
     *         the list of all names.
     * @return the list of capitals.
     */
    public Collection<GeoName> filterCapitals(Collection<GeoName> names) {
        Collection<GeoName> capitals = new ArrayList<GeoName>();
        Collection<String> countries = getCountries();

        for (GeoName name : names) {
            if (GeoName.FEATURE_PPLC.equals(name.getFeatureCode())) {
                capitals.add(name);
                countries.remove(name.getCountryCode());
            }
        }

        // For all countries without capitals, find the next best matching city
        // type.
        if (!countries.isEmpty()) {
            Map<String, GeoName> best = new TreeMap<String, GeoName>();
            GeoName place;
            String cc;
            for (GeoName name : names) {
                cc = name.getCountryCode();

                if (countries.contains(cc)) {
                    place = best.get(cc);
                    if (place == null) {
                        best.put(cc, name);
                        continue;
                    }
                    place = betterPlace(name, place);
                    best.put(cc, place);
                }
            }
            capitals.addAll(best.values());
        }

        return capitals;
    }

    /**
     * Get the better place by comparing its feature type as being more
     * populated.
     *
     * @param name1
     *         a name.
     * @param name2
     *         a name.
     * @return the better name.
     */
    private GeoName betterPlace(GeoName name1, GeoName name2) {
        String feature1 = name1.getFeatureCode();
        String feature2 = name2.getFeatureCode();
        int rank1 = getFeatureCodeRank(feature1);
        int rank2 = getFeatureCodeRank(feature2);

        // Compare features.
        if (rank1 < 0)
            return name2;
        if (rank2 < 0)
            return (rank1 < rank2) ? name2 : name1;
        // if (rank2 > rank1)
        // return name2;

        // Compare populations.
        // if (rank1 == rank2) {
        long pop1 = name1.getPopulation();
        long pop2 = name2.getPopulation();
        if (pop2 > pop1)
            return name2;
        // }

        return name1;
    }

    private Map<String, Integer> ranks;

    /**
     * Get the rank of the feature code.
     *
     * @param code
     *         the feature code.
     * @return the rank.
     */
    private int getFeatureCodeRank(String code) {
        if (ranks == null) {
            ranks = new TreeMap<String, Integer>();
            int rank = -2;
            ranks.put(GeoName.FEATURE_PPLW, rank++);
            ranks.put(GeoName.FEATURE_PPLQ, rank++);
            ranks.put(GeoName.FEATURE_P, rank++);
            ranks.put(GeoName.FEATURE_PPLX, rank++);
            ranks.put(GeoName.FEATURE_PPL, rank++);
            ranks.put(GeoName.FEATURE_PPLS, rank++);
            ranks.put(GeoName.FEATURE_PPLL, rank++);
            ranks.put(GeoName.FEATURE_PPLF, rank++);
            ranks.put(GeoName.FEATURE_PPLR, rank++);
            ranks.put(GeoName.FEATURE_STLMT, rank++);
            ranks.put(GeoName.FEATURE_PPLA4, rank++);
            ranks.put(GeoName.FEATURE_PPLA3, rank++);
            ranks.put(GeoName.FEATURE_PPLA2, rank++);
            ranks.put(GeoName.FEATURE_PPLA, rank++);
            ranks.put(GeoName.FEATURE_PPLG, rank++);
            ranks.put(GeoName.FEATURE_PPLC, rank++);
        }
        return ranks.get(code);
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
    public void toAndroidXML(Collection<GeoName> names, String language) throws ParserConfigurationException, TransformerException {
        List<GeoName> sorted = null;
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
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);

        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);

        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(longitudesElement);

        Element zonesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "time_zones");
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(zonesElement);

        Element country, latitude, longitude, zone;

        for (GeoName place : sorted) {
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(place.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(String.valueOf(place.getLatitude()));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(String.valueOf(place.getLongitude()));
            zone = doc.createElement(ANDROID_ELEMENT_ITEM);
            zone.setTextContent(place.getTimeZone());

            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
            zonesElement.appendChild(zone);
        }

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        transformer.transform(src, result);
    }

    /**
     * Get the list of country codes.
     *
     * @return the list of countries.
     */
    protected Collection<String> getCountries() {
        Collection<String> countries = new TreeSet<String>();
        for (String country : Locale.getISOCountries())
            countries.add(country);
        return countries;
    }

    /**
     * Populate the list of names with elevations.
     *
     * @param geoNames
     *         the list of names to populate.
     */
    public void populateElevations(Collection<GeoName> geoNames) {
        double elevation;
        for (GeoName name : geoNames) {
            elevation = name.getElevation();
            if ((elevation == 0) || Double.isNaN(elevation)) {
                try {
                    populateElevation(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void populateElevation(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
        geoName.setElevation(0);
        try {
            populateElevationGeoNames(geoName);
        } catch (Exception e) {
            populateElevationGoogle(geoName);
        }
    }

    protected void populateElevationGeoNames(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
        double latitude = geoName.getLatitude();
        double longitude = geoName.getLongitude();
        String queryUrl = String.format(Locale.US, URL_ELEVATION_AGDEM, latitude, longitude, USERNAME);
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        String elevationValue = new String(data).trim();
        double elevation = Double.parseDouble(elevationValue);
        geoName.setElevation(elevation);
    }

    protected void populateElevationGoogle(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
        double latitude = geoName.getLatitude();
        double longitude = geoName.getLongitude();
        String queryUrl = String.format(Locale.US, URL_ELEVATION_GOOGLE, latitude, longitude);
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
        InputStream source = new ByteArrayInputStream(data);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(source);
        Element root = doc.getDocumentElement();
        Element statusNode = (Element) root.getElementsByTagName(TAG_ELEVATION_STATUS).item(0);
        String status = statusNode.getTextContent();
        if (!"OK".equals(status)) {
            System.err.println("status: " + status);
            return;
        }
        Element resultNode = (Element) root.getElementsByTagName(TAG_ELEVATION_RESULT).item(0);
        Element elevationNode = (Element) resultNode.getElementsByTagName(TAG_ELEVATION_ELEVATION).item(0);
        String elevationValue = elevationNode.getTextContent().trim();
        double elevation = Double.parseDouble(elevationValue);
        geoName.setElevation(elevation);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param geoNames
     *         the list of names to populate.
     */
    public void populateAlternateNames(Collection<GeoName> geoNames) {
        Map<String, AlternateName> alternateNames;
        for (GeoName geoName : geoNames) {
            alternateNames = geoName.getAlternateNamesMap();
            if (alternateNames.size() <= 1) {
                try {
                    populateAlternateNames(geoName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void populateAlternateNames(GeoName geoName) throws IOException {
        String queryUrl = String.format(Locale.US, URL_GEONAME_GET, geoName.getGeoNameId(), USERNAME);
        queryUrl = "file:///C:\\Users\\moshe.w\\workspace\\Halachic Times\\GeoNames\\res\\524901.json";
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        JsonReader reader = Json.createReader(new ByteArrayInputStream(data));
        JsonObject json = reader.readObject();
        JsonArray arr = json.getJsonArray("alternateNames");

        Map<String, AlternateName> alternateNames = geoName.getAlternateNamesMap();
        AlternateName alternateName;
        JsonObject jsonAlternateName;
        String lang;
        String name;
        int length = arr.size();
        for (int i = 0; i < length; i++) {
            jsonAlternateName = arr.getJsonObject(i);
            if (!jsonAlternateName.containsKey("lang")) {
                continue;
            }
            lang = jsonAlternateName.getString("lang");
            name = jsonAlternateName.getString("name");
            alternateName = new AlternateName(lang, name);
            alternateNames.put(lang, alternateName);
        }
    }

    @Override
    public boolean accept(GeoName name) {
        return false;
    }
}

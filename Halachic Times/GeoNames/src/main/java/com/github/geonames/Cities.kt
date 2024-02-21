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

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

import org.geonames.InsufficientStyleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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

import static com.github.geonames.Features.FEATURE_P;
import static com.github.geonames.Features.FEATURE_PPL;
import static com.github.geonames.Features.FEATURE_PPLA;
import static com.github.geonames.Features.FEATURE_PPLA2;
import static com.github.geonames.Features.FEATURE_PPLA3;
import static com.github.geonames.Features.FEATURE_PPLA4;
import static com.github.geonames.Features.FEATURE_PPLC;
import static com.github.geonames.Features.FEATURE_PPLF;
import static com.github.geonames.Features.FEATURE_PPLG;
import static com.github.geonames.Features.FEATURE_PPLL;
import static com.github.geonames.Features.FEATURE_PPLQ;
import static com.github.geonames.Features.FEATURE_PPLR;
import static com.github.geonames.Features.FEATURE_PPLS;
import static com.github.geonames.Features.FEATURE_PPLW;
import static com.github.geonames.Features.FEATURE_PPLX;
import static com.github.geonames.Features.FEATURE_STLMT;

/**
 * Cities.
 *
 * @author Moshe Waisberg
 */
public class Cities {

    public static final String ANDROID_ATTRIBUTE_NAME = "name";
    public static final String ANDROID_ATTRIBUTE_TRANSLATABLE = "translatable";
    public static final String ANDROID_ELEMENT_RESOURCES = "resources";
    public static final String ANDROID_ELEMENT_STRING_ARRAY = "string-array";
    public static final String ANDROID_ELEMENT_INTEGER_ARRAY = "integer-array";
    public static final String ANDROID_ELEMENT_ITEM = "item";

    protected static final String HEADER = "Generated from geonames.org data";

    private static final String APP_RES = "/src/main/res";

    protected static final String S_KEY_INDENT_AMOUNT = OutputPropertiesFactory.S_KEY_INDENT_AMOUNT;

    protected final GeoNames geoNames = new GeoNames();
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

    public static void main(String[] args) throws Exception {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        System.out.println(res);
        System.out.println(res.getAbsolutePath());
        Cities cities = new Cities();

        Collection<GeoNamesToponym> names = cities.loadNames(res, new CityFilter());
        Collection<GeoNamesToponym> capitals = cities.filterCapitals(names);
        cities.writeAndroidXML(capitals, null);
    }

    /**
     * Load the list of names.
     *
     * @param file
     *         the geonames CSV file.
     * @param filter
     *         the name filter.
     * @return the sorted list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoNamesToponym> loadNames(File file, NameFilter filter) throws IOException {
        return loadNames(file, filter, null);
    }

    /**
     * Load the list of names.
     *
     * @param file
     *         the geonames CSV file.
     * @param filter
     *         the name filter.
     * @param zippedName
     *         the zipped name.
     * @return the sorted list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoNamesToponym> loadNames(File file, NameFilter filter, String zippedName) throws IOException {
        return geoNames.parseTabbed(file, filter, zippedName);
    }

    /**
     * Filter the list of names to find only capital (or the next-best-to-capital) cities.
     *
     * @param names
     *         the list of cites.
     * @return the list of capitals.
     */
    public Collection<GeoNamesToponym> filterCapitals(Collection<GeoNamesToponym> names) throws InsufficientStyleException {
        Collection<GeoNamesToponym> capitals = new ArrayList<GeoNamesToponym>();
        Collection<String> countries = getCountries();

        for (GeoNamesToponym name : names) {
            if (FEATURE_PPLC.equals(name.getFeatureCode())) {
                capitals.add(name);
                countries.remove(name.getCountryCode());
            }
        }

        // For all countries without capitals, find the next best matching city type.
        if (!countries.isEmpty()) {
            Map<String, GeoNamesToponym> best = new TreeMap<String, GeoNamesToponym>();
            GeoNamesToponym place;
            String cc;
            for (GeoNamesToponym name : names) {
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
    private GeoNamesToponym betterPlace(GeoNamesToponym name1, GeoNamesToponym name2) throws InsufficientStyleException {
        String feature1 = name1.getFeatureCode();
        String feature2 = name2.getFeatureCode();
        int rank1 = getFeatureCodeRank(feature1);
        int rank2 = getFeatureCodeRank(feature2);

        // Compare features.
        if (rank1 < 0)
            return name2;
        if (rank2 < 0)
            return (rank1 < rank2) ? name2 : name1;

        // Compare populations.
        long pop1 = name1.getPopulation();
        long pop2 = name2.getPopulation();
        if (pop2 > pop1)
            return name2;

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
        Map<String, Integer> ranks = this.ranks;
        if (ranks == null) {
            ranks = new TreeMap<>();
            int rank = -2;
            ranks.put(FEATURE_PPLW, rank++);
            ranks.put(FEATURE_PPLQ, rank++);
            ranks.put(FEATURE_P, rank++);
            ranks.put(FEATURE_PPLX, rank++);
            ranks.put(FEATURE_PPL, rank++);
            ranks.put(FEATURE_PPLS, rank++);
            ranks.put(FEATURE_PPLL, rank++);
            ranks.put(FEATURE_PPLF, rank++);
            ranks.put(FEATURE_PPLR, rank++);
            ranks.put(FEATURE_STLMT, rank++);
            ranks.put(FEATURE_PPLA4, rank++);
            ranks.put(FEATURE_PPLA3, rank++);
            ranks.put(FEATURE_PPLA2, rank++);
            ranks.put(FEATURE_PPLA, rank++);
            ranks.put(FEATURE_PPLG, rank++);
            ranks.put(FEATURE_PPLC, rank++);
            this.ranks = ranks;
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
    public void writeAndroidXML(Collection<GeoNamesToponym> names, String language) throws ParserConfigurationException, TransformerException, InsufficientStyleException {
        List<GeoNamesToponym> sorted = null;
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
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);

        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);

        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
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

        for (GeoNamesToponym place : sorted) {
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(place.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(Integer.toString((int) (place.getLatitude() * CountryRegion.FACTOR_TO_INT)));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(Integer.toString((int) (place.getLongitude() * CountryRegion.FACTOR_TO_INT)));
            zone = doc.createElement(ANDROID_ELEMENT_ITEM);
            zone.setTextContent(place.getTimezone().getTimezoneId());

            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
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

    public void populateElevations(Collection<GeoNamesToponym> records) {
        geoNames.populateElevations(records);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param file
     *         the alternate names file.
     * @param records
     *         the list of records to populate.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(File file, Collection<GeoNamesToponym> records) throws IOException {
        populateAlternateNames(file, records, null);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param input
     *         the alternate names input.
     * @param records
     *         the list of records to populate.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(InputStream input, Collection<GeoNamesToponym> records) throws IOException {
        populateAlternateNames(input, records, null);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param file
     *         the alternate names file.
     * @param records
     *         the list of records to populate.
     * @param zippedName
     *         the zipped file name.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(File file, Collection<GeoNamesToponym> records, String zippedName) throws IOException {
        geoNames.populateAlternateNames(file, records, zippedName);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param input
     *         the alternate names input.
     * @param records
     *         the list of records to populate.
     * @param zippedName
     *         the zipped file name.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(InputStream input, Collection<GeoNamesToponym> records, String zippedName) throws IOException {
        geoNames.populateAlternateNames(input, records, zippedName);
    }
}

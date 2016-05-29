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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Manage lists of GeoName records.
 *
 * @author Moshe Waisberg
 */
public class GeoNames {

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

    public GeoNames() {
        super();
    }

    /**
     * Parse the file with GeoName records.
     *
     * @param file
     *         the file to parse.
     * @return the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> parseCSV(File file) throws IOException {
        return parseCSV(file, null);
    }

    /**
     * Parse the file with GeoName records.
     *
     * @param file
     *         the file to parse.
     * @param filter
     *         the filter.
     * @return the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> parseCSV(File file, NameFilter filter) throws IOException {
        Collection<GeoName> records = null;
        Reader reader = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            in = null;
            records = parseCSV(reader, filter);
        } finally {
            if (in != null)
                in.close();
            if (reader != null)
                reader.close();
        }
        return records;
    }

    /**
     * Parse the file with GeoName records.
     *
     * @param reader
     *         the reader.
     * @return the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> parseCSV(Reader reader) throws IOException {
        return parseCSV(reader, null);
    }

    /**
     * Parse the file with GeoName records.
     * <p>
     * <code>
     * The main 'geoname' table has the following fields :<br>
     * ---------------------------------------------------<br>
     * geonameid         : integer id of record in geonames database<br>
     * name              : name of geographical point (utf8) varchar(200)<br>
     * asciiname         : name of geographical point in plain ascii characters, varchar(200)<br>
     * alternatenames    : alternatenames, comma separated varchar(5000)<br>
     * latitude          : latitude in decimal degrees (wgs84)<br>
     * longitude         : longitude in decimal degrees (wgs84)<br>
     * feature class     : see http://www.geonames.org/export/codes.html, char(1)<br>
     * feature code      : see http://www.geonames.org/export/codes.html, varchar(10)<br>
     * country code      : ISO-3166 2-letter country code, 2 characters<br>
     * cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters<br>
     * admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)<br>
     * admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)<br>
     * admin3 code       : code for third level administrative division, varchar(20)<br>
     * admin4 code       : code for fourth level administrative division, varchar(20)<br>
     * population        : bigint (8 byte int)<br>
     * elevation         : in meters, integer<br>
     * dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.<br>
     * timezone          : the timezone id (see file timeZone.txt) varchar(40)<br>
     * modification date : date of last modification in yyyy-MM-dd format<br>
     * </code>
     * </p>
     *
     * @param reader
     *         the reader.
     * @param filter
     *         the filter.
     * @return the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> parseCSV(Reader reader, NameFilter filter) throws IOException {
        Collection<GeoName> records = new ArrayList<GeoName>();
        GeoName record;
        String line;
        BufferedReader buf = new BufferedReader(reader);
        String[] fields;
        int column;
        String field;

        while (true) {
            line = buf.readLine();
            if (line == null)
                break;
            fields = line.split("\t");
            record = new GeoName();

            column = 0;
            field = fields[column++];
            record.setGeoNameId(Long.parseLong(field));
            field = fields[column++];
            record.setName(field);
            field = fields[column++];
            record.setAsciiName(field);
            field = fields[column++];
            record.setAlternateNames(field);
            field = fields[column++];
            record.setLatitude(Double.parseDouble(field));
            field = fields[column++];
            record.setLongitude(Double.parseDouble(field));
            field = fields[column++];
            record.setFeatureClass(field);
            field = fields[column++];
            record.setFeatureCode(field);
            field = fields[column++];
            record.setCountryCode(field);
            field = fields[column++];
            record.setAlternateCountryCodes(field);
            field = fields[column++];
            record.setAdminCode1(field);
            field = fields[column++];
            record.setAdminCode2(field);
            field = fields[column++];
            record.setAdminCode3(field);
            field = fields[column++];
            record.setAdminCode4(field);
            field = fields[column++];
            record.setPopulation(Long.parseLong(field));
            field = fields[column++];
            if (field.length() > 0)
                record.setElevation(Integer.parseInt(field));
            field = fields[column++];
            if (field.length() > 0)
                record.setDigitalElevation(Integer.parseInt(field));
            field = fields[column++];
            if (field.length() == 0) {
                // throw new NullPointerException("time zone required for " +
                // record.getGeoNameId());
                System.err.println("time zone required for " + record.getGeoNameId());
                System.err.println(line);
                continue;
            }
            record.setTimeZone(field);
            field = fields[column++];
            record.setModification(field);

            if ((filter == null) || filter.accept(record)) {
                records.add(record);
            }
        }

        return records;
    }

    /**
     * Populate the list of names with elevations.
     *
     * @param geoNames
     *         the list of names to populate.
     */
    public void populateElevations(Collection<GeoName> geoNames) {
        int elevation;
        for (GeoName name : geoNames) {
            elevation = name.getGrossElevation();
            if (elevation == Integer.MIN_VALUE) {
                try {
                    populateElevation(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void populateElevation(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
        geoName.setElevation(0);
        try {
            populateElevationGeoNames(geoName);
        } catch (Exception e) {
            populateElevationGoogle(geoName);
        }
    }

    public void populateElevationGeoNames(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
        double latitude = geoName.getLatitude();
        double longitude = geoName.getLongitude();
        String queryUrl = String.format(Locale.US, URL_ELEVATION_AGDEM, latitude, longitude, USERNAME);
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        String elevationValue = new String(data).trim();
        double elevation = Double.parseDouble(elevationValue);
        geoName.setElevation((int) elevation);
    }

    public void populateElevationGoogle(GeoName geoName) throws IOException, ParserConfigurationException, SAXException {
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
        geoName.setElevation((int) elevation);
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param records
     *         the list of records to populate.
     * @see #populateAlternateNamesInternet(GeoName)
     */
    public void populateAlternateNames(Collection<GeoName> records) {
        Map<String, AlternateName> alternateNames;
        for (GeoName record : records) {
            alternateNames = record.getAlternateNamesMap();
            if (alternateNames.size() <= 1) {
                try {
                    populateAlternateNamesInternet(record);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Populate the list of names with alternate names from the Internet.
     *
     * @param record
     *         the name to populate.
     * @see {@linktourl http://download.geonames.org/export/dump/readme.txt}
     */
    protected void populateAlternateNamesInternet(GeoName record) throws IOException {
        String queryUrl = String.format(Locale.US, URL_GEONAME_GET, record.getGeoNameId(), USERNAME);
        //queryUrl = "GeoNames/res/524901.json";
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        JsonReader reader = Json.createReader(new ByteArrayInputStream(data));
        JsonObject json = reader.readObject();
        JsonArray arr = json.getJsonArray("alternateNames");

        Map<String, AlternateName> alternateNames = record.getAlternateNamesMap();
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

    /**
     * Parse the file with GeoName alternate names.
     *
     * @param file
     *         the file to parse.
     * @param records
     *         the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(File file, Collection<GeoName> records) throws IOException {
        Reader reader = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            in = null;
            populateAlternateNames(reader, records);
        } finally {
            if (in != null)
                in.close();
            if (reader != null)
                reader.close();
        }
    }

    /**
     * Parse the file with GeoName alternate names.
     * <p>
     * <code>
     * The table 'alternate names' :<br>
     * -----------------------------
     * alternateNameId   : the id of this alternate name, int<br>
     * geonameid         : geonameId referring to id in table 'geoname', int<br>
     * isolanguage       : iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and faac for airport codes, fr_1793 for French Revolution names,  abbr for abbreviation, link for a website, varchar(7)<br>
     * alternate name    : alternate name or name variant, varchar(200)<br>
     * isPreferredName   : '1', if this alternate name is an official/preferred name<br>
     * isShortName       : '1', if this is a short name like 'California' for 'State of California'<br>
     * isColloquial      : '1', if this alternate name is a colloquial or slang term<br>
     * isHistoric        : '1', if this alternate name is historic and was used in the past<br>
     * </code>
     * </p>
     *
     * @param reader
     *         the reader.
     * @param records
     *         the list of names.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public void populateAlternateNames(Reader reader, Collection<GeoName> records) throws IOException {
        Map<Long, GeoName> recordsById = new HashMap<>();
        for (GeoName record : records) {
            recordsById.put(record.getGeoNameId(), record);
        }

        GeoName record;
        String line;
        BufferedReader buf = new BufferedReader(reader);
        String[] fields;
        String field;

        long geonameId;
        String language;
        Locale locale;
        String name;
        boolean isPreferredName;

        while (true) {
            line = buf.readLine();
            if (line == null)
                break;
            fields = line.split("\t");

            field = fields[1];
            geonameId = Long.parseLong(field);
            record = recordsById.get(geonameId);
            if (record == null) {
                continue;
            }
            field = fields[2];
            if (field.isEmpty()) {
                continue;
            }
            if ("link".equals(field) && (record.getWikipediaURL() == null)) {
                record.setWikipediaURL(field);
                continue;
            }
            language = field;
            locale = new Locale(language);
            language = locale.getLanguage();

            field = fields[3];
            name = field;

            if (fields.length > 4) {
                field = fields[4];
                isPreferredName = "1".equals(field);
            } else {
                isPreferredName = false;
            }

            record.putAlternateName(language, name, isPreferredName);
        }
    }

}

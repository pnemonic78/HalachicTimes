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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Manage lists of GeoName records.
 *
 * @author Moshe Waisberg
 */
public class GeoNames {

    public GeoNames() {
        super();
    }

    /**
     * Parse the file with GeoName records.
     *
     * @param file
     *         the file to parseCSV.
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
     *         the file to parseCSV.
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
            reader = new InputStreamReader(in, "UTF-8");
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
            record.setCc2(field);
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
                record.setElevation(Double.parseDouble(field));
            field = fields[column++];
            if (field.length() > 0)
                record.setDem(Integer.parseInt(field));
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
}

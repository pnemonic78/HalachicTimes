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
package net.sf.times.location;

import android.content.Context;
import android.location.Address;
import android.test.AndroidTestCase;

import net.sf.times.location.test.R;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GeocoderTestCase extends AndroidTestCase {

    private static SAXParserFactory parserFactory;
    private static SAXParser parser;

    protected SAXParserFactory getParserFactory() {
        if (parserFactory == null)
            parserFactory = SAXParserFactory.newInstance();
        return parserFactory;
    }

    protected SAXParser getParser() throws ParserConfigurationException, SAXException {
        if (parser == null)
            parser = getParserFactory().newSAXParser();
        return parser;
    }

    /**
     * Test Google address geocoder.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void testGoogleAddress() throws Exception {
        Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeocoderBase geocoder = new GoogleGeocoder(context);
        int maxResults = 5;

        // Holon
        List<Address> results = new ArrayList<>(maxResults);
        InputStream in = context.getResources().openRawResource(R.raw.google_holon);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(maxResults, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertNotNull(address.getExtras());
        assertEquals(32.0234380, address.getLatitude());
        assertEquals(34.7766799, address.getLongitude());
        assertEquals("Kalischer St 1-5, Holon, Israel", address.getExtras().getString(ZmanimAddress.KEY_FORMATTED));

        // Near Elad
        results = new ArrayList<>(maxResults);
        in = context.getResources().openRawResource(R.raw.google_near_elad);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(maxResults, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertNotNull(address.getExtras());
        assertEquals(32.0626167, address.getLatitude());
        assertEquals(34.9717498, address.getLongitude());
        assertEquals("Unnamed Road, Rosh Haayin, Israel", address.getExtras().getString(ZmanimAddress.KEY_FORMATTED));
    }

    /**
     * Test Google elevation geocoder.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void testGoogleElevation() throws Exception {
        Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeocoderBase geocoder = new GoogleGeocoder(context);

        // Access Denied
        List<ZmanimLocation> results = new ArrayList<>();
        InputStream in = context.getResources().openRawResource(R.raw.google_elevation_denied);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createElevationResponseHandler(results);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(0, results.size());

        // Near Elad
        results = new ArrayList<>();
        in = context.getResources().openRawResource(R.raw.google_elevation_near_elad);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createElevationResponseHandler(results);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(1, results.size());
        ZmanimLocation location = results.get(0);
        assertNotNull(location);
        assertEquals(32.0629985, location.getLatitude());
        assertEquals(34.9768113, location.getLongitude());
        assertEquals(94.6400452, location.getAltitude());
    }

    /**
     * Test GeoNames address geocoder.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void testGeoNamesAddress() throws Exception {
        Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeoNamesGeocoder geocoder = new GeoNamesGeocoder(context);
        int maxResults = 10;

        // Near Elad
        List<Address> results = new ArrayList<>(maxResults);
        InputStream in = context.getResources().openRawResource(R.raw.geonames_near_elad);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(5, results.size());

        Address address = results.get(4);
        assertNotNull(address);
        assertEquals(32.04984, address.getLatitude());
        assertEquals(34.95382, address.getLongitude());
        assertEquals("Israel", address.getCountryName());
        assertEquals("El‘ad", address.getFeatureName());
    }

}

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
package net.sf.times.location;

import android.content.Context;
import android.location.Address;
import android.test.AndroidTestCase;

import net.sf.times.location.impl.BingGeocoder;
import net.sf.times.location.impl.GeoNamesGeocoder;
import net.sf.times.location.impl.GoogleGeocoder;
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
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeocoderBase geocoder = new GoogleGeocoder(context);
        int maxResults = 10;

        // Holon
        List<Address> results = new ArrayList<>(maxResults);
        InputStream in = context.getResources().openRawResource(R.raw.google_holon);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertTrue(maxResults >= results.size());
        assertEquals(5, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0234380, address.getLatitude());
        assertEquals(34.7766799, address.getLongitude());
        assertEquals("1-5, Kalischer St, Holon, Israel", ((ZmanimAddress) address).getFormatted());

        // Near Elad
        results = new ArrayList<>(maxResults);
        in = context.getResources().openRawResource(R.raw.google_near_elad);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertTrue(maxResults >= results.size());
        assertEquals(6, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0626167, address.getLatitude());
        assertEquals(34.9717498, address.getLongitude());
        assertEquals("Unnamed Road, Rosh Haayin, Israel", ((ZmanimAddress) address).getFormatted());
    }

    /**
     * Test Google elevation geocoder.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void testGoogleElevation() throws Exception {
        final Context context = getContext();
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
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeocoderBase geocoder = new GeoNamesGeocoder(context);
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
        assertTrue(maxResults >= results.size());
        assertEquals(5, results.size());

        Address address = results.get(4);
        assertNotNull(address);
        assertEquals(32.04984, address.getLatitude());
        assertEquals(34.95382, address.getLongitude());
        assertEquals("Israel", address.getCountryName());
        assertEquals("El‘ad", address.getFeatureName());
    }

    /**
     * Test Bing address geocoder.
     *
     * @throws Exception
     *         if an error occurs.
     */
    public void testBingAddress() throws Exception {
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.getDefault();
        GeocoderBase geocoder = new BingGeocoder(context);
        int maxResults = 10;

        // Holon
        List<Address> results = new ArrayList<>(maxResults);
        InputStream in = context.getResources().openRawResource(R.raw.bing_holon);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertTrue(maxResults >= results.size());
        assertEquals(1, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0236, address.getLatitude());
        assertEquals(34.776698, address.getLongitude());
        assertEquals("Street, Holon, Israel", ((ZmanimAddress) address).getFormatted());

        // Near Elad
        results = new ArrayList<>(maxResults);
        in = context.getResources().openRawResource(R.raw.bing_near_elad);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertTrue(maxResults >= results.size());
        assertEquals(1, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.094619750976563, address.getLatitude());
        assertEquals(34.885761260986328, address.getLongitude());
        assertEquals("Petah Tiqwa, Israel", ((ZmanimAddress) address).getFormatted());
    }

}

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
package com.github.times.location;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.github.times.location.bing.BingGeocoder;
import com.github.times.location.geonames.GeoNamesGeocoder;
import com.github.times.location.google.GoogleGeocoder;
import com.github.times.location.test.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getContext;
import static com.github.times.location.GeocoderBase.SAME_CITY;
import static com.github.times.location.GeocoderBase.SAME_PLATEAU;
import static com.github.times.location.GeocoderBase.USER_PROVIDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class GeocoderTestCase {

    private static final double DELTA = 1e-3;

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
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGoogleAddress() throws Exception {
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GoogleGeocoder(locale);
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
        assertEquals(32.0234380, address.getLatitude(), DELTA);
        assertEquals(34.7766799, address.getLongitude(), DELTA);
        assertEquals("1-5, Kalischer St, Holon, Center District, Israel", ((ZmanimAddress) address).getFormatted());

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
        assertEquals(32.0626167, address.getLatitude(), DELTA);
        assertEquals(34.9717498, address.getLongitude(), DELTA);
        assertEquals("Unnamed Road, Rosh Haayin, Petach Tikva, Center District, Israel", ((ZmanimAddress) address).getFormatted());

        // Bar Yochai
        results = new ArrayList<>(maxResults);
        in = context.getResources().openRawResource(R.raw.google_bar_yohai);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createAddressResponseHandler(results, maxResults, locale);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertTrue(maxResults >= results.size());
        assertEquals(9, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.99505, address.getLatitude(), DELTA);
        assertEquals(35.44968, address.getLongitude(), DELTA);
        assertEquals("331, Bar Yohai, Tzfat, North District, Israel", ((ZmanimAddress) address).getFormatted());

        AddressProvider addressProvider = new AddressProvider(context);
        Location location = new Location(USER_PROVIDER);
        location.setLatitude(32.99505);
        location.setLongitude(35.44968);
        address = addressProvider.findBestAddress(location, results, SAME_PLATEAU);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(results.get(0), address);
        address = addressProvider.findBestAddress(location, results, SAME_CITY);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(results.get(0), address);
    }

    /**
     * Test Google elevation geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGoogleElevation() throws Exception {
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GoogleGeocoder(locale);

        // Access Denied
        List<ZmanimLocation> results = new ArrayList<>();
        InputStream in = context.getResources().openRawResource(R.raw.google_elevation_denied);
        assertNotNull(in);
        SAXParser parser = getParser();
        assertNotNull(parser);
        DefaultHandler handler = geocoder.createElevationResponseHandler(0.0, 0.0, results);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(0, results.size());

        // Near Elad
        results = new ArrayList<>();
        in = context.getResources().openRawResource(R.raw.google_elevation_near_elad);
        assertNotNull(in);
        parser = getParser();
        assertNotNull(parser);
        handler = geocoder.createElevationResponseHandler(0.0, 0.0, results);
        assertNotNull(handler);
        parser.parse(in, handler);
        assertEquals(1, results.size());
        ZmanimLocation location = results.get(0);
        assertNotNull(location);
        assertEquals(32.0629985, location.getLatitude(), DELTA);
        assertEquals(34.9768113, location.getLongitude(), DELTA);
        assertEquals(94.6400452, location.getAltitude(), DELTA);
    }

    /**
     * Test GeoNames address geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGeoNamesAddress() throws Exception {
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GeoNamesGeocoder(locale);
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
        assertEquals(32.04984, address.getLatitude(), DELTA);
        assertEquals(34.95382, address.getLongitude(), DELTA);
        assertEquals("Israel", address.getCountryName());
        assertEquals("El‘ad", address.getFeatureName());
    }

    /**
     * Test Bing address geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testBingAddress() throws Exception {
        final Context context = getContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new BingGeocoder(locale);
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
        assertEquals(32.0236, address.getLatitude(), DELTA);
        assertEquals(34.776698, address.getLongitude(), DELTA);
        assertEquals("Street, Holon, Tel Aviv, Israel", ((ZmanimAddress) address).getFormatted());

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
        assertEquals(32.094619750976563, address.getLatitude(), DELTA);
        assertEquals(34.885761260986328, address.getLongitude(), DELTA);
        assertEquals("Petah Tiqwa, Merkaz, Israel", ((ZmanimAddress) address).getFormatted());
    }
}

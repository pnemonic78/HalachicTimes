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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.github.times.location.GeocoderBase.SAME_CITY;
import static com.github.times.location.GeocoderBase.SAME_PLATEAU;
import static com.github.times.location.GeocoderBase.USER_PROVIDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeocoderTestCase {

    private static final double DELTA = 1e-3;

    /**
     * Test Google address geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGoogleAddress() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GoogleGeocoder(locale);
        final AddressResponseParser parser = geocoder.getAddressResponseParser();
        assertNotNull(parser);
        int maxResults = 10;
        List<Address> results;

        // Holon
        InputStream in = context.getResources().openRawResource(R.raw.google_holon);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(9, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0234380, address.getLatitude(), DELTA);
        assertEquals(34.7766799, address.getLongitude(), DELTA);
        assertEquals("1, Kalischer St, Holon, Center District, Israel", ((ZmanimAddress) address).getFormatted());

        // Near Elad
        in = context.getResources().openRawResource(R.raw.google_near_elad);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(6, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0626167, address.getLatitude(), DELTA);
        assertEquals(34.9717498, address.getLongitude(), DELTA);
        assertEquals("Unnamed Road, Rosh Haayin, Petach Tikva, Center District, Israel", ((ZmanimAddress) address).getFormatted());

        // Bar Yochai
        in = context.getResources().openRawResource(R.raw.google_bar_yohai);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
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

        // Near Jerusalem
        in = context.getResources().openRawResource(R.raw.google_jerusalem);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(0, results.size());
    }

    /**
     * Test Google elevation geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGoogleElevation() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GoogleGeocoder(locale);
        final ElevationResponseParser parser = geocoder.getElevationResponseParser();
        assertNotNull(parser);

        // Access Denied
        List<Location> results;
        InputStream in;

        // Near Elad
        in = context.getResources().openRawResource(R.raw.google_elevation_near_elad);
        assertNotNull(in);
        results = parser.parse(in, 0.0, 0.0, 1);
        assertNotNull(results);
        assertEquals(1, results.size());
        Location location = results.get(0);
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
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GeoNamesGeocoder(locale);
        final AddressResponseParser parser = geocoder.getAddressResponseParser();
        assertNotNull(parser);
        int maxResults = 10;
        List<Address> results;

        // Near Elad
        InputStream in = context.getResources().openRawResource(R.raw.geonames_near_elad);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(5, results.size());

        Address address = results.get(4);
        assertNotNull(address);
        assertEquals(32.04984, address.getLatitude(), DELTA);
        assertEquals(34.95382, address.getLongitude(), DELTA);
        assertEquals("Israel", address.getCountryName());
        assertEquals("El‘ad", address.getFeatureName());

        // Tel-Aviv
        in = context.getResources().openRawResource(R.raw.geonames_telaviv);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(6, results.size());

        address = results.get(5);
        assertNotNull(address);
        assertEquals(32.06948, address.getLatitude(), DELTA);
        assertEquals(34.7689, address.getLongitude(), DELTA);
        assertEquals("Israel", address.getCountryName());
        assertEquals("Kerem HaTemanim", address.getFeatureName());

        // Arctic Ocean
        in = context.getResources().openRawResource(R.raw.geonames_arctic);
        assertNotNull(in);
        results = parser.parse(in, 89.89511, -36.3637, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(1, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertEquals(89.89511, address.getLatitude(), DELTA);
        assertEquals(-36.3637, address.getLongitude(), DELTA);
        assertEquals("Arctic Ocean", address.getFeatureName());

        // Empty
        in = context.getResources().openRawResource(R.raw.geonames_empty);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(0, results.size());
    }

    /**
     * Test GeoNames elevation geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testGeoNamesElevation() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new GeoNamesGeocoder(locale);
        final ElevationResponseParser parser = geocoder.getElevationResponseParser();
        assertNotNull(parser);
        List<Location> results;

        // Near Elad
        InputStream in = context.getResources().openRawResource(R.raw.geonames_elevation_near_elad);
        assertNotNull(in);
        results = parser.parse(in, 32.04984, 34.95382, 1);
        assertNotNull(results);
        assertEquals(1, results.size());
        Location location = results.get(0);
        assertNotNull(location);
        assertEquals(32.04984, location.getLatitude(), DELTA);
        assertEquals(34.95382, location.getLongitude(), DELTA);
        assertEquals(30, location.getAltitude(), DELTA);
    }

    /**
     * Test Bing address geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testBingAddress() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        final GeocoderBase geocoder = new BingGeocoder(locale);
        final AddressResponseParser parser = geocoder.getAddressResponseParser();
        assertNotNull(parser);
        int maxResults = 10;
        List<Address> results;

        // Holon
        InputStream in = context.getResources().openRawResource(R.raw.bing_holon);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(5, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.0236, address.getLatitude(), DELTA);
        assertEquals(34.776698, address.getLongitude(), DELTA);
        assertEquals("Shenkar Arye, Holon, Tel-Aviv, Tel Aviv, Israel", ((ZmanimAddress) address).getFormatted());

        // Near Elad
        in = context.getResources().openRawResource(R.raw.bing_near_elad);
        assertNotNull(in);
        results = parser.parse(in, maxResults, locale);
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(1, results.size());

        address = results.get(0);
        assertNotNull(address);
        assertTrue(address instanceof ZmanimAddress);
        assertEquals(32.094619750976563, address.getLatitude(), DELTA);
        assertEquals(34.885761260986328, address.getLongitude(), DELTA);
        assertEquals("Orlov Ze'Ev & Bar Kokhva, Petah Tikva, Merkaz, Israel", ((ZmanimAddress) address).getFormatted());
    }

    /**
     * Test Bing elevation geocoder.
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void testBingElevation() throws Exception {
        final Context context = getApplicationContext();
        assertNotNull(context);

        Locale locale = Locale.US;
        GeocoderBase geocoder = new BingGeocoder(locale);
        final ElevationResponseParser parser = geocoder.getElevationResponseParser();
        assertNotNull(parser);
        List<Location> results;

        // Holon
        InputStream in = context.getResources().openRawResource(R.raw.bing_elevation_holon);
        assertNotNull(in);
        results = parser.parse(in, 32.0236, 34.776698, 1);
        assertNotNull(results);
        assertEquals(1, results.size());
        Location location = results.get(0);
        assertNotNull(location);
        assertEquals(32.0236, location.getLatitude(), DELTA);
        assertEquals(34.776698, location.getLongitude(), DELTA);
        assertEquals(35, location.getAltitude(), DELTA);
    }

    /**
     * Test internal address geocoder.
     */
    @Test
    public void testInternalGeocoderAddress() {
        final Context context = getApplicationContext();
        assertNotNull(context);

        int maxResults = 5;

        // Bar Yochai
        List<Address> results = createInternalGeocoderAddresses();
        assertNotNull(results);
        assertTrue(maxResults >= results.size());
        assertEquals(5, results.size());

        Address address = results.get(0);
        assertNotNull(address);
        assertFalse(address instanceof ZmanimAddress);
        assertEquals(32.99505, address.getLatitude(), DELTA);
        assertEquals(35.44968, address.getLongitude(), DELTA);
        ZmanimAddress zmanimAddress = new ZmanimAddress(address);
        assertEquals("331, Bar Yohai, Tzfat, North District, Israel", zmanimAddress.getFormatted());
    }

    private List<Address> createInternalGeocoderAddresses() {
        List<Address> results = new ArrayList<>(5);
        Address address;
        Locale locale = Locale.US;

        address = new Address(locale);
        address.setAddressLine(0, "331, Bar Yohai, Israel");
        address.setAdminArea("North District");
        address.setCountryCode("IL");
        address.setCountryName("Israel");
        address.setFeatureName("331");
        address.setLatitude(32.9959042);
        address.setLocality("Bar Yohai");
        address.setLongitude(35.450468199999996);
        address.setPremises("331");
        address.setSubAdminArea("Tzfat");
        results.add(address);

        address = new Address(locale);
        address.setAddressLine(0, "86, Bar Yohai, Israel");
        address.setAdminArea("North District");
        address.setCountryCode("IL");
        address.setCountryName("Israel");
        address.setFeatureName("86");
        address.setLatitude(32.9964071);
        address.setLocality("Bar Yohai");
        address.setLongitude(35.4495705);
        address.setPremises("86");
        address.setSubAdminArea("Tzfat");
        results.add(address);

        address = new Address(locale);
        address.setAddressLine(0, "Derech HaZayit, Bar Yohai, Israel");
        address.setAdminArea("North District");
        address.setCountryCode("IL");
        address.setCountryName("Israel");
        address.setFeatureName("Derech HaZayit");
        address.setLatitude(32.996314999999996);
        address.setLocality("Bar Yohai");
        address.setLongitude(35.4487106);
        address.setSubAdminArea("Tzfat");
        address.setThoroughfare("Derech HaZayit");
        results.add(address);

        address = new Address(locale);
        address.setAddressLine(0, "Bar Yohai, Israel");
        address.setAdminArea("North District");
        address.setCountryCode("IL");
        address.setCountryName("Israel");
        address.setFeatureName("Bar Yohai");
        address.setLatitude(32.997704);
        address.setLocality("Bar Yohai");
        address.setLongitude(35.44819);
        address.setSubAdminArea("Tzfat");
        results.add(address);

        address = new Address(locale);
        address.setAddressLine(0, "Merom HaGalil Regional Council, Israel");
        address.setAdminArea("North District");
        address.setCountryCode("IL");
        address.setCountryName("Israel");
        address.setFeatureName("Merom HaGalil Regional Council");
        address.setLatitude(32.9916546);
        address.setLongitude(35.467236799999995);
        address.setSubAdminArea("Merom HaGalil Regional Council");
        results.add(address);

        return results;
    }
}

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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.github.times.location.LocationPreferences.Values.FORMAT_DECIMAL;
import static com.github.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.location.Location;

import org.junit.Test;

import java.util.TimeZone;

public class LocationsTestCase {

    protected LocationApplication getApplication() {
        Context applicationContext = getApplicationContext();
        assertNotNull(applicationContext);
        assertTrue(applicationContext instanceof LocationApplication);
        return (LocationApplication) applicationContext;
    }

    /**
     * Test application.
     */
    @Test
    public void testApp() {
        final Context context = getApplicationContext();
        assertNotNull(context);
        assertEquals("com.github.times.location.test", context.getPackageName());

        LocationApplication app = getApplication();
        assertNotNull(app);
        LocationsProvider locations = app.getLocations();
        assertNotNull(locations);
    }

    /**
     * Test time zones.
     * <p/>
     * Loop through all TZs and check that their longitude and latitude are
     * valid.
     */
    @Test
    public void testTZ() {
        LocationApplication app = getApplication();
        assertNotNull(app);
        LocationsProvider locations = app.getLocations();
        assertNotNull(locations);

        String[] ids = TimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertNotEquals(0, ids.length);

        TimeZone tz;
        Location location;
        double latitude;
        double longitude;

        for (String id : ids) {
            assertNotNull(id);
            tz = TimeZone.getTimeZone(id);
            assertNotNull(tz);

            location = locations.getLocationTZ(tz);
            assertNotNull(location);
            latitude = location.getLatitude();
            assertTrue(id + " " + latitude, latitude >= ZmanimLocation.LATITUDE_MIN);
            assertTrue(id + " " + latitude, latitude <= ZmanimLocation.LATITUDE_MAX);
            longitude = location.getLongitude();
            assertTrue(id + " " + longitude, longitude >= ZmanimLocation.LONGITUDE_MIN);
            assertTrue(id + " " + longitude, longitude <= ZmanimLocation.LONGITUDE_MAX);
        }
    }

    /**
     * Test Rhumb Line angles.
     */
    @Test
    public void testAngle() {
        ZmanimLocation temple = new ZmanimLocation("temple");
        temple.setLatitude(toDegrees(31, 46, 40, Hemisphere.NORTH));
        temple.setLongitude(toDegrees(35, 14, 04, Hemisphere.EAST));
        assertLocation(temple);
        assertEquals(31.77777777f, temple.getLatitude(), 0.000001);

        ZmanimLocation location = new ZmanimLocation("city");
        final float delta = 1f;

        // Anchorage
        location.setLatitude(toDegrees(61, 01, Hemisphere.NORTH));
        location.setLongitude(toDegrees(150, 00, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(256, location.angleTo(temple), delta);

        // San Francisco
        location.setLatitude(toDegrees(37, 45, Hemisphere.NORTH));
        location.setLongitude(toDegrees(122, 27, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(93, location.angleTo(temple), delta);

        // Los Angeles
        location.setLatitude(toDegrees(34, 03, Hemisphere.NORTH));
        location.setLongitude(toDegrees(118, 15, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(91, location.angleTo(temple), delta);

        // Chicago
        location.setLatitude(toDegrees(41, 50, Hemisphere.NORTH));
        location.setLongitude(toDegrees(87, 37, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(96, location.angleTo(temple), delta);

        // Miami
        location.setLatitude(toDegrees(25, 45, Hemisphere.NORTH));
        location.setLongitude(toDegrees(80, 15, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(87, location.angleTo(temple), delta);

        // Toronto
        location.setLatitude(toDegrees(43, 42, Hemisphere.NORTH));
        location.setLongitude(toDegrees(79, 25, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(97, location.angleTo(temple), delta);

        // Washington
        location.setLatitude(toDegrees(38, 55, Hemisphere.NORTH));
        location.setLongitude(toDegrees(77, 00, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(94, location.angleTo(temple), delta);

        // Philadelphia
        location.setLatitude(toDegrees(40, 00, Hemisphere.NORTH));
        location.setLongitude(toDegrees(75, 10, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(95, location.angleTo(temple), delta);

        // New York
        location.setLatitude(toDegrees(40, 43, Hemisphere.NORTH));
        location.setLongitude(toDegrees(74, 00, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(96, location.angleTo(temple), delta);

        // Boston
        location.setLatitude(toDegrees(42, 21, Hemisphere.NORTH));
        location.setLongitude(toDegrees(71, 04, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(97, location.angleTo(temple), delta);

        // Bueons Aires
        location.setLatitude(toDegrees(34, 40, Hemisphere.SOUTH));
        location.setLongitude(toDegrees(58, 30, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(53, location.angleTo(temple), delta);

        // London
        location.setLatitude(toDegrees(51, 30, Hemisphere.NORTH));
        location.setLongitude(toDegrees(0, 10, Hemisphere.WEST));
        assertLocation(location);
        assertEquals(127, location.angleTo(temple), delta);

        // Paris
        location.setLatitude(toDegrees(48, 52, Hemisphere.NORTH));
        location.setLongitude(toDegrees(2, 20, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(125, location.angleTo(temple), delta);

        // Budapest
        location.setLatitude(toDegrees(47, 30, Hemisphere.NORTH));
        location.setLongitude(toDegrees(19, 03, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(142, location.angleTo(temple), delta);

        // Johannesburg
        location.setLatitude(toDegrees(26, 10, Hemisphere.SOUTH));
        location.setLongitude(toDegrees(28, 02, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(7, location.angleTo(temple), delta);

        // Kiev
        location.setLatitude(toDegrees(50, 25, Hemisphere.NORTH));
        location.setLongitude(toDegrees(30, 30, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(169, location.angleTo(temple), delta);

        // Tel Aviv
        location.setLatitude(toDegrees(32, 05, Hemisphere.NORTH));
        location.setLongitude(toDegrees(34, 46, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(128, location.angleTo(temple), delta);

        // Haifa
        location.setLatitude(toDegrees(32, 49, Hemisphere.NORTH));
        location.setLongitude(toDegrees(34, 59, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(168, location.angleTo(temple), delta);

        // Moscow
        location.setLatitude(toDegrees(55, 45, Hemisphere.NORTH));
        location.setLongitude(toDegrees(37, 37, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(184, location.angleTo(temple), delta);

        // Tokyo
        location.setLatitude(toDegrees(35, 40, Hemisphere.NORTH));
        location.setLongitude(toDegrees(139, 45, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(267, location.angleTo(temple), delta);

        // Melbourne
        location.setLatitude(toDegrees(37, 50, Hemisphere.SOUTH));
        location.setLongitude(toDegrees(144, 59, Hemisphere.EAST));
        assertLocation(location);
        assertEquals(304, location.angleTo(temple), delta);
    }

    private void assertLocation(Location location) {
        final double latitude = location.getLatitude();
        if ((latitude < ZmanimLocation.LATITUDE_MIN) || (latitude > ZmanimLocation.LATITUDE_MAX))
            fail("Invalid latitude: " + latitude);
        final double longitude = location.getLongitude();
        if ((longitude < ZmanimLocation.LONGITUDE_MIN) || (longitude > ZmanimLocation.LONGITUDE_MAX))
            fail("Invalid longitude: " + longitude);
    }

    private enum Hemisphere {
        NORTH(+1),
        EAST(+1),
        SOUTH(-1),
        WEST(-1);

        private final int sign;

        Hemisphere(int sign) {
            this.sign = sign;
        }

        public int sign() {
            return sign;
        }
    }

    private double toDegrees(int d, int m, Hemisphere hemisphere) {
        return toDegrees(d, m, 0, hemisphere);
    }

    private double toDegrees(int d, int m, int s, Hemisphere hemisphere) {
        assertTrue(d >= 0);
        assertTrue(d <= 180);
        assertTrue(m >= 0);
        assertTrue(m < 60);
        assertTrue(s >= 0);
        assertTrue(s < 60);
        return hemisphere.sign() * (d + ((m + (s / 60.0)) / 60.0));
    }

    @Test
    public void testParseLatitudeDecimal() {
        Context context = getApplicationContext();
        SimpleLocationPreferences.init(context);

        LocationFormatter formatter = new SimpleLocationFormatter(context, FORMAT_DECIMAL, true);
        assertTrue(Double.isNaN(formatter.parseLatitude("")));
        assertTrue(Double.isNaN(formatter.parseLatitude("a")));
        assertTrue(Double.isNaN(formatter.parseLatitude(",")));
        assertTrue(Double.isNaN(formatter.parseLatitude(".")));

        double delta = 1e-6;
        assertEquals(12.0, formatter.parseLatitude("12"), delta);
        assertEquals(12.0, formatter.parseLatitude("12."), delta);
        assertEquals(12.0, formatter.parseLatitude("12.0"), delta);
        assertEquals(12.3456, formatter.parseLatitude("12.3456000"), delta);
        assertEquals(-12, formatter.parseLatitude("-12"), delta);
        assertEquals(-12.3456, formatter.parseLatitude("-12.3456"), delta);

        assertEquals(-90, formatter.parseLatitude("-90"), delta);
        assertEquals(90, formatter.parseLatitude("90"), delta);
        assertTrue(Double.isNaN(formatter.parseLatitude("-91")));
        assertTrue(Double.isNaN(formatter.parseLatitude("91")));
    }

    @Test
    public void testParseLongitudeDecimal() {
        Context context = getApplicationContext();
        SimpleLocationPreferences.init(context);

        LocationFormatter formatter = new SimpleLocationFormatter(context, FORMAT_DECIMAL, true);
        assertTrue(Double.isNaN(formatter.parseLongitude("")));
        assertTrue(Double.isNaN(formatter.parseLongitude("a")));
        assertTrue(Double.isNaN(formatter.parseLongitude(",")));
        assertTrue(Double.isNaN(formatter.parseLongitude(".")));

        double delta = 1e-6;
        assertEquals(12.0, formatter.parseLongitude("12"), delta);
        assertEquals(12.0, formatter.parseLongitude("12."), delta);
        assertEquals(12.0, formatter.parseLongitude("12.0"), delta);
        assertEquals(12.3456, formatter.parseLongitude("12.3456000"), delta);
        assertEquals(-12, formatter.parseLongitude("-12"), delta);
        assertEquals(-12.3456, formatter.parseLongitude("-12.3456"), delta);

        assertEquals(-180, formatter.parseLongitude("-180"), delta);
        assertEquals(180, formatter.parseLongitude("180"), delta);
        assertTrue(Double.isNaN(formatter.parseLongitude("-181")));
        assertTrue(Double.isNaN(formatter.parseLongitude("181")));
    }

    @Test
    public void testParseLatitudeSexagecimal() {
        Context context = getApplicationContext();
        SimpleLocationPreferences.init(context);

        LocationFormatter formatter = new SimpleLocationFormatter(context, FORMAT_SEXAGESIMAL, true);
        assertTrue(Double.isNaN(formatter.parseLatitude("")));
        assertTrue(Double.isNaN(formatter.parseLatitude("a")));
        assertTrue(Double.isNaN(formatter.parseLatitude("\u00B0")));

        double delta = 1e-6;
        assertEquals(12.0, formatter.parseLatitude("12"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B0"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B00"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B000"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'00"), delta);
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'00\""), delta);
        assertEquals(12.0, formatter.parseLatitude(formatter.formatLatitudeSexagesimal(12.0)), delta);

        assertEquals(12.345, formatter.parseLatitude(formatter.formatLatitudeSexagesimal(12.345)), delta);
        assertEquals(-12.345, formatter.parseLatitude(formatter.formatLatitudeSexagesimal(-12.345)), delta);

        assertEquals(-90, formatter.parseLatitude("-90"), delta);
        assertEquals(90, formatter.parseLatitude("90"), delta);
        assertTrue(Double.isNaN(formatter.parseLatitude("-91")));
        assertTrue(Double.isNaN(formatter.parseLatitude("91")));
    }

    @Test
    public void testParseLongitudeSexagecimal() {
        Context context = getApplicationContext();
        SimpleLocationPreferences.init(context);

        LocationFormatter formatter = new SimpleLocationFormatter(context, FORMAT_SEXAGESIMAL, true);
        assertTrue(Double.isNaN(formatter.parseLongitude("")));
        assertTrue(Double.isNaN(formatter.parseLongitude("a")));
        assertTrue(Double.isNaN(formatter.parseLongitude("\u00B0")));

        double delta = 1e-6;
        assertEquals(12.0, formatter.parseLongitude("12"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B0"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B00"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B000"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'00"), delta);
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'00\""), delta);
        assertEquals(12.0, formatter.parseLongitude(formatter.formatLongitudeSexagesimal(12.0)), delta);

        assertEquals(12.345, formatter.parseLongitude(formatter.formatLongitudeSexagesimal(12.345)), delta);
        assertEquals(-12.345, formatter.parseLongitude(formatter.formatLongitudeSexagesimal(-12.345)), delta);

        assertEquals(-180, formatter.parseLongitude("-180\u00B0"), delta);
        assertEquals(180, formatter.parseLongitude("180\u00B0"), delta);
        assertTrue(Double.isNaN(formatter.parseLongitude("-181\u00B0")));
        assertTrue(Double.isNaN(formatter.parseLongitude("181\u00B0")));
    }
}

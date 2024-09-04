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
package com.github.times.location

import android.location.Location
import com.github.times.BaseTests
import java.util.TimeZone
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationsTestCase : BaseTests() {
    private val application: LocationApplication<*, *, *>
        get() {
            val applicationContext = context
            assertNotNull(applicationContext)
            assertTrue(applicationContext is LocationApplication<*, *, *>)
            return applicationContext as LocationApplication<*, *, *>
        }

    /**
     * Test application.
     */
    @Test
    fun testApp() {
        assertEquals("com.github.times.location.test", context.packageName)
        val app = application
        assertNotNull(app)
        val locations = app.locations
        assertNotNull(locations)
    }

    /**
     * Test time zones.
     *
     *
     * Loop through all TZs and check that their longitude and latitude are
     * valid.
     */
    @Test
    fun testTZ() {
        val app = application
        assertNotNull(app)
        val locations = app.locations
        assertNotNull(locations)
        val ids = TimeZone.getAvailableIDs()
        assertNotNull(ids)
        assertNotEquals(0, ids.size)
        var tz: TimeZone?
        var location: Location?
        var latitude: Double
        var longitude: Double
        for (id in ids) {
            assertNotNull(id)
            tz = TimeZone.getTimeZone(id)
            assertNotNull(tz)
            location = locations.getLocationTZ(tz)
            assertNotNull(location)
            latitude = location.latitude
            assertTrue("$id $latitude", latitude >= ZmanimLocation.LATITUDE_MIN)
            assertTrue("$id $latitude", latitude <= ZmanimLocation.LATITUDE_MAX)
            longitude = location.longitude
            assertTrue("$id $longitude", longitude >= ZmanimLocation.LONGITUDE_MIN)
            assertTrue("$id $longitude", longitude <= ZmanimLocation.LONGITUDE_MAX)
        }
    }

    /**
     * Test Rhumb Line angles.
     */
    @Test
    fun testAngle() {
        val temple = ZmanimLocation("temple")
        temple.latitude = toDegrees(31, 46, 40, Hemisphere.NORTH)
        temple.longitude = toDegrees(35, 14, 4, Hemisphere.EAST)
        assertLocation(temple)
        assertEquals(31.77777777, temple.latitude, 0.000001)

        val location = ZmanimLocation("city")
        val delta = 1f

        // Anchorage
        location.latitude = toDegrees(61, 1, Hemisphere.NORTH)
        location.longitude = toDegrees(150, 0, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(256f, location.angleTo(temple), delta)

        // San Francisco
        location.latitude = toDegrees(37, 45, Hemisphere.NORTH)
        location.longitude = toDegrees(122, 27, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(93f, location.angleTo(temple), delta)

        // Los Angeles
        location.latitude = toDegrees(34, 3, Hemisphere.NORTH)
        location.longitude = toDegrees(118, 15, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(91f, location.angleTo(temple), delta)

        // Chicago
        location.latitude = toDegrees(41, 50, Hemisphere.NORTH)
        location.longitude = toDegrees(87, 37, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(96f, location.angleTo(temple), delta)

        // Miami
        location.latitude = toDegrees(25, 45, Hemisphere.NORTH)
        location.longitude = toDegrees(80, 15, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(87f, location.angleTo(temple), delta)

        // Toronto
        location.latitude = toDegrees(43, 42, Hemisphere.NORTH)
        location.longitude = toDegrees(79, 25, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(97f, location.angleTo(temple), delta)

        // Washington
        location.latitude = toDegrees(38, 55, Hemisphere.NORTH)
        location.longitude = toDegrees(77, 0, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(94f, location.angleTo(temple), delta)

        // Philadelphia
        location.latitude = toDegrees(40, 0, Hemisphere.NORTH)
        location.longitude = toDegrees(75, 10, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(95f, location.angleTo(temple), delta)

        // New York
        location.latitude = toDegrees(40, 43, Hemisphere.NORTH)
        location.longitude = toDegrees(74, 0, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(96f, location.angleTo(temple), delta)

        // Boston
        location.latitude = toDegrees(42, 21, Hemisphere.NORTH)
        location.longitude = toDegrees(71, 4, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(97f, location.angleTo(temple), delta)

        // Bueons Aires
        location.latitude = toDegrees(34, 40, Hemisphere.SOUTH)
        location.longitude = toDegrees(58, 30, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(53f, location.angleTo(temple), delta)

        // London
        location.latitude = toDegrees(51, 30, Hemisphere.NORTH)
        location.longitude = toDegrees(0, 10, Hemisphere.WEST)
        assertLocation(location)
        assertEquals(127f, location.angleTo(temple), delta)

        // Paris
        location.latitude = toDegrees(48, 52, Hemisphere.NORTH)
        location.longitude = toDegrees(2, 20, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(125f, location.angleTo(temple), delta)

        // Budapest
        location.latitude = toDegrees(47, 30, Hemisphere.NORTH)
        location.longitude = toDegrees(19, 3, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(142f, location.angleTo(temple), delta)

        // Johannesburg
        location.latitude = toDegrees(26, 10, Hemisphere.SOUTH)
        location.longitude = toDegrees(28, 2, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(7f, location.angleTo(temple), delta)

        // Kiev
        location.latitude = toDegrees(50, 25, Hemisphere.NORTH)
        location.longitude = toDegrees(30, 30, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(169f, location.angleTo(temple), delta)

        // Tel Aviv
        location.latitude = toDegrees(32, 5, Hemisphere.NORTH)
        location.longitude = toDegrees(34, 46, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(128f, location.angleTo(temple), delta)

        // Haifa
        location.latitude = toDegrees(32, 49, Hemisphere.NORTH)
        location.longitude = toDegrees(34, 59, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(168f, location.angleTo(temple), delta)

        // Moscow
        location.latitude = toDegrees(55, 45, Hemisphere.NORTH)
        location.longitude = toDegrees(37, 37, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(184f, location.angleTo(temple), delta)

        // Tokyo
        location.latitude = toDegrees(35, 40, Hemisphere.NORTH)
        location.longitude = toDegrees(139, 45, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(267f, location.angleTo(temple), delta)

        // Melbourne
        location.latitude = toDegrees(37, 50, Hemisphere.SOUTH)
        location.longitude = toDegrees(144, 59, Hemisphere.EAST)
        assertLocation(location)
        assertEquals(304f, location.angleTo(temple), delta)
    }

    private fun assertLocation(location: Location) {
        val latitude = location.latitude
        if (latitude < ZmanimLocation.LATITUDE_MIN || latitude > ZmanimLocation.LATITUDE_MAX) Assert.fail(
            "Invalid latitude: $latitude"
        )
        val longitude = location.longitude
        if (longitude < ZmanimLocation.LONGITUDE_MIN || longitude > ZmanimLocation.LONGITUDE_MAX) Assert.fail(
            "Invalid longitude: $longitude"
        )
    }

    private enum class Hemisphere(private val sign: Int) {
        NORTH(+1), EAST(+1), SOUTH(-1), WEST(-1);

        fun sign(): Int {
            return sign
        }
    }

    private fun toDegrees(d: Int, m: Int, hemisphere: Hemisphere): Double {
        return toDegrees(d, m, 0, hemisphere)
    }

    private fun toDegrees(d: Int, m: Int, s: Int, hemisphere: Hemisphere): Double {
        assertTrue(d >= 0)
        assertTrue(d <= 180)
        assertTrue(m >= 0)
        assertTrue(m < 60)
        assertTrue(s >= 0)
        assertTrue(s < 60)
        return hemisphere.sign() * (d + (m +( s / 60.0)) / 60.0)
    }

    @Test
    fun testParseLatitudeDecimal() {
        SimpleLocationPreferences.init(context)
        val formatter: LocationFormatter =
            SimpleLocationFormatter(context, LocationPreferences.Values.FORMAT_DECIMAL!!, true)
        assertTrue(formatter.parseLatitude("").isNaN())
        assertTrue(formatter.parseLatitude("a").isNaN())
        assertTrue(formatter.parseLatitude(",").isNaN())
        assertTrue(formatter.parseLatitude(".").isNaN())
        val delta = 1e-6
        assertEquals(12.0, formatter.parseLatitude("12"), delta)
        assertEquals(12.0, formatter.parseLatitude("12."), delta)
        assertEquals(12.0, formatter.parseLatitude("12.0"), delta)
        assertEquals(12.3456, formatter.parseLatitude("12.3456000"), delta)
        assertEquals(-12.0, formatter.parseLatitude("-12"), delta)
        assertEquals(-12.3456, formatter.parseLatitude("-12.3456"), delta)
        assertEquals(-90.0, formatter.parseLatitude("-90"), delta)
        assertEquals(90.0, formatter.parseLatitude("90"), delta)
        assertTrue(formatter.parseLatitude("-91").isNaN())
        assertTrue(formatter.parseLatitude("91").isNaN())
    }

    @Test
    fun testParseLongitudeDecimal() {
        SimpleLocationPreferences.init(context)
        val formatter: LocationFormatter =
            SimpleLocationFormatter(context, LocationPreferences.Values.FORMAT_DECIMAL!!, true)
        assertTrue(formatter.parseLongitude("").isNaN())
        assertTrue(formatter.parseLongitude("a").isNaN())
        assertTrue(formatter.parseLongitude(",").isNaN())
        assertTrue(formatter.parseLongitude(".").isNaN())
        val delta = 1e-6
        assertEquals(12.0, formatter.parseLongitude("12"), delta)
        assertEquals(12.0, formatter.parseLongitude("12."), delta)
        assertEquals(12.0, formatter.parseLongitude("12.0"), delta)
        assertEquals(12.3456, formatter.parseLongitude("12.3456000"), delta)
        assertEquals(-12.0, formatter.parseLongitude("-12"), delta)
        assertEquals(-12.3456, formatter.parseLongitude("-12.3456"), delta)
        assertEquals(-180.0, formatter.parseLongitude("-180"), delta)
        assertEquals(180.0, formatter.parseLongitude("180"), delta)
        assertTrue(formatter.parseLongitude("-181").isNaN())
        assertTrue(formatter.parseLongitude("181").isNaN())
    }

    @Test
    fun testParseLatitudeSexagecimal() {
        SimpleLocationPreferences.init(context)
        val formatter: LocationFormatter =
            SimpleLocationFormatter(context, LocationPreferences.Values.FORMAT_SEXAGESIMAL!!, true)
        assertTrue(formatter.parseLatitude("").isNaN())
        assertTrue(formatter.parseLatitude("a").isNaN())
        assertTrue(formatter.parseLatitude("\u00B0").isNaN())
        val delta = 1e-6
        assertEquals(12.0, formatter.parseLatitude("12"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B0"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B00"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B000"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'00"), delta)
        assertEquals(12.0, formatter.parseLatitude("12\u00B000'00\""), delta)
        assertEquals(
            12.0,
            formatter.parseLatitude(formatter.formatLatitudeSexagesimal(12.0)),
            delta
        )
        assertEquals(
            12.345,
            formatter.parseLatitude(formatter.formatLatitudeSexagesimal(12.345)),
            delta
        )
        assertEquals(
            -12.345,
            formatter.parseLatitude(formatter.formatLatitudeSexagesimal(-12.345)),
            delta
        )
        assertEquals(-90.0, formatter.parseLatitude("-90"), delta)
        assertEquals(90.0, formatter.parseLatitude("90"), delta)
        assertTrue(formatter.parseLatitude("-91").isNaN())
        assertTrue(formatter.parseLatitude("91").isNaN())
    }

    @Test
    fun testParseLongitudeSexagecimal() {
        SimpleLocationPreferences.init(context)
        val formatter: LocationFormatter =
            SimpleLocationFormatter(context, LocationPreferences.Values.FORMAT_SEXAGESIMAL!!, true)
        assertTrue(formatter.parseLongitude("").isNaN())
        assertTrue(formatter.parseLongitude("a").isNaN())
        assertTrue(formatter.parseLongitude("\u00B0").isNaN())
        val delta = 1e-6
        assertEquals(12.0, formatter.parseLongitude("12"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B0"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B00"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B000"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'00"), delta)
        assertEquals(12.0, formatter.parseLongitude("12\u00B000'00\""), delta)
        assertEquals(
            12.0,
            formatter.parseLongitude(formatter.formatLongitudeSexagesimal(12.0)),
            delta
        )
        assertEquals(
            12.345,
            formatter.parseLongitude(formatter.formatLongitudeSexagesimal(12.345)),
            delta
        )
        assertEquals(
            -12.345,
            formatter.parseLongitude(formatter.formatLongitudeSexagesimal(-12.345)),
            delta
        )
        assertEquals(-180.0, formatter.parseLongitude("-180\u00B0"), delta)
        assertEquals(180.0, formatter.parseLongitude("180\u00B0"), delta)
        assertTrue(formatter.parseLongitude("-181\u00B0").isNaN())
        assertTrue(formatter.parseLongitude("181\u00B0").isNaN())
    }
}
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

import android.location.Address
import android.location.Location
import com.github.BaseTests
import com.github.times.location.bing.BingGeocoder
import com.github.times.location.bing.BingResponse
import com.github.times.location.country.CountriesGeocoder
import com.github.times.location.geonames.GeoNamesGeocoder
import com.github.times.location.google.GoogleGeocoder
import com.github.times.location.test.R
import java.io.InputStream
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeocoderTestCase : BaseTests() {
    /**
     * Test Google address geocoder.
     */
    @Test
    fun testGoogleAddress() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = GoogleGeocoder(locale)
        val parser = geocoder.addressResponseParser
        assertNotNull(parser)
        val maxResults = 10
        var results: List<Address?>

        // Holon
        var input = context.resources.openRawResource(R.raw.google_holon)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(9, results.size)
        var address: Address? = results[0]
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(32.0234380, address!!.latitude, DELTA)
        assertEquals(34.7766799, address.longitude, DELTA)
        assertEquals(
            "1, Kalischer St, Holon, Center District, Israel",
            (address as ZmanimAddress).formatted
        )

        // Near Elad
        input = context.resources.openRawResource(R.raw.google_near_elad)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(6, results.size)
        address = results[0]
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(32.0626167, address.latitude, DELTA)
        assertEquals(34.9717498, address.longitude, DELTA)
        assertEquals(
            "Unnamed Road, Rosh Haayin, Petach Tikva, Center District, Israel",
            (address as ZmanimAddress).formatted
        )

        // Bar Yochai
        input = context.resources.openRawResource(R.raw.google_bar_yohai)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(9, results.size)
        address = results[0]
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(32.99505, address.latitude, DELTA)
        assertEquals(35.44968, address.longitude, DELTA)
        assertEquals(
            "331, Bar Yohai, Tzfat, North District, Israel",
            (address as ZmanimAddress).formatted
        )
        val addressProvider = AddressProvider(context)
        val location = Location(GeocoderBase.USER_PROVIDER)
        location.latitude = 32.99505
        location.longitude = 35.44968
        address = addressProvider.findBestAddress(location, results, GeocoderBase.SAME_PLATEAU)
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(results[0], address)
        address = addressProvider.findBestAddress(location, results, GeocoderBase.SAME_CITY)
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(results[0], address)

        // Near Jerusalem
        input = context.resources.openRawResource(R.raw.google_jerusalem)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(0, results.size)
    }

    /**
     * Test Google elevation geocoder.
     */
    @Test
    fun testGoogleElevation() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = GoogleGeocoder(locale)
        val parser = geocoder.elevationResponseParser
        assertNotNull(parser)

        // Access Denied
        val results: List<Location>
        val input: InputStream

        // Near Elad
        input = context.resources.openRawResource(R.raw.google_elevation_near_elad)
        assertNotNull(input)
        results = parser.parse(input, 0.0, 0.0, 1)
        assertNotNull(results)
        assertEquals(1, results.size)
        val location = results[0]
        assertNotNull(location)
        assertEquals(32.0629985, location.latitude, DELTA)
        assertEquals(34.9768113, location.longitude, DELTA)
        assertEquals(94.6400452, location.altitude, DELTA)
    }

    /**
     * Test GeoNames address geocoder.
     */
    @Test
    fun testGeoNamesAddress() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = GeoNamesGeocoder(locale)
        val parser = geocoder.addressResponseParser
        assertNotNull(parser)
        val maxResults = 10
        var results: List<Address>

        // Near Elad
        var input = context.resources.openRawResource(R.raw.geonames_near_elad)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(5, results.size)
        var address = results[4]
        assertNotNull(address)
        assertEquals(32.04984, address.latitude, DELTA)
        assertEquals(34.95382, address.longitude, DELTA)
        assertEquals("Israel", address.countryName)
        assertEquals("El‘ad", address.featureName)

        // Tel-Aviv
        input = context.resources.openRawResource(R.raw.geonames_telaviv)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(6, results.size)
        address = results[5]
        assertNotNull(address)
        assertEquals(32.06948, address.latitude, DELTA)
        assertEquals(34.7689, address.longitude, DELTA)
        assertEquals("Israel", address.countryName)
        assertEquals("Kerem HaTemanim", address.featureName)

        // Arctic Ocean
        input = context.resources.openRawResource(R.raw.geonames_arctic)
        assertNotNull(input)
        results = parser.parse(input, 89.89511, -36.3637, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(1, results.size)
        address = results[0]
        assertNotNull(address)
        assertEquals(89.89511, address.latitude, DELTA)
        assertEquals(-36.3637, address.longitude, DELTA)
        assertEquals("Arctic Ocean", address.featureName)

        // Empty
        input = context.resources.openRawResource(R.raw.geonames_empty)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(0, results.size)
    }

    /**
     * Test GeoNames elevation geocoder.
     */
    @Test
    fun testGeoNamesElevation() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = GeoNamesGeocoder(locale)
        val parser = geocoder.elevationResponseParser
        assertNotNull(parser)
        val results: List<Location>

        // Near Elad
        val input = context.resources.openRawResource(R.raw.geonames_elevation_near_elad)
        assertNotNull(input)
        results = parser.parse(input, 32.04984, 34.95382, 1)
        assertNotNull(results)
        assertEquals(1, results.size)
        val location = results[0]
        assertNotNull(location)
        assertEquals(32.04984, location.latitude, DELTA)
        assertEquals(34.95382, location.longitude, DELTA)
        assertEquals(30.0, location.altitude, DELTA)
    }

    /**
     * Test Bing POJO response class.
     */
    @Test
    fun testBingResponseClass() {
        val classResponse = BingResponse::class
        assertFalse(classResponse.isAbstract)
        assertFalse(classResponse.isData)
        assertTrue(classResponse.isFinal)
    }

    /**
     * Test Bing address geocoder.
     */
    @Test
    fun testBingAddress() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = BingGeocoder(locale)
        val parser = geocoder.addressResponseParser
        assertNotNull(parser)
        val maxResults = 10
        var results: List<Address>

        // Holon
        var input = context.resources.openRawResource(R.raw.bing_holon)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(5, results.size)
        var address = results[0]
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(32.0236, address.latitude, DELTA)
        assertEquals(34.776698, address.longitude, DELTA)
        assertEquals(
            "Shenkar Arye, Holon, Tel-Aviv, Tel Aviv, Israel",
            (address as ZmanimAddress).formatted
        )

        // Near Elad
        input = context.resources.openRawResource(R.raw.bing_near_elad)
        assertNotNull(input)
        results = parser.parse(input, maxResults, locale)
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(1, results.size)
        address = results[0]
        assertNotNull(address)
        assertTrue(address is ZmanimAddress)
        assertEquals(32.094619750976563, address.latitude, DELTA)
        assertEquals(34.885761260986328, address.longitude, DELTA)
        assertEquals(
            "Orlov Ze'Ev & Bar Kokhva, Petah Tikva, Merkaz, Israel",
            (address as ZmanimAddress).formatted
        )
    }

    /**
     * Test Bing elevation geocoder.
     */
    @Test
    fun testBingElevation() {
        assertNotNull(context)
        val locale = Locale.US
        val geocoder: GeocoderBase = BingGeocoder(locale)
        val parser = geocoder.elevationResponseParser
        assertNotNull(parser)
        val results: List<Location>

        // Holon
        val input = context.resources.openRawResource(R.raw.bing_elevation_holon)
        assertNotNull(input)
        results = parser.parse(input, 32.0236, 34.776698, 1)
        assertNotNull(results)
        assertEquals(1, results.size)
        val location = results[0]
        assertNotNull(location)
        assertEquals(32.0236, location.latitude, DELTA)
        assertEquals(34.776698, location.longitude, DELTA)
        assertEquals(35.0, location.altitude, DELTA)
    }

    /**
     * Test internal address geocoder.
     */
    @Test
    fun testInternalGeocoderAddress() {
        assertNotNull(context)
        val maxResults = 5

        // Bar Yochai
        val results = createInternalGeocoderAddresses()
        assertNotNull(results)
        assertTrue(maxResults >= results.size)
        assertEquals(5, results.size)
        val address = results[0]
        assertNotNull(address)
        assertFalse(address is ZmanimAddress)
        assertEquals(32.99505, address.latitude, DELTA)
        assertEquals(35.44968, address.longitude, DELTA)
        val zmanimAddress = ZmanimAddress(address)
        assertEquals(
            "331, Bar Yohai, Tzfat, North District, Israel",
            zmanimAddress.formatted
        )
    }

    private fun createInternalGeocoderAddresses(): List<Address> {
        val results = mutableListOf<Address>()
        var address: Address
        val locale = Locale.US
        address = Address(locale)
        address.setAddressLine(0, "331, Bar Yohai, Israel")
        address.adminArea = "North District"
        address.countryCode = "IL"
        address.countryName = "Israel"
        address.featureName = "331"
        address.latitude = 32.9959042
        address.locality = "Bar Yohai"
        address.longitude = 35.450468199999996
        address.premises = "331"
        address.subAdminArea = "Tzfat"
        results.add(address)
        address = Address(locale)
        address.setAddressLine(0, "86, Bar Yohai, Israel")
        address.adminArea = "North District"
        address.countryCode = "IL"
        address.countryName = "Israel"
        address.featureName = "86"
        address.latitude = 32.9964071
        address.locality = "Bar Yohai"
        address.longitude = 35.4495705
        address.premises = "86"
        address.subAdminArea = "Tzfat"
        results.add(address)
        address = Address(locale)
        address.setAddressLine(0, "Derech HaZayit, Bar Yohai, Israel")
        address.adminArea = "North District"
        address.countryCode = "IL"
        address.countryName = "Israel"
        address.featureName = "Derech HaZayit"
        address.latitude = 32.996314999999996
        address.locality = "Bar Yohai"
        address.longitude = 35.4487106
        address.subAdminArea = "Tzfat"
        address.thoroughfare = "Derech HaZayit"
        results.add(address)
        address = Address(locale)
        address.setAddressLine(0, "Bar Yohai, Israel")
        address.adminArea = "North District"
        address.countryCode = "IL"
        address.countryName = "Israel"
        address.featureName = "Bar Yohai"
        address.latitude = 32.997704
        address.locality = "Bar Yohai"
        address.longitude = 35.44819
        address.subAdminArea = "Tzfat"
        results.add(address)
        address = Address(locale)
        address.setAddressLine(0, "Merom HaGalil Regional Council, Israel")
        address.adminArea = "North District"
        address.countryCode = "IL"
        address.countryName = "Israel"
        address.featureName = "Merom HaGalil Regional Council"
        address.latitude = 32.9916546
        address.longitude = 35.467236799999995
        address.subAdminArea = "Merom HaGalil Regional Council"
        results.add(address)
        return results
    }

    /**
     * Test countries geocoder - Jerusalem, Israel.
     */
    @Test
    fun testCountries_Holiest() {
        assertNotNull(context)
        val geocoder = CountriesGeocoder(context)

        val country = geocoder.findCountry(HOLIEST_LATITUDE, HOLIEST_LONGITUDE)
        assertNotNull(country)
        assertEquals("IL", country!!.countryCode)
    }

    /**
     * Test countries geocoder - Delhi, India.
     */
    @Test
    fun testCountries_Delhi() {
        assertNotNull(context)
        val geocoder = CountriesGeocoder(context)

        val country = geocoder.findCountry(28.65195, 77.23149)
        assertNotNull(country)
        assertEquals("IN", country!!.countryCode)
    }

    /**
     * Test countries geocoder - SanJose, USA.
     */
    @Test
    fun testCountries_SanJose() {
        assertNotNull(context)
        val geocoder = CountriesGeocoder(context)

        val country = geocoder.findCountry(37.38754, -122.06)
        assertNotNull(country)
        assertEquals("US", country!!.countryCode)
    }

    /**
     * Test cities geocoder.
     */
    @Test
    fun testCities() {
        assertNotNull(context)
        val geocoder = CountriesGeocoder(context)

        val city = geocoder.findCity(HOLIEST_LATITUDE, HOLIEST_LONGITUDE)
        assertNotNull(city)
        assertEquals(HOLIEST_LATITUDE, city!!.latitude, 0.1)
        assertEquals(HOLIEST_LONGITUDE, city.longitude, 0.1)
        assertEquals("IL", city.countryCode)
        assertEquals("Asia/Jerusalem", city.timeZone.id)
    }

    companion object {
        private const val DELTA = 1e-3

        /**
         * Latitude of the Holy of Holies.
         */
        private const val HOLIEST_LATITUDE = 31.778

        /**
         * Longitude of the Holy of Holies.
         */
        private const val HOLIEST_LONGITUDE = 35.2353
    }
}
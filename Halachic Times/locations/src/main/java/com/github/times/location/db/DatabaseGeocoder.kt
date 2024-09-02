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
package com.github.times.location.db

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.provider.BaseColumns
import com.github.database.CursorFilter
import com.github.times.location.AddressResponseParser
import com.github.times.location.City
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import com.github.times.location.ZmanimAddress
import com.github.times.location.ZmanimLocation
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_LOCATION
import com.github.times.location.country.Country
import com.github.times.location.provider.LocationContract
import com.github.times.location.provider.LocationContract.AddressColumns
import com.github.times.location.provider.LocationContract.CityColumns
import com.github.times.location.provider.LocationContract.ElevationColumns
import com.github.times.location.provider.LocationContract.Elevations
import com.github.util.getDefaultLocale
import java.io.Closeable
import java.io.IOException
import java.util.Locale
import kotlin.math.min
import timber.log.Timber

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 *
 * The local database is supposed to reduce redundant network requests.
 *
 * @author Moshe Waisberg
 */
class DatabaseGeocoder(
    private val context: Context,
    locale: Locale = context.getDefaultLocale()
) : GeocoderBase(locale), Closeable {

    /**
     * Close database resources.
     */
    override fun close() = Unit

    @Throws(IOException::class)
    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address> {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        val filter = createDistanceFilter(latitude, longitude, SAME_PLATEAU)
        val q = queryAddresses(filter)
        if (q.size <= 1) {
            return q
        }
        val sorted = q.sortedWith(DistanceComparator(latitude, longitude))
        return sorted.subList(0, min(maxResults, sorted.size))
    }

    private fun createDistanceFilter(
        latitude: Double,
        longitude: Double,
        distanceMax: Float
    ): CursorFilter {
        return object : CursorFilter {
            private val distance = FloatArray(1)

            override fun accept(cursor: Cursor): Boolean {
                val locationLatitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LATITUDE)
                val locationLongitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LONGITUDE)
                Location.distanceBetween(
                    latitude,
                    longitude,
                    locationLatitude,
                    locationLongitude,
                    distance
                )
                if (distance[0] <= distanceMax) {
                    return true
                }
                val addressLatitude = cursor.getDouble(INDEX_ADDRESS_LATITUDE)
                val addressLongitude = cursor.getDouble(INDEX_ADDRESS_LONGITUDE)
                Location.distanceBetween(
                    latitude,
                    longitude,
                    addressLatitude,
                    addressLongitude,
                    distance
                )
                return distance[0] <= distanceMax
            }
        }
    }

    override fun createAddressResponseParser(): AddressResponseParser {
        throw LocationException()
    }

    @Throws(IOException::class)
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        val filter: CursorFilter = object : CursorFilter {
            private val distance = FloatArray(1)

            override fun accept(cursor: Cursor): Boolean {
                val locationLatitude = cursor.getDouble(INDEX_ELEVATION_LATITUDE)
                val locationLongitude = cursor.getDouble(INDEX_ELEVATION_LONGITUDE)
                Location.distanceBetween(
                    latitude,
                    longitude,
                    locationLatitude,
                    locationLongitude,
                    distance
                )
                return distance[0] <= SAME_PLATEAU
            }
        }
        val locations = queryElevations(filter)
        val locationsCount = locations.size
        if (locationsCount == 0) return null
        var distance: Float
        val distanceLoc = FloatArray(1)
        var d: Double
        var distancesSum = 0.0
        var n = 0
        val distances = DoubleArray(locationsCount)
        val elevations = DoubleArray(locationsCount)
        for (location in locations) {
            Location.distanceBetween(
                latitude,
                longitude,
                location.latitude,
                location.longitude,
                distanceLoc
            )
            distance = distanceLoc[0]
            elevations[n] = location.altitude
            d = (distance * distance).toDouble()
            distances[n] = d
            distancesSum += d
            n++
        }
        if (n == 1 && distanceLoc[0] <= SAME_CITY) {
            return locations[0]
        }
        if (n <= 1) {
            return null
        }
        var weightSum = 0.0
        for (i in 0 until n) {
            weightSum += (1 - distances[i] / distancesSum) * elevations[i]
        }
        return ZmanimLocation(DB_PROVIDER).apply {
            this.time = System.currentTimeMillis()
            this.latitude = latitude
            this.longitude = longitude
            this.altitude = weightSum / (n - 1)
            this.id = -1
        }
    }

    @Throws(LocationException::class)
    override fun createElevationResponseParser(): ElevationResponseParser {
        throw LocationException()
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter a cursor filter.
     * @return the list of addresses.
     */
    fun queryAddresses(filter: CursorFilter?): List<ZmanimAddress> {
        val context: Context = context
        val language = locale.language
        val country = locale.country
        val addresses = mutableListOf<ZmanimAddress>()
        val selection = "(${AddressColumns.LANGUAGE} IS NULL) OR (${AddressColumns.LANGUAGE}=?)"
        val selectionArgs = arrayOf(language)
        val cursor = context.contentResolver.query(
            LocationContract.Addresses.CONTENT_URI(context),
            PROJECTION_ADDRESS,
            selection,
            selectionArgs,
            null
        )
        if (cursor == null || cursor.isClosed) {
            return addresses
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (filter != null && !filter.accept(cursor)) {
                        continue
                    }
                    val locationLanguage = cursor.getString(INDEX_ADDRESS_LANGUAGE)
                    val locale = if (locationLanguage.isNullOrEmpty()) {
                        this.locale
                    } else {
                        Locale(locationLanguage, country)
                    }
                    val location = Location(USER_PROVIDER).apply {
                        latitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LATITUDE)
                        longitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LONGITUDE)
                    }
                    val address = ZmanimAddress(locale).apply {
                        id = cursor.getLong(INDEX_ADDRESS_ID)
                        latitude = cursor.getDouble(INDEX_ADDRESS_LATITUDE)
                        longitude = cursor.getDouble(INDEX_ADDRESS_LONGITUDE)
                        isFavorite = cursor.getInt(INDEX_ADDRESS_FAVORITE) != 0
                        setFormatted(cursor.getString(INDEX_ADDRESS_ADDRESS))
                        extras = Bundle().apply {
                            putParcelable(EXTRA_LOCATION, location)
                        }
                    }
                    addresses.add(address)
                } while (cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            Timber.e(e, "Query addresses: %s", e.message)
        } finally {
            cursor.close()
        }
        return addresses
    }

    /**
     * Insert the address into the local database.
     *
     * @param location the location.
     * @param address  the address.
     */
    fun insertAddress(location: Location?, address: ZmanimAddress?) {
        if (address == null) return
        var id = address.id
        if (id != 0L) {
            return
        }
        // Cities have their own table.
        if (address is City) {
            insertOrUpdateCity(address)
            return
        }
        // Nothing to save.
        if (address is Country) {
            return
        }
        val latitude: Double
        val longitude: Double
        if (location == null) {
            latitude = address.latitude
            longitude = address.longitude
        } else {
            latitude = location.latitude
            longitude = location.longitude
        }
        val addresses = getFromLocation(latitude, longitude, 10)
        val nearest = findNearestAddress(latitude, longitude, addresses, SAME_STREET)
        if (nearest != null) {
            return
        }
        val values = ContentValues().apply {
            put(AddressColumns.LOCATION_LATITUDE, latitude)
            put(AddressColumns.LOCATION_LONGITUDE, longitude)
            put(AddressColumns.ADDRESS, formatAddress(address).toString())
            put(AddressColumns.LANGUAGE, address.locale.language)
            put(AddressColumns.LATITUDE, address.latitude)
            put(AddressColumns.LONGITUDE, address.longitude)
            put(AddressColumns.TIMESTAMP, System.currentTimeMillis())
            put(AddressColumns.FAVORITE, address.isFavorite)
        }

        val context: Context = context
        val resolver = context.contentResolver
        try {
            val uri = resolver.insert(LocationContract.Addresses.CONTENT_URI(context), values)
            if (uri != null) {
                id = ContentUris.parseId(uri)
                // Insert succeeded?
                if (id > 0L) {
                    address.id = id
                }
            }
        } catch (e: Exception) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/address
            Timber.e(e, "Error inserting address at $latitude,$longitude: ${e.message}")
        }
    }

    /**
     * Delete the list of cached addresses.
     */
    fun deleteAddresses() {
        val context: Context = context
        context.contentResolver.delete(LocationContract.Addresses.CONTENT_URI(context), null, null)
    }

    /**
     * Fetch elevations from the database.
     *
     * @param filter a cursor filter.
     * @return the list of locations with elevations.
     */
    fun queryElevations(filter: CursorFilter?): List<ZmanimLocation> {
        val locations = mutableListOf<ZmanimLocation>()
        val context: Context = context
        val cursor = context.contentResolver.query(
            Elevations.CONTENT_URI(context), PROJECTION_ELEVATION, null, null, null
        )
        if (cursor == null || cursor.isClosed) {
            return locations
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (filter != null && !filter.accept(cursor)) {
                        continue
                    }
                    val location = ZmanimLocation(DB_PROVIDER)
                    location.id = cursor.getLong(INDEX_ELEVATION_ID)
                    location.latitude = cursor.getDouble(INDEX_ELEVATION_LATITUDE)
                    location.longitude = cursor.getDouble(INDEX_ELEVATION_LONGITUDE)
                    location.altitude = cursor.getDouble(INDEX_ELEVATION_ELEVATION)
                    location.time = cursor.getLong(INDEX_ELEVATION_TIMESTAMP)
                    locations.add(location)
                } while (cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            Timber.e(e, "Query elevations: %s", e.message)
        } finally {
            cursor.close()
        }
        return locations
    }

    /**
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location the location.
     */
    fun insertOrUpdateElevation(location: ZmanimLocation?) {
        if (location == null || !location.hasAltitude()) return
        var id = location.id
        if (id < 0L) return
        val values = ContentValues().apply {
            put(ElevationColumns.LATITUDE, location.latitude)
            put(ElevationColumns.LONGITUDE, location.longitude)
            put(ElevationColumns.ELEVATION, location.altitude)
            put(ElevationColumns.TIMESTAMP, System.currentTimeMillis())
        }
        val context: Context = context
        val resolver = context.contentResolver
        val contentUri = Elevations.CONTENT_URI(context)
        try {
            if (id == 0L) {
                val uri = resolver.insert(contentUri, values)
                if (uri != null) {
                    id = ContentUris.parseId(uri)
                    if (id > 0L) {
                        location.id = id
                    }
                }
            } else {
                val uri = ContentUris.withAppendedId(contentUri, id)
                resolver.update(uri, values, null, null)
            }
        } catch (e: Exception) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/elevation
            Timber.e(
                e,
                "Error inserting elevation at " + location.latitude + "," + location.longitude + ": " + e.message
            )
        }
    }

    /**
     * Delete the list of cached elevations.
     */
    fun deleteElevations() {
        val context: Context = context
        context.contentResolver.delete(Elevations.CONTENT_URI(context), null, null)
    }

    /**
     * Fetch cities from the database.
     *
     * @param filter a cursor filter.
     * @return the list of cities.
     */
    fun queryCities(filter: CursorFilter?): List<City> {
        val context: Context = context
        val cities = mutableListOf<City>()
        val cursor = context.contentResolver.query(
            LocationContract.Cities.CONTENT_URI(context), PROJECTION_CITY, null, null, null
        )
        if (cursor == null || cursor.isClosed) {
            return cities
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (filter != null && !filter.accept(cursor)) {
                        continue
                    }
                    val city = City(locale)
                    city.id = cursor.getLong(INDEX_CITY_ID)
                    city.isFavorite = cursor.getInt(INDEX_CITY_FAVORITE) != 0
                    cities.add(city)
                } while (cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            Timber.e(e, "Query cities: %s", e.message)
        } finally {
            cursor.close()
        }
        return cities
    }

    /**
     * Insert or update the city in the local database.
     *
     * @param city the city.
     */
    fun insertOrUpdateCity(city: City?) {
        if (city == null) return
        val values = ContentValues().apply {
            put(CityColumns.TIMESTAMP, System.currentTimeMillis())
            put(CityColumns.FAVORITE, city.isFavorite)
        }
        val context: Context = context
        val resolver = context.contentResolver
        var id = city.id
        try {
            if (id == 0L) {
                values.put(BaseColumns._ID, City.generateCityId(city))
                val uri = resolver.insert(LocationContract.Cities.CONTENT_URI(context), values)
                if (uri != null) {
                    id = ContentUris.parseId(uri)
                    if (id > 0L) {
                        city.id = id
                    }
                }
            } else {
                val uri = ContentUris.withAppendedId(
                    LocationContract.Cities.CONTENT_URI(context), id
                )
                resolver.update(uri, values, null, null)
            }
        } catch (e: Exception) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/city
            Timber.e(e, "Error inserting city for " + city.formatted + ": " + e.message)
        }
    }

    /**
     * Delete the list of cached cities and re-populate.
     */
    fun deleteCities() {
        val context: Context = context
        context.contentResolver.delete(LocationContract.Cities.CONTENT_URI(context), null, null)
    }

    /**
     * Delete the address from the local database.
     *
     * @param address the address.
     */
    fun deleteAddress(address: ZmanimAddress?): Boolean {
        if (address == null) return false
        val context: Context = context
        val id = address.id
        if (id < 0L) {
            return false
        }
        // Cities have their own table.
        if (address is City) {
            return false
        }
        // Nothing to delete.
        if (address is Country) {
            return false
        }
        val resolver = context.contentResolver
        try {
            val uri = ContentUris.withAppendedId(
                LocationContract.Addresses.CONTENT_URI(context), id
            )
            return resolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/address
            Timber.e(
                e,
                "Error deleting address " + address.id + " at " + address.latitude + "," + address.longitude + ": " + e.message
            )
        }
        return false
    }

    private inner class DistanceComparator(
        private val latitude: Double,
        private val longitude: Double
    ) : Comparator<ZmanimAddress> {
        private val distance = FloatArray(1)

        override fun compare(a1: ZmanimAddress, a2: ZmanimAddress): Int {
            Location.distanceBetween(latitude, longitude, a1.latitude, a1.longitude, distance)
            val d1 = distance[0]
            Location.distanceBetween(latitude, longitude, a2.latitude, a2.longitude, distance)
            val d2 = distance[0]
            // Closer distance at top of the list.
            return d1.compareTo(d2)
        }
    }

    /**
     * Format the address.
     *
     * @param a the address.
     * @return the formatted address name.
     */
    private fun formatAddress(a: ZmanimAddress): CharSequence {
        return a.formatted
    }

    companion object {
        /**
         * Database
         */
        private const val DB_PROVIDER = "db"

        private val PROJECTION_ADDRESS = arrayOf(
            BaseColumns._ID,
            AddressColumns.LOCATION_LATITUDE,
            AddressColumns.LOCATION_LONGITUDE,
            AddressColumns.LATITUDE,
            AddressColumns.LONGITUDE,
            AddressColumns.ADDRESS,
            AddressColumns.LANGUAGE,
            AddressColumns.FAVORITE
        )
        private const val INDEX_ADDRESS_ID = 0
        private const val INDEX_ADDRESS_LOCATION_LATITUDE = 1
        private const val INDEX_ADDRESS_LOCATION_LONGITUDE = 2
        private const val INDEX_ADDRESS_LATITUDE = 3
        private const val INDEX_ADDRESS_LONGITUDE = 4
        private const val INDEX_ADDRESS_ADDRESS = 5
        private const val INDEX_ADDRESS_LANGUAGE = 6
        private const val INDEX_ADDRESS_FAVORITE = 7

        private val PROJECTION_ELEVATION = arrayOf(
            BaseColumns._ID,
            ElevationColumns.LATITUDE,
            ElevationColumns.LONGITUDE,
            ElevationColumns.ELEVATION,
            ElevationColumns.TIMESTAMP
        )
        private const val INDEX_ELEVATION_ID = 0
        private const val INDEX_ELEVATION_LATITUDE = 1
        private const val INDEX_ELEVATION_LONGITUDE = 2
        private const val INDEX_ELEVATION_ELEVATION = 3
        private const val INDEX_ELEVATION_TIMESTAMP = 4

        private val PROJECTION_CITY = arrayOf(
            BaseColumns._ID,
            CityColumns.TIMESTAMP,
            CityColumns.FAVORITE
        )
        private const val INDEX_CITY_ID = 0
        private const val INDEX_CITY_TIMESTAMP = 1
        private const val INDEX_CITY_FAVORITE = 2
    }
}
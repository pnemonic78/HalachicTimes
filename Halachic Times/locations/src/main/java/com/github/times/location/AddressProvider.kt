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

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.Location.distanceBetween
import com.github.database.CursorFilter
import com.github.lang.isTrue
import com.github.times.location.City.Companion.generateCityId
import com.github.times.location.GeocoderBase.Companion.SAME_CITY
import com.github.times.location.GeocoderBase.Companion.SAME_PLANET
import com.github.times.location.GeocoderBase.Companion.SAME_PLATEAU
import com.github.times.location.GeocoderBase.Companion.SAME_STREET
import com.github.times.location.ZmanimAddress.Companion.compare
import com.github.times.location.ZmanimLocation.Companion.compare
import com.github.times.location.bing.BingGeocoder
import com.github.times.location.country.CountriesGeocoder
import com.github.times.location.db.DatabaseGeocoder
import com.github.times.location.geonames.GeoNamesGeocoder
import com.github.times.location.google.GoogleGeocoder
import com.github.util.getDefaultLocale
import java.util.Locale
import timber.log.Timber

/**
 * Address provider.<br></br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 *
 * @author Moshe Waisberg
 */
class AddressProvider @JvmOverloads constructor(
    private val context: Context,
    private val locale: Locale = context.getDefaultLocale()
) {
    interface OnFindAddressListener {
        /**
         * Called when an address is found.
         *
         * @param provider the address provider.
         * @param location the requested location.
         * @param address  the found address.
         */
        fun onFindAddress(provider: AddressProvider, location: Location, address: Address)

        /**
         * Called when a location with an elevation is found.
         *
         * @param provider the address provider.
         * @param location the requested location.
         * @param elevated the location with elevation.
         */
        fun onFindElevation(provider: AddressProvider, location: Location, elevated: Location)
    }

    /**
     * The list of countries.
     */
    private val geocoderCountries: CountriesGeocoder = CountriesGeocoder(context, locale)
    private val geocoderDatabase: DatabaseGeocoder = DatabaseGeocoder(context, locale)
    private val geocoder by lazy { Geocoder(context) }
    private val geocoderGoogle by lazy { GoogleGeocoder(locale) }
    private val geocoderBing by lazy { BingGeocoder(locale) }
    private val geocoderGeonames by lazy { GeoNamesGeocoder(locale) }
    private val isOnline = BuildConfig.INTERNET

    /**
     * Find the nearest address of the location.
     *
     * @param location the location.
     * @param listener the listener.
     * @return the address - `null` otherwise.
     */
    fun findNearestAddress(location: Location?, listener: OnFindAddressListener?): Address? {
        Timber.v("findNearestAddress %s", location)
        if (location == null) return null
        if (listener == null) return null

        val latitude = location.latitude
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX) {
            return null
        }
        val longitude = location.longitude
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX) {
            return null
        }
        val forceFetch = location.extras?.getBoolean(PARAMETER_FORCE, FORCE_DEFAULT).isTrue

        var addresses: List<Address>
        var best: Address?
        var bestStreet: Address? = null

        // Find a country.
        addresses = findNearestCountries(location)
        best = findBestAddress(location, addresses, SAME_PLANET)
        if (best != null) {
            notifyAddressFound(listener, location, best)
        }
        var bestCountry = best

        // Find a city.
        addresses = findNearestCity(location)
        best = findBestAddress(location, addresses, SAME_CITY)
        if (best != null) {
            notifyAddressFound(listener, location, best)
        }
        var bestCity = best

        // Find the best cached location.
        addresses = findNearestAddress(location, geocoderDatabase)
        best = findBestAddress(location, addresses, SAME_PLATEAU)
        if (best != null) {
            notifyAddressFound(listener, location, best)
        }
        var bestPlateau = best
        best = findBestAddress(location, addresses, SAME_CITY)
        if (best != null) {
            bestCity = best
            notifyAddressFound(listener, location, best)
        }
        best = findBestAddress(location, addresses, SAME_STREET)
        if (best != null) {
            bestStreet = best
            notifyAddressFound(listener, location, best)
        }

        // Find the best address from some Geocoder provider.
        if ((bestStreet == null || forceFetch) && isOnline) {
            addresses = findNearestAddressGeocoder(location)
            if (bestCountry == null) {
                bestCountry = findBestAddress(location, addresses, SAME_PLANET)
            }
            best = findBestAddress(location, addresses, SAME_PLATEAU)
            if (best != null && compare(best, bestPlateau) != 0) {
                bestPlateau = best
                notifyAddressFound(listener, location, best)
            }
            best = findBestAddress(location, addresses, SAME_CITY)
            if (best != null && compare(best, bestCity) != 0) {
                bestCity = best
                notifyAddressFound(listener, location, best)
            }
            best = findBestAddress(location, addresses, SAME_STREET)
            if (best != null && compare(best, bestStreet) != 0) {
                bestStreet = best
                notifyAddressFound(listener, location, best)
            }
        }

        // Find the best address from remote Geocoder providers.
        if ((bestStreet == null || forceFetch) && isOnline) {
            var foundBest = false
            for (geocoder in remoteAddressProviders) {
                addresses = findNearestAddress(location, geocoder)
                best = findBestAddress(location, addresses, SAME_PLATEAU)
                if (best != null && compare(best, bestPlateau) != 0) {
                    bestPlateau = best
                    notifyAddressFound(listener, location, best)
                }
                best = findBestAddress(location, addresses, SAME_CITY)
                if (best != null) {
                    if (compare(best, bestCity) != 0) {
                        bestCity = best
                        notifyAddressFound(listener, location, best)
                    }
                    foundBest = true
                }
                best = findBestAddress(location, addresses, SAME_STREET)
                if (best != null) {
                    if (compare(best, bestStreet) != 0) {
                        bestStreet = best
                        notifyAddressFound(listener, location, best)
                    }
                    foundBest = true
                }
                if (foundBest) {
                    break
                }
            }
        }

        return bestStreet ?: bestCity ?: bestPlateau ?: bestCountry
    }

    private fun notifyAddressFound(
        listener: OnFindAddressListener,
        location: Location,
        address: Address
    ) {
        listener.onFindAddress(this, location, address)
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     *
     * Uses the built-in Android [Geocoder] API.
     *
     * @param location the location.
     * @return the list of addresses.
     */
    private fun findNearestAddressGeocoder(location: Location): List<Address> {
        val latitude = location.latitude
        val longitude = location.longitude
        try {
            return geocoder.getFromLocation(latitude, longitude, maxResults) ?: emptyList()
        } catch (ignore: Exception) {
        }
        return emptyList()
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     *
     * Uses the built-in Android [Geocoder] API.
     *
     * @param location the location.
     * @param geocoder the geocoder.
     * @return the list of addresses.
     */
    private fun findNearestAddress(location: Location, geocoder: GeocoderBase): List<Address> {
        val latitude = location.latitude
        val longitude = location.longitude
        try {
            return geocoder.getFromLocation(latitude, longitude, maxResults)
        } catch (e: Exception) {
            Timber.e(
                e,
                "Address geocoder: $geocoder error: ${e.message} at $latitude,$longitude"
            )
        }
        return emptyList()
    }

    /**
     * Get the list of remote geocoder providers for addresses.
     *
     * @return the list of providers.
     */
    private val remoteAddressProviders: List<GeocoderBase> = listOf(
        geocoderGoogle,
        geocoderBing,
        geocoderGeonames
    )

    /**
     * Find the nearest address, and checking relevant fields.
     *
     * @param location  the location.
     * @param addresses the list of addresses.
     * @param radius    the maximum radius.
     * @return the best address - `null` otherwise.
     */
    internal fun findBestAddress(
        location: Location,
        addresses: List<Address>,
        radius: Float
    ): Address? {
        if (addresses.isEmpty()) {
            return null
        }

        // First, find the closest location.
        val latitude = location.latitude
        val longitude = location.longitude
        var distanceMin = radius
        var candidate: Address? = null
        val distances = FloatArray(1)
        val near = mutableListOf<Address>()
        for (a in addresses) {
            if (!a.hasLatitude() || !a.hasLongitude()) continue
            distanceBetween(latitude, longitude, a.latitude, a.longitude, distances)
            if (distances[0] <= radius) {
                near.add(a)
                if (distances[0] <= distanceMin) {
                    distanceMin = distances[0]
                    candidate = a
                }
            }
        }
        if (candidate != null) return candidate
        if (near.isEmpty()) return null
        if (near.size == 1) return near[0]

        // Next, find the best address part.
        for (a in near) {
            if (a.featureName != null) return a
            if (a.locality != null) return a
            if (a.subLocality != null) return a
            if (a.adminArea != null) return a
            if (a.subAdminArea != null) return a
            if (a.countryName != null) return a
        }
        return near[0]
    }

    /**
     * Insert or update the address in the local database.
     *
     * @param location the location.
     * @param address  the address.
     */
    fun insertOrUpdateAddress(location: Location?, address: ZmanimAddress) {
        geocoderDatabase.insertAddress(location, address)
    }

    /**
     * Close resources.
     */
    fun close() {
        geocoderDatabase.close()
    }

    /**
     * Find the nearest countries to the latitude and longitude.
     *
     * Uses the pre-compiled array of countries from GeoNames.
     *
     * @param location the location.
     * @return the list of addresses with at most 1 entry.
     */
    private fun findNearestCountries(location: Location): List<Address> {
        val country: Address? = geocoderCountries.findCountry(location)
        if (country != null) {
            return listOf(country)
        }
        return emptyList()
    }

    /**
     * Find the nearest city to the latitude and longitude.
     *
     *
     * Uses the pre-compiled array of cities from GeoNames.
     *
     * @param location the location.
     * @return the list of addresses with at most 1 entry.
     */
    private fun findNearestCity(location: Location): List<Address> {
        val latitude = location.latitude
        val longitude = location.longitude
        val geocoder: GeocoderBase = geocoderCountries
        try {
            return geocoder.getFromLocation(latitude, longitude, maxResults)
        } catch (e: Exception) {
            Timber.e(e, "City: ${e.message} at $latitude,$longitude")
        }
        return emptyList()
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter a cursor filter.
     * @return the list of addresses.
     */
    fun queryAddresses(filter: CursorFilter?): List<ZmanimAddress> {
        return geocoderDatabase.queryAddresses(filter)
    }

    /**
     * Find the elevation (altitude).
     *
     * @param location the location.
     * @param listener the listener.
     * @return the elevated location - `null` otherwise.
     */
    fun findElevation(location: Location?, listener: OnFindAddressListener?): Location? {
        if (location == null) {
            return null
        }
        val latitude = location.latitude
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX) {
            return null
        }
        val longitude = location.longitude
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX) {
            return null
        }
        var elevated: Location?
        if (location.hasAltitude()) {
            elevated = findElevationDatabase(location)
            if (elevated == null) {
                elevated = findElevationCities(location)
                if (elevated == null) {
                    elevated = ZmanimLocation(location)
                } else if (compare(location, elevated) == 0) {
                    elevated.altitude = location.altitude
                }
            } else if (compare(location, elevated) == 0) {
                elevated.altitude = location.altitude
            }
            listener?.onFindElevation(this, location, elevated)
            return elevated
        }
        elevated = findElevationDatabase(location)
        if (elevated != null && elevated.hasAltitude()) {
            listener?.onFindElevation(this, location, elevated)
            return elevated
        }
        elevated = findElevationCities(location)
        if (elevated != null && elevated.hasAltitude()) {
            listener?.onFindElevation(this, location, elevated)
            return elevated
        }
        if (isOnline) {
            for (geocoder in remoteElevationProviders) {
                elevated = try {
                    geocoder.getElevation(latitude, longitude)
                } catch (e: Exception) {
                    Timber.e(
                        e,
                        "Elevation geocoder: $geocoder, error: ${e.message} at $latitude,$longitude"
                    )
                    continue
                }
                if (elevated != null && elevated.hasAltitude()) {
                    listener?.onFindElevation(this, location, elevated)
                    return elevated
                }
            }
        }
        return null
    }

    /**
     * Find elevation of nearest cities. Calculates the average elevation of
     * neighbouring cities if more than `1` is found.
     *
     * @param location the location.
     * @return the elevated location - `null` otherwise.
     */
    private fun findElevationCities(location: Location): Location? {
        val latitude = location.latitude
        val longitude = location.longitude
        try {
            return geocoderCountries.getElevation(latitude, longitude)
        } catch (e: Exception) {
            Timber.e(e, "Countries geocoder: ${e.message} at $latitude,$longitude")
        }
        return null
    }

    /**
     * Get the list of remote geocoder providers for elevations.
     *
     * @return the list of providers.
     */
    private val remoteElevationProviders: List<GeocoderBase> = listOf(
        geocoderBing,
        geocoderGeonames,
        geocoderGoogle
    )

    /**
     * Find elevation of nearest locations cached in the database.
     * Calculates the average elevation of neighbouring locations if more than `1` is found.
     *
     * @param location the location.
     * @return the elevated location - `null` otherwise.
     */
    private fun findElevationDatabase(location: Location): Location? {
        val latitude = location.latitude
        val longitude = location.longitude
        val geocoder: GeocoderBase = geocoderDatabase
        try {
            return geocoder.getElevation(latitude, longitude)
        } catch (e: Exception) {
            Timber.e(e, "Database geocoder: ${e.message} at $latitude,$longitude")
        }
        return null
    }

    /**
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location the location.
     */
    fun insertOrUpdateElevation(location: ZmanimLocation) {
        geocoderDatabase.insertOrUpdateElevation(location)
    }

    /**
     * Populate the cities with data from the table.
     *
     * @param cities the list of cities to populate.
     */
    private fun populateCities(cities: Collection<City>) {
        val citiesById = mutableMapOf<Long, City>()
        var id: Long
        for (city in cities) {
            id = city.id
            if (id == 0L) {
                id = generateCityId(city)
            }
            citiesById[id] = city
        }
        val citiesDb = geocoderDatabase.queryCities(null)
        var city: City?
        for (cityDb in citiesDb) {
            id = cityDb.id
            city = citiesById[id] ?: continue
            city.id = id
            city.isFavorite = cityDb.isFavorite
        }
    }

    /**
     * Delete the list of cached addresses.
     */
    fun deleteAddresses() {
        geocoderDatabase.deleteAddresses()
    }

    /**
     * Delete the list of cached elevations.
     */
    fun deleteElevations() {
        geocoderDatabase.deleteElevations()
    }

    /**
     * Get the list of internal cities.
     *
     * @return the list of cities.
     */
    val cities: List<City>
        get() {
            val cities = geocoderCountries.cities
            populateCities(cities)
            return cities
        }

    /**
     * Delete the list of cached cities and re-populate.
     */
    fun deleteCities() {
        geocoderDatabase.deleteCities()
    }

    fun deleteAddress(address: ZmanimAddress): Boolean {
        return geocoderDatabase.deleteAddress(address)
    }

    companion object {
        private const val LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN
        private const val LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX
        private const val LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN
        private const val LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX

        private const val FORCE_DEFAULT = false

        private const val PARAMETER_FORCE = ZmanimLocationListener.EXTRA_FORCE

        private const val maxResults = 10
    }
}
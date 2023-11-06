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
package com.github.times.location.country

import android.content.Context
import android.location.Address
import android.location.Location
import android.text.format.DateUtils
import com.github.lang.isTrue
import com.github.times.location.AddressResponseParser
import com.github.times.location.City
import com.github.times.location.City.Companion.generateCityId
import com.github.times.location.ElevationResponseParser
import com.github.times.location.GeocoderBase
import com.github.times.location.LocationException
import com.github.times.location.R
import com.github.times.location.ZmanimLocation
import com.github.times.location.country.Country.Companion.generateCountryId
import com.github.util.LocaleUtils.getDefaultLocale
import java.io.IOException
import java.util.Locale
import java.util.TimeZone
import kotlin.math.round

/**
 * Maintains the lists of countries.
 *
 * @author Moshe Waisberg
 */
class CountriesGeocoder @JvmOverloads constructor(
    context: Context,
    locale: Locale = getDefaultLocale(context)
) : GeocoderBase(locale) {

    private lateinit var countryBorders: Array<CountryPolygon>
    private lateinit var citiesNames: Array<String>
    private lateinit var citiesCountries: Array<String>
    private lateinit var citiesLatitudes: DoubleArray
    private lateinit var citiesLongitudes: DoubleArray
    private lateinit var citiesElevations: DoubleArray
    private lateinit var citiesTimeZones: Array<TimeZone>

    init {
        initCountries(context)
        initCities(context)
    }

    // Populate arrays from XMLs
    private fun initCountries(context: Context) {
        var borders = immutableCountryBorders
        if (borders == null) {
            val res = context.resources
            val countryCodes = res.getStringArray(R.array.countries)
            val latitudes = res.getIntArray(R.array.latitudes)
            val longitudes = res.getIntArray(R.array.longitudes)
            val verticesCounts = res.getIntArray(R.array.vertices_count)

            val countriesCount = countryCodes.size
            var i = 0
            borders = Array(countriesCount) { c ->
                val verticesCount = verticesCounts[c]
                val country = CountryPolygon(countryCodes[c])
                for (v in 0 until verticesCount) {
                    country.addPoint(latitudes[i], longitudes[i])
                    i++
                }
                country
            }
            immutableCountryBorders = borders
        }
        countryBorders = borders
    }

    // Populate arrays from XMLs
    private fun initCities(context: Context) {
        val res = context.resources

        var countries = immutableCitiesCountries
        var citiesCount = countries?.size ?: 0
        if (countries == null) {
            countries = res.getStringArray(R.array.cities_countries)
            citiesCount = countries.size
            immutableCitiesCountries = countries
        }
        citiesCountries = countries

        var latitudes = immutableCitiesLatitudes
        if (latitudes == null) {
            val latitudesRes = res.getIntArray(R.array.cities_latitudes)
            latitudes = DoubleArray(citiesCount) { latitudesRes[it] / RATIO }
            immutableCitiesLatitudes = latitudes
        }
        citiesLatitudes = latitudes

        var longitudes = immutableCitiesLongitudes
        if (longitudes == null) {
            val longitudesRes = res.getIntArray(R.array.cities_longitudes)
            longitudes = DoubleArray(citiesCount) { longitudesRes[it] / RATIO }
            immutableCitiesLongitudes = longitudes
        }
        citiesLongitudes = longitudes

        var elevations = immutableCitiesElevations
        if (elevations == null) {
            val elevationsRes = res.getIntArray(R.array.cities_elevations)
            elevations = DoubleArray(citiesCount) { elevationsRes[it].toDouble() }
            immutableCitiesElevations = elevations
        }
        citiesElevations = elevations

        var timeZones = immutableCitiesTimeZones
        if (timeZones == null) {
            val timeZonesRes = res.getStringArray(R.array.cities_time_zones)
            timeZones = Array(citiesCount) { TimeZone.getTimeZone(timeZonesRes[it]) }
            immutableCitiesTimeZones = timeZones
        }
        citiesTimeZones = timeZones

        citiesNames = res.getStringArray(R.array.cities)
    }

    /**
     * Find the nearest city to the location.
     *
     * @param location the location.
     * @return the country - `null` otherwise.
     */
    fun findCountry(location: Location): Country? {
        return findCountry(location.latitude, location.longitude)
    }

    /**
     * Find the nearest country for the location.
     *
     * @param latitude the latitude.
     * @param longitude the longitude.
     * @return the country - `null` otherwise.
     */
    fun findCountry(latitude: Double, longitude: Double): Country? {
        var countryIndex = findCountryIndex(latitude, longitude)
        if (countryIndex < 0) {
            return null
        }
        countryIndex = countryIndex.coerceAtMost(countryBorders.size - 1)
        val locale = Locale(language, countryBorders[countryIndex].countryCode)
        return Country(locale).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.countryCode = locale.country
            this.countryName = locale.getDisplayCountry(locale)
            this.id = generateCountryId(this)
        }
    }

    /**
     * Find the nearest country index for the location.
     *
     * @param latitude the latitude.
     * @param longitude the longitude.
     * @return the country index - `-1` otherwise.
     */
    fun findCountryIndex(latitude: Double, longitude: Double): Int {
        val borders = countryBorders
        if (borders.isEmpty()) {
            return -1
        }
        var distanceToBorder: Double
        var distanceMin = Double.MAX_VALUE
        var found = -1
        val countriesSize = borders.size
        var country: CountryPolygon?
        val matches = IntArray(MAX_COUNTRIES_OVERLAP)
        var matchesCount = 0
        val fixedPointLatitude = round(latitude * RATIO).toInt()
        val fixedPointLongitude = round(longitude * RATIO).toInt()
        var c = 0
        while (c < countriesSize && matchesCount < MAX_COUNTRIES_OVERLAP) {
            country = borders[c]
            if (country.containsBox(fixedPointLatitude, fixedPointLongitude)) {
                matches[matchesCount++] = c
            }
            c++
        }
        if (matchesCount == 0) {
            // Find the nearest border.
            for (i in 0 until countriesSize) {
                country = borders[i]
                distanceToBorder =
                    country.minimumDistanceToBorders(fixedPointLatitude, fixedPointLongitude)
                if (distanceToBorder < distanceMin) {
                    distanceMin = distanceToBorder
                    found = i
                }
            }
        } else if (matchesCount == 1) {
            found = matches[0]
        } else {
            // Case 1: Smaller country inside a larger country.
            var other: CountryPolygon?
            country = borders[matches[0]]
            var matchCountryIndex: Int
            for (m in 1 until matchesCount) {
                matchCountryIndex = matches[m]
                other = borders[matchCountryIndex]
                if (country != null) {
                    if (country.containsBox(other).isTrue) {
                        country = other
                        found = matchCountryIndex
                    } else if (found < 0 && other.containsBox(country)) {
                        found = matches[0]
                    }
                }
            }

            // Case 2: Country rectangle intersects another country's rectangle.
            if (found < 0) {
                // Only include countries foe which the location is actually
                // inside the defined borders.
                for (m in 0 until matchesCount) {
                    matchCountryIndex = matches[m]
                    country = borders[matchCountryIndex]
                    if (country.contains(fixedPointLatitude, fixedPointLongitude)) {
                        distanceToBorder = country.minimumDistanceToBorders(
                            fixedPointLatitude,
                            fixedPointLongitude
                        )
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder
                            found = matchCountryIndex
                        }
                    }
                }
                if (found < 0) {
                    // Find the nearest border.
                    for (m in 0 until matchesCount) {
                        matchCountryIndex = matches[m]
                        country = borders[matchCountryIndex]
                        distanceToBorder = country.minimumDistanceToBorders(
                            fixedPointLatitude,
                            fixedPointLongitude
                        )
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder
                            found = matchCountryIndex
                        }
                    }
                }
            }
        }
        return found
    }

    /**
     * Find the first corresponding location for the time zone.
     *
     * @param tz the time zone.
     * @return the location - `null` otherwise.
     */
    fun findLocation(tz: TimeZone?): Location? {
        if (tz == null) return null
        val tzId = tz.id
        val offsetMillis = tz.rawOffset.toLong()
        var longitudeTZ = (TZ_HOUR * offsetMillis) / DateUtils.HOUR_IN_MILLIS
        if (longitudeTZ > ZmanimLocation.LONGITUDE_MAX) {
            longitudeTZ -= LONGITUDE_GLOBE
        } else if (longitudeTZ < ZmanimLocation.LONGITUDE_MIN) {
            longitudeTZ += LONGITUDE_GLOBE
        }
        val location = Location(TIMEZONE_PROVIDER)
        location.longitude = longitudeTZ

        // Find a close city in the timezone.
        val names = citiesNames
        val zones = citiesTimeZones
        val latitudes = citiesLatitudes
        val longitudes = citiesLongitudes
        val elevations = citiesElevations
        val citiesCount = names.size
        var latitude: Double
        var longitude: Double
        var distanceMin = Float.MAX_VALUE
        var nearestCityIndex = -1
        val matches = IntArray(citiesCount)
        var matchesCount = 0
        var cityIndex: Int

        // First filter for all cities with the same time zone.
        cityIndex = 0
        while (cityIndex < citiesCount) {
            if (zones[cityIndex].id == tzId) {
                matches[matchesCount++] = cityIndex
            }
            cityIndex++
        }
        if (matchesCount == 1) {
            nearestCityIndex = matches[0]
            location.latitude = latitudes[nearestCityIndex]
            location.longitude = longitudes[nearestCityIndex]
            location.altitude = elevations[nearestCityIndex]
            location.accuracy = distanceMin
            return location
        }
        if (matchesCount == 0) {
            // Maybe find the cities within the time zone.
            var longitudeWest = longitudeTZ - TZ_HOUR_HALF
            var longitudeEast = longitudeTZ + TZ_HOUR_HALF

            // In case longitudeTZ is edge case like +/-170 degrees.
            var longitudeWest2 = longitudeWest
            var longitudeEast2 = longitudeEast
            if (longitudeEast > ZmanimLocation.LONGITUDE_MAX) {
                longitudeEast = ZmanimLocation.LONGITUDE_MAX
                longitudeWest2 = ZmanimLocation.LONGITUDE_MIN
                longitudeEast2 -= LONGITUDE_GLOBE
            } else if (longitudeWest < ZmanimLocation.LONGITUDE_MIN) {
                longitudeWest = ZmanimLocation.LONGITUDE_MIN
                longitudeWest2 += LONGITUDE_GLOBE
                longitudeEast2 = ZmanimLocation.LONGITUDE_MAX
            }
            cityIndex = 0
            while (cityIndex < citiesCount) {
                longitude = longitudes[cityIndex]
                if (longitude in longitudeWest..longitudeEast || longitude in longitudeWest2..longitudeEast2) {
                    matches[matchesCount++] = cityIndex
                } else if (longitude > longitudeEast) {
                    // Cities are sorted by longitude ascending.
                    break
                }
                cityIndex++
            }
            if (matchesCount == 1) {
                nearestCityIndex = matches[0]
                location.latitude = latitudes[nearestCityIndex]
                location.longitude = longitudes[nearestCityIndex]
                location.altitude = elevations[nearestCityIndex]
                location.accuracy = distanceMin
                return location
            }
            if (matchesCount == 0) {
                // Maybe find the cities within related time zones.
                longitudeWest = longitudeTZ - TZ_HOUR
                longitudeEast = longitudeTZ + TZ_HOUR

                // In case longitudeTZ is edge case like +/-170 degrees.
                longitudeWest2 = longitudeWest
                longitudeEast2 = longitudeEast
                if (longitudeEast > ZmanimLocation.LONGITUDE_MAX) {
                    longitudeEast = ZmanimLocation.LONGITUDE_MAX
                    longitudeWest2 = ZmanimLocation.LONGITUDE_MIN
                    longitudeEast2 -= LONGITUDE_GLOBE
                } else if (longitudeWest < ZmanimLocation.LONGITUDE_MIN) {
                    longitudeWest = ZmanimLocation.LONGITUDE_MIN
                    longitudeWest2 += LONGITUDE_GLOBE
                    longitudeEast2 = ZmanimLocation.LONGITUDE_MAX
                }
                cityIndex = 0
                while (cityIndex < citiesCount) {
                    longitude = longitudes[cityIndex]
                    if (longitudeWest <= longitude && longitude <= longitudeEast || longitudeWest2 <= longitude && longitude <= longitudeEast2) {
                        matches[matchesCount++] = cityIndex
                    } else if (longitude > longitudeEast) {
                        // Cities are sorted by longitude ascending.
                        break
                    }
                    cityIndex++
                }
                if (matchesCount == 1) {
                    nearestCityIndex = matches[0]
                    location.latitude = latitudes[nearestCityIndex]
                    location.longitude = longitudes[nearestCityIndex]
                    location.altitude = elevations[nearestCityIndex]
                    location.accuracy = distanceMin
                    return location
                }
            }
        }

        // Next find the nearest city within the time zone.
        if (matchesCount > 0) {
            var searchLatitude = 0.0
            var searchLongitude = 0.0
            for (i in 0 until matchesCount) {
                cityIndex = matches[i]
                searchLatitude += latitudes[cityIndex]
                searchLongitude += longitudes[cityIndex]
            }
            searchLatitude /= matchesCount.toDouble()
            searchLongitude /= matchesCount.toDouble()
            val distances = FloatArray(1)
            for (i in 0 until matchesCount) {
                cityIndex = matches[i]
                latitude = latitudes[cityIndex]
                longitude = longitudes[cityIndex]
                Location.distanceBetween(
                    searchLatitude,
                    searchLongitude,
                    latitude,
                    longitude,
                    distances
                )
                if (distances[INDEX_DISTANCE] <= distanceMin) {
                    distanceMin = distances[INDEX_DISTANCE]
                    nearestCityIndex = cityIndex
                }
            }
            if (nearestCityIndex >= 0) {
                location.latitude = latitudes[nearestCityIndex]
                location.longitude = longitudes[nearestCityIndex]
                location.altitude = elevations[nearestCityIndex]
                location.accuracy = distanceMin
            }
        }
        return location
    }

    /**
     * Find the nearest valid city for the location.
     *
     * @param location the location.
     * @return the city - `null` otherwise.
     */
    fun findCity(location: Location): City? {
        return findCity(location.latitude, location.longitude)
    }

    /**
     * Find the nearest valid city for the location.
     *
     * @param searchLatitude the latitude to search.
     * @param searchLongitude the longitude to search.
     * @return the city - `null` otherwise.
     */
    fun findCity(searchLatitude: Double, searchLongitude: Double): City? {
        val names = citiesNames
        val countries = citiesCountries
        val latitudes = citiesLatitudes
        val longitudes = citiesLongitudes
        val elevations = citiesElevations
        val timeZones = citiesTimeZones
        val citiesCount = names.size
        var latitude: Double
        var longitude: Double
        var distanceMin = Float.MAX_VALUE
        val distances = FloatArray(1)
        val cityLocale: Locale
        var nearestCityIndex = -1
        for (i in 0 until citiesCount) {
            latitude = latitudes[i]
            longitude = longitudes[i]
            Location.distanceBetween(
                searchLatitude,
                searchLongitude,
                latitude,
                longitude,
                distances
            )
            if (distances[INDEX_DISTANCE] <= distanceMin) {
                distanceMin = distances[INDEX_DISTANCE]
                if (distanceMin <= CITY_RADIUS) {
                    nearestCityIndex = i
                }
            }
        }
        if (nearestCityIndex >= 0) {
            cityLocale = Locale(language, countries[nearestCityIndex])
            return City(locale).apply {
                this.latitude = latitudes[nearestCityIndex]
                this.longitude = longitudes[nearestCityIndex]
                this.elevation = elevations[nearestCityIndex]
                this.timeZone = timeZones[nearestCityIndex]
                this.countryCode = cityLocale.country
                this.countryName = cityLocale.getDisplayCountry(locale)
                this.locality = names[nearestCityIndex]
            }
        }
        return null
    }

    /**
     * Get the list of cities.
     *
     * @return the list of addresses.
     */
    val cities: List<City>
        get() {
            val names = citiesNames
            val countries = citiesCountries
            val latitudes = citiesLatitudes
            val longitudes = citiesLongitudes
            val elevations = citiesElevations
            val timeZones = citiesTimeZones
            val citiesCount = names.size
            val cities = mutableListOf<City>()
            val locale = locale
            var cityLocale: Locale
            val languageCode = locale.language
            for (i in 0 until citiesCount) {
                cityLocale = Locale(languageCode, countries[i])
                City(locale).apply {
                    this.latitude = latitudes[i]
                    this.longitude = longitudes[i]
                    this.elevation = elevations[i]
                    this.timeZone = timeZones[i]
                    this.countryCode = cityLocale.country
                    this.countryName = cityLocale.getDisplayCountry(locale)
                    this.locality = names[i]
                    cities.add(this)
                }
            }
            return cities
        }

    @Throws(IOException::class)
    override fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int
    ): List<Address> {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        val cities = mutableListOf<Address>()
        val names = citiesNames
        val countries = citiesCountries
        val latitudes = citiesLatitudes
        val longitudes = citiesLongitudes
        val elevations = citiesElevations
        val timeZones = citiesTimeZones
        val citiesCount = names.size
        var cityLatitude: Double
        var cityLongitude: Double
        val distances = FloatArray(1)
        var cityLocale: Locale
        val locale = locale
        for (i in 0 until citiesCount) {
            cityLatitude = latitudes[i]
            cityLongitude = longitudes[i]
            Location.distanceBetween(latitude, longitude, cityLatitude, cityLongitude, distances)
            if (distances[INDEX_DISTANCE] <= CITY_RADIUS) {
                cityLocale = Locale(language, countries[i])
                City(locale).apply {
                    this.latitude = cityLatitude
                    this.longitude = cityLongitude
                    this.elevation = elevations[i]
                    this.timeZone = timeZones[i]
                    this.countryCode = cityLocale.country
                    this.countryName = cityLocale.getDisplayCountry(locale)
                    this.locality = names[i]
                    this.id = (-1 - i).toLong() //Don't persist in db.
                    cities.add(this)
                }
            }
        }
        return cities
    }

    @Throws(LocationException::class)
    override fun createAddressResponseParser(): AddressResponseParser {
        throw LocationException()
    }

    @Throws(IOException::class)
    override fun getElevation(latitude: Double, longitude: Double): Location? {
        require(latitude in LATITUDE_MIN..LATITUDE_MAX) { "latitude == $latitude" }
        require(longitude in LONGITUDE_MIN..LONGITUDE_MAX) { "longitude == $longitude" }
        val cities = cities
        val citiesCount = cities.size
        var distance: Float
        val distanceCity = FloatArray(1)
        var d: Double
        var distancesSum = 0.0
        var n = 0
        val distances = DoubleArray(citiesCount)
        val elevations = DoubleArray(citiesCount)
        var cityNearest: City? = null
        var distanceCityMin = SAME_CITY.toDouble()
        for (city in cities) {
            if (!city.hasElevation()) continue
            Location.distanceBetween(
                latitude,
                longitude,
                city.latitude,
                city.longitude,
                distanceCity
            )
            distance = distanceCity[INDEX_DISTANCE]
            if (distance <= SAME_PLATEAU) {
                if (distance < distanceCityMin) {
                    cityNearest = city
                    distanceCityMin = distance.toDouble()
                }
                elevations[n] = city.elevation
                d = (distance * distance).toDouble()
                distances[n] = d
                distancesSum += d
                n++
            }
        }
        if (cityNearest != null) {
            val cityId = cityNearest.id
            return ZmanimLocation(USER_PROVIDER).apply {
                this.time = System.currentTimeMillis()
                this.latitude = cityNearest.latitude
                this.longitude = cityNearest.longitude
                this.altitude = cityNearest.elevation
                this.id = -if (cityId != 0L) cityId else generateCityId(cityNearest)
            }
        }
        if (n <= 1) return null
        var weightSum = 0.0
        for (i in 0 until n) {
            weightSum += (1 - distances[i] / distancesSum) * elevations[i]
        }
        return ZmanimLocation(USER_PROVIDER).apply {
            this.time = System.currentTimeMillis()
            this.latitude = latitude
            this.longitude = longitude
            this.altitude = weightSum / (n - 1)
            this.id = -generateCityId(latitude, longitude)
        }
    }

    @Throws(LocationException::class)
    override fun createElevationResponseParser(): ElevationResponseParser {
        throw LocationException()
    }

    companion object {
        /** The time zone location provider.  */
        const val TIMEZONE_PROVIDER = "timezone"

        /** Degrees per globe.  */
        private const val LONGITUDE_GLOBE = 360.0

        /** Degrees per time zone hour.  */
        private const val TZ_HOUR = LONGITUDE_GLOBE / 24

        /** Middle of a time zone, in degrees.  */
        private const val TZ_HOUR_HALF = TZ_HOUR * 0.5

        /** Factor to convert a fixed-point integer to double.  */
        private const val RATIO = CountryPolygon.RATIO

        /**
         * Not physically possible for more than 20 countries to overlap each other.
         */
        private const val MAX_COUNTRIES_OVERLAP = 20

        /** Maximum radius for which a zman is the same (20 kilometres).  */
        private const val CITY_RADIUS = 20000f

        /** The results index for the distance.  */
        private const val INDEX_DISTANCE = 0

        private var immutableCountryBorders: Array<CountryPolygon>? = null
        private var immutableCitiesCountries: Array<String>? = null
        private var immutableCitiesLatitudes: DoubleArray? = null
        private var immutableCitiesLongitudes: DoubleArray? = null
        private var immutableCitiesElevations: DoubleArray? = null
        private var immutableCitiesTimeZones: Array<TimeZone>? = null
    }
}
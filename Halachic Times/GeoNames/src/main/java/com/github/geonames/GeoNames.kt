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
package com.github.geonames

import com.github.geonames.dump.AlternateNamesParser
import com.github.geonames.dump.CountriesParser
import com.github.geonames.dump.GeoNamesParser
import com.github.geonames.dump.ShapesParser
import com.github.io.openZipStream
import com.github.json.JsonIgnore
import com.github.location.LocationException
import com.github.net.HTTPReader
import com.github.net.HTTPReader.read
import com.github.times.location.geonames.GeoNamesGeocoder
import com.github.times.location.google.GoogleGeocoder
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream
import org.geonames.AlternateName
import org.geonames.GeoNameId

/**
 * Manage lists of GeoName records.
 *
 * [GeoNames export](https://download.geonames.org/export/dump)
 *
 * @author Moshe Waisberg
 */
class GeoNames {
    /**
     * Parse the tab-delimited file with GeoName records.
     *
     * @param file       the file to parse.
     * @param filter     the filter.
     * @param zippedName the zipped name.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(
        file: File,
        filter: ToponymFilter? = null,
        zippedName: String? = null
    ): Collection<GeoNamesToponym> {
        return GeoNamesParser().parseTabbed(file, filter, zippedName)
    }

    /**
     * Populate the list of names with elevations.
     *
     * @param toponyms the list of toponyms to populate.
     */
    fun populateElevations(toponyms: Collection<GeoNamesToponym>) {
        for (toponym in toponyms) {
            try {
                if (toponym.elevation == null) {
                    populateElevation(toponym)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class, LocationException::class)
    fun populateElevation(toponym: GeoNamesToponym) {
        try {
            populateElevationGeoNames(toponym)
            if (toponym.elevation == null) {
                populateElevationGoogle(toponym)
                if (toponym.elevation == null) {
                    toponym.elevation = 0
                }
            }
        } catch (e: Exception) {
            toponym.elevation = 0
        }
    }

    @Throws(IOException::class, LocationException::class)
    fun populateElevationGeoNames(toponym: GeoNamesToponym) {
        val latitude = toponym.latitude
        val longitude = toponym.longitude
        val geocoder = GeoNamesGeocoder()
        val location = geocoder.getElevation(latitude, longitude)
        toponym.elevation = location?.altitude?.toInt()
    }

    @Throws(IOException::class, LocationException::class)
    fun populateElevationGoogle(toponym: GeoNamesToponym) {
        val latitude = toponym.latitude
        val longitude = toponym.longitude
        val geocoder = GoogleGeocoder()
        val location = geocoder.getElevation(latitude, longitude)
        toponym.elevation = location?.altitude?.toInt()
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param records the list of records to populate.
     * @see .populateAlternateNamesInternet
     */
    fun populateAlternateNames(records: Collection<GeoNamesToponym>) {
        for (record in records) {
            if ((record.alternateNames?.size ?: 0) <= 1) {
                try {
                    populateAlternateNamesInternet(record)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Populate the list of names with alternate names from the Internet.
     *
     * @param record the name to populate.
     * @see {@linktourl http://download.geonames.org/export/dump/readme.txt}
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class, SerializationException::class)
    protected fun populateAlternateNamesInternet(record: GeoNamesToponym) {
        val queryUrl = String.format(Locale.US, URL_GEONAME_GET, record.geoNameId, USERNAME)
        val url = URL(queryUrl)
        val data = read(url, HTTPReader.CONTENT_JSON) ?: return
        val toponym = JsonIgnore.decodeFromStream<GeoNamesToponym>(data)
        record.alternateNames = toponym.alternateNames
    }

    /**
     * Parse the file with GeoName alternate names.
     *
     * @param file       the file to parse.
     * @param records    the list of names.
     * @param zippedName the zipped file name.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun populateAlternateNames(
        file: File,
        records: Collection<GeoNamesToponym>,
        zippedName: String? = null
    ) {
        FileInputStream(file).use { input ->
            populateAlternateNames(input, records, zippedName)
        }
    }

    /**
     * Parse the file with GeoName alternate names.
     *
     * @param input      the input to parse.
     * @param records    the list of names.
     * @param zippedName the zipped file name.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun populateAlternateNames(
        input: InputStream,
        records: Collection<GeoNamesToponym>,
        zippedName: String? = null
    ) {
        openZipStream(input, zippedName).use { inputStream ->
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                populateAlternateNames(reader, records)
            }
        }
    }

    @Throws(IOException::class)
    fun populateAlternateNames(reader: Reader, records: Collection<GeoNamesToponym>) {
        val filter = AlternateNameFilter(toponyms = records)
        val alternateNames = AlternateNamesParser().parseTabbed(reader, filter)
        val alternateNamesById: MutableMap<GeoNameId, MutableList<AlternateName>> = mutableMapOf()
        alternateNames.forEach { alternateName ->
            val id = alternateName.geoNameId
            val names = alternateNamesById[id] ?: mutableListOf()
            names.add(alternateName)
            alternateNamesById[id] = names
        }

        records.forEach { record ->
            val names = alternateNamesById[record.geoNameId]
            if (names != null) {
                val alternateNamesList =
                    (record.alternateNames as? MutableList<AlternateName>) ?: mutableListOf()
                alternateNamesList += names
                record.alternateNames = alternateNamesList
            }
        }
    }

    /**
     * Parse the tab-delimited file with country records.
     *
     * @param file the file to parse.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseCountries(file: File): Collection<CountryInfo> {
        return CountriesParser().parseTabbed(file)
    }

    @Throws(IOException::class)
    fun parseShapes(file: File, zippedName: String? = null): Collection<GeoShape> {
        return ShapesParser().parseTabbed(file, zippedName)
    }

    companion object {
        /**
         * GeoNames user name.
         */
        private const val USERNAME = ""
        private const val TAG_ELEVATION_STATUS = "status"
        private const val TAG_ELEVATION_RESULT = "result"
        private const val TAG_ELEVATION_ELEVATION = "elevation"

        /**
         * URL that returns the attribute of the geoNames feature with the given geoNameId as JSON document.
         */
        private const val URL_GEONAME_GET =
            "http://api.geonames.org/getJSON?geoNameId=%d&username=%s"

        /**
         * abbreviation
         */
        private const val ALTERNATE_NAMES_ABBR = "abbr"

        /**
         * airport codes
         */
        private const val ALTERNATE_NAMES_FAAC = "faac"

        /**
         * French Revolution names
         */
        private const val ALTERNATE_NAMES_FR_1793 = "fr_1793"

        /**
         * airport codes
         */
        private const val ALTERNATE_NAMES_IATA = "iata"

        /**
         * airport codes
         */
        private const val ALTERNATE_NAMES_ICAO = "icao"

        /**
         * a website
         */
        private const val ALTERNATE_NAMES_LINK = "link"

        /**
         * phonetics
         */
        private const val ALTERNATE_NAMES_PHON = "phon"

        /**
         * pinyin
         */
        private const val ALTERNATE_NAMES_PINY = "piny"

        /**
         * postal codes
         */
        private const val ALTERNATE_NAMES_POST = "post"

        /**
         * airport codes
         */
        private const val ALTERNATE_NAMES_TCID = "tcid"

        /**
         * UNLOCODE
         */
        private const val ALTERNATE_NAMES_UNLC = "unlc"

        /**
         * wikidataid
         */
        private const val ALTERNATE_NAMES_WKDT = "wkdt"
    }
}

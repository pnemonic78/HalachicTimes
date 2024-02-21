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

import com.github.net.HTTPReader
import com.github.net.HTTPReader.read
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.json.Json
import javax.json.JsonObject
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.geonames.FeatureClass
import org.geonames.Timezone
import org.w3c.dom.Element
import org.xml.sax.SAXException

/**
 * Manage lists of GeoName records.
 *
 * [GeoNames export](https://download.geonames.org/export/dump/)
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
        filter: NameFilter? = null,
        zippedName: String? = null
    ): Collection<GeoNamesToponym> {
        FileInputStream(file).use { input ->
            openZipStream(input, zippedName).use { inputStream ->
                InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                    return parseTabbed(reader, filter)
                }
            }
        }
    }

    /**
     * Parse the tab-delimited file with GeoName records.
     * `
     * The main 'geoname' table has the following fields :<br></br>
     * ---------------------------------------------------<br></br>
     * geonameid         : integer id of record in geonames database<br></br>
     * name              : name of geographical point (utf8) varchar(200)<br></br>
     * asciiname         : name of geographical point in plain ascii characters, varchar(200)<br></br>
     * alternatenames    : alternatenames, comma separated varchar(5000)<br></br>
     * latitude          : latitude in decimal degrees (wgs84)<br></br>
     * longitude         : longitude in decimal degrees (wgs84)<br></br>
     * feature class     : see http://www.geonames.org/export/codes.html, char(1)<br></br>
     * feature code      : see http://www.geonames.org/export/codes.html, varchar(10)<br></br>
     * country code      : ISO-3166 2-letter country code, 2 characters<br></br>
     * cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters<br></br>
     * admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)<br></br>
     * admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)<br></br>
     * admin3 code       : code for third level administrative division, varchar(20)<br></br>
     * admin4 code       : code for fourth level administrative division, varchar(20)<br></br>
     * population        : bigint (8 byte int)<br></br>
     * elevation         : in meters, integer<br></br>
     * dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.<br></br>
     * timezone          : the timezone id (see file timeZone.txt) varchar(40)<br></br>
     * modification date : date of last modification in yyyy-MM-dd format<br></br>
     *
     * @param reader the reader.
     * @param filter the filter.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(reader: Reader, filter: NameFilter? = null): Collection<GeoNamesToponym> {
        val records = mutableListOf<GeoNamesToponym>()
        var record: GeoNamesToponym
        var line: String?
        val buf = BufferedReader(reader)
        var fields: Array<String>
        var field: String
        var timezone: Timezone
        while (true) {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            record = GeoNamesToponym()
            field = fields[FIELD_GEONAME_ID]
            record.geoNameId = field.toInt()
            field = fields[FIELD_GEONAME_NAME]
            record.name = field
            field = fields[FIELD_GEONAME_ASCIINAME]
            record.asciiName = field
            field = fields[FIELD_GEONAME_ALTERNATENAMES]
            record.alternateNames = field
            field = fields[FIELD_GEONAME_LATITUDE]
            record.latitude = field.toDouble()
            field = fields[FIELD_GEONAME_LONGITUDE]
            record.longitude = field.toDouble()
            field = fields[FIELD_GEONAME_FEATURE_CLASS]
            record.featureClass = FeatureClass.fromValue(field)
            field = fields[FIELD_GEONAME_FC]
            record.featureCode = field
            field = fields[FIELD_GEONAME_CC]
            record.countryCode = field
            field = fields[FIELD_GEONAME_CC2]
            record.alternateCountryCodes = field
            field = fields[FIELD_GEONAME_ADMIN1_CODE]
            record.adminCode1 = field
            field = fields[FIELD_GEONAME_ADMIN2_CODE]
            record.adminCode2 = field
            field = fields[FIELD_GEONAME_ADMIN3_CODE]
            record.adminCode3 = field
            field = fields[FIELD_GEONAME_ADMIN4_CODE]
            record.adminCode4 = field
            field = fields[FIELD_GEONAME_POPULATION]
            record.population = field.toLong()
            field = fields[FIELD_GEONAME_ELEVATION]
            if (field.length > 0) record.elevation = field.toInt()
            field = fields[FIELD_GEONAME_DEM]
            if (field.length > 0) record.setDigitalElevation(field.toInt())
            field = fields[FIELD_GEONAME_TIMEZONE]
            if (field.length == 0) {
                // throw new NullPointerException("time zone required for " +
                // record.getGeoNameId());
                System.err.println("time zone required for " + record.geoNameId)
                System.err.println(line)
                continue
            }
            timezone = Timezone()
            timezone.timezoneId = field
            record.timezone = timezone
            field = fields[FIELD_GEONAME_MODIFICATION]
            record.modification = field
            if (filter == null || filter.accept(record)) {
                filter?.replaceLocation(record)
                records.add(record)
            }
        }
        return records
    }

    /**
     * Populate the list of names with elevations.
     *
     * @param geoNames the list of names to populate.
     */
    fun populateElevations(geoNames: Collection<GeoNamesToponym>) {
        var elevation: Int?
        for (name in geoNames) {
            try {
                elevation = name.grossElevation
                if (elevation == null) {
                    populateElevation(name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun populateElevation(geoName: GeoNamesToponym) {
        geoName.elevation = 0
        try {
            populateElevationGeoNames(geoName)
        } catch (e: Exception) {
            populateElevationGoogle(geoName)
        }
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun populateElevationGeoNames(geoName: GeoNamesToponym) {
        val latitude = geoName.latitude
        val longitude = geoName.longitude
        val queryUrl = String.format(Locale.US, URL_ELEVATION_AGDEM, latitude, longitude, USERNAME)
        val url = URL(queryUrl)
        val data = read(url) ?: return
        val elevationValue =
            BufferedReader(InputStreamReader(data, StandardCharsets.UTF_8)).readLine()
                .trim { it <= ' ' }
        geoName.elevation = elevationValue.toDouble().toInt()
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun populateElevationGoogle(geoName: GeoNamesToponym) {
        val latitude = geoName.latitude
        val longitude = geoName.longitude
        val queryUrl = String.format(Locale.US, URL_ELEVATION_GOOGLE, latitude, longitude)
        val url = URL(queryUrl)
        val data = read(url, HTTPReader.CONTENT_XML)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(data)
        val root = doc.documentElement
        val statusNode = root.getElementsByTagName(TAG_ELEVATION_STATUS).item(0) as Element
        val status = statusNode.textContent
        if ("OK" != status) {
            System.err.println("status: $status")
            return
        }
        val resultNode = root.getElementsByTagName(TAG_ELEVATION_RESULT).item(0) as Element
        val elevationNode =
            resultNode.getElementsByTagName(TAG_ELEVATION_ELEVATION).item(0) as Element
        val elevationValue = elevationNode.textContent.trim { it <= ' ' }
        val elevation = elevationValue.toDouble()
        geoName.elevation = elevation.toInt()
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param records the list of records to populate.
     * @see .populateAlternateNamesInternet
     */
    fun populateAlternateNames(records: Collection<GeoNamesToponym>) {
        var alternateNames: Map<String, AlternateName>
        for (record in records) {
            alternateNames = record.alternateNamesMap
            if (alternateNames.size <= 1) {
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
    @Throws(IOException::class)
    protected fun populateAlternateNamesInternet(record: GeoNamesToponym) {
        val queryUrl = String.format(Locale.US, URL_GEONAME_GET, record.geoNameId, USERNAME)
        val url = URL(queryUrl)
        val data = read(url)
        val reader = Json.createReader(data)
        val json = reader.readObject()
        val arr = json.getJsonArray("alternateNames")
        val alternateNames = record.alternateNamesMap
        var alternateName: AlternateName
        var jsonAlternateName: JsonObject
        var lang: String
        var name: String?
        val length = arr.size
        for (i in 0 until length) {
            jsonAlternateName = arr.getJsonObject(i)
            if (!jsonAlternateName.containsKey("lang")) {
                continue
            }
            lang = jsonAlternateName.getString("lang")
            name = jsonAlternateName.getString("name")
            alternateName = AlternateName(lang, name)
            alternateNames.put(lang, alternateName)
        }
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

    /**
     * Parse the file with GeoName alternate names.
     * `
     * The table 'alternate names' :<br></br>
     * -----------------------------
     * alternateNameId   : the id of this alternate name, int<br></br>
     * geonameid         : geonameId referring to id in table 'geoname', int<br></br>
     * isolanguage       : iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and faac for airport codes, fr_1793 for French Revolution names,  abbr for abbreviation, link for a website, varchar(7)<br></br>
     * alternate name    : alternate name or name variant, varchar(200)<br></br>
     * isPreferredName   : '1', if this alternate name is an official/preferred name<br></br>
     * isShortName       : '1', if this is a short name like 'California' for 'State of California'<br></br>
     * isColloquial      : '1', if this alternate name is a colloquial or slang term<br></br>
     * isHistoric        : '1', if this alternate name is historic and was used in the past<br></br>
     *
     * @param reader  the reader.
     * @param records the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun populateAlternateNames(reader: Reader, records: Collection<GeoNamesToponym>) {
        val recordsById: MutableMap<Int, GeoNamesToponym> = HashMap()
        for (record in records) {
            recordsById[record.geoNameId] = record
        }
        var record: GeoNamesToponym?
        var line: String?
        val buf = BufferedReader(reader)
        var fields: Array<String>
        var geonameId: GeoNameId
        var language: String
        var name: String
        var preferredName: Boolean
        var shortName: Boolean
        var colloquial: Boolean
        var historic: Boolean
        val finished = mutableSetOf<GeoNameId>()
        while (true) {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty()) continue
            fields = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            geonameId = fields[FIELD_ALTERNATE_NAMES_GEONAME_ID].toInt()
            if (finished.contains(geonameId)) {
                continue
            }
            record = recordsById[geonameId]
            if (record == null) {
                continue
            }
            language = fields[FIELD_ALTERNATE_NAMES_LANGUAGE]
            if (language.isEmpty() || ALTERNATE_NAMES_ABBR == language || ALTERNATE_NAMES_FAAC == language || ALTERNATE_NAMES_FR_1793 == language || ALTERNATE_NAMES_IATA == language || ALTERNATE_NAMES_ICAO == language || ALTERNATE_NAMES_PHON == language || ALTERNATE_NAMES_PINY == language || ALTERNATE_NAMES_POST == language || ALTERNATE_NAMES_TCID == language || ALTERNATE_NAMES_WKDT == language) {
                continue
            }
            // "unlc" is almost always the last record in the group, so anything following is probably a mistake.
            if (ALTERNATE_NAMES_UNLC == language) {
                finished.add(geonameId)
                continue
            }
            name = fields[FIELD_ALTERNATE_NAMES_NAME]
            if (ALTERNATE_NAMES_LINK == language) {
                if (record.wikipediaURL == null) {
                    record.wikipediaURL = name
                }
                continue
            }
            preferredName =
                fields.size > FIELD_ALTERNATE_NAMES_PREFERRED && "1" == fields[FIELD_ALTERNATE_NAMES_PREFERRED]
            shortName =
                fields.size > FIELD_ALTERNATE_NAMES_SHORT && "1" == fields[FIELD_ALTERNATE_NAMES_SHORT]
            colloquial =
                fields.size > FIELD_ALTERNATE_NAMES_COLLOQUIAL && "1" == fields[FIELD_ALTERNATE_NAMES_COLLOQUIAL]
            historic =
                fields.size > FIELD_ALTERNATE_NAMES_HISTORIC && "1" == fields[FIELD_ALTERNATE_NAMES_HISTORIC]
            record.putAlternateName(
                geonameId,
                language,
                name,
                preferredName,
                shortName,
                colloquial,
                historic
            )
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
        FileInputStream(file).use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return parseCountries(reader)
            }
        }
    }

    /**
     * Parse the tab-delimited file with GeoName records.
     *
     * @param reader the reader.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseCountries(reader: Reader): Collection<CountryInfo> {
        val records = mutableListOf<CountryInfo>()
        var record: CountryInfo
        var line: String?
        val buf = BufferedReader(reader)
        var fields: Array<String>
        var column: Int
        var field: String
        while (true) {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            record = CountryInfo()
            column = 0
            field = fields[column++]
            record.iso = field
            field = fields[column++]
            record.iso3 = field
            field = fields[column++]
            record.isoNumeric = field.toInt()
            field = fields[column++]
            record.fips = field
            field = fields[column++]
            record.country = field
            field = fields[column++]
            record.capital = field
            field = fields[column++]
            record.area = field.toDouble()
            field = fields[column++]
            record.population = field.toLong()
            field = fields[column++]
            record.continent = field
            field = fields[column++]
            record.tld = field
            field = fields[column++]
            record.currencyCode = field
            field = fields[column++]
            record.currencyName = field
            field = fields[column++]
            record.phone = field
            field = fields[column++]
            record.postalCodeFormat = field
            field = fields[column++]
            record.postalCodeRegex = field
            field = fields[column++]
            record.languages =
                listOf(*field.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
            field = fields[column++]
            record.geoNameId = field.toInt()
            if (column < fields.size) {
                field = fields[column++]
                record.neighbours =
                    listOf(*field.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                if (column < fields.size) {
                    field = fields[column++]
                    record.equivalentFipsCode = field
                }
            }
            records.add(record)
        }
        return records
    }

    @Throws(IOException::class)
    fun parseShapes(file: File): Collection<GeoShape> {
        FileInputStream(file).use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return parseShapes(reader)
            }
        }
    }

    @Throws(IOException::class)
    fun parseShapes(reader: Reader): Collection<GeoShape> {
        val records = mutableListOf<GeoShape>()
        var record: GeoShape
        var line: String?
        val buf = BufferedReader(reader)
        var fields: Array<String>
        var column: Int
        var field: String

        // Skip header row.
        buf.readLine()
        while (true) {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            record = GeoShape()
            column = 0
            field = fields[column++]
            record.geoNameId = field.toInt()
            field = fields[column++]
            record.geoJSON = field
            records.add(record)
        }
        return records
    }

    @Throws(IOException::class)
    private fun openZipStream(input: InputStream, zippedName: String?): InputStream {
        if (zippedName.isNullOrEmpty()) return input
        val zin = ZipInputStream(input)
        var entry: ZipEntry?
        do {
            entry = zin.nextEntry
        } while (entry != null && zippedName != entry.name)
        return zin
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
         * URL that returns the attribute of the geoNames feature with the given geonameId as JSON document.
         */
        private const val URL_GEONAME_GET =
            "http://api.geonames.org/getJSON?geonameId=%d&username=%s"

        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.
         */
        private const val URL_ELEVATION_GOOGLE =
            "http://maps.googleapis.com/maps/api/elevation/xml?locations=%f,%f"

        /**
         * URL that accepts latitude and longitude coordinates as parameters for an
         * elevation.<br></br>
         * Uses Aster Global Digital Elevation Model data.
         */
        private const val URL_ELEVATION_AGDEM =
            "http://api.geonames.org/astergdem?lat=%f&lng=%f&username=%s"

        /**
         * integer id of record in geonames database
         */
        private const val FIELD_GEONAME_ID = 0

        /**
         * name of geographical point (utf8) varchar(200)
         */
        private const val FIELD_GEONAME_NAME = 1

        /**
         * name of geographical point in plain ascii characters, varchar(200)
         */
        private const val FIELD_GEONAME_ASCIINAME = 2

        /**
         * alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
         */
        private const val FIELD_GEONAME_ALTERNATENAMES = 3

        /**
         * latitude in decimal degrees (wgs84)
         */
        private const val FIELD_GEONAME_LATITUDE = 4

        /**
         * longitude in decimal degrees (wgs84)
         */
        private const val FIELD_GEONAME_LONGITUDE = 5

        /**
         * see http://www.geonames.org/export/codes.html, char(1)
         */
        private const val FIELD_GEONAME_FEATURE_CLASS = 6

        /**
         * see http://www.geonames.org/export/codes.html, varchar(10)
         */
        private const val FIELD_GEONAME_FC = 7

        /**
         * ISO-3166 2-letter country code, 2 characters
         */
        private const val FIELD_GEONAME_CC = 8

        /**
         * alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
         */
        private const val FIELD_GEONAME_CC2 = 9

        /**
         * fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
         */
        private const val FIELD_GEONAME_ADMIN1_CODE = 10

        /**
         * code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
         */
        private const val FIELD_GEONAME_ADMIN2_CODE = 11

        /**
         * code for third level administrative division, varchar(20)
         */
        private const val FIELD_GEONAME_ADMIN3_CODE = 12

        /**
         * code for fourth level administrative division, varchar(20)
         */
        private const val FIELD_GEONAME_ADMIN4_CODE = 13

        /**
         * bigint (8 byte int)
         */
        private const val FIELD_GEONAME_POPULATION = 14

        /**
         * in meters, integer
         */
        private const val FIELD_GEONAME_ELEVATION = 15

        /**
         * digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
         */
        private const val FIELD_GEONAME_DEM = 16

        /**
         * the iana timezone id (see file timeZone.txt) varchar(40)
         */
        private const val FIELD_GEONAME_TIMEZONE = 17

        /**
         * date of last modification in yyyy-MM-dd format
         */
        private const val FIELD_GEONAME_MODIFICATION = 18

        /**
         * the id of this alternate name, int
         */
        private const val FIELD_ALTERNATE_NAMES_ID = 0

        /**
         * geonameId referring to id in table 'geoname', int
         */
        private const val FIELD_ALTERNATE_NAMES_GEONAME_ID = 1

        /**
         * iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and faac for airport codes, fr_1793 for French Revolution names,  abbr for abbreviation, link to a website (mostly to wikipedia), wkdt for the wikidataid, varchar(7)
         */
        private const val FIELD_ALTERNATE_NAMES_LANGUAGE = 2

        /**
         * alternate name or name variant, varchar(400)
         */
        private const val FIELD_ALTERNATE_NAMES_NAME = 3

        /**
         * '1', if this alternate name is an official/preferred name
         */
        private const val FIELD_ALTERNATE_NAMES_PREFERRED = 4

        /**
         * '1', if this is a short name like 'California' for 'State of California'
         */
        private const val FIELD_ALTERNATE_NAMES_SHORT = 5

        /**
         * '1', if this alternate name is a colloquial or slang term. Example: 'Big Apple' for 'New York'.
         */
        private const val FIELD_ALTERNATE_NAMES_COLLOQUIAL = 6

        /**
         * '1', if this alternate name is historic and was used in the past. Example 'Bombay' for 'Mumbai'.
         */
        private const val FIELD_ALTERNATE_NAMES_HISTORIC = 7

        /**
         * from period when the name was used
         */
        private const val FIELD_ALTERNATE_NAMES_FROM = 8

        /**
         * to period when the name was used
         */
        private const val FIELD_ALTERNATE_NAMES_TO = 9

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

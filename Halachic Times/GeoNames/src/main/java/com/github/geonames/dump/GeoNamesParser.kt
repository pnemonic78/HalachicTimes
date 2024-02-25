package com.github.geonames.dump

import com.github.geonames.GeoNamesToponym
import com.github.geonames.ToponymFilter
import com.github.io.openZipStream
import com.github.lang.RegexTab
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import org.geonames.FeatureClass
import org.geonames.TimeZone

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
 */
class GeoNamesParser {
    /**
     * Parse the tab-delimited file with GeoName records.
     * @param reader the reader.
     * @param filter the filter.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(reader: Reader, filter: ToponymFilter? = null): Collection<GeoNamesToponym> {
        val buf = BufferedReader(reader)
        val records = mutableListOf<GeoNamesToponym>()
        var record: GeoNamesToponym
        var line: String?
        var fields: List<String>
        var field: String

        do {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split(RegexTab)
            record = GeoNamesToponym()
            field = fields[FIELD_GEONAME_ID]
            record.geoNameId = field.toInt()
            field = fields[FIELD_GEONAME_NAME]
            record.name = field
            field = fields[FIELD_GEONAME_ASCIINAME]
            record.asciiName = field
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
            if (field.isNotEmpty()) record.population = field.toLong()
            field = fields[FIELD_GEONAME_ELEVATION]
            if (field.isNotEmpty()) record.elevation = field.toInt()
            field = fields[FIELD_GEONAME_DEM]
            if (field.isNotEmpty()) record.elevationAsterGDEM = field.toInt()
            field = fields[FIELD_GEONAME_TIMEZONE]
            if (field.isEmpty()) {
                // throw new NullPointerException("time zone required for " +
                // record.getGeoNameId());
                System.err.println("time zone required for " + record.geoNameId)
                System.err.println(line)
                continue
            }
            record.timeZone = TimeZone(id = field)
            field = fields[FIELD_GEONAME_MODIFICATION]
            record.modification = field
            if (filter == null || filter.accept(record)) {
                filter?.replaceLocation(record)
                records.add(record)
            }
        } while (true)
        return records
    }

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
        FileInputStream(file).use { input ->
            openZipStream(input, zippedName).use { inputStream ->
                InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                    return parseTabbed(reader, filter)
                }
            }
        }
    }

    companion object {
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
    }
}
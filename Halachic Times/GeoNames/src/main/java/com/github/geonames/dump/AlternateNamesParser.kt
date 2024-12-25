package com.github.geonames.dump

import com.github.geonames.AlternateNameFilter
import com.github.lang.RegexTab
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import org.geonames.AlternateName

class AlternateNamesParser {
    /**
     * Parse the file with GeoName alternate names.
     * `
     * The table 'alternate names' :<br/>
     * -----------------------------
     * alternateNameId   : the id of this alternate name, int<br/>
     * geonameid         : geonameId referring to id in table 'geoname', int<br/>
     * isolanguage       : iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and faac for airport codes, fr_1793 for French Revolution names,  abbr for abbreviation, link for a website, varchar(7)<br/>
     * alternate name    : alternate name or name variant, varchar(200)<br/>
     * isPreferredName   : '1', if this alternate name is an official/preferred name<br/>
     * isShortName       : '1', if this is a short name like 'California' for 'State of California'<br/>
     * isColloquial      : '1', if this alternate name is a colloquial or slang term<br/>
     * isHistoric        : '1', if this alternate name is historic and was used in the past<br/>
     *
     * @param reader the reader.
     * @param filter the name filter.
     * @throws IOException if an I/O error occurs.
     * @return the list of names.
     */
    @Throws(IOException::class)
    fun parseTabbed(reader: Reader, filter: AlternateNameFilter? = null): Collection<AlternateName> {
        val buf = BufferedReader(reader)
        val records = mutableListOf<AlternateName>()
        var line: String?
        var fields: List<String>
        var record: AlternateName?

        do {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty()) continue
            fields = line.split(RegexTab)
            if (fields.size <= FIELD_ALTERNATE_NAMES_NAME) continue

            val nameId = fields[FIELD_ALTERNATE_NAMES_ID].toInt()
            if (nameId < 0) continue
            val geoNameId = fields[FIELD_ALTERNATE_NAMES_GEONAME_ID].toInt()
            if (geoNameId <= 0) continue
            val language = fields[FIELD_ALTERNATE_NAMES_LANGUAGE]
            if (language.isEmpty()) continue
            val name = fields[FIELD_ALTERNATE_NAMES_NAME]
            if (name.isEmpty()) continue

            record = AlternateName(
                alternateNameId = nameId,
                geoNameId = geoNameId,
                language = language,
                name = name,
                isPreferred = fields.size > FIELD_ALTERNATE_NAMES_PREFERRED &&
                    VALUE_TRUE == fields[FIELD_ALTERNATE_NAMES_PREFERRED],
                isShort = fields.size > FIELD_ALTERNATE_NAMES_SHORT &&
                    VALUE_TRUE == fields[FIELD_ALTERNATE_NAMES_SHORT],
                isColloquial = fields.size > FIELD_ALTERNATE_NAMES_COLLOQUIAL &&
                    VALUE_TRUE == fields[FIELD_ALTERNATE_NAMES_COLLOQUIAL],
                isHistoric = fields.size > FIELD_ALTERNATE_NAMES_HISTORIC &&
                    VALUE_TRUE == fields[FIELD_ALTERNATE_NAMES_HISTORIC]
            )

            if (filter == null || filter.accept(record)) {
                records.add(record)
            }
        } while (true)

        return records
    }

    /**
     * Parse the tab-delimited file with alternate names.
     *
     * @param file the file to parse.
     * @param filter the name filter.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(file: File, filter: AlternateNameFilter? = null): Collection<AlternateName> {
        FileInputStream(file).use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return parseTabbed(reader, filter)
            }
        }
    }

    companion object {
        /**
         * The id of this alternate name, int
         */
        private const val FIELD_ALTERNATE_NAMES_ID = 0

        /**
         * geonameId referring to id in table 'geoname', int
         */
        private const val FIELD_ALTERNATE_NAMES_GEONAME_ID = 1

        /**
         * ISO 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and faac for airport codes, fr_1793 for French Revolution names,  abbr for abbreviation, link to a website (mostly to wikipedia), wkdt for the wikidataid, varchar(7)
         */
        private const val FIELD_ALTERNATE_NAMES_LANGUAGE = 2

        /**
         * Alternate name or name variant, varchar(400)
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
         * From period when the name was used
         */
        private const val FIELD_ALTERNATE_NAMES_FROM = 8

        /**
         * To period when the name was used
         */
        private const val FIELD_ALTERNATE_NAMES_TO = 9

        private const val ALTERNATE_NAMES_ABBR = AlternateName.TYPE_ABBR
        private const val ALTERNATE_NAMES_FAAC = AlternateName.TYPE_FAAC
        private const val ALTERNATE_NAMES_FR_1793 = AlternateName.TYPE_FR_1793
        private const val ALTERNATE_NAMES_IATA = AlternateName.TYPE_IATA
        private const val ALTERNATE_NAMES_ICAO = AlternateName.TYPE_ICAO
        private const val ALTERNATE_NAMES_LINK = AlternateName.TYPE_LINK
        private const val ALTERNATE_NAMES_PHON = AlternateName.TYPE_PHON
        private const val ALTERNATE_NAMES_PINY = AlternateName.TYPE_PINY
        private const val ALTERNATE_NAMES_POST = AlternateName.TYPE_POST
        private const val ALTERNATE_NAMES_TCID = AlternateName.TYPE_TCID
        private const val ALTERNATE_NAMES_UNLC = AlternateName.TYPE_UNLC
        private const val ALTERNATE_NAMES_WKDT = AlternateName.TYPE_WKDT

        private const val VALUE_TRUE = "1"
    }
}
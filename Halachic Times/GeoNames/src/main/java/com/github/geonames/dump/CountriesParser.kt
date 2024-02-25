package com.github.geonames.dump

import com.github.geonames.CountryInfo
import com.github.lang.RegexComma
import com.github.lang.RegexMinus
import com.github.lang.RegexTab
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

class CountriesParser {

    /**
     * Parse the tab-delimited file with GeoName records.
     *
     * @param reader the reader.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(reader: Reader): Collection<CountryInfo> {
        val buf = BufferedReader(reader)
        val records = mutableListOf<CountryInfo>()
        var record: CountryInfo
        var line: String?
        var fields: List<String>
        var field: String

        do {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split(RegexTab)

            record = CountryInfo()
            field = fields[FIELD_ISO]
            record.iso = field
            field = fields[FIELD_ISO3]
            record.iso3 = field
            field = fields[FIELD_ISO_NUMERIC]
            record.isoNumeric = field.toInt()
            field = fields[FIELD_FIPS]
            record.fips = field
            field = fields[FIELD_COUNTRY]
            record.country = field
            field = fields[FIELD_CAPITAL]
            record.capital = field
            field = fields[FIELD_AREA]
            record.area = field.toDouble()
            field = fields[FIELD_POPULATION]
            record.population = field.toLong()
            field = fields[FIELD_CONTINENT]
            record.continent = field
            field = fields[FIELD_TLD]
            record.tld = field
            field = fields[FIELD_CURRENCY_CODE]
            record.currencyCode = field
            field = fields[FIELD_CURRENCY_NAME]
            record.currencyName = field
            field = fields[FIELD_PHONE]
            record.phone = field
            field = fields[FIELD_POSTALCODE_FORMAT]
            record.postalCodeFormat = field
            field = fields[FIELD_POSTALCODE_REGEX]
            record.postalCodeRegex = field
            field = fields[FIELD_LANGUAGES]
            record.languages = field.split(RegexComma).dropLastWhile { it.isEmpty() }
            field = fields[FIELD_GEONAME_ID]
            record.geoNameId = field.toInt()
            if (fields.size > FIELD_NEIGHBOURS) {
                field = fields[FIELD_NEIGHBOURS]
                record.neighbours = field.split(RegexComma).dropLastWhile { it.isEmpty() }
                if (fields.size > FIELD_EQUIVALENT_FIPS_CODE) {
                    field = fields[FIELD_EQUIVALENT_FIPS_CODE]
                    record.equivalentFipsCode = field
                }
            }
            records.add(record)
        } while (true)
        return records
    }

    /**
     * Parse the tab-delimited file with country records.
     *
     * @param file the file to parse.
     * @return the list of names.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun parseTabbed(file: File): Collection<CountryInfo> {
        FileInputStream(file).use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return parseTabbed(reader)
            }
        }
    }

    companion object {
        private const val FIELD_ISO = 0
        private const val FIELD_ISO3 = 1
        private const val FIELD_ISO_NUMERIC = 2
        private const val FIELD_FIPS = 3
        private const val FIELD_COUNTRY = 4
        private const val FIELD_CAPITAL = 5
        private const val FIELD_AREA = 6
        private const val FIELD_POPULATION = 7
        private const val FIELD_CONTINENT = 8
        private const val FIELD_TLD = 9
        private const val FIELD_CURRENCY_CODE = 10
        private const val FIELD_CURRENCY_NAME = 11
        private const val FIELD_PHONE = 12
        private const val FIELD_POSTALCODE_FORMAT = 13
        private const val FIELD_POSTALCODE_REGEX = 14
        private const val FIELD_LANGUAGES = 15
        private const val FIELD_GEONAME_ID = 16
        private const val FIELD_NEIGHBOURS = 17
        private const val FIELD_EQUIVALENT_FIPS_CODE = 18
    }
}
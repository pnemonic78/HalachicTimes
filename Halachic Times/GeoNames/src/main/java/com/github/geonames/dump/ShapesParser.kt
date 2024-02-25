package com.github.geonames.dump

import com.github.geonames.GeoShape
import com.github.io.openZipStream
import com.github.lang.RegexTab
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

class ShapesParser {
    @Throws(IOException::class)
    fun parseTabbed(reader: Reader): Collection<GeoShape> {
        val buf = BufferedReader(reader)
        val records = mutableListOf<GeoShape>()
        var record: GeoShape
        var line: String?
        var fields: List<String>
        var field: String

        // Skip header row.
        buf.readLine()
        do {
            line = buf.readLine()
            if (line == null) break
            if (line.isEmpty() || line.startsWith("#")) continue
            fields = line.split(RegexTab)
            record = GeoShape()
            field = fields[FIELD_GEONAME_ID]
            record.geoNameId = field.toInt()
            field = fields[FIELD_GEOJSON]
            record.geoJSON = field
            records.add(record)
        } while (true)
        return records
    }

    @Throws(IOException::class)
    fun parseTabbed(file: File, zippedName: String? = null): Collection<GeoShape> {
        FileInputStream(file).use { input ->
            return parseTabbed(input, zippedName)
        }
    }

    @Throws(IOException::class)
    fun parseTabbed(input: InputStream, zippedName: String? = null): Collection<GeoShape> {
        openZipStream(input, zippedName).use { inputStream ->
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                return parseTabbed(reader)
            }
        }
    }

    companion object {
        private const val FIELD_GEONAME_ID = 0
        private const val FIELD_GEOJSON = 1
    }
}
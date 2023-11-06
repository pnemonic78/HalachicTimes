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
package com.github.times.location.geonames

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.Date
import org.geonames.Timezone

/**
 * JSON type adapter for GeoNames timezones.
 *
 * @author Moshe Waisberg
 */
class GeoNamesTimezoneAdapter : TypeAdapter<Timezone>() {
    // TODO inject via constructor that created via factory
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Timezone? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return readTimezone(`in`)
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Timezone?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("timezoneId").value(value.timezoneId)
        out.name("dstOffset").value(value.dstOffset)
        out.name("gmtOffset").value(value.gmtOffset)
        out.name("countryCode").value(value.countryCode)
        out.name("sunrise").value(writeDate(value.sunrise))
        out.name("sunset").value(writeDate(value.sunset))
        out.name("time").value(writeDate(value.time))
        out.endObject()
    }

    @Throws(IOException::class)
    private fun readTimezone(reader: JsonReader): Timezone {
        val timezone = Timezone()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "timezoneId" -> timezone.timezoneId = reader.nextString()
                "dstOffset" -> timezone.dstOffset = reader.nextDouble()
                "gmtOffset" -> timezone.gmtOffset = reader.nextDouble()
                "countryCode" -> timezone.countryCode = reader.nextString()
                "sunrise" -> timezone.sunrise = readDate(reader)
                "sunset" -> timezone.sunset = readDate(reader)
                "time" -> timezone.time = readDate(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return timezone
    }

    @Throws(IOException::class)
    private fun readDate(reader: JsonReader): Date {
        return gson.getAdapter(Date::class.java).read(reader)
    }

    private fun writeDate(value: Date): String {
        return gson.getAdapter(Date::class.java).toJson(value)
    }
}
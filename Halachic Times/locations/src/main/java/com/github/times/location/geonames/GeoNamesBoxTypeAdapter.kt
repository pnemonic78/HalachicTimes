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

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import org.geonames.BoundingBox

/**
 * JSON type adapter for GeoNames bounding boxes.
 *
 * @author Moshe Waisberg
 */
class GeoNamesBoxTypeAdapter : TypeAdapter<BoundingBox>() {
    @Throws(IOException::class)
    override fun read(`in`: JsonReader): BoundingBox? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return readBoundingBox(`in`)
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: BoundingBox?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("west").value(value.west)
        out.name("east").value(value.east)
        out.name("south").value(value.south)
        out.name("north").value(value.north)
        out.endObject()
    }

    @Throws(IOException::class)
    private fun readBoundingBox(reader: JsonReader): BoundingBox {
        var west = Double.NaN
        var east = Double.NaN
        var south = Double.NaN
        var north = Double.NaN
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "west" -> west = reader.nextDouble()
                "east" -> east = reader.nextDouble()
                "south" -> south = reader.nextDouble()
                "north" -> north = reader.nextDouble()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return BoundingBox(west, east, south, north)
    }
}
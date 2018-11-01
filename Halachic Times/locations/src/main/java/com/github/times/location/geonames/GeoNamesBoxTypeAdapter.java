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
package com.github.times.location.geonames;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.geonames.BoundingBox;

import java.io.IOException;

/**
 * JSON type adapter for GeoNames bounding boxes.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesBoxTypeAdapter extends TypeAdapter<BoundingBox> {

    @Override
    public BoundingBox read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return readBoundingBox(in);
    }

    @Override
    public void write(JsonWriter out, BoundingBox value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("west").value(value.getWest());
        out.name("east").value(value.getEast());
        out.name("south").value(value.getSouth());
        out.name("north").value(value.getNorth());
        out.endObject();
    }

    private static BoundingBox readBoundingBox(JsonReader in) throws IOException {
        double west = Double.NaN;
        double east = Double.NaN;
        double south = Double.NaN;
        double north = Double.NaN;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "west":
                    west = in.nextDouble();
                    break;
                case "east":
                    east = in.nextDouble();
                    break;
                case "south":
                    south = in.nextDouble();
                    break;
                case "north":
                    north = in.nextDouble();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new BoundingBox(west, east, south, north);
    }
}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.geonames.Timezone;

import java.io.IOException;
import java.util.Date;

/**
 * JSON type adapter for GeoNames timezones.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesTimezoneAdapter extends TypeAdapter<Timezone> {

    // TODO inject via constructor that created via factory
    private final Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create();

    @Override
    public Timezone read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return readTimezone(in);
    }

    @Override
    public void write(JsonWriter out, Timezone value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("timezoneId").value(value.getTimezoneId());
        out.name("dstOffset").value(value.getDstOffset());
        out.name("gmtOffset").value(value.getGmtOffset());
        out.name("countryCode").value(value.getCountryCode());
        out.name("sunrise").value(writeDate(value.getSunrise()));
        out.name("sunset").value(writeDate(value.getSunset()));
        out.name("time").value(writeDate(value.getTime()));
        out.endObject();
    }

    private Timezone readTimezone(JsonReader in) throws IOException {
        Timezone timezone = new Timezone();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "timezoneId":
                    timezone.setTimezoneId(in.nextString());
                    break;
                case "dstOffset":
                    timezone.setDstOffset(in.nextDouble());
                    break;
                case "gmtOffset":
                    timezone.setGmtOffset(in.nextDouble());
                    break;
                case "countryCode":
                    timezone.setCountryCode(in.nextString());
                    break;
                case "sunrise":
                    timezone.setSunrise(readDate(in));
                    break;
                case "sunset":
                    timezone.setSunset(readDate(in));
                    break;
                case "time":
                    timezone.setTime(readDate(in));
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return timezone;
    }

    private Date readDate(JsonReader in) throws IOException {
        return gson.getAdapter(Date.class).read(in);
    }

    private String writeDate(Date value) {
        return gson.getAdapter(Date.class).toJson(value);
    }
}

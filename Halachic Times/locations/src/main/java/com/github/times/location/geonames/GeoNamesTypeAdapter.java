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

import org.geonames.BoundingBox;
import org.geonames.FeatureClass;
import org.geonames.InsufficientStyleException;
import org.geonames.Timezone;

import java.io.IOException;

/**
 * JSON type adapter for GeoNames toponyms.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesTypeAdapter extends TypeAdapter<GeoNamesRecord> {

    // TODO inject via constructor that created via factory
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(BoundingBox.class, new GeoNamesBoxTypeAdapter())
        .registerTypeAdapter(Timezone.class, new GeoNamesTimezoneAdapter())
        .create();

    @Override
    public GeoNamesRecord read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return readToponym(in);
    }

    @Override
    public void write(JsonWriter out, GeoNamesRecord value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("name").value(value.getName());
        out.name("lat").value(value.getLatitude());
        out.name("lng").value(value.getLongitude());
        out.name("geonameId").value(value.getGeoNameId());
        out.name("countryCode").value(value.getCountryCode());
        out.name("countryName").value(value.getCountryName());
        out.name("fcl").value(value.getFeatureClass().name());
        out.name("fcode").value(value.getFeatureCode());
        out.name("fclName").value(value.getFeatureClassName());
        out.name("fCodeName").value(value.getFeatureCodeName());
        try {
            out.name("alternateNames").value(value.getAlternateNames());
            out.name("continentCode").value(value.getContinentCode());
            out.name("population").value(value.getPopulation());
            out.name("elevation").value(value.getElevation());
            out.name("adminCode1").value(value.getAdminCode1());
            out.name("adminName1").value(value.getAdminName1());
            out.name("adminCode2").value(value.getAdminCode2());
            out.name("adminName2").value(value.getAdminName2());
            out.name("adminCode3").value(value.getAdminCode3());
            out.name("adminCode4").value(value.getAdminCode4());
            //TODO out.name( "timezone").value(value.getTimezone());
            //TODO out.name( "bbox").value(value.getBoundingBox());
        } catch (InsufficientStyleException e) {
        }
        out.name("adminName3").value(value.getAdminName3());
        out.name("adminName4").value(value.getAdminName4());
        out.name("adminCode5").value(value.getAdminCode5());
        out.name("adminName5").value(value.getAdminName5());
        out.endObject();
    }

    private GeoNamesRecord readToponym(JsonReader in) throws IOException {
        GeoNamesRecord toponym = new GeoNamesRecord();

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "name":
                    toponym.setName(in.nextString());
                    break;
                case "alternateNames":
                    toponym.setAlternateNames(in.nextString());
                    break;
                case "lat":
                    toponym.setLatitude(in.nextDouble());
                    break;
                case "lng":
                    toponym.setLongitude(in.nextDouble());
                    break;
                case "geonameId":
                    toponym.setGeoNameId(in.nextInt());
                    break;
                case "continentCode":
                    toponym.setContinentCode(in.nextString());
                    break;
                case "countryCode":
                    toponym.setCountryCode(in.nextString());
                    break;
                case "countryName":
                    toponym.setCountryName(in.nextString());
                    break;
                case "fcl":
                    toponym.setFeatureClass(FeatureClass.fromValue(in.nextString()));
                    break;
                case "fcode":
                    toponym.setFeatureCode(in.nextString());
                    break;
                case "fclName":
                    toponym.setFeatureClassName(in.nextString());
                    break;
                case "fCodeName":
                    toponym.setFeatureCodeName(in.nextString());
                    break;
                case "population":
                    toponym.setPopulation(in.nextLong());
                    break;
                case "elevation":
                    toponym.setElevation(in.nextInt());
                    break;
                case "adminCode1":
                    toponym.setAdminCode1(in.nextString());
                    break;
                case "adminName1":
                    toponym.setAdminName1(in.nextString());
                    break;
                case "adminCode2":
                    toponym.setAdminCode2(in.nextString());
                    break;
                case "adminName2":
                    toponym.setAdminName2(in.nextString());
                    break;
                case "adminCode3":
                    toponym.setAdminCode3(in.nextString());
                    break;
                case "adminName3":
                    toponym.setAdminName3(in.nextString());
                    break;
                case "adminCode4":
                    toponym.setAdminCode4(in.nextString());
                    break;
                case "adminName4":
                    toponym.setAdminName4(in.nextString());
                    break;
                case "adminCode5":
                    toponym.setAdminCode5(in.nextString());
                    break;
                case "adminName5":
                    toponym.setAdminName5(in.nextString());
                    break;
                case "timezone":
                    toponym.setTimezone(readTimezone(in));
                    break;
                case "bbox":
                    toponym.setBoundingBox(readBoundingBox(in));
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return toponym;
    }

    private BoundingBox readBoundingBox(JsonReader in) throws IOException {
        return gson.getAdapter(BoundingBox.class).read(in);
    }

    private Timezone readTimezone(JsonReader in) throws IOException {
        return gson.getAdapter(Timezone.class).read(in);
    }
}

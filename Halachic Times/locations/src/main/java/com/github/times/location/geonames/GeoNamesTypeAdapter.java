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

import org.geonames.FeatureClass;

import java.io.IOException;

/**
 * JSON type adapter for GeoNames toponyms.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesTypeAdapter extends TypeAdapter<GeoNamesRecord> {
    @Override
    public GeoNamesRecord read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return getToponymFromElement(in);
    }

    @Override
    public void write(JsonWriter out, GeoNamesRecord value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("name").value(value.getName());
        out.endObject();
    }

    private static GeoNamesRecord getToponymFromElement(JsonReader in) throws IOException {
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
                    in.beginObject();
                    in.skipValue();
//                Element timezoneElement = toponymElement.getChild();
//                if (timezoneElement != null) {
//                    Timezone timezone = new Timezone();
//                    timezone.setTimezoneId(timezoneElement.getValue());
//                    timezone.setDstOffset(Double.parseDouble(timezoneElement.getAttributeValue("dstOffset")));
//                    timezone.setGmtOffset(Double.parseDouble(timezoneElement.getAttributeValue("gmtOffset")));
//                    toponym.setTimezone(timezone);
//                }
                    in.endObject();
                    break;
                case "bbox":
                    in.beginObject();
                    in.skipValue();
//                Element bboxElement = toponymElement.getChild();
//                if (bboxElement != null) {
//                    BoundingBox boundingBox = new BoundingBox(
//                        Double.parseDouble(bboxElement.getChildText("west")),
//                        Double.parseDouble(bboxElement.getChildText("east")),
//                        Double.parseDouble(bboxElement.getChildText("south")),
//                        Double.parseDouble(bboxElement.getChildText("north")));
//                    toponym.setBoundingBox(boundingBox);
//                }
                    in.endObject();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return toponym;
    }
}

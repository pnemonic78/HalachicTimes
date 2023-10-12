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

import com.google.gson.annotations.SerializedName;

import org.geonames.BoundingBox;
import org.geonames.FeatureClass;
import org.geonames.Timezone;

/**
 * GeoNames toponym.
 *
 * @author Moshe Waisberg
 */
public class Toponym {
    @SerializedName("geonameId")
    public int geoNameId;
    @SerializedName("name")
    public String name;
    @SerializedName("alternateNames")
    public String alternateNames;
    @SerializedName("continentCode")
    public String continentCode;
    @SerializedName("countryCode")
    public String countryCode;
    @SerializedName("countryName")
    public String countryName;
    @SerializedName("population")
    public Long population;
    @SerializedName("elevation")
    public Integer elevation;
    @SerializedName("fcl")
    public FeatureClass featureClass;
    @SerializedName("fclName")
    public String featureClassName;
    @SerializedName("fcode")
    public String featureCode;
    @SerializedName("fCodeName")
    public String featureCodeName;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("lng")
    public double longitude;
    @SerializedName("adminCode1")
    public String adminCode1;
    @SerializedName("adminName1")
    public String adminName1;
    @SerializedName("adminCode2")
    public String adminCode2;
    @SerializedName("adminName2")
    public String adminName2;
    @SerializedName("adminCode3")
    public String adminCode3;
    @SerializedName("adminName3")
    public String adminName3;
    @SerializedName("adminCode4")
    public String adminCode4;
    @SerializedName("adminName4")
    public String adminName4;
    @SerializedName("adminCode5")
    public String adminCode5;
    @SerializedName("adminName5")
    public String adminName5;
    @SerializedName("timezone")
    public Timezone timezone;
    @SerializedName("bbox")
    public BoundingBox boundingBox;
}

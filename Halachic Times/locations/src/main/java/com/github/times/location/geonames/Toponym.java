package com.github.times.location.geonames;

import com.google.gson.annotations.SerializedName;

import org.geonames.BoundingBox;
import org.geonames.FeatureClass;
import org.geonames.Timezone;

/**
 * @author moshe on 2018/10/27.
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

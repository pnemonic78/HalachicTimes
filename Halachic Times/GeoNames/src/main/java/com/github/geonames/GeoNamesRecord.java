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
package com.github.geonames;

import static com.github.geonames.CountryInfo.ISO639_ISRAEL;
import static com.github.geonames.CountryInfo.ISO639_PALESTINE;

/**
 * GeoNames data record.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesRecord {

    private long geonameId;
    private String name;
    private String asciiName;
    private String alternateNames;
    private double latitude;
    private double longitude;
    private String featureClass;
    private String featureCode;
    private String countryCode;
    private String cc2;
    private String adminCode1;
    private String adminCode2;
    private String adminCode3;
    private String adminCode4;
    private String adminCode5;
    private long population;
    private int elevation = Integer.MIN_VALUE;
    private int dem = Integer.MIN_VALUE;
    private String timezone;
    private String modification;

    /**
     * Get the geoname id.
     *
     * @return the id.
     */
    public long getGeoNameId() {
        return geonameId;
    }

    /**
     * Set the geoname id.
     *
     * @param geonameId
     *         the id.
     */
    public void setGeoNameId(long geonameId) {
        this.geonameId = geonameId;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name
     *         the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the ASCII name.
     *
     * @return the name.
     */
    public String getAsciiName() {
        return asciiName;
    }

    /**
     * Set the ASCII name.
     *
     * @param asciiName
     *         the name.
     */
    public void setAsciiName(String asciiName) {
        this.asciiName = asciiName;
    }

    /**
     * Get the alternate names.
     *
     * @return the alternateNames
     */
    public String getAlternateNames() {
        return alternateNames;
    }

    /**
     * Set the alternate names.
     *
     * @param alternateNames
     *         the alternate names.
     */
    public void setAlternateNames(String alternateNames) {
        this.alternateNames = alternateNames;
    }

    /**
     * Get the latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude.
     *
     * @param latitude
     *         the latitude.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude.
     *
     * @param longitude
     *         the longitude.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the featureClass.
     *
     * @return the featureClass
     */
    public String getFeatureClass() {
        return featureClass;
    }

    /**
     * Set the featureClass.
     *
     * @param featureClass
     *         the featureClass.
     */
    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    /**
     * Get the featureCode.
     *
     * @return the featureCode
     */
    public String getFeatureCode() {
        return featureCode;
    }

    /**
     * Set the featureCode.
     *
     * @param featureCode
     *         the featureCode.
     */
    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    /**
     * Get the countryCode.
     *
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Set the countryCode.
     *
     * @param countryCode
     *         the countryCode.
     */
    public void setCountryCode(String countryCode) {
        if (ISO639_PALESTINE.equals(countryCode)) {
            countryCode = ISO639_ISRAEL;
        }
        this.countryCode = countryCode;
    }

    /**
     * Get the alternate country codes.
     *
     * @return the comma-separated list of codes.
     */
    public String getAlternateCountryCodes() {
        return cc2;
    }

    /**
     * Set the alternate country codes.
     *
     * @param cc2
     *         the the comma-separated list of codes.
     */
    public void setAlternateCountryCodes(String cc2) {
        this.cc2 = cc2;
    }

    /**
     * Get the admin 1 code.
     *
     * @return the code.
     */
    public String getAdminCode1() {
        return adminCode1;
    }

    /**
     * Set the admin 1 code.
     *
     * @param code
     *         the code.
     */
    public void setAdminCode1(String code) {
        this.adminCode1 = code;
    }

    /**
     * Get the admin 2 code.
     *
     * @return the code.
     */
    public String getAdminCode2() {
        return adminCode2;
    }

    /**
     * Set the admin 2 code.
     *
     * @param code
     *         the code.
     */
    public void setAdminCode2(String code) {
        this.adminCode2 = code;
    }

    /**
     * Get the admin 3 code.
     *
     * @return the code.
     */
    public String getAdminCode3() {
        return adminCode3;
    }

    /**
     * Set the admin 3 code.
     *
     * @param code
     *         the code.
     */
    public void setAdminCode3(String code) {
        this.adminCode3 = code;
    }

    /**
     * Get the admin 4 code.
     *
     * @return the code.
     */
    public String getAdminCode4() {
        return adminCode4;
    }

    /**
     * Set the admin 5 code.
     *
     * @param code
     *         the code.
     */
    public void setAdminCode4(String code) {
        this.adminCode4 = code;
    }

    /**
     * Get the admin 5 code.
     *
     * @return the code.
     */
    public String getAdminCode5() {
        return adminCode5;
    }

    /**
     * Set the admin 4 code.
     *
     * @param code
     *         the code.
     */
    public void setAdminCode5(String code) {
        this.adminCode5 = code;
    }

    /**
     * Get the population.
     *
     * @return the population
     */
    public long getPopulation() {
        return population;
    }

    /**
     * Set the population.
     *
     * @param population
     *         the population.
     */
    public void setPopulation(long population) {
        this.population = population;
    }

    /**
     * Get the elevation.
     *
     * @return the elevation
     */
    public int getElevation() {
        return elevation;
    }

    /**
     * Set the elevation.
     *
     * @param elevation
     *         the elevation.
     */
    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    /**
     * Get the SRTM3 elevation.
     *
     * @return the elevation.
     */
    public int getDigitalElevation() {
        return dem;
    }

    /**
     * Set the SRTM3 elevation.
     *
     * @param dem
     *         the elevation.
     */
    public void setDigitalElevation(int dem) {
        this.dem = dem;
    }

    /**
     * Get the timezone.
     *
     * @return the timezone
     */
    public String getTimeZone() {
        return timezone;
    }

    /**
     * Set the timezone.
     *
     * @param timezone
     *         the timezone.
     */
    public void setTimeZone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Get the modification.
     *
     * @return the modification
     */
    public String getModification() {
        return modification;
    }

    /**
     * Set the modification.
     *
     * @param modification
     *         the modification.
     */
    public void setModification(String modification) {
        this.modification = modification;
    }

    @Override
    public int hashCode() {
        return (int) getGeoNameId();
    }

    public int getGrossElevation() {
        int elevation = getElevation();
        if (elevation == Integer.MIN_VALUE) {
            elevation = getDigitalElevation();
        }
        return elevation;
    }
}

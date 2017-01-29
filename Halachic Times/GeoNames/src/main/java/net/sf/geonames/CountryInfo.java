/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.geonames;

import java.util.List;

/**
 * Countries.
 *
 * @author Moshe Waisberg
 */
public class CountryInfo {

    /** ISO 639 country code for Israel. */
    public static final String ISO639_ISRAEL = "IL";
    /** ISO 639 country code for Palestine. */
    public static final String ISO639_PALESTINE = "PS";

    private String iso;
    private String iso3;
    private int isoNumeric;
    private String fips;
    private String country;
    private String capital;
    private double area;
    private long population;
    private String continent;
    private String tld;
    private String currencyCode;
    private String currencyName;
    private String phone;
    private String postalCodeFormat;
    private String postalCodeRegex;
    private List<String> languages;
    private long geoNameId;
    private List<String> neighbours;
    private String equivalentFipsCode;

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getEquivalentFipsCode() {
        return equivalentFipsCode;
    }

    public void setEquivalentFipsCode(String equivalentFipsCode) {
        this.equivalentFipsCode = equivalentFipsCode;
    }

    public String getFips() {
        return fips;
    }

    public void setFips(String fips) {
        this.fips = fips;
    }

    public long getGeoNameId() {
        return geoNameId;
    }

    public void setGeoNameId(long geoNameId) {
        this.geoNameId = geoNameId;
    }

    public String getIso3() {
        return iso3;
    }

    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public int getIsoNumeric() {
        return isoNumeric;
    }

    public void setIsoNumeric(int isoNumeric) {
        this.isoNumeric = isoNumeric;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<String> neighbours) {
        this.neighbours = neighbours;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public String getPostalCodeFormat() {
        return postalCodeFormat;
    }

    public void setPostalCodeFormat(String postalCodeFormat) {
        this.postalCodeFormat = postalCodeFormat;
    }

    public String getPostalCodeRegex() {
        return postalCodeRegex;
    }

    public void setPostalCodeRegex(String postalCodeRegex) {
        this.postalCodeRegex = postalCodeRegex;
    }

    public String getTld() {
        return tld;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }
}

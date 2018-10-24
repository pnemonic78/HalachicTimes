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

import org.geonames.InsufficientStyleException;
import org.geonames.Toponym;

import static com.github.geonames.CountryInfo.ISO639_ISRAEL;
import static com.github.geonames.CountryInfo.ISO639_PALESTINE;

/**
 * GeoNames data record.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesRecord extends Toponym {

    private String asciiName;
    private String cc2;
    private int dem = Integer.MIN_VALUE;
    private String modification;

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
     * @param asciiName the name.
     */
    public void setAsciiName(String asciiName) {
        this.asciiName = asciiName;
    }

    /**
     * Set the countryCode.
     *
     * @param countryCode the countryCode.
     */
    @Override
    public void setCountryCode(String countryCode) {
        if (ISO639_PALESTINE.equals(countryCode)) {
            countryCode = ISO639_ISRAEL;
        }
        super.setCountryCode(countryCode);
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
     * @param cc2 the the comma-separated list of codes.
     */
    public void setAlternateCountryCodes(String cc2) {
        this.cc2 = cc2;
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
     * @param dem the elevation.
     */
    public void setDigitalElevation(int dem) {
        this.dem = dem;
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
     * @param modification the modification.
     */
    public void setModification(String modification) {
        this.modification = modification;
    }

    @Override
    public int hashCode() {
        return getGeoNameId();
    }

    public Integer getGrossElevation() {
        Integer elevation = null;
        try {
            elevation = getElevation();
        } catch (InsufficientStyleException e) {
            e.printStackTrace();
        }
        if (elevation == null) {
            elevation = getDigitalElevation();
        }
        return elevation;
    }
}

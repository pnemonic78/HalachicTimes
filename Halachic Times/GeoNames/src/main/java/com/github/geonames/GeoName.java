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

import com.github.geonames.util.LocaleUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * GeoName POJO.
 *
 * @author Moshe Waisberg
 */
public class GeoName extends GeoNameRecord {

    private BoundingBox bbox;
    private String countryId;
    private String countryName;
    private String adminId1;
    private String continentCode;
    private String toponymName;
    private String wikipediaURL;
    private String adminName1;
    private String adminName2;
    private String adminName3;
    private String adminName4;
    private String adminName5;
    private final Map<String, AlternateName> alternateNamesMap = new HashMap<>();
    private String fclName;
    private String fcodeName;

    public Map<String, AlternateName> getAlternateNamesMap() {
        return alternateNamesMap;
    }

    public void setAlternateNames(Map<String, AlternateName> alternateNames) {
        this.alternateNamesMap.clear();
        if (alternateNamesMap != null) {
            this.alternateNamesMap.putAll(alternateNamesMap);
        }
    }

    public void setAlternateNames(Collection<AlternateName> alternateNames) {
        this.alternateNamesMap.clear();
        if (alternateNamesMap != null) {
            for (AlternateName name : alternateNames) {
                this.alternateNamesMap.put(name.getName(), name);
            }
        }
    }

    public BoundingBox getBounds() {
        return bbox;
    }

    public void setBounds(BoundingBox bbox) {
        this.bbox = bbox;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getAdminId1() {
        return adminId1;
    }

    public void setAdminId1(String adminId1) {
        this.adminId1 = adminId1;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    public String getToponymName() {
        return toponymName;
    }

    public void setToponymName(String toponymName) {
        this.toponymName = toponymName;
    }

    public String getWikipediaURL() {
        return wikipediaURL;
    }

    public void setWikipediaURL(String wikipediaURL) {
        this.wikipediaURL = wikipediaURL;
    }

    public String getAdminName1() {
        return adminName1;
    }

    public void setAdminName1(String adminName1) {
        this.adminName1 = adminName1;
    }

    public String getAdminName2() {
        return adminName2;
    }

    public void setAdminName2(String adminName2) {
        this.adminName2 = adminName2;
    }

    public String getAdminName3() {
        return adminName3;
    }

    public void setAdminName3(String adminName3) {
        this.adminName3 = adminName3;
    }

    public String getAdminName4() {
        return adminName4;
    }

    public void setAdminName4(String adminName4) {
        this.adminName4 = adminName4;
    }

    public String getAdminName5() {
        return adminName5;
    }

    public void setAdminName5(String adminName5) {
        this.adminName5 = adminName5;
    }

    public String getFeatureClassName() {
        return fclName;
    }

    public void setFeatureClassName(String fclName) {
        this.fclName = fclName;
    }

    public String getFeatureCodeName() {
        return fcodeName;
    }

    public void setFeatureCodeName(String fcodeName) {
        this.fcodeName = fcodeName;
    }

    public void putAlternateName(long geonameId, String language, String name) {
        putAlternateName(geonameId, language, name, false, false, false, false);
    }

    public void putAlternateName(long geonameId, String language, String name, boolean preferred, boolean shortName, boolean colloquial, boolean historic) {
        String languageCode = LocaleUtils.toLanguageCode(language);
        String languageCodeISO = LocaleUtils.getISOLanguage(languageCode);
        AlternateName alternateName = alternateNamesMap.get(languageCodeISO);
        if ((alternateName == null) || preferred) {
            alternateName = new AlternateName(language, name, preferred, shortName, colloquial, historic);
            alternateNamesMap.put(languageCodeISO, alternateName);
        } else if (!alternateName.isPreferred()) {
            if ((alternateName.isShortName() && !shortName)
                    || (alternateName.isColloquial() && !colloquial)
                    || (alternateName.isHistoric() && !historic)) {
                alternateName = new AlternateName(language, name, preferred, shortName, colloquial, historic);
                alternateNamesMap.put(languageCodeISO, alternateName);
            } else if (!historic && !colloquial && (languageCode.length() <= alternateName.getLanguage().length())) {
                alternateName = new AlternateName(language, name, preferred, shortName, colloquial, historic);
                alternateNamesMap.put(languageCodeISO, alternateName);
            } else {
                System.err.println("Duplicate name! id: " + geonameId + " language: " + language + " name: [" + name + "]");
            }
        } else {
            System.err.println("Duplicate name! id: " + geonameId + " language: " + language + " name: [" + name + "]");
        }
    }

    public String getBestName(String language) {
        String name = getName(language);
        return name != null ? name : getName();
    }

    public String getName(String language) {
        String languageCode = LocaleUtils.toLanguageCode(language);
        AlternateName alternateName = getAlternateNamesMap().get(languageCode);
        if (alternateName != null) {
            return alternateName.getName();
        }
        if (language == null) {
            return getName();
        }
        return null;
    }
}

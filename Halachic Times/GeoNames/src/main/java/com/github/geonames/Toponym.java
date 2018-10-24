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
 * GeoNames toponym POJO.
 *
 * @author Moshe Waisberg
 */
public class Toponym extends GeoNamesRecord {

    private String wikipediaURL;
    private final Map<String, AlternateName> alternateNamesMap = new HashMap<>();

    public Map<String, AlternateName> getAlternateNamesMap() {
        return alternateNamesMap;
    }

    public void setAlternateNames(Map<String, AlternateName> alternateNames) {
        this.alternateNamesMap.clear();
        if (alternateNames != null) {
            this.alternateNamesMap.putAll(alternateNames);
        }
    }

    public void setAlternateNames(Collection<AlternateName> alternateNames) {
        this.alternateNamesMap.clear();
        if (alternateNames != null) {
            for (AlternateName name : alternateNames) {
                this.alternateNamesMap.put(name.getName(), name);
            }
        }
    }

    public String getWikipediaURL() {
        return wikipediaURL;
    }

    public void setWikipediaURL(String wikipediaURL) {
        this.wikipediaURL = wikipediaURL;
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

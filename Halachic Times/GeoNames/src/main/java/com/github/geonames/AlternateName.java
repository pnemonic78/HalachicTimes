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

import java.util.Locale;

/**
 * Alternate name.
 *
 * @author Moshe Waisberg
 */
public class AlternateName {

    private String name;
    private String languageCode;
    private Locale locale;
    private boolean preferred;
    private boolean historic;
    private boolean shortName;
    private boolean colloquial;

    public AlternateName() {
    }

    public AlternateName(String languageCode, String name) {
        this(languageCode, name, false);
    }

    public AlternateName(String languageCode, String name, boolean preferred) {
        this(languageCode, name, preferred, false, false, false);
    }

    public AlternateName(String languageCode, String name, boolean preferred, boolean shortName, boolean colloquial, boolean historic) {
        setLanguage(languageCode);
        setName(name);
        setPreferred(preferred);
        setShortName(shortName);
        setColloquial(colloquial);
        setHistoric(historic);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name required");
        }
        this.name = name;
    }

    public String getLanguage() {
        return languageCode;
    }

    public void setLanguage(String languageCode) {
        if (languageCode == null) {
            throw new IllegalArgumentException("language code required");
        }
        this.languageCode = languageCode;
        this.locale = null;
    }

    /**
     * Get the ISO 639-1 code.
     *
     * @return the language code.
     */
    public String getLanguageISO2() {
        return getLocale().getLanguage();
    }

    public Locale getLocale() {
        if (locale == null) {
            locale = new Locale(getLanguage());
        }
        return locale;
    }

    @Override
    public String toString() {
        return languageCode + ": " + name;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public boolean isHistoric() {
        return historic;
    }

    public void setHistoric(boolean historic) {
        this.historic = historic;
    }

    public boolean isShortName() {
        return shortName;
    }

    public void setShortName(boolean shortName) {
        this.shortName = shortName;
    }

    public boolean isColloquial() {
        return colloquial;
    }

    public void setColloquial(boolean colloquial) {
        this.colloquial = colloquial;
    }
}

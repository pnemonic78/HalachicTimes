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
    private boolean preferred;
    private Locale locale;

    public AlternateName() {
    }

    public AlternateName(String languageCode, String name) {
        this(languageCode, name, false);
    }

    public AlternateName(String languageCode, String name, boolean preferred) {
        setLanguage(languageCode);
        setName(name);
        setPreferred(preferred);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return languageCode;
    }

    public void setLanguage(String languageCode) {
        this.languageCode = languageCode;
        this.locale = null;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    @Override
    public String toString() {
        return languageCode + ": " + name;
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
}

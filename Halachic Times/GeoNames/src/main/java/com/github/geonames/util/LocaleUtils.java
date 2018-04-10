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
package com.github.geonames.util;

import java.util.Locale;

/**
 * Locale utilities.
 *
 * @author Moshe Waisberg
 */
public class LocaleUtils {

    private LocaleUtils() {
    }

    public static String toLanguageCode(String language) {
        String languageCode = language;
        if (languageCode == null) {
            languageCode = Locale.ENGLISH.getLanguage();
        } else {
            Locale locale = new Locale(language);
            languageCode = locale.getLanguage();
            languageCode = getISOLanguage(languageCode);
        }
        return languageCode;
    }

    public static String getISOLanguage(String languageCode) {
        if (languageCode.length() >= 3) {
            switch (languageCode) {
                case "arz":
                case "yhd":
                    return "ar";
                case "ces":
                case "cze":
                    return "cs";
                case "frr":
                case "ger":
                    return "de";
                case "ell":
                case "grc":
                case "gre":
                    return "el";
                case "fas":
                case "per":
                    return "fa";
                case "olo":
                    return "fi";
                case "fra":
                case "fre":
                case "frp":
                    return "fr";
                case "arm":
                case "hye":
                    return "hy";
                case "ice":
                case "isl":
                    return "is";
                case "fur":
                    return "it";
                case "geo":
                case "kat":
                    return "ka";
                case "mac":
                case "mkd":
                    return "mk";
                case "may":
                case "msa":
                    return "ms";
                case "dut":
                case "nld":
                case "zea":
                    return "nl";
                case "ron":
                case "rum":
                    return "ro";
                case "slk":
                case "slo":
                    return "sk";
                case "fil":
                case "ilo":
                case "phi":
                    return "tl";
                case "ota":
                    return "tr";
                case "chi":
                case "czh":
                case "czo":
                case "yue":
                case "zho":
                    return "zh";
            }
        }
        return languageCode;
    }
}

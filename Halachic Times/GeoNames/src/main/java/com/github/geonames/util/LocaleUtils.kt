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
package com.github.geonames.util

/**
 * Locale utilities.
 *
 * @author Moshe Waisberg
 */
object LocaleUtils {
    @JvmStatic
    fun getISOLanguage(languageCode: String): String {
        if (languageCode.length >= 3) {
            return when (languageCode) {
                "arz", "yhd" -> "ar"
                "ces", "cze" -> "cs"
                "frr", "ger" -> "de"
                "ell", "grc", "gre" -> "el"
                "fas", "per" -> "fa"
                "olo" -> "fi"
                "fra", "fre", "frp" -> "fr"
                "arm", "hye" -> "hy"
                "ice", "isl" -> "is"
                "fur" -> "it"
                "geo", "kat" -> "ka"
                "mac", "mkd" -> "mk"
                "may", "msa" -> "ms"
                "dut", "nld", "zea" -> "nl"
                "ron", "rum" -> "ro"
                "slk", "slo" -> "sk"
                "fil", "ilo", "phi" -> "tl"
                "ota" -> "tr"
                "chi", "czh", "czo", "yue", "zho" -> "zh"
                else -> languageCode
            }
        }
        return languageCode
    }

    fun applyAndroid() {
        System.setProperty("java.locale.useOldISOCodes", "true")
    }
}

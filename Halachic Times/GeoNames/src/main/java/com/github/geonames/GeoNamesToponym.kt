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
package com.github.geonames

import com.github.geonames.util.LocaleUtils.getISOLanguage
import com.github.geonames.util.LocaleUtils.toLanguageCode

/**
 * GeoNames toponym POJO.
 *
 * @author Moshe Waisberg
 */
class GeoNamesToponym : GeoNamesRecord() {
    var wikipediaURL: String? = null

    val alternateNamesMap = mutableMapOf<String, AlternateName>()

    fun setAlternateNames(alternateNames: Map<String, AlternateName>?) {
        alternateNamesMap.clear()
        if (alternateNames != null) {
            alternateNamesMap.putAll(alternateNames)
        }
    }

    fun setAlternateNames(alternateNames: Collection<AlternateName>?) {
        alternateNamesMap.clear()
        if (alternateNames != null) {
            for (name in alternateNames) {
                alternateNamesMap[name.name] = name
            }
        }
    }

    fun putAlternateName(
        geonameId: GeoNameId,
        language: String,
        name: String,
        preferred: Boolean = false,
        shortName: Boolean = false,
        colloquial: Boolean = false,
        historic: Boolean = false
    ) {
        val languageCode = toLanguageCode(language)
        val languageCodeISO = getISOLanguage(languageCode)
        var alternateName = alternateNamesMap[languageCodeISO]
        if (alternateName == null || preferred) {
            alternateName =
                AlternateName(language, name, preferred, shortName, colloquial, historic)
            alternateNamesMap[languageCodeISO] = alternateName
        } else if (!alternateName.isPreferred) {
            if (alternateName.isShortName && !shortName || alternateName.isColloquial && !colloquial || alternateName.isHistoric && !historic) {
                alternateName =
                    AlternateName(language, name, preferred, shortName, colloquial, historic)
                alternateNamesMap[languageCodeISO] = alternateName
            } else if (!historic && !colloquial && languageCode.length <= alternateName.language.length) {
                alternateName =
                    AlternateName(language, name, preferred, shortName, colloquial, historic)
                alternateNamesMap[languageCodeISO] = alternateName
            } else {
                System.err.println("Duplicate name! id: $geonameId language: $language name: [$name]")
            }
        } else {
            System.err.println("Duplicate name! id: $geonameId language: $language name: [$name]")
        }
    }

    fun getBestName(language: String?): String {
        return getName(language) ?: name
    }

    fun getName(language: String?): String? {
        val languageCode = toLanguageCode(language)
        val alternateName = alternateNamesMap[languageCode]
        if (alternateName != null) {
            return alternateName.name
        }
        return if (language.isNullOrEmpty()) name else null
    }
}

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

import com.github.util.LocaleUtils.toLanguageCode

/**
 * GeoNames toponym POJO.
 *
 * @author Moshe Waisberg
 */
class GeoNamesToponym : GeoNamesRecord() {

    fun getBestName(language: String?): String {
        val languageCode = toLanguageCode(language)
        val names = alternateNames
            ?.filter { it.language == languageCode }
            ?.sortedByDescending { it.alternateNameId }
            ?: return name
        val prefer =
            names.firstOrNull() { it.isPreferred && !it.isShort && !it.isColloquial && !it.isHistoric }
                ?: names.firstOrNull { it.isPreferred && !it.isColloquial && !it.isHistoric }
                ?: names.firstOrNull { it.isPreferred && !it.isShort && !it.isHistoric }
                ?: names.firstOrNull { it.isPreferred && !it.isHistoric }
                ?: names.firstOrNull { !it.isShort && !it.isColloquial && !it.isHistoric }
                ?: names.firstOrNull { !it.isShort && !it.isHistoric }
                ?: names.firstOrNull { !it.isHistoric }
        return prefer?.name ?: names.firstOrNull()?.name ?: name
    }
}

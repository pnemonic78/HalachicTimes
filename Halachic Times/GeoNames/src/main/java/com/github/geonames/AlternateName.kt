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

import java.util.Locale

/**
 * Alternate name.
 *
 * @author Moshe Waisberg
 */
class AlternateName {

    var name: String = ""
    var locale: Locale = Locale.ENGLISH
    var isPreferred = false
    var isHistoric = false
    var isShortName = false
    var isColloquial = false

    constructor(
        languageCode: String,
        name: String,
        preferred: Boolean = false,
        shortName: Boolean = false,
        colloquial: Boolean = false,
        historic: Boolean = false
    ) {
        this.language = languageCode
        this.name = name
        this.isPreferred = preferred
        this.isShortName = shortName
        this.isColloquial = colloquial
        this.isHistoric = historic
    }

    var language: String
        get() = locale.getISO3Language()
        set(value) {
            locale = Locale(value, locale.country, locale.variant)
        }

    /**
     * The ISO 639-1 code.
     */
    val languageISO2: String
        get() = locale.language

    override fun toString(): String {
        return "$language: $name"
    }
}

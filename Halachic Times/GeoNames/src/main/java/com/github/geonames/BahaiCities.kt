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

import com.github.geonames.dump.NameAlternateNames
import com.github.geonames.dump.NameCities1000
import com.github.geonames.dump.PathAlternateNames
import com.github.geonames.dump.PathCities1000
import com.github.geonames.dump.PathGoogleNames
import java.io.File

/**
 * Bahai cities for Android application resources.
 *
 * @author Moshe Waisberg
 */
class BahaiCities : CompassCities() {
    init {
        moduleName = "compass-bahai"
    }

    companion object {
        private val LANGUAGES = arrayOf(
            null,
            "ar",
            "bg",
            "cs",
            "da",
            "de",
            "el",
            "es",
            "et",
            "fa",
            "fi",
            "fr",
            "he",
            "hi",
            "hu",
            "it",
            "iw",
            "lt",
            "nb",
            "no",
            "nl",
            "pl",
            "pt",
            "ro",
            "ru",
            "sv",
            "tr",
            "uk"
        )

        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) {
            var pathCities = PathCities1000
            var pathNames = PathAlternateNames
            if (args.isNotEmpty()) {
                pathCities = args[0]
                if (args.size > 1) {
                    pathNames = args[1]
                }
            }
            val cities = BahaiCities()
            val names = cities.loadNames(File(pathCities), BahaiCitiesFilter(), NameCities1000)
            if (names.isEmpty()) throw NoSuchElementException()
            cities.populateElevations(names)
            cities.populateAlternateNames(File(pathNames), names, NameAlternateNames)
            val googleNames = cities.javaClass.getResourceAsStream(PathGoogleNames)!!
            cities.populateAlternateNames(googleNames, names)
            LANGUAGES.forEach { lang ->
                cities.writeAndroidXML(names, lang)
            }
        }
    }
}

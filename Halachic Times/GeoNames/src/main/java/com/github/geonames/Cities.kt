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

import com.github.geonames.Features.FEATURE_P
import com.github.geonames.Features.FEATURE_PPL
import com.github.geonames.Features.FEATURE_PPLA
import com.github.geonames.Features.FEATURE_PPLA2
import com.github.geonames.Features.FEATURE_PPLA3
import com.github.geonames.Features.FEATURE_PPLA4
import com.github.geonames.Features.FEATURE_PPLC
import com.github.geonames.Features.FEATURE_PPLF
import com.github.geonames.Features.FEATURE_PPLG
import com.github.geonames.Features.FEATURE_PPLL
import com.github.geonames.Features.FEATURE_PPLQ
import com.github.geonames.Features.FEATURE_PPLR
import com.github.geonames.Features.FEATURE_PPLS
import com.github.geonames.Features.FEATURE_PPLW
import com.github.geonames.Features.FEATURE_PPLX
import com.github.geonames.Features.FEATURE_STLMT
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.TreeMap
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.geonames.InsufficientStyleException
import org.w3c.dom.Element

/**
 * Cities.
 *
 * @author Moshe Waisberg
 */
open class Cities {
    protected val geoNames = GeoNames()
    var moduleName: String? = null
    protected val modulePath: File
        get() = File(moduleName, APP_RES)

    /**
     * Load the list of names.
     *
     * @param file       the geonames CSV file.
     * @param filter     the name filter.
     * @param zippedName the zipped name.
     * @return the sorted list of records.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun loadNames(
        file: File,
        filter: NameFilter?,
        zippedName: String? = null
    ): Collection<GeoNamesToponym> {
        return geoNames.parseTabbed(file, filter, zippedName)
    }

    /**
     * Filter the list of names to find only capital (or the next-best-to-capital) cities.
     *
     * @param names the list of cites.
     * @return the list of capitals.
     */
    @Throws(InsufficientStyleException::class)
    fun filterCapitals(names: Collection<GeoNamesToponym>): Collection<GeoNamesToponym> {
        val capitals = mutableListOf<GeoNamesToponym>()
        val countries = countries
        for (name in names) {
            if (FEATURE_PPLC == name.featureCode) {
                capitals.add(name)
                countries.remove(name.countryCode)
            }
        }

        // For all countries without capitals, find the next best matching city type.
        if (!countries.isEmpty()) {
            val best: MutableMap<String, GeoNamesToponym> = TreeMap()
            var place: GeoNamesToponym?
            var cc: String
            for (name in names) {
                cc = name.countryCode
                if (countries.contains(cc)) {
                    place = best[cc]
                    if (place == null) {
                        best[cc] = name
                        continue
                    }
                    place = betterPlace(name, place)
                    best[cc] = place
                }
            }
            capitals.addAll(best.values)
        }
        return capitals
    }

    /**
     * Get the better place by comparing its feature type as being more
     * populated.
     *
     * @param name1 a name.
     * @param name2 a name.
     * @return the better name.
     */
    @Throws(InsufficientStyleException::class)
    private fun betterPlace(name1: GeoNamesToponym, name2: GeoNamesToponym): GeoNamesToponym {
        // Compare features.
        val feature1 = name1.featureCode
        val feature2 = name2.featureCode
        val rank1 = getFeatureCodeRank(feature1)
        val rank2 = getFeatureCodeRank(feature2)
        val rankCompare = rank1.compareTo(rank2)
        if (rankCompare > 0) return name1
        if (rankCompare < 0) return name2

        // Compare populations.
        val pop1 = name1.population
        val pop2 = name2.population
        val popCompare = pop1.compareTo(pop2)
        return if (popCompare >= 0) name1 else name2
    }

    private val ranks: MutableMap<String, Int> = TreeMap()

    init {
        moduleName = "locations"
    }

    /**
     * Get the rank of the feature code.
     *
     * @param code the feature code.
     * @return the rank.
     */
    private fun getFeatureCodeRank(code: String): Int {
        if (ranks.isEmpty()) {
            var rank = -2
            ranks[FEATURE_PPLW] = rank++
            ranks[FEATURE_PPLQ] = rank++
            ranks[FEATURE_P] = rank++
            ranks[FEATURE_PPLX] = rank++
            ranks[FEATURE_PPL] = rank++
            ranks[FEATURE_PPLS] = rank++
            ranks[FEATURE_PPLL] = rank++
            ranks[FEATURE_PPLF] = rank++
            ranks[FEATURE_PPLR] = rank++
            ranks[FEATURE_STLMT] = rank++
            ranks[FEATURE_PPLA4] = rank++
            ranks[FEATURE_PPLA3] = rank++
            ranks[FEATURE_PPLA2] = rank++
            ranks[FEATURE_PPLA] = rank++
            ranks[FEATURE_PPLG] = rank++
            ranks[FEATURE_PPLC] = rank++
        }
        return ranks[code]!!
    }

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param names    the list of names.
     * @param language the language code.
     * @throws ParserConfigurationException if a DOM error occurs.
     * @throws TransformerException         if a DOM error occurs.
     */
    @Throws(
        ParserConfigurationException::class,
        TransformerException::class,
        InsufficientStyleException::class
    )
    open fun writeAndroidXML(names: Collection<GeoNamesToponym>, language: String?) {
        val sorted: List<GeoNamesToponym> = names.toList().sortedWith(LocationComparator())

        val builderFactory = DocumentBuilderFactory.newInstance()
        val builder = builderFactory.newDocumentBuilder()
        val doc = builder.newDocument()

        val resources = doc.createElement(ANDROID_ELEMENT_RESOURCES)
        resources.appendChild(doc.createComment(HEADER))
        doc.appendChild(resources)

        val citiesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY)
        citiesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities")
        resources.appendChild(citiesElement)

        val countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY)
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries")
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(countriesElement)

        val latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes")
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(latitudesElement)

        val longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes")
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(longitudesElement)

        val zonesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY)
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "time_zones")
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(zonesElement)

        var country: Element
        var latitude: Element
        var longitude: Element
        var zone: Element
        for (place in sorted) {
            country = doc.createElement(ANDROID_ELEMENT_ITEM)
            country.textContent = place.countryCode
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM)
            latitude.textContent = (place.latitude * CountryRegion.FACTOR_TO_INT).toInt().toString()
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM)
            longitude.textContent =
                (place.longitude * CountryRegion.FACTOR_TO_INT).toInt().toString()
            zone = doc.createElement(ANDROID_ELEMENT_ITEM)
            zone.textContent = place.timezone.timezoneId
            countriesElement.appendChild(country)
            latitudesElement.appendChild(latitude)
            longitudesElement.appendChild(longitude)
            zonesElement.appendChild(zone)
        }
        val file: File = if (language.isNullOrEmpty())
            File(modulePath, "values/cities.xml")
        else
            File(modulePath, "values-$language/cities.xml")
        file.parentFile.mkdirs()

        val src: Source = DOMSource(doc)
        val result: Result = StreamResult(file)
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(S_KEY_INDENT_AMOUNT, "4")
        transformer.transform(src, result)
    }

    protected val countries: MutableCollection<String> = Locale.getISOCountries().toMutableSet()

    fun populateElevations(records: Collection<GeoNamesToponym>) {
        geoNames.populateElevations(records)
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param file       the alternate names file.
     * @param records    the list of records to populate.
     * @param zippedName the zipped file name.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun populateAlternateNames(
        file: File,
        records: Collection<GeoNamesToponym>,
        zippedName: String? = null
    ) {
        geoNames.populateAlternateNames(file = file, records = records, zippedName = zippedName)
    }

    /**
     * Populate the list of names with alternate names.
     *
     * @param input      the alternate names input.
     * @param records    the list of records to populate.
     * @param zippedName the zipped file name.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun populateAlternateNames(
        input: InputStream,
        records: Collection<GeoNamesToponym>,
        zippedName: String? = null
    ) {
        geoNames.populateAlternateNames(input = input, records = records, zippedName = zippedName)
    }

    companion object {
        const val ANDROID_ATTRIBUTE_NAME = "name"
        const val ANDROID_ATTRIBUTE_TRANSLATABLE = "translatable"
        const val ANDROID_ELEMENT_RESOURCES = "resources"
        const val ANDROID_ELEMENT_STRING_ARRAY = "string-array"
        const val ANDROID_ELEMENT_INTEGER_ARRAY = "integer-array"
        const val ANDROID_ELEMENT_ITEM = "item"
        const val HEADER = "Generated from geonames.org data"
        const val S_KEY_INDENT_AMOUNT = OutputPropertiesFactory.S_KEY_INDENT_AMOUNT
        private const val APP_RES = "/src/main/res"
    }
}

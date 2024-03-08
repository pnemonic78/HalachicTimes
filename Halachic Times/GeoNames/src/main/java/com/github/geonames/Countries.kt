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

import com.github.geonames.CountryRegion.Companion.VERTICES_COUNT
import com.github.geonames.CountryRegion.Companion.toRegion
import com.github.geonames.dump.NameShapesLow
import com.github.geonames.dump.PathCountryInfo
import com.github.geonames.dump.PathShapesLow
import java.io.File
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.geonames.GeoNameId
import org.w3c.dom.Element

/**
 * Countries.
 *
 * @author Moshe Waisberg
 */
class Countries : Cities() {
    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param countries the list of countries.
     * @throws ParserConfigurationException if a DOM error occurs.
     * @throws TransformerException if a DOM error occurs.
     */
    @Throws(ParserConfigurationException::class, TransformerException::class)
    fun writeAndroidXML(countries: Collection<CountryRegion>) {
        val sorted: List<CountryRegion> = countries.toList().sortedWith(RegionComparator())

        val builderFactory = DocumentBuilderFactory.newInstance()
        val builder = builderFactory.newDocumentBuilder()
        val doc = builder.newDocument()

        val resources = doc.createElement(ANDROID_ELEMENT_RESOURCES)
        resources.appendChild(doc.createComment(HEADER))
        doc.appendChild(resources)

        val countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY)
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries")
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        resources.appendChild(countriesElement)

        val verticesCountElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "vertices_count")
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        resources.appendChild(verticesCountElement)

        val latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes")
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        resources.appendChild(latitudesElement)

        val longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes")
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        resources.appendChild(longitudesElement)

        var country: Element
        var latitude: Element
        var longitude: Element
        var verticesCount: Element
        var pointIndexes: IntArray
        var pointIndex: Int
        var pointCount: Int

        for (region in sorted) {
            pointIndexes = region.findMainVertices(region.boundary, VERTICES_COUNT)

            country = doc.createElement(ANDROID_ELEMENT_ITEM)
            country.textContent = region.countryCode
            countriesElement.appendChild(country)

            pointCount = 0
            for (i in 0 until VERTICES_COUNT) {
                pointIndex = pointIndexes[i]
                if (pointIndex < 0) continue

                latitude = doc.createElement(ANDROID_ELEMENT_ITEM)
                latitude.textContent = region.boundary.ypoints[pointIndex].toString()
                latitudesElement.appendChild(latitude)

                longitude = doc.createElement(ANDROID_ELEMENT_ITEM)
                longitude.textContent = region.boundary.xpoints[pointIndex].toString()
                longitudesElement.appendChild(longitude)

                pointCount++
            }
            verticesCount = doc.createElement(ANDROID_ELEMENT_ITEM)
            verticesCount.textContent = pointCount.toString()
            verticesCountElement.appendChild(verticesCount)
        }

        val file = File(modulePath, "values/countries.xml")
        file.parentFile.mkdirs()

        val src: Source = DOMSource(doc)
        val result: Result = StreamResult(file)
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(S_KEY_INDENT_AMOUNT, "4")
        transformer.transform(src, result)
    }

    /**
     * Load the list of countries.
     *
     * @param file the country info file.
     * @return the list of records.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun loadInfo(file: File): Collection<CountryInfo> {
        return geoNames.parseCountries(file)
    }

    /**
     * Load the list of shapes.
     *
     * @param file the shapes file.
     * @param zippedName the zipped file name.
     * @return the list of records.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun loadShapes(file: File, zippedName: String? = null): Collection<GeoShape> {
        return geoNames.parseShapes(file, zippedName)
    }

    /**
     * Transform the list of countries to a list of country regions.
     *
     * @param names the list of countries.
     * @param shapes the list of country shapes.
     * @return the list of regions.
     */
    @Throws(IOException::class)
    fun toRegions(
        names: Collection<CountryInfo>,
        shapes: Collection<GeoShape>
    ): Collection<CountryRegion> {
        val regions = mutableListOf<CountryRegion>()
        var geoNameId: GeoNameId
        val shapesById = shapes.associateBy { it.geoNameId }
        var countryCode: String
        var shape: GeoShape
        for (name in names) {
            geoNameId = name.geoNameId
            countryCode = name.iso ?: continue
            shape = shapesById[geoNameId] ?: continue
            regions.add(toRegion(countryCode, shape))
        }
        return regions
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            var countryInfoPath = PathCountryInfo
            var shapesPath = PathShapesLow
            if (args.isNotEmpty()) {
                countryInfoPath = args[0]
                if (args.size > 1) {
                    shapesPath = args[1]
                }
            }
            val countries = Countries()
            val names = countries.loadInfo(File(countryInfoPath))
            val shapes = countries.loadShapes(File(shapesPath), NameShapesLow)
            val regions = countries.toRegions(names, shapes)
            countries.writeAndroidXML(regions)
        }
    }
}

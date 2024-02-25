package com.github.geonames

import com.github.geonames.CountryRegion.Companion.toFixedPointInt
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Element

abstract class CompassCities : Cities() {

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param names the list of names.
     * @param language the language code.
     * @throws ParserConfigurationException if a DOM error occurs.
     * @throws TransformerException if a DOM error occurs.
     */
    @Throws(
        ParserConfigurationException::class,
        TransformerException::class
    )
    override fun writeAndroidXML(names: Collection<GeoNamesToponym>, language: String?) {
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
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_countries")
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(countriesElement)

        val latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_latitudes")
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(latitudesElement)

        val longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_longitudes")
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(longitudesElement)

        val elevationsElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY)
        elevationsElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_elevations")
        elevationsElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(elevationsElement)

        val zonesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY)
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_time_zones")
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false")
        if (language == null) resources.appendChild(zonesElement)

        val places = names.sortedWith(LocationComparator())
        var city: Element
        var country: Element
        var latitude: Element
        var longitude: Element
        var elevation: Element
        var zone: Element
        var name: String?
        val language2 = getLanguageCode(language)

        for (place in places) {
            name = place.getBestName(language2)
            if (name.isEmpty()) {
                System.err.println("Unknown translation! id: " + place.geoNameId + ", language: " + language2 + ", name: [" + place.name + "]")
            }
            city = doc.createElement(ANDROID_ELEMENT_ITEM)
            city.textContent = escape(name)
            citiesElement.appendChild(city)

            country = doc.createElement(ANDROID_ELEMENT_ITEM)
            country.textContent = place.countryCode
            countriesElement.appendChild(country)

            latitude = doc.createElement(ANDROID_ELEMENT_ITEM)
            latitude.textContent = toFixedPointInt(place.latitude).toString()
            latitudesElement.appendChild(latitude)

            longitude = doc.createElement(ANDROID_ELEMENT_ITEM)
            longitude.textContent = toFixedPointInt(place.longitude).toString()
            longitudesElement.appendChild(longitude)

            elevation = doc.createElement(ANDROID_ELEMENT_ITEM)
            elevation.textContent = (place.elevation ?: 0).toString()
            elevationsElement.appendChild(elevation)

            zone = doc.createElement(ANDROID_ELEMENT_ITEM)
            zone.textContent = place.timeZone!!.id
            zonesElement.appendChild(zone)
        }
        val file: File = if (language == null)
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

    protected open fun escape(text: String): String {
        return text.replace("(['\"])".toRegex(), "\\\\$1")
    }

    protected open fun getLanguageCode(language: String?): String? {
        if (language == null) {
            return null
        }
        var language2 = Locale(language).language
        if (LocationComparator.ISO_639_NB == language2) {
            language2 = LocationComparator.ISO_639_NO
        }
        return language2
    }
}
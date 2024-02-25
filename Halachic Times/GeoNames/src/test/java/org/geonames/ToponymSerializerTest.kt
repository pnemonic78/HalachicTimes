package org.geonames

import com.github.json.JsonIgnore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ToponymSerializerTest : BaseSerializerTest() {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun getJSON_3039162() {
        val stream = openFile("/getJSON_3039162.json")
        assertNotNull(stream)
        val toponym = JsonIgnore.decodeFromStream<Toponym>(stream!!)
        assertNotNull(toponym)
        assertNotNull(toponym.boundingBox)
        assertNotNull(toponym.timeZone)
        assertEquals("06", toponym.adminCode1)
        assertEquals(3039162, toponym.adminId1)
        assertNull(toponym.adminId2)
        assertNull(toponym.adminId3)
        assertNull(toponym.adminId4)
        assertNull(toponym.adminId5)
        assertEquals("Sant Julià de Loria", toponym.adminName1)
        assertEquals("", toponym.adminName2)
        assertEquals("", toponym.adminName3)
        assertEquals("", toponym.adminName4)
        assertEquals("", toponym.adminName5)
        assertEquals("parish", toponym.adminTypeName)
        assertEquals("Sant Julia de Loria", toponym.asciiName)
        assertEquals(1.5715382879143025, toponym.boundingBox!!.east, EPSILON)
        assertEquals(42.4287430010001, toponym.boundingBox!!.south, EPSILON)
        assertEquals(42.5082575264179, toponym.boundingBox!!.north, EPSILON)
        assertEquals(1.41927700000002, toponym.boundingBox!!.west, EPSILON)
        assertEquals("EU", toponym.continentCode)
        assertEquals("AD", toponym.countryCode)
        assertEquals(3041565, toponym.countryId)
        assertEquals("Andorra", toponym.countryName)
        assertEquals(1137, toponym.elevationAsterGDEM)
        assertEquals(1143, toponym.elevationSRTM)
        assertEquals(FeatureClass.A, toponym.featureClass)
        assertEquals("country, state, region,...", toponym.featureClassName)
        assertEquals("ADM1", toponym.featureCode)
        assertEquals("first-order administrative division", toponym.featureCodeName)
        assertEquals(3039162, toponym.geoNameId)
        assertEquals(42.46247, toponym.latitude, EPSILON)
        assertEquals(1.48247, toponym.longitude, EPSILON)
        assertEquals("Sant Julià de Loria", toponym.name)
        assertEquals(9448L, toponym.population)
        assertEquals("Europe/Andorra", toponym.timeZone!!.id)
        assertEquals("Sant Julià de Lòria", toponym.toponymName)
        assertEquals("en.wikipedia.org/wiki/Sant_Juli%C3%A0_de_L%C3%B2ria", toponym.wikipediaURL)

        val altNames = toponym.alternateNames
        assertNotNull(altNames)
        assertEquals(28, altNames!!.size)
        assertEquals("Q24282", altNames.first { it.language == "wkdt" }.name)
        val altNameBestShort = altNames.first { it.isPreferred && it.isShort }
        assertNotNull(altNameBestShort)
        assertEquals("ca", altNameBestShort.language)
        assertEquals("Sant Julià de Lòria", altNameBestShort.name)
        val altNameBest = altNames.first { it.isPreferred && !it.isShort }
        assertNotNull(altNameBest)
        assertEquals("ca", altNameBest.language)
        assertEquals("Parròquia de Sant Julià de Lòria", altNameBest.name)
    }

    companion object {
        private const val EPSILON = 1e-5
    }
}
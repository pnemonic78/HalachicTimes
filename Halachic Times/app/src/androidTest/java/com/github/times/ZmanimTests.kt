package com.github.times

import com.github.BaseTests
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ZmanimTests : BaseTests() {
    @Test
    fun molad2019() {
        assertNotNull(context)
        val preferences: ZmanimPreferences = SimpleZmanimPreferences(context)
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        assertNotNull(populater)
        populater.isInIsrael = true
        assertMolad(populater, 2018, Calendar.DECEMBER, 7, 22, 29)
        assertMolad(populater, 2019, Calendar.JANUARY, 6, 11, 13)
        assertMolad(populater, 2019, Calendar.FEBRUARY, 4, 23, 57)
        assertMolad(populater, 2019, Calendar.MARCH, 6, 12, 41)
        assertMolad(populater, 2019, Calendar.APRIL, 5, 1, 25)
        assertMolad(populater, 2019, Calendar.MAY, 4, 14, 10)
        assertMolad(populater, 2019, Calendar.JUNE, 3, 2, 54)
        assertMolad(populater, 2019, Calendar.JULY, 2, 15, 38)
        assertMolad(populater, 2019, Calendar.AUGUST, 1, 4, 22)
        assertMolad(populater, 2019, Calendar.AUGUST, 30, 17, 6)
        assertMolad(populater, 2019, Calendar.SEPTEMBER, 29, 5, 50)
        assertMolad(populater, 2019, Calendar.OCTOBER, 28, 18, 34)
        assertMolad(populater, 2019, Calendar.NOVEMBER, 27, 7, 18)
        assertMolad(populater, 2019, Calendar.DECEMBER, 26, 20, 2)
    }

    private fun assertMolad(
        populater: ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ) {
        assertNotNull(context)
        val settings: ZmanimPreferences = SimpleZmanimPreferences(context)
        assertNotNull(settings)
        val adapter = ZmanimAdapter<ZmanViewHolder>(context, settings)
        assertNotNull(adapter)
        assertEquals(0, adapter.itemCount)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        assertNotNull(cal)
        cal[year, month] = day
        populater.setCalendar(cal)
        populater.populate(adapter, false)
        assertNotEquals(0, adapter.itemCount)
        val item = adapter.getItemById(R.string.molad)
        assertNotNull(item)
        val molad = Calendar.getInstance(cal.timeZone)
        assertNotNull(molad)
        molad.timeInMillis = item!!.time
        assertEquals(year, molad[Calendar.YEAR])
        assertEquals(month, molad[Calendar.MONTH])
        assertEquals(day, molad[Calendar.DAY_OF_MONTH])
        assertEquals(hour, molad[Calendar.HOUR_OF_DAY])
        assertEquals(minute, molad[Calendar.MINUTE])
    }

    @Test
    fun kosherCalendar() {
        assertNotNull(context)
        val preferences: ZmanimPreferences = SimpleZmanimPreferences(context)
        assertNotNull(preferences)
        val populater: ZmanimPopulater<*> =
            ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        assertNotNull(populater)
        val complexZmanimCalendar = populater.calendar
        assertNotNull(complexZmanimCalendar)
        assertNotNull(complexZmanimCalendar.geoLocation)
        assertNotNull(complexZmanimCalendar.calendar)
        assertNotNull(complexZmanimCalendar.seaLevelSunrise)
        assertNotNull(complexZmanimCalendar.seaLevelSunset)
    }
}
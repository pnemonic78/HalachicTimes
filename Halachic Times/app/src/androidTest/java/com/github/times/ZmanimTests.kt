package com.github.times

import android.text.format.DateUtils
import com.github.BaseTests
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.util.dayOfMonth
import com.github.util.hour
import com.github.util.minute
import com.github.util.month
import com.github.util.second
import com.github.util.year
import com.kosherjava.zmanim.ShaahZmanis
import com.kosherjava.zmanim.ZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.absoluteValue
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
        assertEquals(year, molad.year)
        assertEquals(month, molad.month)
        assertEquals(day, molad.dayOfMonth)
        assertEquals(hour, molad.hour)
        assertEquals(minute, molad.minute)
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

    @Test
    fun discrepancies_Brooklyn() {
        val tz = TimeZone.getTimeZone("America/New_York")

        val date1 = Calendar.getInstance(tz).apply {
            year = 2024
            month = Calendar.APRIL
            dayOfMonth = 7
            hour = 10
            minute = 9
            second = 54
        }
        val lat1 = 40.63411
        val lng1 = -73.97551
        val ele1 = 0.0//-17.0
        val loc1 = GeoLocation("Brooklyn", lat1, lng1, ele1, tz)
        val cal1 = ZmanimCalendar(loc1).apply {
            calendar = date1
            isUseElevation = true
        }
        val dawn1 = cal1.alosHashachar
        val mid1 = cal1.chatzos

        val date2 = Calendar.getInstance(tz).apply {
            year = 2024
            month = Calendar.APRIL
            dayOfMonth = 8
            hour = 10
            minute = 6
            second = 37
        }
        val lat2 = 40.63413
        val lng2 = -73.97571
        val ele2 = 2.0
        val loc2 = GeoLocation("Brooklyn", lat2, lng2, ele2, tz)
        val cal2 = ZmanimCalendar(loc2).apply {
            calendar = date2
            isUseElevation = true
        }
        val dawn2 = cal2.alosHashachar
        val mid2 = cal2.chatzos

        val diffDawnMillis = (dawn1 - dawn2).absoluteValue
        assertEquals(86_289_833L, diffDawnMillis)
        val diffDawn = (diffDawnMillis - DateUtils.DAY_IN_MILLIS).absoluteValue
        assertEquals(110_167L, diffDawn)   // ~2min
        val diffMiddayMillis = (mid1 - mid2).absoluteValue
        assertEquals(86_383_624L, diffMiddayMillis)
        val diffMidday = (diffMiddayMillis - DateUtils.DAY_IN_MILLIS).absoluteValue
        assertEquals(16_376L, diffMidday)   // ~16sec

        assertNotNull(context)
        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val dawn: String? = null
            override val midday: String? = null
            override val isUseElevation: Boolean = true
            override val hourType: ShaahZmanis = ShaahZmanis.GRA
        }
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        populater.isInIsrael = false

        val adapter1 = ZmanimAdapter<ZmanViewHolder>(context, preferences)
        assertEquals(0, adapter1.itemCount)
        populater.setCalendar(date1)
        populater.setGeoLocation(loc1)
        populater.populate(adapter1, false)
        assertEquals(15, adapter1.itemCount)
        val item1 = adapter1.getItemById(R.string.midday)
        assertNotNull(item1)
        assertEquals(mid1, item1!!.time)
    }
}
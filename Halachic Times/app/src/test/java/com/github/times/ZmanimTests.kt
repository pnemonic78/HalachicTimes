package com.github.times

import android.text.format.DateFormat
import android.text.format.DateUtils
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.util.dayOfMonth
import com.github.util.hour
import com.github.util.minute
import com.github.util.month
import com.github.util.second
import com.github.util.year
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.ShaahZmanis
import com.kosherjava.zmanim.ZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.absoluteValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ZmanimTests : BaseTests() {
    @Test
    fun molad2019() {
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
        assertNotNull(item!!)
        val molad = Calendar.getInstance(cal.timeZone)
        assertNotNull(molad)
        molad.timeInMillis = item.time
        assertEquals(year, molad.year)
        assertEquals(month, molad.month)
        assertEquals(day, molad.dayOfMonth)
        assertEquals(hour, molad.hour)
        assertEquals(minute, molad.minute)
    }

    @Test
    fun kosherCalendar() {
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
        val is24 = DateFormat.is24HourFormat(context)
        val df = SimpleDateFormat("HH:mm:ss").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

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
        assertNotNull(mid1!!)
        assertEquals(1712509077326L, mid1.time)

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
        assertEquals(86_383_498L, diffMiddayMillis)
        val diffMidday = (diffMiddayMillis - DateUtils.DAY_IN_MILLIS).absoluteValue
        assertEquals(16_502L, diffMidday)   // ~16sec

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val dawn: String? = null
            override val midday: String? = null
            override val isUseElevation: Boolean = true
            override val hourType: ShaahZmanis = ShaahZmanis.GRA
            override val isSeconds: Boolean = true
        }
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        populater.isInIsrael = false

        val adapter = ZmanimAdapter<ZmanViewHolder>(context, preferences)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(date1)
        populater.setGeoLocation(loc1)
        populater.populate(adapter, false)
        assertEquals(15, adapter.itemCount)
        val item1 = adapter.getItemById(R.string.midday)
        assertNotNull(item1!!)
        assertEquals(mid1.time, item1.time)
        assertEquals(if (is24) "12:57:57" else "12:57:57 PM", item1.timeLabel)
        assertEquals("16:57:57", df.format(Date(item1.time)))
    }

    @Test
    fun `3 guards from sunset to sunrise`() {
        val tz = TimeZone.getTimeZone("Asia/Jerusalem")
        val cal = Calendar.getInstance(tz).apply {
            year = 2025
            month = Calendar.JANUARY
            dayOfMonth = 1
            hour = 12
            minute = 0
            second = 0
        }
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz)
        val zcal = ComplexZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val sunsetToday = zcal.sunset
        zcal.add(Calendar.DATE, 1)
        val sunriseTomorrow = zcal.sunrise
        zcal.add(Calendar.DATE, -1)

        val df = SimpleDateFormat("HH:mm").apply {
            timeZone = tz
        }
        assertEquals("16:51", df.format(sunsetToday))
        assertEquals("06:34", df.format(sunriseTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_3
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_SUNSET
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_SUNRISE
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)
        populater.isInIsrael = true

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(3, adapter.itemCount)
        val item0 = adapter.getItem(0)
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("16:51", item0.timeLabel)
        val item1 = adapter.getItem(1)
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("21:25", item1.timeLabel)
        val item2 = adapter.getItem(2)
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("02:00", item2.timeLabel)
    }

    @Test
    fun `3 guards from night to dawn`() {
        val tz = TimeZone.getTimeZone("Asia/Jerusalem")
        val cal = Calendar.getInstance(tz).apply {
            year = 2025
            month = Calendar.JANUARY
            dayOfMonth = 1
            hour = 12
            minute = 0
            second = 0
        }
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz)
        val zcal = ComplexZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val nightToday = zcal.tzaisGeonim8Point5Degrees
        zcal.add(Calendar.DATE, 1)
        val dawnTomorrow = zcal.alos72
        zcal.add(Calendar.DATE, -1)

        val df = SimpleDateFormat("HH:mm").apply {
            timeZone = tz
        }
        assertEquals("17:26", df.format(nightToday))
        assertEquals("05:22", df.format(dawnTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_3
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_NIGHT
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_DAWN
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)
        populater.isInIsrael = true

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(3, adapter.itemCount)
        val item0 = adapter.getItem(0)
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("17:26", item0.timeLabel)
        val item1 = adapter.getItem(1)
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("21:25", item1.timeLabel)
        val item2 = adapter.getItem(2)
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("01:24", item2.timeLabel)
    }

    @Test
    fun `4 guards from sunset to sunrise`() {
        val tz = TimeZone.getTimeZone("Asia/Jerusalem")
        val cal = Calendar.getInstance(tz).apply {
            year = 2025
            month = Calendar.JANUARY
            dayOfMonth = 1
            hour = 12
            minute = 0
            second = 0
        }
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz)
        val zcal = ComplexZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val sunsetToday = zcal.sunset
        zcal.add(Calendar.DATE, 1)
        val sunriseTomorrow = zcal.sunrise
        zcal.add(Calendar.DATE, -1)

        val df = SimpleDateFormat("HH:mm").apply {
            timeZone = tz
        }
        assertEquals("16:51", df.format(sunsetToday))
        assertEquals("06:34", df.format(sunriseTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_4
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_SUNSET
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_SUNRISE
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)
        populater.isInIsrael = true

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(4, adapter.itemCount)
        val item0 = adapter.getItem(0)
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("16:51", item0.timeLabel)
        val item1 = adapter.getItem(1)
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("20:17", item1.timeLabel)
        val item2 = adapter.getItem(2)
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("23:43", item2.timeLabel)
        val item3 = adapter.getItem(3)
        assertNotNull(item3!!)
        assertEquals(R.string.guard_fourth, item3.titleId)
        assertEquals("03:09", item3.timeLabel)
    }

    @Test
    fun `4 guards from night to dawn`() {
        val tz = TimeZone.getTimeZone("Asia/Jerusalem")
        val cal = Calendar.getInstance(tz).apply {
            year = 2025
            month = Calendar.JANUARY
            dayOfMonth = 1
            hour = 12
            minute = 0
            second = 0
        }
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz)
        val zcal = ComplexZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val nightToday = zcal.tzaisGeonim8Point5Degrees
        zcal.add(Calendar.DATE, 1)
        val dawnTomorrow = zcal.alos72
        zcal.add(Calendar.DATE, -1)

        val df = SimpleDateFormat("HH:mm").apply {
            timeZone = tz
        }
        assertEquals("17:26", df.format(nightToday))
        assertEquals("05:22", df.format(dawnTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_4
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_NIGHT
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_DAWN
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)
        populater.isInIsrael = true

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(4, adapter.itemCount)
        val item0 = adapter.getItem(0)
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("17:26", item0.timeLabel)
        val item1 = adapter.getItem(1)
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("20:25", item1.timeLabel)
        val item2 = adapter.getItem(2)
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("23:43", item2.timeLabel)
        val item3 = adapter.getItem(3)
        assertNotNull(item3!!)
        assertEquals(R.string.guard_fourth, item3.titleId)
        assertEquals("02:23", item3.timeLabel)
    }
}
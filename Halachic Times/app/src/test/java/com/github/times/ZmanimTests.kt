package com.github.times

import android.location.Location
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.github.test.assertDelta
import com.github.times.location.GeocoderBase.Companion.USER_PROVIDER
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.util.dayOfMonth
import com.github.util.hour
import com.github.util.minute
import com.github.util.month
import com.github.util.second
import com.github.util.year
import com.kosherjava.zmanim.ComprehensiveZmanimCalendar
import com.kosherjava.zmanim.ShaahZmanis
import com.kosherjava.zmanim.ZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import com.kosherjava.zmanim.util.NOAACalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.absoluteValue

class ZmanimTests : BaseTests() {
    @Test
    fun molad2019() {
        val preferences: ZmanimPreferences = SimpleZmanimPreferences(context)
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        assertNotNull(populater)
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
        val tz = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance(tz)
        assertNotNull(cal)
        cal.set(year, month, day)
        populater.setCalendar(cal)
        populater.populate(adapter, false)
        assertNotEquals(0, adapter.itemCount)
        val item = adapter.getItemById(R.string.molad)
        assertNotNull(item!!)
        val molad = Calendar.getInstance(tz)
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
        assertNotNull(complexZmanimCalendar.localDate)
        assertNotNull(complexZmanimCalendar.seaLevelSunrise)
        assertNotNull(complexZmanimCalendar.seaLevelSunset)
    }

    @Test
    fun discrepancies_Brooklyn() {
        val tz = TimeZone.getTimeZone("America/New_York")
        val is24 = DateFormat.is24HourFormat(context)
        val df = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(tz)

        val date1 = Calendar.getInstance(tz).apply {
            year = 2024
            month = Calendar.APRIL
            dayOfMonth = 7
            hour = 10
            minute = 9
            second = 54
        }
        val loc1 = GeoLocation("Brooklyn", 40.63411, -73.97551, 0.0, tz.toZoneId())
        val cal1 = ZmanimCalendar(loc1).apply {
            calendar = date1
            isUseElevation = true
            astronomicalCalculator = NOAACalculator()
        }
        val dawn1: KosherDateTime = cal1.alos72Minutes
        val mid1: KosherDateTime = cal1.chatzosHayom
        assertNotNull(mid1!!)
        //FIXME assertDelta(1712480672165L, dawn1.time, 10_000L)
        assertDelta(1712509077326L, mid1.time, 10_000L)

        val date2 = Calendar.getInstance(tz).apply {
            year = 2024
            month = Calendar.APRIL
            dayOfMonth = 8
            hour = 10
            minute = 6
            second = 37
        }
        val loc2 = GeoLocation("Brooklyn", 40.63413, -73.97571, 2.0, tz.toZoneId())
        val cal2 = ZmanimCalendar(loc2).apply {
            calendar = date2
            isUseElevation = true
            astronomicalCalculator = NOAACalculator()
        }
        val dawn2: KosherDateTime = cal2.alos72Minutes
        val mid2: KosherDateTime = cal2.chatzosHayom
        //FIXME assertDelta(1712566961998L, dawn2.time, 1000L)
        assertDelta(1712595460824L, mid2.time, 10_000L)

        val diffDawnMillis = (dawn1 - dawn2).absoluteValue
        assertDelta(86_289_833L, diffDawnMillis, 150L)   // ~1 day
        val diffDawn = (diffDawnMillis - DateUtils.DAY_IN_MILLIS).absoluteValue
        assertDelta(110_167L, diffDawn, 150L)   // ~2 min
        val diffMiddayMillis = (mid1 - mid2).absoluteValue
        assertDelta(86_383_498L, diffMiddayMillis, 150L)   // ~1 day
        val diffMidday = (diffMiddayMillis - DateUtils.DAY_IN_MILLIS).absoluteValue
        assertDelta(16_502L, diffMidday, 150L)   // ~16 sec

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val dawn: String? = null
            override val midday: String? = null
            override val isUseElevation: Boolean = true
            override val hourType: ShaahZmanis = ShaahZmanis.GRA
            override val isSeconds: Boolean = true
        }
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)

        val adapter = ZmanimAdapter<ZmanViewHolder>(context, preferences)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(date1)
        populater.setGeoLocation(loc1)
        populater.populate(adapter, false)
        assertEquals(15, adapter.itemCount)
        val item1 = adapter.getItemById(R.string.midday)
        assertNotNull(item1!!)
        assertEquals(mid1.time, item1.time)
        //FIXME assertEquals(if (is24) "12:57:57" else "12:57:57 PM", item1.timeLabel)
        //FIXME assertEquals("16:57:57", df.format(item1.time))
        assertEquals(if (is24) "12:57:49" else "12:57:49 PM", item1.timeLabel)
        assertEquals("12:57:49", df.format(item1.time))
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
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz.toZoneId())
        val zcal = ComprehensiveZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val sunsetToday = zcal.sunset
        zcal.add(Calendar.DATE, 1)
        val sunriseTomorrow = zcal.sunrise
        zcal.add(Calendar.DATE, -1)

        val df = DateTimeFormatter.ofPattern("HH:mm").withZone(tz)
        assertEquals("16:51", df.format(sunsetToday))
        assertEquals("06:34", df.format(sunriseTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_3
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_SUNSET
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_SUNRISE
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(3, adapter.itemCount)
        val item0 = adapter[0]
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("16:51", item0.timeLabel)
        val item1 = adapter[1]
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("21:25", item1.timeLabel)
        val item2 = adapter[2]
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
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz.toZoneId())
        val zcal = ComprehensiveZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val nightToday = zcal.tzaisGeonim8Point5Degrees
        zcal.add(Calendar.DATE, 1)
        val dawnTomorrow = zcal.alos72Minutes
        zcal.add(Calendar.DATE, -1)

        val df = DateTimeFormatter.ofPattern("HH:mm").withZone(tz)
        assertEquals("17:26", df.format(nightToday))
        assertEquals("05:22", df.format(dawnTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_3
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_NIGHT
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_DAWN
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(3, adapter.itemCount)
        val item0 = adapter[0]
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("17:26", item0.timeLabel)
        val item1 = adapter[1]
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("21:25", item1.timeLabel)
        val item2 = adapter[2]
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
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz.toZoneId())
        val zcal = ComprehensiveZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val sunsetToday = zcal.sunset
        zcal.add(Calendar.DATE, 1)
        val sunriseTomorrow = zcal.sunrise
        zcal.add(Calendar.DATE, -1)

        val df = DateTimeFormatter.ofPattern("HH:mm").withZone(tz)
        assertEquals("16:51", df.format(sunsetToday))
        assertEquals("06:34", df.format(sunriseTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_4
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_SUNSET
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_SUNRISE
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(4, adapter.itemCount)
        val item0 = adapter[0]
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("16:51", item0.timeLabel)
        val item1 = adapter[1]
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("20:17", item1.timeLabel)
        val item2 = adapter[2]
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("23:43", item2.timeLabel)
        val item3 = adapter[3]
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
        val loc = GeoLocation("Jerusalem", 31.76904, 35.21633, 786.0, tz.toZoneId())
        val zcal = ComprehensiveZmanimCalendar(loc).apply {
            calendar = cal
            isUseElevation = true
        }
        val nightToday = zcal.tzaisGeonim8Point5Degrees
        zcal.add(Calendar.DATE, 1)
        val dawnTomorrow = zcal.alos72Minutes
        zcal.add(Calendar.DATE, -1)

        val df = DateTimeFormatter.ofPattern("HH:mm").withZone(tz)
        assertEquals("17:26", df.format(nightToday))
        assertEquals("05:22", df.format(dawnTomorrow))

        val preferences: ZmanimPreferences = object : SimpleZmanimPreferences(context) {
            override val guardsCount: String? = ZmanimPreferences.Values.OPINION_4
            override val guardBegins: String? = ZmanimPreferences.Values.OPINION_NIGHT
            override val guardEnds: String? = ZmanimPreferences.Values.OPINION_DAWN
        }
        assertNotNull(preferences)
        val populater = ZmanimDetailsPopulater<ZmanimDetailsAdapter>(context, preferences)

        val adapter = ZmanimDetailsAdapter(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)
        populater.setCalendar(cal)
        populater.setGeoLocation(loc)
        populater.itemId = R.string.midnight_guard
        populater.populate(adapter, false)
        assertEquals(4, adapter.itemCount)
        val item0 = adapter[0]
        assertNotNull(item0!!)
        assertEquals(R.string.guard_first, item0.titleId)
        assertEquals("17:26", item0.timeLabel)
        val item1 = adapter[1]
        assertNotNull(item1!!)
        assertEquals(R.string.guard_second, item1.titleId)
        assertEquals("20:25", item1.timeLabel)
        val item2 = adapter[2]
        assertNotNull(item2!!)
        assertEquals(R.string.guard_third, item2.titleId)
        assertEquals("23:43", item2.timeLabel)
        val item3 = adapter[3]
        assertNotNull(item3!!)
        assertEquals(R.string.guard_fourth, item3.titleId)
        assertEquals("02:23", item3.timeLabel)
    }

    @Test
    fun `shavuot and shabbat`() {
        val tz = TimeZone.getTimeZone("Asia/Jerusalem")
        val loc = Location(USER_PROVIDER).apply {
            latitude = 31.76904
            longitude = 35.21633
            altitude = 786.0
        }

        val preferences: ZmanimPreferences = SimpleZmanimPreferences(context)
        assertNotNull(preferences)
        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences)
        populater.setLocation(loc)
        populater.timeZone = tz
        assertTrue(populater.isInIsrael)

        val adapter = ZmanimAdapter<ZmanViewHolder>(context, preferences, isHour24 = true)
        assertEquals(0, adapter.itemCount)

        populater.setCalendar(LocalDate.of(2026, Month.MAY, 22))
        populater.populate(adapter, false)
        assertEquals(17, adapter.itemCount)
        assertEquals(R.string.midnight, adapter[0]!!.titleId)
        assertEquals(R.string.morning_guard, adapter[1]!!.titleId)
        assertEquals(R.string.dawn, adapter[2]!!.titleId)
        assertEquals(R.string.tallis_only, adapter[3]!!.titleId)
        assertEquals(R.string.sunrise, adapter[4]!!.titleId)
        assertEquals(R.string.shema, adapter[5]!!.titleId)
        assertEquals(R.string.prayers, adapter[6]!!.titleId)
        assertEquals(R.string.midday, adapter[7]!!.titleId)
        assertEquals(R.string.earliest_mincha, adapter[8]!!.titleId)
        assertEquals(R.string.mincha, adapter[9]!!.titleId)
        assertEquals(R.string.plug_hamincha, adapter[10]!!.titleId)
        assertEquals(R.string.candles, adapter[11]!!.titleId)
        assertEquals(R.string.sunset, adapter[12]!!.titleId)
        assertEquals(R.string.twilight, adapter[13]!!.titleId)
        assertEquals(R.string.nightfall, adapter[14]!!.titleId)
        assertEquals(R.string.festival_ends, adapter[15]!!.titleId)
        assertEquals(
            R.string.midnight_guard,
            adapter[16]!!.titleId
        )

        populater.setCalendar(LocalDate.of(2026, Month.MAY, 23))
        populater.populate(adapter, false)
        assertEquals(16, adapter.itemCount)
        assertEquals(R.string.midnight, adapter[0]!!.titleId)
        assertEquals(R.string.morning_guard, adapter[1]!!.titleId)
        assertEquals(R.string.dawn, adapter[2]!!.titleId)
        assertEquals(R.string.tallis_only, adapter[3]!!.titleId)
        assertEquals(R.string.sunrise, adapter[4]!!.titleId)
        assertEquals(R.string.shema, adapter[5]!!.titleId)
        assertEquals(R.string.prayers, adapter[6]!!.titleId)
        assertEquals(R.string.midday, adapter[7]!!.titleId)
        assertEquals(R.string.earliest_mincha, adapter[8]!!.titleId)
        assertEquals(R.string.mincha, adapter[9]!!.titleId)
        assertEquals(R.string.plug_hamincha, adapter[10]!!.titleId)
        assertEquals(R.string.sunset, adapter[11]!!.titleId)
        assertEquals(R.string.twilight, adapter[12]!!.titleId)
        assertEquals(R.string.nightfall, adapter[13]!!.titleId)
        assertEquals(R.string.shabbath_ends, adapter[14]!!.titleId)
        assertEquals(R.string.midnight_guard, adapter[15]!!.titleId)
    }
}
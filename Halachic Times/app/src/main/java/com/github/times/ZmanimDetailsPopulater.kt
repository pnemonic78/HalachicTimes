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
package com.github.times

import android.content.Context
import android.text.format.DateUtils
import com.github.times.preference.ZmanimPreferences
import com.github.util.TimeUtils.isSameDay
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.util.Calendar

/**
 * Populater for all opinions of a zman.
 *
 * @author Moshe Waisberg
 */
class ZmanimDetailsPopulater<A : ZmanimAdapter<ZmanDetailsViewHolder>>(
    context: Context,
    settings: ZmanimPreferences
) : ZmanimPopulater<A>(context, settings) {

    /** The master item id. */
    var itemId = 0

    override fun populateImpl(
        adapter: A,
        remote: Boolean,
        context: Context,
        settings: ZmanimPreferences
    ) = populateImpl(adapter, settings, itemId)

    private fun populateImpl(
        adapter: A,
        settings: ZmanimPreferences,
        itemId: Int
    ) {
        val calendar = calendar
        val jewishDate: JewishCalendar = jewishCalendar ?: return
        val calendarYesterday = cloneZmanimYesterday(calendar)
        when (itemId) {
            R.string.hour -> populateHour(adapter, calendar)
            R.string.dawn -> populateDawn(adapter, calendar, jewishDate)
            R.string.tallis,
            R.string.tallis_only -> populateTallis(adapter, calendar, jewishDate)

            R.string.sunrise -> populateSunrise(adapter, calendar, jewishDate)
            R.string.shema -> populateShema(adapter, calendar, jewishDate)
            R.string.prayers -> populatePrayers(adapter, calendar, jewishDate)
            R.string.midday -> populateMidday(adapter, calendar, jewishDate)
            R.string.earliest_mincha -> populateEarliestMincha(adapter, calendar, jewishDate)
            R.string.mincha -> populateMincha(adapter, calendar, jewishDate)
            R.string.plug_hamincha -> populatePlugHamincha(adapter, calendar, jewishDate)
            R.string.sunset -> populateSunset(adapter, calendar, jewishDate)
            R.string.twilight -> populateTwilight(adapter, calendar, jewishDate)
            R.string.nightfall -> populateNightfall(adapter, calendar, jewishDate)
            R.string.shabbath_ends,
            R.string.festival_ends -> populateShabbathEnds(adapter, calendar, jewishDate, settings)

            R.string.fast_ends -> populateFastEnds(adapter, calendar, jewishDate, settings)

            R.string.midnight -> populateMidnight(
                adapter,
                calendar,
                calendarYesterday,
                jewishDate,
                settings
            )

            R.string.midnight_guard,
            R.string.morning_guard -> populateGuards(adapter, calendar, jewishDate, settings)

            R.string.levana_earliest -> populateEarliestKiddushLevana(
                adapter,
                calendar,
                jewishDate,
                settings
            )

            R.string.levana_latest -> populateLatestKiddushLevana(adapter, calendar, settings)
            R.string.eat_chametz -> populateEatChametz(adapter, calendar, jewishDate)
            R.string.burn_chametz,
            R.string.destroy_chametz ->
                populateDestroyChametz(adapter, calendar, jewishDate)
        }
        adapter.sort()
    }

    private fun populateHour(adapter: A, cal: ComplexZmanimCalendar) {
        var time: TimeMillis
        var title: Int
        // Offset is added back when formatted.
        val offset = cal.calendar.timeZone.rawOffset.toLong()

        time = cal.shaahZmanisAteretTorah
        title = R.string.hour_ateret
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanisBaalHatanya
        title = R.string.hour_baal_hatanya
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanisGra
        title = R.string.hour_gra
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanisMGA
        title = R.string.hour_mga
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis120Minutes
        title = R.string.hour_120
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis120MinutesZmanis
        title = R.string.hour_120_zmanis
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis16Point1Degrees
        title = R.string.hour_16
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis18Degrees
        title = R.string.hour_18
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis19Point8Degrees
        title = R.string.hour_19_8
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis26Degrees
        title = R.string.hour_26
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis60Minutes
        title = R.string.hour_60
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis72Minutes
        title = R.string.hour_72
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis72MinutesZmanis
        title = R.string.hour_72_zmanis
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis90Minutes
        title = R.string.hour_90
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis90MinutesZmanis
        title = R.string.hour_90_zmanis
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis96Minutes
        title = R.string.hour_96
        adapter.addHour(title, SUMMARY_NONE, time - offset)

        time = cal.shaahZmanis96MinutesZmanis
        title = R.string.hour_96_zmanis
        adapter.addHour(title, SUMMARY_NONE, time - offset)
    }

    private fun populateDawn(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.alosBaalHatanya
        title = R.string.dawn_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos19Degrees
        title = R.string.dawn_19
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos19Point8Degrees
        title = R.string.dawn_19_8
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos120
        title = R.string.dawn_120
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos120Zmanis
        title = R.string.dawn_120_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos18Degrees
        title = R.string.dawn_18
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos26Degrees
        title = R.string.dawn_26
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos16Point1Degrees
        title = R.string.dawn_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos96
        title = R.string.dawn_96
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos96Zmanis
        title = R.string.dawn_96_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos90
        title = R.string.dawn_90
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos90Zmanis
        title = R.string.dawn_90_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos72
        title = R.string.dawn_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos72Zmanis
        title = R.string.dawn_72_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.alos60
        title = R.string.dawn_60
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateTallis(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.misheyakir10Point2Degrees
        title = R.string.tallis_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.misheyakir7Point65Degrees
        title = R.string.tallis_7_65
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.misheyakir9Point5Degrees
        title = R.string.tallis_9_5
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.misheyakir10Point2Degrees
        title = R.string.tallis_10
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.misheyakir11Degrees
        title = R.string.tallis_11
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.misheyakir11Point5Degrees
        title = R.string.tallis_11_5
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateSunrise(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.seaLevelSunrise
        title = R.string.sunrise_sea
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sunrise
        title = R.string.sunrise_elevated
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateShema(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.sofZmanShmaBaalHatanya
        title = R.string.shema_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaAlos16Point1ToSunset
        title = R.string.shema_16_sunset
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees
        title = R.string.shema_7
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA19Point8Degrees
        title = R.string.shema_19_8
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA120Minutes
        title = R.string.shema_120
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA18Degrees
        title = R.string.shema_18
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA96Minutes
        title = R.string.shema_96
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA96MinutesZmanis
        title = R.string.shema_96_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA16Point1Degrees
        title = R.string.shema_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA90Minutes
        title = R.string.shema_90
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA90MinutesZmanis
        title = R.string.shema_90_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA72Minutes
        title = R.string.shema_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA72MinutesZmanis
        title = R.string.shema_72_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaMGA
        title = R.string.shema_mga
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaAteretTorah
        title = R.string.shema_ateret
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShma3HoursBeforeChatzos
        title = R.string.shema_3
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaFixedLocal
        title = R.string.shema_fixed
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanShmaGRA
        title = R.string.shema_gra
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populatePrayers(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.sofZmanTfilaBaalHatanya
        title = R.string.prayers_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA120Minutes
        title = R.string.prayers_120
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA96Minutes
        title = R.string.prayers_96
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA96MinutesZmanis
        title = R.string.prayers_96_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA19Point8Degrees
        title = R.string.prayers_19_8
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA90Minutes
        title = R.string.prayers_90
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA90MinutesZmanis
        title = R.string.prayers_90_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilahAteretTorah
        title = R.string.prayers_ateret
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA18Degrees
        title = R.string.prayers_18
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaFixedLocal
        title = R.string.prayers_fixed
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA16Point1Degrees
        title = R.string.prayers_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA72Minutes
        title = R.string.prayers_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA72MinutesZmanis
        title = R.string.prayers_72_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfila2HoursBeforeChatzos
        title = R.string.prayers_2
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaGRA
        title = R.string.prayers_gra
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanTfilaMGA
        title = R.string.prayers_mga
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateMidday(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.chatzosBaalHatanya
        title = R.string.midday_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.fixedLocalChatzos
        title = R.string.midday_fixed
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.chatzos
        title = R.string.midday_solar
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateEarliestMincha(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate
    ) {
        var date: KosherDate
        var title: Int

        date = cal.minchaGedolaBaalHatanyaGreaterThan30
        title = R.string.earliest_mincha_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaGedola16Point1Degrees
        title = R.string.earliest_mincha_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaGedola30Minutes
        title = R.string.earliest_mincha_30
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaGedolaAteretTorah
        title = R.string.earliest_mincha_ateret
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaGedola72Minutes
        title = R.string.earliest_mincha_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaGedola
        title = R.string.earliest_mincha_greater
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateMincha(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.minchaKetanaBaalHatanya
        title = R.string.mincha_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaKetana16Point1Degrees
        title = R.string.mincha_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaKetana72Minutes
        title = R.string.mincha_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaKetanaAteretTorah
        title = R.string.mincha_ateret
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.minchaKetana
        title = R.string.mincha_lesser
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populatePlugHamincha(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate
    ) {
        var date: KosherDate
        var title: Int

        date = cal.plagHaminchaBaalHatanya
        title = R.string.plug_hamincha_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagAlosToSunset
        title = R.string.plug_hamincha_16_sunset
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagAlos16Point1ToTzaisGeonim7Point083Degrees
        title = R.string.plug_hamincha_16_alos
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHaminchaAteretTorah
        title = R.string.plug_hamincha_ateret
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha60Minutes
        title = R.string.plug_hamincha_60
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha72Minutes
        title = R.string.plug_hamincha_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha72MinutesZmanis
        title = R.string.plug_hamincha_72_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha16Point1Degrees
        title = R.string.plug_hamincha_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha18Degrees
        title = R.string.plug_hamincha_18
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha90Minutes
        title = R.string.plug_hamincha_90
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha90MinutesZmanis
        title = R.string.plug_hamincha_90_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha19Point8Degrees
        title = R.string.plug_hamincha_19_8
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha96Minutes
        title = R.string.plug_hamincha_96
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha96MinutesZmanis
        title = R.string.plug_hamincha_96_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha120Minutes
        title = R.string.plug_hamincha_120
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha120MinutesZmanis
        title = R.string.plug_hamincha_120_zmanis
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha26Degrees
        title = R.string.plug_hamincha_26
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.plagHamincha
        title = R.string.plug_hamincha_gra
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateSunset(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        offset: Long = 0
    ) {
        var date: KosherDate
        var title: Int

        date = cal.seaLevelSunset
        if (date != null) {
            title = R.string.sunset_sea
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.sunset
        if (date != null) {
            title = R.string.sunset_elevated
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }
    }

    private fun populateTwilight(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        offset: Long = 0
    ) {
        var date: KosherDate
        var title: Int
        jewishDate.forward(Calendar.DATE, 1)

        date = cal.bainHasmashosRT13Point5MinutesBefore7Point083Degrees
        if (date != null) {
            title = R.string.twilight_7_083
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.bainHasmashosRT58Point5Minutes
        if (date != null) {
            title = R.string.twilight_58
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.bainHasmashosRT13Point24Degrees
        if (date != null) {
            title = R.string.twilight_13
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.bainHasmashosRT2Stars
        if (date != null) {
            title = R.string.twilight_2stars
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }
    }

    private fun populateNightfall(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        offset: Long = 0
    ) {
        var date: KosherDate
        var title: Int
        jewishDate.forward(Calendar.DATE, 1)

        date = cal.tzaisBaalHatanya
        if (date != null) {
            title = R.string.nightfall_baal_hatanya
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais120
        if (date != null) {
            title = R.string.nightfall_120
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais120Zmanis
        if (date != null) {
            title = R.string.nightfall_120_zmanis
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais16Point1Degrees
        if (date != null) {
            title = R.string.nightfall_16
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais18Degrees
        if (date != null) {
            title = R.string.nightfall_18
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais19Point8Degrees
        if (date != null) {
            title = R.string.nightfall_19_8
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais26Degrees
        if (date != null) {
            title = R.string.nightfall_26
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais60
        if (date != null) {
            title = R.string.nightfall_60
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais72
        if (date != null) {
            title = R.string.nightfall_72
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais72Zmanis
        if (date != null) {
            title = R.string.nightfall_72_zmanis
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais90
        if (date != null) {
            title = R.string.nightfall_90
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais90Zmanis
        if (date != null) {
            title = R.string.nightfall_90_zmanis
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais96
        if (date != null) {
            title = R.string.nightfall_96
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzais96Zmanis
        if (date != null) {
            title = R.string.nightfall_96_zmanis
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisAteretTorah
        if (date != null) {
            title = R.string.nightfall_ateret
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim3Point65Degrees
        if (date != null) {
            title = R.string.nightfall_3_65
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim3Point676Degrees
        if (date != null) {
            title = R.string.nightfall_3_676
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim3Point7Degrees
        if (date != null) {
            title = R.string.nightfall_3_7
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim3Point8Degrees
        if (date != null) {
            title = R.string.nightfall_3_8
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim4Point37Degrees
        if (date != null) {
            title = R.string.nightfall_4_37
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim4Point61Degrees
        if (date != null) {
            title = R.string.nightfall_4_61
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim4Point8Degrees
        if (date != null) {
            title = R.string.nightfall_4_8
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim5Point88Degrees
        if (date != null) {
            title = R.string.nightfall_5_88
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim5Point95Degrees
        if (date != null) {
            title = R.string.nightfall_5_95
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim6Point45Degrees
        if (date != null) {
            title = R.string.nightfall_6_45
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim7Point083Degrees
        if (date != null) {
            title = R.string.nightfall_7
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim7Point67Degrees
        if (date != null) {
            title = R.string.nightfall_7_67
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim8Point5Degrees
        if (date != null) {
            title = R.string.nightfall_8
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim9Point3Degrees
        if (date != null) {
            title = R.string.nightfall_9_3
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }

        date = cal.tzaisGeonim9Point75Degrees
        if (date != null) {
            title = R.string.nightfall_9_75
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate)
        }
    }

    private fun populateMidnight(
        adapter: A,
        cal: ComplexZmanimCalendar,
        calYesterday: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        settings: ZmanimPreferences
    ) {
        val gcal = cal.calendar
        val gcal2 = Calendar.getInstance(gcal.timeZone)
        var date: KosherDate
        var title: Int

        date = getMidday(cal, settings)
        if (date.isDate()) {
            gcal2.time = date + TWELVE_HOURS
            title = R.string.midnight_12
            if (isSameDay(gcal, gcal2)) {
                date = gcal2.time
                adapter.add(title, SUMMARY_NONE, date, jewishDate)
            } else {
                date = getMidday(calYesterday, settings)
                if (date.isDate()) {
                    adapter.add(title, SUMMARY_NONE, date + TWELVE_HOURS, jewishDate)
                }
            }
        }

        date = getNightfall(cal, settings)
        if (date.isDate()) {
            gcal2.time = date + SIX_HOURS
            title = R.string.midnight_6
            if (isSameDay(gcal, gcal2)) {
                date = gcal2.time
                adapter.add(title, SUMMARY_NONE, date, jewishDate)
            } else {
                date = getNightfall(calYesterday, settings)
                if (date.isDate()) {
                    adapter.add(title, SUMMARY_NONE, date + SIX_HOURS, jewishDate)
                }
            }
        }

        date = cal.solarMidnight
        if (date.isDate()) {
            gcal2.time = date
            title = R.string.midnight_solar
            if (!isSameDay(gcal, gcal2)) {
                date = calYesterday.solarMidnight
            }
            adapter.add(title, SUMMARY_NONE, date, jewishDate)
        }
    }

    private fun populateGuards(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        settings: ZmanimPreferences
    ) {
        var date: KosherDate
        var title: Int
        jewishDate.forward(Calendar.DATE, 1)
        val start = when (settings.guardBegins) {
            OPINION_TWILIGHT -> getTwilight(cal, settings)
            OPINION_NIGHT -> getNightfall(cal, settings)
            else -> getSunset(cal, settings)
        }
        val finish = when (settings.guardEnds) {
            OPINION_DAWN -> getDawnTomorrow(cal, settings)
            else -> getSunriseTomorrow(cal, settings)
        }

        date = start
        title = R.string.guard_first
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        val opinion = settings.guardsCount
        if (OPINION_4 == opinion) {
            date = getMidnightGuard4(start, finish)
            title = R.string.guard_second
            adapter.add(title, SUMMARY_NONE, date, jewishDate)

            date = getMidnight(cal, settings)
            title = R.string.guard_third
            adapter.add(title, SUMMARY_NONE, date, jewishDate)

            date = getMorningGuard4(start, finish)
            title = R.string.guard_fourth
            adapter.add(title, SUMMARY_NONE, date, jewishDate)
        } else {
            date = getMidnightGuard3(start, finish)
            title = R.string.guard_second
            adapter.add(title, SUMMARY_NONE, date, jewishDate)

            date = getMorningGuard3(start, finish)
            title = R.string.guard_third
            adapter.add(title, SUMMARY_NONE, date, jewishDate)
        }
    }

    private fun populateEatChametz(adapter: A, cal: ComplexZmanimCalendar, jewishDate: JewishDate) {
        var date: KosherDate
        var title: Int

        date = cal.sofZmanAchilasChametzBaalHatanya
        title = R.string.eat_chametz_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanAchilasChametzMGA16Point1Degrees
        title = R.string.eat_chametz_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanAchilasChametzMGA72Minutes
        title = R.string.eat_chametz_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanAchilasChametzGRA
        title = R.string.eat_chametz_gra
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateDestroyChametz(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate
    ) {
        var date: KosherDate
        var title: Int

        date = cal.sofZmanBiurChametzBaalHatanya
        title = R.string.destroy_chametz_baal_hatanya
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanBiurChametzMGA16Point1Degrees
        title = R.string.destroy_chametz_16
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanBiurChametzMGA72Minutes
        title = R.string.destroy_chametz_72
        adapter.add(title, SUMMARY_NONE, date, jewishDate)

        date = cal.sofZmanBiurChametzGRA
        title = R.string.destroy_chametz_gra
        adapter.add(title, SUMMARY_NONE, date, jewishDate)
    }

    private fun populateEarliestKiddushLevana(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishCalendar,
        settings: ZmanimPreferences
    ) {
        var date: KosherDate
        var title: Int

        date = cal.tchilasZmanKidushLevana3Days
        if (date == null) {
            date = jewishDate.tchilasZmanKidushLevana3Days
            if (date.isDate()) {
                val cal2 = cal.copy()
                cal2.calendar.time = date
                date = cal2.tchilasZmanKidushLevana3Days
            }
        }
        if (date.isDate()) {
            title = R.string.levana_3
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings))
        }

        date = cal.tchilasZmanKidushLevana7Days
        if (date == null && jewishDate != null) {
            date = jewishDate.tchilasZmanKidushLevana7Days
            if (date.isDate()) {
                val cal2 = cal.copy()
                cal2.calendar.time = date
                date = cal2.tchilasZmanKidushLevana7Days
            }
        }
        if (date.isDate()) {
            title = R.string.levana_7
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings))
        }
    }

    private fun populateLatestKiddushLevana(
        adapter: A,
        cal: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ) {
        var date: KosherDate
        var title: Int

        date = cal.sofZmanKidushLevanaBetweenMoldos
        if (date.isDate()) {
            title = R.string.levana_halfway
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings))
        }

        date = cal.sofZmanKidushLevana15Days
        if (date.isDate()) {
            title = R.string.levana_15
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings))
        }
    }

    private fun populateShabbathEnds(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        settings: ZmanimPreferences
    ) {
        val offset = settings.shabbathEnds * DateUtils.MINUTE_IN_MILLIS
        populateSunset(adapter, cal, jewishDate, offset)
        populateTwilight(adapter, cal, jewishDate, offset)
        populateNightfall(adapter, cal, jewishDate, offset)
    }

    private fun populateFastEnds(
        adapter: A,
        cal: ComplexZmanimCalendar,
        jewishDate: JewishDate,
        settings: ZmanimPreferences
    ) {
        val offset = settings.fastEnds * DateUtils.MINUTE_IN_MILLIS
        populateSunset(adapter, cal, jewishDate, offset)
        populateTwilight(adapter, cal, jewishDate, offset)
        populateNightfall(adapter, cal, jewishDate, offset)
    }
}
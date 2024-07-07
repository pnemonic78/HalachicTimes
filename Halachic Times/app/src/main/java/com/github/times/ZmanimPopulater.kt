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
import android.util.Pair
import com.github.times.ZmanimDays.NO_HOLIDAY
import com.github.times.ZmanimDays.SHABBATH
import com.github.times.preference.ZmanimPreferences
import com.github.util.TimeUtils.isSameDay
import com.github.util.dayOfMonth
import com.github.util.era
import com.github.util.millisecond
import com.github.util.month
import com.github.util.year
import com.kosherjava.zmanim.AstronomicalCalendar
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import com.kosherjava.zmanim.util.GeoLocation
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.floor
import timber.log.Timber

/**
 * Populate a list of zmanim.
 *
 * @author Moshe Waisberg
 */
open class ZmanimPopulater<A : ZmanimAdapter<*>>(
    protected val context: Context,
    protected val settings: ZmanimPreferences
) {
    /**
     * Get the calendar.
     *
     * @return the calendar.
     */
    val calendar: ComplexZmanimCalendar = ComplexZmanimCalendar().apply {
        setShaahZmanisType(settings.hourType)
        isUseElevation = settings.isUseElevation
    }
    private val calendarTemp = Calendar.getInstance()

    /**
     * Is the current location in Israel?<br></br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @return `true` if user is in Israel - `false` otherwise.
     */
    var isInIsrael = false

    protected open fun prePopulate(adapter: A) {
        adapter.clear()
        val cal = calendar.clone() as ComplexZmanimCalendar
        adapter.calendar = cal
        adapter.inIsrael = isInIsrael
    }

    /**
     * Populate the list of times.
     *
     * @param adapter the adapter to populate.
     * @param remote  is for remote views?
     */
    fun populate(adapter: A?, remote: Boolean) {
        if (adapter == null) {
            Timber.e("adapter required to populate")
            return
        }
        prePopulate(adapter)
        populateImpl(adapter, remote, context, settings)
    }

    /**
     * Populate the list of times - implementation.
     *
     * @param adapter  the adapter to populate.
     * @param remote   is for remote views?
     * @param context  the context.
     * @param settings the preferences.
     */
    protected open fun populateImpl(
        adapter: A,
        remote: Boolean,
        context: Context,
        settings: ZmanimPreferences
    ) {
        val cal = calendar
        val gcal = cal.calendar
        if (gcal.era < GregorianCalendar.AD) {
            // Ignore potential "IllegalArgumentException".
            return
        }
        val calYesterday = cloneZmanimYesterday(cal)
        val calTomorrow = cloneZmanimTomorrow(cal)
        val jcal = jewishCalendar
        if (jcal == null || jcal.jewishYear < 0) {
            // Ignore potential "IllegalArgumentException".
            return
        }
        val jewishDate = jcal.clone() as JewishCalendar
        val jewishDayOfMonth = jewishDate.jewishDayOfMonth
        val dayOfWeek = jewishDate.dayOfWeek
        val jewishDateTomorrow = cloneJewishTomorrow(jewishDate)
        val shabbathAfter = settings.shabbathEndsAfter
        val shabbathOffset = settings.shabbathEnds
        val candles = calculateCandles(jewishDate, jewishDateTomorrow, settings)
        val candlesTomorrow = candles.countTomorrow
        val holidayToday = candles.holidayToday
        val holidayTomorrow = candles.holidayTomorrow
        val candlesOffset = candles.candlesOffset
        val candlesWhen = candles.whenCandles
        adapter.candles = candles
        var date: KosherDate
        var summary: Int
        var dateAndSummary: Pair<KosherDate, Int>
        var summaryText: CharSequence?
        val res = context.resources
        val shabbathAfterName = res.getString(shabbathAfter)
        val calDate = Calendar.getInstance(gcal.timeZone)
        if (!remote && settings.isHour) {
            val time: TimeMillis

            when (settings.hour) {
                OPINION_19_8 -> {
                    time = cal.shaahZmanis19Point8Degrees
                    summary = R.string.hour_19_8
                }

                OPINION_120 -> {
                    time = cal.shaahZmanis120Minutes
                    summary = R.string.hour_120
                }

                OPINION_120_ZMANIS -> {
                    time = cal.shaahZmanis120MinutesZmanis
                    summary = R.string.hour_120_zmanis
                }

                OPINION_18 -> {
                    time = cal.shaahZmanis18Degrees
                    summary = R.string.hour_18
                }

                OPINION_26 -> {
                    time = cal.shaahZmanis26Degrees
                    summary = R.string.hour_26
                }

                OPINION_16_1 -> {
                    time = cal.shaahZmanis16Point1Degrees
                    summary = R.string.hour_16
                }

                OPINION_96 -> {
                    time = cal.shaahZmanis96Minutes
                    summary = R.string.hour_96
                }

                OPINION_96_ZMANIS -> {
                    time = cal.shaahZmanis96MinutesZmanis
                    summary = R.string.hour_96_zmanis
                }

                OPINION_90 -> {
                    time = cal.shaahZmanis90Minutes
                    summary = R.string.hour_90
                }

                OPINION_90_ZMANIS -> {
                    time = cal.shaahZmanis90MinutesZmanis
                    summary = R.string.hour_90_zmanis
                }

                OPINION_72 -> {
                    time = cal.shaahZmanis72Minutes
                    summary = R.string.hour_72
                }

                OPINION_72_ZMANIS -> {
                    time = cal.shaahZmanis72MinutesZmanis
                    summary = R.string.hour_72_zmanis
                }

                OPINION_60 -> {
                    time = cal.shaahZmanis60Minutes
                    summary = R.string.hour_60
                }

                OPINION_ATERET -> {
                    time = cal.shaahZmanisAteretTorah
                    summary = R.string.hour_ateret
                }

                OPINION_MGA -> {
                    time = cal.shaahZmanisMGA
                    summary = R.string.hour_mga
                }

                OPINION_BAAL_HATANYA -> {
                    time = cal.shaahZmanisBaalHatanya
                    summary = R.string.hour_baal_hatanya
                }

                else -> {
                    time = cal.shaahZmanisGra
                    summary = R.string.hour_gra
                }
            }
            // Offset is added back when formatted.
            adapter.addHour(R.string.hour, summary, time - gcal.timeZone.rawOffset, remote)
        }

        when (settings.dawn) {
            OPINION_19 -> {
                date = cal.alos19Degrees
                summary = R.string.dawn_19
            }

            OPINION_19_8 -> {
                date = cal.alos19Point8Degrees
                summary = R.string.dawn_19_8
            }

            OPINION_120 -> {
                date = cal.alos120
                summary = R.string.dawn_120
            }

            OPINION_120_ZMANIS -> {
                date = cal.alos120Zmanis
                summary = R.string.dawn_120_zmanis
            }

            OPINION_18 -> {
                date = cal.alos18Degrees
                summary = R.string.dawn_18
            }

            OPINION_26 -> {
                date = cal.alos26Degrees
                summary = R.string.dawn_26
            }

            OPINION_16_1 -> {
                date = cal.alos16Point1Degrees
                summary = R.string.dawn_16
            }

            OPINION_96 -> {
                date = cal.alos96
                summary = R.string.dawn_96
            }

            OPINION_96_ZMANIS -> {
                date = cal.alos96Zmanis
                summary = R.string.dawn_96_zmanis
            }

            OPINION_90 -> {
                date = cal.alos90
                summary = R.string.dawn_90
            }

            OPINION_90_ZMANIS -> {
                date = cal.alos90Zmanis
                summary = R.string.dawn_90_zmanis
            }

            OPINION_72 -> {
                date = cal.alos72
                summary = R.string.dawn_72
            }

            OPINION_72_ZMANIS -> {
                date = cal.alos72Zmanis
                summary = R.string.dawn_72_zmanis
            }

            OPINION_60 -> {
                date = cal.alos60
                summary = R.string.dawn_60
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.alosBaalHatanya
                summary = R.string.dawn_baal_hatanya
            }

            else -> {
                date = cal.alosHashachar
                summary = R.string.dawn_16
            }
        }
        if (date == null) {
            date = cal.alos120Zmanis
            summary = R.string.dawn_120_zmanis
        }
        adapter.add(R.string.dawn, summary, date, jewishDate, remote)
        val dawn = toTime(date)

        when (settings.tallis) {
            OPINION_10_2 -> {
                date = cal.misheyakir10Point2Degrees
                summary = R.string.tallis_10
            }

            OPINION_11 -> {
                date = cal.misheyakir11Degrees
                summary = R.string.tallis_11
            }

            OPINION_7_65 -> {
                date = cal.misheyakir7Point65Degrees
                summary = R.string.tallis_7_65
            }

            OPINION_9_5 -> {
                date = cal.misheyakir9Point5Degrees
                summary = R.string.tallis_9_5
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.misheyakir10Point2Degrees
                summary = R.string.tallis_baal_hatanya
            }

            else -> {
                date = cal.misheyakir11Point5Degrees
                summary = R.string.tallis_11_5
            }
        }
        var tallisTitle = R.string.tallis
        when (holidayToday) {
            SHABBATH,
            JewishCalendar.PESACH,
            JewishCalendar.CHOL_HAMOED_PESACH,
            JewishCalendar.SHAVUOS,
            JewishCalendar.ROSH_HASHANA,
            JewishCalendar.YOM_KIPPUR,
            JewishCalendar.SUCCOS,
            JewishCalendar.CHOL_HAMOED_SUCCOS,
            JewishCalendar.HOSHANA_RABBA,
            JewishCalendar.SHEMINI_ATZERES,
            JewishCalendar.SIMCHAS_TORAH,
            JewishCalendar.TISHA_BEAV -> tallisTitle = R.string.tallis_only
        }
        adapter.add(tallisTitle, summary, date, jewishDate, remote)

        dateAndSummary = getSunrise(cal, settings.sunrise)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        adapter.add(R.string.sunrise, summary, date, jewishDate, remote)
        val sunrise = date
        val sunriseTomorrow = getSunrise(calTomorrow, settings)

        when (settings.lastShema) {
            OPINION_16_1_SUNSET -> {
                date = cal.sofZmanShmaAlos16Point1ToSunset
                summary = R.string.shema_16_sunset
            }

            OPINION_7_083 -> {
                date = cal.sofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees
                summary = R.string.shema_7
            }

            OPINION_19_8 -> {
                date = cal.sofZmanShmaMGA19Point8Degrees
                summary = R.string.shema_19_8
            }

            OPINION_120 -> {
                date = cal.sofZmanShmaMGA120Minutes
                summary = R.string.shema_120
            }

            OPINION_18 -> {
                date = cal.sofZmanShmaMGA18Degrees
                summary = R.string.shema_18
            }

            OPINION_96 -> {
                date = cal.sofZmanShmaMGA96Minutes
                summary = R.string.shema_96
            }

            OPINION_96_ZMANIS -> {
                date = cal.sofZmanShmaMGA96MinutesZmanis
                summary = R.string.shema_96_zmanis
            }

            OPINION_16_1 -> {
                date = cal.sofZmanShmaMGA16Point1Degrees
                summary = R.string.shema_16
            }

            OPINION_90 -> {
                date = cal.sofZmanShmaMGA90Minutes
                summary = R.string.shema_90
            }

            OPINION_90_ZMANIS -> {
                date = cal.sofZmanShmaMGA90MinutesZmanis
                summary = R.string.shema_90_zmanis
            }

            OPINION_72 -> {
                date = cal.sofZmanShmaMGA72Minutes
                summary = R.string.shema_72
            }

            OPINION_72_ZMANIS -> {
                date = cal.sofZmanShmaMGA72MinutesZmanis
                summary = R.string.shema_72_zmanis
            }

            OPINION_MGA -> {
                date = cal.sofZmanShmaMGA
                summary = R.string.shema_mga
            }

            OPINION_ATERET -> {
                date = cal.sofZmanShmaAteretTorah
                summary = R.string.shema_ateret
            }

            OPINION_3 -> {
                date = cal.sofZmanShma3HoursBeforeChatzos
                summary = R.string.shema_3
            }

            OPINION_FIXED -> {
                date = cal.sofZmanShmaFixedLocal
                summary = R.string.shema_fixed
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.sofZmanShmaBaalHatanya
                summary = R.string.shema_baal_hatanya
            }

            else -> {
                date = cal.sofZmanShmaGRA
                summary = R.string.shema_gra
            }
        }
        adapter.add(R.string.shema, summary, date, jewishDate, remote)

        when (settings.lastTfila) {
            OPINION_120 -> {
                date = cal.sofZmanTfilaMGA120Minutes
                summary = R.string.prayers_120
            }

            OPINION_96 -> {
                date = cal.sofZmanTfilaMGA96Minutes
                summary = R.string.prayers_96
            }

            OPINION_96_ZMANIS -> {
                date = cal.sofZmanTfilaMGA96MinutesZmanis
                summary = R.string.prayers_96_zmanis
            }

            OPINION_19_8 -> {
                date = cal.sofZmanTfilaMGA19Point8Degrees
                summary = R.string.prayers_19_8
            }

            OPINION_90 -> {
                date = cal.sofZmanTfilaMGA90Minutes
                summary = R.string.prayers_90
            }

            OPINION_90_ZMANIS -> {
                date = cal.sofZmanTfilaMGA90MinutesZmanis
                summary = R.string.prayers_90_zmanis
            }

            OPINION_ATERET -> {
                date = cal.sofZmanTfilahAteretTorah
                summary = R.string.prayers_ateret
            }

            OPINION_18 -> {
                date = cal.sofZmanTfilaMGA18Degrees
                summary = R.string.prayers_18
            }

            OPINION_FIXED -> {
                date = cal.sofZmanTfilaFixedLocal
                summary = R.string.prayers_fixed
            }

            OPINION_16_1 -> {
                date = cal.sofZmanTfilaMGA16Point1Degrees
                summary = R.string.prayers_16
            }

            OPINION_72 -> {
                date = cal.sofZmanTfilaMGA72Minutes
                summary = R.string.prayers_72
            }

            OPINION_72_ZMANIS -> {
                date = cal.sofZmanTfilaMGA72MinutesZmanis
                summary = R.string.prayers_72_zmanis
            }

            OPINION_2 -> {
                date = cal.sofZmanTfila2HoursBeforeChatzos
                summary = R.string.prayers_2
            }

            OPINION_MGA -> {
                date = cal.sofZmanTfilaMGA
                summary = R.string.prayers_mga
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.sofZmanTfilaBaalHatanya
                summary = R.string.prayers_baal_hatanya
            }

            else -> {
                date = cal.sofZmanTfilaGRA
                summary = R.string.prayers_gra
            }
        }
        adapter.add(R.string.prayers, summary, date, jewishDate, remote)

        dateAndSummary = getMidday(cal, settings.midday)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        adapter.add(R.string.midday, summary, date, jewishDate, remote)
        val midday: KosherDate = date
        val middayYesterday = getMidday(calYesterday, settings.midday).first

        when (settings.earliestMincha) {
            OPINION_16_1 -> {
                date = cal.minchaGedola16Point1Degrees
                summary = R.string.earliest_mincha_16
            }

            OPINION_30 -> {
                date = cal.minchaGedola30Minutes
                summary = R.string.earliest_mincha_30
            }

            OPINION_ATERET -> {
                date = cal.minchaGedolaAteretTorah
                summary = R.string.earliest_mincha_ateret
            }

            OPINION_72 -> {
                date = cal.minchaGedola72Minutes
                summary = R.string.earliest_mincha_72
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.minchaGedolaBaalHatanyaGreaterThan30
                summary = R.string.earliest_mincha_baal_hatanya
            }

            else -> {
                date = cal.minchaGedola
                summary = R.string.earliest_mincha_greater
            }
        }
        adapter.add(R.string.earliest_mincha, summary, date, jewishDate, remote)

        when (settings.mincha) {
            OPINION_16_1 -> {
                date = cal.minchaKetana16Point1Degrees
                summary = R.string.mincha_16
            }

            OPINION_72 -> {
                date = cal.minchaKetana72Minutes
                summary = R.string.mincha_72
            }

            OPINION_ATERET -> {
                date = cal.minchaKetanaAteretTorah
                summary = R.string.mincha_ateret
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.minchaKetanaBaalHatanya
                summary = R.string.mincha_baal_hatanya
            }

            else -> {
                date = cal.minchaKetana
                summary = R.string.mincha_lesser
            }
        }
        adapter.add(R.string.mincha, summary, date, jewishDate, remote)

        when (settings.plugHamincha) {
            OPINION_16_1_SUNSET -> {
                date = cal.plagAlosToSunset
                summary = R.string.plug_hamincha_16_sunset
            }

            OPINION_16_1_ALOS -> {
                date = cal.plagAlos16Point1ToTzaisGeonim7Point083Degrees
                summary = R.string.plug_hamincha_16_alos
            }

            OPINION_ATERET -> {
                date = cal.plagHaminchaAteretTorah
                summary = R.string.plug_hamincha_ateret
            }

            OPINION_60 -> {
                date = cal.plagHamincha60Minutes
                summary = R.string.plug_hamincha_60
            }

            OPINION_72 -> {
                date = cal.plagHamincha72Minutes
                summary = R.string.plug_hamincha_72
            }

            OPINION_72_ZMANIS -> {
                date = cal.plagHamincha72MinutesZmanis
                summary = R.string.plug_hamincha_72_zmanis
            }

            OPINION_16_1 -> {
                date = cal.plagHamincha16Point1Degrees
                summary = R.string.plug_hamincha_16
            }

            OPINION_18 -> {
                date = cal.plagHamincha18Degrees
                summary = R.string.plug_hamincha_18
            }

            OPINION_90 -> {
                date = cal.plagHamincha90Minutes
                summary = R.string.plug_hamincha_90
            }

            OPINION_90_ZMANIS -> {
                date = cal.plagHamincha90MinutesZmanis
                summary = R.string.plug_hamincha_90_zmanis
            }

            OPINION_19_8 -> {
                date = cal.plagHamincha19Point8Degrees
                summary = R.string.plug_hamincha_19_8
            }

            OPINION_96 -> {
                date = cal.plagHamincha96Minutes
                summary = R.string.plug_hamincha_96
            }

            OPINION_96_ZMANIS -> {
                date = cal.plagHamincha96MinutesZmanis
                summary = R.string.plug_hamincha_96_zmanis
            }

            OPINION_120 -> {
                date = cal.plagHamincha120Minutes
                summary = R.string.plug_hamincha_120
            }

            OPINION_120_ZMANIS -> {
                date = cal.plagHamincha120MinutesZmanis
                summary = R.string.plug_hamincha_120_zmanis
            }

            OPINION_26 -> {
                date = cal.plagHamincha26Degrees
                summary = R.string.plug_hamincha_26
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.plagHaminchaBaalHatanya
                summary = R.string.plug_hamincha_baal_hatanya
            }

            else -> {
                date = cal.plagHamincha
                summary = R.string.plug_hamincha_gra
            }
        }
        adapter.add(R.string.plug_hamincha, summary, date, jewishDate, remote)

        dateAndSummary = getSunset(cal, settings.sunset)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        adapter.add(R.string.sunset, summary, date, jewishDate, remote)
        val sunset = date
        val summarySunset = summary
        val opinionSunset = settings.sunset

        dateAndSummary = getTwilight(cal, settings.twilight)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        adapter.add(R.string.twilight, summary, date, jewishDateTomorrow, remote)
        val twilight: KosherDate = date
        val summaryTwilight = summary

        var opinionNightfall = settings.nightfall
        dateAndSummary = getNightfall(cal, opinionNightfall)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        adapter.add(R.string.nightfall, summary, date, jewishDateTomorrow, remote)
        val nightfall: KosherDate = date
        val summaryNightfall = summary
        val nightfallYesterday = getNightfall(calYesterday, opinionNightfall).first
        if (shabbathAfter == R.string.sunset) {
            opinionNightfall = settings.shabbathEndsSunset
            date = if (OPINION_NONE == opinionNightfall) {
                sunset
            } else {
                getSunset(cal, opinionNightfall).first
            }
        } else if (shabbathAfter == R.string.twilight) {
            opinionNightfall = settings.shabbathEndsTwilight
            date = if (OPINION_NONE == opinionNightfall) {
                twilight
            } else {
                getTwilight(cal, opinionNightfall).first
            }
        } else {
            opinionNightfall = settings.shabbathEndsNightfall
            date = if (OPINION_NONE == opinionNightfall) {
                nightfall
            } else {
                getNightfall(cal, opinionNightfall).first
            }
        }
        var shabbatEnds: KosherDate = NEVER
        var summaryShabbatEnds: CharSequence? = null
        if (date.isDate()) {
            if (dayOfWeek == Calendar.SATURDAY) {
                shabbatEnds = AstronomicalCalendar.getTimeOffset(
                    date,
                    shabbathOffset * DateUtils.MINUTE_IN_MILLIS
                )
                summaryText = res.getQuantityString(
                    R.plurals.shabbath_ends_summary,
                    shabbathOffset,
                    shabbathOffset,
                    shabbathAfterName
                )
                adapter.add(
                    R.string.shabbath_ends,
                    summaryText,
                    shabbatEnds,
                    jewishDateTomorrow,
                    remote
                )
                summaryShabbatEnds = summaryText
            } else if (holidayToday >= 0) {
                when (holidayToday) {
                    JewishCalendar.PESACH, JewishCalendar.SHAVUOS, JewishCalendar.ROSH_HASHANA, JewishCalendar.YOM_KIPPUR, JewishCalendar.SUCCOS, JewishCalendar.SHEMINI_ATZERES, JewishCalendar.SIMCHAS_TORAH -> {
                        shabbatEnds = AstronomicalCalendar.getTimeOffset(
                            date,
                            shabbathOffset * DateUtils.MINUTE_IN_MILLIS
                        )
                        summaryText = res.getQuantityString(
                            R.plurals.shabbath_ends_summary,
                            shabbathOffset,
                            shabbathOffset,
                            shabbathAfterName
                        )
                        adapter.add(
                            R.string.festival_ends,
                            summaryText,
                            shabbatEnds,
                            jewishDateTomorrow,
                            remote
                        )
                        summaryShabbatEnds = summaryText
                    }
                }
            }
        }
        if (candlesTomorrow > 0) {
            when (candlesWhen) {
                CandleData.BEFORE_SUNSET -> if (sunset.isDate()) {
                    date = sunset - candlesOffset * DateUtils.MINUTE_IN_MILLIS
                    summaryText = if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                        res.getQuantityString(
                            R.plurals.candles_chanukka,
                            candlesTomorrow,
                            candlesTomorrow
                        )
                    } else {
                        res.getQuantityString(
                            R.plurals.candles_summary,
                            candlesOffset,
                            candlesOffset
                        )
                    }
                    adapter.add(R.string.candles, summaryText, date, jewishDate, remote)
                }

                CandleData.AT_SUNSET -> if (sunset.isDate()) {
                    if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                        summaryText = res.getQuantityString(
                            R.plurals.candles_chanukka,
                            candlesTomorrow,
                            candlesTomorrow
                        )
                        adapter.add(R.string.candles, summaryText, sunset, jewishDate, remote)
                    } else {
                        adapter.add(R.string.candles, summarySunset, sunset, jewishDate, remote)
                    }
                }

                CandleData.AT_TWILIGHT -> if (twilight.isDate()) {
                    if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                        summaryText = res.getQuantityString(
                            R.plurals.candles_chanukka,
                            candlesTomorrow,
                            candlesTomorrow
                        )
                        adapter.add(
                            R.string.candles,
                            summaryText,
                            twilight,
                            jewishDateTomorrow,
                            remote
                        )
                    } else {
                        adapter.add(
                            R.string.candles,
                            summaryTwilight,
                            twilight,
                            jewishDateTomorrow,
                            remote
                        )
                    }
                }

                CandleData.AT_NIGHT -> if (nightfall.isDate()) {
                    if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                        summaryText = res.getQuantityString(
                            R.plurals.candles_chanukka,
                            candlesTomorrow,
                            candlesTomorrow
                        )
                        adapter.add(
                            R.string.candles,
                            summaryText,
                            nightfall,
                            jewishDateTomorrow,
                            remote
                        )
                    } else {
                        adapter.add(
                            R.string.candles,
                            summaryNightfall,
                            nightfall,
                            jewishDateTomorrow,
                            remote
                        )
                    }
                }

                CandleData.MOTZE_SHABBATH -> if (shabbatEnds.isDate()) {
                    if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                        summaryText = res.getQuantityString(
                            R.plurals.candles_chanukka,
                            candlesTomorrow,
                            candlesTomorrow
                        )
                        adapter.add(
                            R.string.candles,
                            summaryText,
                            shabbatEnds,
                            jewishDateTomorrow,
                            remote
                        )
                    } else {
                        adapter.add(
                            R.string.candles,
                            summaryShabbatEnds,
                            shabbatEnds,
                            jewishDateTomorrow,
                            remote
                        )
                    }
                }
            }
        }

        val opinionMidnight = settings.midnight
        dateAndSummary = getMidnight(cal, opinionMidnight, midday, nightfall)
        date = dateAndSummary.first
        summary = dateAndSummary.second
        val midnightTomorrow = date
        dateAndSummary =
            getMidnight(calYesterday, opinionMidnight, middayYesterday, nightfallYesterday)
        date = dateAndSummary.first
        val midnight = date
        if (date != null && date < sunset) {
            calDate.time = date
            if (isSameDay(gcal, calDate)) {
                adapter.add(R.string.midnight, summary, date, jewishDate, remote)
            } else {
                date = midnightTomorrow
                adapter.add(R.string.midnight, summary, date, jewishDateTomorrow, remote)
            }
        } else {
            date = midnightTomorrow
            adapter.add(R.string.midnight, summary, date, jewishDateTomorrow, remote)
        }

        when (settings.guardsCount) {
            OPINION_4 -> {
                date = getMidnightGuard4(sunset, midnightTomorrow)
                summary = R.string.guard_second
            }

            else -> {
                date = getMidnightGuard3(sunset, sunriseTomorrow)
                summary = R.string.guard_second
            }
        }
        adapter.add(R.string.midnight_guard, summary, date, jewishDateTomorrow, remote)

        when (settings.guardsCount) {
            OPINION_4 -> {
                summary = R.string.guard_fourth
                if (midnight != null && midnight < sunrise) {
                    date = getMorningGuard4(midnight, sunrise)
                    adapter.add(R.string.morning_guard, summary, date, jewishDate, remote)
                } else {
                    date = getMorningGuard4(midnightTomorrow, sunriseTomorrow)
                    adapter.add(R.string.morning_guard, summary, date, jewishDateTomorrow, remote)
                }
            }

            else -> {
                summary = R.string.guard_third
                date = getMorningGuard3(sunset, sunriseTomorrow)
                calDate.time = date
                if (isSameDay(gcal, calDate)) {
                    adapter.add(R.string.morning_guard, summary, date, jewishDateTomorrow, remote)
                } else {
                    val sunsetYesterday = getSunset(calYesterday, opinionSunset).first
                    date = getMorningGuard3(sunsetYesterday, sunrise)
                    adapter.add(R.string.morning_guard, summary, date, jewishDate, remote)
                }
            }
        }

        when (holidayToday) {
            JewishCalendar.EREV_PESACH -> {
                when (settings.eatChametz) {
                    OPINION_16_1 -> {
                        date = cal.sofZmanAchilasChametzMGA16Point1Degrees
                        summary = R.string.eat_chametz_16
                    }

                    OPINION_72 -> {
                        date = cal.sofZmanAchilasChametzMGA72Minutes
                        summary = R.string.eat_chametz_72
                    }

                    OPINION_BAAL_HATANYA -> {
                        date = cal.sofZmanAchilasChametzBaalHatanya
                        summary = R.string.eat_chametz_baal_hatanya
                    }

                    else -> {
                        date = cal.sofZmanAchilasChametzGRA
                        summary = R.string.eat_chametz_gra
                    }
                }
                adapter.add(R.string.eat_chametz, summary, date, jewishDate, remote)
            }

            JewishCalendar.SEVENTEEN_OF_TAMMUZ,
            JewishCalendar.FAST_OF_GEDALYAH,
            JewishCalendar.TENTH_OF_TEVES,
            JewishCalendar.FAST_OF_ESTHER -> {
                adapter.add(R.string.fast_begins, SUMMARY_NONE, dawn, jewishDate, remote)
                adapter.add(R.string.fast_ends, SUMMARY_NONE, twilight, jewishDateTomorrow, remote)
            }

            JewishCalendar.TISHA_BEAV -> adapter.add(
                R.string.fast_ends,
                SUMMARY_NONE,
                twilight,
                jewishDateTomorrow,
                remote
            )

            JewishCalendar.YOM_KIPPUR -> adapter.add(
                R.string.fast_ends,
                SUMMARY_NONE,
                shabbatEnds,
                jewishDateTomorrow,
                remote
            )
        }
        when (holidayTomorrow) {
            JewishCalendar.TISHA_BEAV -> adapter.add(
                R.string.fast_begins,
                SUMMARY_NONE,
                sunset,
                jewishDate,
                remote
            )

            JewishCalendar.YOM_KIPPUR -> adapter.add(
                R.string.fast_begins,
                SUMMARY_NONE,
                AstronomicalCalendar.getTimeOffset(
                    sunset,
                    -candlesOffset * DateUtils.MINUTE_IN_MILLIS
                ),
                jewishDate,
                remote
            )
        }

        // Not allowed to burn chametz on Shabbat.
        if (holidayToday == JewishCalendar.EREV_PESACH && dayOfWeek != Calendar.SATURDAY || holidayTomorrow == JewishCalendar.EREV_PESACH && dayOfWeek == Calendar.FRIDAY) {
            when (settings.burnChametz) {
                OPINION_16_1 -> {
                    date = cal.sofZmanBiurChametzMGA16Point1Degrees
                    summary = R.string.burn_chametz_16
                }

                OPINION_72 -> {
                    date = cal.sofZmanBiurChametzMGA72Minutes
                    summary = R.string.burn_chametz_72
                }

                OPINION_BAAL_HATANYA -> {
                    date = cal.sofZmanBiurChametzBaalHatanya
                    summary = R.string.burn_chametz_baal_hatanya
                }

                else -> {
                    date = cal.sofZmanBiurChametzGRA
                    summary = R.string.burn_chametz_gra
                }
            }
            adapter.add(R.string.burn_chametz, summary, date, jewishDate, remote)
        }

        // Molad.
        if (jewishDayOfMonth <= 1 || jewishDayOfMonth >= 25) {
            val y = gcal.year
            val m = gcal.month
            val d = gcal.dayOfMonth

            // Molad is always of the previous month.
            val jLastDayOfMonth = jcal.daysInJewishMonth
            if (jewishDayOfMonth > 1 && jewishDayOfMonth < jLastDayOfMonth) {
                jcal.setJewishDate(jcal.jewishYear, jcal.jewishMonth, jLastDayOfMonth)
            }
            var molad = jcal.molad
            var moladYear = molad.gregorianYear
            var moladMonth = molad.gregorianMonth
            var moladDay = molad.gregorianDayOfMonth
            if (moladYear != y || moladMonth != m || moladDay != d) {
                jcal.forward(Calendar.DATE, 1)
                molad = jcal.molad
                moladYear = molad.gregorianYear
                moladMonth = molad.gregorianMonth
                moladDay = molad.gregorianDayOfMonth
            }
            if (moladYear == y && moladMonth == m && moladDay == d) {
                val moladSeconds = (molad.moladChalakim * 10.0) / 3.0
                val moladSecondsFloor = floor(moladSeconds)
                val calMolad = gcal.clone() as Calendar
                calMolad[moladYear, moladMonth, moladDay, molad.moladHours, molad.moladMinutes] =
                    moladSecondsFloor.toInt()
                calMolad.millisecond =
                    (DateUtils.SECOND_IN_MILLIS * (moladSeconds - moladSecondsFloor)).toInt()
                summary = R.string.molad_average
                val moladTime = calMolad.timeInMillis
                var moonDate: JewishDate = jewishDate
                if (sunset.isDate() && sunset < moladTime) {
                    moonDate = jewishDateTomorrow
                }
                adapter.add(R.string.molad, summary, moladTime, moonDate, remote)
            }
        } else if (jewishDayOfMonth in 2..8) {
            when (settings.earliestKiddushLevana) {
                OPINION_7,
                OPINION_168 -> {
                    date = cal.tchilasZmanKidushLevana7Days
                    summary = R.string.levana_7
                }

                else -> {
                    date = cal.tchilasZmanKidushLevana3Days
                    summary = R.string.levana_3
                }
            }
            if (date != null && isSameDay(gcal, date)) {
                var moonDate: JewishDate = jewishDate
                if (sunset.isDate() && sunset < date) {
                    moonDate = jewishDateTomorrow
                }
                adapter.add(R.string.levana_earliest, summary, date, moonDate, remote)
            }
        } else if (jewishDayOfMonth in 11..19) {
            when (settings.latestKiddushLevana) {
                OPINION_15,
                OPINION_15_ALOS -> {
                    date = cal.sofZmanKidushLevana15Days
                    summary = R.string.levana_15
                }

                else -> {
                    date = cal.sofZmanKidushLevanaBetweenMoldos
                    summary = R.string.levana_halfway
                }
            }
            if (date != null && isSameDay(gcal, date)) {
                var moonDate: JewishDate = jewishDate
                if (sunset.isDate() && sunset < date) {
                    moonDate = jewishDateTomorrow
                }
                adapter.add(R.string.levana_latest, summary, date, moonDate, remote)
            }
        }
        adapter.sort()
    }

    /**
     * Set the calendar.
     *
     * @param calendar the calendar.
     */
    fun setCalendar(calendar: Calendar) {
        this.calendar.calendar = calendar
    }

    /**
     * Set the calendar time.
     *
     * @param time the time in milliseconds.
     */
    fun setCalendar(time: Long) {
        calendar.calendar.timeInMillis = time
    }

    /**
     * Sets the [GeoLocation].
     *
     * @param geoLocation the location.
     */
    fun setGeoLocation(geoLocation: GeoLocation) {
        calendar.geoLocation = geoLocation
    }

    /**
     * Get the Jewish calendar.
     *
     * @return the calendar - `null` if date is invalid.
     */
    val jewishCalendar: JewishCalendar?
        get() {
            val gcal = calendar.calendar
            if (gcal.era < GregorianCalendar.AD) {
                // Avoid future "IllegalArgumentException".
                return null
            }
            return JewishCalendar(gcal).apply {
                inIsrael = isInIsrael
            }
        }

    /**
     * Get the Jewish date.
     *
     * @param date the civil date and time.
     * @return the date - `null` if time is invalid.
     */
    protected fun getJewishDate(date: KosherDate) = getJewishDate(date, calendar, settings)

    /**
     * Get the Jewish date.
     *
     * @param date the civil date and time.
     * @return the date - `null` if time is invalid.
     */
    protected fun getJewishDate(
        date: KosherDate,
        calendar: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ) = getJewishDate(date, getSunset(calendar, settings))

    /**
     * Get the Jewish date.
     *
     * @param date   the civil date and time.
     * @param sunset the sunset time.
     * @return the date - `null` if time is invalid.
     */
    private fun getJewishDate(date: KosherDate, sunset: KosherDate): JewishDate? {
        if (date == null) return null
        val cal = Calendar.getInstance().apply {
            time = date
        }
        val jewishDate = JewishDate(cal)
        if (date > sunset) {
            jewishDate.forward(Calendar.DATE, 1)
        }
        return jewishDate
    }

    private fun getSunrise(cal: ComplexZmanimCalendar, opinion: String?): Pair<KosherDate, Int> {
        val date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_SEA -> {
                date = cal.seaLevelSunrise
                summary = R.string.sunrise_sea
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.seaLevelSunrise
                summary = R.string.sunrise_baal_hatanya
            }

            else -> {
                date = cal.sunrise
                summary = R.string.sunrise_elevated
            }
        }
        return Pair.create(toTime(date), summary)
    }

    private fun getSunrise(cal: ComplexZmanimCalendar, settings: ZmanimPreferences): KosherDate {
        val opinion = settings.sunrise
        return getSunrise(cal, opinion).first
    }

    protected fun getSunriseTomorrow(
        cal: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ): KosherDate {
        val calTomorrow = cal.clone() as ComplexZmanimCalendar
        calTomorrow.calendar.add(Calendar.DATE, 1)
        return getSunrise(calTomorrow, settings)
    }

    protected fun getMidday(cal: ComplexZmanimCalendar, opinion: String?): Pair<KosherDate, Int> {
        val date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_FIXED -> {
                date = cal.fixedLocalChatzos
                summary = R.string.midday_fixed
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.chatzosBaalHatanya
                summary = R.string.midday_baal_hatanya
            }

            else -> {
                date = cal.chatzos
                summary = R.string.midday_solar
            }
        }
        return Pair.create(toTime(date), summary)
    }

    protected fun getMidday(cal: ComplexZmanimCalendar, settings: ZmanimPreferences): KosherDate {
        val opinion = settings.midday
        return getMidday(cal, opinion).first
    }

    private fun getSunset(cal: ComplexZmanimCalendar, opinion: String?): Pair<KosherDate, Int> {
        val date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_SEA -> {
                date = cal.seaLevelSunset
                summary = R.string.sunset_sea
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.seaLevelSunset
                summary = R.string.sunset_baal_hatanya
            }

            else -> {
                date = cal.sunset
                summary = R.string.sunset_elevated
            }
        }
        return Pair.create(toTime(date), summary)
    }

    protected fun getSunset(cal: ComplexZmanimCalendar, settings: ZmanimPreferences): KosherDate {
        val opinion = settings.sunset
        return getSunset(cal, opinion).first
    }

    private fun getTwilight(cal: ComplexZmanimCalendar, opinion: String?): Pair<KosherDate, Int> {
        val date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_7_083 -> {
                date = cal.bainHasmashosRT13Point5MinutesBefore7Point083Degrees
                summary = R.string.twilight_7_083
            }

            OPINION_58 -> {
                date = cal.bainHasmashosRT58Point5Minutes
                summary = R.string.twilight_58
            }

            OPINION_13 -> {
                date = cal.bainHasmashosRT13Point24Degrees
                summary = R.string.twilight_13
            }

            else -> {
                date = cal.bainHasmashosRT2Stars
                summary = R.string.twilight_2stars
            }
        }
        return Pair.create(toTime(date), summary)
    }

    private fun getNightfall(cal: ComplexZmanimCalendar, opinion: String?): Pair<KosherDate, Int> {
        val date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_120 -> {
                date = cal.tzais120
                summary = R.string.nightfall_120
            }

            OPINION_120_ZMANIS -> {
                date = cal.tzais120Zmanis
                summary = R.string.nightfall_120_zmanis
            }

            OPINION_16_1 -> {
                date = cal.tzais16Point1Degrees
                summary = R.string.nightfall_16
            }

            OPINION_18 -> {
                date = cal.tzais18Degrees
                summary = R.string.nightfall_18
            }

            OPINION_19_8 -> {
                date = cal.tzais19Point8Degrees
                summary = R.string.nightfall_19_8
            }

            OPINION_26 -> {
                date = cal.tzais26Degrees
                summary = R.string.nightfall_26
            }

            OPINION_60 -> {
                date = cal.tzais60
                summary = R.string.nightfall_60
            }

            OPINION_72 -> {
                date = cal.tzais72
                summary = R.string.nightfall_72
            }

            OPINION_72_ZMANIS -> {
                date = cal.tzais72Zmanis
                summary = R.string.nightfall_72_zmanis
            }

            OPINION_90 -> {
                date = cal.tzais90
                summary = R.string.nightfall_90
            }

            OPINION_90_ZMANIS -> {
                date = cal.tzais90Zmanis
                summary = R.string.nightfall_90_zmanis
            }

            OPINION_96 -> {
                date = cal.tzais96
                summary = R.string.nightfall_96
            }

            OPINION_96_ZMANIS -> {
                date = cal.tzais96Zmanis
                summary = R.string.nightfall_96_zmanis
            }

            OPINION_ATERET -> {
                date = cal.tzaisAteretTorah
                summary = R.string.nightfall_ateret
            }

            OPINION_3_65 -> {
                date = cal.tzaisGeonim3Point65Degrees
                summary = R.string.nightfall_3_65
            }

            OPINION_3_676 -> {
                date = cal.tzaisGeonim3Point676Degrees
                summary = R.string.nightfall_3_676
            }

            OPINION_3_7 -> {
                date = cal.tzaisGeonim3Point7Degrees
                summary = R.string.nightfall_3_7
            }

            OPINION_3_8 -> {
                date = cal.tzaisGeonim3Point8Degrees
                summary = R.string.nightfall_3_8
            }

            OPINION_4_37 -> {
                date = cal.tzaisGeonim4Point37Degrees
                summary = R.string.nightfall_4_37
            }

            OPINION_4_61 -> {
                date = cal.tzaisGeonim4Point61Degrees
                summary = R.string.nightfall_4_61
            }

            OPINION_4_8 -> {
                date = cal.tzaisGeonim4Point8Degrees
                summary = R.string.nightfall_4_8
            }

            OPINION_5_88 -> {
                date = cal.tzaisGeonim5Point88Degrees
                summary = R.string.nightfall_5_88
            }

            OPINION_5_95 -> {
                date = cal.tzaisGeonim5Point95Degrees
                summary = R.string.nightfall_5_95
            }

            OPINION_6_45 -> {
                date = cal.tzaisGeonim6Point45Degrees
                summary = R.string.nightfall_6_45
            }

            OPINION_7_083 -> {
                date = cal.tzaisGeonim7Point083Degrees
                summary = R.string.nightfall_7
            }

            OPINION_7_67 -> {
                date = cal.tzaisGeonim7Point67Degrees
                summary = R.string.nightfall_7_67
            }

            OPINION_8_5 -> {
                date = cal.tzaisGeonim8Point5Degrees
                summary = R.string.nightfall_8
            }

            OPINION_9_3 -> {
                date = cal.tzaisGeonim9Point3Degrees
                summary = R.string.nightfall_9_3
            }

            OPINION_9_75 -> {
                date = cal.tzaisGeonim9Point75Degrees
                summary = R.string.nightfall_9_75
            }

            OPINION_BAAL_HATANYA -> {
                date = cal.tzaisBaalHatanya
                summary = R.string.nightfall_baal_hatanya
            }

            else -> {
                date = cal.tzaisGeonim8Point5Degrees
                summary = R.string.nightfall_8
            }
        }
        return Pair.create(toTime(date), summary)
    }

    protected fun getNightfall(
        cal: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ): KosherDate {
        val opinion = settings.nightfall
        return getNightfall(cal, opinion).first
    }

    private fun getMidnight(
        cal: ComplexZmanimCalendar,
        opinion: String?,
        midday: KosherDate,
        nightfall: KosherDate
    ): Pair<KosherDate, Int> {
        var date: KosherDate
        val summary: Int
        when (opinion) {
            OPINION_12 -> {
                date = midday
                if (midday.isDate()) {
                    date += TWELVE_HOURS
                }
                summary = R.string.midnight_12
            }

            OPINION_6 -> {
                date = nightfall
                if (nightfall.isDate()) {
                    date += SIX_HOURS
                }
                summary = R.string.midnight_6
            }

            else -> {
                date = cal.solarMidnight
                summary = R.string.midnight_solar
            }
        }
        return Pair.create(toTime(date), summary)
    }

    protected fun getMidnight(cal: ComplexZmanimCalendar, settings: ZmanimPreferences): KosherDate {
        val opinion = settings.midnight
        val midday = getMidday(cal, settings)
        val nightfall = getNightfall(cal, settings)
        return getMidnight(cal, opinion, midday, nightfall).first
    }

    /**
     * A method that returns "the midnight guard" (ashmurat hatichona).
     *
     * Nocturnal guard is from sunset until 22:00.<br></br>
     * Midnight guard is from 22:00 until 02:00.<br></br>
     * Morning guard is from 02:00 until sunrise.
     *
     * @return the Second Guard.
     */
    protected fun getMidnightGuard(
        cal: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ): KosherDate {
        val sunset = getSunset(cal, settings)
        val opinion = settings.guardsCount
        return if (OPINION_4 == opinion) {
            getMidnightGuard4(sunset, getMidnight(cal, settings))
        } else {
            getMidnightGuard3(sunset, getSunriseTomorrow(cal, settings))
        }
    }

    protected fun getMidnightGuard3(sunset: KosherDate, sunrise: KosherDate): KosherDate {
        if (sunset.isNever() || sunrise.isNever()) {
            return NEVER
        }
        val night = sunrise - sunset
        return sunset + night / 3L
    }

    protected fun getMidnightGuard4(sunset: KosherDate, midnight: KosherDate): KosherDate {
        if (sunset.isNever() || midnight.isNever()) {
            return NEVER
        }
        return sunset + (midnight - sunset / 2L)
    }

    /**
     * A method that returns "the morning guard" (ashmurat haboker).
     *
     * Nocturnal guard is from sunset until 22:00.<br></br>
     * Midnight guard is from 22:00 until 02:00.<br></br>
     * Morning guard is from 02:00 until sunrise.
     *
     * @return the Third Guard.
     */
    protected fun getMorningGuard(
        cal: ComplexZmanimCalendar,
        settings: ZmanimPreferences
    ): KosherDate {
        val sunrise = getSunriseTomorrow(cal, settings)
        val opinion = settings.guardsCount
        return if (OPINION_4 == opinion) {
            getMorningGuard4(getMidnight(cal, settings), sunrise)
        } else {
            getMorningGuard3(getSunset(cal, settings), sunrise)
        }
    }

    protected fun getMorningGuard3(sunset: KosherDate, sunrise: KosherDate): KosherDate {
        if (sunset.isNever() || sunrise.isNever()) {
            return NEVER
        }
        val night = sunrise - sunset
        return sunset + (night * 2) / 3L //sunset + (night * 2 / 3)
    }

    protected fun getMorningGuard4(midnight: KosherDate, sunrise: KosherDate): KosherDate {
        if (midnight.isNever() || sunrise.isNever()) {
            return NEVER
        }
        return midnight + (sunrise - midnight / 2)
    }

    protected fun getSofZmanBiurChametz(startOfDay: KosherDate, shaahZmanis: Long): KosherDate {
        return AstronomicalCalendar.getTimeOffset(startOfDay, shaahZmanis * 5)
    }

    private fun toTime(date: KosherDate): KosherDate {
        return date ?: NEVER
    }

    private fun cloneZmanimTomorrow(cal: ComplexZmanimCalendar): ComplexZmanimCalendar {
        val calTomorrow = cal.clone() as ComplexZmanimCalendar
        calTomorrow.calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calTomorrow
    }

    protected fun cloneZmanimYesterday(cal: ComplexZmanimCalendar): ComplexZmanimCalendar {
        val calYesterday = cal.clone() as ComplexZmanimCalendar
        calYesterday.calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calYesterday
    }

    companion object {
        /**
         * 12 hours (half of a full day).
         */
        internal const val TWELVE_HOURS = DateUtils.DAY_IN_MILLIS shr 1

        /**
         * 6 hours (quarter of a full day).
         */
        internal const val SIX_HOURS = DateUtils.DAY_IN_MILLIS shr 2

        internal val OPINION_2 = ZmanimPreferences.Values.OPINION_2

        //TODO remove unused, and its strings
        internal val OPINION_2_STARS = ZmanimPreferences.Values.OPINION_2_STARS
        internal val OPINION_3 = ZmanimPreferences.Values.OPINION_3
        internal val OPINION_3_65 = ZmanimPreferences.Values.OPINION_3_65
        internal val OPINION_3_676 = ZmanimPreferences.Values.OPINION_3_676
        internal val OPINION_3_7 = ZmanimPreferences.Values.OPINION_3_7
        internal val OPINION_3_8 = ZmanimPreferences.Values.OPINION_3_8
        internal val OPINION_4 = ZmanimPreferences.Values.OPINION_4
        internal val OPINION_4_37 = ZmanimPreferences.Values.OPINION_4_37
        internal val OPINION_4_61 = ZmanimPreferences.Values.OPINION_4_61
        internal val OPINION_4_8 = ZmanimPreferences.Values.OPINION_4_8
        internal val OPINION_5_88 = ZmanimPreferences.Values.OPINION_5_88
        internal val OPINION_5_95 = ZmanimPreferences.Values.OPINION_5_95
        internal val OPINION_6 = ZmanimPreferences.Values.OPINION_6
        internal val OPINION_6_45 = ZmanimPreferences.Values.OPINION_6_45
        internal val OPINION_7 = ZmanimPreferences.Values.OPINION_7
        internal val OPINION_7_083 = ZmanimPreferences.Values.OPINION_7_083

        //TODO remove unused, and its strings
        internal val OPINION_7_083_ZMANIS = ZmanimPreferences.Values.OPINION_7_083_ZMANIS
        internal val OPINION_7_65 = ZmanimPreferences.Values.OPINION_7_65
        internal val OPINION_7_67 = ZmanimPreferences.Values.OPINION_7_67
        internal val OPINION_8_5 = ZmanimPreferences.Values.OPINION_8_5
        internal val OPINION_9_3 = ZmanimPreferences.Values.OPINION_9_3
        internal val OPINION_9_5 = ZmanimPreferences.Values.OPINION_9_5
        internal val OPINION_9_75 = ZmanimPreferences.Values.OPINION_9_75
        internal val OPINION_10_2 = ZmanimPreferences.Values.OPINION_10_2
        internal val OPINION_11 = ZmanimPreferences.Values.OPINION_11
        internal val OPINION_12 = ZmanimPreferences.Values.OPINION_12
        internal val OPINION_13 = ZmanimPreferences.Values.OPINION_13
        internal val OPINION_15 = ZmanimPreferences.Values.OPINION_15
        internal val OPINION_15_ALOS = ZmanimPreferences.Values.OPINION_15_ALOS
        internal val OPINION_16_1 = ZmanimPreferences.Values.OPINION_16_1
        internal val OPINION_16_1_ALOS = ZmanimPreferences.Values.OPINION_16_1_ALOS
        internal val OPINION_16_1_SUNSET = ZmanimPreferences.Values.OPINION_16_1_SUNSET
        internal val OPINION_18 = ZmanimPreferences.Values.OPINION_18
        internal val OPINION_19 = ZmanimPreferences.Values.OPINION_19
        internal val OPINION_19_8 = ZmanimPreferences.Values.OPINION_19_8
        internal val OPINION_26 = ZmanimPreferences.Values.OPINION_26
        internal val OPINION_30 = ZmanimPreferences.Values.OPINION_30
        internal val OPINION_58 = ZmanimPreferences.Values.OPINION_58
        internal val OPINION_60 = ZmanimPreferences.Values.OPINION_60
        internal val OPINION_72 = ZmanimPreferences.Values.OPINION_72
        internal val OPINION_72_ZMANIS = ZmanimPreferences.Values.OPINION_72_ZMANIS
        internal val OPINION_90 = ZmanimPreferences.Values.OPINION_90
        internal val OPINION_90_ZMANIS = ZmanimPreferences.Values.OPINION_90_ZMANIS
        internal val OPINION_96 = ZmanimPreferences.Values.OPINION_96
        internal val OPINION_96_ZMANIS = ZmanimPreferences.Values.OPINION_96_ZMANIS
        internal val OPINION_120 = ZmanimPreferences.Values.OPINION_120
        internal val OPINION_120_ZMANIS = ZmanimPreferences.Values.OPINION_120_ZMANIS
        internal val OPINION_168 = ZmanimPreferences.Values.OPINION_168
        internal val OPINION_ATERET = ZmanimPreferences.Values.OPINION_ATERET
        internal val OPINION_BAAL_HATANYA = ZmanimPreferences.Values.OPINION_BAAL_HATANYA
        internal val OPINION_FIXED = ZmanimPreferences.Values.OPINION_FIXED
        internal val OPINION_GRA = ZmanimPreferences.Values.OPINION_GRA

        //TODO remove unused, and its strings
        internal val OPINION_HALF = ZmanimPreferences.Values.OPINION_HALF

        //TODO remove unused, and its strings
        internal val OPINION_LEVEL = ZmanimPreferences.Values.OPINION_LEVEL
        internal val OPINION_MGA = ZmanimPreferences.Values.OPINION_MGA
        internal val OPINION_NIGHT = ZmanimPreferences.Values.OPINION_NIGHT
        internal val OPINION_NONE = ZmanimPreferences.Values.OPINION_NONE
        internal val OPINION_SEA = ZmanimPreferences.Values.OPINION_SEA
        internal val OPINION_TWILIGHT = ZmanimPreferences.Values.OPINION_TWILIGHT

        /**
         * No summary.
         */
        internal const val SUMMARY_NONE = ZmanimAdapter.SUMMARY_NONE

        /**
         * Calculate candle parameters, e.g. the number of candles to light.
         *
         * Some rules for showing candles:
         * <table>
         * <tr>
         * <th>Today \ Tomorrow</th>
         * <td>weekday</td>
         * <td>Saturday</td>
         * <td>festival</td>
         * <td>Yom Kippur</td>
         * <td>Channuka</td>
         * </tr>
         * <tr>
         * <td>weekday</td>
         * <td>x</td>
         * <td>sunset</td>
         * <td>sunset</td>
         * <td>sunset</td>
         * <td>Channuka</td>
         * </tr>
         * <tr>
         * <td>Saturday</td>
         * <td>x</td>
         * <td>x</td>
         * <td>Shabbath ends</td>
         * <td>x</td>
         * <td>Shabbath ends</td>
         * </tr>
         * <tr>
         * <td>festival</td>
         * <td>x</td>
         * <td>sunset</td>
         * <td>Shabbath ends</td>
         * <td>x</td>
         * <td>x</td>
         * </tr>
         * <tr>
         * <td>Yom Kippur</td>
         * <td>x</td>
         * <td>x</td>
         * <td>x</td>
         * <td>x</td>
         * <td>x</td>
         * </tr>
         * <tr>
         * <td>Channuka</td>
         * <td>x</td>
         * <td>sunset</td>
         * <td>x</td>
         * <td>x</td>
         * <td>Channuka</td>
         * </tr>
         * </table>
         *
         * @param jewishDateToday    the Jewish calendar for today.
         * @param jewishDateTomorrow the Jewish calendar for tomorrow.
         * @return the candle data.
         */
        fun calculateCandles(
            jewishDateToday: JewishCalendar?,
            jewishDateTomorrow: JewishCalendar?,
            settings: ZmanimPreferences
        ): CandleData {
            if (jewishDateToday == null) {
                return CandleData()
            }
            val dayOfWeek = jewishDateToday.dayOfWeek

            // Check if the following day is special, because we can't check EREV_CHANUKAH.
            var holidayToday = jewishDateToday.yomTovIndex
            var jcalTomorrow = jewishDateTomorrow
            if (jcalTomorrow == null) {
                jcalTomorrow = cloneJewishTomorrow(jewishDateToday)
            }
            var countToday = CandleData.CANDLES_NONE
            var holidayTomorrow = jcalTomorrow.yomTovIndex
            var countTomorrow = CandleData.CANDLES_NONE
            var whenCandles = CandleData.BEFORE_SUNSET
            val omerToday = jewishDateToday.dayOfOmer
            val omerTomorrow = jcalTomorrow.dayOfOmer
            when (holidayToday) {
                JewishCalendar.PESACH, JewishCalendar.SHAVUOS, JewishCalendar.ROSH_HASHANA, JewishCalendar.SUCCOS, JewishCalendar.SHEMINI_ATZERES, JewishCalendar.SIMCHAS_TORAH -> countToday =
                    CandleData.CANDLES_FESTIVAL

                JewishCalendar.YOM_KIPPUR -> countToday = CandleData.CANDLES_YOM_KIPPUR
                JewishCalendar.CHANUKAH -> countToday = jewishDateToday.dayOfChanukah
                else -> if (dayOfWeek == Calendar.SATURDAY) {
                    countToday = CandleData.CANDLES_SHABBATH
                }
            }
            when (holidayTomorrow) {
                JewishCalendar.PESACH,
                JewishCalendar.SHAVUOS,
                JewishCalendar.ROSH_HASHANA,
                JewishCalendar.SUCCOS,
                JewishCalendar.SHEMINI_ATZERES,
                JewishCalendar.SIMCHAS_TORAH -> countTomorrow = CandleData.CANDLES_FESTIVAL

                JewishCalendar.YOM_KIPPUR -> countTomorrow = CandleData.CANDLES_YOM_KIPPUR

                JewishCalendar.CHANUKAH -> {
                    countTomorrow = jcalTomorrow.dayOfChanukah
                    if (dayOfWeek != Calendar.FRIDAY && dayOfWeek != Calendar.SATURDAY) {
                        val opinion = settings.chanukkaCandles
                        whenCandles = if (OPINION_TWILIGHT == opinion) {
                            CandleData.AT_TWILIGHT
                        } else if (OPINION_NIGHT == opinion) {
                            CandleData.AT_NIGHT
                        } else {
                            CandleData.AT_SUNSET
                        }
                    }
                }

                else -> if (dayOfWeek == Calendar.FRIDAY) {
                    if (holidayTomorrow < 0) holidayTomorrow = SHABBATH
                    countTomorrow = CandleData.CANDLES_SHABBATH
                }
            }
            whenCandles = when (dayOfWeek) {
                Calendar.FRIDAY -> CandleData.BEFORE_SUNSET

                Calendar.SATURDAY -> {
                    if (holidayToday == NO_HOLIDAY) {
                        holidayToday = SHABBATH
                    }
                    CandleData.MOTZE_SHABBATH
                }

                else -> when (holidayToday) {
                    JewishCalendar.PESACH,
                    JewishCalendar.SHAVUOS,
                    JewishCalendar.ROSH_HASHANA,
                    JewishCalendar.SUCCOS,
                    JewishCalendar.SHEMINI_ATZERES,
                    JewishCalendar.SIMCHAS_TORAH -> CandleData.MOTZE_SHABBATH

                    else -> whenCandles
                }
            }

            val candlesOffset = settings.candleLightingOffset
            return CandleData(
                holidayToday,
                countToday,
                holidayTomorrow,
                countTomorrow,
                whenCandles,
                candlesOffset,
                omerToday,
                omerTomorrow
            )
        }

        internal fun cloneJewishTomorrow(jcal: JewishCalendar): JewishCalendar {
            val jcalTomorrow = jcal.clone() as JewishCalendar
            jcalTomorrow.forward(Calendar.DATE, 1)
            return jcalTomorrow
        }
    }
}

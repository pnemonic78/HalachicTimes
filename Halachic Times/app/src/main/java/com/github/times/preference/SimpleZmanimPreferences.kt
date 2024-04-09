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
package com.github.times.preference

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.text.format.DateUtils
import androidx.annotation.StyleRes
import com.github.content.isNightMode
import com.github.media.RingtoneManager
import com.github.preference.LocalePreferences
import com.github.preference.SimpleLocalePreferences
import com.github.preference.SimplePreferences
import com.github.preference.SimpleThemePreferences
import com.github.preference.ThemePreferences
import com.github.preference.ThemePreferences.Values.THEME_DARK
import com.github.preference.ThemePreferences.Values.THEME_DEFAULT
import com.github.preference.ThemePreferences.Values.THEME_LIGHT
import com.github.preference.TimePreference.Companion.parseTime
import com.github.times.R
import com.github.times.ZmanimItem
import com.github.times.preference.ZmanimPreferences.Values.OMER_B
import com.github.times.preference.ZmanimPreferences.Values.OMER_L
import com.github.times.preference.ZmanimPreferences.Values.OMER_NONE
import com.github.times.preference.ZmanimPreferences.Values.OPINION_10_2
import com.github.times.preference.ZmanimPreferences.Values.OPINION_11
import com.github.times.preference.ZmanimPreferences.Values.OPINION_12
import com.github.times.preference.ZmanimPreferences.Values.OPINION_120
import com.github.times.preference.ZmanimPreferences.Values.OPINION_120_ZMANIS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_13
import com.github.times.preference.ZmanimPreferences.Values.OPINION_15
import com.github.times.preference.ZmanimPreferences.Values.OPINION_15_ALOS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_168
import com.github.times.preference.ZmanimPreferences.Values.OPINION_16_1
import com.github.times.preference.ZmanimPreferences.Values.OPINION_16_1_ALOS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_16_1_SUNSET
import com.github.times.preference.ZmanimPreferences.Values.OPINION_18
import com.github.times.preference.ZmanimPreferences.Values.OPINION_19
import com.github.times.preference.ZmanimPreferences.Values.OPINION_19_8
import com.github.times.preference.ZmanimPreferences.Values.OPINION_2
import com.github.times.preference.ZmanimPreferences.Values.OPINION_26
import com.github.times.preference.ZmanimPreferences.Values.OPINION_2_STARS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_3
import com.github.times.preference.ZmanimPreferences.Values.OPINION_30
import com.github.times.preference.ZmanimPreferences.Values.OPINION_3_65
import com.github.times.preference.ZmanimPreferences.Values.OPINION_3_676
import com.github.times.preference.ZmanimPreferences.Values.OPINION_3_7
import com.github.times.preference.ZmanimPreferences.Values.OPINION_3_8
import com.github.times.preference.ZmanimPreferences.Values.OPINION_4
import com.github.times.preference.ZmanimPreferences.Values.OPINION_4_37
import com.github.times.preference.ZmanimPreferences.Values.OPINION_4_61
import com.github.times.preference.ZmanimPreferences.Values.OPINION_4_8
import com.github.times.preference.ZmanimPreferences.Values.OPINION_58
import com.github.times.preference.ZmanimPreferences.Values.OPINION_5_88
import com.github.times.preference.ZmanimPreferences.Values.OPINION_5_95
import com.github.times.preference.ZmanimPreferences.Values.OPINION_6
import com.github.times.preference.ZmanimPreferences.Values.OPINION_60
import com.github.times.preference.ZmanimPreferences.Values.OPINION_6_45
import com.github.times.preference.ZmanimPreferences.Values.OPINION_7
import com.github.times.preference.ZmanimPreferences.Values.OPINION_72
import com.github.times.preference.ZmanimPreferences.Values.OPINION_72_ZMANIS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_7_083
import com.github.times.preference.ZmanimPreferences.Values.OPINION_7_083_ZMANIS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_7_65
import com.github.times.preference.ZmanimPreferences.Values.OPINION_7_67
import com.github.times.preference.ZmanimPreferences.Values.OPINION_8_5
import com.github.times.preference.ZmanimPreferences.Values.OPINION_90
import com.github.times.preference.ZmanimPreferences.Values.OPINION_90_ZMANIS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_96
import com.github.times.preference.ZmanimPreferences.Values.OPINION_96_ZMANIS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_9_3
import com.github.times.preference.ZmanimPreferences.Values.OPINION_9_5
import com.github.times.preference.ZmanimPreferences.Values.OPINION_9_75
import com.github.times.preference.ZmanimPreferences.Values.OPINION_ATERET
import com.github.times.preference.ZmanimPreferences.Values.OPINION_BAAL_HATANYA
import com.github.times.preference.ZmanimPreferences.Values.OPINION_FIXED
import com.github.times.preference.ZmanimPreferences.Values.OPINION_GRA
import com.github.times.preference.ZmanimPreferences.Values.OPINION_HALF
import com.github.times.preference.ZmanimPreferences.Values.OPINION_LEVEL
import com.github.times.preference.ZmanimPreferences.Values.OPINION_MGA
import com.github.times.preference.ZmanimPreferences.Values.OPINION_NIGHT
import com.github.times.preference.ZmanimPreferences.Values.OPINION_NONE
import com.github.times.preference.ZmanimPreferences.Values.OPINION_SEA
import com.github.times.preference.ZmanimPreferences.Values.OPINION_TWILIGHT
import com.github.times.preference.ZmanimPreferences.Values.THEME_NONE
import com.github.times.preference.ZmanimPreferences.Values.THEME_WHITE
import com.github.util.hour
import com.github.util.millisecond
import com.github.util.minute
import com.github.util.second
import com.kosherjava.zmanim.ShaahZmanis
import java.io.File
import java.util.Calendar
import java.util.Locale

/**
 * Simple application preferences implementation.
 *
 * @author Moshe Waisberg
 */
class SimpleZmanimPreferences(context: Context) : SimplePreferences(context), ZmanimPreferences {

    private val themePreferences: ThemePreferences = SimpleThemePreferences(context)
    private val localePreferences: LocalePreferences = SimpleLocalePreferences(context)

    init {
        init(context)
    }

    override val isSeconds: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_SECONDS,
            context.resources.getBoolean(R.bool.seconds_visible_defaultValue)
        )

    override val isSummaries: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_SUMMARIES,
            context.resources.getBoolean(com.github.times.common.R.bool.summaries_visible_defaultValue)
        )

    override val isPast: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_PAST,
            context.resources.getBoolean(R.bool.past_defaultValue)
        )

    override val isUpcomingNotification: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_NOTIFICATION_UPCOMING,
            context.resources.getBoolean(R.bool.notification_upcoming_defaultValue)
        )

    override val themeValue: String?
        get() = themePreferences.themeValue

    override fun getTheme(value: String?): Int {
        if (value.isNullOrEmpty()) {
            return R.style.Theme_Zmanim_NoGradient
        }
        return when (value) {
            THEME_NONE -> R.style.Theme_Zmanim_NoGradient
            THEME_DARK -> R.style.Theme_Zmanim_Dark
            THEME_LIGHT -> R.style.Theme_Zmanim_Light
            THEME_WHITE -> R.style.Theme_Zmanim_White
            else -> R.style.Theme_Zmanim_DayNight
        }
    }

    override val theme: Int
        get() = getTheme(themeValue)

    override fun isDarkTheme(@StyleRes themeId: Int): Boolean {
        return when (themeId) {
            R.style.Theme_Zmanim_NoGradient -> true
            R.style.Theme_Zmanim_Dark -> true
            R.style.Theme_Zmanim_Light -> false
            R.style.Theme_Zmanim_White -> false
            else -> context.isNightMode
        }
    }

    override val isDarkTheme: Boolean
        get() = isDarkTheme(theme)

    override val isHour: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_HOUR,
            context.resources.getBoolean(R.bool.hour_visible_defaultValue)
        )

    override val candleLightingOffset: Int
        get() = preferences.getInt(
            ZmanimPreferences.KEY_OPINION_CANDLES,
            context.resources.getInteger(R.integer.candles_defaultValue)
        )

    override val chanukkaCandles: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_CANDLES_CHANUKKA,
            context.getString(R.string.candles_chanukka_defaultValue)
        )

    override val hour: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_HOUR,
            context.getString(R.string.hour_defaultValue)
        )

    override val hourType: ShaahZmanis
        get() {
            val opinion = hour
            if (OPINION_19_8 == opinion) {
                return ShaahZmanis.DEGREES_19POINT8
            }
            if (OPINION_120 == opinion) {
                return ShaahZmanis.MINUTES_120
            }
            if (OPINION_120_ZMANIS == opinion) {
                return ShaahZmanis.MINUTES_120
            }
            if (OPINION_18 == opinion) {
                return ShaahZmanis.DEGREES_18
            }
            if (OPINION_26 == opinion) {
                return ShaahZmanis.DEGREES_26
            }
            if (OPINION_16_1 == opinion) {
                return ShaahZmanis.DEGREES_16POINT1
            }
            if (OPINION_96 == opinion) {
                return ShaahZmanis.MINUTES_96
            }
            if (OPINION_96_ZMANIS == opinion) {
                return ShaahZmanis.MINUTES_96
            }
            if (OPINION_90 == opinion) {
                return ShaahZmanis.MINUTES_90
            }
            if (OPINION_90_ZMANIS == opinion) {
                return ShaahZmanis.MINUTES_90
            }
            if (OPINION_72 == opinion) {
                return ShaahZmanis.MINUTES_72
            }
            if (OPINION_72_ZMANIS == opinion) {
                return ShaahZmanis.MINUTES_72
            }
            if (OPINION_60 == opinion) {
                return ShaahZmanis.MINUTES_60
            }
            if (OPINION_MGA == opinion) {
                return ShaahZmanis.MGA
            }
            if (OPINION_BAAL_HATANYA == opinion) {
                return ShaahZmanis.BAAL_HATANYA
            }
            return if (OPINION_ATERET == opinion) {
                ShaahZmanis.ATERET
            } else ShaahZmanis.GRA
        }

    override val dawn: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_DAWN,
            context.getString(R.string.dawn_defaultValue)
        )

    override val tallis: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_TALLIS,
            context.getString(R.string.tallis_defaultValue)
        )

    override val sunrise: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SUNRISE,
            context.getString(R.string.sunrise_defaultValue)
        )

    override val lastShema: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SHEMA,
            context.getString(R.string.shema_defaultValue)
        )

    override val lastTfila: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_TFILA,
            context.getString(R.string.prayers_defaultValue)
        )

    override val eatChametz: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_EAT,
            context.getString(R.string.eat_chametz_defaultValue)
        )

    override val burnChametz: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_BURN,
            context.getString(R.string.burn_chametz_defaultValue)
        )

    override val midday: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_NOON,
            context.getString(R.string.midday_defaultValue)
        )

    override val earliestMincha: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_EARLIEST_MINCHA,
            context.getString(R.string.earliest_mincha_defaultValue)
        )

    override val mincha: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_MINCHA,
            context.getString(R.string.mincha_defaultValue)
        )

    override val plugHamincha: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_PLUG_MINCHA,
            context.getString(R.string.plug_hamincha_defaultValue)
        )

    override val sunset: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SUNSET,
            context.getString(R.string.sunset_defaultValue)
        )

    override val twilight: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_TWILIGHT,
            context.getString(R.string.twilight_defaultValue)
        )

    override val nightfall: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_NIGHTFALL,
            context.getString(R.string.nightfall_defaultValue)
        )

    override val shabbathEndsAfter: Int
        get() = toId(
            preferences.getString(
                ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER,
                context.getString(R.string.shabbath_ends_after_defaultValue)
            )
        )

    override val shabbathEndsSunset: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_SUNSET,
            context.getString(R.string.shabbath_ends_sunset_defaultValue)
        )

    override val shabbathEndsTwilight: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_TWILIGHT,
            context.getString(R.string.shabbath_ends_twilight_defaultValue)
        )

    override val shabbathEndsNightfall: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_NIGHTFALL,
            context.getString(R.string.shabbath_ends_nightfall_defaultValue)
        )

    override val shabbathEnds: Int
        get() = preferences.getInt(
            ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES,
            context.resources.getInteger(R.integer.shabbath_ends_defaultValue)
        )

    override val midnight: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_MIDNIGHT,
            context.getString(R.string.midnight_defaultValue)
        )

    override val earliestKiddushLevana: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_EARLIEST_LEVANA,
            context.getString(R.string.levana_earliest_defaultValue)
        )

    override val latestKiddushLevana: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_LATEST_LEVANA,
            context.getString(R.string.levana_latest_defaultValue)
        )

    override val guardsCount: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_GUARDS,
            context.getString(R.string.guards_defaultValue)
        )

    override fun getReminder(id: Int, time: Long): Long {
        if (time == ZmanimItem.NEVER) return ZmanimItem.NEVER
        val key = toKey(id) ?: return ZmanimItem.NEVER
        val keyReminder = key + ZmanimPreferences.REMINDER_SUFFIX
        val value = preferences.getString(
            keyReminder,
            context.resources.getString(R.string.reminder_defaultValue)
        )

        if (value.isNullOrEmpty()) return ZmanimItem.NEVER
        if (value.indexOf(':') >= 0) {
            val parsed = parseTime(value)
            if (parsed != null) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = time
                    hour = parsed.hour
                    minute = parsed.minute
                    second = parsed.second
                    millisecond = 0
                }

                var yesterday = cal.timeInMillis
                // Reminders should always be before the zman.
                if (yesterday > time) {
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    yesterday = cal.timeInMillis
                }
                return yesterday
            }
        } else {
            return getReminder(time, value.toInt())
        }
        return ZmanimItem.NEVER
    }

    private fun getReminder(time: Long, value: Int): Long {
        if (value < 0) return ZmanimItem.NEVER
        val before = value * DateUtils.MINUTE_IN_MILLIS
        return time - before
    }

    override var latestReminder: Long
        get() = preferences.getLong(ZmanimPreferences.KEY_REMINDER_LATEST, 0L)
        set(time) {
            preferences.edit().putLong(ZmanimPreferences.KEY_REMINDER_LATEST, time).apply()
        }

    override val isCandlesAnimated: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_ANIM_CANDLES,
            context.resources.getBoolean(R.bool.animate_defaultValue)
        )

    override val reminderStream: Int
        get() = preferences.getString(
            ZmanimPreferences.KEY_REMINDER_STREAM,
            context.getString(R.string.reminder_stream_defaultValue)
        )?.toInt() ?: RingtoneManager.TYPE_ALARM

    override val reminderType: Int
        get() = if (reminderStream == AudioManager.STREAM_NOTIFICATION) {
            RingtoneManager.TYPE_NOTIFICATION
        } else {
            RingtoneManager.TYPE_ALARM
        }

    override val reminderSilenceOffset: Int
        get() = preferences.getInt(
            ZmanimPreferences.KEY_REMINDER_SILENCE,
            context.resources.getInteger(R.integer.reminder_silence_defaultValue)
        )

    override fun isEmphasis(id: Int): Boolean {
        val key = toKey(id)
        return key != null && preferences.getBoolean(
            key + ZmanimPreferences.EMPHASIS_SUFFIX,
            context.resources.getBoolean(R.bool.emphasis_defaultValue)
        )
    }

    override val emphasisScale: Float
        get() = preferences.getString(
            ZmanimPreferences.KEY_EMPHASIS_SCALE,
            context.getString(R.string.emphasis_scale_defaultValue)
        )?.toFloat() ?: 1f

    override fun toKey(id: Int): String? {
        return when (id) {
            R.string.burn_chametz -> ZmanimPreferences.KEY_OPINION_BURN
            R.string.candles -> ZmanimPreferences.KEY_OPINION_CANDLES
            R.string.chanukka -> ZmanimPreferences.KEY_OPINION_CANDLES_CHANUKKA
            R.string.chanukka_count -> ZmanimPreferences.KEY_OPINION_CANDLES_CHANUKKA
            R.string.dawn -> ZmanimPreferences.KEY_OPINION_DAWN
            R.string.earliest_mincha -> ZmanimPreferences.KEY_OPINION_EARLIEST_MINCHA
            R.string.eat_chametz -> ZmanimPreferences.KEY_OPINION_EAT
            R.string.fast_begins -> ZmanimPreferences.KEY_OPINION_FAST_BEGINS
            R.string.fast_ends -> ZmanimPreferences.KEY_OPINION_FAST_ENDS
            R.string.festival_ends -> ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS
            R.string.hour -> ZmanimPreferences.KEY_OPINION_HOUR
            R.string.levana_earliest -> ZmanimPreferences.KEY_OPINION_EARLIEST_LEVANA
            R.string.levana_latest -> ZmanimPreferences.KEY_OPINION_LATEST_LEVANA
            R.string.midday -> ZmanimPreferences.KEY_OPINION_NOON
            R.string.midnight -> ZmanimPreferences.KEY_OPINION_MIDNIGHT
            R.string.midnight_guard -> ZmanimPreferences.KEY_OPINION_MIDNIGHT_GUARD
            R.string.mincha -> ZmanimPreferences.KEY_OPINION_MINCHA
            R.string.molad -> ZmanimPreferences.KEY_OPINION_MOLAD
            R.string.morning_guard -> ZmanimPreferences.KEY_OPINION_MORNING_GUARD
            R.string.nightfall -> ZmanimPreferences.KEY_OPINION_NIGHTFALL
            R.string.omer -> ZmanimPreferences.KEY_OPINION_OMER
            R.string.plug_hamincha -> ZmanimPreferences.KEY_OPINION_PLUG_MINCHA
            R.string.prayers -> ZmanimPreferences.KEY_OPINION_TFILA
            R.string.shabbath_ends -> ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS
            R.string.shema -> ZmanimPreferences.KEY_OPINION_SHEMA
            R.string.sunrise -> ZmanimPreferences.KEY_OPINION_SUNRISE
            R.string.sunset -> ZmanimPreferences.KEY_OPINION_SUNSET
            R.string.tallis -> ZmanimPreferences.KEY_OPINION_TALLIS
            R.string.tallis_only -> ZmanimPreferences.KEY_OPINION_TALLIS
            R.string.twilight -> ZmanimPreferences.KEY_OPINION_TWILIGHT
            else -> null
        }
    }

    override fun toId(key: String?): Int {
        return when (key) {
            ZmanimPreferences.KEY_OPINION_HOUR -> R.string.hour
            ZmanimPreferences.KEY_OPINION_DAWN -> R.string.dawn
            ZmanimPreferences.KEY_OPINION_TALLIS -> R.string.tallis
            ZmanimPreferences.KEY_OPINION_SUNRISE -> R.string.sunrise
            ZmanimPreferences.KEY_OPINION_SHEMA -> R.string.shema
            ZmanimPreferences.KEY_OPINION_TFILA -> R.string.prayers
            ZmanimPreferences.KEY_OPINION_EAT -> R.string.eat_chametz
            ZmanimPreferences.KEY_OPINION_BURN -> R.string.burn_chametz
            ZmanimPreferences.KEY_OPINION_NOON -> R.string.midday
            ZmanimPreferences.KEY_OPINION_EARLIEST_MINCHA -> R.string.earliest_mincha
            ZmanimPreferences.KEY_OPINION_MINCHA -> R.string.mincha
            ZmanimPreferences.KEY_OPINION_PLUG_MINCHA -> R.string.plug_hamincha
            ZmanimPreferences.KEY_OPINION_CANDLES -> R.string.candles
            ZmanimPreferences.KEY_OPINION_CANDLES_CHANUKKA -> R.string.chanukka
            ZmanimPreferences.KEY_OPINION_SUNSET, ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_SUNSET -> R.string.sunset
            ZmanimPreferences.KEY_OPINION_TWILIGHT, ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_TWILIGHT -> R.string.twilight
            ZmanimPreferences.KEY_OPINION_NIGHTFALL, ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_NIGHTFALL -> R.string.nightfall
            ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS, ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER, ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES -> R.string.shabbath_ends
            ZmanimPreferences.KEY_OPINION_MIDNIGHT -> R.string.midnight
            ZmanimPreferences.KEY_OPINION_MIDNIGHT_GUARD -> R.string.midnight_guard
            ZmanimPreferences.KEY_OPINION_MORNING_GUARD -> R.string.morning_guard
            ZmanimPreferences.KEY_OPINION_MOLAD -> R.string.molad
            ZmanimPreferences.KEY_OPINION_EARLIEST_LEVANA -> R.string.levana_earliest
            ZmanimPreferences.KEY_OPINION_LATEST_LEVANA -> R.string.levana_latest
            ZmanimPreferences.KEY_OPINION_OMER -> R.string.omer
            ZmanimPreferences.KEY_OPINION_FAST_BEGINS -> R.string.fast_begins
            ZmanimPreferences.KEY_OPINION_FAST_ENDS -> R.string.fast_ends
            else -> 0
        }
    }

    override val reminderRingtone: Uri?
        get() {
            val type = reminderType
            var path = preferences.getString(
                ZmanimPreferences.KEY_REMINDER_RINGTONE,
                RingtoneManager.DEFAULT_PATH
            )
            if (path === RingtoneManager.DEFAULT_PATH) {
                path = RingtoneManager.getDefaultUri(type).toString()
            }
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(type)
            path = ringtoneManager.filterInternalMaybe(path)
            return if (path.isNullOrEmpty()) {
                null
            } else if (path[0] == File.separatorChar) {
                Uri.fromFile(File(path))
            } else {
                Uri.parse(path)
            }
        }

    override fun isReminderSunday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_SUNDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_1_defaultValue)
            )
        }
        return true
    }

    override fun isReminderMonday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_MONDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_2_defaultValue)
            )
        }
        return true
    }

    override fun isReminderTuesday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_TUESDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_3_defaultValue)
            )
        }
        return true
    }

    override fun isReminderWednesday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_WEDNESDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_4_defaultValue)
            )
        }
        return true
    }

    override fun isReminderThursday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_THURSDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_5_defaultValue)
            )
        }
        return true
    }

    override fun isReminderFriday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_FRIDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_6_defaultValue)
            )
        }
        return true
    }

    override fun isReminderSaturday(id: Int): Boolean {
        val key = toKey(id)
        if (key != null) {
            val keyDay =
                key + ZmanimPreferences.REMINDER_SUFFIX + ZmanimPreferences.REMINDER_SATURDAY_SUFFIX
            return preferences.getBoolean(
                keyDay,
                context.resources.getBoolean(R.bool.reminder_day_7_defaultValue)
            )
        }
        return false
    }

    override val omerSuffix: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_OPINION_OMER,
            context.getString(R.string.omer_defaultValue)
        )

    override val appWidgetThemeValue: String?
        get() = preferences.getString(
            ZmanimPreferences.KEY_THEME_WIDGET,
            context.getString(R.string.appwidget_theme_defaultValue)
        )

    override fun getAppWidgetTheme(value: String?): Int {
        if (value.isNullOrEmpty() || THEME_DEFAULT == value || THEME_NONE == value) {
            return 0
        }
        return if (THEME_LIGHT == value) THEME_APPWIDGET_LIGHT else THEME_APPWIDGET_DARK
    }

    override val appWidgetTheme: Int
        get() = getAppWidgetTheme(appWidgetThemeValue)

    override fun isAppWidgetDarkTheme(value: String?): Boolean {
        return THEME_LIGHT != value
    }

    override val isAppWidgetDarkTheme: Boolean
        get() = isAppWidgetDarkTheme(appWidgetThemeValue)

    override val locale: Locale
        get() = localePreferences.locale

    override val isUseElevation: Boolean
        get() = OPINION_SEA != sunrise

    override val isYearFinalForm: Boolean
        get() = preferences.getBoolean(
            ZmanimPreferences.KEY_YEAR_FINAL,
            context.resources.getBoolean(R.bool.year_final_defaultValue)
        )

    companion object {
        @StyleRes
        private val THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark

        @StyleRes
        private val THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light

        /**
         * Initialize. Should be called only once when application created.
         *
         * @param context the context.
         */
        fun init(context: Context) {
            val res = context.resources
            OPINION_2 = res.getString(R.string.opinion_value_2)
            OPINION_2_STARS = res.getString(R.string.opinion_value_2_stars)
            OPINION_3 = res.getString(R.string.opinion_value_3)
            OPINION_3_65 = res.getString(R.string.opinion_value_3_65)
            OPINION_3_676 = res.getString(R.string.opinion_value_3_676)
            OPINION_3_7 = res.getString(R.string.opinion_value_3_7)
            OPINION_3_8 = res.getString(R.string.opinion_value_3_8)
            OPINION_4 = res.getString(R.string.opinion_value_4)
            OPINION_4_37 = res.getString(R.string.opinion_value_4_37)
            OPINION_4_61 = res.getString(R.string.opinion_value_4_61)
            OPINION_4_8 = res.getString(R.string.opinion_value_4_8)
            OPINION_5_88 = res.getString(R.string.opinion_value_5_88)
            OPINION_5_95 = res.getString(R.string.opinion_value_5_95)
            OPINION_6 = res.getString(R.string.opinion_value_6)
            OPINION_6_45 = res.getString(R.string.opinion_value_6_45)
            OPINION_7 = res.getString(R.string.opinion_value_7)
            OPINION_7_083 = res.getString(R.string.opinion_value_7_083)
            OPINION_7_083_ZMANIS = res.getString(R.string.opinion_value_7_083_zmanis)
            OPINION_7_65 = res.getString(R.string.opinion_value_7_65)
            OPINION_7_67 = res.getString(R.string.opinion_value_7_67)
            OPINION_8_5 = res.getString(R.string.opinion_value_8)
            OPINION_9_3 = res.getString(R.string.opinion_value_9_3)
            OPINION_9_5 = res.getString(R.string.opinion_value_9_5)
            OPINION_9_75 = res.getString(R.string.opinion_value_9_75)
            OPINION_10_2 = res.getString(R.string.opinion_value_10)
            OPINION_11 = res.getString(R.string.opinion_value_11)
            OPINION_12 = res.getString(R.string.opinion_value_12)
            OPINION_13 = res.getString(R.string.opinion_value_13)
            OPINION_15 = res.getString(R.string.opinion_value_15)
            OPINION_15_ALOS = res.getString(R.string.opinion_value_15_alos)
            OPINION_16_1 = res.getString(R.string.opinion_value_16)
            OPINION_16_1_ALOS = res.getString(R.string.opinion_value_16_alos)
            OPINION_16_1_SUNSET = res.getString(R.string.opinion_value_16_sunset)
            OPINION_18 = res.getString(R.string.opinion_value_18)
            OPINION_19 = res.getString(R.string.opinion_value_19)
            OPINION_19_8 = res.getString(R.string.opinion_value_19_8)
            OPINION_26 = res.getString(R.string.opinion_value_26)
            OPINION_30 = res.getString(R.string.opinion_value_30)
            OPINION_58 = res.getString(R.string.opinion_value_58)
            OPINION_60 = res.getString(R.string.opinion_value_60)
            OPINION_72 = res.getString(R.string.opinion_value_72)
            OPINION_72_ZMANIS = res.getString(R.string.opinion_value_72_zmanis)
            OPINION_90 = res.getString(R.string.opinion_value_90)
            OPINION_90_ZMANIS = res.getString(R.string.opinion_value_90_zmanis)
            OPINION_96 = res.getString(R.string.opinion_value_96)
            OPINION_96_ZMANIS = res.getString(R.string.opinion_value_96_zmanis)
            OPINION_120 = res.getString(R.string.opinion_value_120)
            OPINION_120_ZMANIS = res.getString(R.string.opinion_value_120_zmanis)
            OPINION_168 = res.getString(R.string.opinion_value_168)
            OPINION_ATERET = res.getString(R.string.opinion_value_ateret)
            OPINION_BAAL_HATANYA = res.getString(R.string.opinion_value_baal_hatanya)
            OPINION_FIXED = res.getString(R.string.opinion_value_fixed)
            OPINION_GRA = res.getString(R.string.opinion_value_gra)
            OPINION_HALF = res.getString(R.string.opinion_value_half)
            OPINION_LEVEL = res.getString(R.string.opinion_value_level)
            OPINION_MGA = res.getString(R.string.opinion_value_mga)
            OPINION_NIGHT = res.getString(R.string.opinion_value_nightfall)
            OPINION_NONE = res.getString(R.string.opinion_value_none)
            OPINION_SEA = res.getString(R.string.opinion_value_sea)
            OPINION_TWILIGHT = res.getString(R.string.opinion_value_twilight)

            THEME_NONE = res.getString(R.string.theme_value_none)
            THEME_WHITE = res.getString(R.string.theme_value_white)

            OMER_NONE = res.getString(R.string.omer_value_off)
            OMER_B = res.getString(R.string.omer_value_b)
            OMER_L = res.getString(R.string.omer_value_l)
        }
    }
}
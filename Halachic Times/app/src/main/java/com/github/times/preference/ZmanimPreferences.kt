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

import android.net.Uri
import androidx.annotation.StyleRes
import com.github.preference.LocalePreferences
import com.github.preference.ThemePreferences
import com.github.times.location.LocationPreferences
import com.kosherjava.zmanim.ShaahZmanis
import java.util.Calendar

/**
 * Application preferences.
 *
 * @author Moshe Waisberg
 */
interface ZmanimPreferences : ThemePreferences, LocalePreferences {
    object Values : LocationPreferences.Values() {
        @JvmField
        var OPINION_2_STARS: String? = null

        @JvmField
        var OPINION_2: String? = null

        @JvmField
        var OPINION_3_65: String? = null

        @JvmField
        var OPINION_3_676: String? = null

        @JvmField
        var OPINION_3_7: String? = null

        @JvmField
        var OPINION_3_8: String? = null

        @JvmField
        var OPINION_3: String? = null

        @JvmField
        var OPINION_4: String? = null

        @JvmField
        var OPINION_4_37: String? = null

        @JvmField
        var OPINION_4_61: String? = null

        @JvmField
        var OPINION_4_8: String? = null

        @JvmField
        var OPINION_5_88: String? = null

        @JvmField
        var OPINION_5_95: String? = null

        @JvmField
        var OPINION_6: String? = null

        @JvmField
        var OPINION_6_45: String? = null

        @JvmField
        var OPINION_7: String? = null

        @JvmField
        var OPINION_7_083_ZMANIS: String? = null

        @JvmField
        var OPINION_7_083: String? = null

        @JvmField
        var OPINION_7_65: String? = null

        @JvmField
        var OPINION_7_67: String? = null

        @JvmField
        var OPINION_8_5: String? = null

        @JvmField
        var OPINION_9_3: String? = null

        @JvmField
        var OPINION_9_5: String? = null

        @JvmField
        var OPINION_9_75: String? = null

        @JvmField
        var OPINION_10_2: String? = null

        @JvmField
        var OPINION_11: String? = null

        @JvmField
        var OPINION_12: String? = null

        @JvmField
        var OPINION_13: String? = null

        @JvmField
        var OPINION_15_ALOS: String? = null

        @JvmField
        var OPINION_15: String? = null

        @JvmField
        var OPINION_16_1_ALOS: String? = null

        @JvmField
        var OPINION_16_1_SUNSET: String? = null

        @JvmField
        var OPINION_16_1: String? = null

        @JvmField
        var OPINION_18: String? = null

        @JvmField
        var OPINION_19_8: String? = null

        @JvmField
        var OPINION_19: String? = null

        @JvmField
        var OPINION_26: String? = null

        @JvmField
        var OPINION_30: String? = null

        @JvmField
        var OPINION_58: String? = null

        @JvmField
        var OPINION_60: String? = null

        @JvmField
        var OPINION_72_ZMANIS: String? = null

        @JvmField
        var OPINION_72: String? = null

        @JvmField
        var OPINION_90_ZMANIS: String? = null

        @JvmField
        var OPINION_90: String? = null

        @JvmField
        var OPINION_96_ZMANIS: String? = null

        @JvmField
        var OPINION_96: String? = null

        @JvmField
        var OPINION_120_ZMANIS: String? = null

        @JvmField
        var OPINION_120: String? = null

        @JvmField
        var OPINION_168: String? = null

        @JvmField
        var OPINION_ATERET: String? = null

        @JvmField
        var OPINION_BAAL_HATANYA: String? = null

        @JvmField
        var OPINION_FIXED: String? = null

        @JvmField
        var OPINION_GRA: String? = null

        @JvmField
        var OPINION_HALF: String? = null

        @JvmField
        var OPINION_LEVEL: String? = null

        @JvmField
        var OPINION_MGA: String? = null

        @JvmField
        var OPINION_NIGHT: String? = null

        @JvmField
        var OPINION_NONE: String? = null

        @JvmField
        var OPINION_SEA: String? = null

        @JvmField
        var OPINION_TWILIGHT: String? = null

        /** Show zmanim list without background.  */
        @JvmField
        var THEME_NONE: String? = null

        /** Show zmanim list with white background.  */
        @JvmField
        var THEME_WHITE: String? = null

        /** No omer count.  */
        @JvmField
        var OMER_NONE: String? = null

        /** Omer count has "BaOmer" suffix.  */
        @JvmField
        var OMER_B: String? = null

        /** Omer count has "LaOmer" suffix.  */
        @JvmField
        var OMER_L: String? = null
    }

    /**
     * Format times with seconds?
     *
     * @return `true` to show seconds.
     */
    val isSeconds: Boolean

    /**
     * Are summaries visible?
     *
     * @return `true` to show summaries.
     */
    val isSummaries: Boolean

    /**
     * Are past times enabled?
     *
     * @return `true` if older times are not grayed.
     */
    val isPast: Boolean

    /**
     * Is the upcoming time notification enabled?
     *
     * @return `true` if upcoming times shown in notification.
     */
    val isUpcomingNotification: Boolean

    /**
     * Is temporal hour visible?
     *
     * @return `true` to show hour.
     */
    val isHour: Boolean

    /**
     * Get the offset in minutes before sunset which is used in calculating
     * candle lighting time.
     *
     * @return the number of minutes.
     */
    val candleLightingOffset: Int

    /**
     * Get the opinion for Chanukka candle lighting time.
     *
     * @return the opinion.
     */
    val chanukkaCandles: String?

    /**
     * Get the opinion for temporal hour (*shaah zmanis*).
     *
     * @return the opinion.
     */
    val hour: String?

    /**
     * Get the type for temporal hour (*shaah zmanis*).
     *
     * @return the type.
     */
    val hourType: ShaahZmanis?

    /**
     * Get the opinion for dawn (*alos*).
     *
     * @return the opinion.
     */
    val dawn: String?

    /**
     * Get the opinion for earliest tallis &amp; tefillin (*misheyakir*).
     *
     * @return the opinion.
     */
    val tallis: String?

    /**
     * Get the opinion for sunrise.
     *
     * @return the opinion.
     */
    val sunrise: String?

    /**
     * Get the opinion for the last shema (*sof zman shma*).
     *
     * @return the opinion.
     */
    val lastShema: String?

    /**
     * Get the opinion for the last morning prayers (*sof zman tfila*).
     *
     * @return the opinion.
     */
    val lastTfila: String?

    /**
     * Get the opinion for eating chametz (*achilat chametz*).
     *
     * @return the opinion.
     */
    val eatChametz: String?

    /**
     * Get the opinion for burning chametz (*biur chametz*).
     *
     * @return the opinion.
     */
    val burnChametz: String?

    /**
     * Get the opinion for noon (*chatzos*).
     *
     * @return the opinion.
     */
    val midday: String?

    /**
     * Get the opinion for earliest afternoon prayers (*mincha gedola*).
     *
     * @return the opinion.
     */
    val earliestMincha: String?

    /**
     * Get the opinion for afternoon prayers (*mincha ketana*).
     *
     * @return the opinion.
     */
    val mincha: String?

    /**
     * Get the opinion for afternoon prayers (*plag hamincha*).
     *
     * @return the opinion.
     */
    val plugHamincha: String?

    /**
     * Get the opinion for sunset.
     *
     * @return the opinion.
     */
    val sunset: String?

    /**
     * Get the opinion for twilight (dusk).
     *
     * @return the opinion.
     */
    val twilight: String?

    /**
     * Get the opinion for nightfall.
     *
     * @return the opinion.
     */
    val nightfall: String?

    /**
     * Get the time when Shabbath ends after.
     *
     * @return the time id.
     */
    val shabbathEndsAfter: Int

    /**
     * Get the number of minutes when Shabbath ends after the specified opinion.
     *
     * @return the opinion.
     */
    val shabbathEnds: Int

    /**
     * Get the opinion for Shabbath ends at sunset.
     *
     * @return the opinion.
     */
    val shabbathEndsSunset: String?

    /**
     * Get the opinion for Shabbath ends at twilight.
     *
     * @return the opinion.
     */
    val shabbathEndsTwilight: String?

    /**
     * Get the opinion for Shabbath ends at nightfall.
     *
     * @return the opinion.
     */
    val shabbathEndsNightfall: String?

    /**
     * Get the opinion for midnight (*chatzos layla*).
     *
     * @return the opinion.
     */
    val midnight: String?

    /**
     * Get the opinion for the number of night guards.
     *
     * @return the opinion.
     */
    val guardsCount: String?

    /**
     * Get the opinion for earliest kiddush levana.
     *
     * @return the opinion.
     */
    val earliestKiddushLevana: String?

    /**
     * Get the opinion for latest kiddush levana.
     *
     * @return the opinion.
     */
    val latestKiddushLevana: String?

    /**
     * Get reminder of the zman. The reminder is either the number of minutes before the zman, or an absolute time.
     *
     * @param id   the zman id.
     * @param time the zman time.
     * @return the reminder in milliseconds - `NEVER` when no reminder.
     */
    fun getReminder(id: Int, time: Long): Long

    /**
     * The time that was used for the latest reminder.
     */
    var latestReminder: Long

    /**
     * Are the candles animated?
     *
     * @return `true` if candles animations enabled.
     */
    val isCandlesAnimated: Boolean

    /**
     * Get the reminder audio stream type.
     *
     * @return the stream type.
     * @see AudioManager.STREAM_ALARM
     * @see AudioManager.STREAM_NOTIFICATION
     */
    val reminderStream: Int

    /**
     * Get the reminder ringtone type.
     *
     * @return the ringtone type. One of [RingtoneManager.TYPE_ALARM] or [RingtoneManager.TYPE_NOTIFICATION].
     */
    val reminderType: Int

    /**
     * Get the offset in minutes after a reminder to stop the reminder.
     * How much time to wait for the notification sound once entered into a day not allowed to disturb.
     *
     * @return the number of minutes.
     */
    val reminderSilenceOffset: Int

    /**
     * Is the time emphasized?
     *
     * @param id the time id.
     * @return `true` for emphasis.
     */
    fun isEmphasis(id: Int): Boolean

    /**
     * Get the emphasis size scale.
     *
     * @return the emphasis scale as a fractional percentage.
     */
    val emphasisScale: Float

    /**
     * Get the preference key name.
     *
     * @param id the time id.
     * @return the key - `null` otherwise.
     */
    fun toKey(id: Int): String?

    /**
     * Get the preference title id.
     *
     * @param key the time name.
     * @return the id - `0` otherwise.
     */
    fun toId(key: String?): Int

    /**
     * Get the reminder ringtone.
     *
     * @return the ringtone.
     * @see RingtoneManager.getDefaultUri
     */
    val reminderRingtone: Uri?

    fun isReminderSunday(id: Int): Boolean
    fun isReminderMonday(id: Int): Boolean
    fun isReminderTuesday(id: Int): Boolean
    fun isReminderWednesday(id: Int): Boolean
    fun isReminderThursday(id: Int): Boolean
    fun isReminderFriday(id: Int): Boolean
    fun isReminderSaturday(id: Int): Boolean

    /**
     * Get the opinion for omer count suffix.
     *
     * @return the opinion.
     */
    val omerSuffix: String?

    /**
     * Get the widget theme value.
     *
     * @return the theme value.
     */
    val appWidgetThemeValue: String?

    /**
     * Get the widget theme.
     *
     * @param value the theme value.
     * @return the theme resource id.
     * @see .getThemeValue
     */
    @StyleRes
    fun getAppWidgetTheme(value: String?): Int

    @get:StyleRes
    val appWidgetTheme: Int

    /**
     * Is the widget theme dark?
     *
     * @param value the theme value.
     * @return `true` if the theme has dark backgrounds and light texts.
     */
    fun isAppWidgetDarkTheme(value: String?): Boolean

    /**
     * Is the widget theme dark?
     *
     * @return `true` if the theme has dark backgrounds and light texts.
     */
    val isAppWidgetDarkTheme: Boolean

    /**
     * Use adjusted elevation for calendar sunrise/sunset?
     * @return `true` if sunrise/sunset should be adjusted for elevation.
     */
    val isUseElevation: Boolean

    /**
     * Format Hebrew with final form letters?
     *
     * @return `true` to use final form letters.
     */
    val isYearFinalForm: Boolean

    companion object {
        /** Preference name for showing seconds.  */
        const val KEY_SECONDS = "seconds.visible"

        /** Preference name for showing summaries.  */
        const val KEY_SUMMARIES = "summaries.visible"

        /** Preference name for enabling past times.  */
        const val KEY_PAST = "past"

        /** Preference name for enabling upcoming time in ongoing notification.  */
        const val KEY_NOTIFICATION_UPCOMING = "notification.next"

        /** Preference name for the last reminder.  */
        const val KEY_REMINDER_LATEST = "reminder"

        /** Preference name for the reminder audio stream type.  */
        const val KEY_REMINDER_STREAM = "reminder.stream"

        /** Preference name for the reminder ringtone.  */
        const val KEY_REMINDER_RINGTONE = "reminder.ringtone"

        /** Preference name for the reminder notification channel settings.  */
        const val KEY_REMINDER_SETTINGS = "reminder.settings"

        /** Preference name for silence offset.  */
        const val KEY_REMINDER_SILENCE = "reminder.silence"

        /** Preference name for the temporal hour visibility.  */
        const val KEY_HOUR = "hour.visible"

        /** Preference name for the emphasis scale.  */
        const val KEY_EMPHASIS_SCALE = "emphasis_scale"

        /** Preference name for the app widget theme.  */
        const val KEY_THEME_WIDGET = "theme.appwidget"

        /** Preference name for the app widget theme permissions rationale.  */
        const val KEY_THEME_WIDGET_RATIONALE = "theme.appwidget.rationale"

        /** Preference name for formatting Hebrew year with final form letters.  */
        const val KEY_YEAR_FINAL = "year.final"

        /** Preference name for temporal hour type.  */
        const val KEY_OPINION_HOUR = "hour"

        /** Preference name for Alos type.  */
        const val KEY_OPINION_DAWN = "dawn"

        /** Preference name for earliest tallis type.  */
        const val KEY_OPINION_TALLIS = "tallis"

        /** Preference name for sunrise type.  */
        const val KEY_OPINION_SUNRISE = "sunrise"

        /** Preference name for Last Shema type.  */
        const val KEY_OPINION_SHEMA = "shema"

        /** Preference name for Last Morning Tfila type.  */
        const val KEY_OPINION_TFILA = "prayers"

        /** Preference name for Last Eating Chametz type.  */
        const val KEY_OPINION_EAT = "eat_chametz"

        /** Preference name for Last Biur Chametz type.  */
        const val KEY_OPINION_BURN = "burn_chametz"

        /** Preference name for midday / noon type.  */
        const val KEY_OPINION_NOON = "midday"

        /** Preference name for Earliest Mincha type.  */
        const val KEY_OPINION_EARLIEST_MINCHA = "earliest_mincha"

        /** Preference name for Mincha Ketana type.  */
        const val KEY_OPINION_MINCHA = "mincha"

        /** Preference name for Plug HaMincha type.  */
        const val KEY_OPINION_PLUG_MINCHA = "plug_hamincha"

        /** Preference name for candle lighting minutes offset.  */
        const val KEY_OPINION_CANDLES = "candles"

        /** Preference name for Chanukka candle lighting.  */
        const val KEY_OPINION_CANDLES_CHANUKKA = "candles_chanukka"

        /** Preference name for sunset type.  */
        const val KEY_OPINION_SUNSET = "sunset"

        /** Preference name for twilight type.  */
        const val KEY_OPINION_TWILIGHT = "twilight"

        /** Preference name for nightfall type.  */
        const val KEY_OPINION_NIGHTFALL = "nightfall"

        /** Preference name for Shabbath ends after nightfall.  */
        const val KEY_OPINION_SHABBATH_ENDS = "shabbath_ends"
        const val KEY_OPINION_SHABBATH_ENDS_AFTER = KEY_OPINION_SHABBATH_ENDS + ".after"
        const val KEY_OPINION_SHABBATH_ENDS_SUNSET =
            KEY_OPINION_SHABBATH_ENDS + "." + KEY_OPINION_SUNSET
        const val KEY_OPINION_SHABBATH_ENDS_TWILIGHT =
            KEY_OPINION_SHABBATH_ENDS + "." + KEY_OPINION_TWILIGHT
        const val KEY_OPINION_SHABBATH_ENDS_NIGHTFALL =
            KEY_OPINION_SHABBATH_ENDS + "." + KEY_OPINION_NIGHTFALL
        const val KEY_OPINION_SHABBATH_ENDS_MINUTES = KEY_OPINION_SHABBATH_ENDS + ".minutes"

        /** Preference name for midnight type.  */
        const val KEY_OPINION_MIDNIGHT = "midnight"

        /** Preference name for midnight guard type.  */
        const val KEY_OPINION_MIDNIGHT_GUARD = "midnight_guard"

        /** Preference name for morning guard type.  */
        const val KEY_OPINION_MORNING_GUARD = "morning_guard"

        /** Preference name for molad type.  */
        const val KEY_OPINION_MOLAD = "molad"

        /** Preference name for earliest kiddush levana type.  */
        const val KEY_OPINION_EARLIEST_LEVANA = "levana_earliest"

        /** Preference name for latest kiddush levana type.  */
        const val KEY_OPINION_LATEST_LEVANA = "levana_latest"

        /** Preference name for omer count suffix.  */
        const val KEY_OPINION_OMER = "omer"

        /** Preference name for guard count.  */
        const val KEY_OPINION_GUARDS = "guards"

        /** Preference name for beginning of fast type.  */
        const val KEY_OPINION_FAST_BEGINS = "fast_begins"

        /** Preference name for ending of fast type.  */
        const val KEY_OPINION_FAST_ENDS = "fast_ends"

        const val REMINDER_SUFFIX = ".reminder"
        const val REMINDER_SUNDAY_SUFFIX = ".day." + Calendar.SUNDAY
        const val REMINDER_MONDAY_SUFFIX = ".day." + Calendar.MONDAY
        const val REMINDER_TUESDAY_SUFFIX = ".day." + Calendar.TUESDAY
        const val REMINDER_WEDNESDAY_SUFFIX = ".day." + Calendar.WEDNESDAY
        const val REMINDER_THURSDAY_SUFFIX = ".day." + Calendar.THURSDAY
        const val REMINDER_FRIDAY_SUFFIX = ".day." + Calendar.FRIDAY
        const val REMINDER_SATURDAY_SUFFIX = ".day." + Calendar.SATURDAY

        const val EMPHASIS_SUFFIX = ".emphasis"
        const val ANIM_SUFFIX = ".anim"

        /** Preference name for candle lighting animations.  */
        const val KEY_ANIM_CANDLES = KEY_OPINION_CANDLES + ANIM_SUFFIX
    }
}
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
package net.sf.times.preference;

import android.media.AudioManager;
import android.net.Uri;

import net.sf.media.RingtoneManager;
import net.sf.preference.LocalePreferences;
import net.sf.preference.ThemePreferences;
import net.sf.times.location.LocationPreferences;
import net.sourceforge.zmanim.ShaahZmanis;
import net.sourceforge.zmanim.ZmanimCalendar;

import java.util.Calendar;

/**
 * Application preferences.
 *
 * @author Moshe Waisberg
 */
public interface ZmanimPreferences extends ThemePreferences, LocalePreferences {

    /** Preference name for showing seconds. */
    String KEY_SECONDS = "seconds.visible";
    /** Preference name for showing summaries. */
    String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for enabling past times. */
    String KEY_PAST = "past";
    /** Preference name for enabling upcoming time in ongoing notification. */
    String KEY_NOTIFICATION_UPCOMING = "notification.next";
    /** Preference name for the last reminder. */
    String KEY_REMINDER_LATEST = "reminder";
    /** Preference name for the reminder audio stream type. */
    String KEY_REMINDER_STREAM = "reminder.stream";
    /** Preference name for the reminder ringtone. */
    String KEY_REMINDER_RINGTONE = "reminder.ringtone";
    /** Preference name for the temporal hour visibility. */
    String KEY_HOUR = "hour.visible";
    /** Preference name for the emphasis scale. */
    String KEY_EMPHASIS_SCALE = "emphasis_scale";

    /** Preference name for temporal hour type. */
    String KEY_OPINION_HOUR = "hour";
    /** Preference name for Alos type. */
    String KEY_OPINION_DAWN = "dawn";
    /** Preference name for earliest tallis type. */
    String KEY_OPINION_TALLIS = "tallis";
    /** Preference name for sunrise type. */
    String KEY_OPINION_SUNRISE = "sunrise";
    /** Preference name for Last Shema type. */
    String KEY_OPINION_SHEMA = "shema";
    /** Preference name for Last Morning Tfila type. */
    String KEY_OPINION_TFILA = "prayers";
    /** Preference name for Last Biur Chametz type. */
    String KEY_OPINION_BURN = "burn_chametz";
    /** Preference name for midday / noon type. */
    String KEY_OPINION_NOON = "midday";
    /** Preference name for Earliest Mincha type. */
    String KEY_OPINION_EARLIEST_MINCHA = "earliest_mincha";
    /** Preference name for Mincha Ketana type. */
    String KEY_OPINION_MINCHA = "mincha";
    /** Preference name for Plug HaMincha type. */
    String KEY_OPINION_PLUG_MINCHA = "plug_hamincha";
    /** Preference name for candle lighting minutes offset. */
    String KEY_OPINION_CANDLES = "candles";
    /** Preference name for Chanukka candle lighting. */
    String KEY_OPINION_CANDLES_CHANUKKA = "candles_chanukka";
    /** Preference name for sunset type. */
    String KEY_OPINION_SUNSET = "sunset";
    /** Preference name for twilight type. */
    String KEY_OPINION_TWILIGHT = "twilight";
    /** Preference name for nightfall type. */
    String KEY_OPINION_NIGHTFALL = "nightfall";
    /** Preference name for Shabbath ends after nightfall. */
    String KEY_OPINION_SHABBATH_ENDS = "shabbath_ends";
    String KEY_OPINION_SHABBATH_ENDS_AFTER = KEY_OPINION_SHABBATH_ENDS + ".after";
    String KEY_OPINION_SHABBATH_ENDS_MINUTES = KEY_OPINION_SHABBATH_ENDS + ".minutes";
    /** Preference name for midnight type. */
    String KEY_OPINION_MIDNIGHT = "midnight";
    /** Preference name for earliest kiddush levana type. */
    String KEY_OPINION_EARLIEST_LEVANA = "levana_earliest";
    /** Preference name for latest kiddush levana type. */
    String KEY_OPINION_LATEST_LEVANA = "levana_latest";
    /** Preference name for omer count suffix. */
    String KEY_OPINION_OMER = "omer";

    String REMINDER_SUFFIX = ".reminder";
    String REMINDER_SUNDAY_SUFFIX = ".day." + Calendar.SUNDAY;
    String REMINDER_MONDAY_SUFFIX = ".day." + Calendar.MONDAY;
    String REMINDER_TUESDAY_SUFFIX = ".day." + Calendar.TUESDAY;
    String REMINDER_WEDNESDAY_SUFFIX = ".day." + Calendar.WEDNESDAY;
    String REMINDER_THURSDAY_SUFFIX = ".day." + Calendar.THURSDAY;
    String REMINDER_FRIDAY_SUFFIX = ".day." + Calendar.FRIDAY;
    String REMINDER_SATURDAY_SUFFIX = ".day." + Calendar.SATURDAY;

    String EMPHASIS_SUFFIX = ".emphasis";
    String ANIM_SUFFIX = ".anim";

    /** Preference name for candle lighting animations. */
    String KEY_ANIM_CANDLES = KEY_OPINION_CANDLES + ANIM_SUFFIX;

    class Values extends LocationPreferences.Values {
        public static String OPINION_10_2;
        public static String OPINION_11;
        public static String OPINION_12;
        public static String OPINION_120;
        public static String OPINION_120_ZMANIS;
        public static String OPINION_13;
        public static String OPINION_15;
        public static String OPINION_15_ALOS;
        public static String OPINION_16_1;
        public static String OPINION_16_1_ALOS;
        public static String OPINION_16_1_SUNSET;
        public static String OPINION_168;
        public static String OPINION_18;
        public static String OPINION_19_8;
        public static String OPINION_2;
        public static String OPINION_26;
        public static String OPINION_3;
        public static String OPINION_3_65;
        public static String OPINION_3_676;
        public static String OPINION_3_7;
        public static String OPINION_3_8;
        public static String OPINION_30;
        public static String OPINION_4_37;
        public static String OPINION_4_61;
        public static String OPINION_4_8;
        public static String OPINION_5_88;
        public static String OPINION_5_95;
        public static String OPINION_58;
        public static String OPINION_6;
        public static String OPINION_60;
        public static String OPINION_7;
        public static String OPINION_7_083;
        public static String OPINION_7_083_ZMANIS;
        public static String OPINION_72;
        public static String OPINION_72_ZMANIS;
        public static String OPINION_8_5;
        public static String OPINION_90;
        public static String OPINION_90_ZMANIS;
        public static String OPINION_96;
        public static String OPINION_96_ZMANIS;
        public static String OPINION_ATERET;
        public static String OPINION_GRA;
        public static String OPINION_HALF;
        public static String OPINION_MGA;
        public static String OPINION_FIXED;
        public static String OPINION_LEVEL;
        public static String OPINION_NIGHT;
        public static String OPINION_SEA;
        public static String OPINION_TWILIGHT;

        /** Show zmanim list without background. */
        public static String THEME_NONE;
        /** Show zmanim list with white background. */
        public static String THEME_WHITE;

        /** No omer count. */
        public static String OMER_NONE;
        /** Omer count has "BaOmer" suffix. */
        public static String OMER_B;
        /** Omer count has "LaOmer" suffix. */
        public static String OMER_L;
    }

    /**
     * Format times with seconds?
     *
     * @return {@code true} to show seconds.
     */
    boolean isSeconds();

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    boolean isSummaries();

    /**
     * Are past times enabled?
     *
     * @return {@code true} if older times are not grayed.
     */
    boolean isPast();

    /**
     * Is the upcoming time notification enabled?
     *
     * @return {@code true} if upcoming times shown in notification.
     */
    boolean isUpcomingNotification();

    /**
     * Is temporal hour visible?
     *
     * @return {@code true} to show hour.
     */
    boolean isHour();

    /**
     * Get the offset in minutes before sunset which is used in calculating
     * candle lighting time.
     *
     * @return the number of minutes.
     */
    int getCandleLightingOffset();

    /**
     * Get the opinion for Chanukka candle lighting time.
     *
     * @return the opinion.
     */
    String getChanukkaCandles();

    /**
     * Get the opinion for temporal hour (<em>shaah zmanis</em>).
     *
     * @return the opinion.
     */
    String getHour();

    /**
     * Get the type for temporal hour (<em>shaah zmanis</em>).
     *
     * @return the type.
     */
    ShaahZmanis getHourType();

    /**
     * Get the opinion for dawn (<em>alos</em>).
     *
     * @return the opinion.
     */
    String getDawn();

    /**
     * Get the opinion for earliest tallis &amp; tefillin (<em>misheyakir</em>).
     *
     * @return the opinion.
     */
    String getTallis();

    /**
     * Get the opinion for sunrise.
     *
     * @return the opinion.
     */
    String getSunrise();

    /**
     * Get the opinion for the last shema (<em>sof zman shma</em>).
     *
     * @return the opinion.
     */
    String getLastShema();

    /**
     * Get the opinion for the last morning prayers (<em>sof zman tfila</em>).
     *
     * @return the opinion.
     */
    String getLastTfila();

    /**
     * Get the opinion for burning chametz (<em>biur chametz</em>).
     *
     * @return the opinion.
     */
    String getBurnChametz();

    /**
     * Get the opinion for noon (<em>chatzos</em>).
     *
     * @return the opinion.
     */
    String getMidday();

    /**
     * Get the opinion for earliest afternoon prayers (<em>mincha gedola</em>).
     *
     * @return the opinion.
     */
    String getEarliestMincha();

    /**
     * Get the opinion for afternoon prayers (<em>mincha ketana</em>).
     *
     * @return the opinion.
     */
    String getMincha();

    /**
     * Get the opinion for afternoon prayers (<em>plag hamincha</em>).
     *
     * @return the opinion.
     */
    String getPlugHamincha();

    /**
     * Get the opinion for sunset.
     *
     * @return the opinion.
     */
    String getSunset();

    /**
     * Get the opinion for twilight (dusk).
     *
     * @return the opinion.
     */
    String getTwilight();

    /**
     * Get the opinion for nightfall.
     *
     * @return the opinion.
     */
    String getNightfall();

    /**
     * Get the time when Shabbath ends after.
     *
     * @return the time id.
     */
    int getShabbathEndsAfter();

    /**
     * Get the number of minutes when Shabbath ends after nightfall.
     *
     * @return the opinion.
     */
    int getShabbathEnds();

    /**
     * Get the opinion for midnight (<em>chatzos layla</em>).
     *
     * @return the opinion.
     */
    String getMidnight();

    /**
     * Get the opinion for earliest kiddush levana.
     *
     * @return the opinion.
     */
    String getEarliestKiddushLevana();

    /**
     * Get the opinion for latest kiddush levana.
     *
     * @return the opinion.
     */
    String getLatestKiddushLevana();

    /**
     * Get reminder of the zman. The reminder is either the number of minutes before the zman, or an absolute time.
     *
     * @param id
     *         the zman id.
     * @param time
     *         the zman time.
     * @return the reminder in milliseconds - {@code #NEVER} when no reminder.
     */
    long getReminder(int id, long time);

    /**
     * Get the time that was used for the latest reminder.
     *
     * @return the time.
     */
    long getLatestReminder();

    /**
     * Set the time that was used for the latest reminder to now.
     *
     * @param time
     *         the time.
     */
    void setLatestReminder(long time);

    /**
     * Are the candles animated?
     *
     * @return {@code true} if candles animations enabled.
     */
    boolean isCandlesAnimated();

    /**
     * Get the reminder audio stream type.
     *
     * @return the stream type.
     * @see AudioManager#STREAM_ALARM
     * @see AudioManager#STREAM_NOTIFICATION
     */
    int getReminderStream();

    /**
     * Get the reminder ringtone type.
     *
     * @return the ringtone type. One of {@link RingtoneManager#TYPE_ALARM} or {@link RingtoneManager#TYPE_NOTIFICATION}.
     */
    int getReminderType();

    /**
     * Is the time emphasized?
     *
     * @param id
     *         the time id.
     * @return {@code true} for emphasis.
     */
    boolean isEmphasis(int id);

    /**
     * Get the emphasis size scale.
     *
     * @return the emphasis scale as a fractional percentage.
     */
    float getEmphasisScale();

    /**
     * Get the preference key name.
     *
     * @param id
     *         the time id.
     * @return the key - {@code null} otherwise.
     */
    String toKey(int id);

    /**
     * Get the preference title id.
     *
     * @param key
     *         the time name.
     * @return the id - {@code null} otherwise.
     */
    int toId(String key);

    /**
     * Get the reminder ringtone.
     *
     * @return the ringtone.
     * @see RingtoneManager#getDefaultUri(int)
     */
    Uri getReminderRingtone();

    boolean isReminderSunday(int id);

    boolean isReminderMonday(int id);

    boolean isReminderTuesday(int id);

    boolean isReminderWednesday(int id);

    boolean isReminderThursday(int id);

    boolean isReminderFriday(int id);

    boolean isReminderSaturday(int id);

    /**
     * Get the opinion for omer count suffix.
     *
     * @return the opinion.
     */
    String getOmerSuffix();
}

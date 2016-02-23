/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.preference;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import net.sf.media.RingtoneManager;
import net.sf.preference.TimePreference;
import net.sf.times.R;
import net.sf.times.compass.preference.CompassSettings;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.ZmanimCalendar;

import java.util.Calendar;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimSettings extends CompassSettings {

    /** Preference name for showing seconds. */
    public static final String KEY_SECONDS = "seconds.visible";
    /** Preference name for showing summaries. */
    public static final String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for enabling past times. */
    public static final String KEY_PAST = "past";
    /** Preference name for the last reminder. */
    private static final String KEY_REMINDER_LATEST = "reminder";
    /** Preference name for the reminder audio stream type. */
    public static final String KEY_REMINDER_STREAM = "reminder.stream";
    /** Preference name for the reminder ringtone. */
    public static final String KEY_REMINDER_RINGTONE = "reminder.ringtone";
    /** Preference name for the temporal hour visibility. */
    public static final String KEY_HOUR = "hour.visible";
    /** Preference name for the emphasis scale. */
    public static final String KEY_EMPHASIS_SCALE = "emphasis_scale";

    /** Preference name for temporal hour type. */
    public static final String KEY_OPINION_HOUR = "hour";
    /** Preference name for Alos type. */
    public static final String KEY_OPINION_DAWN = "dawn";
    /** Preference name for earliest tallis type. */
    public static final String KEY_OPINION_TALLIS = "tallis";
    /** Preference name for sunrise type. */
    public static final String KEY_OPINION_SUNRISE = "sunrise";
    /** Preference name for Last Shema type. */
    public static final String KEY_OPINION_SHEMA = "shema";
    /** Preference name for Last Morning Tfila type. */
    public static final String KEY_OPINION_TFILA = "prayers";
    /** Preference name for Last Biur Chametz type. */
    public static final String KEY_OPINION_BURN = "burn_chametz";
    /** Preference name for midday / noon type. */
    public static final String KEY_OPINION_NOON = "midday";
    /** Preference name for Earliest Mincha type. */
    public static final String KEY_OPINION_EARLIEST_MINCHA = "earliest_mincha";
    /** Preference name for Mincha Ketana type. */
    public static final String KEY_OPINION_MINCHA = "mincha";
    /** Preference name for Plug HaMincha type. */
    public static final String KEY_OPINION_PLUG_MINCHA = "plug_hamincha";
    /** Preference name for candle lighting minutes offset. */
    public static final String KEY_OPINION_CANDLES = "candles";
    /** Preference name for Chanukka candle lighting. */
    public static final String KEY_OPINION_CANDLES_CHANUKKA = "candles_chanukka";
    /** Preference name for sunset type. */
    public static final String KEY_OPINION_SUNSET = "sunset";
    /** Preference name for twilight type. */
    public static final String KEY_OPINION_TWILIGHT = "twilight";
    /** Preference name for nightfall type. */
    public static final String KEY_OPINION_NIGHTFALL = "nightfall";
    /** Preference name for Shabbath ends after nightfall. */
    public static final String KEY_OPINION_SHABBATH_ENDS = "shabbath_ends";
    public static final String KEY_OPINION_SHABBATH_ENDS_AFTER = KEY_OPINION_SHABBATH_ENDS + ".after";
    public static final String KEY_OPINION_SHABBATH_ENDS_MINUTES = KEY_OPINION_SHABBATH_ENDS + ".minutes";
    /** Preference name for midnight type. */
    public static final String KEY_OPINION_MIDNIGHT = "midnight";
    /** Preference name for earliest kiddush levana type. */
    public static final String KEY_OPINION_EARLIEST_LEVANA = "levana_earliest";
    /** Preference name for latest kiddush levana type. */
    public static final String KEY_OPINION_LATEST_LEVANA = "levana_latest";
    /** Preference name for omer count suffix. */
    public static final String KEY_OPINION_OMER = "omer";

    static final String REMINDER_SUFFIX = ".reminder";
    static final String REMINDER_SUNDAY_SUFFIX = ".day." + Calendar.SUNDAY;
    static final String REMINDER_MONDAY_SUFFIX = ".day." + Calendar.MONDAY;
    static final String REMINDER_TUESDAY_SUFFIX = ".day." + Calendar.TUESDAY;
    static final String REMINDER_WEDNESDAY_SUFFIX = ".day." + Calendar.WEDNESDAY;
    static final String REMINDER_THURSDAY_SUFFIX = ".day." + Calendar.THURSDAY;
    static final String REMINDER_FRIDAY_SUFFIX = ".day." + Calendar.FRIDAY;
    static final String REMINDER_SATURDAY_SUFFIX = ".day." + Calendar.SATURDAY;

    private static final String EMPHASIS_SUFFIX = ".emphasis";
    private static final String ANIM_SUFFIX = ".anim";

    /** Preference name for Alos reminder. */
    public static final String KEY_REMINDER_DAWN = KEY_OPINION_DAWN + REMINDER_SUFFIX;
    /** Preference name for earliest tallis reminder. */
    public static final String KEY_REMINDER_TALLIS = KEY_OPINION_TALLIS + REMINDER_SUFFIX;
    /** Preference name for sunrise reminder. */
    public static final String KEY_REMINDER_SUNRISE = KEY_OPINION_SUNRISE + REMINDER_SUFFIX;
    /** Preference name for Last Shema reminder. */
    public static final String KEY_REMINDER_SHEMA = KEY_OPINION_SHEMA + REMINDER_SUFFIX;
    /** Preference name for Last Morning Tfila reminder. */
    public static final String KEY_REMINDER_TFILA = KEY_OPINION_TFILA + REMINDER_SUFFIX;
    /** Preference name for midday / noon reminder. */
    public static final String KEY_REMINDER_NOON = KEY_OPINION_NOON + REMINDER_SUFFIX;
    /** Preference name for Earliest Mincha reminder. */
    public static final String KEY_REMINDER_EARLIEST_MINCHA = KEY_OPINION_EARLIEST_MINCHA + REMINDER_SUFFIX;
    /** Preference name for Mincha Ketana reminder. */
    public static final String KEY_REMINDER_MINCHA = KEY_OPINION_MINCHA + REMINDER_SUFFIX;
    /** Preference name for Plug HaMincha reminder. */
    public static final String KEY_REMINDER_PLUG_MINCHA = KEY_OPINION_PLUG_MINCHA + REMINDER_SUFFIX;
    /** Preference name for candle lighting reminder. */
    public static final String KEY_REMINDER_CANDLES = KEY_OPINION_CANDLES + REMINDER_SUFFIX;
    /** Preference name for sunset reminder. */
    public static final String KEY_REMINDER_SUNSET = KEY_OPINION_SUNSET + REMINDER_SUFFIX;
    /** Preference name for twilight reminder. */
    public static final String KEY_REMINDER_TWILIGHT = KEY_OPINION_TWILIGHT + REMINDER_SUFFIX;
    /** Preference name for nightfall reminder. */
    public static final String KEY_REMINDER_NIGHTFALL = KEY_OPINION_NIGHTFALL + REMINDER_SUFFIX;
    /** Preference name for midnight reminder. */
    public static final String KEY_REMINDER_MIDNIGHT = KEY_OPINION_MIDNIGHT + REMINDER_SUFFIX;
    /** Preference name for earliest kiddush levana reminder. */
    public static final String KEY_REMINDER_EARLIEST_LEVANA = KEY_OPINION_EARLIEST_LEVANA + REMINDER_SUFFIX;
    /** Preference name for latest kiddush levana reminder. */
    public static final String KEY_REMINDER_LATEST_LEVANA = KEY_OPINION_LATEST_LEVANA + REMINDER_SUFFIX;

    /** Preference name for candle lighting animations. */
    public static final String KEY_ANIM_CANDLES = KEY_OPINION_CANDLES + ANIM_SUFFIX;

    public static String OPINION_10_2;
    public static String OPINION_11;
    public static String OPINION_12;
    public static String OPINION_120;
    public static String OPINION_120_ZMANIS;
    public static String OPINION_13;
    public static String OPINION_15;
    public static String OPINION_16_1;
    public static String OPINION_16_1_ALOS;
    public static String OPINION_16_1_SUNSET;
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
    public static String OPINION_MGA;
    public static String OPINION_FIXED;
    public static String OPINION_LEVEL;
    public static String OPINION_NIGHT;
    public static String OPINION_SEA;
    public static String OPINION_TWILIGHT;

    /** Show zmanim list without background. */
    public static String LIST_THEME_NONE;
    /** Show zmanim list with white background. */
    public static String LIST_THEME_WHITE;

    /** No omer count. */
    public static String OMER_NONE;
    /** Omer count has "BaOmer" suffix. */
    public static String OMER_B;
    /** Omer count has "LaOmer" suffix. */
    public static String OMER_L;

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public ZmanimSettings(Context context) {
        super(context);
    }

    /**
     * Format times with seconds?
     *
     * @return {@code true} to show seconds.
     */
    public boolean isSeconds() {
        return preferences.getBoolean(KEY_SECONDS, context.getResources().getBoolean(R.bool.seconds_visible_defaultValue));
    }

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    public boolean isSummaries() {
        return preferences.getBoolean(KEY_SUMMARIES, context.getResources().getBoolean(R.bool.summaries_visible_defaultValue));
    }

    /**
     * Are past times enabled?
     *
     * @return {@code true} if older times are not grayed.
     */
    public boolean isPast() {
        return preferences.getBoolean(KEY_PAST, context.getResources().getBoolean(R.bool.past_defaultValue));
    }

    @Override
    protected int getTheme(String value) {
        if (TextUtils.isEmpty(value) || LIST_THEME_NONE.equals(value)) {
            return R.style.Theme_Zmanim_NoGradient;
        }
        if (LIST_THEME_LIGHT.equals(value)) {
            return R.style.Theme_Zmanim_Light;
        }
        if (LIST_THEME_WHITE.equals(value)) {
            return R.style.Theme_Zmanim_White;
        }
        return R.style.Theme_Zmanim_Dark;
    }

    /**
     * Is temporal hour visible?
     *
     * @return {@code true} to show hour.
     */
    public boolean isHour() {
        return preferences.getBoolean(KEY_HOUR, context.getResources().getBoolean(R.bool.hour_visible_defaultValue));
    }

    /**
     * Get the offset in minutes before sunset which is used in calculating
     * candle lighting time.
     *
     * @return the number of minutes.
     */
    public int getCandleLightingOffset() {
        return preferences.getInt(KEY_OPINION_CANDLES, context.getResources().getInteger(R.integer.candles_defaultValue));
    }

    /**
     * Get the opinion for Chanukka candle lighting time.
     *
     * @return the opinion.
     */
    public String getChanukkaCandles() {
        return preferences.getString(KEY_OPINION_CANDLES_CHANUKKA, context.getString(R.string.candles_chanukka_defaultValue));
    }

    /**
     * Get the opinion for temporal hour (<em>shaah zmanis</em>).
     *
     * @return the opinion.
     */
    public String getHour() {
        return preferences.getString(KEY_OPINION_HOUR, context.getString(R.string.hour_defaultValue));
    }

    /**
     * Get the type for temporal hour (<em>shaah zmanis</em>).
     *
     * @return the type.
     * @see ZmanimCalendar#SHAAH_ZMANIS_GRA
     * @see ZmanimCalendar#SHAAH_ZMANIS_MGA
     */
    public int getHourType() {
        String opinion = getHour();
        if (OPINION_19_8.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_19POINT8DEGREES;
        }
        if (OPINION_120.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_120MINUTES;
        }
        if (OPINION_120_ZMANIS.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_120MINUTES;
        }
        if (OPINION_18.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_18DEGREES;
        }
        if (OPINION_26.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_26DEGREES;
        }
        if (OPINION_16_1.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_16POINT1DEGREES;
        }
        if (OPINION_96.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_96MINUTES;
        }
        if (OPINION_96_ZMANIS.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_96MINUTES;
        }
        if (OPINION_90.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_90MINUTES;
        }
        if (OPINION_90_ZMANIS.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_90MINUTES;
        }
        if (OPINION_72.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_72MINUTES;
        }
        if (OPINION_72_ZMANIS.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_72MINUTES;
        }
        if (OPINION_60.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_60MINUTES;
        }
        if (OPINION_ATERET.equals(opinion)) {
            return ComplexZmanimCalendar.SHAAH_ZMANIS_GRA;
        }
        if (OPINION_MGA.equals(opinion)) {
            return ZmanimCalendar.SHAAH_ZMANIS_MGA;
        }
        return ZmanimCalendar.SHAAH_ZMANIS_GRA;
    }

    /**
     * Get the opinion for dawn (<em>alos</em>).
     *
     * @return the opinion.
     */
    public String getDawn() {
        return preferences.getString(KEY_OPINION_DAWN, context.getString(R.string.dawn_defaultValue));
    }

    /**
     * Get the opinion for earliest tallis &amp; tefillin (<em>misheyakir</em>).
     *
     * @return the opinion.
     */
    public String getTallis() {
        return preferences.getString(KEY_OPINION_TALLIS, context.getString(R.string.tallis_defaultValue));
    }

    /**
     * Get the opinion for sunrise.
     *
     * @return the opinion.
     */
    public String getSunrise() {
        return preferences.getString(KEY_OPINION_SUNRISE, context.getString(R.string.sunrise_defaultValue));
    }

    /**
     * Get the opinion for the last shema (<em>sof zman shma</em>).
     *
     * @return the opinion.
     */
    public String getLastShema() {
        return preferences.getString(KEY_OPINION_SHEMA, context.getString(R.string.shema_defaultValue));
    }

    /**
     * Get the opinion for the last morning prayers (<em>sof zman tfila</em>).
     *
     * @return the opinion.
     */
    public String getLastTfila() {
        return preferences.getString(KEY_OPINION_TFILA, context.getString(R.string.prayers_defaultValue));
    }

    /**
     * Get the opinion for burning chametz (<em>biur chametz</em>).
     *
     * @return the opinion.
     */
    public String getBurnChametz() {
        return preferences.getString(KEY_OPINION_BURN, context.getString(R.string.burn_chametz_defaultValue));
    }

    /**
     * Get the opinion for noon (<em>chatzos</em>).
     *
     * @return the opinion.
     */
    public String getMidday() {
        return preferences.getString(KEY_OPINION_NOON, context.getString(R.string.midday_defaultValue));
    }

    /**
     * Get the opinion for earliest afternoon prayers (<em>mincha gedola</em>).
     *
     * @return the opinion.
     */
    public String getEarliestMincha() {
        return preferences.getString(KEY_OPINION_EARLIEST_MINCHA, context.getString(R.string.earliest_mincha_defaultValue));
    }

    /**
     * Get the opinion for afternoon prayers (<em>mincha ketana</em>).
     *
     * @return the opinion.
     */
    public String getMincha() {
        return preferences.getString(KEY_OPINION_MINCHA, context.getString(R.string.mincha_defaultValue));
    }

    /**
     * Get the opinion for afternoon prayers (<em>plag hamincha</em>).
     *
     * @return the opinion.
     */
    public String getPlugHamincha() {
        return preferences.getString(KEY_OPINION_PLUG_MINCHA, context.getString(R.string.plug_hamincha_defaultValue));
    }

    /**
     * Get the opinion for sunset.
     *
     * @return the opinion.
     */
    public String getSunset() {
        return preferences.getString(KEY_OPINION_SUNSET, context.getString(R.string.sunset_defaultValue));
    }

    /**
     * Get the opinion for twilight (dusk).
     *
     * @return the opinion.
     */
    public String getTwilight() {
        return preferences.getString(KEY_OPINION_TWILIGHT, context.getString(R.string.twilight_defaultValue));
    }

    /**
     * Get the opinion for nightfall.
     *
     * @return the opinion.
     */
    public String getNightfall() {
        return preferences.getString(KEY_OPINION_NIGHTFALL, context.getString(R.string.nightfall_defaultValue));
    }

    /**
     * Get the time when Shabbath ends after.
     *
     * @return the time id.
     */
    public int getShabbathEndsAfter() {
        return toId(preferences.getString(KEY_OPINION_SHABBATH_ENDS_AFTER, context.getString(R.string.shabbath_ends_after_defaultValue)));
    }

    /**
     * Get the number of minutes when Shabbath ends after nightfall.
     *
     * @return the opinion.
     */
    public int getShabbathEnds() {
        return preferences.getInt(KEY_OPINION_SHABBATH_ENDS_MINUTES, context.getResources().getInteger(R.integer.shabbath_ends_defaultValue));
    }

    /**
     * Get the opinion for midnight (<em>chatzos layla</em>).
     *
     * @return the opinion.
     */
    public String getMidnight() {
        return preferences.getString(KEY_OPINION_MIDNIGHT, context.getString(R.string.midnight_defaultValue));
    }

    /**
     * Get the opinion for earliest kiddush levana.
     *
     * @return the opinion.
     */
    public String getEarliestKiddushLevana() {
        return preferences.getString(KEY_OPINION_EARLIEST_LEVANA, context.getString(R.string.levana_earliest_defaultValue));
    }

    /**
     * Get the opinion for latest kiddush levana.
     *
     * @return the opinion.
     */
    public String getLatestKiddushLevana() {
        return preferences.getString(KEY_OPINION_LATEST_LEVANA, context.getString(R.string.levana_latest_defaultValue));
    }

    /**
     * Get reminder of the zman. The reminder is either the number of minutes before the zman, or an absolute time.
     *
     * @param id
     *         the zman id.
     * @param time
     *         the zman time.
     * @return the reminder in milliseconds - {@code #NEVER} when no reminder.
     */
    public long getReminder(int id, long time) {
        if (time == NEVER) {
            return NEVER;
        }
        String key = toKey(id);
        if (key == null) {
            return NEVER;
        }
        String keyReminder = key + REMINDER_SUFFIX;

        String value = preferences.getString(keyReminder, context.getString(R.string.reminder_defaultValue));
        if (TextUtils.isEmpty(value)) {
            return NEVER;
        }

        if (value.indexOf(':') >= 0) {
            Calendar parsed = TimePreference.parseTime(value);
            if (parsed != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                cal.set(Calendar.HOUR_OF_DAY, parsed.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, parsed.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, parsed.get(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, 0);
                long when = cal.getTimeInMillis();
                // Reminders should always be before the zman.
                if (when > time) {
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    when = cal.getTimeInMillis();
                }
                return when;
            }
        } else {
            long before = Long.parseLong(value) * DateUtils.MINUTE_IN_MILLIS;
            if (before >= 0L) {
                return time - before;
            }
        }

        return NEVER;
    }

    /**
     * Get the time that was used for the latest reminder.
     *
     * @return the time.
     */
    public long getLatestReminder() {
        return preferences.getLong(KEY_REMINDER_LATEST, 0L);
    }

    /**
     * Set the time that was used for the latest reminder to now.
     *
     * @param time
     *         the time.
     */
    public void setLatestReminder(long time) {
        Editor editor = preferences.edit();
        editor.putLong(KEY_REMINDER_LATEST, time);
        editor.commit();
    }

    /**
     * Are the candles animated?
     *
     * @return {@code true} if candles animations enabled.
     */
    public boolean isCandlesAnimated() {
        return preferences.getBoolean(KEY_ANIM_CANDLES, context.getResources().getBoolean(R.bool.animate_defaultValue));
    }

    /**
     * Get the reminder audio stream type.
     *
     * @return the stream type.
     * @see AudioManager#STREAM_ALARM
     * @see AudioManager#STREAM_NOTIFICATION
     */
    public int getReminderStream() {
        return Integer.parseInt(preferences.getString(KEY_REMINDER_STREAM, context.getString(R.string.reminder_stream_defaultValue)));
    }

    /**
     * Get the reminder ringtone type.
     *
     * @return the ringtone type. One of {@link RingtoneManager#TYPE_ALARM} or {@link RingtoneManager#TYPE_NOTIFICATION}.
     */
    public int getReminderType() {
        int audioStreamType = getReminderStream();
        if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
            return RingtoneManager.TYPE_NOTIFICATION;
        }
        return RingtoneManager.TYPE_ALARM;
    }

    /**
     * Is the time emphasized?
     *
     * @param id
     *         the time id.
     * @return {@code true} for emphasis.
     */
    public boolean isEmphasis(int id) {
        String key = toKey(id);
        if (key != null)
            return preferences.getBoolean(key + EMPHASIS_SUFFIX, context.getResources().getBoolean(R.bool.emphasis_defaultValue));
        return false;
    }

    /**
     * Get the emphasis size scale.
     *
     * @return the emphasis scale as a fractional percentage.
     */
    public float getEmphasisScale() {
        String value = preferences.getString(KEY_EMPHASIS_SCALE, context.getString(R.string.emphasis_scale_defaultValue));
        return Float.parseFloat(value);
    }

    /**
     * Get the preference key name.
     *
     * @param id
     *         the time id.
     * @return the key - {@code null} otherwise.
     */
    protected String toKey(int id) {
        switch (id) {
            case R.string.hour:
                return KEY_OPINION_HOUR;
            case R.string.dawn:
                return KEY_OPINION_DAWN;
            case R.string.tallis:
            case R.string.tallis_only:
                return KEY_OPINION_TALLIS;
            case R.string.sunrise:
                return KEY_OPINION_SUNRISE;
            case R.string.shema:
                return KEY_OPINION_SHEMA;
            case R.string.prayers:
                return KEY_OPINION_TFILA;
            case R.string.midday:
                return KEY_OPINION_NOON;
            case R.string.earliest_mincha:
                return KEY_OPINION_EARLIEST_MINCHA;
            case R.string.mincha:
                return KEY_OPINION_MINCHA;
            case R.string.plug_hamincha:
                return KEY_OPINION_PLUG_MINCHA;
            case R.string.candles:
                return KEY_OPINION_CANDLES;
            case R.string.sunset:
                return KEY_OPINION_SUNSET;
            case R.string.twilight:
                return KEY_OPINION_TWILIGHT;
            case R.string.nightfall:
                return KEY_OPINION_NIGHTFALL;
            case R.string.shabbath_ends:
            case R.string.festival_ends:
                return KEY_OPINION_SHABBATH_ENDS;
            case R.string.midnight:
                return KEY_OPINION_MIDNIGHT;
            case R.string.levana_earliest:
                return KEY_OPINION_EARLIEST_LEVANA;
            case R.string.levana_latest:
                return KEY_OPINION_LATEST_LEVANA;
            default:
                return null;
        }
    }

    /**
     * Get the preference title id.
     *
     * @param name
     *         the time name.
     * @return the id - {@code null} otherwise.
     */
    protected int toId(String name) {
        switch (name) {
            case KEY_OPINION_HOUR:
                return R.string.hour;
            case KEY_OPINION_DAWN:
                return R.string.dawn;
            case KEY_OPINION_TALLIS:
                return R.string.tallis;
            case KEY_OPINION_SUNRISE:
                return R.string.sunrise;
            case KEY_OPINION_SHEMA:
                return R.string.shema;
            case KEY_OPINION_TFILA:
                return R.string.prayers;
            case KEY_OPINION_NOON:
                return R.string.midday;
            case KEY_OPINION_EARLIEST_MINCHA:
                return R.string.earliest_mincha;
            case KEY_OPINION_MINCHA:
                return R.string.mincha;
            case KEY_OPINION_PLUG_MINCHA:
                return R.string.plug_hamincha;
            case KEY_OPINION_CANDLES:
                return R.string.candles;
            case KEY_OPINION_SUNSET:
                return R.string.sunset;
            case KEY_OPINION_TWILIGHT:
                return R.string.twilight;
            case KEY_OPINION_NIGHTFALL:
                return R.string.nightfall;
            case KEY_OPINION_SHABBATH_ENDS:
                return R.string.shabbath_ends;
            case KEY_OPINION_MIDNIGHT:
                return R.string.midnight;
            case KEY_OPINION_EARLIEST_LEVANA:
                return R.string.levana_earliest;
            case KEY_OPINION_LATEST_LEVANA:
                return R.string.levana_latest;
            default:
                return 0;
        }
    }

    /**
     * Get the reminder ringtone.
     *
     * @return the ringtone.
     * @see RingtoneManager#getDefaultUri(int)
     */
    public Uri getReminderRingtone() {
        int type = getReminderType();
        String path = preferences.getString(KEY_REMINDER_RINGTONE, RingtoneManager.DEFAULT_PATH);
        if (path == RingtoneManager.DEFAULT_PATH) {
            path = RingtoneManager.getDefaultUri(type).toString();
        }
        RingtoneManager ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setType(type);
        if (!ringtoneManager.isIncludeExternal()) {
            path = ringtoneManager.filterInternal(path);
        }
        return TextUtils.isEmpty(path) ? null : Uri.parse(path);
    }

    public boolean isReminderSunday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_SUNDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_1_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderMonday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_MONDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_2_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderTuesday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_TUESDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_3_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderWednesday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_WEDNESDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_4_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderThursday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_THURSDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_5_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderFriday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_FRIDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_6_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderSaturday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_SATURDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_7_defaultValue));
            }
        }
        return true;
    }

    /**
     * Get the opinion for omer count suffix.
     *
     * @return the opinion.
     */
    public String getOmerSuffix() {
        return preferences.getString(KEY_OPINION_OMER, context.getString(R.string.omer_defaultValue));
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        CompassSettings.init(context);

        OPINION_10_2 = context.getString(R.string.opinion_value_10);
        OPINION_11 = context.getString(R.string.opinion_value_11);
        OPINION_12 = context.getString(R.string.opinion_value_12);
        OPINION_120 = context.getString(R.string.opinion_value_120);
        OPINION_120_ZMANIS = context.getString(R.string.opinion_value_120_zmanis);
        OPINION_13 = context.getString(R.string.opinion_value_13);
        OPINION_15 = context.getString(R.string.opinion_value_15);
        OPINION_16_1 = context.getString(R.string.opinion_value_16);
        OPINION_16_1_ALOS = context.getString(R.string.opinion_value_16_alos);
        OPINION_16_1_SUNSET = context.getString(R.string.opinion_value_16_sunset);
        OPINION_18 = context.getString(R.string.opinion_value_18);
        OPINION_19_8 = context.getString(R.string.opinion_value_19);
        OPINION_2 = context.getString(R.string.opinion_value_2);
        OPINION_26 = context.getString(R.string.opinion_value_26);
        OPINION_3 = context.getString(R.string.opinion_value_3);
        OPINION_3_65 = context.getString(R.string.opinion_value_3_65);
        OPINION_3_676 = context.getString(R.string.opinion_value_3_676);
        OPINION_3_7 = context.getString(R.string.opinion_value_3_7);
        OPINION_3_8 = context.getString(R.string.opinion_value_3_8);
        OPINION_30 = context.getString(R.string.opinion_value_30);
        OPINION_4_37 = context.getString(R.string.opinion_value_4_37);
        OPINION_4_61 = context.getString(R.string.opinion_value_4_61);
        OPINION_4_8 = context.getString(R.string.opinion_value_4_8);
        OPINION_5_88 = context.getString(R.string.opinion_value_5_88);
        OPINION_5_95 = context.getString(R.string.opinion_value_5_95);
        OPINION_58 = context.getString(R.string.opinion_value_58);
        OPINION_6 = context.getString(R.string.opinion_value_6);
        OPINION_60 = context.getString(R.string.opinion_value_60);
        OPINION_7 = context.getString(R.string.opinion_value_7);
        OPINION_7_083 = context.getString(R.string.opinion_value_7_083);
        OPINION_7_083_ZMANIS = context.getString(R.string.opinion_value_7_083_zmanis);
        OPINION_72 = context.getString(R.string.opinion_value_72);
        OPINION_72_ZMANIS = context.getString(R.string.opinion_value_72_zmanis);
        OPINION_8_5 = context.getString(R.string.opinion_value_8);
        OPINION_90 = context.getString(R.string.opinion_value_90);
        OPINION_90_ZMANIS = context.getString(R.string.opinion_value_90_zmanis);
        OPINION_96 = context.getString(R.string.opinion_value_96);
        OPINION_96_ZMANIS = context.getString(R.string.opinion_value_96_zmanis);
        OPINION_ATERET = context.getString(R.string.opinion_value_ateret);
        OPINION_GRA = context.getString(R.string.opinion_value_gra);
        OPINION_MGA = context.getString(R.string.opinion_value_mga);
        OPINION_FIXED = context.getString(R.string.opinion_value_fixed);
        OPINION_LEVEL = context.getString(R.string.opinion_value_level);
        OPINION_SEA = context.getString(R.string.opinion_value_sea);
        OPINION_TWILIGHT = context.getString(R.string.opinion_value_twilight);
        OPINION_NIGHT = context.getString(R.string.opinion_value_nightfall);

        LIST_THEME_NONE = context.getString(R.string.theme_value_none);
        LIST_THEME_WHITE = context.getString(R.string.theme_value_white);

        OMER_NONE = context.getString(R.string.omer_value_off);
        OMER_B = context.getString(R.string.omer_value_b);
        OMER_L = context.getString(R.string.omer_value_l);
    }
}

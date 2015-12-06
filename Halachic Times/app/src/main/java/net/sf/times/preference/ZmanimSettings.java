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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import net.sf.media.RingtoneManager;
import net.sf.times.R;

import java.util.Calendar;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimSettings {

    /** Preference name for the latitude. */
    private static final String KEY_LATITUDE = "latitude";
    /** Preference name for the longitude. */
    private static final String KEY_LONGITUDE = "longitude";
    /** Preference key for the elevation / altitude. */
    private static final String KEY_ELEVATION = "altitude";
    /** Preference name for the location provider. */
    private static final String KEY_PROVIDER = "provider";
    /** Preference name for the location time. */
    private static final String KEY_TIME = "time";
    /** Preference name for the co-ordinates visibility. */
    public static final String KEY_COORDS = "coords.visible";
    /** Preference name for the co-ordinates format. */
    public static final String KEY_COORDS_FORMAT = "coords.format";
    /** Preference name for showing seconds. */
    public static final String KEY_SECONDS = "seconds.visible";
    /** Preference name for showing summaries. */
    public static final String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for enabling past times. */
    public static final String KEY_PAST = "past";
    /**
     * Preference name for the background gradient.
     *
     * @deprecated use #KEY_THEME
     */
    @Deprecated
    public static final String KEY_BG_GRADIENT = "gradient";
    /** Preference name for the theme. */
    public static final String KEY_THEME = "theme";
    /** Preference name for the last reminder. */
    private static final String KEY_REMINDER_LATEST = "reminder";
    /** Preference name for the reminder audio stream type. */
    public static final String KEY_REMINDER_STREAM = "reminder.stream";
    /** Preference name for the reminder ringtone. */
    public static final String KEY_REMINDER_RINGTONE = "reminder.ringtone";
    /** Preference name for the temporal hour visibility. */
    public static final String KEY_HOUR = "hour.visible";

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
    public static final String KEY_OPINION_BURN = "biur_chametz";
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

    /** Format the coordinates in decimal notation. */
    public static String FORMAT_DECIMAL;
    /** Format the coordinates in sexagesimal notation. */
    public static String FORMAT_SEXIGESIMAL;

    /** Show zmanim list without background. */
    public static String LIST_THEME_NONE;
    /** Show zmanim list with dark gradient background. */
    public static String LIST_THEME_DARK;
    /** Show zmanim list with light gradient background. */
    public static String LIST_THEME_LIGHT;

    /** No omer count. */
    public static String OMER_NONE;
    /** Omer count has "BaOmer" suffix. */
    public static String OMER_B;
    /** Omer count has "LaOmer" suffix. */
    public static String OMER_L;

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    private Context context;
    private final SharedPreferences preferences;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public ZmanimSettings(Context context) {
        Context app = context.getApplicationContext();
        if (app != null)
            context = app;
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the data.
     *
     * @return the shared preferences.
     */
    public SharedPreferences getData() {
        return preferences;
    }

    /**
     * Get the editor to modify the preferences data.
     *
     * @return the editor.
     */
    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }

    /**
     * Get the location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        if (!preferences.contains(KEY_LATITUDE))
            return null;
        if (!preferences.contains(KEY_LONGITUDE))
            return null;
        double latitude;
        double longitude;
        double elevation;
        try {
            latitude = Double.parseDouble(preferences.getString(KEY_LATITUDE, "0"));
            longitude = Double.parseDouble(preferences.getString(KEY_LONGITUDE, "0"));
            elevation = Double.parseDouble(preferences.getString(KEY_ELEVATION, "0"));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return null;
        }
        String provider = preferences.getString(KEY_PROVIDER, "");
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        location.setTime(preferences.getLong(KEY_TIME, 0L));
        return location;
    }

    /**
     * Set the location.
     *
     * @return the location.
     */
    public void putLocation(Location location) {
        Editor editor = preferences.edit();
        editor.putString(KEY_PROVIDER, location.getProvider());
        editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
        editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
        editor.putString(KEY_ELEVATION, Double.toString(location.hasAltitude() ? location.getAltitude() : 0));
        editor.putLong(KEY_TIME, location.getTime());
        editor.commit();
    }

    /**
     * Are coordinates visible?
     *
     * @return {@code true} to show coordinates.
     */
    public boolean isCoordinates() {
        return preferences.getBoolean(KEY_COORDS, context.getResources().getBoolean(R.bool.coords_visible_defaultValue));
    }

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    public String getCoordinatesFormat() {
        return preferences.getString(KEY_COORDS_FORMAT, context.getString(R.string.coords_format_defaultValue));
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

    /**
     * Get the application theme.
     *
     * @return the theme resource id.
     */
    public int getTheme() {
        String value = preferences.getString(KEY_THEME, context.getString(R.string.theme_defaultValue));
        if (TextUtils.isEmpty(value) || LIST_THEME_NONE.equals(value) || !preferences.getBoolean(KEY_BG_GRADIENT, true)) {
            return R.style.Theme_Zmanim_NoGradient;
        }
        if (LIST_THEME_LIGHT.equals(value)) {
            return R.style.Theme_Zmanim_Light;
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
     * Get reminder as the number of minutes before the zman.
     *
     * @param id
     *         the zman id.
     * @return the number of minutes in milliseconds - negative value when no reminder.
     */
    public long getReminderBefore(int id) {
        String key = getKey(id);
        if (key != null)
            return getReminderBefore(key + REMINDER_SUFFIX);
        return NEVER;
    }

    /**
     * Get reminder as the number of minutes before the zman.
     *
     * @param key
     *         the zman reminder key.
     * @return the number of minutes in milliseconds - negative value when no reminder.
     */
    public long getReminderBefore(String key) {
        String value = preferences.getString(key, context.getString(R.string.reminder_defaultValue));
        if (!TextUtils.isEmpty(value))
            return Long.parseLong(value) * DateUtils.MINUTE_IN_MILLIS;
        return NEVER;
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
        long before = getReminderBefore(id);
        if (before < 0L) {
            return NEVER;
        }
        return time - before;
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
        String key = getKey(id);
        if (key != null)
            return preferences.getBoolean(key + EMPHASIS_SUFFIX, context.getResources().getBoolean(R.bool.emphasis_defaultValue));
        return false;
    }

    /**
     * Get the preference key name.
     *
     * @param id
     *         the time id.
     * @return the key - {@code null} otherwise.
     */
    protected String getKey(int id) {
        if (id == R.string.hour)
            return KEY_OPINION_HOUR;
        if (id == R.string.dawn)
            return KEY_OPINION_DAWN;
        if ((id == R.string.tallis) || (id == R.string.tallis_only))
            return KEY_OPINION_TALLIS;
        if (id == R.string.sunrise)
            return KEY_OPINION_SUNRISE;
        if (id == R.string.shema)
            return KEY_OPINION_SHEMA;
        if (id == R.string.prayers)
            return KEY_OPINION_TFILA;
        if (id == R.string.midday)
            return KEY_OPINION_NOON;
        if (id == R.string.earliest_mincha)
            return KEY_OPINION_EARLIEST_MINCHA;
        if (id == R.string.mincha)
            return KEY_OPINION_MINCHA;
        if (id == R.string.plug_hamincha)
            return KEY_OPINION_PLUG_MINCHA;
        if (id == R.string.candles)
            return KEY_OPINION_CANDLES;
        if (id == R.string.sunset)
            return KEY_OPINION_SUNSET;
        if (id == R.string.twilight)
            return KEY_OPINION_TWILIGHT;
        if (id == R.string.nightfall)
            return KEY_OPINION_NIGHTFALL;
        if (id == R.string.midnight)
            return KEY_OPINION_MIDNIGHT;
        if (id == R.string.levana_earliest)
            return KEY_OPINION_EARLIEST_LEVANA;
        if (id == R.string.levana_latest)
            return KEY_OPINION_LATEST_LEVANA;

        return null;
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
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_SUNDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_1_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderMonday(int id) {
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_MONDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_2_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderTuesday(int id) {
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_TUESDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_3_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderWednesday(int id) {
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_WEDNESDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_4_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderThursday(int id) {
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_THURSDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_5_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderFriday(int id) {
        String key = getKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_FRIDAY_SUFFIX;
            if (keyDay != null) {
                return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_6_defaultValue));
            }
        }
        return true;
    }

    public boolean isReminderSaturday(int id) {
        String key = getKey(id);
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

        FORMAT_DECIMAL = context.getString(R.string.coords_format_value_decimal);
        FORMAT_SEXIGESIMAL = context.getString(R.string.coords_format_value_sexagesimal);

        LIST_THEME_NONE = context.getString(R.string.theme_value_none);
        LIST_THEME_DARK = context.getString(R.string.theme_value_dark);
        LIST_THEME_LIGHT = context.getString(R.string.theme_value_light);

        OMER_NONE = context.getString(R.string.omer_value_off);
        OMER_B = context.getString(R.string.omer_value_b);
        OMER_L = context.getString(R.string.omer_value_l);
    }
}

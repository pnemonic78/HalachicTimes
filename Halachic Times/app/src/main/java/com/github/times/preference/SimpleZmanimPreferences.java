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
package com.github.times.preference;

import net.sourceforge.zmanim.ShaahZmanis;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import com.github.media.RingtoneManager;
import com.github.preference.LocalePreferences;
import com.github.preference.SimpleLocalePreferences;
import com.github.preference.SimplePreferences;
import com.github.preference.SimpleThemePreferences;
import com.github.preference.ThemePreferences;
import com.github.preference.TimePreference;
import com.github.times.R;

import androidx.annotation.NonNull;
import androidx.core.os.BuildCompat;

import static android.text.TextUtils.isEmpty;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static com.github.preference.ThemePreferences.Values.THEME_DEFAULT;
import static com.github.preference.ThemePreferences.Values.THEME_LIGHT;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.times.preference.ZmanimPreferences.Values.*;

/**
 * Simple application preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleZmanimPreferences extends SimplePreferences implements ZmanimPreferences {

    private final ThemePreferences themePreferences;
    private final LocalePreferences localePreferences;

    /**
     * Constructs a new preferences.
     *
     * @param context the context.
     */
    public SimpleZmanimPreferences(Context context) {
        super(context);
        this.themePreferences = new SimpleThemePreferences(context);
        this.localePreferences = new SimpleLocalePreferences(context);
        init(context);
    }

    @Override
    public boolean isSeconds() {
        return preferences.getBoolean(KEY_SECONDS, context.getResources().getBoolean(R.bool.seconds_visible_defaultValue));
    }

    @Override
    public boolean isSummaries() {
        return preferences.getBoolean(KEY_SUMMARIES, context.getResources().getBoolean(R.bool.summaries_visible_defaultValue));
    }

    @Override
    public boolean isPast() {
        return preferences.getBoolean(KEY_PAST, context.getResources().getBoolean(R.bool.past_defaultValue));
    }

    @Override
    public boolean isUpcomingNotification() {
        return preferences.getBoolean(KEY_NOTIFICATION_UPCOMING, context.getResources().getBoolean(R.bool.notification_upcoming_defaultValue));
    }

    @Override
    public String getThemeValue() {
        return themePreferences.getThemeValue();
    }

    @Override
    public int getTheme(String value) {
        if (isEmpty(value) || THEME_NONE.equals(value)) {
            return R.style.Theme_Zmanim_NoGradient;
        }
        if (THEME_LIGHT.equals(value)) {
            return R.style.Theme_Zmanim_Light;
        }
        if (THEME_WHITE.equals(value)) {
            return R.style.Theme_Zmanim_White;
        }
        return R.style.Theme_Zmanim_Dark;
    }

    @Override
    public int getTheme() {
        return getTheme(getThemeValue());
    }

    @Override
    public boolean isDarkTheme(String value) {
        if (THEME_LIGHT.equals(value) || THEME_WHITE.equals(value)) {
            return false;
        }
        if (THEME_DEFAULT.equals(value)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (BuildCompat.isAtLeastQ()) {
                    final int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightMode) {
                        case Configuration.UI_MODE_NIGHT_NO:
                            return false;
                        case Configuration.UI_MODE_NIGHT_YES:
                            return true;
                    }
                }

                // Material
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDarkTheme() {
        return isDarkTheme(getThemeValue());
    }

    @Override
    public boolean isHour() {
        return preferences.getBoolean(KEY_HOUR, context.getResources().getBoolean(R.bool.hour_visible_defaultValue));
    }

    @Override
    public int getCandleLightingOffset() {
        return preferences.getInt(KEY_OPINION_CANDLES, context.getResources().getInteger(R.integer.candles_defaultValue));
    }

    @Override
    public String getChanukkaCandles() {
        return preferences.getString(KEY_OPINION_CANDLES_CHANUKKA, context.getString(R.string.candles_chanukka_defaultValue));
    }

    @Override
    public String getHour() {
        return preferences.getString(KEY_OPINION_HOUR, context.getString(R.string.hour_defaultValue));
    }

    @Override
    public ShaahZmanis getHourType() {
        String opinion = getHour();
        if (OPINION_19_8.equals(opinion)) {
            return ShaahZmanis.DEGREES_19POINT8;
        }
        if (OPINION_120.equals(opinion)) {
            return ShaahZmanis.MINUTES_120;
        }
        if (OPINION_120_ZMANIS.equals(opinion)) {
            return ShaahZmanis.MINUTES_120;
        }
        if (OPINION_18.equals(opinion)) {
            return ShaahZmanis.DEGREES_18;
        }
        if (OPINION_26.equals(opinion)) {
            return ShaahZmanis.DEGREES_26;
        }
        if (OPINION_16_1.equals(opinion)) {
            return ShaahZmanis.DEGREES_16POINT1;
        }
        if (OPINION_96.equals(opinion)) {
            return ShaahZmanis.MINUTES_96;
        }
        if (OPINION_96_ZMANIS.equals(opinion)) {
            return ShaahZmanis.MINUTES_96;
        }
        if (OPINION_90.equals(opinion)) {
            return ShaahZmanis.MINUTES_90;
        }
        if (OPINION_90_ZMANIS.equals(opinion)) {
            return ShaahZmanis.MINUTES_90;
        }
        if (OPINION_72.equals(opinion)) {
            return ShaahZmanis.MINUTES_72;
        }
        if (OPINION_72_ZMANIS.equals(opinion)) {
            return ShaahZmanis.MINUTES_72;
        }
        if (OPINION_60.equals(opinion)) {
            return ShaahZmanis.MINUTES_60;
        }
        if (OPINION_MGA.equals(opinion)) {
            return ShaahZmanis.MGA;
        }
        if (OPINION_BAAL_HATANYA.equals(opinion)) {
            return ShaahZmanis.BAAL_HATANYA;
        }
        return ShaahZmanis.GRA;
    }

    @Override
    public String getDawn() {
        return preferences.getString(KEY_OPINION_DAWN, context.getString(R.string.dawn_defaultValue));
    }

    @Override
    public String getTallis() {
        return preferences.getString(KEY_OPINION_TALLIS, context.getString(R.string.tallis_defaultValue));
    }

    @Override
    public String getSunrise() {
        return preferences.getString(KEY_OPINION_SUNRISE, context.getString(R.string.sunrise_defaultValue));
    }

    @Override
    public String getLastShema() {
        return preferences.getString(KEY_OPINION_SHEMA, context.getString(R.string.shema_defaultValue));
    }

    @Override
    public String getLastTfila() {
        return preferences.getString(KEY_OPINION_TFILA, context.getString(R.string.prayers_defaultValue));
    }

    @Override
    public String getEatChametz() {
        return preferences.getString(KEY_OPINION_EAT, context.getString(R.string.eat_chametz_defaultValue));
    }

    @Override
    public String getBurnChametz() {
        return preferences.getString(KEY_OPINION_BURN, context.getString(R.string.burn_chametz_defaultValue));
    }

    @Override
    public String getMidday() {
        return preferences.getString(KEY_OPINION_NOON, context.getString(R.string.midday_defaultValue));
    }

    @Override
    public String getEarliestMincha() {
        return preferences.getString(KEY_OPINION_EARLIEST_MINCHA, context.getString(R.string.earliest_mincha_defaultValue));
    }

    @Override
    public String getMincha() {
        return preferences.getString(KEY_OPINION_MINCHA, context.getString(R.string.mincha_defaultValue));
    }

    @Override
    public String getPlugHamincha() {
        return preferences.getString(KEY_OPINION_PLUG_MINCHA, context.getString(R.string.plug_hamincha_defaultValue));
    }

    @Override
    public String getSunset() {
        return preferences.getString(KEY_OPINION_SUNSET, context.getString(R.string.sunset_defaultValue));
    }

    @Override
    public String getTwilight() {
        return preferences.getString(KEY_OPINION_TWILIGHT, context.getString(R.string.twilight_defaultValue));
    }

    @Override
    public String getNightfall() {
        return preferences.getString(KEY_OPINION_NIGHTFALL, context.getString(R.string.nightfall_defaultValue));
    }

    @Override
    public int getShabbathEndsAfter() {
        return toId(preferences.getString(KEY_OPINION_SHABBATH_ENDS_AFTER, context.getString(R.string.shabbath_ends_after_defaultValue)));
    }

    @Override
    public String getShabbathEndsSunset() {
        return preferences.getString(KEY_OPINION_SHABBATH_ENDS_SUNSET, context.getString(R.string.shabbath_ends_sunset_defaultValue));
    }

    @Override
    public String getShabbathEndsTwilight() {
        return preferences.getString(KEY_OPINION_SHABBATH_ENDS_TWILIGHT, context.getString(R.string.shabbath_ends_twilight_defaultValue));
    }

    @Override
    public String getShabbathEndsNightfall() {
        return preferences.getString(KEY_OPINION_SHABBATH_ENDS_NIGHTFALL, context.getString(R.string.shabbath_ends_nightfall_defaultValue));
    }

    @Override
    public int getShabbathEnds() {
        return preferences.getInt(KEY_OPINION_SHABBATH_ENDS_MINUTES, context.getResources().getInteger(R.integer.shabbath_ends_defaultValue));
    }

    @Override
    public String getMidnight() {
        return preferences.getString(KEY_OPINION_MIDNIGHT, context.getString(R.string.midnight_defaultValue));
    }

    @Override
    public String getEarliestKiddushLevana() {
        return preferences.getString(KEY_OPINION_EARLIEST_LEVANA, context.getString(R.string.levana_earliest_defaultValue));
    }

    @Override
    public String getLatestKiddushLevana() {
        return preferences.getString(KEY_OPINION_LATEST_LEVANA, context.getString(R.string.levana_latest_defaultValue));
    }

    @Override
    public String getGuardsCount() {
        return preferences.getString(KEY_OPINION_GUARDS, context.getString(R.string.guards_defaultValue));
    }

    @Override
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
        if (isEmpty(value)) {
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
            long before = Long.parseLong(value) * MINUTE_IN_MILLIS;
            if (before >= 0L) {
                return time - before;
            }
        }

        return NEVER;
    }

    @Override
    public long getLatestReminder() {
        return preferences.getLong(KEY_REMINDER_LATEST, 0L);
    }

    @Override
    public void setLatestReminder(long time) {
        Editor editor = preferences.edit();
        editor.putLong(KEY_REMINDER_LATEST, time);
        editor.apply();
    }

    @Override
    public boolean isCandlesAnimated() {
        return preferences.getBoolean(KEY_ANIM_CANDLES, context.getResources().getBoolean(R.bool.animate_defaultValue));
    }

    @Override
    public int getReminderStream() {
        return Integer.parseInt(preferences.getString(KEY_REMINDER_STREAM, context.getString(R.string.reminder_stream_defaultValue)));
    }

    @Override
    public int getReminderType() {
        int audioStreamType = getReminderStream();
        if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
            return RingtoneManager.TYPE_NOTIFICATION;
        }
        return RingtoneManager.TYPE_ALARM;
    }

    @Override
    public boolean isEmphasis(int id) {
        String key = toKey(id);
        return (key != null) && preferences.getBoolean(key + EMPHASIS_SUFFIX, context.getResources().getBoolean(R.bool.emphasis_defaultValue));
    }

    @Override
    public float getEmphasisScale() {
        String value = preferences.getString(KEY_EMPHASIS_SCALE, context.getString(R.string.emphasis_scale_defaultValue));
        return Float.parseFloat(value);
    }

    @Override
    public String toKey(int id) {
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
            case R.string.eat_chametz:
                return KEY_OPINION_EAT;
            case R.string.burn_chametz:
                return KEY_OPINION_BURN;
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
            case R.string.chanukka:
                return KEY_OPINION_CANDLES_CHANUKKA;
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
            case R.string.midnight_guard:
                return KEY_OPINION_MIDNIGHT_GUARD;
            case R.string.morning_guard:
                return KEY_OPINION_MORNING_GUARD;
            case R.string.levana_earliest:
                return KEY_OPINION_EARLIEST_LEVANA;
            case R.string.levana_latest:
                return KEY_OPINION_LATEST_LEVANA;
            case R.string.omer:
                return KEY_OPINION_OMER;
            default:
                return null;
        }
    }

    @Override
    public int toId(String key) {
        if (TextUtils.isEmpty(key)) {
            return 0;
        }
        switch (key) {
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
            case KEY_OPINION_EAT:
                return R.string.eat_chametz;
            case KEY_OPINION_BURN:
                return R.string.burn_chametz;
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
            case KEY_OPINION_CANDLES_CHANUKKA:
                return R.string.chanukka;
            case KEY_OPINION_SUNSET:
            case KEY_OPINION_SHABBATH_ENDS_SUNSET:
                return R.string.sunset;
            case KEY_OPINION_TWILIGHT:
            case KEY_OPINION_SHABBATH_ENDS_TWILIGHT:
                return R.string.twilight;
            case KEY_OPINION_NIGHTFALL:
            case KEY_OPINION_SHABBATH_ENDS_NIGHTFALL:
                return R.string.nightfall;
            case KEY_OPINION_SHABBATH_ENDS:
            case KEY_OPINION_SHABBATH_ENDS_AFTER:
            case KEY_OPINION_SHABBATH_ENDS_MINUTES:
                return R.string.shabbath_ends;
            case KEY_OPINION_MIDNIGHT:
                return R.string.midnight;
            case KEY_OPINION_MIDNIGHT_GUARD:
                return R.string.midnight_guard;
            case KEY_OPINION_MORNING_GUARD:
                return R.string.morning_guard;
            case KEY_OPINION_EARLIEST_LEVANA:
                return R.string.levana_earliest;
            case KEY_OPINION_LATEST_LEVANA:
                return R.string.levana_latest;
            case KEY_OPINION_OMER:
                return R.string.omer;
            default:
                return 0;
        }
    }

    @Override
    public Uri getReminderRingtone() {
        int type = getReminderType();
        String path = preferences.getString(KEY_REMINDER_RINGTONE, RingtoneManager.DEFAULT_PATH);
        if (path == RingtoneManager.DEFAULT_PATH) {
            path = RingtoneManager.getDefaultUri(type).toString();
        }
        RingtoneManager ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setType(type);
        path = ringtoneManager.filterInternalMaybe(path);
        return isEmpty(path) ? null : (path.charAt(0) == File.separatorChar ? Uri.fromFile(new File(path)) : Uri.parse(path));
    }

    @Override
    public boolean isReminderSunday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_SUNDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_1_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderMonday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_MONDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_2_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderTuesday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_TUESDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_3_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderWednesday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_WEDNESDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_4_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderThursday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_THURSDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_5_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderFriday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_FRIDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_6_defaultValue));
        }
        return true;
    }

    @Override
    public boolean isReminderSaturday(int id) {
        String key = toKey(id);
        if (key != null) {
            String keyDay = key + REMINDER_SUFFIX + REMINDER_SATURDAY_SUFFIX;
            return preferences.getBoolean(keyDay, context.getResources().getBoolean(R.bool.reminder_day_7_defaultValue));
        }
        return true;
    }

    @Override
    public String getOmerSuffix() {
        return preferences.getString(KEY_OPINION_OMER, context.getString(R.string.omer_defaultValue));
    }

    @Override
    public String getAppWidgetThemeValue() {
        return preferences.getString(KEY_THEME_WIDGET, context.getString(R.string.appwidget_theme_defaultValue));
    }

    @Override
    public int getAppWidgetTheme(String value) {
        if (isEmpty(value) || THEME_NONE.equals(value)) {
            return 0;
        }
        if (THEME_LIGHT.equals(value)) {
            return R.style.Theme_AppWidget_Light;
        }
        return R.style.Theme_AppWidget_Dark;
    }

    @Override
    public int getAppWidgetTheme() {
        return getAppWidgetTheme(getAppWidgetThemeValue());
    }

    @Override
    public boolean isAppWidgetDarkTheme(String value) {
        if (THEME_LIGHT.equals(value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAppWidgetDarkTheme() {
        return isAppWidgetDarkTheme(getAppWidgetThemeValue());
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context the context.
     */
    public static void init(Context context) {
        final Resources res = context.getResources();

        OPINION_2 = res.getString(R.string.opinion_value_2);
        OPINION_2_STARS = res.getString(R.string.opinion_value_2_stars);
        OPINION_3 = res.getString(R.string.opinion_value_3);
        OPINION_3_65 = res.getString(R.string.opinion_value_3_65);
        OPINION_3_676 = res.getString(R.string.opinion_value_3_676);
        OPINION_3_7 = res.getString(R.string.opinion_value_3_7);
        OPINION_3_8 = res.getString(R.string.opinion_value_3_8);
        OPINION_4 = res.getString(R.string.opinion_value_4);
        OPINION_4_37 = res.getString(R.string.opinion_value_4_37);
        OPINION_4_61 = res.getString(R.string.opinion_value_4_61);
        OPINION_4_8 = res.getString(R.string.opinion_value_4_8);
        OPINION_5_88 = res.getString(R.string.opinion_value_5_88);
        OPINION_5_95 = res.getString(R.string.opinion_value_5_95);
        OPINION_6 = res.getString(R.string.opinion_value_6);
        OPINION_6_45 = res.getString(R.string.opinion_value_6_45);
        OPINION_7 = res.getString(R.string.opinion_value_7);
        OPINION_7_083 = res.getString(R.string.opinion_value_7_083);
        OPINION_7_083_ZMANIS = res.getString(R.string.opinion_value_7_083_zmanis);
        OPINION_7_65 = res.getString(R.string.opinion_value_7_65);
        OPINION_7_67 = res.getString(R.string.opinion_value_7_67);
        OPINION_8_5 = res.getString(R.string.opinion_value_8);
        OPINION_9_3 = res.getString(R.string.opinion_value_9_3);
        OPINION_9_5 = res.getString(R.string.opinion_value_9_5);
        OPINION_9_75 = res.getString(R.string.opinion_value_9_75);
        OPINION_10_2 = res.getString(R.string.opinion_value_10);
        OPINION_11 = res.getString(R.string.opinion_value_11);
        OPINION_12 = res.getString(R.string.opinion_value_12);
        OPINION_13 = res.getString(R.string.opinion_value_13);
        OPINION_15 = res.getString(R.string.opinion_value_15);
        OPINION_15_ALOS = res.getString(R.string.opinion_value_15_alos);
        OPINION_16_1 = res.getString(R.string.opinion_value_16);
        OPINION_16_1_ALOS = res.getString(R.string.opinion_value_16_alos);
        OPINION_16_1_SUNSET = res.getString(R.string.opinion_value_16_sunset);
        OPINION_18 = res.getString(R.string.opinion_value_18);
        OPINION_19 = res.getString(R.string.opinion_value_19);
        OPINION_19_8 = res.getString(R.string.opinion_value_19_8);
        OPINION_26 = res.getString(R.string.opinion_value_26);
        OPINION_30 = res.getString(R.string.opinion_value_30);
        OPINION_58 = res.getString(R.string.opinion_value_58);
        OPINION_60 = res.getString(R.string.opinion_value_60);
        OPINION_72 = res.getString(R.string.opinion_value_72);
        OPINION_72_ZMANIS = res.getString(R.string.opinion_value_72_zmanis);
        OPINION_90 = res.getString(R.string.opinion_value_90);
        OPINION_90_ZMANIS = res.getString(R.string.opinion_value_90_zmanis);
        OPINION_96 = res.getString(R.string.opinion_value_96);
        OPINION_96_ZMANIS = res.getString(R.string.opinion_value_96_zmanis);
        OPINION_120 = res.getString(R.string.opinion_value_120);
        OPINION_120_ZMANIS = res.getString(R.string.opinion_value_120_zmanis);
        OPINION_168 = res.getString(R.string.opinion_value_168);
        OPINION_ATERET = res.getString(R.string.opinion_value_ateret);
        OPINION_BAAL_HATANYA = res.getString(R.string.opinion_value_baal_hatanya);
        OPINION_FIXED = res.getString(R.string.opinion_value_fixed);
        OPINION_GRA = res.getString(R.string.opinion_value_gra);
        OPINION_HALF = res.getString(R.string.opinion_value_half);
        OPINION_LEVEL = res.getString(R.string.opinion_value_level);
        OPINION_MGA = res.getString(R.string.opinion_value_mga);
        OPINION_NIGHT = res.getString(R.string.opinion_value_nightfall);
        OPINION_NONE = res.getString(R.string.opinion_value_none);
        OPINION_SEA = res.getString(R.string.opinion_value_sea);
        OPINION_TWILIGHT = res.getString(R.string.opinion_value_twilight);

        THEME_NONE = res.getString(R.string.theme_value_none);
        THEME_WHITE = res.getString(R.string.theme_value_white);

        OMER_NONE = res.getString(R.string.omer_value_off);
        OMER_B = res.getString(R.string.omer_value_b);
        OMER_L = res.getString(R.string.omer_value_l);
    }

    @NonNull
    @Override
    public Locale getLocale() {
        return localePreferences.getLocale();
    }

    @Override
    public boolean isUseElevation() {
        return !OPINION_SEA.equals(getSunrise());
    }
}

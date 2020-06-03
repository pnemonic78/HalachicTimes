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
package com.github.times;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.times.preference.ZmanimPreferences;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import timber.log.Timber;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.times.ZmanimDays.SHABBATH;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.TimeUtils.isSameDay;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.SATURDAY;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHOL_HAMOED_PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHOL_HAMOED_SUCCOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.EREV_PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.FAST_OF_ESTHER;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.FAST_OF_GEDALYAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.HOSHANA_RABBA;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.ROSH_HASHANA;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SEVENTEEN_OF_TAMMUZ;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHAVUOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHEMINI_ATZERES;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SIMCHAS_TORAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SUCCOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TENTH_OF_TEVES;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TISHA_BEAV;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

/**
 * Populate a list of zmanim.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPopulater<A extends ZmanimAdapter> {

    /**
     * 12 hours (half of a full day).
     */
    protected static final long TWELVE_HOURS = DAY_IN_MILLIS >> 1;
    /**
     * 6 hours (quarter of a full day).
     */
    protected static final long SIX_HOURS = DAY_IN_MILLIS >> 2;

    /**
     * No candles to light.
     */
    private static final int CANDLES_NONE = 0;
    /**
     * Number of candles to light for Shabbath.
     */
    private static final int CANDLES_SHABBATH = 2;
    /**
     * Number of candles to light for a festival.
     */
    private static final int CANDLES_FESTIVAL = 2;
    /**
     * Number of candles to light for Yom Kippurim.
     */
    private static final int CANDLES_YOM_KIPPUR = 1;

    static final int CANDLES_MASK_OFFSET = 0;
    private static final int CANDLES_MASK_BITS = 4;
    static final int CANDLES_MASK = (1 << CANDLES_MASK_BITS) - 1;
    static final int HOLIDAY_MASK_OFFSET = CANDLES_MASK_OFFSET + CANDLES_MASK_BITS;
    private static final int HOLIDAY_MASK_BITS = 8;
    static final int HOLIDAY_MASK = (1 << HOLIDAY_MASK_BITS) - 1;
    static final int HOLIDAY_TOMORROW_MASK_OFFSET = HOLIDAY_MASK_OFFSET + HOLIDAY_MASK_BITS;
    static final int MOTZE_MASK_OFFSET = HOLIDAY_TOMORROW_MASK_OFFSET + HOLIDAY_MASK_BITS;
    private static final int MOTZE_MASK_BITS = 4;
    static final int MOTZE_MASK = (1 << MOTZE_MASK_BITS) - 1;
    private static final int OFFSET_MASK_OFFSET = MOTZE_MASK_OFFSET + MOTZE_MASK_BITS;
    private static final int OFFSET_MASK_BITS = 12;
    private static final int OFFSET_MASK = (1 << OFFSET_MASK_BITS) - 1;

    /**
     * Flag indicating lighting times before sunset.
     */
    static final int BEFORE_SUNSET = 0;
    /**
     * Flag indicating lighting times at sunset.
     */
    static final int AT_SUNSET = 1;
    /**
     * Flag indicating lighting times at twilight.
     */
    static final int AT_TWILIGHT = 2;
    /**
     * Flag indicating lighting times after nightfall.
     */
    static final int AT_NIGHT = 3;
    /**
     * Flag indicating lighting times after Shabbath.
     */
    static final int MOTZE_SHABBATH = 4;

    protected static final String OPINION_2 = ZmanimPreferences.Values.OPINION_2;
    protected static final String OPINION_2_STARS = ZmanimPreferences.Values.OPINION_2_STARS;
    protected static final String OPINION_3 = ZmanimPreferences.Values.OPINION_3;
    protected static final String OPINION_3_65 = ZmanimPreferences.Values.OPINION_3_65;
    protected static final String OPINION_3_676 = ZmanimPreferences.Values.OPINION_3_676;
    protected static final String OPINION_3_7 = ZmanimPreferences.Values.OPINION_3_7;
    protected static final String OPINION_3_8 = ZmanimPreferences.Values.OPINION_3_8;
    protected static final String OPINION_4 = ZmanimPreferences.Values.OPINION_4;
    protected static final String OPINION_4_37 = ZmanimPreferences.Values.OPINION_4_37;
    protected static final String OPINION_4_61 = ZmanimPreferences.Values.OPINION_4_61;
    protected static final String OPINION_4_8 = ZmanimPreferences.Values.OPINION_4_8;
    protected static final String OPINION_5_88 = ZmanimPreferences.Values.OPINION_5_88;
    protected static final String OPINION_5_95 = ZmanimPreferences.Values.OPINION_5_95;
    protected static final String OPINION_6 = ZmanimPreferences.Values.OPINION_6;
    protected static final String OPINION_6_45 = ZmanimPreferences.Values.OPINION_6_45;
    protected static final String OPINION_7 = ZmanimPreferences.Values.OPINION_7;
    protected static final String OPINION_7_083 = ZmanimPreferences.Values.OPINION_7_083;
    protected static final String OPINION_7_083_ZMANIS = ZmanimPreferences.Values.OPINION_7_083_ZMANIS;
    protected static final String OPINION_7_65 = ZmanimPreferences.Values.OPINION_7_65;
    protected static final String OPINION_7_67 = ZmanimPreferences.Values.OPINION_7_67;
    protected static final String OPINION_8_5 = ZmanimPreferences.Values.OPINION_8_5;
    protected static final String OPINION_9_3 = ZmanimPreferences.Values.OPINION_9_3;
    protected static final String OPINION_9_5 = ZmanimPreferences.Values.OPINION_9_5;
    protected static final String OPINION_9_75 = ZmanimPreferences.Values.OPINION_9_75;
    protected static final String OPINION_10_2 = ZmanimPreferences.Values.OPINION_10_2;
    protected static final String OPINION_11 = ZmanimPreferences.Values.OPINION_11;
    protected static final String OPINION_12 = ZmanimPreferences.Values.OPINION_12;
    protected static final String OPINION_13 = ZmanimPreferences.Values.OPINION_13;
    protected static final String OPINION_15 = ZmanimPreferences.Values.OPINION_15;
    protected static final String OPINION_15_ALOS = ZmanimPreferences.Values.OPINION_15_ALOS;
    protected static final String OPINION_16_1 = ZmanimPreferences.Values.OPINION_16_1;
    protected static final String OPINION_16_1_ALOS = ZmanimPreferences.Values.OPINION_16_1_ALOS;
    protected static final String OPINION_16_1_SUNSET = ZmanimPreferences.Values.OPINION_16_1_SUNSET;
    protected static final String OPINION_18 = ZmanimPreferences.Values.OPINION_18;
    protected static final String OPINION_19 = ZmanimPreferences.Values.OPINION_19;
    protected static final String OPINION_19_8 = ZmanimPreferences.Values.OPINION_19_8;
    protected static final String OPINION_26 = ZmanimPreferences.Values.OPINION_26;
    protected static final String OPINION_30 = ZmanimPreferences.Values.OPINION_30;
    protected static final String OPINION_58 = ZmanimPreferences.Values.OPINION_58;
    protected static final String OPINION_60 = ZmanimPreferences.Values.OPINION_60;
    protected static final String OPINION_72 = ZmanimPreferences.Values.OPINION_72;
    protected static final String OPINION_72_ZMANIS = ZmanimPreferences.Values.OPINION_72_ZMANIS;
    protected static final String OPINION_90 = ZmanimPreferences.Values.OPINION_90;
    protected static final String OPINION_90_ZMANIS = ZmanimPreferences.Values.OPINION_90_ZMANIS;
    protected static final String OPINION_96 = ZmanimPreferences.Values.OPINION_96;
    protected static final String OPINION_96_ZMANIS = ZmanimPreferences.Values.OPINION_96_ZMANIS;
    protected static final String OPINION_120 = ZmanimPreferences.Values.OPINION_120;
    protected static final String OPINION_120_ZMANIS = ZmanimPreferences.Values.OPINION_120_ZMANIS;
    protected static final String OPINION_168 = ZmanimPreferences.Values.OPINION_168;
    protected static final String OPINION_ATERET = ZmanimPreferences.Values.OPINION_ATERET;
    protected static final String OPINION_BAAL_HATANYA = ZmanimPreferences.Values.OPINION_BAAL_HATANYA;
    protected static final String OPINION_FIXED = ZmanimPreferences.Values.OPINION_FIXED;
    protected static final String OPINION_GRA = ZmanimPreferences.Values.OPINION_GRA;
    protected static final String OPINION_HALF = ZmanimPreferences.Values.OPINION_HALF;
    protected static final String OPINION_LEVEL = ZmanimPreferences.Values.OPINION_LEVEL;
    protected static final String OPINION_MGA = ZmanimPreferences.Values.OPINION_MGA;
    protected static final String OPINION_NIGHT = ZmanimPreferences.Values.OPINION_NIGHT;
    protected static final String OPINION_NONE = ZmanimPreferences.Values.OPINION_NONE;
    protected static final String OPINION_SEA = ZmanimPreferences.Values.OPINION_SEA;
    protected static final String OPINION_TWILIGHT = ZmanimPreferences.Values.OPINION_TWILIGHT;

    /**
     * No summary.
     */
    protected static final int SUMMARY_NONE = ZmanimAdapter.SUMMARY_NONE;

    private final Context context;
    private final ZmanimPreferences settings;
    protected final ComplexZmanimCalendar calendar;
    private final Calendar calendarTemp = Calendar.getInstance();
    private boolean inIsrael;

    /**
     * Creates a new populater.
     *
     * @param context  the context.
     * @param settings the application preferences.
     */
    public ZmanimPopulater(Context context, ZmanimPreferences settings) {
        this.context = context;
        this.settings = settings;
        this.calendar = new ComplexZmanimCalendar();
        calendar.setShaahZmanisType(settings.getHourType());
        calendar.setUseElevation(settings.isUseElevation());
    }

    protected Context getContext() {
        return context;
    }

    protected ZmanimPreferences getSettings() {
        return settings;
    }

    /**
     * Get the calendar.
     *
     * @return the calendar.
     */
    @NonNull
    public ComplexZmanimCalendar getCalendar() {
        return calendar;
    }

    protected void prePopulate(A adapter) {
        adapter.clear();
        ComplexZmanimCalendar cal = (ComplexZmanimCalendar) calendar.clone();
        adapter.setCalendar(cal);
        adapter.setInIsrael(inIsrael);
    }

    /**
     * Populate the list of times.
     *
     * @param adapter the adapter to populate.
     * @param remote  is for remote views?
     */
    public void populate(A adapter, boolean remote) {
        if (adapter == null) {
            Timber.e("adapter required to populate");
            return;
        }
        final Context context = getContext();
        if (context == null) {
            Timber.e("context required to populate");
            return;
        }
        ZmanimPreferences settings = getSettings();

        prePopulate(adapter);
        populateImpl(adapter, remote, context, settings);
    }

    /**
     * Populate the list of times - implementation.
     *
     * @param adapter  the adapter to populate.
     * @param remote   is for remote views?
     * @param context  the context.
     * @param settings the preferences.
     */
    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimPreferences settings) {
        ComplexZmanimCalendar cal = getCalendar();
        Calendar gcal = cal.getCalendar();
        if (gcal.get(Calendar.ERA) < GregorianCalendar.AD) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        ComplexZmanimCalendar calYesterday = cloneZmanimYesterday(cal);
        ComplexZmanimCalendar calTomorrow = cloneZmanimTomorrow(cal);

        JewishCalendar jcal = getJewishCalendar();
        if ((jcal == null) || (jcal.getJewishYear() < 0)) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        final JewishCalendar jewishDate = (JewishCalendar) jcal.clone();
        final int jewishDayOfMonth = jewishDate.getJewishDayOfMonth();
        final int dayOfWeek = jewishDate.getDayOfWeek();
        final JewishCalendar jewishDateTomorrow = cloneJewishTomorrow(jewishDate);
        final int shabbathAfter = settings.getShabbathEndsAfter();
        final int shabbathOffset = settings.getShabbathEnds();
        final int candles = calculateCandles(jewishDate, jewishDateTomorrow, settings);
        final int candlesCount = (candles >> CANDLES_MASK_OFFSET) & CANDLES_MASK;
        final int holidayToday = (byte) ((candles >> HOLIDAY_MASK_OFFSET) & HOLIDAY_MASK);
        final int holidayTomorrow = (byte) ((candles >> HOLIDAY_TOMORROW_MASK_OFFSET) & HOLIDAY_MASK);
        final int candlesOffset = (candles >> OFFSET_MASK_OFFSET) & OFFSET_MASK;
        final int candlesWhen = (candles >> MOTZE_MASK_OFFSET) & MOTZE_MASK;

        adapter.setCandles(candles);

        Long date;
        int summary;
        Pair<Long, Integer> dateAndSummary;
        String opinion;
        CharSequence summaryText;
        final Resources res = context.getResources();
        final String shabbathAfterName = res.getString(shabbathAfter);
        Calendar calDate = Calendar.getInstance(gcal.getTimeZone());

        if (!remote && settings.isHour()) {
            long time;
            opinion = settings.getHour();
            if (OPINION_19_8.equals(opinion)) {
                time = cal.getShaahZmanis19Point8Degrees();
                summary = R.string.hour_19_8;
            } else if (OPINION_120.equals(opinion)) {
                time = cal.getShaahZmanis120Minutes();
                summary = R.string.hour_120;
            } else if (OPINION_120_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis120MinutesZmanis();
                summary = R.string.hour_120_zmanis;
            } else if (OPINION_18.equals(opinion)) {
                time = cal.getShaahZmanis18Degrees();
                summary = R.string.hour_18;
            } else if (OPINION_26.equals(opinion)) {
                time = cal.getShaahZmanis26Degrees();
                summary = R.string.hour_26;
            } else if (OPINION_16_1.equals(opinion)) {
                time = cal.getShaahZmanis16Point1Degrees();
                summary = R.string.hour_16;
            } else if (OPINION_96.equals(opinion)) {
                time = cal.getShaahZmanis96Minutes();
                summary = R.string.hour_96;
            } else if (OPINION_96_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis96MinutesZmanis();
                summary = R.string.hour_96_zmanis;
            } else if (OPINION_90.equals(opinion)) {
                time = cal.getShaahZmanis90Minutes();
                summary = R.string.hour_90;
            } else if (OPINION_90_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis90MinutesZmanis();
                summary = R.string.hour_90_zmanis;
            } else if (OPINION_72.equals(opinion)) {
                time = cal.getShaahZmanis72Minutes();
                summary = R.string.hour_72;
            } else if (OPINION_72_ZMANIS.equals(opinion)) {
                time = cal.getShaahZmanis72MinutesZmanis();
                summary = R.string.hour_72_zmanis;
            } else if (OPINION_60.equals(opinion)) {
                time = cal.getShaahZmanis60Minutes();
                summary = R.string.hour_60;
            } else if (OPINION_ATERET.equals(opinion)) {
                time = cal.getShaahZmanisAteretTorah();
                summary = R.string.hour_ateret;
            } else if (OPINION_MGA.equals(opinion)) {
                time = cal.getShaahZmanisMGA();
                summary = R.string.hour_mga;
            } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
                time = cal.getShaahZmanisBaalHatanya();
                summary = R.string.hour_baal_hatanya;
            } else {
                time = cal.getShaahZmanisGra();
                summary = R.string.hour_gra;
            }
            // Offset is added back when formatted.
            adapter.addHour(R.string.hour, summary, time - gcal.getTimeZone().getRawOffset(), remote);
        }

        opinion = settings.getDawn();
        if (OPINION_19.equals(opinion)) {
            date = cal.getAlos19Degrees();
            summary = R.string.dawn_19;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getAlos19Point8Degrees();
            summary = R.string.dawn_19_8;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getAlos120();
            summary = R.string.dawn_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getAlos120Zmanis();
            summary = R.string.dawn_120_zmanis;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getAlos18Degrees();
            summary = R.string.dawn_18;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getAlos26Degrees();
            summary = R.string.dawn_26;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getAlos16Point1Degrees();
            summary = R.string.dawn_16;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getAlos96();
            summary = R.string.dawn_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getAlos90Zmanis();
            summary = R.string.dawn_96_zmanis;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getAlos90();
            summary = R.string.dawn_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getAlos90Zmanis();
            summary = R.string.dawn_90_zmanis;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getAlos72();
            summary = R.string.dawn_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getAlos72Zmanis();
            summary = R.string.dawn_72_zmanis;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getAlos60();
            summary = R.string.dawn_60;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getAlosBaalHatanya();
            summary = R.string.dawn_baal_hatanya;
        } else {
            date = cal.getAlosHashachar();
            summary = R.string.dawn_16;
        }
        if (date == null) {
            date = cal.getAlos120Zmanis();
            summary = R.string.dawn_120_zmanis;
        }
        adapter.add(R.string.dawn, summary, date, jewishDate, remote);
        final long dawn = toDate(date);

        opinion = settings.getTallis();
        if (OPINION_10_2.equals(opinion)) {
            date = cal.getMisheyakir10Point2Degrees();
            summary = R.string.tallis_10;
        } else if (OPINION_11.equals(opinion)) {
            date = cal.getMisheyakir11Degrees();
            summary = R.string.tallis_11;
        } else if (OPINION_7_65.equals(opinion)) {
            date = cal.getMisheyakir7Point65Degrees();
            summary = R.string.tallis_7_65;
        } else if (OPINION_9_5.equals(opinion)) {
            date = cal.getMisheyakir9Point5Degrees();
            summary = R.string.tallis_9_5;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getMisheyakir10Point2Degrees();
            summary = R.string.tallis_baal_hatanya;
        } else {
            date = cal.getMisheyakir11Point5Degrees();
            summary = R.string.tallis_11_5;
        }
        int tallisTitle = R.string.tallis;
        switch (holidayToday) {
            case SHABBATH:
            case PESACH:
            case CHOL_HAMOED_PESACH:
            case SHAVUOS:
            case ROSH_HASHANA:
            case YOM_KIPPUR:
            case SUCCOS:
            case CHOL_HAMOED_SUCCOS:
            case HOSHANA_RABBA:
            case SHEMINI_ATZERES:
            case SIMCHAS_TORAH:
            case TISHA_BEAV:
                tallisTitle = R.string.tallis_only;
                break;
        }
        adapter.add(tallisTitle, summary, date, jewishDate, remote);

        opinion = settings.getSunrise();
        dateAndSummary = getSunrise(cal, opinion);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        adapter.add(R.string.sunrise, summary, date, jewishDate, remote);
        final long sunrise = date;
        final long sunriseTomorrow = getSunrise(calTomorrow, settings);

        opinion = settings.getLastShema();
        if (OPINION_16_1_SUNSET.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToSunset();
            summary = R.string.shema_16_sunset;
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
            summary = R.string.shema_7;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getSofZmanShmaMGA19Point8Degrees();
            summary = R.string.shema_19_8;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getSofZmanShmaMGA120Minutes();
            summary = R.string.shema_120;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getSofZmanShmaMGA18Degrees();
            summary = R.string.shema_18;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getSofZmanShmaMGA96Minutes();
            summary = R.string.shema_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA96MinutesZmanis();
            summary = R.string.shema_96_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getSofZmanShmaMGA16Point1Degrees();
            summary = R.string.shema_16;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getSofZmanShmaMGA90Minutes();
            summary = R.string.shema_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA90MinutesZmanis();
            summary = R.string.shema_90_zmanis;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getSofZmanShmaMGA72Minutes();
            summary = R.string.shema_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanShmaMGA72MinutesZmanis();
            summary = R.string.shema_72_zmanis;
        } else if (OPINION_MGA.equals(opinion)) {
            date = cal.getSofZmanShmaMGA();
            summary = R.string.shema_mga;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getSofZmanShmaAteretTorah();
            summary = R.string.shema_ateret;
        } else if (OPINION_3.equals(opinion)) {
            date = cal.getSofZmanShma3HoursBeforeChatzos();
            summary = R.string.shema_3;
        } else if (OPINION_FIXED.equals(opinion)) {
            date = cal.getSofZmanShmaFixedLocal();
            summary = R.string.shema_fixed;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getSofZmanShmaBaalHatanya();
            summary = R.string.shema_baal_hatanya;
        } else {
            date = cal.getSofZmanShmaGRA();
            summary = R.string.shema_gra;
        }
        adapter.add(R.string.shema, summary, date, jewishDate, remote);

        opinion = settings.getLastTfila();
        if (OPINION_120.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA120Minutes();
            summary = R.string.prayers_120;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA96Minutes();
            summary = R.string.prayers_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA96MinutesZmanis();
            summary = R.string.prayers_96_zmanis;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA19Point8Degrees();
            summary = R.string.prayers_19_8;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA90Minutes();
            summary = R.string.prayers_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA90MinutesZmanis();
            summary = R.string.prayers_90_zmanis;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getSofZmanTfilahAteretTorah();
            summary = R.string.prayers_ateret;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA18Degrees();
            summary = R.string.prayers_18;
        } else if (OPINION_FIXED.equals(opinion)) {
            date = cal.getSofZmanTfilaFixedLocal();
            summary = R.string.prayers_fixed;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA16Point1Degrees();
            summary = R.string.prayers_16;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA72Minutes();
            summary = R.string.prayers_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA72MinutesZmanis();
            summary = R.string.prayers_72_zmanis;
        } else if (OPINION_2.equals(opinion)) {
            date = cal.getSofZmanTfila2HoursBeforeChatzos();
            summary = R.string.prayers_2;
        } else if (OPINION_MGA.equals(opinion)) {
            date = cal.getSofZmanTfilaMGA();
            summary = R.string.prayers_mga;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getSofZmanTfilaBaalHatanya();
            summary = R.string.prayers_baal_hatanya;
        } else {
            date = cal.getSofZmanTfilaGRA();
            summary = R.string.prayers_gra;
        }
        adapter.add(R.string.prayers, summary, date, jewishDate, remote);

        opinion = settings.getMidday();
        dateAndSummary = getMidday(cal, opinion);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        adapter.add(R.string.midday, summary, date, jewishDate, remote);
        final long midday = date;
        final long middayYesterday = getMidday(calYesterday, opinion).first;

        opinion = settings.getEarliestMincha();
        if (OPINION_16_1.equals(opinion)) {
            date = cal.getMinchaGedola16Point1Degrees();
            summary = R.string.earliest_mincha_16;
        } else if (OPINION_30.equals(opinion)) {
            date = cal.getMinchaGedola30Minutes();
            summary = R.string.earliest_mincha_30;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getMinchaGedolaAteretTorah();
            summary = R.string.earliest_mincha_ateret;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getMinchaGedola72Minutes();
            summary = R.string.earliest_mincha_72;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getMinchaGedolaBaalHatanyaGreaterThan30();
            summary = R.string.earliest_mincha_baal_hatanya;
        } else {
            date = cal.getMinchaGedola();
            summary = R.string.earliest_mincha_greater;
        }
        adapter.add(R.string.earliest_mincha, summary, date, jewishDate, remote);

        opinion = settings.getMincha();
        if (OPINION_16_1.equals(opinion)) {
            date = cal.getMinchaKetana16Point1Degrees();
            summary = R.string.mincha_16;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getMinchaKetana72Minutes();
            summary = R.string.mincha_72;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getMinchaKetanaAteretTorah();
            summary = R.string.mincha_ateret;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getMinchaKetanaBaalHatanya();
            summary = R.string.mincha_baal_hatanya;
        } else {
            date = cal.getMinchaKetana();
            summary = R.string.mincha_lesser;
        }
        adapter.add(R.string.mincha, summary, date, jewishDate, remote);

        opinion = settings.getPlugHamincha();
        if (OPINION_16_1_SUNSET.equals(opinion)) {
            date = cal.getPlagAlosToSunset();
            summary = R.string.plug_hamincha_16_sunset;
        } else if (OPINION_16_1_ALOS.equals(opinion)) {
            date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
            summary = R.string.plug_hamincha_16_alos;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getPlagHaminchaAteretTorah();
            summary = R.string.plug_hamincha_ateret;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getPlagHamincha60Minutes();
            summary = R.string.plug_hamincha_60;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getPlagHamincha72Minutes();
            summary = R.string.plug_hamincha_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha72MinutesZmanis();
            summary = R.string.plug_hamincha_72_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getPlagHamincha16Point1Degrees();
            summary = R.string.plug_hamincha_16;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getPlagHamincha18Degrees();
            summary = R.string.plug_hamincha_18;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getPlagHamincha90Minutes();
            summary = R.string.plug_hamincha_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha90MinutesZmanis();
            summary = R.string.plug_hamincha_90_zmanis;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getPlagHamincha19Point8Degrees();
            summary = R.string.plug_hamincha_19_8;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getPlagHamincha96Minutes();
            summary = R.string.plug_hamincha_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha96MinutesZmanis();
            summary = R.string.plug_hamincha_96_zmanis;
        } else if (OPINION_120.equals(opinion)) {
            date = cal.getPlagHamincha120Minutes();
            summary = R.string.plug_hamincha_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getPlagHamincha120MinutesZmanis();
            summary = R.string.plug_hamincha_120_zmanis;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getPlagHamincha26Degrees();
            summary = R.string.plug_hamincha_26;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getPlagHaminchaBaalHatanya();
            summary = R.string.plug_hamincha_baal_hatanya;
        } else {
            date = cal.getPlagHamincha();
            summary = R.string.plug_hamincha_gra;
        }
        adapter.add(R.string.plug_hamincha, summary, date, jewishDate, remote);

        opinion = settings.getSunset();
        dateAndSummary = getSunset(cal, opinion);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        adapter.add(R.string.sunset, summary, date, jewishDate, remote);
        final long sunset = date;
        final int summarySunset = summary;

        opinion = settings.getTwilight();
        dateAndSummary = getTwilight(cal, opinion);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        adapter.add(R.string.twilight, summary, date, jewishDateTomorrow, remote);
        final long twilight = date;
        final int summaryTwilight = summary;

        opinion = settings.getNightfall();
        dateAndSummary = getNightfall(cal, opinion);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        adapter.add(R.string.nightfall, summary, date, jewishDateTomorrow, remote);
        final long nightfall = date;
        final int summaryNightfall = summary;
        final long nightfallYesterday = getNightfall(calYesterday, opinion).first;

        switch (shabbathAfter) {
            case R.string.sunset:
                opinion = settings.getShabbathEndsSunset();
                if (OPINION_NONE.equals(opinion)) {
                    date = sunset;
                } else {
                    date = getSunset(cal, opinion).first;
                }
                break;
            case R.string.twilight:
                opinion = settings.getShabbathEndsTwilight();
                if (OPINION_NONE.equals(opinion)) {
                    date = twilight;
                } else {
                    date = getTwilight(cal, opinion).first;
                }
                break;
            case R.string.nightfall:
            default:
                opinion = settings.getShabbathEndsNightfall();
                if (OPINION_NONE.equals(opinion)) {
                    date = nightfall;
                } else {
                    date = getNightfall(cal, opinion).first;
                }
                break;
        }
        long shabbatEnds = NEVER;
        CharSequence summaryShabbatEnds = null;
        if (date != NEVER) {
            if (dayOfWeek == SATURDAY) {
                shabbatEnds = cal.getTimeOffset(date, shabbathOffset * MINUTE_IN_MILLIS);
                summaryText = res.getQuantityString(R.plurals.shabbath_ends_summary, shabbathOffset, shabbathOffset, shabbathAfterName);
                adapter.add(R.string.shabbath_ends, summaryText, shabbatEnds, jewishDateTomorrow, remote);
                summaryShabbatEnds = summaryText;
            } else if (holidayToday >= 0) {
                switch (holidayToday) {
                    case PESACH:
                    case SHAVUOS:
                    case ROSH_HASHANA:
                    case YOM_KIPPUR:
                    case SUCCOS:
                    case SHEMINI_ATZERES:
                    case SIMCHAS_TORAH:
                        shabbatEnds = cal.getTimeOffset(date, shabbathOffset * MINUTE_IN_MILLIS);
                        summaryText = res.getQuantityString(R.plurals.shabbath_ends_summary, shabbathOffset, shabbathOffset, shabbathAfterName);
                        adapter.add(R.string.festival_ends, summaryText, shabbatEnds, jewishDateTomorrow, remote);
                        summaryShabbatEnds = summaryText;
                        break;
                }
            }
        }

        if (candlesCount > 0) {
            switch (candlesWhen) {
                case BEFORE_SUNSET:
                    if (sunset != NEVER) {
                        date = sunset - (candlesOffset * MINUTE_IN_MILLIS);
                        if (holidayTomorrow == CHANUKAH) {
                            summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                        } else {
                            summaryText = res.getQuantityString(R.plurals.candles_summary, candlesOffset, candlesOffset);
                        }
                        adapter.add(R.string.candles, summaryText, date, jewishDate, remote);
                    }
                    break;
                case AT_SUNSET:
                    if (sunset != NEVER) {
                        if (holidayTomorrow == CHANUKAH) {
                            summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                            adapter.add(R.string.candles, summaryText, sunset, jewishDate, remote);
                        } else {
                            adapter.add(R.string.candles, summarySunset, sunset, jewishDate, remote);
                        }
                    }
                    break;
                case AT_TWILIGHT:
                    if (twilight != NEVER) {
                        if (holidayTomorrow == CHANUKAH) {
                            summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                            adapter.add(R.string.candles, summaryText, twilight, jewishDateTomorrow, remote);
                        } else {
                            adapter.add(R.string.candles, summaryTwilight, twilight, jewishDateTomorrow, remote);
                        }
                    }
                    break;
                case AT_NIGHT:
                    if (nightfall != NEVER) {
                        if (holidayTomorrow == CHANUKAH) {
                            summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                            adapter.add(R.string.candles, summaryText, nightfall, jewishDateTomorrow, remote);
                        } else {
                            adapter.add(R.string.candles, summaryNightfall, nightfall, jewishDateTomorrow, remote);
                        }
                    }
                    break;
                case MOTZE_SHABBATH:
                    if (shabbatEnds != NEVER) {
                        if (holidayTomorrow == CHANUKAH) {
                            summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                            adapter.add(R.string.candles, summaryText, shabbatEnds, jewishDateTomorrow, remote);
                        } else {
                            adapter.add(R.string.candles, summaryShabbatEnds, shabbatEnds, jewishDateTomorrow, remote);
                        }
                    }
                    break;
            }
        }

        opinion = settings.getMidnight();
        dateAndSummary = getMidnight(cal, opinion, midday, nightfall);
        date = dateAndSummary.first;
        summary = dateAndSummary.second;
        final long midnightTomorrow = date;
        dateAndSummary = getMidnight(calYesterday, opinion, middayYesterday, nightfallYesterday);
        date = dateAndSummary.first;
        final long midnight = date;
        if (date < sunset) {
            calDate.setTimeInMillis(date);
            if (isSameDay(gcal, calDate)) {
                adapter.add(R.string.midnight, summary, date, jewishDate, remote);
            } else {
                date = midnightTomorrow;
                adapter.add(R.string.midnight, summary, date, jewishDateTomorrow, remote);
            }
        } else {
            date = midnightTomorrow;
            adapter.add(R.string.midnight, summary, date, jewishDateTomorrow, remote);
        }

        opinion = settings.getGuardsCount();
        if (OPINION_4.equals(opinion)) {
            date = getMidnightGuard4(sunset, midnightTomorrow);
            summary = R.string.guard_second;
        } else {
            date = getMidnightGuard3(sunset, sunriseTomorrow);
            summary = R.string.guard_second;
        }
        adapter.add(R.string.midnight_guard, summary, date, jewishDateTomorrow, remote);

        opinion = settings.getGuardsCount();
        if (OPINION_4.equals(opinion)) {
            summary = R.string.guard_fourth;
            if (midnight < sunrise) {
                date = getMorningGuard4(midnight, sunrise);
                adapter.add(R.string.morning_guard, summary, date, jewishDate, remote);
            } else {
                date = getMorningGuard4(midnightTomorrow, sunriseTomorrow);
                adapter.add(R.string.morning_guard, summary, date, jewishDateTomorrow, remote);
            }
        } else {
            summary = R.string.guard_third;
            date = getMorningGuard3(sunset, sunriseTomorrow);
            adapter.add(R.string.morning_guard, summary, date, jewishDateTomorrow, remote);
        }

        switch (holidayToday) {
            case EREV_PESACH:
                opinion = settings.getEatChametz();
                if (OPINION_16_1.equals(opinion)) {
                    date = cal.getSofZmanAchilasChametzMGA16Point1Degrees();
                    summary = R.string.eat_chametz_16;
                } else if (OPINION_72.equals(opinion)) {
                    date = cal.getSofZmanAchilasChametzMGA72Minutes();
                    summary = R.string.eat_chametz_72;
                } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
                    date = cal.getSofZmanAchilasChametzBaalHatanya();
                    summary = R.string.eat_chametz_baal_hatanya;
                } else {
                    date = cal.getSofZmanAchilasChametzGRA();
                    summary = R.string.eat_chametz_gra;
                }
                adapter.add(R.string.eat_chametz, summary, date, jewishDate, remote);

                opinion = settings.getBurnChametz();
                if (OPINION_16_1.equals(opinion)) {
                    date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
                    summary = R.string.burn_chametz_16;
                } else if (OPINION_72.equals(opinion)) {
                    date = cal.getSofZmanBiurChametzMGA72Minutes();
                    summary = R.string.burn_chametz_72;
                } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
                    date = cal.getSofZmanBiurChametzBaalHatanya();
                    summary = R.string.burn_chametz_baal_hatanya;
                } else {
                    date = cal.getSofZmanBiurChametzGRA();
                    summary = R.string.burn_chametz_gra;
                }
                adapter.add(R.string.burn_chametz, summary, date, jewishDate, remote);
                break;
            case SEVENTEEN_OF_TAMMUZ:
            case FAST_OF_GEDALYAH:
            case TENTH_OF_TEVES:
            case FAST_OF_ESTHER:
                adapter.add(R.string.fast_begins, SUMMARY_NONE, dawn, jewishDate, remote);
                adapter.add(R.string.fast_ends, SUMMARY_NONE, nightfall, jewishDateTomorrow, remote);
                break;
            case TISHA_BEAV:
                adapter.add(R.string.fast_ends, SUMMARY_NONE, nightfall, jewishDateTomorrow, remote);
                break;
            case YOM_KIPPUR:
                adapter.add(R.string.fast_ends, SUMMARY_NONE, shabbatEnds, jewishDateTomorrow, remote);
                break;
        }
        switch (holidayTomorrow) {
            case TISHA_BEAV:
                adapter.add(R.string.fast_begins, SUMMARY_NONE, sunset, jewishDate, remote);
                break;
            case YOM_KIPPUR:
                adapter.add(R.string.fast_begins, SUMMARY_NONE, cal.getTimeOffset(sunset, -candlesOffset * MINUTE_IN_MILLIS), jewishDate, remote);
                break;
        }

        // Molad.
        if ((jewishDayOfMonth <= 1) || (jewishDayOfMonth >= 25)) {
            int y = gcal.get(Calendar.YEAR);
            int m = gcal.get(Calendar.MONTH);
            int d = gcal.get(Calendar.DAY_OF_MONTH);

            // Molad is always of the previous month.
            int jLastDayOfMonth = jcal.getDaysInJewishMonth();
            if ((jewishDayOfMonth > 1) && (jewishDayOfMonth < jLastDayOfMonth)) {
                jcal.setJewishDate(jcal.getJewishYear(), jcal.getJewishMonth(), jLastDayOfMonth);
            }
            JewishDate molad = jcal.getMolad();
            int moladYear = molad.getGregorianYear();
            int moladMonth = molad.getGregorianMonth();
            int moladDay = molad.getGregorianDayOfMonth();
            if ((moladYear != y) || (moladMonth != m) || (moladDay != d)) {
                jcal.forward(Calendar.DATE, 1);

                molad = jcal.getMolad();
                moladYear = molad.getGregorianYear();
                moladMonth = molad.getGregorianMonth();
                moladDay = molad.getGregorianDayOfMonth();
            }

            if ((moladYear == y) && (moladMonth == m) && (moladDay == d)) {
                double moladSeconds = (molad.getMoladChalakim() * 10.0) / 3.0;
                double moladSecondsFloor = Math.floor(moladSeconds);
                Calendar calMolad = (Calendar) gcal.clone();
                calMolad.set(moladYear, moladMonth, moladDay, molad.getMoladHours(), molad.getMoladMinutes(), (int) moladSecondsFloor);
                calMolad.set(Calendar.MILLISECOND, (int) (SECOND_IN_MILLIS * (moladSeconds - moladSecondsFloor)));
                summary = R.string.molad_average;
                final long moladTime = calMolad.getTimeInMillis();
                JewishDate moonDate = jewishDate;
                if ((sunset != NEVER) && (sunset < moladTime)) {
                    moonDate = jewishDateTomorrow;
                }
                adapter.add(R.string.molad, summary, moladTime, moonDate, remote);
            }
        }
        // First Kiddush Levana.
        else if ((jewishDayOfMonth >= 2) && (jewishDayOfMonth <= 8)) {
            opinion = settings.getEarliestKiddushLevana();
            if (OPINION_7.equals(opinion) || OPINION_168.equals(opinion)) {
                date = cal.getTchilasZmanKidushLevana7Days();
                summary = R.string.levana_7;
            } else {
                date = cal.getTchilasZmanKidushLevana3Days();
                summary = R.string.levana_3;
            }
            if ((date != null) && isSameDay(gcal, date)) {
                JewishDate moonDate = jewishDate;
                if ((sunset != NEVER) && (sunset < date)) {
                    moonDate = jewishDateTomorrow;
                }
                adapter.add(R.string.levana_earliest, summary, date, moonDate, remote);
            }
        }
        // Last Kiddush Levana.
        else if ((jewishDayOfMonth > 10) && (jewishDayOfMonth < 20)) {
            opinion = settings.getLatestKiddushLevana();
            if (OPINION_15.equals(opinion) || OPINION_15_ALOS.equals(opinion)) {
                date = cal.getSofZmanKidushLevana15Days();
                summary = R.string.levana_15;
            } else {
                date = cal.getSofZmanKidushLevanaBetweenMoldos();
                summary = R.string.levana_halfway;
            }
            if ((date != null) && isSameDay(gcal, date)) {
                JewishDate moonDate = jewishDate;
                if ((sunset != NEVER) && (sunset < date)) {
                    moonDate = jewishDateTomorrow;
                }
                adapter.add(R.string.levana_latest, summary, date, moonDate, remote);
            }
        }

        adapter.sort();
    }

    /**
     * Calculate candle parameters, e.g. the number of candles to light.
     * <p>
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
     * </p>
     *
     * @param jewishDateToday    the Jewish calendar for today.
     * @param jewishDateTomorrow the Jewish calendar for tomorrow.
     * @return the number of candles to light, the holiday, and when to light.
     */
    protected int calculateCandles(@Nullable JewishCalendar jewishDateToday, @Nullable JewishCalendar jewishDateTomorrow, ZmanimPreferences settings) {
        if (jewishDateToday == null) {
            return 0;
        }
        final int dayOfWeek = jewishDateToday.getDayOfWeek();

        // Check if the following day is special, because we can't check EREV_CHANUKAH.
        int holidayToday = jewishDateToday.getYomTovIndex();
        JewishCalendar jcalTomorrow = jewishDateTomorrow;
        if (jcalTomorrow == null) {
            jcalTomorrow = cloneJewishTomorrow(jewishDateToday);
        }
        int holidayTomorrow = jcalTomorrow.getYomTovIndex();
        int count = CANDLES_NONE;
        int when = BEFORE_SUNSET;

        switch (holidayTomorrow) {
            case PESACH:
            case SHAVUOS:
            case ROSH_HASHANA:
            case SUCCOS:
            case SHEMINI_ATZERES:
            case SIMCHAS_TORAH:
                count = CANDLES_FESTIVAL;
                break;
            case YOM_KIPPUR:
                count = CANDLES_YOM_KIPPUR;
                break;
            case CHANUKAH:
                count = jcalTomorrow.getDayOfChanukah();
                if ((dayOfWeek != FRIDAY) && (dayOfWeek != SATURDAY)) {
                    String opinion = settings.getChanukkaCandles();
                    if (OPINION_TWILIGHT.equals(opinion)) {
                        when = AT_TWILIGHT;
                    } else if (OPINION_NIGHT.equals(opinion)) {
                        when = AT_NIGHT;
                    } else {
                        when = AT_SUNSET;
                    }
                }
                break;
            default:
                if (dayOfWeek == FRIDAY) {
                    holidayTomorrow = SHABBATH;
                    count = CANDLES_SHABBATH;
                }
                break;
        }

        switch (dayOfWeek) {
            // Forbidden to light candles during Shabbath.
            // Never happens that Yom Kippurim falls on a Friday.
            case FRIDAY:
                when = BEFORE_SUNSET;
                break;
            // Forbidden to light candles during Shabbath.
            case SATURDAY:
                if (holidayToday == -1) {
                    holidayToday = SHABBATH;
                }
                when = MOTZE_SHABBATH;
                break;
            default:
                // During a holiday, we can light for the next day from an existing flame,
                // but preferable to light havdala candle after the Yom Tov.
                switch (holidayToday) {
                    case PESACH:
                    case SHAVUOS:
                    case ROSH_HASHANA:
                    case SUCCOS:
                    case SHEMINI_ATZERES:
                    case SIMCHAS_TORAH:
                        when = MOTZE_SHABBATH;
                        break;
                }
                break;
        }

        final int candlesOffset = settings.getCandleLightingOffset();

        return ((when & MOTZE_MASK) << MOTZE_MASK_OFFSET)
                | ((candlesOffset & OFFSET_MASK) << OFFSET_MASK_OFFSET)
                | ((holidayTomorrow & HOLIDAY_MASK) << HOLIDAY_TOMORROW_MASK_OFFSET)
                | ((holidayToday & HOLIDAY_MASK) << HOLIDAY_MASK_OFFSET)
                | ((count & CANDLES_MASK) << CANDLES_MASK_OFFSET);
    }

    /**
     * Set the calendar.
     *
     * @param calendar the calendar.
     */
    public void setCalendar(@NonNull Calendar calendar) {
        this.calendar.setCalendar(calendar);
    }

    /**
     * Set the calendar time.
     *
     * @param time the time in milliseconds.
     */
    public void setCalendar(long time) {
        Calendar cal = getCalendar().getCalendar();
        cal.setTimeInMillis(time);
    }

    /**
     * Sets the {@link GeoLocation}.
     *
     * @param geoLocation the location.
     */
    public void setGeoLocation(@NonNull GeoLocation geoLocation) {
        this.calendar.setGeoLocation(geoLocation);
    }

    /**
     * Sets whether to use Israel holiday scheme or not.
     *
     * @param inIsrael set to {@code true} for calculations for Israel.
     */
    public void setInIsrael(boolean inIsrael) {
        this.inIsrael = inIsrael;
    }

    /**
     * Is the current location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean isInIsrael() {
        return inIsrael;
    }

    /**
     * Get the Jewish calendar.
     *
     * @return the calendar - {@code null} if date is invalid.
     */
    @Nullable
    public JewishCalendar getJewishCalendar() {
        Calendar gcal = getCalendar().getCalendar();
        if (gcal.get(Calendar.ERA) < GregorianCalendar.AD) {
            // Avoid future "IllegalArgumentException".
            return null;
        }
        JewishCalendar jcal = new JewishCalendar(gcal);
        jcal.setInIsrael(inIsrael);
        return jcal;
    }

    /**
     * Get the Jewish date.
     *
     * @param date the civil date and time.
     * @return the date - {@code null} if time is invalid.
     */
    protected JewishDate getJewishDate(long date) {
        return getJewishDate(date, getCalendar(), getSettings());
    }

    /**
     * Get the Jewish date.
     *
     * @param date the civil date and time.
     * @return the date - {@code null} if time is invalid.
     */
    protected JewishDate getJewishDate(long date, ComplexZmanimCalendar calendar, ZmanimPreferences settings) {
        return getJewishDate(date, getSunset(calendar, settings));
    }

    /**
     * Get the Jewish date.
     *
     * @param date   the civil date and time.
     * @param sunset the sunset time.
     * @return the date - {@code null} if time is invalid.
     */
    protected JewishDate getJewishDate(long date, long sunset) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        JewishDate jewishDate = new JewishDate(cal);
        if (date > sunset) {
            jewishDate.forward(Calendar.DATE, 1);
        }
        return jewishDate;
    }

    protected Pair<Long, Integer> getSunrise(ComplexZmanimCalendar cal, String opinion) {
        Long date;
        int summary;

        if (OPINION_SEA.equals(opinion)) {
            date = cal.getSeaLevelSunrise();
            summary = R.string.sunrise_sea;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getSeaLevelSunrise();
            summary = R.string.sunrise_baal_hatanya;
        } else {
            date = cal.getSunrise();
            summary = R.string.sunrise_elevated;
        }

        return Pair.create(toDate(date), summary);
    }

    protected long getSunrise(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        String opinion = settings.getSunrise();
        return getSunrise(cal, opinion).first;
    }

    protected long getSunriseTomorrow(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        final ComplexZmanimCalendar calTomorrow = (ComplexZmanimCalendar) cal.clone();
        calTomorrow.getCalendar().add(Calendar.DATE, 1);
        return getSunrise(calTomorrow, settings);
    }

    protected Pair<Long, Integer> getMidday(ComplexZmanimCalendar cal, String opinion) {
        Long date;
        int summary;

        if (OPINION_FIXED.equals(opinion)) {
            date = cal.getFixedLocalChatzos();
            summary = R.string.midday_fixed;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getChatzosBaalHatanya();
            summary = R.string.midday_baal_hatanya;
        } else {
            date = cal.getChatzos();
            summary = R.string.midday_solar;
        }

        return Pair.create(toDate(date), summary);
    }

    protected long getMidday(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        String opinion = settings.getMidday();
        return getMidday(cal, opinion).first;
    }

    protected Pair<Long, Integer> getSunset(ComplexZmanimCalendar cal, String opinion) {
        Long date;
        int summary;

        if (OPINION_SEA.equals(opinion)) {
            date = cal.getSeaLevelSunset();
            summary = R.string.sunset_sea;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getSeaLevelSunset();
            summary = R.string.sunset_baal_hatanya;
        } else {
            date = cal.getSunset();
            summary = R.string.sunset_elevated;
        }

        return Pair.create(toDate(date), summary);
    }

    protected long getSunset(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        String opinion = settings.getSunset();
        return getSunset(cal, opinion).first;
    }

    protected Pair<Long, Integer> getTwilight(ComplexZmanimCalendar cal, String opinion) {
        Long date;
        int summary;

        if (OPINION_7_083.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
            summary = R.string.twilight_7_083;
        } else if (OPINION_7_083_ZMANIS.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point5MinutesZmanisBefore7Point083Degrees();
            summary = R.string.twilight_7_083_zmanis;
        } else if (OPINION_58.equals(opinion)) {
            date = cal.getBainHasmashosRT58Point5Minutes();
            summary = R.string.twilight_58;
        } else if (OPINION_13.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point24Degrees();
            summary = R.string.twilight_13;
        } else {
            date = cal.getBainHasmashosRT2Stars();
            summary = R.string.twilight_2stars;
        }

        return Pair.create(toDate(date), summary);
    }

    protected Pair<Long, Integer> getNightfall(ComplexZmanimCalendar cal, String opinion) {
        Long date;
        int summary;

        if (OPINION_120.equals(opinion)) {
            date = cal.getTzais120();
            summary = R.string.nightfall_120;
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getTzais120Zmanis();
            summary = R.string.nightfall_120_zmanis;
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getTzais16Point1Degrees();
            summary = R.string.nightfall_16;
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getTzais18Degrees();
            summary = R.string.nightfall_18;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getTzais19Point8Degrees();
            summary = R.string.nightfall_19_8;
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getTzais26Degrees();
            summary = R.string.nightfall_26;
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getTzais60();
            summary = R.string.nightfall_60;
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getTzais72();
            summary = R.string.nightfall_72;
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getTzais72Zmanis();
            summary = R.string.nightfall_72_zmanis;
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getTzais90();
            summary = R.string.nightfall_90;
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getTzais90Zmanis();
            summary = R.string.nightfall_90_zmanis;
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getTzais96();
            summary = R.string.nightfall_96;
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getTzais96Zmanis();
            summary = R.string.nightfall_96_zmanis;
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getTzaisAteretTorah();
            summary = R.string.nightfall_ateret;
        } else if (OPINION_3_65.equals(opinion)) {
            date = cal.getTzaisGeonim3Point65Degrees();
            summary = R.string.nightfall_3_65;
        } else if (OPINION_3_676.equals(opinion)) {
            date = cal.getTzaisGeonim3Point676Degrees();
            summary = R.string.nightfall_3_676;
        } else if (OPINION_3_7.equals(opinion)) {
            date = cal.getTzaisGeonim3Point7Degrees();
            summary = R.string.nightfall_3_7;
        } else if (OPINION_3_8.equals(opinion)) {
            date = cal.getTzaisGeonim3Point8Degrees();
            summary = R.string.nightfall_3_8;
        } else if (OPINION_4_37.equals(opinion)) {
            date = cal.getTzaisGeonim4Point37Degrees();
            summary = R.string.nightfall_4_37;
        } else if (OPINION_4_61.equals(opinion)) {
            date = cal.getTzaisGeonim4Point61Degrees();
            summary = R.string.nightfall_4_61;
        } else if (OPINION_4_8.equals(opinion)) {
            date = cal.getTzaisGeonim4Point8Degrees();
            summary = R.string.nightfall_4_8;
        } else if (OPINION_5_88.equals(opinion)) {
            date = cal.getTzaisGeonim5Point88Degrees();
            summary = R.string.nightfall_5_88;
        } else if (OPINION_5_95.equals(opinion)) {
            date = cal.getTzaisGeonim5Point95Degrees();
            summary = R.string.nightfall_5_95;
        } else if (OPINION_6_45.equals(opinion)) {
            date = cal.getTzaisGeonim6Point45Degrees();
            summary = R.string.nightfall_6_45;
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getTzaisGeonim7Point083Degrees();
            summary = R.string.nightfall_7;
        } else if (OPINION_7_67.equals(opinion)) {
            date = cal.getTzaisGeonim7Point67Degrees();
            summary = R.string.nightfall_7_67;
        } else if (OPINION_8_5.equals(opinion)) {
            date = cal.getTzaisGeonim8Point5Degrees();
            summary = R.string.nightfall_8;
        } else if (OPINION_9_3.equals(opinion)) {
            date = cal.getTzaisGeonim9Point3Degrees();
            summary = R.string.nightfall_9_3;
        } else if (OPINION_9_75.equals(opinion)) {
            date = cal.getTzaisGeonim9Point75Degrees();
            summary = R.string.nightfall_9_75;
        } else if (OPINION_BAAL_HATANYA.equals(opinion)) {
            date = cal.getTzaisBaalHatanya();
            summary = R.string.nightfall_baal_hatanya;
        } else {
            date = cal.getTzaisGeonim8Point5Degrees();
            summary = R.string.nightfall_8;
        }

        return Pair.create(toDate(date), summary);
    }

    protected long getNightfall(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        String opinion = settings.getNightfall();
        return getNightfall(cal, opinion).first;
    }

    protected Pair<Long, Integer> getMidnight(ComplexZmanimCalendar cal, String opinion, long midday, long nightfall) {
        Long date;
        int summary;

        if (OPINION_12.equals(opinion)) {
            date = midday;
            if (midday != NEVER) {
                date += TWELVE_HOURS;
            }
            summary = R.string.midnight_12;
        } else if (OPINION_6.equals(opinion)) {
            date = nightfall;
            if (nightfall != NEVER) {
                date += SIX_HOURS;
            }
            summary = R.string.midnight_6;
        } else {
            date = cal.getSolarMidnight();
            summary = R.string.midnight_solar;
        }

        return Pair.create(toDate(date), summary);
    }

    protected long getMidnight(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        String opinion = settings.getMidnight();
        long midday = getMidday(cal, settings);
        long nightfall = getNightfall(cal, settings);
        return getMidnight(cal, opinion, midday, nightfall).first;
    }

    /**
     * A method that returns "the midnight guard" (ashmurat hatichona).
     * <p/>
     * Nocturnal guard is from sunset until 22:00.<br/>
     * Midnight guard is from 22:00 until 02:00.<br/>
     * Morning guard is from 02:00 until sunrise.
     *
     * @return the Second Guard.
     */
    protected long getMidnightGuard(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        long sunset = getSunset(cal, settings);

        String opinion = settings.getGuardsCount();
        if (OPINION_4.equals(opinion)) {
            return getMidnightGuard4(sunset, getMidnight(cal, settings));
        }
        return getMidnightGuard3(sunset, getSunriseTomorrow(cal, settings));
    }

    protected long getMidnightGuard3(long sunset, long sunrise) {
        if ((sunset == NEVER) || (sunrise == NEVER)) {
            return NEVER;
        }
        long night = sunrise - sunset;
        return sunset + (night / 3L);
    }

    protected long getMidnightGuard4(long sunset, long midnight) {
        if ((sunset == NEVER) || (midnight == NEVER)) {
            return NEVER;
        }
        return sunset + ((midnight - sunset) >> 1);
    }

    /**
     * A method that returns "the morning guard" (ashmurat haboker).
     * <p/>
     * Nocturnal guard is from sunset until 22:00.<br/>
     * Midnight guard is from 22:00 until 02:00.<br/>
     * Morning guard is from 02:00 until sunrise.
     *
     * @return the Third Guard.
     */
    protected long getMorningGuard(ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        long sunrise = getSunriseTomorrow(cal, settings);

        String opinion = settings.getGuardsCount();
        if (OPINION_4.equals(opinion)) {
            return getMorningGuard4(getMidnight(cal, settings), sunrise);
        }
        return getMorningGuard3(getSunset(cal, settings), sunrise);
    }

    protected long getMorningGuard3(long sunset, long sunrise) {
        if ((sunset == NEVER) || (sunrise == NEVER)) {
            return NEVER;
        }
        long night = sunrise - sunset;
        return sunset + ((night << 1) / 3L);//sunset + (night * 2 / 3)
    }

    protected long getMorningGuard4(long midnight, long sunrise) {
        if ((midnight == NEVER) || (sunrise == NEVER)) {
            return NEVER;
        }
        return midnight + ((sunrise - midnight) >> 1);
    }

    protected Long getSofZmanBiurChametz(ComplexZmanimCalendar cal, long startOfDay, long shaahZmanis) {
        return cal.getTimeOffset(startOfDay, shaahZmanis * 5);
    }

    protected long toDate(Long date) {
        return (date != null) ? date : NEVER;
    }

    protected boolean isDate(Long date) {
        return (date != null) && (date != NEVER);
    }

    protected JewishCalendar cloneJewishTomorrow(JewishCalendar jcal) {
        JewishCalendar jcalTomorrow = (JewishCalendar) jcal.clone();
        jcalTomorrow.forward(Calendar.DATE, 1);
        return jcalTomorrow;
    }

    protected ComplexZmanimCalendar cloneZmanimTomorrow(ComplexZmanimCalendar cal) {
        ComplexZmanimCalendar calTomorrow = (ComplexZmanimCalendar) cal.clone();
        calTomorrow.getCalendar().add(Calendar.DAY_OF_MONTH, 1);
        return calTomorrow;
    }

    protected ComplexZmanimCalendar cloneZmanimYesterday(ComplexZmanimCalendar cal) {
        ComplexZmanimCalendar calYesterday = (ComplexZmanimCalendar) cal.clone();
        calYesterday.getCalendar().add(Calendar.DAY_OF_MONTH, -1);
        return calYesterday;
    }
}

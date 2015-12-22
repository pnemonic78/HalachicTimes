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
package net.sf.times;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;

import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;
import java.util.Date;

/**
 * Populate a list of zmanim.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPopulater<A extends ZmanimAdapter> {

    /** 12 hours (half of a full day). */
    protected static final long TWELVE_HOURS = DateUtils.DAY_IN_MILLIS >> 1;
    /** 6 hours (quarter of a full day). */
    protected static final long SIX_HOURS = DateUtils.DAY_IN_MILLIS >> 2;

    /** Holiday id for Shabbath. */
    public static final int SHABBATH = 100;

    /** No candles to light. */
    private static final int CANDLES_NONE = 0;
    /** Number of candles to light for Shabbath. */
    private static final int CANDLES_SHABBATH = 2;
    /** Number of candles to light for a festival. */
    private static final int CANDLES_FESTIVAL = 2;
    /** Number of candles to light for Yom Kippurim. */
    private static final int CANDLES_YOM_KIPPUR = 1;

    /** Flag indicating lighting times before sunset. */
    private static final int BEFORE_SUNSET = 0x00000000;
    /** Flag indicating lighting times at sunset. */
    private static final int AT_SUNSET = 0x10000000;
    /** Flag indicating lighting times at twilight. */
    private static final int AT_TWILIGHT = 0x20000000;
    /** Flag indicating lighting times after nightfall. */
    private static final int AT_NIGHT = 0x40000000;
    /** Flag indicating lighting times after Shabbath. */
    private static final int MOTZE_SHABBATH = AT_NIGHT;

    protected static final int CANDLES_MASK = 0x0000000F;
    protected static final int HOLIDAY_MASK = 0x000000FF;
    protected static final int MOTZE_MASK = 0xF0000000;

    protected static final String OPINION_10_2 = ZmanimSettings.OPINION_10_2;
    protected static final String OPINION_11 = ZmanimSettings.OPINION_11;
    protected static final String OPINION_12 = ZmanimSettings.OPINION_12;
    protected static final String OPINION_120 = ZmanimSettings.OPINION_120;
    protected static final String OPINION_120_ZMANIS = ZmanimSettings.OPINION_120_ZMANIS;
    protected static final String OPINION_13 = ZmanimSettings.OPINION_13;
    protected static final String OPINION_15 = ZmanimSettings.OPINION_15;
    protected static final String OPINION_16_1 = ZmanimSettings.OPINION_16_1;
    protected static final String OPINION_16_1_ALOS = ZmanimSettings.OPINION_16_1_ALOS;
    protected static final String OPINION_16_1_SUNSET = ZmanimSettings.OPINION_16_1_SUNSET;
    protected static final String OPINION_18 = ZmanimSettings.OPINION_18;
    protected static final String OPINION_19_8 = ZmanimSettings.OPINION_19_8;
    protected static final String OPINION_2 = ZmanimSettings.OPINION_2;
    protected static final String OPINION_26 = ZmanimSettings.OPINION_26;
    protected static final String OPINION_3 = ZmanimSettings.OPINION_3;
    protected static final String OPINION_3_65 = ZmanimSettings.OPINION_3_65;
    protected static final String OPINION_3_676 = ZmanimSettings.OPINION_3_676;
    protected static final String OPINION_30 = ZmanimSettings.OPINION_30;
    protected static final String OPINION_4_37 = ZmanimSettings.OPINION_4_37;
    protected static final String OPINION_4_61 = ZmanimSettings.OPINION_4_61;
    protected static final String OPINION_4_8 = ZmanimSettings.OPINION_4_8;
    protected static final String OPINION_5_88 = ZmanimSettings.OPINION_5_88;
    protected static final String OPINION_5_95 = ZmanimSettings.OPINION_5_95;
    protected static final String OPINION_58 = ZmanimSettings.OPINION_58;
    protected static final String OPINION_6 = ZmanimSettings.OPINION_6;
    protected static final String OPINION_60 = ZmanimSettings.OPINION_60;
    protected static final String OPINION_7 = ZmanimSettings.OPINION_7;
    protected static final String OPINION_7_083 = ZmanimSettings.OPINION_7_083;
    protected static final String OPINION_72 = ZmanimSettings.OPINION_72;
    protected static final String OPINION_72_ZMANIS = ZmanimSettings.OPINION_72_ZMANIS;
    protected static final String OPINION_8_5 = ZmanimSettings.OPINION_8_5;
    protected static final String OPINION_90 = ZmanimSettings.OPINION_90;
    protected static final String OPINION_90_ZMANIS = ZmanimSettings.OPINION_90_ZMANIS;
    protected static final String OPINION_96 = ZmanimSettings.OPINION_96;
    protected static final String OPINION_96_ZMANIS = ZmanimSettings.OPINION_96_ZMANIS;
    protected static final String OPINION_ATERET = ZmanimSettings.OPINION_ATERET;
    protected static final String OPINION_GRA = ZmanimSettings.OPINION_GRA;
    protected static final String OPINION_MGA = ZmanimSettings.OPINION_MGA;
    protected static final String OPINION_FIXED = ZmanimSettings.OPINION_FIXED;
    protected static final String OPINION_LEVEL = ZmanimSettings.OPINION_LEVEL;
    protected static final String OPINION_SEA = ZmanimSettings.OPINION_SEA;
    protected static final String OPINION_TWILIGHT = ZmanimSettings.OPINION_TWILIGHT;
    protected static final String OPINION_NIGHT = ZmanimSettings.OPINION_NIGHT;

    private final Context context;
    private final ZmanimSettings settings;
    protected final ComplexZmanimCalendar calendar;
    protected boolean inIsrael;
    protected boolean summaries;
    protected boolean showElapsed;

    /**
     * Creates a new populater.
     *
     * @param context
     *         the context.
     * @param settings
     *         the application settings.
     */
    public ZmanimPopulater(Context context, ZmanimSettings settings) {
        this.context = context;
        this.settings = settings;
        this.calendar = new ComplexZmanimCalendar();
    }

    protected Context getContext() {
        return context;
    }

    protected ZmanimSettings getSettings() {
        return settings;
    }

    /**
     * Get the calendar.
     *
     * @return the calendar.
     */
    public ComplexZmanimCalendar getCalendar() {
        return calendar;
    }

    protected void prePopulate(A adapter) {
        adapter.clear();
        adapter.setCalendar(calendar.getCalendar());
        adapter.setGeoLocation(calendar.getGeoLocation());
        adapter.setInIsrael(inIsrael);

        ZmanimSettings settings = getSettings();

        summaries = settings.isSummaries();
        showElapsed = settings.isPast();
    }

    /**
     * Populate the list of times.
     *
     * @param adapter
     *         the adapter to populate.
     * @param remote
     *         is for remote views?
     */
    public void populate(A adapter, boolean remote) {
        Context context = getContext();
        ZmanimSettings settings = getSettings();

        prePopulate(adapter);
        populateImpl(adapter, remote, context, settings);
    }

    /**
     * Populate the list of times - implementation.
     *
     * @param adapter
     *         the adapter to populate.
     * @param remote
     *         is for remote views?
     * @param context
     *         the context.
     * @param settings
     *         the settings.
     */
    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimSettings settings) {
        ComplexZmanimCalendar cal = getCalendar();
        Calendar gcal = cal.getCalendar();
        JewishCalendar jcal = getJewishCalendar();
        int candlesOffset = settings.getCandleLightingOffset();
        int candles = getCandles(jcal);
        int candlesCount = candles & CANDLES_MASK;
        boolean hasCandles = candlesCount > 0;
        int candlesHow = candles & MOTZE_MASK;
        int holidayTomorrow = (candles >> 4) & HOLIDAY_MASK;
        int holidayToday = (candles >> 12) & HOLIDAY_MASK;
        Date dateCandles;

        Date date;
        int summary;
        String opinion;
        final Resources res = context.getResources();

        if (!remote && settings.isHour()) {
            long time;
            opinion = settings.getHour();
            if (OPINION_19_8.equals(opinion)) {
                time = cal.getShaahZmanis19Point8Degrees();
                summary = R.string.hour_19;
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
            } else {
                time = cal.getShaahZmanisGra();
                summary = R.string.hour_gra;
            }
            // Offset is added back when formatted.
            adapter.add(R.string.hour, summary, time - gcal.getTimeZone().getRawOffset(), remote);
        }

        opinion = settings.getDawn();
        if (OPINION_19_8.equals(opinion)) {
            date = cal.getAlos19Point8Degrees();
            summary = R.string.dawn_19;
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
        } else {
            date = cal.getAlosHashachar();
            summary = R.string.dawn_16;
        }
        if (date == null) {
            date = cal.getAlos120Zmanis();
            summary = R.string.dawn_120_zmanis;
        }
        adapter.add(R.string.dawn, summary, date, remote);
        if ((holidayToday == JewishCalendar.SEVENTEEN_OF_TAMMUZ) || (holidayToday == JewishCalendar.FAST_OF_GEDALYAH) || (holidayToday == JewishCalendar.TENTH_OF_TEVES)
                || (holidayToday == JewishCalendar.FAST_OF_ESTHER)) {
            adapter.add(R.string.fast_begins, null, date, remote);
        }

        opinion = settings.getTallis();
        if (OPINION_10_2.equals(opinion)) {
            date = cal.getMisheyakir10Point2Degrees();
            summary = R.string.tallis_10;
        } else if (OPINION_11.equals(opinion)) {
            date = cal.getMisheyakir11Degrees();
            summary = R.string.tallis_11;
        } else {
            date = cal.getMisheyakir11Point5Degrees();
            summary = R.string.tallis_summary;
        }
        int tallisTitle = R.string.tallis;
        if ((holidayToday == SHABBATH) || jcal.isYomTov() || jcal.isCholHamoed()) {
            tallisTitle = R.string.tallis_only;
        }
        adapter.add(tallisTitle, summary, date, remote);

        opinion = settings.getSunrise();
        if (OPINION_SEA.equals(opinion)) {
            date = cal.getSeaLevelSunrise();
            summary = R.string.sunrise_sea;
        } else {
            date = cal.getSunrise();
            summary = R.string.sunrise_summary;
        }
        adapter.add(R.string.sunrise, summary, date, remote);

        opinion = settings.getLastShema();
        if (OPINION_16_1_SUNSET.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToSunset();
            summary = R.string.shema_16_sunset;
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
            summary = R.string.shema_7;
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getSofZmanShmaMGA19Point8Degrees();
            summary = R.string.shema_19;
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
        } else {
            date = cal.getSofZmanShmaGRA();
            summary = R.string.shema_gra;
        }
        adapter.add(R.string.shema, summary, date, remote);

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
            summary = R.string.prayers_19;
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
        } else {
            date = cal.getSofZmanTfilaGRA();
            summary = R.string.prayers_gra;
        }
        adapter.add(R.string.prayers, summary, date, remote);
        if (holidayToday == JewishCalendar.EREV_PESACH)
            adapter.add(R.string.eat_chametz, summary, date, remote);

        if (!remote && (holidayToday == JewishCalendar.EREV_PESACH)) {
            opinion = settings.getBurnChametz();
            if (OPINION_16_1.equals(opinion)) {
                date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
                summary = R.string.burn_chametz_16;
            } else if (OPINION_72.equals(opinion)) {
                date = cal.getSofZmanBiurChametzMGA72Minutes();
                summary = R.string.burn_chametz_72;
            } else {
                date = cal.getSofZmanBiurChametzGRA();
                summary = R.string.burn_chametz_gra;
            }
            adapter.add(R.string.burn_chametz, summary, date, remote);
        }

        opinion = settings.getMidday();
        if (OPINION_FIXED.equals(opinion)) {
            date = cal.getFixedLocalChatzos();
            summary = R.string.midday_fixed;
        } else {
            date = cal.getChatzos();
            summary = R.string.midday_summary;
        }
        adapter.add(R.string.midday, summary, date, remote);
        Date midday = date;

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
        } else {
            date = cal.getMinchaGedola();
            summary = R.string.earliest_mincha_summary;
        }
        adapter.add(R.string.earliest_mincha, summary, date, remote);

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
        } else {
            date = cal.getMinchaKetana();
            summary = R.string.mincha_summary;
        }
        adapter.add(R.string.mincha, summary, date, remote);

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
            summary = R.string.plug_hamincha_19;
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
        } else {
            date = cal.getPlagHamincha();
            summary = R.string.plug_hamincha_gra;
        }
        adapter.add(R.string.plug_hamincha, summary, date, remote);

        opinion = settings.getSunset();
        if (OPINION_LEVEL.equals(opinion)) {
            date = cal.getSunset();
            summary = R.string.sunset_summary;
        } else {
            date = cal.getSeaLevelSunset();
            summary = R.string.sunset_sea;
        }
        if (hasCandles && (candlesHow == BEFORE_SUNSET)) {
            dateCandles = cal.getTimeOffset(date, -candlesOffset * DateUtils.MINUTE_IN_MILLIS);
            String summaryText;
            if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
            } else {
                summaryText = res.getQuantityString(R.plurals.candles_summary, candlesOffset, candlesOffset);
            }
            adapter.add(R.string.candles, summaryText, dateCandles, remote);
        }

        if (hasCandles && (candlesHow == AT_SUNSET)) {
            if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                adapter.add(R.string.candles, summaryText, date, remote);
            } else {
                adapter.add(R.string.candles, summary, date, remote);
            }
        }
        if ((holidayTomorrow == JewishCalendar.TISHA_BEAV) || (holidayTomorrow == JewishCalendar.YOM_KIPPUR)) {
            adapter.add(R.string.fast_begins, null, date, remote);
        }
        adapter.add(R.string.sunset, summary, date, remote);

        opinion = settings.getTwilight();
        if (OPINION_7_083.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
            summary = R.string.twilight_7_083;
        } else if (OPINION_58.equals(opinion)) {
            date = cal.getBainHasmashosRT58Point5Minutes();
            summary = R.string.twilight_58;
        } else if (OPINION_13.equals(opinion)) {
            date = cal.getBainHasmashosRT13Point24Degrees();
            summary = R.string.twilight_13;
        } else {
            date = cal.getBainHasmashosRT2Stars();
            summary = R.string.twilight_2stars;
            if (date == null) {
                date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
                summary = R.string.twilight_7_083;
            }
        }
        if (hasCandles && (candlesHow == AT_TWILIGHT)) {
            if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                adapter.add(R.string.candles, summaryText, date, remote);
            } else {
                adapter.add(R.string.candles, summary, date, remote);
            }
        }
        adapter.add(R.string.twilight, summary, date, remote);
        if ((holidayToday == JewishCalendar.SEVENTEEN_OF_TAMMUZ) || (holidayToday == JewishCalendar.TISHA_BEAV) || (holidayToday == JewishCalendar.FAST_OF_GEDALYAH)
                || (holidayToday == JewishCalendar.TENTH_OF_TEVES) || (holidayToday == JewishCalendar.FAST_OF_ESTHER)) {
            adapter.add(R.string.fast_ends, null, date, remote);
        }

        opinion = settings.getNightfall();
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
            summary = R.string.nightfall_19;
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
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getTzaisGeonim7Point083Degrees();
            summary = R.string.nightfall_7;
        } else if (OPINION_8_5.equals(opinion)) {
            date = cal.getTzaisGeonim8Point5Degrees();
            summary = R.string.nightfall_8;
        } else {
            date = cal.getTzais();
            summary = R.string.nightfall_3stars;
        }
        adapter.add(R.string.nightfall, summary, date, remote);
        if (holidayToday == JewishCalendar.YOM_KIPPUR) {
            adapter.add(R.string.fast_ends, null, date, remote);
        }
        if (hasCandles && (candlesHow == AT_NIGHT)) {
            if (holidayTomorrow == JewishCalendar.CHANUKAH) {
                String summaryText = res.getQuantityString(R.plurals.candles_chanukka, candlesCount, candlesCount);
                adapter.add(R.string.candles, summaryText, date, remote);
            } else {
                adapter.add(R.string.candles, summary, date, remote);
            }
        }
        Date nightfall = date;

        opinion = settings.getMidnight();
        if (OPINION_12.equals(opinion)) {
            date = midday;
            if (date != null)
                date.setTime(date.getTime() + TWELVE_HOURS);
            summary = R.string.midnight_12;
        } else if (OPINION_6.equals(opinion)) {
            date = nightfall;
            if (date != null)
                date.setTime(date.getTime() + SIX_HOURS);
            summary = R.string.midnight_6;
        } else {
            date = cal.getSolarMidnight();
            summary = R.string.midnight_summary;
        }
        adapter.add(R.string.midnight, summary, date, remote);

        if (!remote) {
            final int jDayOfMonth = jcal.getJewishDayOfMonth();
            // Molad.
            if ((jDayOfMonth <= 1) || (jDayOfMonth >= 25)) {
                int y = gcal.get(Calendar.YEAR);
                int m = gcal.get(Calendar.MONTH);
                int d = gcal.get(Calendar.DAY_OF_MONTH);
                jcal.forward();// Molad is always of the previous month.
                JewishDate molad = jcal.getMolad();
                int moladYear = molad.getGregorianYear();
                int moladMonth = molad.getGregorianMonth();
                int moladDay = molad.getGregorianDayOfMonth();
                if ((moladYear == y) && (moladMonth == m) && (moladDay == d)) {
                    double moladSeconds = (molad.getMoladChalakim() * 10.0) / 3.0;
                    double moladSecondsFloor = Math.floor(moladSeconds);
                    Calendar calMolad = (Calendar) gcal.clone();
                    calMolad.set(moladYear, moladMonth, moladDay, molad.getMoladHours(), molad.getMoladMinutes(), (int) moladSecondsFloor);
                    calMolad.set(Calendar.MILLISECOND, (int) (DateUtils.SECOND_IN_MILLIS * (moladSeconds - moladSecondsFloor)));
                    summary = R.string.molad_summary;
                    adapter.add(R.string.molad, summary, calMolad.getTime(), remote);
                }
            }
            // First Kiddush Levana.
            else if ((jDayOfMonth >= 2) && (jDayOfMonth <= 8)) {
                opinion = settings.getEarliestKiddushLevana();
                if (OPINION_7.equals(opinion)) {
                    date = cal.getTchilasZmanKidushLevana7Days();
                    summary = R.string.levana_7;
                } else {
                    date = cal.getTchilasZmanKidushLevana3Days();
                    summary = R.string.levana_earliest_summary;
                }
                adapter.add(R.string.levana_earliest, summary, date, remote);
            }
            // Last Kiddush Levana.
            else if ((jDayOfMonth > 10) && (jDayOfMonth < 20)) {
                opinion = settings.getLatestKiddushLevana();
                if (OPINION_15.equals(opinion)) {
                    date = cal.getSofZmanKidushLevana15Days();
                    summary = R.string.levana_15;
                } else {
                    date = cal.getSofZmanKidushLevanaBetweenMoldos();
                    summary = R.string.levana_latest_summary;
                }
                adapter.add(R.string.levana_latest, summary, date, remote);
            }
        }

        adapter.sort();
    }

    /**
     * Get the number of candles to light.
     *
     * @param jcal
     *         the Jewish calendar.
     * @return the number of candles to light, the holiday, and when to light.
     */
    protected int getCandles(JewishCalendar jcal) {
        final int dayOfWeek = jcal.getDayOfWeek();

        // Check if the following day is special, because we can't check EREV_CHANUKAH.
        int holidayToday = jcal.getYomTovIndex();
        jcal.forward();
        int holidayTomorrow = jcal.getYomTovIndex();
        int count = CANDLES_NONE;
        int flags = BEFORE_SUNSET;

        switch (holidayTomorrow) {
            case JewishCalendar.PESACH:
            case JewishCalendar.SHAVUOS:
            case JewishCalendar.ROSH_HASHANA:
            case JewishCalendar.SUCCOS:
            case JewishCalendar.SHEMINI_ATZERES:
            case JewishCalendar.SIMCHAS_TORAH:
                count = CANDLES_FESTIVAL;
                break;
            case JewishCalendar.YOM_KIPPUR:
                count = CANDLES_YOM_KIPPUR;
                break;
            case JewishCalendar.CHANUKAH:
                count = jcal.getDayOfChanukah();
                if ((dayOfWeek != Calendar.FRIDAY) && (dayOfWeek != Calendar.SATURDAY)) {
                    String opinion = settings.getChanukkaCandles();
                    if (OPINION_TWILIGHT.equals(opinion)) {
                        flags = AT_TWILIGHT;
                    } else if (OPINION_NIGHT.equals(opinion)) {
                        flags = AT_NIGHT;
                    } else {
                        flags = AT_SUNSET;
                    }
                }
                break;
            default:
                if (dayOfWeek == Calendar.FRIDAY) {
                    holidayTomorrow = SHABBATH;
                    count = CANDLES_SHABBATH;
                }
                break;
        }

        // Forbidden to light candles during Shabbath.
        switch (dayOfWeek) {
            case Calendar.FRIDAY:
                // Probably never happens that Yom Kippurim falls on a Friday.
                // Prohibited to light candles on Yom Kippurim for Shabbath.
                if (holidayToday == JewishCalendar.YOM_KIPPUR) {
                    count = CANDLES_NONE;
                }
                break;
            case Calendar.SATURDAY:
                if (holidayToday == -1) {
                    holidayToday = SHABBATH;
                }
                flags = MOTZE_SHABBATH;
                break;
            default:
                // During a holiday, we can light for the next day from an existing
                // flame.
                switch (holidayToday) {
                    case JewishCalendar.ROSH_HASHANA:
                    case JewishCalendar.SUCCOS:
                    case JewishCalendar.SHEMINI_ATZERES:
                    case JewishCalendar.SIMCHAS_TORAH:
                    case JewishCalendar.PESACH:
                    case JewishCalendar.SHAVUOS:
                        flags = AT_SUNSET;
                        break;
                }
                break;
        }

        return flags | ((holidayToday & HOLIDAY_MASK) << 12) | ((holidayTomorrow & HOLIDAY_MASK) << 4) | (count & CANDLES_MASK);
    }

    /**
     * Set the calendar.
     *
     * @param calendar
     *         the calendar.
     */
    public void setCalendar(Calendar calendar) {
        this.calendar.setCalendar(calendar);
    }

    /**
     * Set the calendar time.
     *
     * @param time
     *         the time in milliseconds.
     */
    public void setCalendar(long time) {
        Calendar cal = calendar.getCalendar();
        cal.setTimeInMillis(time);
    }

    /**
     * Sets the {@link GeoLocation}.
     *
     * @param geoLocation
     *         the location.
     */
    public void setGeoLocation(GeoLocation geoLocation) {
        this.calendar.setGeoLocation(geoLocation);
    }

    /**
     * Sets whether to use Israel holiday scheme or not.
     *
     * @param inIsrael
     *         set to {@code true} for calculations for Israel.
     */
    public void setInIsrael(boolean inIsrael) {
        this.inIsrael = inIsrael;
    }

    /**
     * Get the Jewish calendar.
     *
     * @return the calendar.
     */
    public JewishCalendar getJewishCalendar() {
        Calendar gcal = getCalendar().getCalendar();
        JewishCalendar jcal = new JewishCalendar(gcal);
        jcal.setInIsrael(inIsrael);
        return jcal;
    }
}

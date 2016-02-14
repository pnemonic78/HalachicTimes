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
import android.text.format.DateUtils;

import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.util.Date;

/**
 * Populater for all opinions of a zman.
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsPopulater<A extends ZmanimAdapter> extends ZmanimPopulater<A> {

    private int itemId;

    /**
     * Creates a new populater.
     *
     * @param context
     *         the context.
     * @param settings
     */
    public ZmanimDetailsPopulater(Context context, ZmanimSettings settings) {
        super(context, settings);
    }

    /**
     * Set the master item id.
     *
     * @param itemId
     *         the master item id.
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    /**
     * Populate the list of times.
     *
     * @param adapter
     *         the adapter to populate.
     * @param remote
     *         is for remote views?
     * @param itemId
     *         the master item id.
     */
    public void populate(A adapter, boolean remote, int itemId) {
        setItemId(itemId);
        super.populate(adapter, remote);
    }

    @Override
    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimSettings settings) {
        populateImpl(adapter, remote, context, settings, itemId);
    }

    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimSettings settings, int itemId) {
        ComplexZmanimCalendar calendar = this.calendar;

        switch (itemId) {
            case R.string.hour:
                populateHour(adapter, calendar, settings);
                break;
            case R.string.dawn:
                populateDawn(adapter, calendar, settings);
                break;
            case R.string.tallis:
            case R.string.tallis_only:
                populateTallis(adapter, calendar, settings);
                break;
            case R.string.sunrise:
                populateSunrise(adapter, calendar, settings);
                break;
            case R.string.shema:
                populateShema(adapter, calendar, settings);
                break;
            case R.string.prayers:
                populatePrayers(adapter, calendar, settings);
                break;
            case R.string.midday:
                populateMidday(adapter, calendar, settings);
                break;
            case R.string.earliest_mincha:
                populateEarliestMincha(adapter, calendar, settings);
                break;
            case R.string.mincha:
                populateMincha(adapter, calendar, settings);
                break;
            case R.string.plug_hamincha:
                populatePlugHamincha(adapter, calendar, settings);
                break;
            case R.string.sunset:
                populateSunset(adapter, calendar, settings);
                break;
            case R.string.twilight:
                populateTwilight(adapter, calendar, settings);
                break;
            case R.string.nightfall:
                populateNightfall(adapter, calendar, settings);
                break;
            case R.string.shabbath_ends:
            case R.string.festival_ends:
                populateShabbathEnds(adapter, calendar, settings);
                break;
            case R.string.midnight:
                populateMidnight(adapter, calendar, settings);
                break;
            case R.string.levana_earliest:
                populateEarliestKiddushLevana(adapter, calendar, settings);
                break;
            case R.string.levana_latest:
                populateLatestKiddushLevana(adapter, calendar, settings);
                break;
            case R.string.eat_chametz:
                populateEatChametz(adapter, calendar, settings);
                break;
            case R.string.burn_chametz:
                populateBurnChametz(adapter, calendar, settings);
                break;
        }

        adapter.sort();
    }

    private void populateHour(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        long time;
        int title;
        // Offset is added back when formatted.
        long offset = cal.getCalendar().getTimeZone().getRawOffset();

        time = cal.getShaahZmanis120Minutes();
        title = R.string.hour_120;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis120MinutesZmanis();
        title = R.string.hour_120_zmanis;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis16Point1Degrees();
        title = R.string.hour_16;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis18Degrees();
        title = R.string.hour_18;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis19Point8Degrees();
        title = R.string.hour_19;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis26Degrees();
        title = R.string.hour_26;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis60Minutes();
        title = R.string.hour_60;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis72Minutes();
        title = R.string.hour_72;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis72MinutesZmanis();
        title = R.string.hour_72_zmanis;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis90Minutes();
        title = R.string.hour_90;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis90MinutesZmanis();
        title = R.string.hour_90_zmanis;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis96Minutes();
        title = R.string.hour_96;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanis96MinutesZmanis();
        title = R.string.hour_96_zmanis;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanisAteretTorah();
        title = R.string.hour_ateret;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanisGra();
        title = R.string.hour_gra;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

        time = cal.getShaahZmanisMGA();
        title = R.string.hour_mga;
        adapter.addHour(title, SUMMARY_NONE, time - offset);
    }

    private void populateDawn(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getAlos19Point8Degrees();
        title = R.string.dawn_19;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos120();
        title = R.string.dawn_120;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos120Zmanis();
        title = R.string.dawn_120_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos18Degrees();
        title = R.string.dawn_18;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos26Degrees();
        title = R.string.dawn_26;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos16Point1Degrees();
        title = R.string.dawn_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos96();
        title = R.string.dawn_96;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos90();
        title = R.string.dawn_90;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos72();
        title = R.string.dawn_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos72Zmanis();
        title = R.string.dawn_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getAlos60();
        title = R.string.dawn_60;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateTallis(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getMisheyakir10Point2Degrees();
        title = R.string.tallis_10;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMisheyakir11Degrees();
        title = R.string.tallis_11;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMisheyakir11Point5Degrees();
        title = R.string.tallis_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateSunrise(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSeaLevelSunrise();
        title = R.string.sunrise_sea;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSunrise();
        title = R.string.sunrise_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateShema(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSofZmanShmaAlos16Point1ToSunset();
        title = R.string.shema_16_sunset;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.shema_7;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA19Point8Degrees();
        title = R.string.shema_19;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA120Minutes();
        title = R.string.shema_120;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA18Degrees();
        title = R.string.shema_18;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA96Minutes();
        title = R.string.shema_96;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA96MinutesZmanis();
        title = R.string.shema_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA16Point1Degrees();
        title = R.string.shema_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA90Minutes();
        title = R.string.shema_90;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA90MinutesZmanis();
        title = R.string.shema_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA72Minutes();
        title = R.string.shema_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA72MinutesZmanis();
        title = R.string.shema_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaMGA();
        title = R.string.shema_mga;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaAteretTorah();
        title = R.string.shema_ateret;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShma3HoursBeforeChatzos();
        title = R.string.shema_3;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaFixedLocal();
        title = R.string.shema_fixed;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanShmaGRA();
        title = R.string.shema_gra;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populatePrayers(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSofZmanTfilaMGA120Minutes();
        title = R.string.prayers_120;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA96Minutes();
        title = R.string.prayers_96;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA96MinutesZmanis();
        title = R.string.prayers_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA19Point8Degrees();
        title = R.string.prayers_19;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA90Minutes();
        title = R.string.prayers_90;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA90MinutesZmanis();
        title = R.string.prayers_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilahAteretTorah();
        title = R.string.prayers_ateret;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA18Degrees();
        title = R.string.prayers_18;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaFixedLocal();
        title = R.string.prayers_fixed;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA16Point1Degrees();
        title = R.string.prayers_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA72Minutes();
        title = R.string.prayers_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA72MinutesZmanis();
        title = R.string.prayers_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfila2HoursBeforeChatzos();
        title = R.string.prayers_2;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaGRA();
        title = R.string.prayers_gra;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanTfilaMGA();
        title = R.string.prayers_mga;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateMidday(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getFixedLocalChatzos();
        title = R.string.midday_fixed;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getChatzos();
        title = R.string.midday_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateEarliestMincha(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getMinchaGedola16Point1Degrees();
        title = R.string.earliest_mincha_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaGedola30Minutes();
        title = R.string.earliest_mincha_30;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaGedolaAteretTorah();
        title = R.string.earliest_mincha_ateret;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaGedola72Minutes();
        title = R.string.earliest_mincha_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaGedola();
        title = R.string.earliest_mincha_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateMincha(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getMinchaKetana16Point1Degrees();
        title = R.string.mincha_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaKetana72Minutes();
        title = R.string.mincha_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaKetanaAteretTorah();
        title = R.string.mincha_ateret;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getMinchaKetana();
        title = R.string.mincha_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populatePlugHamincha(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getPlagAlosToSunset();
        title = R.string.plug_hamincha_16_sunset;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.plug_hamincha_16_alos;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHaminchaAteretTorah();
        title = R.string.plug_hamincha_ateret;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha60Minutes();
        title = R.string.plug_hamincha_60;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha72Minutes();
        title = R.string.plug_hamincha_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha72MinutesZmanis();
        title = R.string.plug_hamincha_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha16Point1Degrees();
        title = R.string.plug_hamincha_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha18Degrees();
        title = R.string.plug_hamincha_18;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha90Minutes();
        title = R.string.plug_hamincha_90;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha90MinutesZmanis();
        title = R.string.plug_hamincha_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha19Point8Degrees();
        title = R.string.plug_hamincha_19;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha96Minutes();
        title = R.string.plug_hamincha_96;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha96MinutesZmanis();
        title = R.string.plug_hamincha_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha120Minutes();
        title = R.string.plug_hamincha_120;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha120MinutesZmanis();
        title = R.string.plug_hamincha_120_zmanis;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha26Degrees();
        title = R.string.plug_hamincha_26;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getPlagHamincha();
        title = R.string.plug_hamincha_gra;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateSunset(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSeaLevelSunset();
        title = R.string.sunset_sea;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSunset();
        title = R.string.sunset_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateTwilight(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
        title = R.string.twilight_7_083;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getBainHasmashosRT58Point5Minutes();
        title = R.string.twilight_58;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getBainHasmashosRT13Point24Degrees();
        title = R.string.twilight_13;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getBainHasmashosRT2Stars();
        title = R.string.twilight_2stars;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateNightfall(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        populateNightfall(adapter, cal, settings, 0);
    }

    private void populateNightfall(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings, long offset) {
        Date date;
        int title;

        date = cal.getTzais120();
        if (date != null) {
            title = R.string.nightfall_120;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais120Zmanis();
        if (date != null) {
            title = R.string.nightfall_120_zmanis;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        if (date != null) {
            date = cal.getTzais16Point1Degrees();
            title = R.string.nightfall_16;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais18Degrees();
        if (date != null) {
            title = R.string.nightfall_18;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais19Point8Degrees();
        if (date != null) {
            title = R.string.nightfall_19;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais26Degrees();
        if (date != null) {
            title = R.string.nightfall_26;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais60();
        if (date != null) {
            title = R.string.nightfall_60;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais72();
        if (date != null) {
            title = R.string.nightfall_72;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais72Zmanis();
        if (date != null) {
            title = R.string.nightfall_72_zmanis;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais90();
        if (date != null) {
            title = R.string.nightfall_90;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais90Zmanis();
        if (date != null) {
            title = R.string.nightfall_90_zmanis;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        if (date != null) {
            date = cal.getTzais96();
            title = R.string.nightfall_96;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzais96Zmanis();
        if (date != null) {
            title = R.string.nightfall_96_zmanis;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisAteretTorah();
        if (date != null) {
            title = R.string.nightfall_ateret;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim3Point65Degrees();
        if (date != null) {
            title = R.string.nightfall_3_65;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim3Point676Degrees();
        if (date != null) {
            title = R.string.nightfall_3_676;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim4Point37Degrees();
        if (date != null) {
            title = R.string.nightfall_4_37;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim4Point61Degrees();
        if (date != null) {
            title = R.string.nightfall_4_61;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim4Point8Degrees();
        if (date != null) {
            title = R.string.nightfall_4_8;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim5Point88Degrees();
        if (date != null) {
            title = R.string.nightfall_5_88;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim5Point95Degrees();
        if (date != null) {
            title = R.string.nightfall_5_95;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim7Point083Degrees();
        if (date != null) {
            title = R.string.nightfall_7;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }

        date = cal.getTzaisGeonim8Point5Degrees();
        if (date != null) {
            title = R.string.nightfall_8;
            adapter.add(title, SUMMARY_NONE, date.getTime() + offset);
        }
    }

    private void populateMidnight(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = getMidday(cal, settings);
        if (date != null)
            date.setTime(date.getTime() + TWELVE_HOURS);
        title = R.string.midnight_12;
        adapter.add(title, SUMMARY_NONE, date);

        date = getNightfall(cal, settings);
        if (date != null)
            date.setTime(date.getTime() + SIX_HOURS);
        title = R.string.midnight_6;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSolarMidnight();
        title = R.string.midnight_summary;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateEatChametz(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSofZmanAchilasChametzGRA();
        title = R.string.prayers_gra;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanAchilasChametzMGA16Point1Degrees();
        title = R.string.prayers_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanAchilasChametzMGA72Minutes();
        title = R.string.prayers_72;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateBurnChametz(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
        title = R.string.burn_chametz_16;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanBiurChametzMGA72Minutes();
        title = R.string.burn_chametz_72;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanBiurChametzGRA();
        title = R.string.burn_chametz_gra;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateEarliestKiddushLevana(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getTchilasZmanKidushLevana3Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getTchilasZmanKidushLevana3Days();
        }
        title = R.string.levana_earliest_summary;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getTchilasZmanKidushLevana7Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getTchilasZmanKidushLevana7Days();
        }
        title = R.string.levana_7;
        adapter.add(title, SUMMARY_NONE, date);
    }

    private void populateLatestKiddushLevana(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        int title;

        date = cal.getSofZmanKidushLevanaBetweenMoldos();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getSofZmanKidushLevanaBetweenMoldos();
        }
        title = R.string.levana_latest_summary;
        adapter.add(title, SUMMARY_NONE, date);

        date = cal.getSofZmanKidushLevana15Days();
        if (date == null) {
            JewishCalendar jcal = new JewishCalendar(cal.getCalendar());
            date = jcal.getSofZmanKidushLevana15Days();
        }
        title = R.string.levana_15;
        adapter.add(title, SUMMARY_NONE, date);
    }

    protected Date getMidday(ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        String opinion = settings.getMidday();
        if (OPINION_FIXED.equals(opinion)) {
            date = cal.getFixedLocalChatzos();
        } else {
            date = cal.getChatzos();
        }
        return date;
    }

    protected Date getNightfall(ComplexZmanimCalendar cal, ZmanimSettings settings) {
        Date date;
        String opinion = settings.getNightfall();
        if (OPINION_120.equals(opinion)) {
            date = cal.getTzais120();
        } else if (OPINION_120_ZMANIS.equals(opinion)) {
            date = cal.getTzais120Zmanis();
        } else if (OPINION_16_1.equals(opinion)) {
            date = cal.getTzais16Point1Degrees();
        } else if (OPINION_18.equals(opinion)) {
            date = cal.getTzais18Degrees();
        } else if (OPINION_19_8.equals(opinion)) {
            date = cal.getTzais19Point8Degrees();
        } else if (OPINION_26.equals(opinion)) {
            date = cal.getTzais26Degrees();
        } else if (OPINION_60.equals(opinion)) {
            date = cal.getTzais60();
        } else if (OPINION_72.equals(opinion)) {
            date = cal.getTzais72();
        } else if (OPINION_72_ZMANIS.equals(opinion)) {
            date = cal.getTzais72Zmanis();
        } else if (OPINION_90.equals(opinion)) {
            date = cal.getTzais90();
        } else if (OPINION_90_ZMANIS.equals(opinion)) {
            date = cal.getTzais90Zmanis();
        } else if (OPINION_96.equals(opinion)) {
            date = cal.getTzais96();
        } else if (OPINION_96_ZMANIS.equals(opinion)) {
            date = cal.getTzais96Zmanis();
        } else if (OPINION_ATERET.equals(opinion)) {
            date = cal.getTzaisAteretTorah();
        } else if (OPINION_3_65.equals(opinion)) {
            date = cal.getTzaisGeonim3Point65Degrees();
        } else if (OPINION_3_676.equals(opinion)) {
            date = cal.getTzaisGeonim3Point676Degrees();
        } else if (OPINION_4_37.equals(opinion)) {
            date = cal.getTzaisGeonim4Point37Degrees();
        } else if (OPINION_4_61.equals(opinion)) {
            date = cal.getTzaisGeonim4Point61Degrees();
        } else if (OPINION_4_8.equals(opinion)) {
            date = cal.getTzaisGeonim4Point8Degrees();
        } else if (OPINION_5_88.equals(opinion)) {
            date = cal.getTzaisGeonim5Point88Degrees();
        } else if (OPINION_5_95.equals(opinion)) {
            date = cal.getTzaisGeonim5Point95Degrees();
        } else if (OPINION_7_083.equals(opinion)) {
            date = cal.getTzaisGeonim7Point083Degrees();
        } else if (OPINION_8_5.equals(opinion)) {
            date = cal.getTzaisGeonim8Point5Degrees();
        } else {
            date = cal.getTzais();
        }

        return date;
    }

    private void populateShabbathEnds(A adapter, ComplexZmanimCalendar cal, ZmanimSettings settings) {
        long offset = settings.getShabbathEnds() * DateUtils.MINUTE_IN_MILLIS;
        populateNightfall(adapter, cal, settings, offset);
    }
}

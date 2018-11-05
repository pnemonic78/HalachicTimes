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

import com.github.times.preference.ZmanimPreferences;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.util.Calendar;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

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
     * @param context the context.
     */
    public ZmanimDetailsPopulater(Context context, ZmanimPreferences settings) {
        super(context, settings);
    }

    /**
     * Set the master item id.
     *
     * @param itemId the master item id.
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @Override
    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimPreferences settings) {
        populateImpl(adapter, remote, context, settings, itemId);
    }

    protected void populateImpl(A adapter, boolean remote, Context context, ZmanimPreferences settings, int itemId) {
        final ComplexZmanimCalendar calendar = this.calendar;

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
            case R.string.midnight_guard:
            case R.string.morning_guard:
                populateGuards(adapter, calendar, settings);
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

    private void populateHour(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        long time;
        int title;
        // Offset is added back when formatted.
        long offset = cal.getCalendar().getTimeZone().getRawOffset();

        time = cal.getShaahZmanisBaalHatanya();
        title = R.string.hour_baal_hatanya;
        adapter.addHour(title, SUMMARY_NONE, time - offset);

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
        title = R.string.hour_19_8;
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

    private void populateDawn(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getAlosBaalHatanya();
        title = R.string.dawn_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos19Degrees();
        title = R.string.dawn_19;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos19Point8Degrees();
        title = R.string.dawn_19_8;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos120();
        title = R.string.dawn_120;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos120Zmanis();
        title = R.string.dawn_120_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos18Degrees();
        title = R.string.dawn_18;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos26Degrees();
        title = R.string.dawn_26;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos16Point1Degrees();
        title = R.string.dawn_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos96();
        title = R.string.dawn_96;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos90();
        title = R.string.dawn_90;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos90Zmanis();
        title = R.string.dawn_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos72();
        title = R.string.dawn_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos72Zmanis();
        title = R.string.dawn_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getAlos60();
        title = R.string.dawn_60;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateTallis(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getMisheyakir10Point2Degrees();
        title = R.string.tallis_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMisheyakir7Point65Degrees();
        title = R.string.tallis_7_65;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMisheyakir9Point5Degrees();
        title = R.string.tallis_9_5;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMisheyakir10Point2Degrees();
        title = R.string.tallis_10;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMisheyakir11Degrees();
        title = R.string.tallis_11;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMisheyakir11Point5Degrees();
        title = R.string.tallis_11_5;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateSunrise(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSeaLevelSunrise();
        title = R.string.sunrise_sea;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSunrise();
        title = R.string.sunrise_elevated;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateShema(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSofZmanShmaBaalHatanya();
        title = R.string.shema_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaAlos16Point1ToSunset();
        title = R.string.shema_16_sunset;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.shema_7;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA19Point8Degrees();
        title = R.string.shema_19_8;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA120Minutes();
        title = R.string.shema_120;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA18Degrees();
        title = R.string.shema_18;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA96Minutes();
        title = R.string.shema_96;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA96MinutesZmanis();
        title = R.string.shema_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA16Point1Degrees();
        title = R.string.shema_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA90Minutes();
        title = R.string.shema_90;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA90MinutesZmanis();
        title = R.string.shema_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA72Minutes();
        title = R.string.shema_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA72MinutesZmanis();
        title = R.string.shema_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaMGA();
        title = R.string.shema_mga;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaAteretTorah();
        title = R.string.shema_ateret;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShma3HoursBeforeChatzos();
        title = R.string.shema_3;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaFixedLocal();
        title = R.string.shema_fixed;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanShmaGRA();
        title = R.string.shema_gra;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populatePrayers(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSofZmanTfilaBaalHatanya();
        title = R.string.prayers_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA120Minutes();
        title = R.string.prayers_120;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA96Minutes();
        title = R.string.prayers_96;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA96MinutesZmanis();
        title = R.string.prayers_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA19Point8Degrees();
        title = R.string.prayers_19_8;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA90Minutes();
        title = R.string.prayers_90;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA90MinutesZmanis();
        title = R.string.prayers_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilahAteretTorah();
        title = R.string.prayers_ateret;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA18Degrees();
        title = R.string.prayers_18;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaFixedLocal();
        title = R.string.prayers_fixed;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA16Point1Degrees();
        title = R.string.prayers_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA72Minutes();
        title = R.string.prayers_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA72MinutesZmanis();
        title = R.string.prayers_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfila2HoursBeforeChatzos();
        title = R.string.prayers_2;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaGRA();
        title = R.string.prayers_gra;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanTfilaMGA();
        title = R.string.prayers_mga;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateMidday(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getChatzosBaalHatanya();
        title = R.string.midday_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getFixedLocalChatzos();
        title = R.string.midday_fixed;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getChatzos();
        title = R.string.midday_solar;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateEarliestMincha(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getMinchaGedolaBaalHatanyaGreaterThan30();
        title = R.string.earliest_mincha_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaGedola16Point1Degrees();
        title = R.string.earliest_mincha_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaGedola30Minutes();
        title = R.string.earliest_mincha_30;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaGedolaAteretTorah();
        title = R.string.earliest_mincha_ateret;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaGedola72Minutes();
        title = R.string.earliest_mincha_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaGedola();
        title = R.string.earliest_mincha_greater;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateMincha(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getMinchaKetanaBaalHatanya();
        title = R.string.mincha_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaKetana16Point1Degrees();
        title = R.string.mincha_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaKetana72Minutes();
        title = R.string.mincha_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaKetanaAteretTorah();
        title = R.string.mincha_ateret;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getMinchaKetana();
        title = R.string.mincha_lesser;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populatePlugHamincha(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getPlagHaminchaBaalHatanya();
        title = R.string.plug_hamincha_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagAlosToSunset();
        title = R.string.plug_hamincha_16_sunset;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagAlos16Point1ToTzaisGeonim7Point083Degrees();
        title = R.string.plug_hamincha_16_alos;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHaminchaAteretTorah();
        title = R.string.plug_hamincha_ateret;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha60Minutes();
        title = R.string.plug_hamincha_60;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha72Minutes();
        title = R.string.plug_hamincha_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha72MinutesZmanis();
        title = R.string.plug_hamincha_72_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha16Point1Degrees();
        title = R.string.plug_hamincha_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha18Degrees();
        title = R.string.plug_hamincha_18;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha90Minutes();
        title = R.string.plug_hamincha_90;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha90MinutesZmanis();
        title = R.string.plug_hamincha_90_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha19Point8Degrees();
        title = R.string.plug_hamincha_19_8;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha96Minutes();
        title = R.string.plug_hamincha_96;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha96MinutesZmanis();
        title = R.string.plug_hamincha_96_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha120Minutes();
        title = R.string.plug_hamincha_120;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha120MinutesZmanis();
        title = R.string.plug_hamincha_120_zmanis;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha26Degrees();
        title = R.string.plug_hamincha_26;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getPlagHamincha();
        title = R.string.plug_hamincha_gra;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateSunset(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        populateSunset(adapter, cal, settings, 0);
    }

    private void populateSunset(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings, long offset) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSeaLevelSunset();
        if (date != null) {
            title = R.string.sunset_sea;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getSunset();
        if (date != null) {
            title = R.string.sunset_elevated;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }
    }

    private void populateTwilight(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        populateTwilight(adapter, cal, settings, 0);
    }

    private void populateTwilight(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings, long offset) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();
        jewishDate.forward(Calendar.DATE, 1);

        date = cal.getBainHasmashosRT13Point5MinutesBefore7Point083Degrees();
        if (date != null) {
            title = R.string.twilight_7_083;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getBainHasmashosRT13Point5MinutesZmanisBefore7Point083Degrees();
        if (date != null) {
            title = R.string.twilight_7_083_zmanis;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getBainHasmashosRT58Point5Minutes();
        if (date != null) {
            title = R.string.twilight_58;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getBainHasmashosRT13Point24Degrees();
        if (date != null) {
            title = R.string.twilight_13;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getBainHasmashosRT2Stars();
        if (date != null) {
            title = R.string.twilight_2stars;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }
    }

    private void populateNightfall(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        populateNightfall(adapter, cal, settings, 0);
    }

    private void populateNightfall(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings, long offset) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();
        jewishDate.forward(Calendar.DATE, 1);

        date = cal.getTzaisBaalHatanya();
        if (date != null) {
            title = R.string.nightfall_baal_hatanya;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais120();
        if (date != null) {
            title = R.string.nightfall_120;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais120Zmanis();
        if (date != null) {
            title = R.string.nightfall_120_zmanis;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais16Point1Degrees();
        if (date != null) {
            title = R.string.nightfall_16;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais18Degrees();
        if (date != null) {
            title = R.string.nightfall_18;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais19Point8Degrees();
        if (date != null) {
            title = R.string.nightfall_19_8;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais26Degrees();
        if (date != null) {
            title = R.string.nightfall_26;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais60();
        if (date != null) {
            title = R.string.nightfall_60;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais72();
        if (date != null) {
            title = R.string.nightfall_72;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais72Zmanis();
        if (date != null) {
            title = R.string.nightfall_72_zmanis;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais90();
        if (date != null) {
            title = R.string.nightfall_90;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais90Zmanis();
        if (date != null) {
            title = R.string.nightfall_90_zmanis;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais96();
        if (date != null) {
            title = R.string.nightfall_96;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzais96Zmanis();
        if (date != null) {
            title = R.string.nightfall_96_zmanis;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisAteretTorah();
        if (date != null) {
            title = R.string.nightfall_ateret;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim3Point65Degrees();
        if (date != null) {
            title = R.string.nightfall_3_65;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim3Point676Degrees();
        if (date != null) {
            title = R.string.nightfall_3_676;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim3Point7Degrees();
        if (date != null) {
            title = R.string.nightfall_3_7;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim3Point8Degrees();
        if (date != null) {
            title = R.string.nightfall_3_8;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim4Point37Degrees();
        if (date != null) {
            title = R.string.nightfall_4_37;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim4Point61Degrees();
        if (date != null) {
            title = R.string.nightfall_4_61;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim4Point8Degrees();
        if (date != null) {
            title = R.string.nightfall_4_8;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim5Point88Degrees();
        if (date != null) {
            title = R.string.nightfall_5_88;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim5Point95Degrees();
        if (date != null) {
            title = R.string.nightfall_5_95;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim7Point083Degrees();
        if (date != null) {
            title = R.string.nightfall_7;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }

        date = cal.getTzaisGeonim8Point5Degrees();
        if (date != null) {
            title = R.string.nightfall_8;
            adapter.add(title, SUMMARY_NONE, date + offset, jewishDate);
        }
    }

    private void populateMidnight(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();
        jewishDate.forward(Calendar.DATE, 1);

        date = getMidday(cal, settings);
        title = R.string.midnight_12;
        adapter.add(title, SUMMARY_NONE, date + TWELVE_HOURS, jewishDate);

        date = getNightfall(cal, settings);
        title = R.string.midnight_6;
        adapter.add(title, SUMMARY_NONE, date + SIX_HOURS, jewishDate);

        date = cal.getSolarMidnight();
        title = R.string.midnight_solar;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateGuards(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();
        jewishDate.forward(Calendar.DATE, 1);

        long sunset = getSunset(cal, settings);
        long sunrise = getSunriseTomorrow(cal, settings);

        date = sunset;
        title = R.string.guard_first;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        String opinion = settings.getGuardsCount();
        if (OPINION_4.equals(opinion)) {
            long midnight = getMidnight(cal, settings);

            date = getMidnightGuard4(sunset, midnight);
            title = R.string.guard_second;
            adapter.add(title, SUMMARY_NONE, date, jewishDate);

            date = midnight;
            title = R.string.guard_third;
            adapter.add(title, SUMMARY_NONE, date, jewishDate);

            date = getMorningGuard4(midnight, sunrise);
            title = R.string.guard_fourth;
            adapter.add(title, SUMMARY_NONE, date, jewishDate);
        } else {
            date = getMidnightGuard3(sunset, sunrise);
            title = R.string.guard_second;
            adapter.add(title, SUMMARY_NONE, date, jewishDate);

            date = getMorningGuard3(sunset, sunrise);
            title = R.string.guard_third;
            adapter.add(title, SUMMARY_NONE, date, jewishDate);
        }
    }

    private void populateEatChametz(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSofZmanAchilasChametzBaalHatanya();
        title = R.string.eat_chametz_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanAchilasChametzMGA16Point1Degrees();
        title = R.string.eat_chametz_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanAchilasChametzMGA72Minutes();
        title = R.string.eat_chametz_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanAchilasChametzGRA();
        title = R.string.eat_chametz_gra;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateBurnChametz(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishDate jewishDate = getJewishCalendar();

        date = cal.getSofZmanBiurChametzBaalHatanya();
        title = R.string.burn_chametz_baal_hatanya;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanBiurChametzMGA16Point1Degrees();
        title = R.string.burn_chametz_16;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanBiurChametzMGA72Minutes();
        title = R.string.burn_chametz_72;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);

        date = cal.getSofZmanBiurChametzGRA();
        title = R.string.burn_chametz_gra;
        adapter.add(title, SUMMARY_NONE, date, jewishDate);
    }

    private void populateEarliestKiddushLevana(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;
        JewishCalendar jcal = getJewishCalendar();

        date = cal.getTchilasZmanKidushLevana3Days();
        if ((date == null) && (jcal != null)) {
            date = jcal.getTchilasZmanKidushLevana3Days();
            if (date != null) {
                ComplexZmanimCalendar cal2 = (ComplexZmanimCalendar) cal.clone();
                cal2.getCalendar().setTimeInMillis(date);
                date = cal2.getTchilasZmanKidushLevana3Days();
            }
        }
        if (date != null) {
            title = R.string.levana_3;
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings));
        }

        date = cal.getTchilasZmanKidushLevana7Days();
        if ((date == null) && (jcal != null)) {
            date = jcal.getTchilasZmanKidushLevana7Days();
            if (date != null) {
                ComplexZmanimCalendar cal2 = (ComplexZmanimCalendar) cal.clone();
                cal2.getCalendar().setTimeInMillis(date);
                date = cal2.getTchilasZmanKidushLevana7Days();
            }
        }
        if (date != null) {
            title = R.string.levana_7;
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings));
        }
    }

    private void populateLatestKiddushLevana(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        Long date;
        int title;

        date = cal.getSofZmanKidushLevanaBetweenMoldos();
        if (date != null) {
            title = R.string.levana_halfway;
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings));
        }

        date = cal.getSofZmanKidushLevana15Days();
        if (date != null) {
            title = R.string.levana_15;
            adapter.add(title, SUMMARY_NONE, date, getJewishDate(date, cal, settings));
        }
    }

    private void populateShabbathEnds(A adapter, ComplexZmanimCalendar cal, ZmanimPreferences settings) {
        long offset = settings.getShabbathEnds() * MINUTE_IN_MILLIS;

        populateSunset(adapter, cal, settings, offset);
        populateTwilight(adapter, cal, settings, offset);
        populateNightfall(adapter, cal, settings, offset);
    }
}

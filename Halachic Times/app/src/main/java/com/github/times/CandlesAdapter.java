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
import android.view.View;
import android.view.ViewGroup;

import com.github.times.preference.ZmanimPreferences;

import static com.github.times.ZmanimPopulater.BEFORE_SUNSET;
import static com.github.times.ZmanimPopulater.HOLIDAY_MASK;
import static com.github.times.ZmanimPopulater.HOLIDAY_TODAY_INDEX;
import static com.github.times.ZmanimPopulater.HOLIDAY_TOMORROW_INDEX;
import static com.github.times.ZmanimPopulater.MOTZE_MASK;
import static com.github.times.ZmanimPopulater.MOTZE_INDEX;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.EREV_YOM_KIPPUR;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

/**
 * Adapter for candles.
 *
 * @author Moshe Waisberg
 */
public class CandlesAdapter extends ZmanimAdapter {

    public CandlesAdapter(Context context, ZmanimPreferences settings) {
        super(context, settings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int holiday = getCandlesHoliday();
        int candlesCount = getCandlesCount();

        switch (holiday) {
            case EREV_YOM_KIPPUR:
            case YOM_KIPPUR:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.candles_kippurim, parent, false);
                }
                break;
            case CHANUKAH:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.candles_channuka, parent, false);
                }
                break;
            default:
                if (candlesCount > 0) {
                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.candles_shabbat, parent, false);
                    }
                }
                break;
        }
        return convertView;
    }

    /**
     * Get the occasion for lighting candles.
     *
     * @return the candles holiday.
     */
    public int getCandlesHoliday() {
        final long candles = getCandles();
        final int when = (int) ((candles >> MOTZE_INDEX) & MOTZE_MASK);
        final int holidayToday = (byte) ((candles >> HOLIDAY_TODAY_INDEX) & HOLIDAY_MASK);
        final int holidayTomorrow = (byte) ((candles >> HOLIDAY_TOMORROW_INDEX) & HOLIDAY_MASK);
        return (when == BEFORE_SUNSET) ? holidayToday : holidayTomorrow;
    }
}

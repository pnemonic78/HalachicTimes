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

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

/**
 * Populater for candles.
 *
 * @author Moshe Waisberg
 */
public class CandlesPopulater extends ZmanimPopulater<CandlesAdapter> {

    /**
     * Creates a new populater.
     *
     * @param context the context.
     */
    public CandlesPopulater(Context context, ZmanimPreferences settings) {
        super(context, settings);
    }

    @Override
    protected void populateImpl(CandlesAdapter adapter, boolean remote, Context context, ZmanimPreferences settings) {
        populateCandles(adapter, settings);
    }

    protected void populateCandles(CandlesAdapter adapter, ZmanimPreferences settings) {
        JewishCalendar jcal = getJewishCalendar();
        int candles = calculateCandles(jcal, settings);
        adapter.setCandles(candles);
    }
}

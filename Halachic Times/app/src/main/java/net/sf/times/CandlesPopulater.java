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
package net.sf.times;

import android.content.Context;

import net.sf.times.preference.ZmanimPreferences;
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
     * @param context
     *         the context.
     * @param settings
     */
    public CandlesPopulater(Context context, ZmanimPreferences settings) {
        super(context, settings);
    }

    @Override
    public void populate(CandlesAdapter adapter, boolean remote) {
        super.populate(adapter, remote);
        populateCandles(adapter);
    }

    protected void populateCandles(CandlesAdapter adapter) {
        JewishCalendar jcal = getJewishCalendar();
        int candles = getCandles(jcal);
        adapter.setCandles(candles);
    }
}

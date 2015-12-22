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

import net.sf.times.preference.ZmanimSettings;
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
    public CandlesPopulater(Context context, ZmanimSettings settings) {
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

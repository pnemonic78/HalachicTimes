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

import java.util.Calendar;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Adapter for candles.
 * 
 * @author Moshe Waisberg
 */
public class CandlesAdapter extends ZmanimAdapter {

	private int mCandles;

	public CandlesAdapter(Context context, ZmanimSettings settings) {
		super(context, settings);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	@Override
	public void populate(boolean remote) {
		prePopulate();

		populateCandles(mCalendar);
	}

	private void populateCandles(ComplexZmanimCalendar cal) {
		Calendar gcal = cal.getCalendar();
		JewishCalendar jcal = new JewishCalendar(gcal);
		jcal.setInIsrael(mInIsrael);
		mCandles = getCandles(jcal);
	}

	/**
	 * Get the candles count.
	 * 
	 * @return the number of candles.
	 */
	public int getCandlesCount() {
		return mCandles & CANDLES_MASK;
	}

	/**
	 * Get the occasion for lighting candles.
	 * 
	 * @return the candles holiday.
	 */
	public int getCandlesHoliday() {
		return (mCandles >> 4) & HOLIDAY_MASK;
	}
}

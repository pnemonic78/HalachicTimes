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

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.view.View;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 * 
 * @author Moshe Waisberg
 */
public class ZmanimDetailsFragment extends ZmanimFragment {

	/** The master id. */
	private int mMasterId;

	/**
	 * Constructs a new fragment.
	 */
	public ZmanimDetailsFragment() {
	}

	@Override
	protected ZmanimAdapter createAdapter(Calendar date, ZmanimLocations locations) {
		if (mMasterId == 0)
			return null;

		GeoLocation gloc = locations.getGeoLocation();
		// Have we been destroyed?
		if (gloc == null)
			return null;
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCalendar(date);
		boolean inIsrael = locations.inIsrael();
		return new ZmanimDetailsAdapter(mActivity, mSettings, cal, inIsrael, mMasterId);
	}

	/**
	 * Populate the list with detailed times.
	 * 
	 * @param date
	 *            the date.
	 * @param id
	 *            the time id.
	 */
	public void populateTimes(Calendar date, int id) {
		mMasterId = id;

		super.populateTimes(date);
	}

	@Override
	protected boolean isBackgroundDrawable() {
		// No special background.
		return false;
	}

	@Override
	protected void setOnClickListener(View view, ZmanimItem item) {
		// No clicking allowed.
	}
}

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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 * 
 * @author Moshe Waisberg
 */
public class ZmanimDetailsActivity extends ZmanimActivity {

	/** The item (time row) parameter. */
	public static final String PARAMETER_ITEM = "item";

	private int mTitleId;

	/**
	 * Creates a new activity.
	 */
	public ZmanimDetailsActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mTitleId = intent.getIntExtra(PARAMETER_ITEM, 0);
		if (mTitleId == 0) {
			finish();
		}
		setTitle(mTitleId);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected ZmanimAdapter createAdapter(Calendar date, ZmanimLocations locations) {
		GeoLocation gloc = locations.getGeoLocation();
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCalendar(date);
		boolean inIsrael = locations.inIsrael();
		return new ZmanimDetailsAdapter(this, mSettings, cal, inIsrael, mTitleId);
	}

	@Override
	protected ZmanimReminder createReminder() {
		return null;
	}

	@Override
	protected boolean isBackgroundDrawable() {
		return false;
	}

	@Override
	protected void setOnClickListener(View view, ZmanimItem item) {
		// Ignore clicks.
	}
}

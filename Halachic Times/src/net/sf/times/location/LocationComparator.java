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
package net.sf.times.location;

import android.annotation.SuppressLint;
import java.text.Collator;
import java.util.Comparator;

/**
 * Compare two cities by their names, then their countries, but not by their
 * locations.
 * 
 * @author Moshe Waisberg
 */
public class LocationComparator implements Comparator<ZmanimAddress> {

	/** Double subtraction error. */
	private static final double EPSILON = 1e-6;

	private Collator mCollator;

	/**
	 * Constructs a new comparator.
	 */
	public LocationComparator() {
		super();
		mCollator = Collator.getInstance();
		mCollator.setStrength(Collator.PRIMARY);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public int compare(ZmanimAddress addr1, ZmanimAddress addr2) {
		String format1 = addr1.getFormatted().toLowerCase();
		String format2 = addr2.getFormatted().toLowerCase();
		int c = mCollator.compare(format1, format2);
		if (c != 0)
			return c;
		double lat1 = addr1.getLatitude();
		double lat2 = addr2.getLatitude();
		double latD = lat1 - lat2;
		if (latD >= EPSILON)
			return 1;
		if (latD <= -EPSILON)
			return -1;
		double lng1 = addr1.getLongitude();
		double lng2 = addr2.getLongitude();
		double lngD = lng1 - lng2;
		if (lngD >= EPSILON)
			return 1;
		if (lngD < -EPSILON)
			return -1;
		return 0;
	}
}

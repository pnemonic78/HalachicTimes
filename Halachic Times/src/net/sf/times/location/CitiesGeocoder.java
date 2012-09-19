/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/MPL-1.1.html
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

import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.R;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;

/**
 * Maintains the lists of cities.
 * 
 * @author Moshe
 */
public class CitiesGeocoder {

	/** The time zone location provider. */
	public static final String TIMEZONE_PROVIDER = "timezone";

	/** One second (1 sec). */
	private static final long ONE_SECOND = 1000;
	/** One minute (1 min). */
	private static final long ONE_MINUTE = 60 * ONE_SECOND;
	/** One hour (1 hr). */
	private static final long ONE_HOUR = 60 * ONE_MINUTE;

	/** Degrees per time zone hour. */
	private static final double TZ_HOUR = 360 / 24;

	protected final Locale mLocale;
	private String[] mNames;
	private static String[] mCountries;
	private static double[] mLatitudes;
	private static double[] mLongitudes;
	private static String[] mTimeZones;

	/**
	 * Constructs a new cities provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public CitiesGeocoder(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Constructs a new cities provider.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public CitiesGeocoder(Context context, Locale locale) {
		super();
		mLocale = locale;

		// Populate arrays from "cities.xml"
		Resources res = context.getResources();
		mNames = res.getStringArray(R.array.cities);
		if (mCountries == null)
			mCountries = res.getStringArray(R.array.countries);
		if (mTimeZones == null)
			mTimeZones = res.getStringArray(R.array.timezones);
		if (mLatitudes == null) {
			String[] latitudes = res.getStringArray(R.array.latitudes);
			mLatitudes = new double[latitudes.length];
			for (int i = 0; i < latitudes.length; i++)
				mLatitudes[i] = Double.parseDouble(latitudes[i]);
		}
		if (mLongitudes == null) {
			String[] longitudes = res.getStringArray(R.array.longitudes);
			mLongitudes = new double[longitudes.length];
			for (int i = 0; i < longitudes.length; i++)
				mLongitudes[i] = Double.parseDouble(longitudes[i]);
		}
	}

	/**
	 * Find the nearest city to the location.
	 * 
	 * @param location
	 *            the location.
	 * @return the city - {@code null} otherwise.
	 */
	public Address getFromLocation(Location location) {
		return getFromLocation(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Find the nearest city to the location.
	 * 
	 * @param latitude
	 *            the latitude.
	 * @param longitude
	 *            the longitude.
	 * @return the city - {@code null} otherwise.
	 */
	public Address getFromLocation(double latitude, double longitude) {
		final double startLatitude = latitude;
		final double startLongitude = longitude;
		double endLatitude = latitude;
		double endLongitude = longitude;
		float[] results = new float[1];
		float distanceMin = Float.MAX_VALUE;
		int found = -1;
		int length = Math.min(mLatitudes.length, mLongitudes.length);

		for (int i = 0; i < length; i++) {
			endLatitude = mLatitudes[i];
			endLongitude = mLongitudes[i];
			Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
			if (results[0] < distanceMin) {
				distanceMin = results[0];
				found = i;
			}
		}

		if (found < 0)
			return null;

		Locale locale = new Locale(mLocale.getLanguage(), mCountries[found]);
		ZmanimAddress city = new ZmanimAddress(locale);
		city.setId(-found);
		city.setLatitude(endLatitude);
		city.setLongitude(endLongitude);
		city.setCountryCode(locale.getCountry());
		city.setCountryName(locale.getDisplayCountry());
		city.setAdminArea(mNames[found]);

		return city;
	}

	/**
	 * Find the first corresponding location for the time zone.
	 * 
	 * @param tz
	 *            the time zone.
	 * @return the location - {@code null} otherwise.
	 */
	public Location findLocation(TimeZone tz) {
		if (tz == null)
			return null;
		String tzId = tz.getID();

		double longitude = (TZ_HOUR * tz.getRawOffset()) / ONE_HOUR;
		Location loc = new Location(TIMEZONE_PROVIDER);
		loc.setLongitude(longitude);

		// Compare IDs.
		for (int i = 0; i < mTimeZones.length; i++) {
			if (tzId.equals(mTimeZones[i])) {
				loc.setLatitude(mLatitudes[i]);
				loc.setLongitude(mLongitudes[i]);
				return loc;
			}
		}

		// Find nearest longitude.
		double delta;
		double deltaMin = Double.MAX_VALUE;
		for (int i = 0; i < mTimeZones.length; i++) {
			delta = Math.abs(longitude - mLongitudes[i]);
			if (delta < deltaMin) {
				deltaMin = delta;
				loc.setLatitude(mLatitudes[i]);
				loc.setLongitude(mLongitudes[i]);
			}
		}

		return loc;
	}
}

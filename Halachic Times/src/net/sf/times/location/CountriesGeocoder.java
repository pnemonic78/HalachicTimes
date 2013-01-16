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

import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.R;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;

/**
 * Maintains the lists of countries.
 * 
 * @author Moshe
 */
public class CountriesGeocoder {

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

	/** Factor to convert a fixed-point integer to double. */
	private static final double RATIO = 1e+6;

	/**
	 * Not physically possible for more than 20 countries to overlap each other.
	 */
	private static final int MAX_COUNTRIES_OVERLAP = 20;

	protected final Locale mLocale;
	private static CountryPolygon[] mCountryBorders;

	/**
	 * Constructs a new cities provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public CountriesGeocoder(Context context) {
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
	public CountriesGeocoder(Context context, Locale locale) {
		super();
		mLocale = locale;

		// Populate arrays from "countries.xml"
		Resources res = context.getResources();
		if (mCountryBorders == null) {
			String[] countryCodes = res.getStringArray(R.array.countries);
			int countriesCount = countryCodes.length;
			mCountryBorders = new CountryPolygon[countriesCount];
			int[] verticesCounts = res.getIntArray(R.array.vertices_count);
			int[] latitudes = res.getIntArray(R.array.latitudes);
			int[] longitudes = res.getIntArray(R.array.longitudes);
			int verticesCount;
			CountryPolygon country;
			int i = 0;

			for (int c = 0; c < countriesCount; c++) {
				verticesCount = verticesCounts[c];
				country = new CountryPolygon(countryCodes[c]);
				for (int v = 0; v < verticesCount; v++, i++) {
					country.addPoint(latitudes[i], longitudes[i]);
				}
				mCountryBorders[c] = country;
			}
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
		final int fixedpointLatitude = (int) Math.rint(latitude * RATIO);
		final int fixedpointLongitude = (int) Math.rint(longitude * RATIO);
		double distanceToBorder;
		double distanceMin = Double.MAX_VALUE;
		int found = -1;
		final int countriesSize = mCountryBorders.length;
		CountryPolygon country;
		int[] matches = new int[MAX_COUNTRIES_OVERLAP];
		int matchesCount = 0;

		for (int c = 0; (c < countriesSize) && (matchesCount < MAX_COUNTRIES_OVERLAP); c++) {
			country = mCountryBorders[c];
			if (country.containsBox(fixedpointLatitude, fixedpointLongitude))
				matches[matchesCount++] = c;
		}
		if (matchesCount == 0) {
			// Find the nearest border.
			for (int c = 0; c < countriesSize; c++) {
				country = mCountryBorders[c];
				distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
				if (distanceToBorder < distanceMin) {
					distanceMin = distanceToBorder;
					found = c;
				}
			}

			if (found < 0)
				return null;
		} else if (matchesCount == 1) {
			found = matches[0];
		} else {
			// Case 1: Smaller country inside a larger country.
			CountryPolygon other;
			country = mCountryBorders[matches[0]];
			int matchCountryIndex;
			for (int m = 1; m < matchesCount; m++) {
				matchCountryIndex = matches[m];
				other = mCountryBorders[matchCountryIndex];
				if (country.containsBox(other)) {
					country = other;
					found = matchCountryIndex;
				} else if ((found < 0) && other.containsBox(country)) {
					found = matches[0];
				}
			}

			// Case 2: Country rectangle intersects another country's rectangle.
			if (found < 0) {
				// Is the location inside of country#1 but actually outside
				// of country#2 ?
				// TODO implement me!

				// Find the nearest border.
				for (int m = 0; m < matchesCount; m++) {
					matchCountryIndex = matches[m];
					country = mCountryBorders[matchCountryIndex];
					distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
					if (distanceToBorder < distanceMin) {
						distanceMin = distanceToBorder;
						found = matchCountryIndex;
					}
				}
			}
		}

		Locale locale = new Locale(mLocale.getLanguage(), mCountryBorders[found].countryCode);
		ZmanimAddress city = new ZmanimAddress(locale);
		city.setId(-found);
		city.setLatitude(latitude);
		city.setLongitude(longitude);
		city.setCountryCode(locale.getCountry());
		city.setCountryName(locale.getDisplayCountry());

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
		Location loc = new Location(TIMEZONE_PROVIDER);
		if (tz != null) {
			double longitude = (TZ_HOUR * tz.getRawOffset()) / ONE_HOUR;
			loc.setLongitude(longitude);
		}
		return loc;
	}
}

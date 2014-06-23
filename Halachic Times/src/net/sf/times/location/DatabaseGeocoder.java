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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.times.ZmanimApplication;
import net.sf.times.database.CursorFilter;

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 * 
 * @author Moshe Waisberg
 */
public class DatabaseGeocoder extends GeocoderBase {

	private static final int INDEX_LOCATION_LATITUDE = 1;
	private static final int INDEX_LOCATION_LONGITUDE = 2;
	private static final int INDEX_LATITUDE = 3;
	private static final int INDEX_LONGITUDE = 4;
	private static final int INDEX_ELEVATION = 5;

	/**
	 * Creates a new database geocoder.
	 * 
	 * @param context
	 *            the context.
	 */
	public DatabaseGeocoder(Context context) {
		super(context);
	}

	/**
	 * Creates a new database geocoder.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public DatabaseGeocoder(Context context, Locale locale) {
		super(context, locale);
	}

	@Override
	public List<Address> getFromLocation(final double latitude, final double longitude, int maxResults) throws IOException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);

		final float[] distanceLocation = new float[1];
		final float[] distanceAddress = new float[1];

		CursorFilter filter = new CursorFilter() {

			@Override
			public boolean accept(Cursor cursor) {
				double locationLatitude = cursor.getDouble(INDEX_LOCATION_LATITUDE);
				double locationLongitude = cursor.getDouble(INDEX_LOCATION_LONGITUDE);
				Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceLocation);
				if (distanceLocation[0] <= SAME_LOCATION)
					return true;

				double addressLatitude = cursor.getDouble(INDEX_LATITUDE);
				double addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
				Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, distanceAddress);
				return (distanceAddress[0] <= SAME_LOCATION);
			}
		};
		ZmanimApplication app = (ZmanimApplication) mContext.getApplicationContext();
		AddressProvider provider = app.getAddresses();
		List<ZmanimAddress> q = provider.query(filter);
		List<Address> addresses = new ArrayList<Address>(q);

		return addresses;
	}

	@Override
	protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
		return null;
	}

	@Override
	public Location getElevation(final double latitude, final double longitude) throws IOException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);

		final float[] distanceLocation = new float[1];
		final float[] distanceAddress = new float[1];

		CursorFilter filter = new CursorFilter() {

			@Override
			public boolean accept(Cursor cursor) {
				if (cursor.isNull(INDEX_ELEVATION))
					return false;

				double locationLatitude = cursor.getDouble(INDEX_LOCATION_LATITUDE);
				double locationLongitude = cursor.getDouble(INDEX_LOCATION_LONGITUDE);
				Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceLocation);
				if (distanceLocation[0] <= SAME_PLATEAU)
					return true;

				double addressLatitude = cursor.getDouble(INDEX_LATITUDE);
				double addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
				Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, distanceAddress);
				return (distanceAddress[0] <= SAME_PLATEAU);
			}
		};
		ZmanimApplication app = (ZmanimApplication) mContext.getApplicationContext();
		AddressProvider provider = app.getAddresses();
		List<ZmanimAddress> addresses = provider.query(filter);

		int addressesCount = addresses.size();
		if (addressesCount == 0)
			return null;

		float distance;
		float[] distanceLoc = new float[1];
		double d;
		double distancesSum = 0;
		int n = 0;
		double[] distances = new double[addressesCount];
		double[] elevations = new double[addressesCount];
		Location elevated;

		for (ZmanimAddress loc : addresses) {
			Location.distanceBetween(latitude, longitude, loc.getLatitude(), loc.getLongitude(), distanceLoc);
			distance = distanceLoc[0];
			elevations[n] = loc.getElevation();
			d = distance * distance;
			distances[n] = d;
			distancesSum += d;
			n++;
		}

		if ((n == 1) && (distanceLoc[0] <= SAME_CITY)) {
			elevated = new Location(USER_PROVIDER);
			elevated.setTime(System.currentTimeMillis());
			elevated.setLatitude(latitude);
			elevated.setLongitude(longitude);
			elevated.setAltitude(elevations[0]);
			return elevated;
		}
		if (n <= 1)
			return null;

		double weightSum = 0;
		for (int i = 0; i < n; i++) {
			weightSum += (1 - (distances[i] / distancesSum)) * elevations[i];
		}

		elevated = new Location(USER_PROVIDER);
		elevated.setTime(System.currentTimeMillis());
		elevated.setLatitude(latitude);
		elevated.setLongitude(longitude);
		elevated.setAltitude(weightSum / (n - 1));
		return elevated;
	}

	@Override
	protected DefaultHandler createElevationResponseHandler(List<Location> results) {
		return null;
	}

}

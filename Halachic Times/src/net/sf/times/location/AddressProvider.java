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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.BaseColumns;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 * 
 * @author Moshe
 */
public class AddressProvider {

	private static final String[] COLUMNS = { BaseColumns._ID, AddressColumns.LOCATION_LATITUDE, AddressColumns.LOCATION_LONGITUDE, AddressColumns.LATITUDE,
			AddressColumns.LONGITUDE, AddressColumns.ADDRESS, AddressColumns.LANGUAGE };
	private static final int INDEX_ID = 0;
	private static final int INDEX_LOCATION_LATITUDE = 1;
	private static final int INDEX_LOCATION_LONGITUDE = 2;
	private static final int INDEX_LATITUDE = 3;
	private static final int INDEX_LONGITUDE = 4;
	private static final int INDEX_ADDRESS = 5;
	private static final int INDEX_LANGUAGE = 6;

	/** Maximum radius to consider two locations in the same vicinity. */
	private static final float SAME_LOCATION = 250f;// 250 meters.

	private final Context mContext;
	private final Locale mLocale;
	private SQLiteOpenHelper mOpenHelper;
	/** The list of countries. */
	private CountriesGeocoder mCountries;

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public AddressProvider(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public AddressProvider(Context context, Locale locale) {
		super();
		mContext = context;
		mLocale = locale;
		mCountries = new CountriesGeocoder(context, locale);
	}

	/**
	 * Find the nearest address of the location.
	 * 
	 * @param location
	 *            the location.
	 * @return the address.
	 */
	public Address findNearestAddress(Location location) {
		List<Address> addresses = null;

		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressDatabase(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGeocoder(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGoogle(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGeoNames(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestCountry(location);
		}

		return findBestAddress(location, addresses);
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the built-in Android {@link Geocoder} API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGeocoder(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		Geocoder geocoder = new Geocoder(mContext);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the Google Maps API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGoogle(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		GoogleGeocoder geocoder = new GoogleGeocoder(mContext, mLocale);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Finds the nearest street and address for a given lat/lng pair.
	 * <p>
	 * Uses the GeoNames API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGeoNames(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		GeoNamesGeocoder geocoder = new GeoNamesGeocoder(mContext, mLocale);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			e.printStackTrace();
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Find the best address by checking relevant fields.
	 * 
	 * @param location
	 *            the location.
	 * @param addresses
	 *            the list of addresses.
	 * @return the best address.
	 */
	private Address findBestAddress(Location location, List<Address> addresses) {
		if ((addresses == null) || addresses.isEmpty())
			return null;
		if (addresses.size() == 1)
			return addresses.get(0);

		// First, find the closest location.
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		float distanceMin = Float.MAX_VALUE;
		Address addrMin = null;
		float[] distances = new float[1];
		for (Address a : addresses) {
			Location.distanceBetween(latitude, longitude, a.getLatitude(), a.getLongitude(), distances);
			if (distances[0] <= distanceMin) {
				distanceMin = distances[0];
				addrMin = a;
			}
		}
		if (addrMin != null)
			return addrMin;

		// Next, find the best address part.
		for (Address a : addresses) {
			if (a.getFeatureName() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getCountryName() != null)
				return a;
		}
		return addresses.get(0);
	}

	/**
	 * Format the address.
	 * 
	 * @param a
	 *            the address.
	 * @return the formatted address name.
	 */
	public static String formatAddress(ZmanimAddress a) {
		return a.getFormatted();
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the local database.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressDatabase(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		final String language = mLocale.getLanguage();

		List<Address> addresses = new ArrayList<Address>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(AddressOpenHelper.TABLE_ADDRESSES, COLUMNS, null, null, null, null, null);
		if (cursor == null) {
			db.close();
			return addresses;
		}

		if (cursor.moveToFirst()) {
			long id;
			double locationLatitude;
			double locationLongitude;
			double addressLatitude;
			double addressLongitude;
			String formatted;
			String locationLanguage;
			Locale locale;
			ZmanimAddress address;
			float[] distanceLocation = new float[1];
			float[] distanceAddress = new float[1];

			do {
				locationLatitude = cursor.getDouble(INDEX_LOCATION_LATITUDE);
				locationLongitude = cursor.getDouble(INDEX_LOCATION_LONGITUDE);
				addressLatitude = cursor.getDouble(INDEX_LATITUDE);
				addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
				locationLanguage = cursor.getString(INDEX_LANGUAGE);

				Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceLocation);
				Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceAddress);
				final boolean sameLocation = (distanceLocation[0] <= SAME_LOCATION) || (distanceAddress[0] <= SAME_LOCATION);
				final boolean sameLanguage = (locationLanguage == null) || locationLanguage.equals(language);
				if (sameLocation && sameLanguage) {
					id = cursor.getLong(INDEX_ID);
					formatted = cursor.getString(INDEX_ADDRESS);
					if (locationLanguage == null)
						locale = mLocale;
					else
						locale = new Locale(locationLanguage, mLocale.getCountry());
					address = new ZmanimAddress(locale);
					address.setFormatted(formatted);
					address.setId(id);
					address.setLatitude(addressLatitude);
					address.setLongitude(addressLongitude);
					addresses.add(address);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();

		return addresses;
	}

	/**
	 * Get the readable addresses database.
	 * 
	 * @return the database - {@code null} otherwise.
	 */
	private SQLiteDatabase getReadableDatabase() {
		if (mOpenHelper == null)
			mOpenHelper = new AddressOpenHelper(mContext);
		try {
			return mOpenHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the writable addresses database.
	 * 
	 * @return the database - {@code null} otherwise.
	 */
	private SQLiteDatabase getWritableDatabase() {
		if (mOpenHelper == null)
			mOpenHelper = new AddressOpenHelper(mContext);
		try {
			return mOpenHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Add the address to the local database. The local database is supposed to
	 * reduce redundant network requests.
	 * 
	 * @param location
	 *            the location.
	 * @param address
	 *            the address.
	 */
	public void insertAddress(Location location, ZmanimAddress address) {
		if (address.getId() != 0)
			return;
		ContentValues values = new ContentValues();
		values.put(AddressColumns.ADDRESS, formatAddress(address));
		values.put(AddressColumns.LANGUAGE, address.getLocale().getLanguage());
		values.put(AddressColumns.LATITUDE, address.getLatitude());
		values.put(AddressColumns.LOCATION_LATITUDE, location.getLatitude());
		values.put(AddressColumns.LOCATION_LONGITUDE, location.getLongitude());
		values.put(AddressColumns.LONGITUDE, address.getLongitude());
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.insert(AddressOpenHelper.TABLE_ADDRESSES, null, values);
		} finally {
			if (db != null)
				db.close();
		}
	}

	/** Close database resources. */
	public void close() {
		if (mOpenHelper != null)
			mOpenHelper.close();
	}

	/**
	 * Find the nearest country to the latitude and longitude.
	 * <p>
	 * Uses the precompiled array of countries from GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses with 1 entry.
	 */
	private List<Address> findNearestCountry(Location location) {
		List<Address> countries = new ArrayList<Address>();
		Address country = mCountries.getFromLocation(location);
		if (country != null)
			countries.add(country);
		return countries;
	}
}

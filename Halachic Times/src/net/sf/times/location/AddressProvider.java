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

import net.sf.times.database.CursorFilter;
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
import android.util.Log;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 * 
 * @author Moshe Waisberg
 */
public class AddressProvider {
	private static final String TAG = "AddressProvider";

	public static interface OnFindAddressListener {

		/**
		 * Called when an address is found.
		 * 
		 * @param provider
		 *            the address provider.
		 * @param location
		 *            the requested location.
		 * @param address
		 *            the found address.
		 */
		public void onFindAddress(AddressProvider provider, Location location, Address address);

	}

	/** Database provider. */
	public static final String DB_PROVIDER = "db";

	private static final String[] COLUMNS = { BaseColumns._ID, AddressColumns.LOCATION_LATITUDE, AddressColumns.LOCATION_LONGITUDE, AddressColumns.LATITUDE,
			AddressColumns.LONGITUDE, AddressColumns.ADDRESS, AddressColumns.LANGUAGE, AddressColumns.FAVORITE };
	static final int INDEX_ID = 0;
	static final int INDEX_LOCATION_LATITUDE = 1;
	static final int INDEX_LOCATION_LONGITUDE = 2;
	static final int INDEX_LATITUDE = 3;
	static final int INDEX_LONGITUDE = 4;
	static final int INDEX_ADDRESS = 6;
	static final int INDEX_LANGUAGE = 7;
	static final int INDEX_FAVORITE = 8;

	private static final String[] COLUMNS_ELEVATIONS = { BaseColumns._ID, ElevationColumns.LATITUDE, ElevationColumns.LONGITUDE, ElevationColumns.ELEVATION,
			ElevationColumns.TIMESTAMP };
	static final int INDEX_ELEVATIONS_ID = 0;
	static final int INDEX_ELEVATIONS_LATITUDE = 1;
	static final int INDEX_ELEVATIONS_LONGITUDE = 2;
	static final int INDEX_ELEVATIONS_ELEVATION = 3;
	static final int INDEX_ELEVATIONS_TIMESTAMP = 4;

	private static final String WHERE_ID = BaseColumns._ID + "=?";

	private final Context mContext;
	private final Locale mLocale;
	private SQLiteOpenHelper mOpenHelper;
	/** The list of countries. */
	private CountriesGeocoder mCountriesGeocoder;
	private Geocoder mGeocoder;
	private GeocoderBase mGoogleGeocoder;
	private GeocoderBase mBingGeocoder;
	private GeocoderBase mGeoNamesGeocoder;
	private GeocoderBase mDatabaseGeocoder;

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
		mCountriesGeocoder = new CountriesGeocoder(context, locale);
	}

	/**
	 * Find the nearest address of the location.
	 * 
	 * @param location
	 *            the location.
	 * @param listener
	 *            the listener.
	 * @return the address.
	 */
	public Address findNearestAddress(Location location, OnFindAddressListener listener) {
		if (location == null)
			return null;
		final double latitude = location.getLatitude();
		if ((latitude > 90) || (latitude < -90))
			return null;
		final double longitude = location.getLongitude();
		if ((longitude > 180) || (longitude < -180))
			return null;

		List<Address> addresses;
		Address best = null;
		Address bestCountry;
		Address bestCity;

		if (listener != null)
			listener.onFindAddress(this, location, best);

		addresses = findNearestCountry(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);
		bestCountry = best;

		addresses = findNearestCity(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);
		bestCity = best;

		addresses = findNearestAddressDatabase(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);

		if (best == null) {
			addresses = findNearestAddressGeocoder(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressGoogle(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressBing(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressGeoNames(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			best = bestCity;
		}
		if (best == null) {
			best = bestCountry;
		}

		return best;
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
		List<Address> addresses = null;
		Geocoder geocoder = mGeocoder;
		if (geocoder == null) {
			geocoder = new Geocoder(mContext);
			mGeocoder = geocoder;
		}
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "Geocoder: " + e.getLocalizedMessage(), e);
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
		List<Address> addresses = null;
		GeocoderBase geocoder = mGoogleGeocoder;
		if (geocoder == null) {
			geocoder = new GoogleGeocoder(mContext, mLocale);
			mGoogleGeocoder = geocoder;
		}
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage(), e);
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
		List<Address> addresses = null;
		GeocoderBase geocoder = mGeoNamesGeocoder;
		if (geocoder == null) {
			geocoder = new GeoNamesGeocoder(mContext, mLocale);
			mGeoNamesGeocoder = geocoder;
		}
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Finds the nearest street and address for a given lat/lng pair.
	 * <p>
	 * Uses the Bing API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressBing(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = mBingGeocoder;
		if (geocoder == null) {
			geocoder = new BingGeocoder(mContext, mLocale);
			mBingGeocoder = geocoder;
		}
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage(), e);
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
			if (!a.hasLatitude())
				continue;
			if (!a.hasLongitude())
				continue;
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
		List<Address> addresses = null;
		GeocoderBase geocoder = mDatabaseGeocoder;
		if (geocoder == null) {
			geocoder = new DatabaseGeocoder(mContext, mLocale);
			mDatabaseGeocoder = geocoder;
		}
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage(), e);
		}
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
			Log.e(TAG, "no readable db", e);
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
			Log.e(TAG, "no writable db", e);
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
	public void insertOrUpdate(Location location, ZmanimAddress address) {
		if (address == null)
			return;
		long id = address.getId();
		if (id < 0L)
			return;

		ContentValues values = new ContentValues();
		if (location == null) {
			values.put(AddressColumns.LOCATION_LATITUDE, address.getLatitude());
			values.put(AddressColumns.LOCATION_LONGITUDE, address.getLongitude());
		} else {
			values.put(AddressColumns.LOCATION_LATITUDE, location.getLatitude());
			values.put(AddressColumns.LOCATION_LONGITUDE, location.getLongitude());
		}
		values.put(AddressColumns.ADDRESS, formatAddress(address));
		values.put(AddressColumns.LANGUAGE, address.getLocale().getLanguage());
		values.put(AddressColumns.LATITUDE, address.getLatitude());
		values.put(AddressColumns.LONGITUDE, address.getLongitude());
		values.put(AddressColumns.TIMESTAMP, System.currentTimeMillis());
		values.put(AddressColumns.FAVORITE, address.isFavorite());

		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db == null)
				return;
			if (id == 0L) {
				id = db.insert(AddressOpenHelper.TABLE_ADDRESSES, null, values);
				address.setId(id);
			} else {
				String[] whereArgs = { Long.toString(id) };
				db.update(AddressOpenHelper.TABLE_ADDRESSES, values, WHERE_ID, whereArgs);
			}
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
	 * Uses the pre-compiled array of countries from GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses with at most 1 entry.
	 */
	private List<Address> findNearestCountry(Location location) {
		List<Address> countries = null;
		Address country = mCountriesGeocoder.findCountry(location);
		if (country != null) {
			countries = new ArrayList<Address>();
			countries.add(country);
		}
		return countries;
	}

	/**
	 * Find the nearest city to the latitude and longitude.
	 * <p>
	 * Uses the pre-compiled array of cities from GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses with at most 1 entry.
	 */
	private List<Address> findNearestCity(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = mCountriesGeocoder;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			Log.e(TAG, "City: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Fetch addresses from the database.
	 * 
	 * @param filter
	 *            a cursor filter.
	 * @return the list of addresses.
	 */
	public List<ZmanimAddress> query(CursorFilter filter) {
		final String language = mLocale.getLanguage();
		final String country = mLocale.getCountry();

		List<ZmanimAddress> addresses = new ArrayList<ZmanimAddress>();
		SQLiteDatabase db = getReadableDatabase();
		if (db == null)
			return addresses;
		Cursor cursor = db.query(AddressOpenHelper.TABLE_ADDRESSES, COLUMNS, null, null, null, null, null);
		if ((cursor == null) || cursor.isClosed()) {
			db.close();
			return addresses;
		}

		try {
			if (cursor.moveToFirst()) {
				long id;
				double addressLatitude;
				double addressLongitude;
				String formatted;
				String locationLanguage;
				Locale locale;
				ZmanimAddress address;
				boolean favorite;

				do {
					locationLanguage = cursor.getString(INDEX_LANGUAGE);
					if ((locationLanguage == null) || locationLanguage.equals(language)) {
						if ((filter != null) && !filter.accept(cursor))
							continue;

						addressLatitude = cursor.getDouble(INDEX_LATITUDE);
						addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
						id = cursor.getLong(INDEX_ID);
						formatted = cursor.getString(INDEX_ADDRESS);
						favorite = cursor.getShort(INDEX_FAVORITE) != 0;
						if (locationLanguage == null)
							locale = mLocale;
						else
							locale = new Locale(locationLanguage, country);

						address = new ZmanimAddress(locale);
						address.setFormatted(formatted);
						address.setId(id);
						address.setLatitude(addressLatitude);
						address.setLongitude(addressLongitude);
						address.setFavorite(favorite);
						addresses.add(address);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException se) {
			Log.e(TAG, "Query addresses: " + se.getLocalizedMessage(), se);
		} finally {
			cursor.close();
			db.close();
		}

		return addresses;
	}

	/**
	 * Find the elevation (altitude).
	 * 
	 * @param location
	 *            the location.
	 * @return the elevated location - {@code null} otherwise.
	 */
	public Location findElevation(Location location) {
		if (location.hasAltitude())
			return location;

		Location elevated;

		elevated = findElevationCities(location);
		if ((elevated != null) && elevated.hasAltitude())
			return elevated;
		elevated = findElevationDatabase(location);
		if ((elevated != null) && elevated.hasAltitude())
			return elevated;
		elevated = findElevationGoogle(location);
		if ((elevated != null) && elevated.hasAltitude())
			return elevated;
		elevated = findElevationBing(location);
		if ((elevated != null) && elevated.hasAltitude())
			return elevated;
		elevated = findElevationGeoNames(location);
		if ((elevated != null) && elevated.hasAltitude())
			return elevated;

		return null;
	}

	/**
	 * Find elevation of nearest cities. Calculates the average elevation of
	 * neighbouring cities if more than {@code 1} is found.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevated location - {@code null} otherwise.
	 */
	private Location findElevationCities(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		try {
			return mCountriesGeocoder.getElevation(latitude, longitude);
		} catch (IOException e) {
			Log.e(TAG, "Countries geocoder: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * Find elevation according to Google Maps.
	 * 
	 * @param location
	 *            the location.
	 * @return the location with elevation - {@code null} otherwise.
	 */
	private Location findElevationGoogle(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		GeocoderBase geocoder = mGoogleGeocoder;
		if (geocoder == null) {
			geocoder = new GoogleGeocoder(mContext, mLocale);
			mGoogleGeocoder = geocoder;
		}
		try {
			return geocoder.getElevation(latitude, longitude);
		} catch (IOException e) {
			Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * Find elevation according to GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevated location - {@code null} otherwise.
	 */
	private Location findElevationGeoNames(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		GeocoderBase geocoder = mGeoNamesGeocoder;
		if (geocoder == null) {
			geocoder = new GeoNamesGeocoder(mContext, mLocale);
			mGeoNamesGeocoder = geocoder;
		}
		try {
			return geocoder.getElevation(latitude, longitude);
		} catch (IOException e) {
			Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * Find elevation according to Bing.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevated location - {@code null} otherwise.
	 */
	private Location findElevationBing(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		GeocoderBase geocoder = mBingGeocoder;
		if (geocoder == null) {
			geocoder = new BingGeocoder(mContext, mLocale);
			mBingGeocoder = geocoder;
		}
		try {
			return geocoder.getElevation(latitude, longitude);
		} catch (IOException e) {
			Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * Find elevation according to Bing.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevated location - {@code null} otherwise.
	 */
	private Location findElevationDatabase(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		GeocoderBase geocoder = mDatabaseGeocoder;
		if (geocoder == null) {
			geocoder = new DatabaseGeocoder(mContext, mLocale);
			mDatabaseGeocoder = geocoder;
		}
		try {
			return geocoder.getElevation(latitude, longitude);
		} catch (IOException e) {
			Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * Add the location with elevation to the local database. The local database
	 * is supposed to reduce redundant network requests.
	 * 
	 * @param location
	 *            the location.
	 */
	public void insertOrUpdate(ZmanimLocation location) {
		if ((location == null) || !location.hasAltitude())
			return;
		long id = location.getId();
		if (id < 0L)
			return;

		ContentValues values = new ContentValues();
		values.put(ElevationColumns.LATITUDE, location.getLatitude());
		values.put(ElevationColumns.LONGITUDE, location.getLongitude());
		values.put(ElevationColumns.ELEVATION, location.getAltitude());
		values.put(ElevationColumns.TIMESTAMP, System.currentTimeMillis());

		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db == null)
				return;
			if (id == 0L) {
				id = db.insert(AddressOpenHelper.TABLE_ELEVATIONS, null, values);
				location.setId(id);
			} else {
				String[] whereArgs = { Long.toString(id) };
				db.update(AddressOpenHelper.TABLE_ELEVATIONS, values, WHERE_ID, whereArgs);
			}
		} finally {
			if (db != null)
				db.close();
		}
	}

	/**
	 * Fetch elevations from the database.
	 * 
	 * @param filter
	 *            a cursor filter.
	 * @return the list of locations with elevations.
	 */
	public List<ZmanimLocation> queryElevations(CursorFilter filter) {
		List<ZmanimLocation> locations = new ArrayList<ZmanimLocation>();
		SQLiteDatabase db = getReadableDatabase();
		if (db == null)
			return locations;
		Cursor cursor = db.query(AddressOpenHelper.TABLE_ELEVATIONS, COLUMNS_ELEVATIONS, null, null, null, null, null);
		if ((cursor == null) || cursor.isClosed()) {
			db.close();
			return locations;
		}

		try {
			if (cursor.moveToFirst()) {
				ZmanimLocation location;

				do {
					if ((filter != null) && !filter.accept(cursor))
						continue;

					location = new ZmanimLocation(DB_PROVIDER);
					location.setId(cursor.getLong(INDEX_ELEVATIONS_ID));
					location.setLatitude(cursor.getDouble(INDEX_ELEVATIONS_LATITUDE));
					location.setLongitude(cursor.getDouble(INDEX_ELEVATIONS_LONGITUDE));
					location.setAltitude(cursor.getDouble(INDEX_ELEVATIONS_ELEVATION));
					location.setTime(cursor.getLong(INDEX_ELEVATIONS_TIMESTAMP));
					locations.add(location);
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException se) {
			Log.e(TAG, "Query elevations: " + se.getLocalizedMessage(), se);
		} finally {
			cursor.close();
			db.close();
		}

		return locations;
	}

}

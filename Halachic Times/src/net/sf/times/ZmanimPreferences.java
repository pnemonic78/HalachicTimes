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
package net.sf.times;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

/**
 * Application preferences.
 * 
 * @author Moshe
 */
public class ZmanimPreferences {

	/** Desired preferences file. */
	private static final String NAME = "zmanim";

	/** Preference name for the latitude. */
	private static final String KEY_LATITUDE = "latitude";
	/** Preference name for the longitude. */
	private static final String KEY_LONGITUDE = "longitude";
	/** Preference key for the altitude. */
	private static final String KEY_ALTITUDE = "altitude";
	/** Preference name for the location provider. */
	private static final String KEY_PROVIDER = "provider";
	/** Preference name for the location time. */
	private static final String KEY_TIME = "time";

	private final SharedPreferences mData;

	/**
	 * Constructs a new preferences.
	 */
	public ZmanimPreferences(Context context) {
		super();
		mData = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

	/**
	 * Get the data.
	 * 
	 * @return the shared preferences.
	 */
	public SharedPreferences getData() {
		return mData;
	}

	/**
	 * Get the editor to modify the preferences data.
	 * 
	 * @return the editor.
	 */
	public SharedPreferences.Editor edit() {
		return mData.edit();
	}

	/**
	 * Get the location.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocation() {
		if (!mData.contains(KEY_LATITUDE))
			return null;
		if (!mData.contains(KEY_LONGITUDE))
			return null;
		double latitude;
		double longitude;
		double altitude;
		try {
			latitude = Double.parseDouble(mData.getString(KEY_LATITUDE, "0"));
			longitude = Double.parseDouble(mData.getString(KEY_LONGITUDE, "0"));
			altitude = Double.parseDouble(mData.getString(KEY_ALTITUDE, "0"));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return null;
		}
		String provider = mData.getString(KEY_PROVIDER, "");
		Location location = new Location(provider);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setAltitude(altitude);
		location.setTime(mData.getLong(KEY_TIME, 0));
		return location;
	}

	/**
	 * Set the location.
	 * 
	 * @return the location.
	 */
	public void putLocation(Location location) {
		Editor editor = mData.edit();
		editor.putString(KEY_PROVIDER, location.getProvider());
		editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
		editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
		editor.putString(KEY_ALTITUDE, Double.toString(location.getAltitude()));
		editor.putLong(KEY_TIME, location.getTime());
		editor.commit();
	}
}

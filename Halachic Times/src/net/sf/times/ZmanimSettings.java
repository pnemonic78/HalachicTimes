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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

/**
 * Application settings.
 * 
 * @author Moshe
 */
public class ZmanimSettings {

	/** Desired preferences file. */
	private static final String NAME = "net.sf.times_preferences";

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
	/** Preference name for the co-ordinates format. */
	public static final String KEY_COORDS_FORMAT = "coords.format";
	/** Preference name for showing summaries. */
	public static final String KEY_SUMMARIES = "summaries.visible";
	/** Preference name for enabling past times. */
	public static final String KEY_PAST = "past";
	/** Preference name for the background gradient. */
	public static final String KEY_BG_GRADIENT = "gradient";
	/** Preference name for Alos type. */
	public static final String KEY_OPINION_DAWN = "dawn";
	/** Preference name for sunrise type. */
	public static final String KEY_OPINION_SUNRISE = "sunrise";
	/** Preference name for Last Shema type. */
	public static final String KEY_OPINION_SHEMA = "shema";
	/** Preference name for midday / noon type. */
	public static final String KEY_OPINION_NOON = "midday";
	/** Preference name for candle lighting minutes offset. */
	public static final String KEY_OPINION_CANDLES = "candles";
	/** Preference name for sunset type. */
	public static final String KEY_OPINION_SUNSET = "sunset";
	/** Preference name for midnight type. */
	public static final String KEY_OPINION_MIDNIGHT = "midnight";

	/** Format the coordinates in decimal notation. */
	public static final String FORMAT_DECIMAL = "decimal";
	/** Format the coordinates in sexagesimal notation. */
	public static final String FORMAT_SEXIGESIMAL = "sexagesimal";

	private final SharedPreferences mData;

	/**
	 * Constructs a new settings.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimSettings(Context context) {
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

	/**
	 * Get the notation of latitude and longitude.
	 * 
	 * @return the format.
	 */
	public String getCoordinatesFormat() {
		return mData.getString(KEY_COORDS_FORMAT, FORMAT_DECIMAL);
	}

	/**
	 * Are summaries visible?
	 * 
	 * @return {@code true} to show summaries.
	 */
	public boolean isSummaries() {
		return mData.getBoolean(KEY_SUMMARIES, true);
	}

	/**
	 * Are past times enabled?
	 * 
	 * @return {@code true} if older times are not grayed.
	 */
	public boolean isPast() {
		return mData.getBoolean(KEY_PAST, false);
	}

	/**
	 * Is background gradient visible?
	 * 
	 * @return {@code true} to show gradient.
	 */
	public boolean isBackgroundGradient() {
		return mData.getBoolean(KEY_BG_GRADIENT, true);
	}

	/**
	 * Get the offset in minutes before sunset which is used in calculating
	 * candle lighting time.
	 * 
	 * @return the number of minutes.
	 */
	public int getCandleLightingOffset() {
		return mData.getInt(KEY_OPINION_CANDLES, 18);
	}

	/**
	 * Get the opinion for dawn (<em>alos</em>).
	 * 
	 * @return the opinion.
	 */
	public String getDawn() {
		return mData.getString(KEY_OPINION_DAWN, "16.1");
	}

	/**
	 * Get the opinion for sunrise.
	 * 
	 * @return the opinion.
	 */
	public String getSunrise() {
		return mData.getString(KEY_OPINION_SUNRISE, "");
	}

	/**
	 * Get the opinion for the last shema (<em>sof zman shma</em>).
	 * 
	 * @return the opinion.
	 */
	public String getLastShema() {
		return mData.getString(KEY_OPINION_SHEMA, "MGA");
	}

	/**
	 * Get the opinion for noon (<em>chatzos</em>).
	 * 
	 * @return the opinion.
	 */
	public String getMidday() {
		return mData.getString(KEY_OPINION_NOON, "");
	}

	/**
	 * Get the opinion for sunset.
	 * 
	 * @return the opinion.
	 */
	public String getSunset() {
		return mData.getString(KEY_OPINION_SUNSET, "");
	}

	/**
	 * Get the opinion for midnight (<em>chatzos layla</em>).
	 * 
	 * @return the opinion.
	 */
	public String getMidnight() {
		return mData.getString(KEY_OPINION_MIDNIGHT, "");
	}
}

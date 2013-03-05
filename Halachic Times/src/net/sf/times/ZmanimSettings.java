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
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Application settings.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimSettings {

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
	/** Preference name for the co-ordinates visibility. */
	public static final String KEY_COORDS = "coords.visible";
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
	/** Preference name for earliest tallis type. */
	public static final String KEY_OPINION_TALLIS = "tallis";
	/** Preference name for sunrise type. */
	public static final String KEY_OPINION_SUNRISE = "sunrise";
	/** Preference name for Last Shema type. */
	public static final String KEY_OPINION_SHEMA = "shema";
	/** Preference name for Last Morning Tfila type. */
	public static final String KEY_OPINION_TFILA = "prayers";
	/** Preference name for midday / noon type. */
	public static final String KEY_OPINION_NOON = "midday";
	/** Preference name for Earliest Mincha type. */
	public static final String KEY_OPINION_EARLIEST_MINCHA = "earliest_mincha";
	/** Preference name for Mincha Ketana type. */
	public static final String KEY_OPINION_MINCHA = "mincha";
	/** Preference name for Plug HaMincha type. */
	public static final String KEY_OPINION_PLUG_MINCHA = "plug_hamincha";
	/** Preference name for candle lighting minutes offset. */
	public static final String KEY_OPINION_CANDLES = "candles";
	/** Preference name for sunset type. */
	public static final String KEY_OPINION_SUNSET = "sunset";
	/** Preference name for nightfall type. */
	public static final String KEY_OPINION_NIGHTFALL = "nightfall";
	/** Preference name for midnight type. */
	public static final String KEY_OPINION_MIDNIGHT = "midnight";

	static final String REMINDER_SUFFIX = ".reminder";

	/** Preference name for Alos reminder. */
	public static final String KEY_REMINDER_DAWN = KEY_OPINION_DAWN + REMINDER_SUFFIX;
	/** Preference name for earliest tallis reminder. */
	public static final String KEY_REMINDER_TALLIS = KEY_OPINION_TALLIS + REMINDER_SUFFIX;
	/** Preference name for sunrise reminder. */
	public static final String KEY_REMINDER_SUNRISE = KEY_OPINION_SUNRISE + REMINDER_SUFFIX;
	/** Preference name for Last Shema reminder. */
	public static final String KEY_REMINDER_SHEMA = KEY_OPINION_SHEMA + REMINDER_SUFFIX;
	/** Preference name for Last Morning Tfila reminder. */
	public static final String KEY_REMINDER_TFILA = KEY_OPINION_TFILA + REMINDER_SUFFIX;
	/** Preference name for midday / noon reminder. */
	public static final String KEY_REMINDER_NOON = KEY_OPINION_NOON + REMINDER_SUFFIX;
	/** Preference name for Earliest Mincha reminder. */
	public static final String KEY_REMINDER_EARLIEST_MINCHA = KEY_OPINION_EARLIEST_MINCHA + REMINDER_SUFFIX;
	/** Preference name for Mincha Ketana reminder. */
	public static final String KEY_REMINDER_MINCHA = KEY_OPINION_MINCHA + REMINDER_SUFFIX;
	/** Preference name for Plug HaMincha reminder. */
	public static final String KEY_REMINDER_PLUG_MINCHA = KEY_OPINION_PLUG_MINCHA + REMINDER_SUFFIX;
	/** Preference name for candle lighting reminder. */
	public static final String KEY_REMINDER_CANDLES = KEY_OPINION_CANDLES + REMINDER_SUFFIX;
	/** Preference name for sunset reminder. */
	public static final String KEY_REMINDER_SUNSET = KEY_OPINION_SUNSET + REMINDER_SUFFIX;
	/** Preference name for nightfall reminder. */
	public static final String KEY_REMINDER_NIGHTFALL = KEY_OPINION_NIGHTFALL + REMINDER_SUFFIX;
	/** Preference name for midnight reminder. */
	public static final String KEY_REMINDER_MIDNIGHT = KEY_OPINION_MIDNIGHT + REMINDER_SUFFIX;

	/** Format the coordinates in decimal notation. */
	public static final String FORMAT_DECIMAL = "decimal";
	/** Format the coordinates in sexagesimal notation. */
	public static final String FORMAT_SEXIGESIMAL = "sexagesimal";

	private final SharedPreferences mPrefs;

	/**
	 * Constructs a new settings.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimSettings(Context context) {
		super();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Get the data.
	 * 
	 * @return the shared preferences.
	 */
	public SharedPreferences getData() {
		return mPrefs;
	}

	/**
	 * Get the editor to modify the preferences data.
	 * 
	 * @return the editor.
	 */
	public SharedPreferences.Editor edit() {
		return mPrefs.edit();
	}

	/**
	 * Get the location.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocation() {
		if (!mPrefs.contains(KEY_LATITUDE))
			return null;
		if (!mPrefs.contains(KEY_LONGITUDE))
			return null;
		double latitude;
		double longitude;
		double altitude;
		try {
			latitude = Double.parseDouble(mPrefs.getString(KEY_LATITUDE, "0"));
			longitude = Double.parseDouble(mPrefs.getString(KEY_LONGITUDE, "0"));
			altitude = Double.parseDouble(mPrefs.getString(KEY_ALTITUDE, "0"));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return null;
		}
		String provider = mPrefs.getString(KEY_PROVIDER, "");
		Location location = new Location(provider);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setAltitude(altitude);
		location.setTime(mPrefs.getLong(KEY_TIME, 0));
		return location;
	}

	/**
	 * Set the location.
	 * 
	 * @return the location.
	 */
	public void putLocation(Location location) {
		Editor editor = mPrefs.edit();
		editor.putString(KEY_PROVIDER, location.getProvider());
		editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
		editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
		editor.putString(KEY_ALTITUDE, Double.toString(location.getAltitude()));
		editor.putLong(KEY_TIME, location.getTime());
		editor.commit();
	}

	/**
	 * Are coordinates visible?
	 * 
	 * @return {@code true} to show coordinates.
	 */
	public boolean isCoordinates() {
		return mPrefs.getBoolean(KEY_COORDS, true);
	}

	/**
	 * Get the notation of latitude and longitude.
	 * 
	 * @return the format.
	 */
	public String getCoordinatesFormat() {
		return mPrefs.getString(KEY_COORDS_FORMAT, FORMAT_DECIMAL);
	}

	/**
	 * Are summaries visible?
	 * 
	 * @return {@code true} to show summaries.
	 */
	public boolean isSummaries() {
		return mPrefs.getBoolean(KEY_SUMMARIES, true);
	}

	/**
	 * Are past times enabled?
	 * 
	 * @return {@code true} if older times are not grayed.
	 */
	public boolean isPast() {
		return mPrefs.getBoolean(KEY_PAST, false);
	}

	/**
	 * Is background gradient visible?
	 * 
	 * @return {@code true} to show gradient.
	 */
	public boolean isBackgroundGradient() {
		return mPrefs.getBoolean(KEY_BG_GRADIENT, true);
	}

	/**
	 * Get the offset in minutes before sunset which is used in calculating
	 * candle lighting time.
	 * 
	 * @return the number of minutes.
	 */
	public int getCandleLightingOffset() {
		return mPrefs.getInt(KEY_OPINION_CANDLES, 18);
	}

	/**
	 * Get the opinion for dawn (<em>alos</em>).
	 * 
	 * @return the opinion.
	 */
	public String getDawn() {
		return mPrefs.getString(KEY_OPINION_DAWN, "16.1");
	}

	/**
	 * Get the opinion for earliest tallis &amp; tefillin (<em>misheyakir</em>).
	 * 
	 * @return the opinion.
	 */
	public String getTallis() {
		return mPrefs.getString(KEY_OPINION_TALLIS, "");
	}

	/**
	 * Get the opinion for sunrise.
	 * 
	 * @return the opinion.
	 */
	public String getSunrise() {
		return mPrefs.getString(KEY_OPINION_SUNRISE, "");
	}

	/**
	 * Get the opinion for the last shema (<em>sof zman shma</em>).
	 * 
	 * @return the opinion.
	 */
	public String getLastShema() {
		return mPrefs.getString(KEY_OPINION_SHEMA, "MGA");
	}

	/**
	 * Get the opinion for the last morning prayers (<em>sof zman tfila</em>).
	 * 
	 * @return the opinion.
	 */
	public String getLastTfila() {
		return mPrefs.getString(KEY_OPINION_TFILA, "MGA");
	}

	/**
	 * Get the opinion for noon (<em>chatzos</em>).
	 * 
	 * @return the opinion.
	 */
	public String getMidday() {
		return mPrefs.getString(KEY_OPINION_NOON, "");
	}

	/**
	 * Get the opinion for earliest afternoon prayers (<em>mincha gedola</em>).
	 * 
	 * @return the opinion.
	 */
	public String getEarliestMincha() {
		return mPrefs.getString(KEY_OPINION_EARLIEST_MINCHA, "");
	}

	/**
	 * Get the opinion for afternoon prayers (<em>mincha ketana</em>).
	 * 
	 * @return the opinion.
	 */
	public String getMincha() {
		return mPrefs.getString(KEY_OPINION_MINCHA, "");
	}

	/**
	 * Get the opinion for afternoon prayers (<em>plag hamincha</em>).
	 * 
	 * @return the opinion.
	 */
	public String getPlugHamincha() {
		return mPrefs.getString(KEY_OPINION_PLUG_MINCHA, "");
	}

	/**
	 * Get the opinion for sunset.
	 * 
	 * @return the opinion.
	 */
	public String getSunset() {
		return mPrefs.getString(KEY_OPINION_SUNSET, "");
	}

	/**
	 * Get the opinion for nightfall.
	 * 
	 * @return the opinion.
	 */
	public String getNightfall() {
		return mPrefs.getString(KEY_OPINION_NIGHTFALL, "");
	}

	/**
	 * Get the opinion for midnight (<em>chatzos layla</em>).
	 * 
	 * @return the opinion.
	 */
	public String getMidnight() {
		return mPrefs.getString(KEY_OPINION_MIDNIGHT, "");
	}

	/**
	 * Get the opinion.
	 * 
	 * @param id
	 *            the time id.
	 * @return the opinion.
	 */
	public String getOpinion(int id) {
		if ((id == R.id.dawn_row) || (id == R.id.dawn_time) || (id == R.string.dawn))
			return getDawn();
		if ((id == R.id.tallis_row) || (id == R.id.tallis_time) || (id == R.string.tallis))
			return getTallis();
		if ((id == R.id.sunrise_row) || (id == R.id.sunrise_time) || (id == R.string.sunrise))
			return getSunrise();
		if ((id == R.id.shema_row) || (id == R.id.shema_time) || (id == R.string.shema))
			return getLastShema();
		if ((id == R.id.prayers_row) || (id == R.id.prayers_time) || (id == R.string.prayers))
			return getLastTfila();
		if ((id == R.id.midday_row) || (id == R.id.midday_time) || (id == R.string.midday))
			return getMidday();
		if ((id == R.id.earliest_mincha_row) || (id == R.id.earliest_mincha_time) || (id == R.string.earliest_mincha))
			return getEarliestMincha();
		if ((id == R.id.mincha_row) || (id == R.id.mincha_time) || (id == R.string.mincha))
			return getMincha();
		if ((id == R.id.plug_hamincha_row) || (id == R.id.plug_hamincha_time) || (id == R.string.plug_hamincha))
			return getPlugHamincha();
		// if ((id == R.id.candles_row) || (id == R.id.candles_time) || (id ==
		// R.string.candles))
		// return getCandleLighting();
		if ((id == R.id.sunset_row) || (id == R.id.sunset_time) || (id == R.string.sunset))
			return getSunset();
		if ((id == R.id.nightfall_row) || (id == R.id.nightfall_time) || (id == R.string.nightfall))
			return getNightfall();
		if ((id == R.id.candles_nightfall_row) || (id == R.id.candles_nightfall_time))
			return getNightfall();
		if ((id == R.id.midnight_row) || (id == R.id.midnight_time) || (id == R.string.midnight))
			return getMidnight();
		return null;
	}

	/**
	 * Get the reminder.
	 * 
	 * @param id
	 *            the time id.
	 * @return the number of minutes before the prayer - positive value when no
	 *         reminder.
	 */
	public int getReminder(int id) {
		if ((id == R.id.dawn_row) || (id == R.id.dawn_time) || (id == R.string.dawn))
			return getReminder(KEY_REMINDER_DAWN);
		if ((id == R.id.tallis_row) || (id == R.id.tallis_time) || (id == R.string.tallis))
			return getReminder(KEY_REMINDER_TALLIS);
		if ((id == R.id.sunrise_row) || (id == R.id.sunrise_time) || (id == R.string.sunrise))
			return getReminder(KEY_REMINDER_SUNRISE);
		if ((id == R.id.shema_row) || (id == R.id.shema_time) || (id == R.string.shema))
			return getReminder(KEY_REMINDER_SHEMA);
		if ((id == R.id.prayers_row) || (id == R.id.prayers_time) || (id == R.string.prayers))
			return getReminder(KEY_REMINDER_TFILA);
		if ((id == R.id.midday_row) || (id == R.id.midday_time) || (id == R.string.midday))
			return getReminder(KEY_REMINDER_NOON);
		if ((id == R.id.earliest_mincha_row) || (id == R.id.earliest_mincha_time) || (id == R.string.earliest_mincha))
			return getReminder(KEY_REMINDER_EARLIEST_MINCHA);
		if ((id == R.id.mincha_row) || (id == R.id.mincha_time) || (id == R.string.mincha))
			return getReminder(KEY_REMINDER_MINCHA);
		if ((id == R.id.plug_hamincha_row) || (id == R.id.plug_hamincha_time) || (id == R.string.plug_hamincha))
			return getReminder(KEY_REMINDER_PLUG_MINCHA);
		if ((id == R.id.candles_row) || (id == R.id.candles_time) || (id == R.string.candles))
			return getReminder(KEY_REMINDER_CANDLES);
		if ((id == R.id.sunset_row) || (id == R.id.sunset_time) || (id == R.string.sunset))
			return getReminder(KEY_REMINDER_SUNSET);
		if ((id == R.id.nightfall_row) || (id == R.id.nightfall_time) || (id == R.string.nightfall))
			return getReminder(KEY_REMINDER_NIGHTFALL);
		if ((id == R.id.candles_nightfall_row) || (id == R.id.candles_nightfall_time))
			return getReminder(KEY_REMINDER_NIGHTFALL);
		if ((id == R.id.midnight_row) || (id == R.id.midnight_time) || (id == R.string.midnight))
			return getReminder(KEY_REMINDER_MIDNIGHT);
		return Integer.MAX_VALUE;
	}

	/**
	 * Get the reminder.
	 * 
	 * @param key
	 *            the key.
	 * @return the number of minutes before the prayer - positive value when no
	 *         reminder.
	 */
	public int getReminder(String key) {
		String value = mPrefs.getString(key, null);
		if (!TextUtils.isEmpty(value))
			return Integer.parseInt(value);
		return Integer.MAX_VALUE;
	}
}

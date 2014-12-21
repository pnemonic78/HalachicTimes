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
import android.text.format.DateUtils;

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
	/** Preference key for the elevation / altitude. */
	private static final String KEY_ELEVATION = "altitude";
	/** Preference name for the location provider. */
	private static final String KEY_PROVIDER = "provider";
	/** Preference name for the location time. */
	private static final String KEY_TIME = "time";
	/** Preference name for the co-ordinates visibility. */
	public static final String KEY_COORDS = "coords.visible";
	/** Preference name for the co-ordinates format. */
	public static final String KEY_COORDS_FORMAT = "coords.format";
	/** Preference name for showing seconds. */
	public static final String KEY_SECONDS = "seconds.visible";
	/** Preference name for showing summaries. */
	public static final String KEY_SUMMARIES = "summaries.visible";
	/** Preference name for enabling past times. */
	public static final String KEY_PAST = "past";
	/** Preference name for the background gradient. */
	public static final String KEY_BG_GRADIENT = "gradient";
	/** Preference name for the latest reminder. */
	private static final String KEY_REMINDER_LATEST = "reminder";

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
	/** Preference name for Last Biur Chametz type. */
	public static final String KEY_OPINION_BURN = "biur_chametz";
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
	/** Preference name for Chanukka candle lighting. */
	public static final String KEY_OPINION_CANDLES_CHANUKKA = "candles_chanukka";
	/** Preference name for sunset type. */
	public static final String KEY_OPINION_SUNSET = "sunset";
	/** Preference name for twilight type. */
	public static final String KEY_OPINION_TWILIGHT = "twilight";
	/** Preference name for nightfall type. */
	public static final String KEY_OPINION_NIGHTFALL = "nightfall";
	/** Preference name for midnight type. */
	public static final String KEY_OPINION_MIDNIGHT = "midnight";
	/** Preference name for earliest kiddush levana type. */
	public static final String KEY_OPINION_EARLIEST_LEVANA = "levana_earliest";
	/** Preference name for latest kiddush levana type. */
	public static final String KEY_OPINION_LATEST_LEVANA = "levana_latest";

	static final String REMINDER_SUFFIX = ".reminder";
	private static final String ANIM_SUFFIX = ".anim";

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
	/** Preference name for twilight reminder. */
	public static final String KEY_REMINDER_TWILIGHT = KEY_OPINION_TWILIGHT + REMINDER_SUFFIX;
	/** Preference name for nightfall reminder. */
	public static final String KEY_REMINDER_NIGHTFALL = KEY_OPINION_NIGHTFALL + REMINDER_SUFFIX;
	/** Preference name for midnight reminder. */
	public static final String KEY_REMINDER_MIDNIGHT = KEY_OPINION_MIDNIGHT + REMINDER_SUFFIX;
	/** Preference name for earliest kiddush levana reminder. */
	public static final String KEY_REMINDER_EARLIEST_LEVANA = KEY_OPINION_EARLIEST_LEVANA + REMINDER_SUFFIX;
	/** Preference name for latest kiddush levana reminder. */
	public static final String KEY_REMINDER_LATEST_LEVANA = KEY_OPINION_LATEST_LEVANA + REMINDER_SUFFIX;

	/** Preference name for candle lighting animations. */
	public static final String KEY_ANIM_CANDLES = KEY_OPINION_CANDLES + ANIM_SUFFIX;

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
		Context app = context.getApplicationContext();
		if (app != null)
			context = app;
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
		double elevation;
		try {
			latitude = Double.parseDouble(mPrefs.getString(KEY_LATITUDE, "0"));
			longitude = Double.parseDouble(mPrefs.getString(KEY_LONGITUDE, "0"));
			elevation = Double.parseDouble(mPrefs.getString(KEY_ELEVATION, "0"));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return null;
		}
		String provider = mPrefs.getString(KEY_PROVIDER, "");
		Location location = new Location(provider);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setAltitude(elevation);
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
		editor.putString(KEY_ELEVATION, Double.toString(location.hasAltitude() ? location.getAltitude() : 0));
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
	 * Format times with seconds?
	 * 
	 * @return {@code true} to show seconds.
	 */
	public boolean isSeconds() {
		return mPrefs.getBoolean(KEY_SECONDS, false);
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
		return mPrefs.getBoolean(KEY_PAST, true);
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
		return mPrefs.getInt(KEY_OPINION_CANDLES, 22);
	}

	/**
	 * Get the opinion for Chanukka candle lighting time.
	 * 
	 * @return the opinion.
	 */
	public String getChanukkaCandles() {
		return mPrefs.getString(KEY_OPINION_CANDLES_CHANUKKA, "");
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
	 * Get the opinion for burning chametz (<em>biur chametz</em>).
	 * 
	 * @return the opinion.
	 */
	public String getBurnChametz() {
		return mPrefs.getString(KEY_OPINION_BURN, "GRA");
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
	 * Get the opinion for twilight (dusk).
	 * 
	 * @return the opinion.
	 */
	public String getTwilight() {
		return mPrefs.getString(KEY_OPINION_TWILIGHT, "");
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
	 * Get the opinion for earliest kiddush levana.
	 * 
	 * @return the opinion.
	 */
	public String getEarliestKiddushLevana() {
		return mPrefs.getString(KEY_OPINION_EARLIEST_LEVANA, "");
	}

	/**
	 * Get the opinion for latest kiddush levana.
	 * 
	 * @return the opinion.
	 */
	public String getLatestKiddushLevana() {
		return mPrefs.getString(KEY_OPINION_LATEST_LEVANA, "");
	}

	/**
	 * Get the opinion.
	 * 
	 * @param id
	 *            the time id.
	 * @return the opinion.
	 */
	public String getOpinion(int id) {
		if ((id == R.id.dawn_row) || (id == R.string.dawn))
			return getDawn();
		if ((id == R.id.tallis_row) || (id == R.string.tallis))
			return getTallis();
		if ((id == R.id.sunrise_row) || (id == R.string.sunrise))
			return getSunrise();
		if ((id == R.id.shema_row) || (id == R.string.shema))
			return getLastShema();
		if ((id == R.id.prayers_row) || (id == R.string.prayers))
			return getLastTfila();
		if ((id == R.id.midday_row) || (id == R.string.midday))
			return getMidday();
		if ((id == R.id.earliest_mincha_row) || (id == R.string.earliest_mincha))
			return getEarliestMincha();
		if ((id == R.id.mincha_row) || (id == R.string.mincha))
			return getMincha();
		if ((id == R.id.plug_hamincha_row) || (id == R.string.plug_hamincha))
			return getPlugHamincha();
		if ((id == R.id.sunset_row) || (id == R.string.sunset))
			return getSunset();
		if ((id == R.id.twilight_row) || (id == R.string.twilight) || (id == R.id.candles_twilight_row))
			return getTwilight();
		if ((id == R.id.nightfall_row) || (id == R.string.nightfall) || (id == R.id.candles_nightfall_row))
			return getNightfall();
		if ((id == R.id.midnight_row) || (id == R.string.midnight))
			return getMidnight();
		if (id == R.string.levana_earliest)
			return getEarliestKiddushLevana();
		if (id == R.string.levana_latest)
			return getLatestKiddushLevana();

		return null;
	}

	/**
	 * Get the reminder.
	 * 
	 * @param id
	 *            the time id.
	 * @return the number of minutes before the prayer, in milliseconds -
	 *         positive value when no reminder.
	 */
	public long getReminder(int id) {
		if ((id == R.id.dawn_row) || (id == R.string.dawn))
			return getReminder(KEY_REMINDER_DAWN);
		if ((id == R.id.tallis_row) || (id == R.string.tallis))
			return getReminder(KEY_REMINDER_TALLIS);
		if ((id == R.id.sunrise_row) || (id == R.string.sunrise))
			return getReminder(KEY_REMINDER_SUNRISE);
		if ((id == R.id.shema_row) || (id == R.string.shema))
			return getReminder(KEY_REMINDER_SHEMA);
		if ((id == R.id.prayers_row) || (id == R.string.prayers))
			return getReminder(KEY_REMINDER_TFILA);
		if ((id == R.id.midday_row) || (id == R.string.midday))
			return getReminder(KEY_REMINDER_NOON);
		if ((id == R.id.earliest_mincha_row) || (id == R.string.earliest_mincha))
			return getReminder(KEY_REMINDER_EARLIEST_MINCHA);
		if ((id == R.id.mincha_row) || (id == R.string.mincha))
			return getReminder(KEY_REMINDER_MINCHA);
		if ((id == R.id.plug_hamincha_row) || (id == R.string.plug_hamincha))
			return getReminder(KEY_REMINDER_PLUG_MINCHA);
		if ((id == R.id.candles_row) || (id == R.string.candles))
			return getReminder(KEY_REMINDER_CANDLES);
		if ((id == R.id.sunset_row) || (id == R.string.sunset))
			return getReminder(KEY_REMINDER_SUNSET);
		if ((id == R.id.twilight_row) || (id == R.string.twilight) || (id == R.id.candles_twilight_row))
			return getReminder(KEY_REMINDER_TWILIGHT);
		if ((id == R.id.nightfall_row) || (id == R.string.nightfall) || (id == R.id.candles_nightfall_row))
			return getReminder(KEY_REMINDER_NIGHTFALL);
		if ((id == R.id.midnight_row) || (id == R.string.midnight))
			return getReminder(KEY_REMINDER_MIDNIGHT);
		if (id == R.string.levana_earliest)
			return getReminder(KEY_REMINDER_EARLIEST_LEVANA);
		if (id == R.string.levana_latest)
			return getReminder(KEY_REMINDER_LATEST_LEVANA);

		return Long.MAX_VALUE;
	}

	/**
	 * Get the reminder.
	 * 
	 * @param key
	 *            the key.
	 * @return the number of minutes before the prayer, in milliseconds -
	 *         positive value when no reminder.
	 */
	public long getReminder(String key) {
		String value = mPrefs.getString(key, null);
		if (!TextUtils.isEmpty(value))
			return Long.parseLong(value) * DateUtils.MINUTE_IN_MILLIS;
		return Long.MAX_VALUE;
	}

	/**
	 * Get the time that was used for the latest reminder.
	 * 
	 * @return the time.
	 */
	public long getLatestReminder() {
		return mPrefs.getLong(KEY_REMINDER_LATEST, 0L);
	}

	/**
	 * Set the time that was used for the latest reminder to now.
	 * 
	 * @param time
	 *            the time.
	 */
	public void setLatestReminder(long time) {
		Editor editor = mPrefs.edit();
		editor.putLong(KEY_REMINDER_LATEST, time);
		editor.commit();
	}

	/**
	 * Are the candles animated?
	 * 
	 * @return {@code true} if candles animations enabled.
	 */
	public boolean isCandlesAnimated() {
		return mPrefs.getBoolean(KEY_ANIM_CANDLES, true);
	}
}

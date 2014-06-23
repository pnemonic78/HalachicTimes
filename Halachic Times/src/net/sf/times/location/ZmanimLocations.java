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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.R;
import net.sf.times.ZmanimApplication;
import net.sf.times.ZmanimSettings;
import net.sourceforge.zmanim.util.GeoLocation;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Location provider.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimLocations implements LocationListener {

	private static final String TAG = "ZmanimLocations";

	/** ISO 639 language code for "Hebrew". */
	public static final String ISO639_HEBREW_FORMER = "he";
	/** ISO 639 language code for "Hebrew" (Java compatibility). */
	public static final String ISO639_HEBREW = "iw";
	/** ISO 639 language code for "Yiddish" (Java compatibility). */
	public static final String ISO639_YIDDISH_FORMER = "ji";
	/** ISO 639 language code for "Yiddish". */
	public static final String ISO639_YIDDISH = "yi";

	/** 1 kilometre. */
	// private static final int KILOMETRE = 1000;

	/** The minimum time interval between location updates, in milliseconds. */
	private static final long UPDATE_TIME = DateUtils.SECOND_IN_MILLIS;
	/** The maximum time interval between location updates, in milliseconds. */
	private static final long UPDATE_TIME_MAX = DateUtils.HOUR_IN_MILLIS;
	/** The time interval between requesting location updates, in milliseconds. */
	private static final long UPDATE_TIME_START = 30 * DateUtils.SECOND_IN_MILLIS;
	/**
	 * The duration to receive updates, in milliseconds.<br>
	 * Should be enough time to get a sufficiently accurate location.
	 */
	private static final long UPDATE_DURATION = 30 * DateUtils.SECOND_IN_MILLIS;
	/** The minimum distance between location updates, in metres. */
	private static final int UPDATE_DISTANCE = 100;

	/** Time zone ID for Jerusalem. */
	private static final String TZ_JERUSALEM = "Asia/Jerusalem";
	/** Time zone ID for Israeli Standard Time. */
	private static final String TZ_IST = "IST";
	/** Time zone ID for Beirut (patch for Israeli law of DST 2013). */
	private static final String TZ_BEIRUT = "Asia/Beirut";

	/** Northern-most latitude for Israel. */
	private static final double ISRAEL_NORTH = 33.289212;
	/** Southern-most latitude for Israel. */
	private static final double ISRAEL_SOUTH = 29.489218;
	/** Eastern-most longitude for Israel. */
	private static final double ISRAEL_EAST = 35.891876;
	/** Western-most longitude for Israel. */
	private static final double ISRAEL_WEST = 34.215317;

	private static final int WHAT_START = 0;
	private static final int WHAT_STOP = 1;
	private static final int WHAT_CHANGED = 2;
	private static final int WHAT_ELEVATION = 3;

	/** The owner location listeners. */
	private final List<LocationListener> mLocationListeners = new ArrayList<LocationListener>();
	/** The owner location listeners for dispatching events. */
	private List<LocationListener> mLocationListenersLoop = mLocationListeners;
	/** Service provider for locations. */
	private LocationManager mLocationManager;
	/** The location. */
	private Location mLocation;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;
	/** The list of countries. */
	private CountriesGeocoder mCountries;
	/** The coordinates format. */
	private String mCoordsFormat;
	/** The time zone. */
	private TimeZone mTimeZone;
	/** The handler thread. */
	private HandlerThread mHandlerThread;
	/** The handler. */
	private Handler mHandler;
	/** The next time to start update locations. */
	private long mStartTaskDelay = UPDATE_TIME_START;
	/** The next time to stop update locations. */
	private final long mStopTaskDelay = UPDATE_DURATION;
	/** The address provider. */
	private AddressProvider mAddressProvider;

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimLocations(Context context) {
		super();
		mSettings = new ZmanimSettings(context);
		mCountries = new CountriesGeocoder(context);
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mCoordsFormat = context.getString(R.string.location_coords);
		mTimeZone = TimeZone.getDefault();

		ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
		mAddressProvider = app.getAddresses();

		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new UpdatesHandler(mHandlerThread.getLooper());
	}

	/**
	 * Add a location listener.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public void addLocationListener(LocationListener listener) {
		if (!mLocationListeners.contains(listener)) {
			mLocationListeners.add(listener);
			mLocationListenersLoop = Collections.unmodifiableList(mLocationListeners);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!isValid(location))
			return;
		if (mLocation != null) {
			// Ignore old locations.
			if (mLocation.getTime() >= location.getTime())
				return;
			// Ignore manual locations.
			if (GeocoderBase.USER_PROVIDER.equals(mLocation.getProvider()))
				return;
		}

		mLocation = location;
		mSettings.putLocation(location);

		for (LocationListener listener : mLocationListenersLoop)
			listener.onLocationChanged(location);

		if (!location.hasAltitude())
			mHandler.sendEmptyMessage(WHAT_ELEVATION);
	}

	@Override
	public void onProviderDisabled(String provider) {
		for (LocationListener listener : mLocationListenersLoop)
			listener.onProviderDisabled(provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		for (LocationListener listener : mLocationListenersLoop)
			listener.onProviderEnabled(provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		for (LocationListener listener : mLocationListenersLoop)
			listener.onStatusChanged(provider, status, extras);
	}

	/**
	 * Get a location from GPS.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationGPS() {
		if (mLocationManager == null)
			return null;
		try {
			return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} catch (IllegalArgumentException iae) {
			Log.e(TAG, "GPS: " + iae.getLocalizedMessage(), iae);
		}
		return null;
	}

	/**
	 * Get a location from the GSM network.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationNetwork() {
		if (mLocationManager == null)
			return null;
		try {
			return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} catch (IllegalArgumentException iae) {
			Log.e(TAG, "Network: " + iae.getLocalizedMessage(), iae);
		}
		return null;
	}

	/**
	 * Get a passive location from other application's GPS.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	@TargetApi(Build.VERSION_CODES.FROYO)
	public Location getLocationPassive() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
			return null;
		if (mLocationManager == null)
			return null;
		try {
			return mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		} catch (IllegalArgumentException iae) {
			Log.e(TAG, "Passive: " + iae.getLocalizedMessage(), iae);
		}
		return null;
	}

	/**
	 * Get a location from the time zone.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationTZ() {
		return getLocationTZ(mTimeZone);
	}

	/**
	 * Get a location from the time zone.
	 * 
	 * @param timeZone
	 *            the time zone.
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationTZ(TimeZone timeZone) {
		return mCountries.findLocation(timeZone);
	}

	/**
	 * Get a location from the saved preferences.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationSaved() {
		return mSettings.getLocation();
	}

	/**
	 * Get the best location.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocation() {
		Location loc = mLocation;
		if (isValid(loc))
			return loc;
		loc = getLocationGPS();
		if (isValid(loc))
			return loc;
		loc = getLocationNetwork();
		if (isValid(loc))
			return loc;
		loc = getLocationPassive();
		if (isValid(loc))
			return loc;
		loc = getLocationSaved();
		if (isValid(loc))
			return loc;
		loc = getLocationTZ();
		return loc;
	}

	/**
	 * Is the location valid?
	 * 
	 * @param location
	 *            the location to check.
	 * @return {@code false} if location is invalid.
	 */
	public boolean isValid(Location location) {
		if (location == null)
			return false;
		final double latitude = location.getLatitude();
		if ((latitude > 90) || (latitude < -90))
			return false;
		final double longitude = location.getLongitude();
		if ((longitude > 180) || (longitude < -180))
			return false;
		return true;
	}

	/**
	 * Stop listening.
	 * 
	 * @param listener
	 *            the listener who wants to stop listening.
	 */
	public void stop(LocationListener listener) {
		if (listener != null) {
			mLocationListeners.remove(listener);
			mLocationListenersLoop = Collections.unmodifiableList(mLocationListeners);
		}

		if (mLocationListeners.isEmpty()) {
			removeUpdates();
			mHandler.removeMessages(WHAT_START);
		}
	}

	/**
	 * Start or resume listening.
	 * 
	 * @param listener
	 *            the listener who wants to resume listening.
	 */
	public void start(LocationListener listener) {
		if (listener != null)
			addLocationListener(listener);

		mStartTaskDelay = UPDATE_TIME_START;
		mHandler.sendEmptyMessage(WHAT_START);

		// Finding address and elevation do network access.
		mHandler.sendEmptyMessage(WHAT_CHANGED);
	}

	/**
	 * Is the location in Israel?<br>
	 * Used to determine if user is in diaspora for 2-day festivals.
	 * 
	 * @param location
	 *            the location.
	 * @param timeZone
	 *            the time zone.
	 * @return {@code true} if user is in Israel - {@code false} otherwise.
	 */
	public boolean inIsrael(Location location, TimeZone timeZone) {
		if (location == null) {
			if (timeZone == null)
				timeZone = mTimeZone;
			String id = timeZone.getID();
			if (TZ_JERUSALEM.equals(id) || TZ_IST.equals(id) || TZ_BEIRUT.equals(id))
				return true;
			return false;
		}

		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		return (latitude <= ISRAEL_NORTH) && (latitude >= ISRAEL_SOUTH) && (longitude >= ISRAEL_WEST) && (longitude <= ISRAEL_EAST);
	}

	/**
	 * Is the current location in Israel?<br>
	 * Used to determine if user is in diaspora for 2-day festivals.
	 * 
	 * @param timeZone
	 *            the time zone.
	 * @return {@code true} if user is in Israel - {@code false} otherwise.
	 */
	public boolean inIsrael(TimeZone timeZone) {
		return inIsrael(getLocation(), timeZone);
	}

	/**
	 * Is the current location in Israel?<br>
	 * Used to determine if user is in diaspora for 2-day festivals.
	 * 
	 * @return {@code true} if user is in Israel - {@code false} otherwise.
	 */
	public boolean inIsrael() {
		return inIsrael(mTimeZone);
	}

	/**
	 * Format the coordinates.
	 * 
	 * @return the coordinates text.
	 */
	public String formatCoordinates() {
		return formatCoordinates(getLocation());
	}

	/**
	 * Format the coordinates.
	 * 
	 * @param location
	 *            the location.
	 * @return the coordinates text.
	 */
	public String formatCoordinates(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		return formatCoordinates(latitude, longitude);
	}

	/**
	 * Format the coordinates.
	 * 
	 * @param address
	 *            the address.
	 * @return the coordinates text.
	 */
	public String formatCoordinates(Address address) {
		final double latitude = address.getLatitude();
		final double longitude = address.getLongitude();
		return formatCoordinates(latitude, longitude);
	}

	/**
	 * Format the coordinates.
	 * 
	 * @param latitude
	 *            the latitude.
	 * @param longitude
	 *            the longitude.
	 * @return the coordinates text.
	 */
	public String formatCoordinates(double latitude, double longitude) {
		final String notation = mSettings.getCoordinatesFormat();
		final String latitudeText;
		final String longitudeText;
		if (ZmanimSettings.FORMAT_SEXIGESIMAL.equals(notation)) {
			latitudeText = Location.convert(latitude, Location.FORMAT_SECONDS);
			longitudeText = Location.convert(longitude, Location.FORMAT_SECONDS);
		} else {
			latitudeText = String.format(Locale.US, "%1$.6f", latitude);
			longitudeText = String.format(Locale.US, "%1$.6f", longitude);
		}
		return String.format(Locale.US, mCoordsFormat, latitudeText, longitudeText);
	}

	/**
	 * Format the coordinates.
	 * 
	 * @param coord
	 *            the coordinate.
	 * @return the coordinate text.
	 */
	public String formatCoordinate(double coord) {
		final String notation = mSettings.getCoordinatesFormat();
		if (ZmanimSettings.FORMAT_SEXIGESIMAL.equals(notation)) {
			return Location.convert(coord, Location.FORMAT_SECONDS);
		}
		return String.format(Locale.US, "%1$.6f", coord);
	}

	/**
	 * Get the location.
	 * 
	 * @param timeZone
	 *            the time zone.
	 * @return the location - {@code null} otherwise.
	 */
	public GeoLocation getGeoLocation(TimeZone timeZone) {
		Location loc = getLocation();
		if (loc == null)
			return null;
		final String locationName = loc.getProvider();
		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double elevation = loc.hasAltitude() ? loc.getAltitude() : 0;

		return new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
	}

	/**
	 * Get the location.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public GeoLocation getGeoLocation() {
		return getGeoLocation(mTimeZone);
	}

	/**
	 * Get the time zone.
	 * 
	 * @return the time zone.
	 */
	public TimeZone getTimeZone() {
		return mTimeZone;
	}

	/**
	 * Set the location.
	 * 
	 * @param location
	 *            the location.
	 */
	public void setLocation(Location location) {
		mLocation = null;
		onLocationChanged(location);
	}

	/**
	 * Is the default locale right-to-left?
	 * 
	 * @return {@code true} if the locale is either Hebrew or Yiddish.
	 */
	public static boolean isLocaleRTL() {
		final String iso639 = Locale.getDefault().getLanguage();
		return ISO639_HEBREW.equals(iso639) || ISO639_HEBREW_FORMER.equals(iso639) || ISO639_YIDDISH.equals(iso639) || ISO639_YIDDISH_FORMER.equals(iso639);
	}

	private void requestUpdates() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(true);
		criteria.setCostAllowed(true);
		// criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = mLocationManager.getBestProvider(criteria, true);
		try {
			mLocationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
		} catch (IllegalArgumentException iae) {
			Log.e(TAG, "resume: " + iae.getLocalizedMessage(), iae);
		}

		// Let the updates run for only a small while to save battery.
		mHandler.sendEmptyMessageDelayed(WHAT_STOP, mStopTaskDelay);
		mStartTaskDelay = Math.min(UPDATE_TIME_MAX, mStartTaskDelay << 1);
	}

	private void removeUpdates() {
		mLocationManager.removeUpdates(this);

		if (!mLocationListeners.isEmpty()) {
			mHandler.sendEmptyMessageDelayed(WHAT_START, mStartTaskDelay);
		}
	}

	/**
	 * Quit updating locations.
	 */
	public void quit() {
		mLocationListeners.clear();
		removeUpdates();
		mHandler.removeMessages(WHAT_START);

		Looper looper = mHandlerThread.getLooper();
		if (looper != null) {
			looper.quit();
		}
		mHandlerThread.interrupt();
	}

	private class UpdatesHandler extends Handler {

		public UpdatesHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_START:
				requestUpdates();
				break;
			case WHAT_STOP:
				removeUpdates();
				break;
			case WHAT_CHANGED:
				onLocationChanged(getLocation());
				break;
			case WHAT_ELEVATION:
				findElevation(getLocation());
				break;
			}
		}
	}

	/**
	 * Find the elevation for the location.
	 * 
	 * @param location
	 *            the location.
	 */
	private void findElevation(Location location) {
		Location elevated = mAddressProvider.findElevation(location);
		if ((elevated != null) && (elevated instanceof ZmanimLocation)) {
			mAddressProvider.insertOrUpdate((ZmanimLocation) elevated);
			onLocationChanged(elevated);
		}
	}

}

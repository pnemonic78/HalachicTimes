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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.location.CountriesGeocoder;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;

/**
 * Location provider.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimLocations implements LocationListener {

	/** 1 kilometre. */
	private static final int ONE_KM = 1000;

	/** Time zone ID for Jerusalem. */
	private static final String TZ_JERUSALEM = "Asia/Jerusalem";
	/** Time zone ID for Israeli Standard Time. */
	private static final String TZ_IST = "IST";

	/** Northern-most latitude for Israel. */
	private static final double ISRAEL_NORTH = 33.289212;
	/** Southern-most latitude for Israel. */
	private static final double ISRAEL_SOUTH = 29.489218;
	/** Eastern-most longitude for Israel. */
	private static final double ISRAEL_EAST = 35.891876;
	/** Western-most longitude for Israel. */
	private static final double ISRAEL_WEST = 34.215317;

	/** The owner location listener. */
	private final List<LocationListener> mLocationListeners = new ArrayList<LocationListener>();
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
	/** The instance. */
	private static ZmanimLocations mInstance;

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 */
	private ZmanimLocations(Context context) {
		super();
		mSettings = new ZmanimSettings(context);
		mCountries = new CountriesGeocoder(context);
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mCoordsFormat = context.getString(R.string.location_coords);
	}

	/**
	 * Get the locations provider instance.
	 * 
	 * @param context
	 *            the context.
	 * @return the provider.
	 */
	public static ZmanimLocations getInstance(Context context) {
		if (mInstance == null)
			mInstance = new ZmanimLocations(context);
		return mInstance;
	}

	/**
	 * Get the locations provider instance.
	 * 
	 * @param context
	 *            the context.
	 * @param listener
	 *            the listener.
	 * @return the provider.
	 */
	public static ZmanimLocations getInstance(Context context, LocationListener listener) {
		if (mInstance == null)
			mInstance = new ZmanimLocations(context);
		mInstance.addLocationListener(listener);
		return mInstance;
	}

	/**
	 * Add a location listener.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public void addLocationListener(LocationListener listener) {
		if (!mLocationListeners.contains(listener))
			mLocationListeners.add(listener);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;
		// Ignore old locations.
		if (mLocation != null) {
			if (mLocation.getTime() >= location.getTime())
				return;
		}
		mLocation = location;
		mSettings.putLocation(location);
		for (LocationListener listener : mLocationListeners)
			listener.onLocationChanged(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		for (LocationListener listener : mLocationListeners)
			listener.onProviderDisabled(provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		for (LocationListener listener : mLocationListeners)
			listener.onProviderEnabled(provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		for (LocationListener listener : mLocationListeners)
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
		return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Get a location from the GSM network.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationNetwork() {
		if (mLocationManager == null)
			return null;
		return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Get a location from the time zone.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationTZ() {
		return getLocationTZ(TimeZone.getDefault());
	}

	/**
	 * Get a location from the time zone.
	 * 
	 * @param tz
	 *            the time zone.
	 * @return the location - {@code null} otherwise.
	 */
	public Location getLocationTZ(TimeZone tz) {
		return mCountries.findLocation(tz);
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
		if (loc == null)
			loc = getLocationGPS();
		if (loc == null)
			loc = getLocationNetwork();
		if (loc == null)
			loc = getLocationSaved();
		if (loc == null)
			loc = getLocationTZ();
		return loc;
	}

	/**
	 * Cancel.
	 * 
	 * @param listener
	 *            the listener who wants to stop listening.
	 */
	public void cancel(LocationListener listener) {
		if (listener != null)
			mLocationListeners.remove(listener);
		if (mLocationListeners.isEmpty()) {
			mLocationManager.removeUpdates(this);
		}
	}

	/**
	 * Resume.
	 * 
	 * @param listener
	 *            the listener who wants to resume listening.
	 */
	public void resume(LocationListener listener) {
		if (listener != null)
			addLocationListener(listener);

		try {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DateUtils.MINUTE_IN_MILLIS, ONE_KM, this);
		} catch (IllegalArgumentException iae) {
			System.err.println(this + ": " + iae.getLocalizedMessage());
		}
		try {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DateUtils.MINUTE_IN_MILLIS, ONE_KM, this);
		} catch (IllegalArgumentException iae) {
			System.err.println(this + ": " + iae.getLocalizedMessage());
		}

		if (listener != null)
			listener.onLocationChanged(getLocation());
	}

	/**
	 * Is the user in Israel?<br>
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
				timeZone = TimeZone.getDefault();
			String id = timeZone.getID();
			if (TZ_JERUSALEM.equals(id) || TZ_IST.equals(id))
				return true;
			return false;
		}

		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		return (latitude <= ISRAEL_NORTH) && (latitude >= ISRAEL_SOUTH) && (longitude >= ISRAEL_WEST) && (longitude <= ISRAEL_EAST);
	}

	/**
	 * Format the coordinates.
	 * 
	 * @return the coordinates text.
	 */
	public String formatCoordinates() {
		Location loc = getLocation();
		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();

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
		final String coordsText = String.format(Locale.US, mCoordsFormat, latitudeText, longitudeText);
		return coordsText;
	}
}

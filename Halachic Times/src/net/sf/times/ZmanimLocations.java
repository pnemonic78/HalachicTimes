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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import net.sf.times.location.CitiesGeocoder;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Location provider.
 * 
 * @author Moshe
 */
public class ZmanimLocations implements LocationListener {

	/** 1 second. */
	private static final long ONE_SECOND = 1000;
	/** 1 minute. */
	private static final long ONE_MINUTE = 60 * ONE_SECOND;

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

	/** The context. */
	private final Context mContext;
	/** The owner location listener. */
	private final List<LocationListener> mLocationListeners = new ArrayList<LocationListener>();
	/** Service provider for locations. */
	private LocationManager mLocationManager;
	/** The location. */
	private Location mLocation;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;
	/** The list of cities. */
	private CitiesGeocoder mCities;

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimLocations(Context context) {
		this(context, null);
	}

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 * @param listener
	 *            the owner location listener.
	 */
	public ZmanimLocations(Context context, LocationListener listener) {
		super();
		mContext = context;
		mSettings = new ZmanimSettings(context);
		addLocationListener(listener);
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
		if (mCities == null)
			mCities = new CitiesGeocoder(mContext);
		return mCities.findLocation(tz);
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
	 */
	public void cancel() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
			mLocationManager = null;
		}
		mLocation = null;
	}

	/**
	 * Resume.
	 */
	public void resume() {
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			try {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE, ONE_KM, this);
			} catch (IllegalArgumentException iae) {
				System.err.println(this + ": " + iae.getLocalizedMessage());
			}
			try {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_MINUTE, ONE_KM, this);
			} catch (IllegalArgumentException iae) {
				System.err.println(this + ": " + iae.getLocalizedMessage());
			}
		}

		if (mLocation == null) {
			onLocationChanged(getLocation());
		}
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
}

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

import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 * 
 * @author Moshe Waisberg
 */
public class CompassActivity extends Activity implements ZmanimLocationListener, SensorEventListener {

	/** Latitude of the Holy of Holies, according to Google. */
	private static final double HOLIEST_LATITUDE = 31.778122;
	/** Longitude of the Holy of Holies, according to Google. */
	private static final double HOLIEST_LONGITUDE = 35.235345;
	/** Elevation of the Holy of Holies, according to Google. */
	private static final double HOLIEST_ELEVATION = 744.5184937;

	/** The sensor manager. */
	private SensorManager mSensorManager;
	/** The accelerometer sensor. */
	private Sensor mAccel;
	/** The magnetic field sensor. */
	private Sensor mMagnetic;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	/** Location of the Holy of Holies. */
	private Location mHoliest;
	/** The main view. */
	private CompassView mView;
	/** The gravity values. */
	private final float[] mGravity = new float[3];
	/** The geomagnetic field. */
	private final float[] mGeomagnetic = new float[3];
	/** Rotation matrix. */
	private final float[] matrixR = new float[9];
	/** Orientation matrix. */
	private final float[] mOrientation = new float[3];
	/** The settings and preferences. */
	private ZmanimSettings mSettings;
	/** The address location. */
	private Location mAddressLocation;
	/** The address. */
	private ZmanimAddress mAddress;
	/** Populate the header in UI thread. */
	private Runnable mPopulateHeader;
	/** Update the location in UI thread. */
	private Runnable mUpdateLocation;

	/**
	 * Constructs a new compass.
	 */
	public CompassActivity() {
		mHoliest = new Location(LocationManager.GPS_PROVIDER);
		mHoliest.setLatitude(HOLIEST_LATITUDE);
		mHoliest.setLongitude(HOLIEST_LONGITUDE);
		mHoliest.setAltitude(HOLIEST_ELEVATION);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compass);
		mView = (CompassView) findViewById(R.id.compass);

		mSettings = new ZmanimSettings(this);
		if (!mSettings.isSummaries()) {
			View summary = findViewById(android.R.id.summary);
			summary.setVisibility(View.GONE);
		}

		ZmanimApplication app = (ZmanimApplication) getApplication();
		mLocations = app.getLocations();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocations.start(this);
		mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		mLocations.stop(this);
		super.onStop();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (ZmanimLocation.compareTo(mAddressLocation, location) != 0) {
			mAddress = null;
		}
		mAddressLocation = location;
		if (mUpdateLocation == null) {
			mUpdateLocation = new Runnable() {
				@Override
				public void run() {
					populateHeader();
					if ((mView != null) && (mAddressLocation != null))
						mView.setHoliest(mAddressLocation.bearingTo(mHoliest));
				}
			};
		}
		runOnUiThread(mUpdateLocation);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, mGravity, 0, 3);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);
			break;
		default:
			return;
		}
		if (SensorManager.getRotationMatrix(matrixR, null, mGravity, mGeomagnetic)) {
			SensorManager.getOrientation(matrixR, mOrientation);
			mView.setAzimuth(mOrientation[0]);
		}
	}

	/** Populate the header item. */
	private void populateHeader() {
		// Have we been destroyed?
		Location loc = (mAddressLocation == null) ? mLocations.getLocation() : mAddressLocation;
		if (loc == null)
			return;

		final String coordsText = mLocations.formatCoordinates(loc);
		final String locationName = formatAddress();

		// Update the location.
		TextView address = (TextView) findViewById(R.id.address);
		address.setText(locationName);
		TextView coordinates = (TextView) findViewById(R.id.coordinates);
		coordinates.setText(coordsText);
		coordinates.setVisibility(mSettings.isCoordinates() ? View.VISIBLE : View.GONE);
	}

	/**
	 * Format the address for the current location.
	 * 
	 * @return the formatted address.
	 */
	private String formatAddress() {
		if (mAddress != null)
			return mAddress.getFormatted();
		return getString(R.string.location_unknown);
	}

	@Override
	public void onAddressChanged(Location location, ZmanimAddress address) {
		mAddressLocation = location;
		mAddress = address;
		if (mPopulateHeader == null) {
			mPopulateHeader = new Runnable() {
				@Override
				public void run() {
					populateHeader();
				}
			};
		}
		runOnUiThread(mPopulateHeader);
	}

	@Override
	public void onElevationChanged(Location location) {
		onLocationChanged(location);
	}
}

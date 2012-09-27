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

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 * 
 * @author Moshe
 */
public class CompassActivity extends Activity implements LocationListener, SensorEventListener {

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

	/**
	 * Constructs a new compass.
	 */
	public CompassActivity() {
		super();

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

		mLocations = new ZmanimLocations(this, this);
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
	protected void onDestroy() {
		super.onDestroy();
		mLocations.cancel();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocations.resume();
		mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Have we been destroyed?
		Location loc = mLocations.getLocation();
		if (loc == null)
			return;

		TextView coordinates = (TextView) findViewById(R.id.coordinates);
		coordinates.setText(mLocations.formatCoordinates());

		mView.setHoliest(location.bearingTo(mHoliest));
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
			mView.setNorth(mOrientation[0]);
		}
	}
}

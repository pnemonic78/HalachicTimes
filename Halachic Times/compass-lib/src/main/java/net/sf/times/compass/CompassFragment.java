/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.times.compass;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.sf.times.compass.preference.CompassPreferences;
import net.sf.times.location.ZmanimLocation;

/**
 * Show the direction in which to pray.
 * Points to the Holy of Holies in Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends Fragment implements SensorEventListener {

    /** Latitude of the Holy of Holies. */
    private static final double HOLIEST_LATITUDE = 31.778;
    /** Longitude of the Holy of Holies. */
    private static final double HOLIEST_LONGITUDE = 35.2353;
    /** Elevation of the Holy of Holies, according to Google. */
    private static final double HOLIEST_ELEVATION = 744.5184937;

    private static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.

    /** The sensor manager. */
    private SensorManager sensorManager;
    /** The accelerometer sensor. */
    private Sensor accelerometer;
    /** The magnetic field sensor. */
    private Sensor magnetic;
    /** Location of the Holy of Holies. */
    private final Location holiest;
    /** The main view. */
    private CompassView view;
    /** The gravity values. */
    private final float[] gravity = new float[3];
    /** The geomagnetic field. */
    private final float[] geomagnetic = new float[3];
    /** Rotation matrix. */
    private final float[] matrixR = new float[9];
    /** Orientation matrix. */
    private final float[] orientation = new float[3];
    /** The settings and preferences. */
    private CompassPreferences settings;

    public CompassFragment() {
        holiest = new Location(LocationManager.GPS_PROVIDER);
        setHoliest(HOLIEST_LATITUDE, HOLIEST_LONGITUDE, HOLIEST_ELEVATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new CompassView(inflater.getContext());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = (CompassView) view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();
        settings = new CompassPreferences(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lowPass(event.values, gravity);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lowPass(event.values, geomagnetic);
                break;
            default:
                return;
        }
        if (SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(matrixR, orientation);
            view.setAzimuth(orientation[0]);
        }
    }

    /**
     * Set the current location.
     *
     * @param location
     *         the location.
     */
    public void setLocation(Location location) {
        float bearing;
        String bearingType = settings.getBearing();
        if (CompassPreferences.BEARING_GREAT_CIRCLE.equals(bearingType)) {
            bearing = location.bearingTo(holiest);
        } else {
            bearing = ZmanimLocation.angleTo(location, holiest);
        }
        view.setHoliest(bearing);
    }

    public void setHoliest(Location location) {
        this.holiest.set(location);
    }

    public void setHoliest(double latitude, double longitude, double elevation) {
        holiest.setLatitude(latitude);
        holiest.setLongitude(longitude);
        holiest.setAltitude(elevation);
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        final int length = Math.min(input.length, output.length);
        for (int i = 0; i < length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}

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
package com.github.times.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.times.compass.lib.R;
import com.github.times.compass.preference.CompassPreferences;
import com.github.times.compass.preference.SimpleCompassPreferences;
import com.github.times.location.GeocoderBase;
import com.github.times.location.ZmanimLocation;

import org.jetbrains.annotations.NotNull;

import static com.github.times.compass.preference.CompassPreferences.Values.BEARING_GREAT_CIRCLE;
import static java.lang.Math.abs;

/**
 * Show the direction in which to pray.
 * Points to the Holy of Holies in Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends Fragment implements SensorEventListener {

    /**
     * Latitude of the Holy of Holies.
     */
    private static final double HOLIEST_LATITUDE = 31.778;
    /**
     * Longitude of the Holy of Holies.
     */
    private static final double HOLIEST_LONGITUDE = 35.2353;
    /**
     * Elevation of the Holy of Holies, according to Google.
     */
    private static final double HOLIEST_ELEVATION = 744.5184937;

    private static final float ALPHA = 0.35f; // if ALPHA = 1 OR 0, no filter applies.

    /** Accuracy of the device's bearing relative to the holiest bearing, in degrees. */
    private static final float EPSILON_BEARING = 2f;
    /** Duration to consider the bearing match accurate and stable. */
    private static final long VIBRATE_DELAY_MS = DateUtils.SECOND_IN_MILLIS;
    /** Duration of a vibration. */
    private static final long VIBRATE_LENGTH_MS = 50;

    /**
     * The sensor manager.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer sensor.
     */
    private Sensor accelerometer;
    /**
     * The magnetic field sensor.
     */
    private Sensor magnetometer;
    /**
     * Location of the Holy of Holies.
     */
    private final Location holiest = new Location(GeocoderBase.USER_PROVIDER);
    /**
     * The main view.
     */
    protected CompassView compassView;
    /**
     * The accelerometer values.
     */
    private final float[] accelerometerValues = new float[3];
    /**
     * The magnetometer field.
     */
    private final float[] magnetometerValues = new float[3];
    /**
     * Rotation matrix.
     */
    private final float[] matrixR = new float[9];
    /**
     * Remapped rotation matrix.
     */
    private final float[] mapR = new float[9];
    /**
     * Orientation matrix.
     */
    private final float[] orientation = new float[3];
    /**
     * The preferences.
     */
    protected CompassPreferences preferences;
    /**
     * The location's geomagnetic field.
     */
    private GeomagneticField geomagneticField;
    /**
     * The display orientation.
     */
    private int displayRotation = Surface.ROTATION_0;

    private float bearing;

    private Vibrator vibrator;
    private long vibrationTime = 0L;

    public CompassFragment() {
        setHoliest(HOLIEST_LATITUDE, HOLIEST_LONGITUDE, HOLIEST_ELEVATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.compass_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        compassView = view.findViewById(R.id.compass);
        displayRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getContext();
        if (context instanceof BaseCompassActivity) {
            preferences = ((BaseCompassActivity) context).getCompassPreferences();
        } else {
            preferences = new SimpleCompassPreferences(context);
        }
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
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
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAttach(@NotNull Activity activity) {
        super.onAttach(activity);
        displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lowPass(event.values, accelerometerValues);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lowPass(event.values, magnetometerValues);
                break;
            default:
                return;
        }
        if (SensorManager.getRotationMatrix(matrixR, null, accelerometerValues, magnetometerValues)) {
            switch (displayRotation) {
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mapR);
                    break;
                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, mapR);
                    break;
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, mapR);
                    break;
                default:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_X, SensorManager.AXIS_Y, mapR);
                    break;
            }
            SensorManager.getOrientation(mapR, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (geomagneticField != null) {
                azimuth += geomagneticField.getDeclination(); // converts magnetic north to true north
            }
            setAzimuth(azimuth);
        }
    }

    protected void setAzimuth(float azimuth) {
        compassView.setAzimuth(-azimuth);
        maybeVibrate(azimuth);
    }

    /**
     * Set the current location.
     *
     * @param location the location.
     */
    public void setLocation(Location location) {
        if (Double.isNaN(holiest.getLatitude()) || Double.isNaN(holiest.getLongitude())) {
            compassView.setHoliest(Float.NaN);
            return;
        }
        geomagneticField = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                location.getTime());

        String bearingType = preferences.getBearing();
        if (BEARING_GREAT_CIRCLE.equals(bearingType)) {
            bearing = location.bearingTo(holiest);
        } else {
            bearing = ZmanimLocation.angleTo(location, holiest);
        }
        compassView.setHoliest(bearing);
    }

    public void setHoliest(Location location) {
        holiest.set(location);
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
            output[i] += ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void maybeVibrate(float azimuth) {
        long now = SystemClock.elapsedRealtime();
        if (abs(azimuth - bearing) < EPSILON_BEARING) {
            if ((now - vibrationTime) >= VIBRATE_DELAY_MS) {
                vibrate();
                // Disable the vibration until accurate again.
                vibrationTime = Long.MAX_VALUE;
            }
        } else {
            vibrationTime = now;
        }
    }

    private void vibrate() {
        Vibrator vibrator = this.vibrator;
        if (vibrator == null) {
            final Context context = getContext();
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            this.vibrator = vibrator;
        }
        if ((vibrator == null) || !vibrator.hasVibrator()) {
            return;
        }
        vibrator.vibrate(VIBRATE_LENGTH_MS);
    }
}

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
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.times.compass.lib.R;
import com.github.times.compass.preference.CompassPreferences;
import com.github.times.compass.preference.SimpleCompassPreferences;

import org.jetbrains.annotations.NotNull;

/**
 * Show a compass.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends Fragment implements SensorEventListener {

    private static final float ALPHA = 0.35f; // if ALPHA = 1 OR 0, no filter applies.

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.compass_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        compassView = view.findViewById(R.id.compass);
        compassView.setHoliest(Float.NaN);
        updateRotation(getActivity(), view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getContext();
        preferences = getPreferences(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    private CompassPreferences getPreferences(Context context) {
        if (context instanceof BaseCompassActivity) {
            return ((BaseCompassActivity) context).getCompassPreferences();
        }
        return new SimpleCompassPreferences(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onAttach(@NotNull Activity activity) {
        super.onAttach(activity);
        updateRotation(activity, getView());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateRotation(getActivity(), getView());
    }

    private void updateRotation(@Nullable Activity activity, @Nullable View view) {
        Display display = null;
        if (view != null) {
            display = view.getDisplay();
        }
        if ((display == null) && (activity != null)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display = activity.getDisplay();
            } else {
                display = activity.getWindowManager().getDefaultDisplay();
            }
        }
        if (display != null) {
            displayRotation = display.getRotation();
        }
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
    }

    /**
     * Set the current location.
     *
     * @param location the location.
     */
    public void setLocation(Location location) {
        geomagneticField = new GeomagneticField(
            (float) location.getLatitude(),
            (float) location.getLongitude(),
            (float) location.getAltitude(),
            location.getTime());
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
}

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

import static com.github.times.compass.preference.CompassPreferences.Values.BEARING_GREAT_CIRCLE;
import static java.lang.Math.abs;

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

import com.github.os.VibratorCompat;
import com.github.times.compass.lib.R;
import com.github.times.compass.preference.CompassPreferences;
import com.github.times.compass.preference.SimpleCompassPreferences;
import com.github.times.location.GeocoderBase;
import com.github.times.location.ZmanimLocation;

import org.jetbrains.annotations.NotNull;

/**
 * Show the direction in which to pray.
 * Points to a holy place.
 *
 * @author Moshe Waisberg
 */
public class HolyCompassFragment extends CompassFragment {

    /**
     * Accuracy of the device's bearing relative to the holiest bearing, in degrees.
     */
    private static final float EPSILON_BEARING = 2f;

    /** Duration to consider the bearing match accurate and stable. */
    private static final long VIBRATE_DELAY_MS = DateUtils.SECOND_IN_MILLIS;
    /** Duration of a vibration. */
    private static final long VIBRATE_LENGTH_MS = 50;

    /**
     * Location of the Holy of Holies.
     */
    private final Location holiest = new Location(GeocoderBase.USER_PROVIDER);

    private float bearing;

    private VibratorCompat vibrator;
    private long vibrationTime = 0L;

    public HolyCompassFragment() {
        setHoliest(Double.NaN, Double.NaN, Double.NaN);
    }

    protected void setAzimuth(float azimuth) {
        super.setAzimuth(-azimuth);
        maybeVibrate(azimuth);
    }

    /**
     * Set the current location.
     *
     * @param location the location.
     */
    public void setLocation(Location location) {
        super.setLocation(location);

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
        VibratorCompat vibrator = this.vibrator;
        if (vibrator == null) {
            final Context context = requireContext();
            vibrator = new VibratorCompat(context);
            this.vibrator = vibrator;
        }
        vibrator.vibrate(VIBRATE_LENGTH_MS, VibratorCompat.USAGE_TOUCH);
    }
}

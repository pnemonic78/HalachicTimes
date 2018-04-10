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
package com.github.times.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.Nullable;

import com.github.preference.SimplePreferences;

import static com.github.times.location.LocationPreferences.Values.ELEVATION_VISIBLE_DEFAULT;
import static com.github.times.location.LocationPreferences.Values.FORMAT_DECIMAL;
import static com.github.times.location.LocationPreferences.Values.FORMAT_DEFAULT;
import static com.github.times.location.LocationPreferences.Values.FORMAT_NONE;
import static com.github.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL;

/**
 * Simple location preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleLocationPreferences extends SimplePreferences implements LocationPreferences {

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public SimpleLocationPreferences(Context context) {
        super(context);
        init(context);
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        final Resources res = context.getResources();

        FORMAT_DEFAULT = res.getString(R.string.coords_format_defaultValue);
        FORMAT_NONE = res.getString(R.string.coords_format_value_none);
        FORMAT_DECIMAL = res.getString(R.string.coords_format_value_decimal);
        FORMAT_SEXAGESIMAL = res.getString(R.string.coords_format_value_sexagesimal);

        ELEVATION_VISIBLE_DEFAULT = res.getBoolean(R.bool.coords_elevation_visible_defaultValue);
    }

    @Override
    public Location getLocation() {
        if (!preferences.contains(KEY_LATITUDE) || !preferences.contains(KEY_LONGITUDE)) {
            return null;
        }
        double latitude;
        double longitude;
        double elevation;
        try {
            latitude = Double.parseDouble(preferences.getString(KEY_LATITUDE, "0"));
            longitude = Double.parseDouble(preferences.getString(KEY_LONGITUDE, "0"));
            elevation = Double.parseDouble(preferences.getString(KEY_ELEVATION, "0"));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return null;
        }
        String provider = preferences.getString(KEY_PROVIDER, "");
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        location.setTime(preferences.getLong(KEY_TIME, 0L));
        return location;
    }

    @Override
    public void putLocation(@Nullable Location location) {
        SharedPreferences.Editor editor = preferences.edit();
        if (location != null) {
            editor.putString(KEY_PROVIDER, location.getProvider());
            editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
            editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
            editor.putString(KEY_ELEVATION, Double.toString(location.hasAltitude() ? location.getAltitude() : 0));
            editor.putLong(KEY_TIME, location.getTime());
        } else {
            editor.remove(KEY_PROVIDER);
            editor.remove(KEY_LATITUDE);
            editor.remove(KEY_LONGITUDE);
            editor.remove(KEY_ELEVATION);
            editor.remove(KEY_TIME);
        }
        editor.apply();
    }

    /**
     * Are coordinates visible?
     *
     * @return {@code true} to show coordinates.
     */
    public boolean isCoordinatesVisible() {
        return !FORMAT_NONE.equals(getCoordinatesFormat());
    }

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    public String getCoordinatesFormat() {
        return preferences.getString(KEY_COORDS_FORMAT, FORMAT_DEFAULT);
    }

    @Override
    public boolean isElevationVisible() {
        return preferences.getBoolean(KEY_COORDS_ELEVATION, ELEVATION_VISIBLE_DEFAULT);
    }
}

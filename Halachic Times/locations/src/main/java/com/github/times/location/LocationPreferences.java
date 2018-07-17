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

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Location preferences.
 *
 * @author Moshe Waisberg
 */
public interface LocationPreferences {

    /** Preference name for the latitude. */
    String KEY_LATITUDE = "location.latitude";
    /** Preference name for the longitude. */
    String KEY_LONGITUDE = "location.longitude";
    /** Preference key for the elevation / altitude. */
    String KEY_ELEVATION = "location.altitude";
    /** Preference name for the location provider. */
    String KEY_PROVIDER = "location.provider";
    /** Preference name for the location time. */
    String KEY_TIME = "location.time";
    /** Preference name for the co-ordinates format. */
    String KEY_COORDS_FORMAT = "coords.format";
    /** Preference name for the co-ordinates with elevation/altitude. */
    String KEY_COORDS_ELEVATION = "coords.elevation";

    class Values {
        /** Default coordinates format. */
        static String FORMAT_DEFAULT;
        /** Format the coordinates in decimal notation. */
        static String FORMAT_NONE;
        /** Format the coordinates in decimal notation. */
        static String FORMAT_DECIMAL;
        /** Format the coordinates in sexagesimal notation. */
        static String FORMAT_SEXAGESIMAL;

        static boolean ELEVATION_VISIBLE_DEFAULT = false;
    }

    /**
     * Get the location.
     *
     * @return the location - {@code null} otherwise.
     */
    @Nullable
    Location getLocation();

    /**
     * Set the location.
     *
     * @return the location.
     */
    void putLocation(@Nullable Location location);

    /**
     * Are coordinates visible?
     *
     * @return {@code true} to show coordinates.
     */
    boolean isCoordinatesVisible();

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    String getCoordinatesFormat();

    /**
     * Are coordinates with elevation (altitude) visible?
     *
     * @return {@code true} to show coordinates with elevation.
     */
    boolean isElevationVisible();
}

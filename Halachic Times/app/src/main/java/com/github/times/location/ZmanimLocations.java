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
import android.location.Location;

import net.sourceforge.zmanim.util.GeoLocation;

import java.util.TimeZone;

/**
 * Locations provider.
 *
 * @author Moshe Waisberg
 */
public class ZmanimLocations extends LocationsProvider {

    /**
     * Constructs a new provider.
     *
     * @param context the context.
     */
    public ZmanimLocations(Context context) {
        super(context);
    }

    /**
     * Get the location.
     *
     * @param timeZone the time zone.
     * @return the location - {@code null} otherwise.
     */
    public GeoLocation getGeoLocation(TimeZone timeZone) {
        Location loc = getLocation();
        if (loc == null)
            return null;
        final String locationName = loc.getProvider();
        final double latitude = loc.getLatitude();
        final double longitude = loc.getLongitude();
        final double elevation = loc.hasAltitude() ? Math.max(0, loc.getAltitude()) : 0;

        return new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
    }

    /**
     * Get the location for the time zone.
     *
     * @return the location - {@code null} otherwise.
     */
    public GeoLocation getGeoLocation() {
        return getGeoLocation(getTimeZone());
    }
}

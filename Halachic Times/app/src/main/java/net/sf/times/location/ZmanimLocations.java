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
package net.sf.times.location;

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
     * @param context
     *         the context.
     */
    public ZmanimLocations(Context context) {
        super(context);
    }

    /**
     * Get the location.
     *
     * @param timeZone
     *         the time zone.
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

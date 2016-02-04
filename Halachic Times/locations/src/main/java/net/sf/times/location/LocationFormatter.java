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

import android.location.Address;
import android.location.Location;

/**
 * Location formatter.
 *
 * @author Moshe Waisberg
 */
public interface LocationFormatter {

    /**
     * Format the coordinates.
     *
     * @param location
     *         the location.
     * @return the coordinates text.
     */
    CharSequence formatCoordinates(Location location);

    /**
     * Format the coordinates.
     *
     * @param address
     *         the address.
     * @return the coordinates text.
     */
    CharSequence formatCoordinates(Address address);

    /**
     * Format the coordinates.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     * @return the coordinates text.
     */
    CharSequence formatCoordinates(double latitude, double longitude);

    /**
     * Format a coordinate.
     *
     * @param coord
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatCoordinate(double coord);

}
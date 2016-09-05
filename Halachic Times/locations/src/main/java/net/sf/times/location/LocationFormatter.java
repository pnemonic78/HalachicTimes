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
     * @param elevation
     *         the elevation or altitude.
     * @return the coordinates text.
     */
    CharSequence formatCoordinates(double latitude, double longitude, double elevation);

    /**
     * Format a latitude coordinate.
     *
     * @param latitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLatitude(double latitude);

    /**
     * Format a latitude coordinate using the decimal notation.
     *
     * @param latitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLatitudeDecimal(double latitude);

    /**
     * Format a latitude coordinate using the sexagesimal notation.
     *
     * @param latitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLatitudeSexagesimal(double latitude);

    /**
     * Format a longitude coordinate.
     *
     * @param longitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLongitude(double longitude);

    /**
     * Format a longitude coordinate using the decimal notation.
     *
     * @param longitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLongitudeDecimal(double longitude);

    /**
     * Format a longitude coordinate using the sexagesimal notation.
     *
     * @param longitude
     *         the coordinate.
     * @return the coordinate text.
     */
    CharSequence formatLongitudeSexagesimal(double longitude);

    /**
     * Format an elevation.
     *
     * @param elevation
     *         the elevation.
     * @return the elevation text.
     */
    CharSequence formatElevation(double elevation);

}

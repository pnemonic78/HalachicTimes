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

    /**
     * Format a azimuth (bearing or yaw or compass angle).
     *
     * @param azimuth
     *         the azimuth.
     * @return the azimuth text.
     */
    CharSequence formatBearing(double azimuth);

    /**
     * Format a azimuth (bearing or yaw or compass angle) using the decimal notation.
     *
     * @param azimuth
     *         the azimuth.
     * @return the azimuth text.
     */
    CharSequence formatBearingDecimal(double azimuth);

    /**
     * Format a azimuth (bearing or yaw or compass angle) using the sexagesimal notation.
     *
     * @param azimuth
     *         the azimuth.
     * @return the azimuth text.
     */
    CharSequence formatBearingSexagesimal(double azimuth);

}

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

import static com.github.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL;
import static com.github.times.location.LocationsProvider.LATITUDE_MAX;
import static com.github.times.location.LocationsProvider.LATITUDE_MIN;
import static com.github.times.location.LocationsProvider.LONGITUDE_MAX;
import static com.github.times.location.LocationsProvider.LONGITUDE_MIN;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.github.util.LocaleUtils;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Default location formatter.
 *
 * @author Moshe Waisberg
 */
public class DefaultLocationFormatter implements LocationFormatter {

    /**
     * The context.
     */
    private final Context context;
    /**
     * The format notation.
     *
     * @see com.github.times.location.LocationPreferences.Values#FORMAT_DECIMAL
     * @see com.github.times.location.LocationPreferences.Values#FORMAT_SEXAGESIMAL
     */
    protected final String notation;
    protected final boolean isElevationVisible;
    /**
     * The coordinates format for decimal format.
     */
    private final String formatDecimal;
    /**
     * The coordinates format for decimal format with elevation.
     */
    private final String formatDecimalElevation;
    /**
     * The coordinates format for sexagesimal format.
     */
    private final String formatSexagesimal;
    /**
     * The coordinates format for sexagesimal format with elevation.
     */
    private final String formatSexagesimalElevation;
    /**
     * The elevation format.
     */
    private final String formatElevation;
    /**
     * The bearing/yaw format for decimal format.
     */
    private final DecimalFormat formatBearingDecimal;

    public DefaultLocationFormatter(Context context, String notation, boolean isElevationVisible) {
        this.context = context;
        this.notation = notation;
        this.isElevationVisible = isElevationVisible;

        formatDecimal = context.getString(R.string.location_decimal);
        formatDecimalElevation = context.getString(R.string.location_decimal_with_elevation);
        formatElevation = context.getString(R.string.location_elevation);

        formatSexagesimal = context.getString(R.string.location_sexagesimal);
        formatSexagesimalElevation = context.getString(R.string.location_sexagesimal_with_elevation);

        formatBearingDecimal = new DecimalFormat("###.#\u00B0");
    }

    @Override
    public String formatCoordinates(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final double altitude = location.getAltitude();
        return formatCoordinates(latitude, longitude, altitude);
    }

    @Override
    public String formatCoordinates(Address address) {
        final double latitude = address.getLatitude();
        final double longitude = address.getLongitude();
        double altitude = 0;
        if (address instanceof ZmanimAddress) {
            ZmanimAddress zaddress = (ZmanimAddress) address;
            if (zaddress.hasElevation()) {
                altitude = zaddress.getElevation();
            }
        }
        return formatCoordinates(latitude, longitude, altitude);
    }

    @Override
    public String formatCoordinates(double latitude, double longitude, double elevation) {
        final boolean elevated = isElevationVisible && !Double.isNaN(elevation);
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatCoordinatesSexagesimal(latitude, longitude, elevation, elevated);
        }
        return formatCoordinatesDecimal(latitude, longitude, elevation, elevated);
    }

    protected String formatCoordinatesDecimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitudeDecimal(latitude);
        final CharSequence longitudeText = formatLongitudeDecimal(longitude);

        if (withElevation) {
            final CharSequence elevationText = formatElevation(elevation);
            return String.format(Locale.US, formatDecimalElevation, latitudeText, longitudeText, elevationText);
        }
        return String.format(Locale.US, formatDecimal, latitudeText, longitudeText);
    }

    protected String formatCoordinatesSexagesimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitudeSexagesimal(latitude);
        final CharSequence longitudeText = formatLongitudeSexagesimal(longitude);
        final CharSequence elevationText = formatElevation(elevation);

        if (withElevation) {
            return String.format(Locale.US, formatSexagesimalElevation, latitudeText, longitudeText, elevationText);
        }
        return String.format(Locale.US, formatSexagesimal, latitudeText, longitudeText);
    }

    @Override
    public String formatLatitude(double latitude) {
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLatitudeSexagesimal(latitude);
        }
        return formatLatitudeDecimal(latitude);
    }

    @Override
    public String formatLatitudeDecimal(double coordinate) {
        return Location.convert(coordinate, Location.FORMAT_DEGREES);
    }

    @Override
    public String formatLatitudeSexagesimal(double coordinate) {
        return Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
    }

    @Override
    public String formatLongitude(double coordinate) {
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLongitudeSexagesimal(coordinate);
        }
        return formatLongitudeDecimal(coordinate);
    }

    @Override
    public String formatLongitudeDecimal(double coordinate) {
        return Location.convert(coordinate, Location.FORMAT_DEGREES);
    }

    @Override
    public String formatLongitudeSexagesimal(double coordinate) {
        return Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
    }

    @Override
    public String formatElevation(double elevation) {
        return String.format(Locale.US, formatElevation, elevation);
    }

    /**
     * Get the locale for formatting.
     *
     * @return the context's locale.
     */
    protected Locale getLocale() {
        return LocaleUtils.getDefaultLocale(context);
    }

    @Override
    public String formatBearing(double azimuth) {
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatBearingSexagesimal(azimuth);
        }
        return formatBearingDecimal(azimuth);
    }

    @Override
    public String formatBearingDecimal(double azimuth) {
        return formatBearingDecimal.format((azimuth + 360) % 360);
    }

    @Override
    public String formatBearingSexagesimal(double azimuth) {
        return formatBearingDecimal.format((azimuth + 360) % 360);
    }

    @Override
    public double parseLatitude(String coordinate) {
        if (coordinate.isEmpty()) return Double.NaN;
        try {
            double value = Location.convert(coordinate);
            if (value < LATITUDE_MIN) return Double.NaN;
            if (value > LATITUDE_MAX) return Double.NaN;
            return value;
        } catch (IllegalArgumentException ignored) {
        }
        return Double.NaN;
    }

    @Override
    public double parseLongitude(String coordinate) {
        if (coordinate.isEmpty()) return Double.NaN;
        try {
            double value = Location.convert(coordinate);
            if (value < LONGITUDE_MIN) return Double.NaN;
            if (value > LONGITUDE_MAX) return Double.NaN;
            return value;
        } catch (IllegalArgumentException ignored) {
        }
        return Double.NaN;
    }
}

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
import android.location.Address;
import android.location.Location;

import net.sf.util.LocaleUtils;

import java.text.DecimalFormat;
import java.util.Locale;

import static com.github.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL;

/**
 * Default location formatter.
 *
 * @author Moshe Waisberg
 */
public class DefaultLocationFormatter implements LocationFormatter {

    /** The context. */
    private final Context context;
    /** The preferences. */
    private LocationPreferences preferences;
    /** The coordinates format for decimal format. */
    private final String formatDecimal;
    /** The coordinates format for decimal format with elevation. */
    private final String formatDecimalElevation;
    /** The coordinates format for sexagesimal format. */
    private final String formatSexagesimal;
    /** The coordinates format for sexagesimal format with elevation. */
    private final String formatSexagesimalElevation;
    /** The elevation format. */
    private final String formatElevation;
    /** The bearing/yaw format for decimal format. */
    private final DecimalFormat formatBearingDecimal;

    public DefaultLocationFormatter(Context context) {
        this(context, null);
    }

    public DefaultLocationFormatter(Context context, LocationPreferences preferences) {
        this.context = context;
        this.preferences = (preferences != null) ? preferences : new SimpleLocationPreferences(context);

        formatDecimal = context.getString(R.string.location_decimal);
        formatDecimalElevation = context.getString(R.string.location_decimal_with_elevation);
        formatElevation = context.getString(R.string.location_elevation);

        formatSexagesimal = context.getString(R.string.location_sexagesimal);
        formatSexagesimalElevation = context.getString(R.string.location_sexagesimal_with_elevation);

        formatBearingDecimal = new DecimalFormat("###.#\u00B0");
    }

    @Override
    public CharSequence formatCoordinates(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final double altitude = location.getAltitude();
        return formatCoordinates(latitude, longitude, altitude);
    }

    @Override
    public CharSequence formatCoordinates(Address address) {
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
    public CharSequence formatCoordinates(double latitude, double longitude, double elevation) {
        final String notation = preferences.getCoordinatesFormat();
        final boolean elevated = !Double.isNaN(elevation) && preferences.isElevationVisible();
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatCoordinatesSexagesimal(latitude, longitude, elevation, elevated);
        }
        return formatCoordinatesDecimal(latitude, longitude, elevation, elevated);
    }

    protected CharSequence formatCoordinatesDecimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitudeDecimal(latitude);
        final CharSequence longitudeText = formatLongitudeDecimal(longitude);

        if (withElevation) {
            final CharSequence elevationText = formatElevation(elevation);
            return String.format(Locale.US, formatDecimalElevation, latitudeText, longitudeText, elevationText);
        }
        return String.format(Locale.US, formatDecimal, latitudeText, longitudeText);
    }

    protected CharSequence formatCoordinatesSexagesimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitudeSexagesimal(latitude);
        final CharSequence longitudeText = formatLongitudeSexagesimal(longitude);
        final CharSequence elevationText = formatElevation(elevation);

        if (withElevation) {
            return String.format(Locale.US, formatSexagesimalElevation, latitudeText, longitudeText, elevationText);
        }
        return String.format(Locale.US, formatSexagesimal, latitudeText, longitudeText);
    }

    @Override
    public CharSequence formatLatitude(double latitude) {
        final String notation = preferences.getCoordinatesFormat();
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLatitudeSexagesimal(latitude);
        }
        return formatLatitudeDecimal(latitude);
    }

    @Override
    public CharSequence formatLatitudeDecimal(double coordinate) {
        return Location.convert(coordinate, Location.FORMAT_DEGREES);
    }

    @Override
    public CharSequence formatLatitudeSexagesimal(double coordinate) {
        return Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
    }

    @Override
    public CharSequence formatLongitude(double coordinate) {
        final String notation = preferences.getCoordinatesFormat();
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLongitudeSexagesimal(coordinate);
        }
        return formatLongitudeDecimal(coordinate);
    }

    @Override
    public CharSequence formatLongitudeDecimal(double coordinate) {
        return Location.convert(coordinate, Location.FORMAT_DEGREES);
    }

    @Override
    public CharSequence formatLongitudeSexagesimal(double coordinate) {
        return Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
    }

    @Override
    public CharSequence formatElevation(double elevation) {
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
    public CharSequence formatBearing(double azimuth) {
        final String notation = preferences.getCoordinatesFormat();
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatBearingSexagesimal(azimuth);
        }
        return formatBearingDecimal(azimuth);
    }

    @Override
    public CharSequence formatBearingDecimal(double azimuth) {
        return formatBearingDecimal.format((Math.toDegrees(azimuth) + 360) % 360);
    }

    @Override
    public CharSequence formatBearingSexagesimal(double azimuth) {
        return formatBearingDecimal.format((Math.toDegrees(azimuth) + 360) % 360);
    }
}

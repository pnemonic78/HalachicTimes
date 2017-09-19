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
package net.sf.times.location;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import net.sf.util.LocaleUtils;

import java.util.Locale;

import static net.sf.times.location.LocationPreferences.Values.FORMAT_SEXAGESIMAL;

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
    /** The format for elevation. */
    private final String formatElevation;

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
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatCoordinatesSexagesimal(latitude, longitude, elevation, preferences.isElevationVisible());
        }
        return formatCoordinatesDecimal(latitude, longitude, elevation, preferences.isElevationVisible());
    }

    protected CharSequence formatCoordinatesDecimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitude(latitude);
        final CharSequence longitudeText = formatLongitude(longitude);
        final CharSequence elevationText = formatElevation(elevation);

        if (withElevation) {
            return String.format(Locale.US, formatDecimalElevation, latitudeText, longitudeText, elevationText);
        }
        return String.format(Locale.US, formatDecimal, latitudeText, longitudeText);
    }

    protected CharSequence formatCoordinatesSexagesimal(double latitude, double longitude, double elevation, boolean withElevation) {
        final CharSequence latitudeText = formatLatitude(latitude);
        final CharSequence longitudeText = formatLongitude(longitude);
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
}

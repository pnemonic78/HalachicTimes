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
import android.location.Address;
import android.location.Location;

import java.util.Locale;

/**
 * Default location formatter.
 *
 * @author Moshe Waisberg
 */
public class DefaultLocationFormatter implements LocationFormatter {

    /** The settings and preferences. */
    private LocationPreferences settings;
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
        settings = new LocationPreferences(context);

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
        final String notation = settings.getCoordinatesFormat();
        if (LocationPreferences.FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatCoordinatesSexagesimal(latitude, longitude, elevation, settings.isElevation());
        }
        return formatCoordinatesDecimal(latitude, longitude, elevation, settings.isElevation());
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
        final String notation = settings.getCoordinatesFormat();
        if (LocationPreferences.FORMAT_SEXAGESIMAL.equals(notation)) {
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
        final String notation = settings.getCoordinatesFormat();
        if (LocationPreferences.FORMAT_SEXAGESIMAL.equals(notation)) {
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
}

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

    /**
     * Constant used to specify formatting of a latitude or longitude in the
     * form "[+-]DDD.DDDDD" where D indicates degrees.
     */
    private static final String FORMAT_DEGREES = "%1$.6f";
    /** http://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_.28Annex_D.29 */
    private static final String FORMAT_SEXAGESIMAL = "%1$02d\u00B0%2$02d\u0027%3$02.3f\u005c\u0022%4$s";

    /** The settings and preferences. */
    private LocationSettings settings;
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
    private final String symbolNorth;
    private final String symbolSouth;
    private final String symbolEast;
    private final String symbolWest;

    public DefaultLocationFormatter(Context context) {
        settings = new LocationSettings(context);

        formatDecimal = context.getString(R.string.location_decimal);
        formatDecimalElevation = context.getString(R.string.location_decimal_with_elevation);
        formatElevation = context.getString(R.string.location_elevation);

        formatSexagesimal = context.getString(R.string.location_sexagesimal);
        formatSexagesimalElevation = context.getString(R.string.location_sexagesimal_with_elevation);
        symbolNorth = context.getString(R.string.north);
        symbolSouth = context.getString(R.string.south);
        symbolEast = context.getString(R.string.east);
        symbolWest = context.getString(R.string.west);
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
        if (LocationSettings.FORMAT_SEXAGESIMAL.equals(notation)) {
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
        if (LocationSettings.FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLatitudeSexagesimal(latitude);
        }
        return formatLatitudeDecimal(latitude);
    }

    protected CharSequence formatLatitudeDecimal(double coordinate) {
        return String.format(Locale.US, FORMAT_DEGREES, coordinate);
    }

    protected CharSequence formatLatitudeSexagesimal(double coordinate) {
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        String symbol = (degrees >= 0) ? symbolNorth : symbolSouth;
        return String.format(FORMAT_SEXAGESIMAL, degrees, minutes, seconds, symbol);
    }

    @Override
    public CharSequence formatLongitude(double coordinate) {
        final String notation = settings.getCoordinatesFormat();
        if (LocationSettings.FORMAT_SEXAGESIMAL.equals(notation)) {
            return formatLongitudeSexagesimal(coordinate);
        }
        return formatLongitudeDecimal(coordinate);
    }

    protected CharSequence formatLongitudeDecimal(double coordinate) {
        return String.format(Locale.US, FORMAT_DEGREES, coordinate);
    }

    protected CharSequence formatLongitudeSexagesimal(double coordinate) {
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        String symbol = (degrees >= 0) ? symbolEast : symbolWest;
        return String.format(FORMAT_SEXAGESIMAL, degrees, minutes, seconds, symbol);
    }

    @Override
    public CharSequence formatElevation(double elevation) {
        return String.format(Locale.US, formatElevation, elevation);
    }
}

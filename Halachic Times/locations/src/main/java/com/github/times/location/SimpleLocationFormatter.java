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
import static com.github.times.location.ZmanimLocation.LATITUDE_MAX;
import static com.github.times.location.ZmanimLocation.LATITUDE_MIN;
import static com.github.times.location.ZmanimLocation.LONGITUDE_MAX;
import static com.github.times.location.ZmanimLocation.LONGITUDE_MIN;

import android.content.Context;

import java.util.Locale;

/**
 * Simple location formatter.
 *
 * @author Moshe Waisberg
 */
public class SimpleLocationFormatter extends DefaultLocationFormatter {

    private static final char SYMBOL_DEGREES = '\u00B0';
    private static final char SYMBOL_MINUTES = '\u2032';
    private static final char SYMBOL_MINUTES_ASCII = '\'';
    private static final char SYMBOL_SECONDS = '\u2033';
    private static final char SYMBOL_SECONDS_ASCII = '"';
    /**
     * <a href="http://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_.28Annex_D.29">ISO-6709</a>
     */
    protected static final String PATTERN_SEXAGESIMAL = "%1$02d\u00B0%2$02d\u2032%3$02.3f\u2033%4$s";
    protected static final String PATTERN_SEXAGESIMAL_ROUND = "%1$02d\u00B0%2$02d\u2032%3$02d\u2033%4$s";

    private final String symbolNorth;
    private final String symbolSouth;
    private final String symbolEast;
    private final String symbolWest;

    /**
     * The bearing/yaw format for decimal format.
     */
    private final String formatBearingDecimal;
    /**
     * The bearing/yaw format for sexagesimal format.
     */
    private final String formatBearingSexagesimal;

    public SimpleLocationFormatter(Context context, LocationPreferences preferences) {
        this(context, preferences.getCoordinatesFormat(), preferences.isElevationVisible());
    }

    public SimpleLocationFormatter(Context context, String notation, boolean isElevationVisible) {
        super(context, notation, isElevationVisible);

        symbolNorth = context.getString(R.string.north);
        symbolSouth = context.getString(R.string.south);
        symbolEast = context.getString(R.string.east);
        symbolWest = context.getString(R.string.west);

        formatBearingDecimal = context.getString(R.string.bearing_decimal);
        formatBearingSexagesimal = context.getString(R.string.bearing_sexagesimal);
    }

    @Override
    public String formatLatitudeSexagesimal(double coordinate) {
        String symbol = (coordinate >= 0) ? symbolNorth : symbolSouth;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        return String.format(getLocale(), PATTERN_SEXAGESIMAL, Math.abs(degrees), minutes, seconds, symbol);
    }

    @Override
    public String formatLongitudeSexagesimal(double coordinate) {
        String symbol = (coordinate >= 0) ? symbolEast : symbolWest;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        return String.format(getLocale(), PATTERN_SEXAGESIMAL, Math.abs(degrees), minutes, seconds, symbol);
    }

    @Override
    public String formatBearingDecimal(double azimuth) {
        final double angle = (azimuth + 360) % 360;
        double bearing = 90 - Math.abs((angle % 180) - 90);
        String symbolLatitude = ((angle <= 90) || (angle >= 270)) ? symbolNorth : symbolSouth;
        String symbolLongitude = ((angle >= 0) && (angle <= 180)) ? symbolEast : symbolWest;

        return String.format(getLocale(), formatBearingDecimal, symbolLatitude, bearing, symbolLongitude);
    }

    @Override
    public String formatBearingSexagesimal(double azimuth) {
        final double angle = (azimuth + 360) % 360;
        double bearing = 90 - Math.abs((angle % 180) - 90);
        String symbolLatitude = ((angle <= 90) || (angle >= 270)) ? symbolNorth : symbolSouth;
        String symbolLongitude = ((angle >= 0) && (angle <= 180)) ? symbolEast : symbolWest;

        double coordinate = bearing;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        int seconds = (int) Math.floor(coordinate);
        final CharSequence bearingText = String.format(getLocale(), PATTERN_SEXAGESIMAL_ROUND, Math.abs(degrees), minutes, seconds, symbolLongitude);

        return String.format(Locale.US, formatBearingSexagesimal, symbolLatitude, bearingText);
    }

    @Override
    public double parseLatitude(String coordinate) {
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return parseLatitudeSexagesimal(coordinate);
        }
        return super.parseLatitude(coordinate);
    }

    @Override
    public double parseLongitude(String coordinate) {
        if (FORMAT_SEXAGESIMAL.equals(notation)) {
            return parseLongitudeSexagesimal(coordinate);
        }
        return super.parseLongitude(coordinate);
    }

    private double parseLatitudeSexagesimal(String coordinate) {
        boolean isNorth = true;
        if (coordinate.endsWith(symbolNorth)) {
            coordinate = coordinate.substring(0, coordinate.length() - symbolNorth.length());
        } else if (coordinate.endsWith(symbolSouth)) {
            isNorth = false;
            coordinate = coordinate.substring(0, coordinate.length() - symbolSouth.length());
        } else if (coordinate.endsWith(symbolEast)) {
            return Double.NaN;
        } else if (coordinate.endsWith(symbolWest)) {
            return Double.NaN;
        }

        try {
            int indexDegrees = coordinate.indexOf(SYMBOL_DEGREES);
            if (indexDegrees <= 0) indexDegrees = coordinate.length();
            String degrees = coordinate.substring(0, indexDegrees);
            double value = Integer.parseInt(degrees, 10);
            if (value < LATITUDE_MIN) return Double.NaN;
            if (value > LATITUDE_MAX) return Double.NaN;

            indexDegrees++;  // length(SYMBOL_MINUTES);
            if (indexDegrees < coordinate.length()) {
                coordinate = coordinate.substring(indexDegrees);
                int indexMinutes = coordinate.indexOf(SYMBOL_MINUTES);
                if (indexMinutes <= 0) indexMinutes = coordinate.indexOf(SYMBOL_MINUTES_ASCII);
                if (indexMinutes <= 0) indexMinutes = coordinate.length();
                String minutes = coordinate.substring(0, indexMinutes);
                double mins = Integer.parseInt(minutes, 10);
                value += mins / 60.0;
                if (value < LATITUDE_MIN) return Double.NaN;
                if (value > LATITUDE_MAX) return Double.NaN;

                indexMinutes++;  // length(SYMBOL_MINUTES);
                if (indexMinutes < coordinate.length()) {
                    coordinate = coordinate.substring(indexMinutes);
                    int indexSeconds = coordinate.indexOf(SYMBOL_SECONDS);
                    if (indexSeconds <= 0) indexSeconds = coordinate.indexOf(SYMBOL_SECONDS_ASCII);
                    if (indexSeconds <= 0) indexSeconds = coordinate.length();
                    String seconds = coordinate.substring(0, indexSeconds);
                    double secs = Double.parseDouble(seconds);
                    value += secs / 3600.0;
                    if (value < LATITUDE_MIN) return Double.NaN;
                    if (value > LATITUDE_MAX) return Double.NaN;
                }
            }

            return isNorth ? value : -value;
        } catch (NumberFormatException ignore) {
        }
        return Double.NaN;
    }

    private double parseLongitudeSexagesimal(String coordinate) {
        boolean isEast = true;
        if (coordinate.endsWith(symbolEast)) {
            coordinate = coordinate.substring(0, coordinate.length() - symbolEast.length());
        } else if (coordinate.endsWith(symbolWest)) {
            isEast = false;
            coordinate = coordinate.substring(0, coordinate.length() - symbolWest.length());
        } else if (coordinate.endsWith(symbolNorth)) {
            return Double.NaN;
        } else if (coordinate.endsWith(symbolSouth)) {
            return Double.NaN;
        }

        try {
            int indexDegrees = coordinate.indexOf(SYMBOL_DEGREES);
            if (indexDegrees <= 0) indexDegrees = coordinate.length();
            String degrees = coordinate.substring(0, indexDegrees);
            double value = Integer.parseInt(degrees, 10);
            if (value < LONGITUDE_MIN) return Double.NaN;
            if (value > LONGITUDE_MAX) return Double.NaN;

            indexDegrees++;  // length(SYMBOL_MINUTES);
            if (indexDegrees < coordinate.length()) {
                coordinate = coordinate.substring(indexDegrees);
                int indexMinutes = coordinate.indexOf(SYMBOL_MINUTES);
                if (indexMinutes <= 0) indexMinutes = coordinate.indexOf(SYMBOL_MINUTES_ASCII);
                if (indexMinutes <= 0) indexMinutes = coordinate.length();
                String minutes = coordinate.substring(0, indexMinutes);
                double mins = Integer.parseInt(minutes, 10);
                value += mins / 60.0;
                if (value < LONGITUDE_MIN) return Double.NaN;
                if (value > LONGITUDE_MAX) return Double.NaN;

                indexMinutes++;  // length(SYMBOL_MINUTES);
                if (indexMinutes < coordinate.length()) {
                    coordinate = coordinate.substring(indexMinutes);
                    int indexSeconds = coordinate.indexOf(SYMBOL_SECONDS);
                    if (indexSeconds <= 0) indexSeconds = coordinate.indexOf(SYMBOL_SECONDS_ASCII);
                    if (indexSeconds <= 0) indexSeconds = coordinate.length();
                    String seconds = coordinate.substring(0, indexSeconds);
                    double secs = Double.parseDouble(seconds);
                    value += secs / 3600.0;
                    if (value < LONGITUDE_MIN) return Double.NaN;
                    if (value > LONGITUDE_MAX) return Double.NaN;
                }
            }

            return isEast ? value : -value;
        } catch (NumberFormatException ignore) {
        }
        return Double.NaN;
    }
}

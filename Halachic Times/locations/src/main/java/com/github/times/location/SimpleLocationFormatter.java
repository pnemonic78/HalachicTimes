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

import java.util.Locale;

/**
 * Simple location formatter.
 *
 * @author Moshe Waisberg
 */
public class SimpleLocationFormatter extends DefaultLocationFormatter {

    /** http://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_.28Annex_D.29 */
    protected static final String PATTERN_SEXAGESIMAL = "%1$02d\u00B0%2$02d\u0027%3$02.3f\u005c\u0022%4$s";
    protected static final String PATTERN_SEXAGESIMAL_ROUND = "%1$02d\u00B0%2$02d\u0027%3$02d\u005c\u0022%4$s";

    private final String symbolNorth;
    private final String symbolSouth;
    private final String symbolEast;
    private final String symbolWest;

    /** The bearing/yaw format for decimal format. */
    private final String formatBearingDecimal;
    /** The bearing/yaw format for sexagesimal format. */
    private final String formatBearingSexagesimal;

    public SimpleLocationFormatter(Context context) {
        this(context, null);
    }

    public SimpleLocationFormatter(Context context, LocationPreferences preferences) {
        super(context, preferences);

        symbolNorth = context.getString(R.string.north);
        symbolSouth = context.getString(R.string.south);
        symbolEast = context.getString(R.string.east);
        symbolWest = context.getString(R.string.west);

        formatBearingDecimal = context.getString(R.string.bearing_decimal);
        formatBearingSexagesimal = context.getString(R.string.bearing_sexagesimal);
    }

    @Override
    public CharSequence formatLatitudeSexagesimal(double coordinate) {
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
    public CharSequence formatLongitudeSexagesimal(double coordinate) {
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
    public CharSequence formatBearingDecimal(double azimuth) {
        final double angle = (Math.toDegrees(azimuth) + 360) % 360;
        double bearing = 90 - Math.abs((angle % 180) - 90);
        String symbolLatitude = ((angle <= 90) || (angle >= 270)) ? symbolNorth : symbolSouth;
        String symbolLongitude = ((angle >= 0) && (angle <= 180)) ? symbolEast : symbolWest;

        return String.format(getLocale(), formatBearingDecimal, symbolLatitude, bearing, symbolLongitude);
    }

    @Override
    public CharSequence formatBearingSexagesimal(double azimuth) {
        final double angle = (Math.toDegrees(azimuth) + 360) % 360;
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
}

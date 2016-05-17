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

/**
 * Simple location formatter.
 *
 * @author Moshe Waisberg
 */
public class SimpleLocationFormatter extends DefaultLocationFormatter {

    /** http://en.wikipedia.org/wiki/ISO_6709#Representation_at_the_human_interface_.28Annex_D.29 */
    protected static final String FORMAT_SEXAGESIMAL = "%1$02d\u00B0%2$02d\u0027%3$02.3f\u005c\u0022%4$s";

    private final String symbolNorth;
    private final String symbolSouth;
    private final String symbolEast;
    private final String symbolWest;

    public SimpleLocationFormatter(Context context) {
        super(context);

        symbolNorth = context.getString(R.string.north);
        symbolSouth = context.getString(R.string.south);
        symbolEast = context.getString(R.string.east);
        symbolWest = context.getString(R.string.west);
    }

    @Override
    protected CharSequence formatLatitudeSexagesimal(double coordinate) {
        String symbol = (coordinate >= 0) ? symbolNorth : symbolSouth;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        return String.format(FORMAT_SEXAGESIMAL, Math.abs(degrees), minutes, seconds, symbol);
    }

    @Override
    protected CharSequence formatLongitudeSexagesimal(double coordinate) {
        String symbol = (coordinate >= 0) ? symbolEast : symbolWest;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;
        return String.format(FORMAT_SEXAGESIMAL, Math.abs(degrees), minutes, seconds, symbol);
    }
}

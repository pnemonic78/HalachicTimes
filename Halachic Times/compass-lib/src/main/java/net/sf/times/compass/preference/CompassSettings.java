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
package net.sf.times.compass.preference;

import android.content.Context;

import net.sf.times.compass.lib.R;
import net.sf.times.location.LocationSettings;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class CompassSettings extends LocationSettings {

    /** Preference name for the compass bearing type. */
    public static final String KEY_COMPASS_BEARING = "compass.bearing";
    /** Preference name for showing summaries. */
    private static final String KEY_SUMMARIES = "summaries.visible";

    /** Calculates the bearing for a Great Circle (shortest distance). */
    public static String BEARING_GREAT_CIRCLE;
    /** Calculates the bearing for a Rhumb Line (constant angle). */
    public static String BEARING_RHUMB_LINE;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public CompassSettings(Context context) {
        super(context);
    }

    /**
     * Get the type of bearing for calculating compass direction.
     *
     * @return the bearing type - either {@link #BEARING_GREAT_CIRCLE} or {@link #BEARING_RHUMB_LINE}.
     */
    public String getBearing() {
        return preferences.getString(KEY_COMPASS_BEARING, context.getString(R.string.compass_bearing_defaultValue));
    }

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    public boolean isSummaries() {
        return preferences.getBoolean(KEY_SUMMARIES, context.getResources().getBoolean(R.bool.summaries_visible_defaultValue));
    }

    @Override
    protected int getTheme(String value) {
        if (LIST_THEME_LIGHT.equals(value)) {
            return R.style.Theme_Compass_Light;
        }
        return R.style.Theme_Compass_Dark;
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        LocationSettings.init(context);

        BEARING_GREAT_CIRCLE = context.getString(R.string.compass_bearing_value_circle);
        BEARING_RHUMB_LINE = context.getString(R.string.compass_bearing_value_rhumb);
    }
}

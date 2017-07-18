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
package net.sf.times.compass.preference;

import android.content.Context;

import net.sf.times.compass.lib.R;
import net.sf.times.location.LocationPreferences;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class CompassPreferences extends LocationPreferences {

    /** Preference name for the compass bearing type. */
    public static final String KEY_COMPASS_BEARING = "compass.bearing";
    /** Preference name for showing summaries. */
    private static final String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for showing summaries. */
    public static final String KEY_THEME_COMPASS = "theme.compass";

    /** Calculates the bearing for a Great Circle (shortest distance). */
    public static String BEARING_GREAT_CIRCLE;
    /** Calculates the bearing for a Rhumb Line (constant angle). */
    public static String BEARING_RHUMB_LINE;

    /** Original theme. */
    public static String COMPASS_THEME_ORIGINAL;
    /** Gold theme. */
    public static String COMPASS_THEME_GOLD;
    /** Silver theme. */
    public static String COMPASS_THEME_SILVER;
    /** Classic theme. */
    public static String COMPASS_THEME_CLASSIC;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public CompassPreferences(Context context) {
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
     * Get the compass colour theme.
     *
     * @return the theme id.
     */
    public int getCompassTheme() {
        String value = preferences.getString(KEY_THEME_COMPASS, context.getString(R.string.compass_theme_defaultValue));
        if (COMPASS_THEME_GOLD.equals(value)) {
            return R.style.Compass_Theme_Gold;
        }
        if (COMPASS_THEME_SILVER.equals(value)) {
            return R.style.Compass_Theme_Silver;
        }
        if (COMPASS_THEME_CLASSIC.equals(value)) {
            return R.style.Compass_Theme_Classic;
        }
        return R.style.Compass_Theme_Original;
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        LocationPreferences.init(context);

        BEARING_GREAT_CIRCLE = context.getString(R.string.compass_bearing_value_circle);
        BEARING_RHUMB_LINE = context.getString(R.string.compass_bearing_value_rhumb);

        COMPASS_THEME_CLASSIC = context.getString(R.string.compass_theme_value_classic);
        COMPASS_THEME_GOLD = context.getString(R.string.compass_theme_value_gold);
        COMPASS_THEME_ORIGINAL = context.getString(R.string.compass_theme_value_original);
        COMPASS_THEME_SILVER = context.getString(R.string.compass_theme_value_silver);
    }
}

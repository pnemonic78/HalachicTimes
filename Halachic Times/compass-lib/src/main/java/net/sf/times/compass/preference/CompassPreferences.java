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

import android.support.annotation.StyleRes;

/**
 * Compass preferences.
 *
 * @author Moshe Waisberg
 */
public interface CompassPreferences {

    /** Preference name for the compass bearing type. */
    String KEY_COMPASS_BEARING = "compass.bearing";
    /** Preference name for showing summaries. */
    String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for the compass theme. */
    String KEY_THEME_COMPASS = "theme.compass";

    class Values {
        /** Default summaries hidden. */
        public static boolean SUMMARIES_DEFAULT = false;

        /** The default bearing. */
        public static String BEARING_DEFAULT;
        /** Calculates the bearing for a Great Circle (shortest distance). */
        public static String BEARING_GREAT_CIRCLE;
        /** Calculates the bearing for a Rhumb Line (constant angle). */
        public static String BEARING_RHUMB_LINE;

        /** Original theme. */
        public static String THEME_ORIGINAL;
        /** Gold theme. */
        public static String THEME_GOLD;
        /** Silver theme. */
        public static String THEME_SILVER;
        /** Classic theme. */
        public static String THEME_CLASSIC;
    }

    /**
     * Get the theme value.
     *
     * @return the theme value.
     */
    String getCompassThemeValue();

    /**
     * Get the theme.
     *
     * @param value
     *         the theme value.
     * @return the theme resource id.
     * @see #getCompassThemeValue()
     */
    @StyleRes
    int getCompassTheme(String value);

    /**
     * Get the theme.
     *
     * @return the theme resource id.
     */
    @StyleRes
    int getCompassTheme();

    /**
     * Get the type of bearing for calculating compass direction.
     *
     * @return the bearing type - either {@link Values#BEARING_GREAT_CIRCLE} or {@link Values#BEARING_RHUMB_LINE}.
     */
    String getBearing();

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    boolean isSummariesVisible();
}

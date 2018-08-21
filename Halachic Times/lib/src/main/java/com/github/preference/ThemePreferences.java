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
package com.github.preference;

import androidx.annotation.StyleRes;

/**
 * Theme preferences.
 *
 * @author Moshe Waisberg
 */
public interface ThemePreferences {

    /** Preference name for the theme. */
    String KEY_THEME = "theme";

    class Values {
        /** Default theme. */
        public static String THEME_DEFAULT;
        /** Dark theme. */
        public static String THEME_DARK;
        /** Light theme. */
        public static String THEME_LIGHT;
    }

    /**
     * Get the theme value.
     *
     * @return the theme value.
     */
    String getThemeValue();

    /**
     * Get the theme.
     *
     * @param value the theme value.
     * @return the theme resource id.
     * @see #getThemeValue()
     */
    @StyleRes
    int getTheme(String value);

    /**
     * Get the theme.
     *
     * @return the theme resource id.
     */
    @StyleRes
    int getTheme();

    /**
     * Is the theme dark?
     *
     * @param value the theme value.
     * @return {@code true} if the theme has dark backgrounds and light texts.
     */
    boolean isDarkTheme(String value);

    /**
     * Is the theme dark?
     *
     * @return {@code true} if the theme has dark backgrounds and light texts.
     */
    boolean isDarkTheme();
}

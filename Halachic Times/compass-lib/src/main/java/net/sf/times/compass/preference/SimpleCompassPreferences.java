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
import android.content.res.Resources;

import net.sf.preference.SimplePreferences;
import net.sf.times.compass.lib.R;

import static net.sf.preference.ThemePreferences.Values.THEME_DEFAULT;
import static net.sf.times.compass.preference.CompassPreferences.Values.BEARING_DEFAULT;
import static net.sf.times.compass.preference.CompassPreferences.Values.BEARING_GREAT_CIRCLE;
import static net.sf.times.compass.preference.CompassPreferences.Values.BEARING_RHUMB_LINE;
import static net.sf.times.compass.preference.CompassPreferences.Values.SUMMARIES_DEFAULT;
import static net.sf.times.compass.preference.CompassPreferences.Values.THEME_CLASSIC;
import static net.sf.times.compass.preference.CompassPreferences.Values.THEME_GOLD;
import static net.sf.times.compass.preference.CompassPreferences.Values.THEME_ORIGINAL;
import static net.sf.times.compass.preference.CompassPreferences.Values.THEME_SILVER;

/**
 * Simple compass preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleCompassPreferences extends SimplePreferences implements CompassPreferences {

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public SimpleCompassPreferences(Context context) {
        super(context);
        init(context);
    }

    @Override
    public String getBearing() {
        return preferences.getString(KEY_COMPASS_BEARING, BEARING_DEFAULT);
    }

    @Override
    public void setBearing(String bearing) {
        preferences.edit().putString(KEY_COMPASS_BEARING, bearing).apply();
    }

    @Override
    public boolean isSummariesVisible() {
        return preferences.getBoolean(KEY_SUMMARIES, SUMMARIES_DEFAULT);
    }

    @Override
    public void setSummariesVisible(boolean visible) {
        preferences.edit().putBoolean(KEY_SUMMARIES, visible).apply();
    }

    @Override
    public String getThemeValue() {
        return preferences.getString(KEY_THEME_COMPASS, THEME_DEFAULT);
    }

    @Override
    public int getTheme(String value) {
        if (THEME_GOLD.equals(value)) {
            return R.style.Compass_Theme_Gold;
        }
        if (THEME_SILVER.equals(value)) {
            return R.style.Compass_Theme_Silver;
        }
        if (THEME_CLASSIC.equals(value)) {
            return R.style.Compass_Theme_Classic;
        }
        return R.style.Compass_Theme_Original;
    }

    @Override
    public int getTheme() {
        return getTheme(getThemeValue());
    }

    @Override
    public void setTheme(String value) {
        preferences.edit().putString(KEY_THEME_COMPASS, value).apply();
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        final Resources res = context.getResources();

        SUMMARIES_DEFAULT = res.getBoolean(R.bool.summaries_visible_defaultValue);

        BEARING_DEFAULT = res.getString(R.string.compass_bearing_defaultValue);
        BEARING_GREAT_CIRCLE = res.getString(R.string.compass_bearing_value_circle);
        BEARING_RHUMB_LINE = res.getString(R.string.compass_bearing_value_rhumb);

        THEME_DEFAULT = res.getString(R.string.compass_theme_defaultValue);
        THEME_CLASSIC = res.getString(R.string.compass_theme_value_classic);
        THEME_GOLD = res.getString(R.string.compass_theme_value_gold);
        THEME_ORIGINAL = res.getString(R.string.compass_theme_value_original);
        THEME_SILVER = res.getString(R.string.compass_theme_value_silver);
    }
}

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
package net.sf.preference;

import android.content.Context;

import net.sf.lib.R;

import static net.sf.preference.ThemePreferences.Themes.THEME_DARK;
import static net.sf.preference.ThemePreferences.Themes.THEME_DEFAULT;
import static net.sf.preference.ThemePreferences.Themes.THEME_LIGHT;

/**
 * Theme preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleThemePreferences extends SimplePreferences implements ThemePreferences {

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public SimpleThemePreferences(Context context) {
        super(context);
        init(context);
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        THEME_DEFAULT = context.getString(R.string.theme_defaultValue);
        THEME_DARK = context.getString(R.string.theme_value_dark);
        THEME_LIGHT = context.getString(R.string.theme_value_light);
    }

    @Override
    public String getThemeValue() {
        return preferences.getString(KEY_THEME, THEME_DEFAULT);
    }

    @Override
    public int getTheme(String value) {
        if (THEME_LIGHT.equals(value)) {
            return R.style.Theme_Base_Light;
        }
        return R.style.Theme_Base;
    }

    @Override
    public int getTheme() {
        return getTheme(getThemeValue());
    }

    @Override
    public void setTheme(String value) {
        preferences.edit().putString(KEY_THEME, value).apply();
    }
}

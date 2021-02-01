/*
 * Copyright 2021, Moshe Waisberg
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
package com.github.times.preference;

import android.content.Context;

import com.github.times.R;
import com.github.times.compass.preference.SimpleCompassPreferences;

import static com.github.preference.ThemePreferences.Values.THEME_DARK;
import static com.github.preference.ThemePreferences.Values.THEME_LIGHT;
import static com.github.times.preference.ZmanimPreferences.Values.THEME_WHITE;

/**
 * Zmanim compass preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class ZmanimCompassPreferences extends SimpleCompassPreferences {

    /**
     * Constructs a new settings.
     *
     * @param context the context.
     */
    public ZmanimCompassPreferences(Context context) {
        super(context);
    }

    @Override
    public int getTheme(String value) {
        if (THEME_DARK.equals(value)) {
            return R.style.Theme_CompassApp_Dark;
        }
        if (THEME_LIGHT.equals(value)) {
            return R.style.Theme_CompassApp_Light;
        }
        if (THEME_WHITE.equals(value)) {
            return R.style.Theme_CompassApp_Light;
        }
        return R.style.Theme_CompassApp_DayNight;
    }
}

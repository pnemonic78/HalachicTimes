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
package com.github.app;

import android.content.Context;

import com.github.preference.SimpleThemePreferences;
import com.github.preference.ThemePreferences;

/**
 * Simple theme callback implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleThemeCallbacks<TP extends ThemePreferences> implements ThemeCallbacks<TP> {

    private final Context context;
    private TP preferences;

    public SimpleThemeCallbacks(Context context) {
        this(context, null);
    }

    public SimpleThemeCallbacks(Context context, TP preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    @Override
    public void onCreate() {
        context.setTheme(getThemePreferences().getTheme());
    }

    @Override
    public TP getThemePreferences() {
        if (preferences == null) {
            preferences = createPreferences(context);
        }
        return preferences;
    }

    protected TP createPreferences(Context context) {
        return (TP) new SimpleThemePreferences(context);
    }
}

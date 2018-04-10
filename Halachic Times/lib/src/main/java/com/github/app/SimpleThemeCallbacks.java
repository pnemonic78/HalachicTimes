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
import android.content.ContextWrapper;

import net.sf.preference.SimpleThemePreferences;
import net.sf.preference.ThemePreferences;

/**
 * Simple theme callback implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleThemeCallbacks<P extends ThemePreferences> implements ThemeCallbacks<P> {

    private final ContextWrapper context;
    private P preferences;

    public SimpleThemeCallbacks(ContextWrapper context) {
        this(context, null);
    }

    public SimpleThemeCallbacks(ContextWrapper context, P preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    @Override
    public void onCreate() {
        context.setTheme(getThemePreferences().getTheme());
    }

    @Override
    public P getThemePreferences() {
        if (preferences == null) {
            preferences = createPreferences(context);
        }
        return preferences;
    }

    protected P createPreferences(Context context) {
        return (P) new SimpleThemePreferences(context);
    }
}

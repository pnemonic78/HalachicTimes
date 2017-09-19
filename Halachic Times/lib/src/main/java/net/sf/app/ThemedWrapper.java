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
package net.sf.app;

import android.content.Context;
import android.content.ContextWrapper;

import net.sf.preference.SimpleThemePreferences;
import net.sf.preference.ThemePreferences;

/**
 * Wraps a callback delegate.
 *
 * @author Moshe Waisberg
 */
public class ThemedWrapper<P extends ThemePreferences> implements ThemedCallbacks<P> {

    private final ContextWrapper context;
    private P preferences;

    public ThemedWrapper(ContextWrapper context) {
        this.context = context;
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

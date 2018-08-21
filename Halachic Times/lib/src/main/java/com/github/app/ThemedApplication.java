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

import android.app.Application;
import android.content.Context;

import com.github.preference.ThemePreferences;

import androidx.annotation.NonNull;

/**
 * Application with a theme.
 *
 * @author Moshe Waisberg
 */
@Deprecated
public abstract class ThemedApplication<P extends ThemePreferences> extends Application implements ThemeCallbacks<P> {

    private ThemeCallbacks<P> themeCallbacks;

    private ThemeCallbacks<P> getThemeCallbacks() {
        if (themeCallbacks == null) {
            themeCallbacks = new SimpleThemeCallbacks<P>(this, createPreferences(this));
        }
        return themeCallbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getThemeCallbacks().onCreate();
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    @NonNull
    protected abstract P createPreferences(Context context);
}

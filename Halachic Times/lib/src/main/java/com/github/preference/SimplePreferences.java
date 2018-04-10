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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.content.ProviderPreferences;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class SimplePreferences {

    protected final Context context;
    protected final SharedPreferences preferences;

    /**
     * Constructs a new settings.
     *
     * @param context the context.
     */
    public SimplePreferences(Context context) {
        this(context, false);
    }

    /**
     * Constructs a new settings.
     *
     * @param context      the context.
     * @param multiProcess is the preferences used for a multi-process application?
     */
    public SimplePreferences(Context context, boolean multiProcess) {
        this.context = context;
        this.preferences = multiProcess ? new ProviderPreferences(context) : PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the preferences.
     *
     * @return the shared preferences.
     */
    public SharedPreferences getPreferences() {
        return preferences;
    }
}

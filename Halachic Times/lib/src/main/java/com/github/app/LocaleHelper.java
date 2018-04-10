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

import android.app.Activity;
import android.content.Context;

import com.github.preference.LocalePreferences;
import com.github.preference.SimpleLocalePreferences;

import static com.github.app.ActivityUtils.restTitle;
import static com.github.util.LocaleUtils.applyLocale;

/**
 * Wraps a callback delegate.
 *
 * @author Moshe Waisberg
 */
public class LocaleHelper<P extends LocalePreferences> implements LocaleCallbacks<P> {

    private final P preferences;

    public LocaleHelper(Context context) {
        this.preferences = (P) new SimpleLocalePreferences(context);
    }

    @Override
    public P getLocalePreferences() {
        return preferences;
    }

    @Override
    public Context attachBaseContext(Context context) {
        return applyLocale(context, preferences.getLocale());
    }

    @Override
    public void onCreate(Context context) {
        if (context instanceof Activity) {
            onCreate((Activity) context);
        }
    }

    protected void onCreate(Activity activity) {
        restTitle(activity);
    }
}

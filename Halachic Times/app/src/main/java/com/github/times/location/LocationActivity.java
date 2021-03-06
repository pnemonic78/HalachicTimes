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
package com.github.times.location;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.LocalePreferences;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;

/**
 * Pick a city from the list.
 *
 * @author Moshe Waisberg
 */
public class LocationActivity extends LocationTabActivity<ZmanimPreferences> {

    private LocaleCallbacks<LocalePreferences> localeCallbacks;
    /** The preferences. */
    private ZmanimPreferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyOverrideConfiguration(context.getResources().getConfiguration());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localeCallbacks.onCreate(this);
    }

    @Override
    protected Class<? extends Activity> getAddLocationActivityClass() {
        return ZmanimAddLocationActivity.class;
    }

    @Override
    protected ThemeCallbacks<ZmanimPreferences> createThemeCallbacks() {
        return new SimpleThemeCallbacks<>(this, getZmanimPreferences());
    }

    public ZmanimPreferences getZmanimPreferences() {
        ZmanimPreferences preferences = this.preferences;
        if (preferences == null) {
            preferences = new SimpleZmanimPreferences(this);
            this.preferences = preferences;
        }
        return preferences;
    }
}

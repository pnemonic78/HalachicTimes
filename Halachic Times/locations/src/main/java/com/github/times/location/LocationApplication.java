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

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;

import static android.content.Intent.ACTION_LOCALE_CHANGED;

/**
 * Location application.
 *
 * @author Moshe Waisberg
 */
public abstract class LocationApplication<TP extends ThemePreferences, AP extends AddressProvider, LP extends LocationsProvider> extends Application implements ThemeCallbacks<TP> {

    private final ThemeCallbacks<TP> themeCallbacks = new SimpleThemeCallbacks<>(this);
    private LocationHolder<AP, LP> locationHolder;
    private final BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ACTION_LOCALE_CHANGED.equals(action)) {
                Configuration newConfig = context.getResources().getConfiguration();
                onConfigurationChanged(newConfig);
            }
        }
    };

    @Override
    public void onPreCreate() {
        super.onCreate();
        themeCallbacks.onPreCreate();
    }

    @Override
    public TP getThemePreferences() {
        return themeCallbacks.getThemePreferences();
    }

    @NonNull
    protected LocationHolder<AP, LP> getLocationHolder() {
        LocationHolder<AP, LP> holder = locationHolder;
        if (holder == null) {
            final Context context = getApplicationContext();
            holder = new LocationHolder<>(createProviderFactory(context));
            this.locationHolder = holder;
            registerComponentCallbacks(holder);

            IntentFilter intentFilter = new IntentFilter(ACTION_LOCALE_CHANGED);
            LocalBroadcastManager.getInstance(context).registerReceiver(localeReceiver, intentFilter);
        }
        return holder;
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    public AP getAddresses() {
        return getLocationHolder().getAddresses();
    }

    @NonNull
    protected abstract LocationsProviderFactory<AP, LP> createProviderFactory(Context context);

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public LP getLocations() {
        return getLocationHolder().getLocations();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopLocationHolder();
    }

    private void stopLocationHolder() {
        final LocationHolder<AP, LP> locationHolder = this.locationHolder;
        if (locationHolder != null) {
            this.locationHolder = null;
            locationHolder.onTerminate();
            unregisterComponentCallbacks(locationHolder);
        }
    }
}

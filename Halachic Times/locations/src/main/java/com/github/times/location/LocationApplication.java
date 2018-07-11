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
import android.content.Context;
import android.support.annotation.NonNull;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;

/**
 * Location application.
 *
 * @author Moshe Waisberg
 */
public abstract class LocationApplication<TP extends ThemePreferences, AP extends AddressProvider, LP extends LocationsProvider> extends Application implements ThemeCallbacks<TP> {

    private final ThemeCallbacks<TP> themeCallbacks = new SimpleThemeCallbacks<TP>(this);
    private LocationHolder<AP, LP> locationHolder;

    @Override
    public void onCreate() {
        super.onCreate();
        themeCallbacks.onCreate();
    }

    @Override
    public TP getThemePreferences() {
        return themeCallbacks.getThemePreferences();
    }

    @NonNull
    protected LocationHolder<AP, LP> getLocationHolder() {
        if (locationHolder == null) {
            final Context context = this;
            locationHolder = new LocationHolder(createProviderFactory(context));
            registerComponentCallbacks(locationHolder);
        }
        return locationHolder;
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

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        stopLocationHolder();
    }

    private void stopLocationHolder() {
        final LocationHolder locationHolder = this.locationHolder;
        if (locationHolder != null) {
            unregisterComponentCallbacks(locationHolder);
        }
    }
}

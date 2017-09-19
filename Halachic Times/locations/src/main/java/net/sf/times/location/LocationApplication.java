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
package net.sf.times.location;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import net.sf.app.SimpleThemeCallbacks;
import net.sf.app.ThemeCallbacks;
import net.sf.preference.ThemePreferences;

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
            locationHolder = new LocationHolder<>(createAddressProvider(this), createLocationsProvider(this));
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

    protected AP createAddressProvider(Context context) {
        return null;
    }

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public LP getLocations() {
        return getLocationHolder().getLocations();
    }

    protected LP createLocationsProvider(Context context) {
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        final LocationHolder locationHolder = getLocationHolder();
        unregisterComponentCallbacks(locationHolder);
        locationHolder.onTerminate();
    }
}

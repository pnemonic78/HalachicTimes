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
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

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
    /** Provider for addresses. */
    private AP addressProvider;
    /** Provider for locations. */
    private LP locations;

    @Override
    public void onCreate() {
        themeCallbacks.onCreate();
        super.onCreate();
    }

    @Override
    public TP getThemePreferences() {
        return themeCallbacks.getThemePreferences();
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    public AP getAddresses() {
        if (addressProvider == null) {
            addressProvider = createAddressProvider(this);
        }
        return addressProvider;
    }

    protected AP createAddressProvider(Context context) {
        return (AP) new AddressProvider(context);
    }

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public LP getLocations() {
        if (locations == null) {
            locations = createLocationsProvider(this);
        }
        return locations;
    }

    protected LP createLocationsProvider(Context context) {
        return (LP) new LocationsProvider(context);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dispose();
        SQLiteDatabase.releaseMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dispose();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dispose();
    }

    private void dispose() {
        if (addressProvider != null) {
            addressProvider.close();
            addressProvider = null;
        }
        if (locations != null) {
            locations.quit();
            locations = null;
        }
    }
}

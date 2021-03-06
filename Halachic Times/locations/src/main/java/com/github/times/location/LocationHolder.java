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

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

import com.github.times.location.impl.LocationsProviderFactoryImpl;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * Holder for locations.
 *
 * @author Moshe Waisberg
 */
public class LocationHolder<AP extends AddressProvider, LP extends LocationsProvider> implements ComponentCallbacks2 {

    private final LocationsProviderFactory<AP, LP> factory;
    /** Provider for addresses. */
    private AP addressProvider;
    /** Provider for locations. */
    private LP locationsProvider;

    public LocationHolder(@NonNull LocationsProviderFactory<AP, LP> factory) {
        this.factory = factory;
        this.addressProvider = null;
        this.locationsProvider = null;
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    public AP getAddresses() {
        AP addressProvider = this.addressProvider;
        if (addressProvider == null) {
            addressProvider = factory.createAddressProvider();
            this.addressProvider = addressProvider;
        }
        return addressProvider;
    }

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public LP getLocations() {
        LP locationsProvider = this.locationsProvider;
        if (locationsProvider == null) {
            locationsProvider = factory.createLocationsProvider();
            this.locationsProvider = locationsProvider;
        }
        return locationsProvider;
    }

    @Override
    public void onLowMemory() {
        onTrimMemory(TRIM_MEMORY_COMPLETE);
    }

    @Override
    public void onTrimMemory(int level) {
        Timber.w("Trim memory: %d", level);
        dispose();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        Timber.w("Configuration Changed: %s", newConfig);
        dispose();
    }

    public void onTerminate() {
        dispose();
    }

    private void dispose() {
        if (addressProvider != null) {
            addressProvider.close();
            addressProvider = null;
        }
        if (locationsProvider != null) {
            locationsProvider.quit();
            locationsProvider = null;
        }
        SQLiteDatabase.releaseMemory();
    }
}

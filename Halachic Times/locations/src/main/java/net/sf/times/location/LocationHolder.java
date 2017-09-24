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

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

/**
 * Holder for locations.
 *
 * @author Moshe Waisberg
 */
public class LocationHolder<AP extends AddressProvider, LP extends LocationsProvider> implements ComponentCallbacks2 {

    private final Context context;
    /** Provider for addresses. */
    private AP addressProvider;
    /** Provider for locations. */
    private LP locationsProvider;

    public LocationHolder(Context context) {
        this.context = context;
        this.addressProvider = null;
        this.locationsProvider = null;
    }

    public LocationHolder(@NonNull AP addressProvider, @NonNull LP locationsProvider) {
        this.context = null;
        this.addressProvider = addressProvider;
        this.locationsProvider = locationsProvider;
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    public AP getAddresses() {
        if (addressProvider == null) {
            addressProvider = createAddressProvider(context);
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
        if (locationsProvider == null) {
            locationsProvider = createLocationsProvider(context);
        }
        return locationsProvider;
    }

    protected LP createLocationsProvider(Context context) {
        return (LP) new LocationsProvider(context);
    }

    @Override
    public void onLowMemory() {
        onTrimMemory(TRIM_MEMORY_RUNNING_LOW);
    }

    @Override
    public void onTrimMemory(int level) {
        dispose();
        SQLiteDatabase.releaseMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
    }

}

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
package com.github.times.location

import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import timber.log.Timber

/**
 * Holder for locations.
 *
 * @author Moshe Waisberg
 */
class LocationHolder<AP : AddressProvider, LP : LocationsProvider>(
    private val factory: LocationsProviderFactory<AP, LP>
) : ComponentCallbacks2 {
    /** Provider for addresses.  */
    private var addressProvider: AP? = null

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    val addresses: AP
        get() {
            var addressProvider = addressProvider
            if (addressProvider == null) {
                addressProvider = factory.createAddressProvider()
                this.addressProvider = addressProvider
            }
            return addressProvider
        }

    /** Provider for locations.  */
    private var locationsProvider: LP? = null

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    val locations: LP
        get() {
            var locationsProvider = locationsProvider
            if (locationsProvider == null) {
                locationsProvider = factory.createLocationsProvider()
                this.locationsProvider = locationsProvider
            }
            return locationsProvider
        }

    override fun onLowMemory() {
        onTrimMemory(TRIM_MEMORY_COMPLETE)
    }

    override fun onTrimMemory(level: Int) {
        Timber.w("Trim memory: %d", level)
        dispose()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Timber.w("Configuration changed: %s", newConfig)
        dispose()
    }

    fun onTerminate() {
        dispose()
    }

    private fun dispose() {
        addressProvider?.close()
        addressProvider = null
        locationsProvider?.quit()
        locationsProvider = null
        SQLiteDatabase.releaseMemory()
    }
}
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

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

interface ZmanimLocationListener : LocationListener {
    /**
     * Called when an address is found.
     *
     * @param location the requested location.
     * @param address  the address for the location.
     */
    fun onAddressChanged(location: Location, address: ZmanimAddress)

    /**
     * Called when an address is found.
     *
     * @param location the location with elevation.
     */
    fun onElevationChanged(location: Location)

    companion object {
        /**
         * The intent action for an address that was found.
         */
        const val ACTION_ADDRESS = "com.github.times.location.ADDRESS"

        /**
         * The intent action for a location with elevation that was found.
         */
        const val ACTION_ELEVATION = "com.github.times.location.ELEVATION"

        /**
         * The intent action for when a location changed.
         */
        const val ACTION_LOCATION_CHANGED = "com.github.times.location.LOCATION_CHANGED"

        /**
         * The location parameter.
         */
        const val EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED

        /**
         * The address parameter.
         */
        const val EXTRA_ADDRESS = "address"

        /**
         * Whether to persist the address?
         */
        const val EXTRA_PERSIST = "persist_address"
    }
}
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

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import androidx.core.app.JobIntentService
import com.github.times.location.AddressProvider.OnFindAddressListener
import timber.log.Timber

/**
 * Service to find an address.
 *
 * @author Moshe Waisberg
 */
class AddressService : JobIntentService(), OnFindAddressListener {
    private var addressProvider: AddressProvider? = null

    override fun onCreate() {
        super.onCreate()
        addressProvider = AddressProvider(this)
    }

    override fun onHandleWork(intent: Intent) {
        val extras = intent.extras ?: return
        if (extras.isEmpty) return
        val location = LocationData.from(extras, PARAMETER_LOCATION) ?: return
        val provider = addressProvider ?: return
        val action = intent.action
        if (ZmanimLocationListener.ACTION_ADDRESS == action) {
            if (extras.containsKey(PARAMETER_PERSIST)) {
                val locationExtras = location.extras ?: Bundle()
                locationExtras.putBoolean(
                    PARAMETER_PERSIST,
                    extras.getBoolean(PARAMETER_PERSIST, PERSIST_DEFAULT)
                )
                location.extras = locationExtras
            }
            provider.findNearestAddress(location, this)
        } else if (ZmanimLocationListener.ACTION_ELEVATION == action) {
            provider.findElevation(location, this)
        }
    }

    override fun onFindAddress(provider: AddressProvider, location: Location, address: Address) {
        val addr: ZmanimAddress?
        if (address is ZmanimAddress) {
            addr = address
        } else {
            addr = ZmanimAddress(address)
            if (location.hasAltitude()) {
                addr.elevation = location.altitude
            }
        }
        val extras = location.extras
        if (extras?.getBoolean(PARAMETER_PERSIST, PERSIST_DEFAULT) ?: PERSIST_DEFAULT) {
            provider.insertOrUpdateAddress(location, addr)
        }
        Timber.i("find address: %s %s", location, addr)
        val result = Intent(ZmanimLocationListener.ACTION_ADDRESS)
            .setPackage(packageName)
            .putExtra(PARAMETER_LOCATION, location)
            .putExtra(PARAMETER_ADDRESS, addr)
        sendBroadcast(result)
    }

    override fun onFindElevation(
        provider: AddressProvider,
        location: Location,
        elevated: Location
    ) {
        if (elevated is ZmanimLocation) {
            provider.insertOrUpdateElevation(elevated)
        }
        Timber.i("find elevation: %s %s", location, elevated)
        val result = Intent(ZmanimLocationListener.ACTION_ELEVATION)
            .setPackage(packageName)
            .putExtra(PARAMETER_LOCATION, elevated)
        sendBroadcast(result)
    }

    companion object {
        private const val JOB_ADDRESS = 0xADD7E55 // "ADDrESS"

        private const val PARAMETER_LOCATION = ZmanimLocationListener.EXTRA_LOCATION
        private const val PARAMETER_ADDRESS = ZmanimLocationListener.EXTRA_ADDRESS
        private const val PARAMETER_PERSIST = ZmanimLocationListener.EXTRA_PERSIST

        private const val PERSIST_DEFAULT = true

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, AddressService::class.java, JOB_ADDRESS, intent)
        }
    }
}
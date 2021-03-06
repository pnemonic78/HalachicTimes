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

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.github.times.location.AddressProvider.OnFindAddressListener;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import timber.log.Timber;

import static com.github.times.location.ZmanimLocationListener.ACTION_ADDRESS;
import static com.github.times.location.ZmanimLocationListener.ACTION_ELEVATION;

/**
 * Service to find an address.
 *
 * @author Moshe Waisberg
 */
public class AddressService extends JobIntentService implements OnFindAddressListener {

    private static final int JOB_ADDRESS = 0xADD7E55; // "ADDrESS"

    private static final String PARAMETER_LOCATION = ZmanimLocationListener.EXTRA_LOCATION;
    private static final String PARAMETER_ADDRESS = ZmanimLocationListener.EXTRA_ADDRESS;
    private static final String PARAMETER_PERSIST = ZmanimLocationListener.EXTRA_PERSIST;

    private static final boolean PERSIST_DEFAULT = true;

    private AddressProvider addressProvider;

    public static void enqueueWork(@NonNull Context context, @NonNull Intent intent) {
        enqueueWork(context, AddressService.class, JOB_ADDRESS, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        addressProvider = new AddressProvider(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final Bundle extras = intent.getExtras();
        if ((extras == null) || extras.isEmpty()) {
            return;
        }
        final Location location = extras.getParcelable(PARAMETER_LOCATION);
        if (location == null) {
            return;
        }

        final AddressProvider provider = addressProvider;
        if (provider == null)
            return;
        final String action = intent.getAction();
        if (ACTION_ADDRESS.equals(action)) {
            if (extras.containsKey(PARAMETER_PERSIST)) {
                Bundle locationExtras = location.getExtras();
                if (locationExtras == null) {
                    locationExtras = new Bundle();
                }
                locationExtras.putBoolean(PARAMETER_PERSIST, extras.getBoolean(PARAMETER_PERSIST, PERSIST_DEFAULT));
                location.setExtras(locationExtras);
            }
            provider.findNearestAddress(location, this);
        } else if (ACTION_ELEVATION.equals(action)) {
            provider.findElevation(location, this);
        }
    }

    @Override
    public void onFindAddress(AddressProvider provider, Location location, Address address) {
        ZmanimAddress addr = null;
        if (address != null) {
            if (address instanceof ZmanimAddress) {
                addr = (ZmanimAddress) address;
            } else {
                addr = new ZmanimAddress(address);
                if (location.hasAltitude()) {
                    addr.setElevation(location.getAltitude());
                }
            }
            Bundle extras = location.getExtras();
            if ((extras == null) || extras.getBoolean(PARAMETER_PERSIST, PERSIST_DEFAULT)) {
                provider.insertOrUpdateAddress(location, addr);
            }
        }

        Timber.i("find address: %s %s", location, addr);
        Intent result = new Intent(ACTION_ADDRESS);
        result.setPackage(getPackageName());
        result.putExtra(PARAMETER_LOCATION, location);
        result.putExtra(PARAMETER_ADDRESS, addr);
        sendBroadcast(result);
    }

    @Override
    public void onFindElevation(AddressProvider provider, Location location, Location elevated) {
        if (elevated instanceof ZmanimLocation) {
            provider.insertOrUpdateElevation((ZmanimLocation) elevated);

            Timber.i("find elevation: %s %s", location, elevated);
            Intent result = new Intent(ACTION_ELEVATION);
            result.setPackage(getPackageName());
            result.putExtra(PARAMETER_LOCATION, elevated);
            sendBroadcast(result);
        }
    }
}

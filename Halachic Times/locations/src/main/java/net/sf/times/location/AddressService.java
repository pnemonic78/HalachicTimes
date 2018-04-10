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

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.JobIntentService;

import net.sf.times.location.AddressProvider.OnFindAddressListener;

import static net.sf.times.location.ZmanimLocationListener.ACTION_ADDRESS;
import static net.sf.times.location.ZmanimLocationListener.ACTION_ELEVATION;

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

    private static final boolean PERSIST = true;

    private AddressProvider addressProvider;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, AddressService.class, JOB_ADDRESS, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        addressProvider = new AddressProvider(this);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent == null) {
            return;
        }
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
                locationExtras.putBoolean(PARAMETER_PERSIST, extras.getBoolean(PARAMETER_PERSIST, PERSIST));
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
            if ((extras == null) || extras.getBoolean(PARAMETER_PERSIST, PERSIST)) {
                provider.insertOrUpdateAddress(location, addr);
            }
        }

        Intent result = new Intent(ACTION_ADDRESS);
        result.setPackage(getPackageName());
        result.putExtra(PARAMETER_LOCATION, location);
        result.putExtra(PARAMETER_ADDRESS, addr);
        sendBroadcast(result);
    }

    @Override
    public void onFindElevation(AddressProvider provider, Location location, ZmanimLocation elevated) {
        if (elevated != null) {
            provider.insertOrUpdateElevation(elevated);

            Intent result = new Intent(ACTION_ELEVATION);
            result.setPackage(getPackageName());
            result.putExtra(PARAMETER_LOCATION, elevated);
            sendBroadcast(result);
        }
    }
}

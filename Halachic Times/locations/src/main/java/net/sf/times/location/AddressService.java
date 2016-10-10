/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import net.sf.times.location.AddressProvider.OnFindAddressListener;

/**
 * Service to find an address.
 *
 * @author Moshe Waisberg
 */
public class AddressService extends IntentService implements OnFindAddressListener {

    private static final String ACTION_ADDRESS = ZmanimLocationListener.ACTION_ADDRESS;
    private static final String ACTION_ELEVATION = ZmanimLocationListener.ACTION_ELEVATION;
    private static final String PARAMETER_LOCATION = ZmanimLocationListener.EXTRA_LOCATION;
    private static final String PARAMETER_ADDRESS = ZmanimLocationListener.EXTRA_ADDRESS;
    private static final String PARAMETER_PERSIST = ZmanimLocationListener.EXTRA_PERSIST;

    private static final boolean PERSIST = true;

    private static final String NAME = "AddressService";

    private AddressProvider addressProvider;

    /**
     * Constructs a new service.
     *
     * @param name
     *         the worker thread name.
     */
    public AddressService(String name) {
        super(name);
    }

    /**
     * Constructs a new service.
     */
    public AddressService() {
        this(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;
        Location location = extras.getParcelable(PARAMETER_LOCATION);
        if (location == null)
            return;

        final AddressProvider provider = addressProvider;
        if (provider == null)
            return;
        String action = intent.getAction();
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
    public void onCreate() {
        super.onCreate();
        LocationApplication app = (LocationApplication) getApplication();
        addressProvider = app.getAddresses();
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

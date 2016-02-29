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

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

/**
 * Location application.
 *
 * @author Moshe Waisberg
 */
public class LocationApplication<AP extends AddressProvider, LP extends LocationsProvider> extends Application {

    /** Provider for addresses. */
    private AP addressProvider;
    /** Provider for locations. */
    private LP locations;

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
    public void onCreate() {
        super.onCreate();
        LocationSettings.init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        SQLiteDatabase.releaseMemory();
    }

    @Override
    public void onTerminate() {
        dispose();
        super.onTerminate();
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

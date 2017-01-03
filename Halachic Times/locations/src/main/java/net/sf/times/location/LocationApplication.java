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

import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

import net.sf.app.ThemedApplication;

/**
 * Location application.
 *
 * @author Moshe Waisberg
 */
public abstract class LocationApplication<P extends LocationPreferences, AP extends AddressProvider, LP extends LocationsProvider> extends ThemedApplication {

    /** Provider for addresses. */
    private AP addressProvider;
    /** Provider for locations. */
    private LP locations;

    @Override
    protected void initPreferences() {
        super.initPreferences();
        P.init(this);
    }

    @Override
    protected P createPreferences(Context context) {
        return (P) new LocationPreferences(context);
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

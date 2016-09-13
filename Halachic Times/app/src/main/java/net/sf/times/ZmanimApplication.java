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
package net.sf.times;

import android.content.Context;

import net.sf.times.location.AddressProvider;
import net.sf.times.location.LocationApplication;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimPreferences;

/**
 * Zmanim application.
 *
 * @author Moshe Waisberg
 */
public class ZmanimApplication extends LocationApplication<AddressProvider, ZmanimLocations> {

    @Override
    public void onCreate() {
        ZmanimPreferences.init(this);
        super.onCreate();
    }

    @Override
    protected ZmanimLocations createLocationsProvider(Context context) {
        return new ZmanimLocations(context);
    }

    @Override
    protected ZmanimPreferences createPreferences(Context context) {
        return new ZmanimPreferences(context);
    }
}

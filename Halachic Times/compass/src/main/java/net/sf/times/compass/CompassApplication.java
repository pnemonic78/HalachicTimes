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
package net.sf.times.compass;

import net.sf.times.compass.preference.CompassSettings;
import net.sf.times.location.CompassLocations;
import net.sf.times.location.LocationApplication;

/**
 * Compass application.
 *
 * @author Moshe Waisberg
 */
public class CompassApplication extends LocationApplication {

    /** Provider for locations. */
    private CompassLocations locations;

    /**
     * Constructs a new application.
     */
    public CompassApplication() {
    }

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public CompassLocations getLocations() {
        if (locations == null) {
            locations = new CompassLocations(this);
        }
        return locations;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CompassSettings.init(this);
    }

    @Override
    public void onTerminate() {
        if (locations != null) {
            locations.quit();
        }
        super.onTerminate();
    }
}

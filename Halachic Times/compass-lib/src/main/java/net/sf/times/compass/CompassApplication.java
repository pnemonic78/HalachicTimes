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
package net.sf.times.compass;

import android.content.Context;

import net.sf.preference.ThemePreferences;
import net.sf.times.location.AddressProvider;
import net.sf.times.location.CompassLocations;
import net.sf.times.location.LocationApplication;

/**
 * Compass application.
 *
 * @author Moshe Waisberg
 */
public class CompassApplication extends LocationApplication<ThemePreferences, AddressProvider, CompassLocations> {

    @Override
    protected CompassLocations createLocationsProvider(Context context) {
        return new CompassLocations(context);
    }
}

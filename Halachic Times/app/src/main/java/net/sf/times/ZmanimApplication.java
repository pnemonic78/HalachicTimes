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
package net.sf.times;

import android.content.Context;
import android.support.annotation.NonNull;

import net.sf.app.LocaleCallbacks;
import net.sf.app.LocaleHelper;
import net.sf.preference.LocalePreferences;
import net.sf.preference.ThemePreferences;
import net.sf.times.location.AddressProvider;
import net.sf.times.location.LocationApplication;
import net.sf.times.location.LocationsProviderFactory;
import net.sf.times.location.ZmanimLocations;

/**
 * Zmanim application.
 *
 * @author Moshe Waisberg
 */
public class ZmanimApplication extends LocationApplication<ThemePreferences, AddressProvider, ZmanimLocations> {

    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @NonNull
    @Override
    protected LocationsProviderFactory<AddressProvider, ZmanimLocations> createProviderFactory(Context context) {
        return new ZmanimProviderFactoryImpl(context);
    }
}

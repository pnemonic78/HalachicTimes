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
package com.github.times.compass;

import android.content.Context;

import com.github.preference.ThemePreferences;
import com.github.times.location.AddressProvider;
import com.github.times.location.CompassLocations;
import com.github.times.location.LocationApplication;
import com.github.times.location.LocationsProviderFactory;

import androidx.annotation.NonNull;

/**
 * Compass application.
 *
 * @author Moshe Waisberg
 */
public class CompassApplication extends LocationApplication<ThemePreferences, AddressProvider, CompassLocations> {

    @NonNull
    @Override
    protected LocationsProviderFactory<AddressProvider, CompassLocations> createProviderFactory(Context context) {
        return new CompassProviderFactoryImpl(context);
    }
}

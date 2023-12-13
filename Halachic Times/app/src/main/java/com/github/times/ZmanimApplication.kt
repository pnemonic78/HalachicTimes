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
package com.github.times

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.preference.LocalePreferences
import com.github.preference.ThemePreferences
import com.github.times.appwidget.ZmanimWallpaperHelper
import com.github.times.location.AddressProvider
import com.github.times.location.LocationApplication
import com.github.times.location.LocationsProviderFactory
import com.github.times.location.ZmanimLocations
import com.github.times.util.CrashlyticsTree
import timber.log.Timber

/**
 * Zmanim application.
 *
 * @author Moshe Waisberg
 */
class ZmanimApplication :
    LocationApplication<ThemePreferences, AddressProvider, ZmanimLocations>() {

    private lateinit var localeCallbacks: LocaleCallbacks<LocalePreferences>

    @TargetApi(Build.VERSION_CODES.O_MR1)
    private val wallpaperHelper: ZmanimWallpaperHelper? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        ZmanimWallpaperHelper(this)
    } else {
        null
    }

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
    }

    override fun onPreCreate() {
        super.onPreCreate()
        Timber.plant(CrashlyticsTree(BuildConfig.DEBUG))
        localeCallbacks.onPreCreate(this)
        LocaleHelper.registerReceiver(this)
    }

    override fun createProviderFactory(context: Context): LocationsProviderFactory<AddressProvider, ZmanimLocations> {
        return ZmanimProviderFactoryImpl(context)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperHelper?.onCreate()
        }
    }

    override fun onTerminate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperHelper?.onDestroy()
        }
        super.onTerminate()
    }
}
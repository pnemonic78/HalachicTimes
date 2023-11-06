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
package com.github.times.location

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.preference.ThemePreferences

/**
 * Location application.
 *
 * @author Moshe Waisberg
 */
abstract class LocationApplication<TP : ThemePreferences, AP : AddressProvider, LP : LocationsProvider> :
    Application(), ThemeCallbacks<TP> {

    private val themeCallbacks: ThemeCallbacks<TP> = SimpleThemeCallbacks(this)
    private var locationHolder: LocationHolder<AP, LP>? = null
    private val localeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_LOCALE_CHANGED == action) {
                val newConfig = context.resources.configuration
                onConfigurationChanged(newConfig)
            }
        }
    }

    override fun onCreate() {
        onPreCreate()
        super.onCreate()
    }

    override fun onPreCreate() {
        themeCallbacks.onPreCreate()
    }

    override val themePreferences: TP
        get() = themeCallbacks.themePreferences

    private fun getLocationHolder(): LocationHolder<AP, LP> {
        var holder = locationHolder
        if (holder == null) {
            val context: Context = applicationContext
            holder = LocationHolder(createProviderFactory(context))
            locationHolder = holder
            registerComponentCallbacks(holder)

            val intentFilter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(localeReceiver, intentFilter)
        }
        return holder
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    val addresses: AP
        get() = getLocationHolder().addresses

    protected abstract fun createProviderFactory(context: Context): LocationsProviderFactory<AP, LP>

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    val locations: LP
        get() = getLocationHolder().locations

    override fun onTerminate() {
        super.onTerminate()
        stopLocationHolder()
    }

    private fun stopLocationHolder() {
        val locationHolder = locationHolder
        if (locationHolder != null) {
            this.locationHolder = null
            locationHolder.onTerminate()
            unregisterComponentCallbacks(locationHolder)
        }
    }
}
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
package com.github.times.compass.preference

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import com.github.preference.AbstractPreferenceFragment
import com.github.times.compass.lib.R
import com.github.times.location.LocationApplication

/**
 * This fragment shows the preferences for the Privacy and Security header.
 */
@Keep
class PrivacyPreferenceFragment : AbstractPreferenceFragment() {

    override val preferencesXml: Int = R.xml.privacy_preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<Preference>("clear_history")?.apply {
            setOnPreferenceClickListener {
                it.isEnabled = false
                deleteHistory(it.context)
                it.isEnabled = true
                true
            }
        }
        findPreference<Preference>("clear_data")?.apply {
            isEnabled = true
            setOnPreferenceClickListener {
                it.isEnabled = false
                deleteAppData(it.context)
                it.isEnabled = true
                true
            }
        }
        validateIntent("location_permission")
    }

    /**
     * Clear the history of addresses.
     */
    private fun deleteHistory(context: Context) {
        val appContext = context.applicationContext
        val app = appContext as LocationApplication<*, *, *>
        val provider = app.addresses
        provider.deleteAddresses()
        provider.deleteElevations()
        provider.deleteCities()
    }

    /**
     * Clear the application data.
     */
    private fun deleteAppData(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.clearApplicationUserData()
    }
}
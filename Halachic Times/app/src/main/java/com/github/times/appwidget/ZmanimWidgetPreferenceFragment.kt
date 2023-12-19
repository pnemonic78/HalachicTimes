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
package com.github.times.appwidget

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.core.content.PermissionChecker
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.github.app.PERMISSION_WALLPAPER
import com.github.times.R
import com.github.times.preference.AbstractPreferenceFragment
import com.github.times.preference.ZmanimPreferences
import timber.log.Timber

/**
 * This fragment shows the preferences for the widgets.
 */
@Keep
class ZmanimWidgetPreferenceFragment : AbstractPreferenceFragment() {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
            Timber.i("Permission to read wallpaper: %s", isGranted)
        }
    private var widgetPreference: ListPreference? = null

    override val preferencesXml: Int = R.xml.widget_preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        widgetPreference = findPreference<ListPreference>(ZmanimPreferences.KEY_THEME_WIDGET)?.apply {
            onPreferenceClickListener = this@ZmanimWidgetPreferenceFragment
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
                    notifyAppWidgets(preference.context)
                    true
                }
        }
        findPreference<Preference>(ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE)?.onPreferenceClickListener =
            this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val key = preference.key
        val context = preference.context
        if (preference === widgetPreference) {
            if (checkWallpaperPermission(context)) {
                return true
            }
        } else if (ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE == key) {
            if (checkWallpaperPermission(context)) {
                return true
            }
        }
        return super.onPreferenceClick(preference)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkWallpaperPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Wallpaper colors don't need permissions.
            return true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(
                    context,
                    PERMISSION_WALLPAPER
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                requestPermission.launch(PERMISSION_WALLPAPER)
                return true
            }
        }
        return false
    }
}
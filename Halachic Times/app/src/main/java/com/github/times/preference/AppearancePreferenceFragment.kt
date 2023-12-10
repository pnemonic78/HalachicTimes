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
package com.github.times.preference

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.github.app.LocaleHelper
import com.github.app.PERMISSION_WALLPAPER
import com.github.app.restart
import com.github.preference.LocalePreferences
import com.github.preference.ThemePreferences
import com.github.times.BuildConfig
import com.github.times.R
import com.github.times.compass.preference.CompassPreferences
import com.github.util.LocaleUtils.sortByDisplay
import com.github.util.LocaleUtils.unique
import java.util.Locale
import timber.log.Timber

/**
 * This fragment shows the preferences for the Appearance header.
 */
@Keep
class AppearancePreferenceFragment : AbstractPreferenceFragment() {

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Timber.i("Permission to read wallpaper: %s", isGranted)
        }

    override val preferencesXml: Int = R.xml.appearance_preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        initList(ThemePreferences.KEY_THEME)
        initList(CompassPreferences.KEY_THEME_COMPASS)
        initList(ZmanimPreferences.KEY_THEME_WIDGET)?.apply {
            setOnPreferenceClickListener {
                val context = it.context
                checkWallpaperPermission(context)
            }
        }
        initList(ZmanimPreferences.KEY_EMPHASIS_SCALE)
        initLocaleList(LocalePreferences.KEY_LOCALE)
        findPreference<Preference>(ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE)?.apply {
            setOnPreferenceClickListener {
                val context = it.context
                checkWallpaperPermission(context)
            }
        }
    }

    private fun initLocaleList(key: String): ListPreference? {
        if (key.isEmpty()) return null
        val preference = findPreference<Preference>(key)
        if (preference is ListPreference) {
            val context = preference.context
            val localeNames = BuildConfig.LOCALES
            val unique = unique(localeNames)
            val sorted = sortByDisplay(unique)!!
            val length = sorted.size
            var length2 = length
            if (!sorted[0].language.isNullOrEmpty()) {
                length2 = length + 1
            }
            val values = arrayOfNulls<CharSequence>(length2)
            val entries = arrayOfNulls<CharSequence>(length2)
            values[0] = context.getString(R.string.locale_defaultValue)
            var locale: Locale
            var i = 0
            var j = length2 - length
            while (i < length) {
                locale = sorted[i]
                values[j] = locale.toString()
                entries[j] = locale.getDisplayName(locale)
                i++
                j++
            }
            if (entries[0].isNullOrEmpty()) {
                entries[0] = context.getString(com.github.lib.R.string.locale_default)
            }
            preference.apply {
                this.entryValues = values
                this.entries = entries
                this.setOnPreferenceChangeListener { _, newValue: Any? ->
                    val newLocale = newValue?.toString().orEmpty()
                    notifyConfigurationChanged(context, newLocale)
                    true
                }
            }
        }
        return initList(key)
    }

    private fun notifyConfigurationChanged(context: Context, newLocale: String) {
        LocaleHelper.sendLocaleChanged(context, newLocale)
        // Restart the activity to refresh views.
        requireActivity().restart()
    }

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
                val activity: Activity = requireActivity()
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        PERMISSION_WALLPAPER
                    )
                ) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.appwidget_theme_title)
                        .setMessage(R.string.appwidget_theme_permission_rationale)
                        .setCancelable(true)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                            requestPermission.launch(PERMISSION_WALLPAPER)
                        }
                        .show()
                } else {
                    requestPermission.launch(PERMISSION_WALLPAPER)
                }
                return true
            }
        }
        return false
    }
}
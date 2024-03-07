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

import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.Keep
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.github.preference.NumberPickerPreference
import com.github.times.R
import com.github.times.remind.ZmanimReminder.Companion.checkPermissions
import com.github.util.isLocaleRTL

/**
 * This fragment shows the preferences for the General header.
 */
@Keep
class GeneralPreferenceFragment : AbstractPreferenceFragment() {
    private var reminderRingtonePreference: RingtonePreference? = null

    override val preferencesXml: Int = R.xml.general_preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<Preference>(ZmanimPreferences.KEY_YEAR_FINAL)?.apply {
            isVisible = context.isLocaleRTL()
        }
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            findPreference<Preference>(ZmanimPreferences.KEY_REMINDER_SETTINGS)?.apply {
                isEnabled = true
                setOnPreferenceClickListener {
                    val context = it.context
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    startActivity(intent)
                    true
                }
            }
        }
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            findPreference<SwitchPreference>(ZmanimPreferences.KEY_NOTIFICATION_UPCOMING)?.apply {
                val preference: SwitchPreference = this
                setOnPreferenceClickListener {
                    if (preference.isChecked) {
                        val activity = activity ?: return@setOnPreferenceClickListener false
                        checkPermissions(activity)
                        return@setOnPreferenceClickListener true
                    }
                    false
                }
            }
        }
        val ringtonePreference =
            initRingtone<RingtonePreference>(ZmanimPreferences.KEY_REMINDER_RINGTONE)?.apply {
                reminderRingtonePreference = this
                setRequestPermissionsCode(this@GeneralPreferenceFragment, REQUEST_PERMISSIONS)
            }

        findPreference<ListPreference>(ZmanimPreferences.KEY_REMINDER_STREAM)?.apply {
            setOnPreferenceChangeListener { _, newValue: Any? ->
                val value = newValue?.toString().orEmpty()
                val audioStreamType =
                    if (value.isEmpty()) AudioManager.STREAM_ALARM else value.toInt()
                ringtonePreference?.ringtoneType =
                    if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                        RingtoneManager.TYPE_NOTIFICATION
                    } else {
                        RingtoneManager.TYPE_ALARM
                    }
                true
            }
        }
        findPreference<NumberPickerPreference>(ZmanimPreferences.KEY_REMINDER_SILENCE)?.apply {
            summaryProvider = ReminderSilenceSummaryProvider.instance
        }
        validateIntent("date_time_settings")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            reminderRingtonePreference?.onRequestPermissionsResult(permissions, grantResults)
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 0x702E // TONE
    }
}
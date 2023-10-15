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
package com.github.times.preference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.github.times.R;
import com.github.times.remind.ZmanimReminder;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;
import static android.text.TextUtils.isEmpty;
import static com.github.times.compass.preference.CompassPreferences.KEY_COMPASS_BEARING;
import static com.github.times.location.LocationPreferences.KEY_COORDS_FORMAT;
import static com.github.times.preference.ZmanimPreferences.KEY_NOTIFICATION_UPCOMING;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_RINGTONE;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_SETTINGS;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_STREAM;
import static com.github.times.preference.ZmanimPreferences.KEY_YEAR_FINAL;
import static com.github.util.LocaleUtils.isLocaleRTL;

/**
 * This fragment shows the preferences for the General header.
 */
@Keep
public class GeneralPreferenceFragment extends AbstractPreferenceFragment {

    private static final int REQUEST_PERMISSIONS = 0x702E; // TONE

    private RingtonePreference reminderRingtonePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.general_preferences;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        Preference yearFinal = findPreference(KEY_YEAR_FINAL);
        if (yearFinal != null) {
            final Context context = yearFinal.getContext();
            yearFinal.setVisible(isLocaleRTL(context));
        }

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            Preference preference = findPreference(KEY_REMINDER_SETTINGS);
            if (preference != null) {
                preference.setEnabled(true);
                preference.setOnPreferenceClickListener(pref -> {
                    final Context context = pref.getContext();
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    startActivity(intent);
                    return true;
                });
            }
        }
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            SwitchPreference preference = findPreference(KEY_NOTIFICATION_UPCOMING);
            if (preference != null) {
                preference.setOnPreferenceClickListener(pref -> {
                    if (preference.isChecked()) {
                        final Activity activity = getActivity();
                        if (activity == null) return false;
                        ZmanimReminder.checkNotificationPermissions(activity);
                        return true;
                    }
                    return false;
                });
            }
        }
        reminderRingtonePreference = (RingtonePreference) initRingtone(KEY_REMINDER_RINGTONE);
        reminderRingtonePreference.setRequestPermissionsCode(this, REQUEST_PERMISSIONS);
        ListPreference reminderPreference = initList(KEY_REMINDER_STREAM);
        if (reminderPreference != null) {
            reminderPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                RingtonePreference ringtonePreference = reminderRingtonePreference;
                if (ringtonePreference == null) return false;
                String value = newValue.toString();
                int audioStreamType = isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
                int ringType;
                if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                    ringType = RingtoneManager.TYPE_NOTIFICATION;
                } else {
                    ringType = RingtoneManager.TYPE_ALARM;
                }
                ringtonePreference.setRingtoneType(ringType);
                return true;
            });
        }

        initList(KEY_COORDS_FORMAT);
        initList(KEY_COMPASS_BEARING);
        validateIntent("date_time_settings");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            reminderRingtonePreference.onRequestPermissionsResult(permissions, grantResults);
        }
    }
}

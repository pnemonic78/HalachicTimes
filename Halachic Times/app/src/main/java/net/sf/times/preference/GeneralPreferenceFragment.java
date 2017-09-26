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
package net.sf.times.preference;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.text.TextUtils;

import net.sf.preference.RingtonePreference;
import net.sf.times.R;

import static net.sf.times.compass.preference.CompassPreferences.KEY_COMPASS_BEARING;
import static net.sf.times.preference.ZmanimPreferences.KEY_PAST;
import static net.sf.times.preference.ZmanimPreferences.KEY_REMINDER_RINGTONE;
import static net.sf.times.preference.ZmanimPreferences.KEY_REMINDER_STREAM;

/**
 * This fragment shows the preferences for the General header.
 */
public class GeneralPreferenceFragment extends AbstractPreferenceFragment {

    private RingtonePreference reminderRingtonePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.general_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference(KEY_PAST).setOnPreferenceChangeListener(this);
        reminderRingtonePreference = initRingtone(KEY_REMINDER_RINGTONE);

        initList(KEY_REMINDER_STREAM);
        initList(KEY_COMPASS_BEARING);
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
            String value = newValue.toString();
            int audioStreamType = TextUtils.isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
            int ringType;
            if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                ringType = RingtoneManager.TYPE_NOTIFICATION;
            } else {
                ringType = RingtoneManager.TYPE_ALARM;
            }
            reminderRingtonePreference.setRingtoneType(ringType);
        }
    }
}

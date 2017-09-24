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

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import net.sf.preference.RingtonePreference;
import net.sf.times.R;

import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static net.sf.preference.LocalePreferences.KEY_LOCALE;
import static net.sf.times.compass.preference.CompassPreferences.KEY_COMPASS_BEARING;
import static net.sf.times.preference.ZmanimPreferences.KEY_REMINDER_RINGTONE;
import static net.sf.times.preference.ZmanimPreferences.KEY_REMINDER_STREAM;
import static net.sf.util.LocaleUtils.sortByDisplay;

/**
 * This fragment shows the preferences for the General header.
 */
public class GeneralPreferenceFragment extends net.sf.preference.AbstractPreferenceFragment {

    private RingtonePreference reminderRingtonePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.general_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reminderRingtonePreference = initRingtone(KEY_REMINDER_RINGTONE);

        initList(KEY_REMINDER_STREAM);
        initList(KEY_COMPASS_BEARING);
        initList(KEY_COMPASS_BEARING);
        initLocaleList(KEY_LOCALE);
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
            String value = newValue.toString();
            int audioStreamType = isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
            int ringType;
            if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                ringType = RingtoneManager.TYPE_NOTIFICATION;
            } else {
                ringType = RingtoneManager.TYPE_ALARM;
            }
            reminderRingtonePreference.setRingtoneType(ringType);
        }
    }

    private ListPreference initLocaleList(String key) {
        if (isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if ((pref != null) && (pref instanceof ListPreference)) {
            ListPreference list = (ListPreference) pref;

            final Context context = getActivity();
            final AssetManager assets = context.getAssets();
            String[] assetsLocales = assets.getLocales();

            Locale[] sorted = sortByDisplay(assetsLocales);
            final int length = assetsLocales.length;
            int length2 = length;
            if (!isEmpty(assetsLocales[0])) {
                length2 = length + 1;
            }

            final CharSequence[] values = new CharSequence[length2];
            final CharSequence[] entries = new CharSequence[length2];
            values[0] = context.getString(R.string.locale_defaultValue);

            Locale locale;
            for (int i = 0, j = length2 - length; i < length; i++, j++) {
                locale = sorted[i];
                values[j] = locale.toString();
                entries[j] = locale.getDisplayName(locale);
            }
            if (isEmpty(entries[0])) {
                entries[0] = context.getString(R.string.locale_system);
            }

            list.setEntryValues(values);
            list.setEntries(entries);
        }

        return initList(key);
    }
}

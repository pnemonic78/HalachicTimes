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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.github.times.BuildConfig;
import com.github.times.R;
import com.github.util.LocaleUtils;

import java.util.Locale;

import static android.content.Intent.ACTION_LOCALE_CHANGED;
import static android.text.TextUtils.isEmpty;
import static com.github.app.ActivityUtils.restartActivity;
import static com.github.preference.LocalePreferences.KEY_LOCALE;
import static com.github.times.compass.preference.CompassPreferences.KEY_THEME_COMPASS;
import static com.github.times.location.LocationPreferences.EXTRA_LOCALE;
import static com.github.times.preference.ZmanimPreferences.KEY_EMPHASIS_SCALE;
import static com.github.times.preference.ZmanimPreferences.KEY_THEME;
import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET;
import static com.github.util.LocaleUtils.sortByDisplay;

/**
 * This fragment shows the preferences for the Appearance header.
 */
public class AppearancePreferenceFragment extends AbstractPreferenceFragment {

    private ListPreference localePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.appearance_preferences;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        initList(KEY_THEME);
        initList(KEY_THEME_COMPASS);
        initList(KEY_THEME_WIDGET);
        initList(KEY_EMPHASIS_SCALE);
        localePreference = initLocaleList(KEY_LOCALE);
    }

    @Override
    protected boolean onListPreferenceChange(ListPreference preference, Object newValue) {
        boolean result = super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (KEY_LOCALE.equals(key) && (localePreference != null)) {
            notifyConfigurationChanged(localePreference.getValue());
        } else if (KEY_THEME_WIDGET.equals(key)) {
            notifyAppWidgets();
        }
        return result;
    }

    private ListPreference initLocaleList(String key) {
        if (isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if ((pref != null) && (pref instanceof ListPreference)) {
            final Context context = getActivity();
            final String[] localeNames = BuildConfig.LOCALES;
            final Locale[] unique = LocaleUtils.unique(localeNames);

            final Locale[] sorted = sortByDisplay(unique);
            final int length = sorted.length;
            int length2 = length;
            if (!isEmpty(sorted[0].getLanguage())) {
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
                entries[0] = context.getString(R.string.locale_default);
            }

            ListPreference list = (ListPreference) pref;
            list.setEntryValues(values);
            list.setEntries(entries);
        }

        return initList(key);
    }

    private void notifyConfigurationChanged(String newLocale) {
        final Context context = getActivity();

        Locale locale = LocaleUtils.parseLocale(newLocale);
        LocaleUtils.applyLocale(context.getApplicationContext(), locale);

        Intent notification = new Intent(ACTION_LOCALE_CHANGED);
        notification.setPackage(context.getPackageName());
        notification.putExtra(EXTRA_LOCALE, newLocale);
        LocalBroadcastManager.getInstance(context).sendBroadcast(notification);

        // Restart the activity to refresh views.
        restartActivity(getActivity());
    }
}

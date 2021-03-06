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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
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
import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE;
import static com.github.util.LocaleUtils.sortByDisplay;

/**
 * This fragment shows the preferences for the Appearance header.
 */
@Keep
public class AppearancePreferenceFragment extends AbstractPreferenceFragment {

    private static final String PERMISSION_WALLPAPER = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int REQUEST_WALLPAPER = 0x3A11;

    private ListPreference widgetPreference;
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
        widgetPreference = initList(KEY_THEME_WIDGET);
        widgetPreference.setOnPreferenceClickListener(this);
        initList(KEY_EMPHASIS_SCALE);
        localePreference = initLocaleList(KEY_LOCALE);
        findPreference(KEY_THEME_WIDGET_RATIONALE).setOnPreferenceClickListener(this);
    }

    @Nullable
    @Override
    protected ListPreference initList(String key) {
        final ListPreference listPreference = super.initList(key);
        if (listPreference != null) {
            if (KEY_THEME_WIDGET.equals(key)) {

            }
        }
        return listPreference;
    }

    @Nullable
    private ListPreference initLocaleList(String key) {
        if (isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            final Context context = getContext();
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

            final ListPreference list = (ListPreference) pref;
            list.setEntryValues(values);
            list.setEntries(entries);

            list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notifyConfigurationChanged(list.getValue());
                    return true;
                }
            });
        }

        return initList(key);
    }

    private void notifyConfigurationChanged(String newLocale) {
        final Context context = getContext();

        Locale locale = LocaleUtils.parseLocale(newLocale);
        LocaleUtils.applyLocale(context.getApplicationContext(), locale);

        Intent notification = new Intent(ACTION_LOCALE_CHANGED);
        notification.setPackage(context.getPackageName());
        notification.putExtra(EXTRA_LOCALE, newLocale);
        LocalBroadcastManager.getInstance(context).sendBroadcast(notification);

        // Restart the activity to refresh views.
        restartActivity(getActivity());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        final Context context = preference.getContext();
        if (preference == widgetPreference) {
            if (checkWallpaperPermission(context)) {
                return true;
            }
        } else if (KEY_THEME_WIDGET_RATIONALE.equals(key)) {
            if (checkWallpaperPermission(context)) {
                return true;
            }
        }
        return super.onPreferenceClick(preference);
    }

    private boolean checkWallpaperPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_WALLPAPER) != PermissionChecker.PERMISSION_GRANTED) {
                final Activity activity = getActivity();
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION_WALLPAPER)) {
                    new AlertDialog.Builder(context)
                        .setTitle(R.string.appwidget_theme_title)
                        .setMessage(R.string.appwidget_theme_permission_rationale)
                        .setCancelable(true)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{PERMISSION_WALLPAPER}, REQUEST_WALLPAPER);
                            }
                        })
                        .show();
                } else {
                    requestPermissions(new String[]{PERMISSION_WALLPAPER}, REQUEST_WALLPAPER);
                }
                return true;
            }
        }
        return false;
    }
}

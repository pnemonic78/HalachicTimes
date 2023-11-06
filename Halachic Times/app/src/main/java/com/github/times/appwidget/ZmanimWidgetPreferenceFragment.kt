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
package com.github.times.appwidget;

import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET;
import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.core.content.PermissionChecker;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.github.times.R;
import com.github.times.preference.AbstractPreferenceFragment;

import timber.log.Timber;

/**
 * This fragment shows the preferences for the widgets.
 */
@Keep
public class ZmanimWidgetPreferenceFragment extends AbstractPreferenceFragment {

    private static final String PERMISSION_WALLPAPER = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        Timber.i("Permission to read wallpaper: %s", isGranted);
    });

    private ListPreference widgetPreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.widget_preferences;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        ListPreference widgetThemePreference = initList(KEY_THEME_WIDGET);
        if (widgetThemePreference != null) {
            widgetThemePreference.setOnPreferenceClickListener(this);
            widgetThemePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                notifyAppWidgets();
                return true;
            });
        }
        this.widgetPreference = widgetThemePreference;

        findPreference(KEY_THEME_WIDGET_RATIONALE).setOnPreferenceClickListener(this);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Wallpaper colors don't need permissions.
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_WALLPAPER) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermission.launch(PERMISSION_WALLPAPER);
                return true;
            }
        }
        return false;
    }
}

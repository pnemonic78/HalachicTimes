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

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.core.content.PermissionChecker;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.github.times.R;
import com.github.times.preference.AbstractPreferenceFragment;

import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET;
import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET_RATIONALE;

/**
 * This fragment shows the preferences for the widgets.
 */
@Keep
public class ZmanimWidgetPreferenceFragment extends AbstractPreferenceFragment {

    private static final String PERMISSION_WALLPAPER = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int REQUEST_WALLPAPER = 0x3A11;

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
            widgetThemePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notifyAppWidgets();
                    return true;
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_WALLPAPER) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(new String[]{PERMISSION_WALLPAPER}, REQUEST_WALLPAPER);
                return true;
            }
        }
        return false;
    }
}

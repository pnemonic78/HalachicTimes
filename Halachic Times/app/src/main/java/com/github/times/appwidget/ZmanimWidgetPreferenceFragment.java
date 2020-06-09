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

import androidx.core.content.PermissionChecker;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.github.times.R;
import com.github.times.preference.AbstractPreferenceFragment;

import static com.github.times.preference.ZmanimPreferences.KEY_THEME_WIDGET;

/**
 * This fragment shows the preferences for the widgets.
 */
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

        widgetPreference = initList(KEY_THEME_WIDGET);
        widgetPreference.setOnPreferenceClickListener(this);
    }

    @Override
    protected boolean onListPreferenceChange(ListPreference preference, Object newValue) {
        boolean result = super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (KEY_THEME_WIDGET.equals(key)) {
            notifyAppWidgets();
        }
        return result;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == widgetPreference) {
            final Context context = preference.getContext();
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

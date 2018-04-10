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
package com.github.preference;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.github.lib.R;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String packageName;
    private boolean restartParentActivityForUi;

    /**
     * Constructs a new preferences.
     */
    public PreferenceActivity() {
        packageName = getClass().getPackage().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Base_Settings);
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        if (VERSION.SDK_INT < JELLY_BEAN) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.startsWith(packageName);
    }

    @Override
    public void finish() {
        // Recreate the parent activity in case a theme has changed.
        if (VERSION.SDK_INT >= JELLY_BEAN) {
            Intent parentIntent = getParentActivityIntent();
            if (parentIntent == null) {
                try {
                    PackageManager pm = getPackageManager();
                    ActivityInfo info = pm.getActivityInfo(getComponentName(), 0);
                    String parentActivity = info.parentActivityName;
                    parentIntent = new Intent();
                    parentIntent.setClassName(this, parentActivity);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            if ((parentIntent != null) && shouldUpRecreateTask(parentIntent)) {
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(parentIntent);
            }
        }
        super.finish();
    }

    @Override
    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return restartParentActivityForUi || super.shouldUpRecreateTask(targetIntent);
    }

    protected void markRestartParentActivityForUi() {
        this.restartParentActivityForUi = true;
    }

    protected boolean shouldRestartParentActivityForUi(String key) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (shouldRestartParentActivityForUi(key)) {
            markRestartParentActivityForUi();
        }
    }
}

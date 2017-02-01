/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.preference;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.sf.lib.R;

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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

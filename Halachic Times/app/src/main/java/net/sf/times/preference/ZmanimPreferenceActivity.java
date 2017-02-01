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
package net.sf.times.preference;

import android.os.Bundle;

import net.sf.preference.PreferenceActivity;
import net.sf.preference.ThemedPreferences;
import net.sf.times.R;
import net.sf.times.compass.preference.CompassPreferences;

import java.util.List;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferenceActivity extends PreferenceActivity {

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferenceActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Zmanim_Settings);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean shouldRestartParentActivityForUi(String key) {
        return ThemedPreferences.KEY_THEME.equals(key) || CompassPreferences.KEY_THEME_COMPASS.equals(key);
    }
}

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
package net.sf.times.compass.preference;

import android.os.Bundle;

import net.sf.preference.AbstractPreferenceFragment;
import net.sf.times.compass.R;

/**
 * This fragment shows the preferences for the Appearance header.
 */
public class AppearancePreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.appearance_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList(CompassPreferences.KEY_COORDS_FORMAT);
        initList(CompassPreferences.KEY_THEME);
        initList(CompassPreferences.KEY_THEME_COMPASS);
    }
}

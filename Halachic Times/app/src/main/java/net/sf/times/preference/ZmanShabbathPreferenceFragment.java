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
import android.preference.ListPreference;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;

/**
 * This fragment shows the preferences for the Candles zman screen.
 */
public class ZmanShabbathPreferenceFragment extends ZmanPreferenceFragment {

    private ListPreference after;
    private SeekBarDialogPreference seek;
    private String shabbathAfterName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        after = (ListPreference) findPreference(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER);
        int shabbathAfter = getSettings().toId(after.getValue());
        shabbathAfterName = getString(shabbathAfter);

        seek = (SeekBarDialogPreference) findPreference(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES);
        seek.setSummaryFormat(R.plurals.shabbath_ends_summary, shabbathAfterName);
        seek.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        super.onListPreferenceChange(preference, newValue);
        if ((preference == after) && (newValue != null)) {
            // Update "seek" summary.
            int shabbathAfter = getSettings().toId(newValue.toString());
            shabbathAfterName = getString(shabbathAfter);
            seek.setSummaryFormat(R.plurals.shabbath_ends_summary, shabbathAfterName);
        }
    }
}

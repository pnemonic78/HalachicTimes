/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/MPL-1.1.html
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
package net.sf.times;

import net.sf.times.preference.SeekBarPreference;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * Application preferences.
 * 
 * @author Moshe
 */
public class ZmanimPreferences extends PreferenceActivity implements OnPreferenceChangeListener {

	private SeekBarPreference mCandles;

	/**
	 * Constructs a new preferences.
	 */
	public ZmanimPreferences() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		mCandles = (SeekBarPreference) findPreference(ZmanimSettings.KEY_CANDLES);
		mCandles.setOnPreferenceChangeListener(this);
		onPreferenceChange(mCandles, null);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mCandles) {
			// On ICS, setSummary always calls onCreateView, so postpone until
			// after preference is persisted.
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String format = getString(R.string.candles_summary);
					CharSequence summary = String.format(format, mCandles.getProgress());
					mCandles.setSummary(summary);
				}
			});
			return true;
		}
		return false;
	}
}

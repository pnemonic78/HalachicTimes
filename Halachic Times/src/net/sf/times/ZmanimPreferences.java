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
package net.sf.times;

import net.sf.times.preference.SeekBarPreference;
import android.os.Bundle;
import android.preference.ListPreference;
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

		mCandles = (SeekBarPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
		mCandles.setOnPreferenceChangeListener(this);
		onPreferenceChange(mCandles, null);

		ListPreference list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_DAWN);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_TALLIS);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_SUNRISE);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_SHEMA);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_NOON);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_MINCHA);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_SUNSET);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());

		list = (ListPreference) findPreference(ZmanimSettings.KEY_OPINION_MIDNIGHT);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mCandles) {
			// Since ECLAIR, setSummary always calls onCreateView, so
			// postpone until after preference is persisted.
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String format = getString(R.string.candles_summary);
					CharSequence summary = String.format(format, mCandles.getProgress());
					mCandles.setSummary(summary);
				}
			});
			return true;
		} else if (preference instanceof ListPreference) {
			updateSummary((ListPreference) preference, newValue.toString());
		}
		return false;
	}

	/**
	 * Find the summary that was selected from the list.
	 * 
	 * @param preference
	 *            the preference.
	 * @param newValue
	 *            the new value.
	 */
	private void updateSummary(ListPreference preference, String newValue) {
		preference.setValue(newValue);

		CharSequence[] values = preference.getEntryValues();
		CharSequence[] entries = preference.getEntries();
		int length = values.length;

		for (int i = 0; i < length; i++) {
			if (newValue.equals(values[i])) {
				preference.setSummary(entries[i]);
				return;
			}
		}
		preference.setSummary(null);
	}
}

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

import java.util.Locale;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.location.AddressProvider;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Application preferences that populate the settings.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimPreferences extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private static final String TAG = "ZmanimPreferences";

	private SeekBarDialogPreference mCandles;
	private ZmanimSettings mSettings;
	private ZmanimReminder mReminder;
	private Preference mClearHistory;
	private Preference mAboutKosherJava;

	/**
	 * Constructs a new preferences.
	 */
	public ZmanimPreferences() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		addPreferencesFromResource(R.xml.preferences);

		mCandles = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
		mCandles.setOnPreferenceChangeListener(this);
		onPreferenceChange(mCandles, null);

		initList(ZmanimSettings.KEY_OPINION_DAWN);
		initList(ZmanimSettings.KEY_OPINION_TALLIS);
		initList(ZmanimSettings.KEY_OPINION_SUNRISE);
		initList(ZmanimSettings.KEY_OPINION_SHEMA);
		initList(ZmanimSettings.KEY_OPINION_TFILA);
		initList(ZmanimSettings.KEY_OPINION_NOON);
		initList(ZmanimSettings.KEY_OPINION_EARLIEST_MINCHA);
		initList(ZmanimSettings.KEY_OPINION_MINCHA);
		initList(ZmanimSettings.KEY_OPINION_PLUG_MINCHA);
		initList(ZmanimSettings.KEY_OPINION_SUNSET);
		initList(ZmanimSettings.KEY_OPINION_TWILIGHT);
		initList(ZmanimSettings.KEY_OPINION_NIGHTFALL);
		initList(ZmanimSettings.KEY_OPINION_MIDNIGHT);
		initList(ZmanimSettings.KEY_OPINION_EARLIEST_LEVANA);
		initList(ZmanimSettings.KEY_OPINION_LATEST_LEVANA);

		initList(ZmanimSettings.KEY_REMINDER_DAWN);
		initList(ZmanimSettings.KEY_REMINDER_TALLIS);
		initList(ZmanimSettings.KEY_REMINDER_SUNRISE);
		initList(ZmanimSettings.KEY_REMINDER_SHEMA);
		initList(ZmanimSettings.KEY_REMINDER_TFILA);
		initList(ZmanimSettings.KEY_REMINDER_NOON);
		initList(ZmanimSettings.KEY_REMINDER_EARLIEST_MINCHA);
		initList(ZmanimSettings.KEY_REMINDER_MINCHA);
		initList(ZmanimSettings.KEY_REMINDER_PLUG_MINCHA);
		initList(ZmanimSettings.KEY_REMINDER_CANDLES);
		initList(ZmanimSettings.KEY_REMINDER_SUNSET);
		initList(ZmanimSettings.KEY_REMINDER_TWILIGHT);
		initList(ZmanimSettings.KEY_REMINDER_NIGHTFALL);
		initList(ZmanimSettings.KEY_REMINDER_MIDNIGHT);
		initList(ZmanimSettings.KEY_REMINDER_EARLIEST_LEVANA);
		initList(ZmanimSettings.KEY_REMINDER_LATEST_LEVANA);

		mClearHistory = findPreference("clear_history");
		mClearHistory.setOnPreferenceClickListener(this);

		Preference version = findPreference("about.version");
		try {
			version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// Never should happen with our own package!
		}
		mAboutKosherJava = findPreference("about.kosherjava");
		mAboutKosherJava.setOnPreferenceClickListener(this);
	}

	@SuppressWarnings("deprecation")
	private void initList(String name) {
		ListPreference list = (ListPreference) findPreference(name);
		list.setOnPreferenceChangeListener(this);
		onPreferenceChange(list, list.getValue());
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mCandles) {
			String format = getString(R.string.candles_summary);
			CharSequence summary = String.format(Locale.getDefault(), format, mCandles.getProgress());
			mCandles.setSummary(summary);
			return true;
		}
		if (preference instanceof ListPreference) {
			ListPreference list = (ListPreference) preference;
			String oldValue = list.getValue();
			String value = newValue.toString();
			updateSummary(list, value);

			if (!oldValue.equals(newValue)) {
				if (preference.getKey().endsWith(ZmanimSettings.REMINDER_SUFFIX)) {
					if (mSettings == null)
						mSettings = new ZmanimSettings(this);
					if (mReminder == null)
						mReminder = new ZmanimReminder(this);
					mReminder.remind(mSettings);
				}
			}
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mClearHistory) {
			deleteHistory();
			return true;
		}
		if (preference == mAboutKosherJava) {
			gotoKosherJava();
			return true;
		}
		return false;
	}

	/**
	 * Navigate to the KosherJava page.
	 */
	private void gotoKosherJava() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.kosherjava_url)));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Cannot view KosherJava", e);
		}
	}

	/**
	 * Clear the history of addresses.
	 */
	private void deleteHistory() {
		ZmanimApplication app = (ZmanimApplication) getApplication();
		AddressProvider provider = app.getAddresses();
		provider.deleteAddresses();
	}
}

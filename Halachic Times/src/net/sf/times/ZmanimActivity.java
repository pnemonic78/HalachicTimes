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

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.AddressProvider;
import net.sf.times.location.FindAddress;
import net.sf.times.location.FindAddress.OnFindAddressListener;
import net.sf.times.location.ZmanimAddress;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimActivity extends Activity implements LocationListener, OnDateSetListener, OnFindAddressListener, OnClickListener {

	/** The date parameter. */
	public static final String PARAMETER_DATE = "date";
	/** The time parameter. */
	public static final String PARAMETER_TIME = "time";
	/** The details list parameter. */
	public static final String PARAMETER_DETAILS = "details";
	/** The location parameter. */
	public static final String PARAMETER_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

	/** ISO 639 language code for "Hebrew". */
	public static final String ISO639_HEBREW = "he";
	/** ISO 639 language code for "Hebrew" - Java compatibility. */
	public static final String ISO639_HEBREW_FORMER = "iw";
	/** ISO 639 language code for "Yiddish" - Java compatibility. */
	public static final String ISO639_YIDDISH_FORMER = "ji";
	/** ISO 639 language code for "Yiddish". */
	public static final String ISO639_YIDDISH = "yi";

	/** Activity id for searching locations. */
	private static final int ACTIVITY_LOCATIONS = 1;

	/** The date. */
	private final Calendar mDate = Calendar.getInstance();
	/** The location header. */
	private View mHeader;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	/** Address provider. */
	private AddressProvider mAddressProvider;
	/** The settings and preferences. */
	protected ZmanimSettings mSettings;
	/** The date picker. */
	private DatePickerDialog mDatePicker;
	/** The address. */
	private ZmanimAddress mAddress;
	/** Populate the header in UI thread. */
	private Runnable mPopulateHeader;
	private ZmanimReminder mReminder;
	protected LayoutInflater mInflater;
	/** The master fragment. */
	private ZmanimFragment mMasterFragment;
	/** The details fragment. */
	private ZmanimDetailsFragment mDetailsFragment;
	/** Is master fragment next to details fragment? */
	private boolean mSideBySide;
	/** The master item selected id. */
	private int mSelectedId;

	/**
	 * Creates a new activity.
	 */
	public ZmanimActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		initLocation();

		Intent intent = getIntent();
		long date = intent.getLongExtra(PARAMETER_DATE, 0L);
		if (date == 0L) {
			date = intent.getLongExtra(PARAMETER_TIME, 0L);
			if (date == 0L)
				date = System.currentTimeMillis();
		}
		setDate(date);

		Location location = intent.getParcelableExtra(PARAMETER_LOCATION);
		if (location != null)
			mLocations.setLocation(location);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(PARAMETER_DATE, mDate.getTimeInMillis());
		outState.putInt(PARAMETER_DETAILS, mSelectedId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setDate(savedInstanceState.getLong(PARAMETER_DATE));
		final int itemId = savedInstanceState.getInt(PARAMETER_DETAILS, 0);

		if (itemId != 0) {
			// We need to wait for the list rows to get their default
			// backgrounds before we can highlight any row.
			new Thread() {
				public void run() {
					try {
						sleep(1000L);
					} catch (InterruptedException e) {
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mMasterFragment.populateTimes(mDate);
							toggleDetails(itemId);
						}
					});
				}
			}.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocations.resume(this);
		if (mReminder == null)
			mReminder = createReminder();
		if (mReminder != null)
			mReminder.cancel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocations.cancel(this);
		if (mAddressProvider != null) {
			mAddressProvider.close();
			mAddressProvider = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mReminder != null)
			mReminder.remind(mSettings, mLocations);
		SQLiteDatabase.releaseMemory();
	}

	/** Initialise. */
	private void init() {
		mSettings = new ZmanimSettings(this);

		mInflater = LayoutInflater.from(this);
		ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.times, null);
		mHeader = view.findViewById(R.id.header);

		setContentView(view);

		mMasterFragment = (ZmanimFragment) view.findViewById(R.id.list_fragment);
		mMasterFragment.setOnClickListener(this);
		mDetailsFragment = (ZmanimDetailsFragment) view.findViewById(R.id.details_fragment);
		mSideBySide = view.findViewById(R.id.frame_fragments) == null;
		hide(mDetailsFragment);
	}

	/** Initialise the location providers. */
	private void initLocation() {
		mLocations = ZmanimLocations.getInstance(this, this);
	}

	/**
	 * Set the date for the list.
	 * 
	 * @param date
	 *            the date.
	 */
	private void setDate(long date) {
		mDate.setTimeInMillis(date);
		TimeZone tz = mLocations.getTimeZone();
		mDate.setTimeZone(tz);

		View header = mHeader;
		// Have we been destroyed?
		if (header == null)
			return;
		CharSequence dateGregorian = DateUtils.formatDateTime(this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_WEEKDAY);
		TextView textGregorian = (TextView) header.findViewById(R.id.date_gregorian);
		textGregorian.setText(dateGregorian);

		JewishDate jewishDate = new JewishDate(mDate);
		TextView textHebrew = (TextView) header.findViewById(R.id.date_hebrew);
		HebrewDateFormatter formatter = new HebrewDateFormatter();
		formatter.setHebrewFormat(isLocaleRTL());
		CharSequence dateHebrew = formatter.format(jewishDate);
		textHebrew.setText(dateHebrew);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mAddressProvider == null)
			mAddressProvider = new AddressProvider(this);
		FindAddress.find(mAddressProvider, location, this);
		populateHeader();

		mMasterFragment.populateTimes(mDate);
		mDetailsFragment.populateTimes(mDate);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * Is the list background painted?
	 * 
	 * @return {@code true} for non-transparent background.
	 */
	protected boolean isBackgroundDrawable() {
		return true;
	}

	protected ZmanimAdapter createAdapter(Calendar date, ZmanimLocations locations) {
		GeoLocation gloc = locations.getGeoLocation();
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCalendar(date);
		boolean inIsrael = locations.inIsrael();
		return new ZmanimAdapter(this, mSettings, cal, inIsrael);
	}

	protected ZmanimReminder createReminder() {
		return new ZmanimReminder(this);
	}

	/** Populate the header item. */
	private void populateHeader() {
		View header = mHeader;
		// Have we been destroyed?
		if (header == null)
			return;
		Location loc = mLocations.getLocation();
		// Have we been destroyed?
		if (loc == null)
			return;

		final String coordsText = mLocations.formatCoordinates(loc);
		final String locationName = formatAddress();

		// Update the location.
		TextView coordinates = (TextView) header.findViewById(R.id.coordinates);
		coordinates.setText(coordsText);
		coordinates.setVisibility(mSettings.isCoordinates() ? View.VISIBLE : View.GONE);
		TextView address = (TextView) header.findViewById(R.id.address);
		address.setText(locationName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.zmanim, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_compass:
			startActivity(new Intent(this, CompassActivity.class));
			return true;
		case R.id.menu_date:
			mDatePicker = new DatePickerDialog(this, this, mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH));
			mDatePicker.show();
			return true;
		case R.id.menu_location:
			Location loc = mLocations.getLocation();
			// Have we been destroyed?
			if (loc == null)
				break;
			Bundle appData = new Bundle();
			appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, loc);

			Intent intent = new Intent(this, LocationActivity.class);
			intent.putExtra(SearchManager.APP_DATA, appData);
			startActivityForResult(intent, ACTIVITY_LOCATIONS);
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, ZmanimPreferences.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, year);
		date.set(Calendar.MONTH, monthOfYear);
		date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		setDate(date.getTimeInMillis());
		populateHeader();
		mMasterFragment.populateTimes(mDate);
		mDetailsFragment.populateTimes(mDate);
	}

	@Override
	public void onAddressFound(Location location, ZmanimAddress address) {
		mAddress = address;
		AddressProvider provider = mAddressProvider;
		// Have we been destroyed?
		if (provider == null)
			return;
		provider.insertAddress(location, address);
		Runnable runner = mPopulateHeader;
		if (runner == null) {
			runner = new Runnable() {
				@Override
				public void run() {
					populateHeader();
				}
			};
			mPopulateHeader = runner;
		}
		runOnUiThread(runner);
	}

	/**
	 * Format the address for the current location or time zone.
	 * 
	 * @return the formatted address.
	 */
	private String formatAddress() {
		if (mAddress != null)
			return mAddress.getFormatted();
		return getString(R.string.location_unknown);
	}

	/**
	 * Is the default locale right-to-left?
	 * 
	 * @return true if the locale is either Hebrew or Yiddish.
	 */
	private boolean isLocaleRTL() {
		final String iso639 = Locale.getDefault().getLanguage();
		return ISO639_HEBREW.equals(iso639) || ISO639_YIDDISH.equals(iso639) || ISO639_HEBREW_FORMER.equals(iso639) || ISO639_YIDDISH_FORMER.equals(iso639);
	}

	/**
	 * Show/hide the details list.
	 * 
	 * @param item
	 *            the master item.
	 * @param view
	 *            the master row view that was clicked.
	 */
	protected void toggleDetails(ZmanimItem item, View view) {
		if (item == null)
			item = (ZmanimItem) view.getTag();
		toggleDetails(item.titleId);
	}

	/**
	 * Show/hide the details list.
	 * 
	 * @param itemId
	 *            the master item id.
	 */
	protected void toggleDetails(int itemId) {
		if (itemId == 0)
			return;

		if (!mSideBySide) {
			mDetailsFragment.populateTimes(mDate, itemId);
			hide(mMasterFragment);
			show(mDetailsFragment);
			mSelectedId = itemId;
		} else if ((mSelectedId == itemId) && mDetailsFragment.isVisible()) {
			hide(mDetailsFragment);
			mMasterFragment.unhighlight();
		} else {
			mDetailsFragment.populateTimes(mDate, itemId);
			mMasterFragment.unhighlight();
			mMasterFragment.highlight(itemId);
			show(mDetailsFragment);
			mSelectedId = itemId;
		}
	}

	/* onBackPressed requires API 5+. */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMasterFragment != null) {
				mMasterFragment.unhighlight();

				if (mDetailsFragment.isVisible()) {
					show(mMasterFragment);
					hide(mDetailsFragment);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		ZmanimItem item = (ZmanimItem) view.getTag();
		toggleDetails(item, view);
	}

	private void hide(ZmanimFragment fragment) {
		fragment.setVisibility(View.GONE);
	}

	private void show(ZmanimFragment fragment) {
		fragment.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onSearchRequested() {
		Location loc = mLocations.getLocation();
		// Have we been destroyed?
		if (loc == null)
			return false;

		Bundle appData = new Bundle();
		appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, loc);
		startSearch(null, false, appData, false);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ACTIVITY_LOCATIONS) {
			if (resultCode == RESULT_OK) {
				Location loc = data.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
				if (loc == null) {
					mLocations.setLocation(null);
					loc = mLocations.getLocation();
				}
				mLocations.setLocation(loc);
			}
		}
	}
}

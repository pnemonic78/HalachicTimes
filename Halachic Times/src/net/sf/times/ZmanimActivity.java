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

import net.sf.times.location.AddressProvider;
import net.sf.times.location.ZmanimAddress;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 * 
 * @author Moshe
 */
public class ZmanimActivity extends Activity implements LocationListener, OnDateSetListener {

	/** The date parameter. */
	public static final String PARAMETER_DATE = "date";
	/** The time parameter. */
	public static final String PARAMETER_TIME = "time";

	/** ISO 639 language code for "Hebrew". */
	public static final String ISO639_HEBREW = "he";
	/** ISO 639 language code for "Hebrew" - Java compatibility. */
	public static final String ISO639_HEBREW_FORMER = "iw";
	/** ISO 639 language code for "Yiddish" - Java compatibility. */
	public static final String ISO639_YIDDISH_FORMER = "ji";
	/** ISO 639 language code for "Yiddish". */
	public static final String ISO639_YIDDISH = "yi";

	/** The date. */
	private final Calendar mDate = Calendar.getInstance();
	/** The time zone. */
	private TimeZone mTimeZone;
	/** The list. */
	private ViewGroup mList;
	/** The location header. */
	private View mHeader;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	/** Address provider. */
	private AddressProvider mAddressProvider;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;
	/** The date picker. */
	private DatePickerDialog mDatePicker;
	/** The address. */
	private ZmanimAddress mAddress;
	/** The address fetcher. */
	private FindAddress mFindAddress;
	/** The gradient background. */
	private Drawable mBackground;
	/** Populate the header in UI thread. */
	private Runnable mPopulateHeader;

	/**
	 * Creates a new activity.
	 */
	public ZmanimActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		initLocation();

		Intent intent = getIntent();
		long date = intent.getLongExtra(PARAMETER_DATE, 0L);
		long time = intent.getLongExtra(PARAMETER_TIME, 0L);
		if (date == 0L) {
			if (time == 0L) {
				date = System.currentTimeMillis();
			} else {
				date = time;
			}
		}
		setDate(date);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mLocations.resume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mAddressProvider != null) {
			mAddressProvider.close();
			mAddressProvider = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocations.cancel(this);
		SQLiteDatabase.releaseMemory();
	}

	/** Initialise. */
	private void init() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mList = (ViewGroup) inflater.inflate(R.layout.times, null);
		mHeader = mList.findViewById(R.id.header);
		mSettings = new ZmanimSettings(this);

		setContentView(mList);
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
		mTimeZone = TimeZone.getDefault();

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
		if (mFindAddress != null)
			mFindAddress.interrupt();
		mFindAddress = new FindAddress(location);
		mFindAddress.start();
		populateHeader();
		populateTimes();
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

	/** Populate the list with times. */
	private void populateTimes() {
		// Have we been destroyed?
		Location loc = mLocations.getLocation();
		if (loc == null)
			return;
		final String locationName = loc.getProvider();
		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = Math.max(0, loc.getAltitude());
		final int candlesOffset = mSettings.getCandleLightingOffset();
		final boolean inIsrael = mLocations.inIsrael(loc, mTimeZone);

		GeoLocation gloc = new GeoLocation(locationName, latitude, longitude, altitude, TimeZone.getDefault());
		ComplexZmanimCalendar cal = new ComplexZmanimCalendar(gloc);
		cal.setCandleLightingOffset(candlesOffset);
		cal.setCalendar(mDate);

		ZmanimAdapter adapter = new ZmanimAdapter(this, mSettings);
		adapter.populate(cal, inIsrael, false);

		if (mList == null)
			return;
		ViewGroup list = (ViewGroup) mList.findViewById(R.id.list);
		if (list == null)
			return;
		if (mSettings.isBackgroundGradient()) {
			if (mBackground == null)
				mBackground = getResources().getDrawable(R.drawable.list_gradient);
			list.setBackgroundDrawable(mBackground);
		} else
			list.setBackgroundDrawable(null);
		adapter.bindViews(list);
	}

	/** Populate the header item. */
	private void populateHeader() {
		// Have we been destroyed?
		Location loc = mLocations.getLocation();
		if (loc == null)
			return;
		View header = mHeader;
		if (header == null)
			return;

		final String locationName = formatAddress();
		final String coordsText = mLocations.formatCoordinates();

		// Update the header.
		TextView address = (TextView) header.findViewById(R.id.address);
		address.setText(locationName);
		TextView coordinates = (TextView) header.findViewById(R.id.coordinates);
		coordinates.setText(coordsText);
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
		case R.id.menu_goto:
			mDatePicker = new DatePickerDialog(this, this, mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH));
			mDatePicker.show();
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
		populateTimes();
	}

	/**
	 * Find an address.
	 * 
	 * @author Moshe
	 */
	private class FindAddress extends Thread {

		private final Location mLocation;

		/** Creates a new finder. */
		public FindAddress(Location location) {
			super();
			mLocation = location;
		}

		@Override
		public void run() {
			if (mAddressProvider == null)
				mAddressProvider = new AddressProvider(ZmanimActivity.this);
			AddressProvider provider = mAddressProvider;
			Address nearest = provider.findNearestAddress(mLocation);
			if (nearest != null) {
				ZmanimAddress addr = (nearest instanceof ZmanimAddress) ? ((ZmanimAddress) nearest) : new ZmanimAddress(nearest);
				if (addr != null) {
					mAddress = addr;
					provider.insertAddress(mLocation, addr);
					if (mPopulateHeader == null) {
						mPopulateHeader = new Runnable() {

							@Override
							public void run() {
								populateHeader();
							}
						};
					}
					runOnUiThread(mPopulateHeader);
				}
			}
		}
	}

	/**
	 * Format the address for the current location or time zone.
	 * 
	 * @return the formatted address.
	 */
	private String formatAddress() {
		if (mAddress != null)
			return mAddress.getFormatted();
		if (mTimeZone != null)
			return mTimeZone.getDisplayName();
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
}

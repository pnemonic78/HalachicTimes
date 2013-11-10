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

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.AddressProvider;
import net.sf.times.location.AddressProvider.OnFindAddressListener;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocations;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
public class ZmanimActivity extends Activity implements LocationListener, OnDateSetListener, OnFindAddressListener, OnClickListener, OnGestureListener {

	/** The date parameter. */
	public static final String PARAMETER_DATE = "date";
	/** The time parameter. */
	public static final String PARAMETER_TIME = "time";
	/** The details list parameter. */
	public static final String PARAMETER_DETAILS = "details";
	/** The location parameter. */
	public static final String PARAMETER_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

	/** Activity id for searching locations. */
	private static final int ACTIVITY_LOCATIONS = 1;

	/** The date. */
	private final Calendar mDate = Calendar.getInstance();
	/** The location header. */
	private View mHeader;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
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
	/** The handler. */
	private final Handler mHandler;
	/** The gesture detector. */
	private GestureDetector mGestureDetector;
	/** Is locale RTL? */
	private boolean mLocaleRTL;

	/**
	 * Creates a new activity.
	 */
	public ZmanimActivity() {
		mHandler = new Handler();
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
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mMasterFragment.populateTimes(mDate);
					toggleDetails(itemId);
				}
			}, 1000L);
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

		setContentView(view);

		mGestureDetector = new GestureDetector(this, this);
		mGestureDetector.setIsLongpressEnabled(false);

		mHeader = view.findViewById(R.id.header);
		mMasterFragment = (ZmanimFragment) view.findViewById(R.id.list_fragment);
		mMasterFragment.setOnClickListener(this);
		mMasterFragment.setGestureDetector(mGestureDetector);
		mDetailsFragment = (ZmanimDetailsFragment) view.findViewById(R.id.details_fragment);
		mDetailsFragment.setGestureDetector(mGestureDetector);
		mSideBySide = view.findViewById(R.id.frame_fragments) == null;
		hide(mDetailsFragment);
	}

	/** Initialise the location providers. */
	private void initLocation() {
		mLocations = ZmanimLocations.getInstance(this, this);
		mLocaleRTL = ZmanimLocations.isLocaleRTL();
	}

	/**
	 * Set the date for the list.
	 * 
	 * @param date
	 *            the date.
	 */
	private void setDate(long date) {
		mDate.setTimeInMillis(date);
		mDate.setTimeZone(mLocations.getTimeZone());

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
		formatter.setHebrewFormat(mLocaleRTL);
		CharSequence dateHebrew = formatter.format(jewishDate);
		textHebrew.setText(dateHebrew);
	}

	@Override
	public void onLocationChanged(Location location) {
		ZmanimApplication app = (ZmanimApplication) getApplication();
		app.findAddress(location, this);
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
			final int year = mDate.get(Calendar.YEAR);
			final int month = mDate.get(Calendar.MONTH);
			final int day = mDate.get(Calendar.DAY_OF_MONTH);
			if (mDatePicker == null) {
				mDatePicker = new DatePickerDialog(this, this, year, month, day);
			} else {
				mDatePicker.updateDate(year, month, day);
			}
			mDatePicker.show();
			return true;
		case R.id.menu_location:
			Location loc = mLocations.getLocation();
			// Have we been destroyed?
			if (loc == null)
				break;

			Intent intent = new Intent(this, LocationActivity.class);
			intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
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
	public void onFindAddress(AddressProvider provider, Location location, Address address) {
		ZmanimAddress zaddr = (ZmanimAddress) address;
		mAddress = zaddr;
		provider.insertAddress(location, zaddr);
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

	@Override
	public boolean onDown(MotionEvent event) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent event) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// Go to date?
		float dX = e2.getX() - e1.getX();
		float dY = e2.getY() - e1.getY();
		if (Math.abs(dX) > Math.abs(dY)) {
			Calendar date = mDate;
			int day = date.get(Calendar.DATE);
			if (dX < 0) {
				date.set(Calendar.DATE, mLocaleRTL ? (day - 1) : (day + 1));
			} else {
				date.set(Calendar.DATE, mLocaleRTL ? (day + 1) : (day - 1));
			}
			setDate(date.getTimeInMillis());
			mMasterFragment.populateTimes(date);
			mDetailsFragment.populateTimes(date);
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mLocaleRTL = ZmanimLocations.isLocaleRTL();
	}
}

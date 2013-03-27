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
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.TextUtils;
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
public class ZmanimActivity extends Activity implements LocationListener, OnDateSetListener, OnFindAddressListener {

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
	protected ZmanimSettings mSettings;
	/** The date picker. */
	private DatePickerDialog mDatePicker;
	/** The address. */
	private ZmanimAddress mAddress;
	/** The gradient background. */
	private Drawable mBackground;
	/** Populate the header in UI thread. */
	private Runnable mPopulateHeader;
	private ZmanimAdapter mAdapter;
	private ZmanimReminder mReminder;
	private OnClickListener mOnClickListener;
	protected LayoutInflater mInflater;
	/** The detailed list panel. */
	private ViewGroup mLayoutDetails;
	/** The detailed list. */
	private ViewGroup mListDetails;
	/** The master item selected id. */
	private int mSelectedId;
	/** The master item selected row. */
	private View mSelectedRow;

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
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(PARAMETER_DATE, mDate.getTimeInMillis());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setDate(savedInstanceState.getLong(PARAMETER_DATE));
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
		if (mAddressProvider != null) {
			mAddressProvider.close();
			mAddressProvider = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocations.cancel(this);
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
		mList = (ViewGroup) view.findViewById(android.R.id.list);
		mLayoutDetails = (ViewGroup) view.findViewById(R.id.panel_details);
		mListDetails = (ViewGroup) view.findViewById(R.id.list_details);

		setContentView(view);
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
		mTimeZone = mLocations.getTimeZone();

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
		GeoLocation gloc = mLocations.getGeoLocation();
		if (gloc == null)
			return;

		ZmanimAdapter adapter = createAdapter(mDate, mLocations);
		adapter.populate(false);

		if (mList == null)
			return;
		ViewGroup list = mList;
		if (list == null)
			return;
		if (isBackgroundDrawable()) {
			if (mSettings.isBackgroundGradient()) {
				if (mBackground == null)
					mBackground = getResources().getDrawable(R.drawable.list_gradient);
				list.setBackgroundDrawable(mBackground);
			} else
				list.setBackgroundDrawable(null);
		}
		bindViews(list, adapter);
		mAdapter = adapter;
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
		// Have we been destroyed?
		Location loc = mLocations.getLocation();
		if (loc == null)
			return;
		View header = mHeader;
		if (header == null)
			return;

		final String locationName = formatAddress();
		final String coordsText = mLocations.formatCoordinates();

		// Update the location.
		TextView address = (TextView) header.findViewById(R.id.address);
		address.setText(locationName);
		TextView coordinates = (TextView) header.findViewById(R.id.coordinates);
		coordinates.setText(coordsText);
		coordinates.setVisibility(mSettings.isCoordinates() ? View.VISIBLE : View.GONE);
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

	@Override
	public void onAddressFound(Location location, ZmanimAddress address) {
		mAddress = address;
		mAddressProvider.insertAddress(location, address);
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
		String tz = mTimeZone.getDisplayName();
		if (!TextUtils.isEmpty(tz))
			return tz;
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
	 * Bind the times to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param adapter
	 *            the list adapter.
	 */
	private void bindViews(ViewGroup list, ZmanimAdapter adapter) {
		if (list == null)
			return;
		final int count = adapter.getCount();
		list.removeAllViews();

		ZmanimItem item;
		View row;

		for (int position = 0; position < count; position++) {
			item = adapter.getItem(position);
			row = adapter.getView(position, null, list);
			bindView(list, position, row, item);
		}
	}

	/**
	 * Bind the time to a list.
	 * 
	 * @param list
	 *            the list.
	 * @param position
	 *            the position index.
	 * @param view
	 *            the row view.
	 * @param item
	 *            the item.
	 */
	private void bindView(ViewGroup list, int position, View row, ZmanimItem item) {
		setOnClickListener(row, item);
		if (position > 0)
			mInflater.inflate(R.layout.divider, list);
		list.addView(row);
	}

	protected void setOnClickListener(View view, ZmanimItem item) {
		boolean clickable = view.isEnabled();
		final int id = item.titleId;
		if (id == R.string.candles)
			clickable = false;

		if (clickable) {
			if (mOnClickListener == null) {
				mOnClickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mSelectedRow != null) {
							// Deselect the previously selected row.
							// TODO implement me!
						}
						mSelectedRow = v;

						ZmanimItem item = (ZmanimItem) v.getTag();
						showDetails(item);
					}
				};
			}
			view.setOnClickListener(mOnClickListener);
		} else {
			view.setOnClickListener(null);
		}
	}

	private void showDetails(ZmanimItem item) {
		if (mLayoutDetails == null) {
			Intent intent = new Intent(this, ZmanimDetailsActivity.class);
			intent.putExtra(ZmanimActivity.PARAMETER_DATE, mDate.getTimeInMillis());
			intent.putExtra(ZmanimDetailsActivity.PARAMETER_ITEM, item.titleId);
			startActivity(intent);
		} else if ((mSelectedId == item.titleId) && (mLayoutDetails.getVisibility() == View.VISIBLE)) {
			mLayoutDetails.setVisibility(View.GONE);
		} else {
			mLayoutDetails.setVisibility(View.VISIBLE);
			populateDetailTimes(item.titleId);
		}
	}

	/**
	 * Populate the list with detailed times.
	 * 
	 * @param id
	 *            the time id.
	 */
	private void populateDetailTimes(int id) {
		// Have we been destroyed?
		GeoLocation gloc = mLocations.getGeoLocation();
		if (gloc == null)
			return;
		boolean inIsrael = mLocations.inIsrael();

		ZmanimAdapter adapterMaster = mAdapter;
		if (adapterMaster == null)
			return;
		ZmanimAdapter adapter = new ZmanimDetailsAdapter(this, mSettings, adapterMaster.getCalendar(), inIsrael, id);
		adapter.populate(false);

		if (mListDetails == null)
			return;
		bindViews(mListDetails, adapter);
		mSelectedId = id;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLayoutDetails != null) {
				if (mLayoutDetails.getVisibility() == View.VISIBLE) {
					if (mSelectedRow != null) {
						// TODO
						// mSelectedRow.setBackgroundDrawable(mDefaultBackground);
					}
					mLayoutDetails.setVisibility(View.GONE);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}

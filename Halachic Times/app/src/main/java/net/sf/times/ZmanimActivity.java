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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import net.sf.app.TodayDatePickerDialog;
import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.LocationActivity;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.view.animation.LayoutWeightAnimation;

import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimActivity extends Activity implements ZmanimLocationListener, OnDateSetListener, View.OnClickListener, OnGestureListener {

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

	private static final int WHAT_TOGGLE_DETAILS = 0;
	private static final int WHAT_COMPASS = 1;
	private static final int WHAT_DATE = 2;
	private static final int WHAT_LOCATION = 3;
	private static final int WHAT_SETTINGS = 4;
	private static final int WHAT_TODAY = 5;

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
	/** The address location. */
	private Location mAddressLocation;
	/** The address. */
	private ZmanimAddress mAddress;
	/** Populate the header in UI thread. */
	private Runnable mPopulateHeader;
	/** Update the location in UI thread. */
	private Runnable mUpdateLocation;
	private ZmanimReminder mReminder;
	protected LayoutInflater mInflater;
	/** The master fragment. */
	private ZmanimFragment<ZmanimAdapter> mMasterFragment;
	/** The details fragment switcher. */
	private ViewSwitcher mDetailsFragment;
	/** The details fragment. */
	private ZmanimDetailsFragment mDetailsListFragment;
	/** The candles fragment. */
	private CandlesFragment mCandesFragment;
	/** Is master fragment switched with details fragment? */
	private ViewSwitcher mSwitcher;
	/** The master item selected id. */
	private int mSelectedId;
	/** The gesture detector. */
	private GestureDetector mGestureDetector;
	/** Is locale RTL? */
	private boolean mLocaleRTL;
	/** Slide left-to-right animation. */
	private Animation mSlideLeftToRight;
	/** Slide right-to-left animation. */
	private Animation mSlideRightToLeft;
	/** Grow details animation. */
	private Animation mDetailsGrow;
	/** Shrink details animation. */
	private Animation mDetailsShrink;

	/** The handler. */
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case WHAT_TOGGLE_DETAILS:
					toggleDetails(msg.arg1);
					break;
				case WHAT_COMPASS:
					startActivity(new Intent(ZmanimActivity.this, CompassActivity.class));
					break;
				case WHAT_DATE:
					final int year = mDate.get(Calendar.YEAR);
					final int month = mDate.get(Calendar.MONTH);
					final int day = mDate.get(Calendar.DAY_OF_MONTH);
					if (mDatePicker == null) {
						mDatePicker = new TodayDatePickerDialog(ZmanimActivity.this, ZmanimActivity.this, year, month, day);
					} else {
						mDatePicker.updateDate(year, month, day);
					}
					mDatePicker.show();
					break;
				case WHAT_LOCATION:
					Location loc = mLocations.getLocation();
					// Have we been destroyed?
					if (loc == null)
						break;

					Intent intent = new Intent(ZmanimActivity.this, LocationActivity.class);
					intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
					startActivityForResult(intent, ACTIVITY_LOCATIONS);
					break;
				case WHAT_SETTINGS:
					startActivity(new Intent(ZmanimActivity.this, ZmanimPreferences.class));
					break;
				case WHAT_TODAY:
					setDate(System.currentTimeMillis());
					mMasterFragment.populateTimes(mDate);
					mDetailsListFragment.populateTimes(mDate);
					mCandesFragment.populateTimes(mDate);
					break;
			}
		}
	};

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
		mSelectedId = savedInstanceState.getInt(PARAMETER_DETAILS, mSelectedId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocations.start(this);
		if (mReminder == null)
			mReminder = createReminder();
		if (mReminder != null)
			mReminder.cancel();
		if (mSelectedId != 0) {
			// We need to wait for the list rows to get their default
			// backgrounds before we can highlight any row.
			Message msg = mHandler.obtainMessage(WHAT_TOGGLE_DETAILS, mSelectedId, 0);
			mHandler.sendMessageDelayed(msg, DateUtils.SECOND_IN_MILLIS);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mReminder != null) {
			// Don't run on UI thread.
			new Thread() {
				public void run() {
					mReminder.remind(mSettings);
				}
			}.start();
		}
		int itemId = mSelectedId;
		hideDetails();
		mSelectedId = itemId;
	}

	@Override
	protected void onStop() {
		mLocations.stop(this);
		super.onStop();
	}

	/** Initialise. */
	@SuppressWarnings("unchecked")
	private void init() {
		mSettings = new ZmanimSettings(this);

		mInflater = LayoutInflater.from(this);
		ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.times, null);

		setContentView(view);

		mGestureDetector = new GestureDetector(this, this);
		mGestureDetector.setIsLongpressEnabled(false);

		mHeader = view.findViewById(R.id.header);
		mMasterFragment = (ZmanimFragment<ZmanimAdapter>) view.findViewById(R.id.list_fragment);
		mMasterFragment.setOnClickListener(this);
		mMasterFragment.setGestureDetector(mGestureDetector);
		mDetailsFragment = (ViewSwitcher) view.findViewById(R.id.details_fragment);
		mDetailsListFragment = (ZmanimDetailsFragment) view.findViewById(R.id.details_list_fragment);
		mDetailsListFragment.setGestureDetector(mGestureDetector);
		mCandesFragment = (CandlesFragment) view.findViewById(R.id.candles_fragment);

		mSwitcher = (ViewSwitcher) view.findViewById(R.id.frame_fragments);
		if (mSwitcher != null) {
			Animation inAnim = AnimationUtils.makeInAnimation(this, false);
			inAnim.setDuration(400);
			mSwitcher.setInAnimation(inAnim);
			Animation outAnim = AnimationUtils.makeOutAnimation(this, true);
			outAnim.setDuration(400);
			mSwitcher.setOutAnimation(outAnim);
		}

		mSlideRightToLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		mSlideRightToLeft.setDuration(400);
		mSlideLeftToRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		mSlideLeftToRight.setDuration(400);
		mDetailsGrow = new LayoutWeightAnimation(mDetailsFragment, 0f, 2f);
		mDetailsGrow.setDuration(500);
		mDetailsShrink = new LayoutWeightAnimation(mDetailsFragment, 2f, 0f);
		mDetailsShrink.setDuration(500);
	}

	/** Initialise the location providers. */
	private void initLocation() {
		ZmanimApplication app = (ZmanimApplication) getApplication();
		mLocations = app.getLocations();
		mLocaleRTL = ZmanimLocations.isLocaleRTL();
	}

	/**
	 * Set the date for the list.
	 *
	 * @param date
	 * 		the date.
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
	}

	/**
	 * Set the date for the list.
	 *
	 * @param year
	 * 		the year.
	 * @param monthOfYear
	 * 		the month of the year.
	 * @param dayOfMonth
	 * 		the day of the month.
	 */
	private void setDate(int year, int monthOfYear, int dayOfMonth) {
		mDate.set(Calendar.YEAR, year);
		mDate.set(Calendar.MONTH, monthOfYear);
		mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		mDate.setTimeZone(mLocations.getTimeZone());

		View header = mHeader;
		// Have we been destroyed?
		if (header == null)
			return;
		CharSequence dateGregorian = DateUtils.formatDateTime(this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_WEEKDAY);
		TextView textGregorian = (TextView) header.findViewById(R.id.date_gregorian);
		textGregorian.setText(dateGregorian);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (ZmanimLocation.compareTo(mAddressLocation, location) != 0) {
			mAddress = null;
		}
		mAddressLocation = location;
		if (mUpdateLocation == null) {
			mUpdateLocation = new Runnable() {
				@Override
				public void run() {
					populateHeader();
					populateFragments(mDate);
				}
			};
		}
		runOnUiThread(mUpdateLocation);
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

	protected ZmanimReminder createReminder() {
		return new ZmanimReminder(this);
	}

	/** Populate the header item. */
	private void populateHeader() {
		View header = mHeader;
		// Have we been destroyed?
		if (header == null)
			return;
		Location loc = (mAddressLocation == null) ? mLocations.getLocation() : mAddressLocation;
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

		PackageManager pkg = getPackageManager();
		if (!pkg.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
			menu.removeItem(R.id.menu_compass);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_compass:
				mHandler.sendEmptyMessage(WHAT_COMPASS);
				return true;
			case R.id.menu_date:
				mHandler.sendEmptyMessage(WHAT_DATE);
				return true;
			case R.id.menu_location:
				mHandler.sendEmptyMessage(WHAT_LOCATION);
				return true;
			case R.id.menu_settings:
				mHandler.sendEmptyMessage(WHAT_SETTINGS);
				return true;
			case R.id.menu_today:
				mHandler.sendEmptyMessage(WHAT_TODAY);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		setDate(year, monthOfYear, dayOfMonth);
		populateFragments(mDate);
	}

	@Override
	public void onAddressChanged(Location location, ZmanimAddress address) {
		mAddressLocation = location;
		mAddress = address;
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

	@Override
	public void onElevationChanged(Location location) {
		onLocationChanged(location);
	}

	/**
	 * Format the address for the current location.
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
	 * 		the master item.
	 * @param view
	 * 		the master row view that was clicked.
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
	 * 		the master item id.
	 */
	protected void toggleDetails(int itemId) {
		if ((itemId == 0) || (itemId == R.string.fast_begins) || (itemId == R.string.fast_ends))
			return;

		if (mSwitcher != null) {
			if (itemId == R.string.candles) {
				mCandesFragment.populateTimes(mDate);
				mDetailsFragment.setDisplayedChild(1);
			} else {
				mDetailsListFragment.populateTimes(mDate, itemId);
				mDetailsFragment.setDisplayedChild(0);
			}
			if (isDetailsShowing())
				hideDetails();
			else
				showDetails();
			mSelectedId = itemId;
		} else if ((mSelectedId == itemId) && isDetailsShowing()) {
			hideDetails();
			mMasterFragment.unhighlight();
		} else {
			if (itemId == R.string.candles) {
				mDetailsFragment.setDisplayedChild(1);
			} else {
				mDetailsListFragment.populateTimes(mDate, itemId);
				mDetailsFragment.setDisplayedChild(0);
			}
			mMasterFragment.unhighlight();
			mMasterFragment.highlight(itemId);
			if (!isDetailsShowing())
				showDetails();
			mSelectedId = itemId;
		}
	}

	/* onBackPressed requires API 5+. */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMasterFragment != null) {
				mMasterFragment.unhighlight();

				if (isDetailsShowing()) {
					hideDetails();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		ZmanimItem item = (ZmanimItem) view.getTag(R.id.time);
		toggleDetails(item, view);
	}

	protected void hideDetails() {
		if (mSwitcher != null) {
			mSwitcher.showPrevious();

			// Not enough to hide the details switcher = must also hide its
			// children otherwise visible when sliding dates.
			if (mDetailsListFragment != null)
				mDetailsListFragment.setVisibility(View.INVISIBLE);
			if (mCandesFragment != null)
				mCandesFragment.setVisibility(View.INVISIBLE);
		} else {
			mDetailsFragment.startAnimation(mDetailsShrink);
		}
		mSelectedId = 0;
	}

	protected void showDetails() {
		if (mSwitcher != null) {
			mSwitcher.showNext();
		} else {
			mDetailsFragment.startAnimation(mDetailsGrow);
		}
	}

	protected boolean isDetailsShowing() {
		if ((mDetailsFragment == null) || (mDetailsFragment.getVisibility() != View.VISIBLE))
			return false;
		if (mSwitcher == null) {
			LinearLayout.LayoutParams lp = (LayoutParams) mDetailsFragment.getLayoutParams();
			return (lp.weight > 0);
		}
		return (mSwitcher.getCurrentView() == mDetailsFragment);
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
		// Disable fling if showing candles to avoid showing no candles.
		if (isDetailsShowing() && (mDetailsFragment.getDisplayedChild() != 0))
			return false;

		// Go to date?
		float dX = e2.getX() - e1.getX();
		float dY = e2.getY() - e1.getY();
		if (Math.abs(dX) > Math.abs(dY)) {
			Calendar date = mDate;
			int day = date.get(Calendar.DATE);
			if (dX < 0) {
				if (mLocaleRTL) {
					date.set(Calendar.DATE, day - 1);
					slideRight(mMasterFragment);
					slideRight(mDetailsFragment);
				} else {
					date.set(Calendar.DATE, day + 1);
					slideLeft(mMasterFragment);
					slideLeft(mDetailsFragment);
				}
			} else {
				if (mLocaleRTL) {
					date.set(Calendar.DATE, day + 1);
					slideLeft(mMasterFragment);
					slideLeft(mDetailsFragment);
				} else {
					date.set(Calendar.DATE, day - 1);
					slideRight(mMasterFragment);
					slideRight(mDetailsFragment);
				}
			}
			setDate(date.getTimeInMillis());
			populateFragments(date);
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

	/**
	 * Slide the view in from right to left.
	 *
	 * @param view
	 * 		the view to animate.
	 */
	protected void slideLeft(View view) {
		view.startAnimation(mSlideRightToLeft);
	}

	/**
	 * Slide the view in from left to right.
	 *
	 * @param view
	 * 		the view to animate.
	 */
	protected void slideRight(View view) {
		view.startAnimation(mSlideLeftToRight);
	}

	private void populateFragments(Calendar date) {
		mMasterFragment.populateTimes(mDate);
		mDetailsListFragment.populateTimes(mDate);
		mCandesFragment.populateTimes(mDate);
	}
}

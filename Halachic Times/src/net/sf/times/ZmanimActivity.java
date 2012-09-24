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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.times.location.AddressProvider;
import net.sf.times.location.ZmanimAddress;
import net.sourceforge.zmanim.ZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
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
import android.location.LocationManager;
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

	/** 1 second. */
	private static final long ONE_SECOND = 1000;
	/** 1 minute. */
	private static final long ONE_MINUTE = 60 * ONE_SECOND;
	/** 1 hour. */
	private static final long ONE_HOUR = 60 * ONE_MINUTE;
	/** 12 hours. */
	private static final long TWELVE_HOURS = 12 * ONE_HOUR;

	/** 11.5&deg; before sunrise. */
	private static final double ZENITH_TALLIS = 101.5;

	/** 1 kilometre. */
	private static final int ONE_KM = 1000;

	/** Holiday id for Shabbath. */
	private static final int SHABBATH = -1;

	/** No candles to light. */
	private static final int CANDLES_NONE = 0;
	/** Number of candles to light for Shabbath. */
	private static final int CANDLES_SHABBATH = 2;
	/** Number of candles to light for a festival. */
	private static final int CANDLES_FESTIVAL = 2;
	/** Number of candles to light for Yom Kippur. */
	private static final int CANDLES_YOM_KIPPUR = 1;

	/** Time zone ID for Jerusalem. */
	private static final String TZ_JERUSALEM = "Asia/Jerusalem";
	/** Time zone ID for Israeli Standard Time. */
	private static final String TZ_IST = "IST";

	/** Northern-most latitude for Israel. */
	private static final double ISRAEL_NORTH = 33.289212;
	/** Southern-most latitude for Israel. */
	private static final double ISRAEL_SOUTH = 29.489218;
	/** Eastern-most longitude for Israel. */
	private static final double ISRAEL_EAST = 35.891876;
	/** Western-most longitude for Israel. */
	private static final double ISRAEL_WEST = 34.215317;

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
	/** The list adapter. */
	private ZmanimAdapter mAdapter;
	/** Service provider for locations. */
	private LocationManager mLocationManager;
	/** The location. */
	private Location mLocation;
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

		if (mLocation == null) {
			Location loc = getLocationGPS();
			if (loc == null)
				loc = getLocationNetwork();
			if (loc == null)
				loc = getLocationSaved();
			if (loc == null)
				loc = getLocationTZ();
			onLocationChanged(loc);
		} else {
			populateHeader();
			populateTimes();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHeader = null;
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
			mLocationManager = null;
		}
		if (mAdapter != null) {
			mAdapter.clear();
			mAdapter = null;
		}
		if (mAddressProvider != null) {
			mAddressProvider.close();
			mAddressProvider = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		SQLiteDatabase.releaseMemory();
	}

	/** Initialise. */
	private void init() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mList = (ViewGroup) inflater.inflate(R.layout.times, null);
		mHeader = mList.findViewById(R.id.header);
		mAdapter = new ZmanimAdapter(this);
		mSettings = new ZmanimSettings(this);

		setContentView(mList);
	}

	/** Initialise the location providers. */
	private void initLocation() {
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			try {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE, ONE_KM, this);
			} catch (IllegalArgumentException iae) {
				System.err.println(this + ": " + iae.getLocalizedMessage());
			}
			try {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_MINUTE, ONE_KM, this);
			} catch (IllegalArgumentException iae) {
				System.err.println(this + ": " + iae.getLocalizedMessage());
			}
		}
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
		if (location == null)
			return;
		// Ignore old locations.
		if (mLocation != null) {
			if (mLocation.getTime() >= location.getTime())
				return;
		}
		mLocation = location;
		mSettings.putLocation(location);
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
		Location loc = mLocation;
		if (loc == null)
			return;
		final String locationName = loc.getProvider();
		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = Math.max(0, loc.getAltitude());
		final int candlesOffset = mSettings.getCandleLightingOffset();

		GeoLocation gloc = new GeoLocation(locationName, latitude, longitude, altitude, TimeZone.getDefault());
		ZmanimCalendar cal = new ZmanimCalendar(gloc);
		cal.setCandleLightingOffset(candlesOffset);
		cal.setCalendar(mDate);

		int candlesCount = 0;
		Date candlesWhen = cal.getCandleLighting();
		if (candlesWhen != null) {
			candlesCount = getCandles(mDate);
		}

		// Have we been destroyed?
		ZmanimAdapter adapter = mAdapter;
		if (adapter == null)
			return;
		synchronized (adapter) {
			adapter.clear();

			adapter.add(R.string.dawn_16deg, R.string.dawn_16deg_summary, cal.getAlosHashachar());
			adapter.add(R.string.dawn_72min, R.string.dawn_72min_summary, cal.getAlos72());
			adapter.add(R.string.earliest, R.string.earliest_summary, cal.getSunriseOffsetByDegrees(ZENITH_TALLIS));
			adapter.add(R.string.sunrise, R.string.sunrise_summary, cal.getSunrise());
			adapter.add(R.string.shema_mga, R.string.shema_mga_summary, cal.getSofZmanShmaMGA());
			adapter.add(R.string.shema_gra, R.string.shema_gra_summary, cal.getSofZmanShmaGRA());
			adapter.add(R.string.prayers_mga, R.string.prayers_mga_summary, cal.getSofZmanTfilaMGA());
			adapter.add(R.string.prayers_gra, R.string.prayers_gra_summary, cal.getSofZmanTfilaGRA());
			adapter.add(R.string.midday, R.string.midday_summary, cal.getChatzos());
			adapter.add(R.string.earliest_mincha, R.string.earliest_mincha_summary, cal.getMinchaGedola());
			adapter.add(R.string.mincha, R.string.mincha_summary, cal.getMinchaKetana());
			adapter.add(R.string.plug_hamincha, R.string.plug_hamincha_summary, cal.getPlagHamincha());
			if (candlesCount > 0) {
				String summary = getString(R.string.candles_summary, candlesOffset);
				adapter.add(R.string.candles, summary, cal.getCandleLighting());
			}
			adapter.add(R.string.sunset, R.string.sunset_summary, cal.getSunset());
			if (candlesCount < 0) {
				adapter.add(R.string.candles, R.string.nightfall_3stars_summary, cal.getTzais());
			}
			adapter.add(R.string.nightfall_3stars, R.string.nightfall_3stars_summary, cal.getTzais());
			adapter.add(R.string.nightfall_72min, R.string.nightfall_72min_summary, cal.getTzais72());
			adapter.add(R.string.midnight, R.string.midnight_summary, cal.getChatzos().getTime() + TWELVE_HOURS);
		}

		final int count = adapter.getCount();
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
		list.removeAllViews();
		for (int i = 0; i < count; i++) {
			list.addView(adapter.getView(i, null, list));
		}
	}

	/** Populate the header item. */
	private void populateHeader() {
		// Have we been destroyed?
		Location loc = mLocation;
		if (loc == null)
			return;
		View header = mHeader;
		if (header == null)
			return;

		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		String locationName = formatAddress();

		final String notation = mSettings.getCoordinatesFormat();
		final String latitudeText;
		final String longitudeText;
		if (ZmanimSettings.FORMAT_SEXIGESIMAL.equals(notation)) {
			latitudeText = Location.convert(latitude, Location.FORMAT_SECONDS);
			longitudeText = Location.convert(longitude, Location.FORMAT_SECONDS);
		} else {
			latitudeText = String.format("%1$.7f", latitude);
			longitudeText = String.format("%1$.7f", longitude);
		}
		final String coordsFormat = getString(R.string.location_coords);
		final String coordsText = String.format(coordsFormat, latitudeText, longitudeText);

		// Update the header.
		TextView address = (TextView) header.findViewById(R.id.address);
		address.setText(locationName);
		TextView coordinates = (TextView) header.findViewById(R.id.coordinates);
		coordinates.setText(coordsText);
	}

	/**
	 * Get the number of candles to light.
	 * 
	 * @param cal
	 *            the Gregorian date.
	 * @return the candles to light. Upper bits are the day type. The lower bits
	 *         art the number of candles. Positive values indicate lighting
	 *         times before sunset. Negative values indicate lighting times
	 *         after nightfall.
	 */
	private int getCandles(Calendar cal) {
		final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		final boolean isShabbath = (dayOfWeek == Calendar.SATURDAY);
		final boolean inIsrael = isIsrael(mLocation, mTimeZone);

		// Check if the following day is special, because we can't check
		// EREV_CHANUKAH.
		cal.add(Calendar.DAY_OF_MONTH, 1);
		JewishCalendar jcal = new JewishCalendar(cal);
		jcal.setInIsrael(inIsrael);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		int holiday = jcal.getYomTovIndex();

		int candles = CANDLES_NONE;

		switch (holiday) {
		case JewishCalendar.ROSH_HASHANA:
		case JewishCalendar.SUCCOS:
		case JewishCalendar.SHEMINI_ATZERES:
		case JewishCalendar.SIMCHAS_TORAH:
		case JewishCalendar.PESACH:
		case JewishCalendar.SHAVUOS:
			candles = CANDLES_FESTIVAL;
			break;
		case JewishCalendar.YOM_KIPPUR:
			candles = CANDLES_YOM_KIPPUR;
			break;
		case JewishCalendar.CHANUKAH:
			candles = jcal.getDayOfChanukah();
			break;
		default:
			if (dayOfWeek == Calendar.FRIDAY) {
				holiday = SHABBATH;
				candles = CANDLES_SHABBATH;
			}
			break;
		}

		// Forbidden to light candles during Shabbath.
		candles = isShabbath ? -candles : candles;

		return candles;
	}

	/**
	 * Is the user in Israel? <br>
	 * Used to determine if user is in diaspora for 2-day festivals.
	 * 
	 * @param loc
	 *            the location.
	 * @param timeZone
	 *            the time zone.
	 * @return {@code true} if user is in Israel - {@code false} otherwise.
	 */
	private boolean isIsrael(Location loc, TimeZone timeZone) {
		if (loc == null) {
			if (timeZone == null)
				timeZone = TimeZone.getDefault();
			String id = timeZone.getID();
			if (TZ_JERUSALEM.equals(id) || TZ_IST.equals(id))
				return true;
			return false;
		}

		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		return (latitude <= ISRAEL_NORTH) && (latitude >= ISRAEL_SOUTH) && (longitude >= ISRAEL_WEST) && (longitude <= ISRAEL_EAST);
	}

	/**
	 * Get a location from GPS.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	private Location getLocationGPS() {
		if (mLocationManager == null)
			return null;
		return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Get a location from the GSM network.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	private Location getLocationNetwork() {
		if (mLocationManager == null)
			return null;
		return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Get a location from the time zone.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	private Location getLocationTZ() {
		if (mAddressProvider == null)
			mAddressProvider = new AddressProvider(this);
		return mAddressProvider.findTimeZone(mTimeZone);
	}

	/**
	 * Get a location from the saved preferences.
	 * 
	 * @return the location - {@code null} otherwise.
	 */
	private Location getLocationSaved() {
		return mSettings.getLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.zmanim, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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

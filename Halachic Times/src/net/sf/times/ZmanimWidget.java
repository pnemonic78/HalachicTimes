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
import java.util.Date;
import java.util.TimeZone;

import net.sourceforge.zmanim.ZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers in a widget.
 * 
 * @author Moshe
 */
public class ZmanimWidget extends AppWidgetProvider implements LocationListener {

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

	/** Which activity to start? */
	private static final String EXTRA_ACTIVITY = "activity";

	/** The context. */
	private Context mContext;
	private AppWidgetManager mAppWidgetManager;
	private int[] mAppWidgetIds;
	/** The remote views. */
	private RemoteViews mViews;
	/** Provider for locations. */
	private ZmanimLocations mLocations;
	/** The settings and preferences. */
	private ZmanimSettings mSettings;

	/**
	 * Constructs a new widget.
	 */
	public ZmanimWidget() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		mContext = context;

		String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			IntentFilter timeChanged = new IntentFilter(Intent.ACTION_TIME_CHANGED);
			context.getApplicationContext().registerReceiver(this, timeChanged);

			IntentFilter tzChanged = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
			context.getApplicationContext().registerReceiver(this, tzChanged);
		} else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
			populateTimes();
		} else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
			populateTimes();
		} else {
			String activity = intent.getStringExtra(EXTRA_ACTIVITY);
			if (activity != null) {
				Intent activityIntent = new Intent();
				activityIntent.setClassName(context, activity);
				activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(activityIntent);
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		mContext = context;
		mAppWidgetManager = appWidgetManager;
		mAppWidgetIds = appWidgetIds;
		if (mSettings == null)
			mSettings = new ZmanimSettings(context);
		if (mLocations == null)
			mLocations = ZmanimLocations.getInstance(context, this);
		mLocations.resume(this);
		mViews = new RemoteViews(context.getPackageName(), R.layout.times_widget);

		// Intent activityIntent = new Intent(Intent.ACTION_MAIN);
		// activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// activityIntent.setClass(context, ZmanimActivity.class);
		// activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// PendingIntent activityPendingIntent =
		// PendingIntent.getBroadcast(context, R.id.list, activityIntent,
		// PendingIntent.FLAG_UPDATE_CURRENT);

		// Pass the activity to ourselves, because starting another activity is
		// not working.
		Intent activityIntent = new Intent(context, ZmanimWidget.class);
		activityIntent.putExtra(EXTRA_ACTIVITY, ZmanimActivity.class.getName());
		PendingIntent activityPendingIntent = PendingIntent.getBroadcast(context, R.id.list, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mViews.setOnClickPendingIntent(R.id.list, activityPendingIntent);

		populateTimes();
	}

	/** Populate the list with times. */
	private void populateTimes() {
		Context context = mContext;
		if (mAppWidgetManager == null)
			return;
		if (mAppWidgetIds == null)
			return;
		RemoteViews views = mViews;
		if (views == null)
			return;

		// Have we been destroyed?
		Location loc = mLocations.getLocation();
		if (loc == null)
			return;
		final String locationName = loc.getProvider();
		final double latitude = loc.getLatitude();
		final double longitude = loc.getLongitude();
		final double altitude = Math.max(0, loc.getAltitude());
		final int candlesOffset = mSettings.getCandleLightingOffset();

		Calendar today = Calendar.getInstance();
		long now = today.getTimeInMillis();
		GeoLocation gloc = new GeoLocation(locationName, latitude, longitude, altitude, TimeZone.getDefault());
		ZmanimCalendar cal = new ZmanimCalendar(gloc);
		cal.setCandleLightingOffset(candlesOffset);
		cal.setCalendar(today);

		int candlesCount = 0;
		Date candlesWhen = cal.getCandleLighting();
		if (candlesWhen != null) {
			candlesCount = getCandles(today, loc);
		}

		populateTime(context, views, R.id.dawn_16deg_row, R.id.dawn_16deg_time, now, cal.getAlosHashachar());
		populateTime(context, views, R.id.dawn_72min_row, R.id.dawn_72min_time, now, cal.getAlos72());
		populateTime(context, views, R.id.earliest_row, R.id.earliest_time, now, cal.getSunriseOffsetByDegrees(ZENITH_TALLIS));
		populateTime(context, views, R.id.sunrise_row, R.id.sunrise_time, now, cal.getSunrise());
		populateTime(context, views, R.id.shema_mga_row, R.id.shema_mga_time, now, cal.getSofZmanShmaMGA());
		populateTime(context, views, R.id.shema_gra_row, R.id.shema_gra_time, now, cal.getSofZmanShmaGRA());
		populateTime(context, views, R.id.prayers_mga_row, R.id.prayers_mga_time, now, cal.getSofZmanTfilaMGA());
		populateTime(context, views, R.id.prayers_gra_row, R.id.prayers_gra_time, now, cal.getSofZmanTfilaGRA());
		populateTime(context, views, R.id.midday_row, R.id.midday_time, now, cal.getChatzos());
		populateTime(context, views, R.id.earliest_mincha_row, R.id.earliest_mincha_time, now, cal.getMinchaGedola());
		populateTime(context, views, R.id.mincha_row, R.id.mincha_time, now, cal.getMinchaKetana());
		populateTime(context, views, R.id.plug_hamincha_row, R.id.plug_hamincha_time, now, cal.getPlagHamincha());
		if (candlesCount > 0) {
			populateTime(context, views, R.id.candles_row, R.id.candles_time, now, cal.getCandleLighting());
		} else {
			populateTime(context, views, R.id.candles_row, R.id.candles_time, now, 0L);
		}
		populateTime(context, views, R.id.sunset_row, R.id.sunset_time, now, cal.getSunset());
		if (candlesCount < 0) {
			populateTime(context, views, R.id.candles2_row, R.id.candles2_time, now, cal.getTzais());
		} else {
			populateTime(context, views, R.id.candles2_row, R.id.candles2_time, now, 0L);
		}
		populateTime(context, views, R.id.nightfall_3stars_row, R.id.nightfall_3stars_time, now, cal.getTzais());
		populateTime(context, views, R.id.nightfall_72min_row, R.id.nightfall_72min_time, now, cal.getTzais72());
		populateTime(context, views, R.id.midnight_row, R.id.midnight_time, now, cal.getChatzos().getTime() + TWELVE_HOURS);

		mAppWidgetManager.updateAppWidget(mAppWidgetIds, mViews);
	}

	private void populateTime(Context context, RemoteViews views, int rowId, int timeId, long now, Date time) {
		populateTime(context, views, rowId, timeId, now, time.getTime());
	}

	private void populateTime(Context context, RemoteViews views, int rowId, int timeId, long now, long time) {
		if (time < now) {
			views.setViewVisibility(rowId, View.GONE);
		} else {
			String text = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
			views.setTextViewText(timeId, text);
		}
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		if (mLocations != null)
			mLocations.cancel(this);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		if (mLocations != null)
			mLocations.resume(this);
	}

	@Override
	public void onLocationChanged(Location location) {
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

	/**
	 * Get the number of candles to light.
	 * 
	 * @param cal
	 *            the Gregorian date.
	 * @param location
	 *            the location.
	 * @return the candles to light. Upper bits are the day type. The lower bits
	 *         art the number of candles. Positive values indicate lighting
	 *         times before sunset. Negative values indicate lighting times
	 *         after nightfall.
	 */
	private int getCandles(Calendar cal, Location location) {
		final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		final boolean isShabbath = (dayOfWeek == Calendar.SATURDAY);
		final boolean inIsrael = mLocations.inIsrael(location, TimeZone.getDefault());

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
}

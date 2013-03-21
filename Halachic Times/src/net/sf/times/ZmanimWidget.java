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
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers in a widget.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimWidget extends AppWidgetProvider implements LocationListener {

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

		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			IntentFilter timeChanged = new IntentFilter(Intent.ACTION_TIME_CHANGED);
			context.getApplicationContext().registerReceiver(this, timeChanged);

			IntentFilter dateChanged = new IntentFilter(Intent.ACTION_DATE_CHANGED);
			context.getApplicationContext().registerReceiver(this, dateChanged);

			IntentFilter tzChanged = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
			context.getApplicationContext().registerReceiver(this, tzChanged);
		} else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
			populateTimes();
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
		if (mLocations == null)
			mLocations = ZmanimLocations.getInstance(context, this);
		mLocations.resume(this);

		populateTimes();
	}

	/** Populate the list with times. */
	private void populateTimes() {
		Context context = mContext;
		if (mAppWidgetManager == null)
			mAppWidgetManager = AppWidgetManager.getInstance(context);
		if (mAppWidgetIds == null) {
			ComponentName provider = new ComponentName(context, ZmanimWidget.class);
			mAppWidgetIds = mAppWidgetManager.getAppWidgetIds(provider);
			if (mAppWidgetIds == null)
				return;
		}
		if (mViews == null) {
			mViews = new RemoteViews(context.getPackageName(), R.layout.times_widget);

			// Pass the activity to ourselves, because starting another activity
			// is not working.
			Intent activityIntent = new Intent(context, ZmanimWidget.class);
			activityIntent.putExtra(EXTRA_ACTIVITY, ZmanimActivity.class.getName());
			PendingIntent activityPendingIntent = PendingIntent.getBroadcast(context, android.R.id.list, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			mViews.setOnClickPendingIntent(android.R.id.list, activityPendingIntent);
		}
		final RemoteViews views = mViews;

		if (mSettings == null)
			mSettings = new ZmanimSettings(context);
		if (mLocations == null) {
			mLocations = ZmanimLocations.getInstance(context, this);
			mLocations.resume(this);
		}
		GeoLocation gloc = mLocations.getGeoLocation();
		if (gloc == null)
			return;
		ComplexZmanimCalendar today = new ComplexZmanimCalendar(gloc);
		final boolean inIsrael = mLocations.inIsrael();

		ZmanimAdapter adapter = new ZmanimAdapter(mContext, mSettings, today, inIsrael);
		adapter.populate(true);
		bindViews(views, adapter);

		mAppWidgetManager.updateAppWidget(mAppWidgetIds, views);

		scheduleForMidnight(mContext);
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
	 * Schedule an update for midnight to populate the new day's list.
	 * 
	 * @param context
	 *            the context.
	 */
	private void scheduleForMidnight(Context context) {
		Calendar midnight = Calendar.getInstance();
		midnight.set(Calendar.HOUR_OF_DAY, 0);
		midnight.set(Calendar.MINUTE, 0);
		midnight.set(Calendar.SECOND, 0);
		midnight.set(Calendar.MILLISECOND, 100);
		midnight.add(Calendar.DATE, 1);
		Intent alarmIntent = new Intent(context, ZmanimWidget.class);
		alarmIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		alarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetIds);
		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.set(AlarmManager.RTC, midnight.getTimeInMillis(), alarmPendingIntent);
	}

	/**
	 * Bind the times to remote views.
	 * 
	 * @param views
	 *            the remote views.
	 */
	private void bindViews(RemoteViews views, ZmanimAdapter adapter) {
		final int count = adapter.getCount();
		ZmanimItem item;

		for (int position = 0; position < count; position++) {
			item = adapter.getItem(position);
			bindView(views, item);
		}
	}

	/**
	 * Bind the times to remote views.
	 * 
	 * @param list
	 *            the remote list.
	 */
	private void bindView(RemoteViews list, ZmanimItem item) {
		if (item.elapsed || (item.timeLabel == null)) {
			list.setViewVisibility(item.titleId, View.GONE);
		} else {
			list.setViewVisibility(item.titleId, View.VISIBLE);
			list.setTextViewText(item.timeId, item.timeLabel);
		}
	}
}

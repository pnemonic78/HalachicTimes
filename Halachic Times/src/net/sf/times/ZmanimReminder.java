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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Reminders. Receive alarm events, or date-time events, to update reminders.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimReminder extends BroadcastReceiver {

	private static final String TAG = "ZmanimReminder";

	/** Reminder id for notifications. */
	private static final int ID_NOTIFY = 1;
	/** Reminder id for alarms. */
	private static final int ID_ALARM = 2;

	private static final long HALF_MINUTE = 30 * DateUtils.SECOND_IN_MILLIS;

	private Context mContext;

	/**
	 * Creats a new reminder.
	 * 
	 * @param context
	 *            the context.
	 */
	public ZmanimReminder(Context context) {
		mContext = context;
	}

	/** No-argument constructor for broadcast receiver. */
	public ZmanimReminder() {
	}

	/**
	 * Setup the first reminder for today.
	 * 
	 * @param settings
	 *            the settings.
	 * @param locations
	 *            the locations provider.
	 */
	public void remind(ZmanimSettings settings, ZmanimLocations locations) {
		// Have we been destroyed?
		GeoLocation gloc = locations.getGeoLocation();
		if (gloc == null)
			return;
		ComplexZmanimCalendar today = new ComplexZmanimCalendar(gloc);
		boolean inIsrael = locations.inIsrael();

		ZmanimAdapter adapter = new ZmanimAdapter(mContext, settings, today, inIsrael);
		adapter.populate(false);

		remind(settings, adapter);
	}

	/**
	 * Setup the first reminder for today.
	 * 
	 * @param settings
	 *            the settings.
	 * @param adapter
	 *            the populated adapter.
	 */
	private void remind(ZmanimSettings settings, ZmanimAdapter adapter) {
		cancel();

		final long now = System.currentTimeMillis();
		final long was = now - HALF_MINUTE;
		final long soon = now + HALF_MINUTE;
		ZmanimItem item;
		long before;
		long when;
		boolean needToday = true;
		boolean needTodayLater = true;
		boolean needTomorrow = true;
		boolean needWeek = true;
		int id;

		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			item = adapter.getItem(i);
			id = item.timeId;
			before = settings.getReminder(id);
			if (before >= 0L) {
				when = item.time - before;
				if (needToday && (was <= when) && (when <= soon)) {
					notifyNow(item.titleId, item.time);
					needToday = false;
				}
				if (needTodayLater && (now < when)) {
					String whenFormat = DateUtils.formatDateTime(mContext, when, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
					String timeFormat = DateUtils.formatDateTime(mContext, item.time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
					Log.i(TAG, "notify today at [" + whenFormat + "] for [" + timeFormat + "]");

					notifyFuture(when);
					needTodayLater = false;
					needTomorrow = false;
					needWeek = false;
				}
			}
		}

		ComplexZmanimCalendar zcal = adapter.getCalendar();
		Calendar cal = zcal.getCalendar();
		if (needTomorrow) {
			// Populate the adapter with tomorrow's times.
			cal.add(Calendar.DAY_OF_MONTH, 1);
			adapter.clear();
			adapter.populate(false);

			count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				item = adapter.getItem(i);
				id = item.timeId;
				before = settings.getReminder(id);
				if (before >= 0L) {
					when = item.time - before;
					if (needToday && (was <= when) && (when <= soon)) {
						notifyNow(item.titleId, item.time);
						needToday = false;
					}
					if (now < when) {
						String whenFormat = DateUtils.formatDateTime(mContext, when, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
						String timeFormat = DateUtils.formatDateTime(mContext, item.time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
						Log.i(TAG, "notify tomorrow at [" + whenFormat + "] for [" + timeFormat + "]");

						notifyFuture(when);
						needWeek = false;
						break;
					}
				}
			}
		}

		if (needWeek) {
			// Populate the adapter with week's worth of times to check
			// "Candle lighting".
			int daysUntilFriday = Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK);
			if ((daysUntilFriday == -1) || (daysUntilFriday == 0) || (daysUntilFriday == 1)) {
				// We already checked a Friday above.
				return;
			}
			adapter.clear();
			for (int d = 1; d <= 5; d++) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				adapter.populate(false);
			}

			count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				item = adapter.getItem(i);
				id = item.timeId;
				// id is supposed to be 1 of 3 possible values: R.id.candles_row
				// or R.id.candles_time or R.string.candles
				before = settings.getReminder(id);
				if (before >= 0L) {
					when = item.time - before;
					if (now < when) {
						String whenFormat = DateUtils.formatDateTime(mContext, when, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
						String timeFormat = DateUtils.formatDateTime(mContext, item.time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
						Log.i(TAG, "notify week at [" + whenFormat + "] for [" + timeFormat + "]");

						notifyFuture(when);
						break;
					}
				}
			}
		}
	}

	/**
	 * Cancel all reminders.
	 */
	public void cancel() {
		AlarmManager alarms = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = createAlarmIntent();
		alarms.cancel(alarmIntent);

		NotificationManager notis = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notis.cancelAll();
	}

	private void notifyNow(int titleId, long when) {
		CharSequence contentTitle = mContext.getText(R.string.app_name);
		CharSequence contentText = mContext.getText(titleId);

		// Clicking on the item will launch the main activity.
		PendingIntent contentIntent = createActivityIntent();

		Notification noti = new Notification();
		noti.icon = R.drawable.ic_launcher;
		noti.defaults = Notification.DEFAULT_ALL;
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.when = when;// When the zman is due.
		noti.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

		NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(ID_NOTIFY, noti);
	}

	/**
	 * Set alarm manager to alert us for the next reminder.
	 * 
	 * @param when
	 *            the upcoming reminder.
	 */
	private void notifyFuture(long when) {
		AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = createAlarmIntent();
		manager.set(AlarmManager.RTC_WAKEUP, when, alarmIntent);
	}

	private PendingIntent createActivityIntent() {
		PackageManager pkg = mContext.getPackageManager();
		Intent intent = pkg.getLaunchIntentForPackage(mContext.getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	private PendingIntent createAlarmIntent() {
		Intent intent = new Intent(mContext, ZmanimReminder.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ID_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String nowFormat = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
		Log.i(TAG, "onReceive " + intent + " [" + nowFormat + "]");

		mContext = context;
		boolean update = false;

		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action))
			update = true;
		else if (Intent.ACTION_DATE_CHANGED.equals(action))
			update = true;
		else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action))
			update = true;
		else if (Intent.ACTION_TIME_CHANGED.equals(action))
			update = true;
		else {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				update = (extras.getInt(Intent.EXTRA_ALARM_COUNT, 0) > 0);
			}
		}

		if (update) {
			ZmanimSettings settings = new ZmanimSettings(context);
			ZmanimLocations locations = ZmanimLocations.getInstance(context);
			remind(settings, locations);
		}
	}
}

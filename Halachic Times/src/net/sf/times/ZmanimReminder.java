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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

	private static final long WAS_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;
	private static final long SOON_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;

	private Context mContext;
	private SimpleDateFormat format;

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
		Log.i(TAG, "remind");
		cancel();

		final long now = System.currentTimeMillis();
		final long latest = settings.getLatestReminder();
		Log.i(TAG, "remind latest [" + formatDateTime(latest) + "]");
		final long was = now - WAS_DELTA;
		final long soon = now + SOON_DELTA;
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
				if (needToday && (latest < was) && (was <= when) && (when <= soon)) {
					notifyNow(item.titleId, item.time);
					settings.setLatestReminder(now);
					needToday = false;
				}
				if (needTodayLater && (now < when)) {
					String whenFormat = formatDateTime(when);
					String timeFormat = formatDateTime(item.time);
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
					if (needToday && (latest < was) && (was <= when) && (when <= soon)) {
						notifyNow(item.titleId, item.time);
						settings.setLatestReminder(now);
						needToday = false;
					}
					if (now < when) {
						String whenFormat = formatDateTime(when);
						String timeFormat = formatDateTime(item.time);
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
				// All non-candle times were checked "today" and "tomorrow"
				// above.
				if ((id != R.id.candles_row) && (id != R.id.candles_time) && (id != R.string.candles))
					continue;
				before = settings.getReminder(id);
				if (before >= 0L) {
					when = item.time - before;
					if (now < when) {
						String whenFormat = formatDateTime(when);
						String timeFormat = formatDateTime(item.time);
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
		Log.i(TAG, "cancel");
		AlarmManager alarms = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = createAlarmIntent();
		alarms.cancel(alarmIntent);

		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	@SuppressWarnings("deprecation")
	private void notifyNow(int titleId, long when) {
		CharSequence contentTitle = mContext.getText(R.string.app_name);
		CharSequence contentText = mContext.getText(titleId);
		Log.i(TAG, "notify now [" + contentText + "]");

		// Clicking on the item will launch the main activity.
		PendingIntent contentIntent = createActivityIntent();

		Notification noti = new Notification();
		noti.icon = R.drawable.ic_launcher;
		noti.defaults = Notification.DEFAULT_ALL;
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.when = when;// When the zman is supposed to occur.
		noti.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);

		// Wake up the device to notify the user.
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		WakeLock wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wake.acquire(2000L);

		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ID_NOTIFY, noti);
	}

	/**
	 * Set alarm manager to alert us for the next reminder.
	 * 
	 * @param when
	 *            the upcoming reminder.
	 */
	private void notifyFuture(long when) {
		Log.i(TAG, "notify future [" + formatDateTime(when) + "]");
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
		String nowFormat = formatDateTime(System.currentTimeMillis());
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

	/**
	 * Format the date and time with seconds.<br>
	 * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
	 * 
	 * @param time
	 *            the time to format.
	 * @return the formatted time.
	 */
	private String formatDateTime(long time) {
		if (format == null) {
			format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
		}
		return format.format(new Date(time));
	}
}

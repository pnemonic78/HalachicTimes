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
import net.sf.times.location.ZmanimLocations;
import net.sourceforge.zmanim.util.GeoLocation;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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

	/**
	 * Reminder id for all notifications.<br>
	 * Newer notifications will override current notifications.
	 */
	private static final int ID_NOTIFY = 1;
	/** Reminder id for alarms. */
	private static final int ID_ALARM = 2;

	private static final long WAS_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;
	private static final long SOON_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;

	private Context mContext;
	private SimpleDateFormat mFormat;
	/** The adapter. */
	private ZmanimAdapter mAdapter;

	/**
	 * Creates a new reminder manager.
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
	public void remind(ZmanimSettings settings) {
		final Context context = mContext;
		ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
		ZmanimLocations locations = app.getLocations();
		GeoLocation gloc = locations.getGeoLocation();
		// Have we been destroyed?
		if (gloc == null)
			return;

		ZmanimAdapter adapter = mAdapter;
		if (adapter == null) {
			adapter = new ZmanimAdapter(context, settings);
			mAdapter = adapter;
		}
		adapter.setCalendar(System.currentTimeMillis());
		adapter.setGeoLocation(gloc);
		adapter.setInIsrael(locations.inIsrael());
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

		final Context context = mContext;
		final long now = System.currentTimeMillis();
		final long latest = settings.getLatestReminder();
		Log.i(TAG, "remind latest [" + formatDateTime(latest) + "]");
		final long was = now - WAS_DELTA;
		final long soon = now + SOON_DELTA;
		ZmanimItem item;
		ZmanimItem itemFirst = null;
		long before;
		long when;
		long whenFirst = Long.MAX_VALUE;
		boolean needToday = true;
		boolean needTomorrow = true;
		boolean needWeek = true;
		int id;

		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			item = adapter.getItem(i);
			id = item.titleId;
			before = settings.getReminder(id);

			if ((before >= 0L) && (item.time != null)) {
				when = item.time.getTime() - before;
				if (needToday && (latest < was) && (was <= when) && (when <= soon)) {
					notifyNow(context, settings, item);
					settings.setLatestReminder(now);
					needToday = false;
				}
				if ((now < when) && (when < whenFirst)) {
					itemFirst = item;
					whenFirst = when;
				}
			}
		}
		if (itemFirst != null) {
			String whenFormat = formatDateTime(whenFirst);
			String timeFormat = formatDateTime(itemFirst.time);
			Log.i(TAG, "notify today at [" + whenFormat + "] for [" + timeFormat + "]");

			notifyFuture(context, whenFirst);
			needTomorrow = false;
			needWeek = false;
		}

		Calendar cal = adapter.getCalendar().getCalendar();
		if (needTomorrow) {
			// Populate the adapter with tomorrow's times.
			cal.add(Calendar.DAY_OF_MONTH, 1);
			adapter.populate(false);
			itemFirst = null;
			whenFirst = Long.MAX_VALUE;

			count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				item = adapter.getItem(i);
				id = item.titleId;
				before = settings.getReminder(id);
				if ((before >= 0L) && (item.time != null)) {
					when = item.time.getTime() - before;
					if (needToday && (latest < was) && (was <= when) && (when <= soon)) {
						notifyNow(context, settings, item);
						settings.setLatestReminder(now);
						needToday = false;
					}
					if ((now < when) && (when < whenFirst)) {
						itemFirst = item;
						whenFirst = when;
					}
				}
			}
			if (itemFirst != null) {
				String whenFormat = formatDateTime(whenFirst);
				String timeFormat = formatDateTime(itemFirst.time);
				Log.i(TAG, "notify tomorrow at [" + whenFormat + "] for [" + timeFormat + "]");

				notifyFuture(context, whenFirst);
				needWeek = false;
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
			itemFirst = null;
			whenFirst = Long.MAX_VALUE;

			count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				item = adapter.getItem(i);
				id = item.titleId;
				// All non-candle times were checked "today" and "tomorrow"
				// above.
				if ((id != R.id.candles_row) && (id != R.string.candles))
					continue;
				before = settings.getReminder(id);
				if ((before >= 0L) && (item.time != null)) {
					when = item.time.getTime() - before;
					if ((now < when) && (when < whenFirst)) {
						itemFirst = item;
						whenFirst = when;
					}
				}
			}
			if (itemFirst != null) {
				String whenFormat = formatDateTime(whenFirst);
				String timeFormat = formatDateTime(itemFirst.time);
				Log.i(TAG, "notify week at [" + whenFormat + "] for [" + timeFormat + "]");

				notifyFuture(context, whenFirst);
			}
		}
	}

	/**
	 * Cancel all reminders.
	 */
	public void cancel() {
		Log.i(TAG, "cancel");
		final Context context = mContext;
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = createAlarmIntent(context);
		alarms.cancel(alarmIntent);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	/**
	 * Notify now.
	 * 
	 * @param context
	 *            the context.
	 * @param settings
	 *            the settings.
	 * @param item
	 *            the zmanim item to notify about.
	 */
	@SuppressLint("NewApi")
	private void notifyNow(Context context, ZmanimSettings settings, ZmanimItem item) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			notifyNowHoneycomb(context, settings, item);
		} else {
			notifyNowEclair(context, settings, item);
		}
	}

	/**
	 * Set alarm manager to alert us for the next reminder.
	 * 
	 * @param context
	 *            the context.
	 * @param when
	 *            the upcoming reminder.
	 */
	private void notifyFuture(Context context, long when) {
		Log.i(TAG, "notify future [" + formatDateTime(when) + "]");
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = createAlarmIntent(context);
		manager.set(AlarmManager.RTC_WAKEUP, when, alarmIntent);
	}

	private PendingIntent createActivityIntent(Context context) {
		PackageManager pkg = context.getPackageManager();
		Intent intent = pkg.getLaunchIntentForPackage(context.getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	private PendingIntent createAlarmIntent(Context context) {
		Intent intent = new Intent(context, ZmanimReminder.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
			remind(settings);
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
	private String formatDateTime(Date time) {
		if (mFormat == null) {
			mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
		}
		return mFormat.format(time);
	}

	/**
	 * Format the date and time with seconds.
	 * 
	 * @param time
	 *            the time to format.
	 * @return the formatted time.
	 * @see #formatDateTime(Date)
	 */
	private String formatDateTime(long time) {
		return formatDateTime(new Date(time));
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "Wakelock", "NewApi" })
	private void notifyNowEclair(Context context, ZmanimSettings settings, ZmanimItem item) {
		CharSequence contentTitle = context.getText(item.titleId);
		CharSequence contentText = item.summary;
		Log.i(TAG, "notify now [" + contentTitle + "]");

		// Clicking on the item will launch the main activity.
		PendingIntent contentIntent = createActivityIntent(context);

		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		Notification notification = new Notification();
		notification.audioStreamType = settings.getReminderStream();
		notification.icon = R.drawable.stat_notify_time;
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = Color.YELLOW;
		notification.ledOffMS = 0;
		notification.ledOnMS = 1;
		notification.when = item.time.getTime();// When the zman is supposed
												// to occur.
		notification.sound = sound;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// Wake up the device to notify the user.
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wake.acquire(3000L);// enough time to also hear an alarm tone

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ID_NOTIFY, notification);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "Wakelock", "NewApi" })
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void notifyNowHoneycomb(Context context, ZmanimSettings settings, ZmanimItem item) {
		CharSequence contentTitle = context.getText(item.titleId);
		CharSequence contentText = item.summary;
		Log.i(TAG, "notify now [" + contentTitle + "]");

		// Clicking on the item will launch the main activity.
		PendingIntent contentIntent = createActivityIntent(context);

		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent);
		builder.setContentText(contentText);
		builder.setContentTitle(contentTitle);
		builder.setDefaults(Notification.DEFAULT_ALL);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		builder.setLights(Color.YELLOW, 1, 0);
		builder.setSmallIcon(R.drawable.stat_notify_time);
		builder.setSound(sound, settings.getReminderStream());
		builder.setWhen(item.time.getTime());// When the zman is supposed to
												// occur.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			builder.setShowWhen(true);
		}
		Notification notification = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder.build();
		} else {
			notification = builder.getNotification();
		}

		// Wake up the device to notify the user.
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wake.acquire(3000L);// enough time to also hear an alarm tone

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ID_NOTIFY, notification);
	}
}

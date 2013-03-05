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

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;

/**
 * Reminders.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimReminder {

	/** Reminder id for notifications. */
	public static final int ID_NOTIFY = 1;
	/** Reminder id for alarms. */
	public static final int ID_ALARM = 2;

	private static final long HALF_MINUTE = 30 * DateUtils.SECOND_IN_MILLIS;

	private final Context mContext;
	private final ZmanimSettings mSettings;
	private final ZmanimLocations mLocations;

	public ZmanimReminder(Context context, ZmanimSettings settings, ZmanimLocations locations) {
		mContext = context;
		mSettings = settings;
		mLocations = locations;

		// TODO register alarm receiver in manifest for changes to:
		// Intent.ACTION_TIME_CHANGED
		// Intent.ACTION_DATE_CHANGED
		// Intent.ACTION_TIMEZONE_CHANGED
	}

	/**
	 * Setup the first reminder for today.
	 */
	public void remind() {
		cancel();

		// Have we been destroyed?
		GeoLocation gloc = mLocations.getGeoLocation();
		if (gloc == null)
			return;
		ComplexZmanimCalendar today = new ComplexZmanimCalendar(gloc);
		final boolean inIsrael = mLocations.inIsrael();

		ZmanimAdapter adapter = new ZmanimAdapter(mContext, mSettings);
		adapter.populate(today, inIsrael, false);

		final long now = today.getCalendar().getTimeInMillis();
		final long was = now - HALF_MINUTE;
		final long soon = now + HALF_MINUTE;
		final int count = adapter.getCount();
		ZmanimItem item;
		int before;
		long when;

		for (int i = 0; i < count; i++) {
			item = adapter.getItem(i);
			// Find the first remind of the day (that is now or in the future,
			// and has a reminder).
			before = mSettings.getReminder(item.timeId);
			if (before >= 0) {
				when = item.time - (before * DateUtils.MINUTE_IN_MILLIS);
				if ((was <= when) && (when <= soon)) {
					notifyNow(item.titleId, item.time);
					break;
				} else if (now < when) {
					notifyFuture(when);
					break;
				}
			}
		}
	}

	private void cancel() {
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
		noti.when = when;// When the prayer is due.
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
		Intent intent = new Intent(mContext, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ID_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
}

/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.times;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.util.Log;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.SimpleZmanimPreferences;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static java.lang.System.currentTimeMillis;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminder {

    private static final String TAG = "ZmanimReminder";

    /**
     * Id for reminder notifications.<br>
     * Newer notifications will override current notifications.
     */
    private static final int ID_NOTIFY = 1;
    /** Id for alarms. */
    private static final int ID_ALARM_REMINDER = 2;
    /**
     * Id for upcoming time notification.<br>
     * Newer notifications will override current notifications.
     */
    private static final int ID_NOTIFY_UPCOMING = 3;
    /** Id for alarms for upcoming time notification. */
    private static final int ID_ALARM_UPCOMING = 4;

    private static final long WAS_DELTA = 30 * SECOND_IN_MILLIS;
    private static final long SOON_DELTA = 30 * SECOND_IN_MILLIS;
    /** The number of days to check forwards for a reminder. */
    private static final int DAYS_FORWARD = 30;

    /* Yellow represents the sun or a candle flame. */
    private static final int LED_COLOR = Color.YELLOW;
    private static final int LED_ON = 750;
    private static final int LED_OFF = 500;

    /** Extras name for the reminder id. */
    private static final String EXTRA_REMINDER_ID = "reminder_id";
    /** Extras name for the reminder title. */
    private static final String EXTRA_REMINDER_TITLE = "reminder_title";
    /** Extras name for the reminder text. */
    private static final String EXTRA_REMINDER_TEXT = "reminder_text";
    /** Extras name for the reminder time. */
    private static final String EXTRA_REMINDER_TIME = "reminder_time";

    /** Action to remind. */
    public static final String ACTION_REMIND = "net.sf.times.action.REMIND";
    /** Action to update reminders. */
    public static final String ACTION_UPDATE = "net.sf.times.action.UPDATE";
    /** Action to cancel reminders. */
    public static final String ACTION_CANCEL = "net.sf.times.action.CANCEL";
    /** Action to silence reminders. */
    public static final String ACTION_SILENCE = "net.sf.times.action.SILENCE";

    /** How much time to wait for the notification sound once entered into a day not allowed to disturb. */
    private static final long STOP_NOTIFICATION_AFTER = MINUTE_IN_MILLIS * 3;

    private final Context context;
    private SimpleDateFormat dateFormat;
    /** The adapter. */
    private ZmanimAdapter adapter;

    /**
     * Constructs a new service.
     *
     * @param context
     *         The context.
     */
    public ZmanimReminder(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    /**
     * Setup the first reminder for today.
     *
     * @param context
     *         the context.
     */
    public void remind(final Context context) {
        ZmanimPreferences settings = new SimpleZmanimPreferences(context);
        remind(context, settings);
    }

    /**
     * Setup the first reminder for today.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     */
    public void remind(final Context context, ZmanimPreferences settings) {
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        ZmanimLocations locations = app.getLocations();
        GeoLocation gloc = locations.getGeoLocation();
        // Have we been destroyed?
        if (gloc == null)
            return;

        ZmanimPopulater<ZmanimAdapter> populater = new ZmanimPopulater<>(context, settings);
        populater.setCalendar(currentTimeMillis());
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        ZmanimAdapter adapter = this.adapter;
        if (adapter == null) {
            adapter = new ZmanimAdapter(context, settings);
            this.adapter = adapter;
        }

        remind(context, settings, populater, adapter);
    }

    /**
     * Setup the first reminder for the week.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     * @param adapter
     *         the populated adapter.
     */
    private void remind(final Context context, ZmanimPreferences settings, ZmanimPopulater<ZmanimAdapter> populater, ZmanimAdapter adapter) {
        final long latest = settings.getLatestReminder();
        Log.i(TAG, "remind latest [" + formatDateTime(latest) + "]");

        final Calendar gcal = Calendar.getInstance();
        final long now = gcal.getTimeInMillis();
        final long was = now - WAS_DELTA;
        final long soon = now + SOON_DELTA;
        ZmanimItem item;
        ZmanimItem itemFirst = null;
        long when;
        long whenFirst = Long.MAX_VALUE;
        boolean nextDay = true;
        int count;
        final boolean upcomingNotification = settings.isUpcomingNotification();
        ZmanimItem itemUpcoming = null;
        long whenUpcoming = Long.MAX_VALUE;

        JewishCalendar jcal = new JewishCalendar(gcal);
        jcal.setInIsrael(populater.isInIsrael());
        Calendar cal = populater.getCalendar().getCalendar();

        // Find the first reminder in the upcoming week.
        for (int day = 1; nextDay && (day <= DAYS_FORWARD); day++) {
            if (day > 1) {
                gcal.add(Calendar.DAY_OF_MONTH, 1);
                jcal.setDate(gcal);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                populater.setCalendar(cal);
            }
            populater.populate(adapter, false);

            count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                item = adapter.getItem(i);

                // Is the zman to be reminded?
                when = settings.getReminder(item.titleId, item.time);
                if ((when != ZmanimPreferences.NEVER) && allowReminder(settings, item, jcal)) {
                    if (nextDay && (latest < when) && (was <= when) && (when <= soon)) {
                        notifyNow(context, settings, item);
                        nextDay = false;
                    }
                    if ((now < when) && (when < whenFirst)) {
                        itemFirst = item;
                        whenFirst = when;
                    }
                }

                // Is the zman to be notified?
                if (upcomingNotification) {
                    when = item.time;
                    if ((when != ZmanimPreferences.NEVER) && (now <= when) && (when < whenUpcoming)) {
                        itemUpcoming = item;
                        whenUpcoming = when;
                    }
                }
            }
        }
        if (itemFirst != null) {
            item = itemFirst;
            Log.i(TAG, "notify at [" + formatDateTime(whenFirst) + "] for [" + formatDateTime(item.time) + "]");
            notifyFuture(context, item, whenFirst);
        }
        if (itemUpcoming != null) {
            notifyUpcoming(context, settings, itemUpcoming);
        }
    }

    /**
     * Cancel all reminders.
     *
     * @param context
     *         the context.
     */
    public void cancel(final Context context) {
        Log.i(TAG, "cancel");
        PendingIntent alarmIntent = createAlarmIntent(context, null);
        PendingIntent upcomingIntent = createUpcomingIntent(context);

        AlarmManager alarms = getAlarmManager(context);
        alarms.cancel(alarmIntent);
        alarms.cancel(upcomingIntent);

        NotificationManager nm = getNotificationManager(context);
        nm.cancel(ID_NOTIFY);
        nm.cancel(ID_NOTIFY_UPCOMING);
    }

    /**
     * Notify now.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     * @param item
     *         the zmanim item to notify about.
     */
    private void notifyNow(Context context, ZmanimPreferences settings, ZmanimItem item) {
        CharSequence contentTitle = context.getText(item.titleId);
        CharSequence contentText = item.summary;
        long when = item.time;
        ZmanimReminderItem reminderItem = new ZmanimReminderItem(item.titleId, contentTitle, contentText, when);

        notifyNow(context, settings, reminderItem);
    }

    /**
     * Notify now.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     * @param item
     *         the reminder item.
     */
    private void notifyNow(Context context, ZmanimPreferences settings, ZmanimReminderItem item) {
        Log.i(TAG, "notify now [" + item.getTitle() + "] for [" + formatDateTime(item.getTime()) + "]");
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification = createReminderNotification(context, settings, item, contentIntent);
        postReminderNotification(context, settings, notification);

        silenceFuture(context, item, currentTimeMillis() + STOP_NOTIFICATION_AFTER);
    }

    /**
     * Set alarm manager to alert us for the next reminder.
     *
     * @param context
     *         the context.
     * @param item
     *         the zmanim item to notify about.
     * @param triggerAt
     *         the upcoming reminder.
     */
    private void notifyFuture(Context context, ZmanimItem item, long triggerAt) {
        CharSequence contentTitle = context.getText(item.titleId);
        long when = item.time;

        Log.i(TAG, "notify future [" + contentTitle + "] at [" + formatDateTime(triggerAt) + "] for [" + formatDateTime(when) + "]");

        AlarmManager manager = getAlarmManager(context);
        PendingIntent alarmIntent = createAlarmIntent(context, item);
        manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
    }

    /**
     * Create the intent for when user clicks on the notification to launch the main activity.
     *
     * @param context
     *         the context.
     * @return the pending intent.
     */
    private PendingIntent createActivityIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createAlarmIntent(Context context, ZmanimItem item) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_REMIND);

        if (item != null) {
            CharSequence contentTitle = context.getText(item.titleId);
            CharSequence contentText = item.summary;
            long when = item.time;

            intent.putExtra(EXTRA_REMINDER_ID, item.titleId);
            intent.putExtra(EXTRA_REMINDER_TITLE, contentTitle);
            intent.putExtra(EXTRA_REMINDER_TEXT, contentText);
            intent.putExtra(EXTRA_REMINDER_TIME, when);
        }

        return PendingIntent.getService(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Create the intent to cancel notifications.
     *
     * @param context
     *         the context.
     * @return the pending intent.
     */
    private PendingIntent createCancelIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_CANCEL);

        return PendingIntent.getService(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void process(@Nullable Intent intent) {
        final Context context = getContext();
        Log.i(TAG, "process " + intent + " [" + formatDateTime(currentTimeMillis()) + "]");
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        boolean update = false;
        ZmanimPreferences settings = new SimpleZmanimPreferences(context);
        Bundle extras;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
            case Intent.ACTION_TIME_CHANGED:
            case ACTION_UPDATE:
                update = true;
                break;
            case ACTION_CANCEL:
                cancel(context);
                break;
            case ACTION_REMIND:
                extras = intent.getExtras();
                if (extras != null) {
                    int id = extras.getInt(EXTRA_REMINDER_ID);
                    CharSequence contentTitle = extras.getCharSequence(EXTRA_REMINDER_TITLE);
                    CharSequence contentText = extras.getCharSequence(EXTRA_REMINDER_TEXT);
                    long when = extras.getLong(EXTRA_REMINDER_TIME, 0L);

                    if ((contentTitle != null) && (contentText != null) && (when > 0L)) {
                        ZmanimReminderItem reminderItem = new ZmanimReminderItem(id, contentTitle, contentText, when);
                        notifyNow(context, settings, reminderItem);
                    }
                    update = true;
                }
                break;
            case ACTION_SILENCE:
                extras = intent.getExtras();
                if (extras != null) {
                    int id = extras.getInt(EXTRA_REMINDER_ID);
                    CharSequence contentTitle = extras.getCharSequence(EXTRA_REMINDER_TITLE);
                    CharSequence contentText = extras.getCharSequence(EXTRA_REMINDER_TEXT);
                    long when = extras.getLong(EXTRA_REMINDER_TIME, 0L);

                    if ((contentTitle != null) && (contentText != null) && (when > 0L)) {
                        ZmanimReminderItem reminderItem = new ZmanimReminderItem(id, contentTitle, contentText, when);
                        silence(context, settings, reminderItem);
                    }
                    update = true;
                } else {
                    cancel(context);
                }
                break;
        }
        if (update) {
            remind(context);
        }
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(Date time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }

    private Notification createReminderNotification(Context context, ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        return createReminderNotification(context, settings, item, contentIntent, false);
    }

    @SuppressLint("NewApi")
    private Notification createReminderNotification(Context context, ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent, boolean silent) {
        CharSequence contentTitle = item.getTitle();
        CharSequence contentText = item.getText();
        long when = item.getTime();

        int audioStreamType = settings.getReminderStream();
        Uri sound = silent ? null : settings.getReminderRingtone();

        Notification.Builder builder = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_solar))
                .setSmallIcon(R.drawable.stat_notify_time)
                .setSound(sound, audioStreamType)
                .setWhen(when);
        if (!silent) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLights(LED_COLOR, LED_ON, LED_OFF);

        }
        if (SDK_INT >= JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        if (SDK_INT >= LOLLIPOP) {
            builder.setCategory(audioStreamType == AudioManager.STREAM_ALARM ? Notification.CATEGORY_ALARM : Notification.CATEGORY_REMINDER);
        }
        Notification notification;
        if (SDK_INT >= JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }

    @SuppressLint("Wakelock")
    private void postReminderNotification(Context context, ZmanimPreferences settings, Notification notification) {
        // Wake up the device to notify the user.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wake.acquire(3000L);// enough time to also hear an alarm tone

        NotificationManager nm = getNotificationManager(context);
        nm.notify(ID_NOTIFY, notification);

        // This was the last notification.
        final long now = currentTimeMillis();
        settings.setLatestReminder(now);
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param item
     *         the item that should be reminded.
     * @param jcal
     *         the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private boolean allowReminder(ZmanimPreferences settings, ZmanimItem item, JewishCalendar jcal) {
        return allowReminder(settings, item.titleId, jcal);
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param settings
     *         the preferences with reminder day flags.
     * @param itemId
     *         the item that should be reminded.
     * @param jcal
     *         the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private boolean allowReminder(ZmanimPreferences settings, int itemId, JewishCalendar jcal) {
        int dayOfWeek = jcal.getDayOfWeek();
        int holidayIndex = jcal.getYomTovIndex();

        switch (holidayIndex) {
            case JewishCalendar.PESACH:
            case JewishCalendar.SHAVUOS:
            case JewishCalendar.ROSH_HASHANA:
            case JewishCalendar.YOM_KIPPUR:
            case JewishCalendar.SUCCOS:
            case JewishCalendar.SHEMINI_ATZERES:
            case JewishCalendar.SIMCHAS_TORAH:
                dayOfWeek = Calendar.SATURDAY;
                break;
        }

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return settings.isReminderSunday(itemId);
            case Calendar.MONDAY:
                return settings.isReminderMonday(itemId);
            case Calendar.TUESDAY:
                return settings.isReminderTuesday(itemId);
            case Calendar.WEDNESDAY:
                return settings.isReminderWednesday(itemId);
            case Calendar.THURSDAY:
                return settings.isReminderThursday(itemId);
            case Calendar.FRIDAY:
                return settings.isReminderFriday(itemId);
            case Calendar.SATURDAY:
                return settings.isReminderSaturday(itemId);
        }

        return true;
    }

    /**
     * Set alarm manager to cancel alert reminders.
     *
     * @param context
     *         the context.
     * @param triggerAt
     *         when to stop.
     */
    private void cancelFuture(Context context, long triggerAt) {
        Log.i(TAG, "cancel future at [" + formatDateTime(triggerAt) + "]");

        AlarmManager manager = getAlarmManager(context);
        PendingIntent alarmIntent = createCancelIntent(context);
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    @SuppressLint("NewApi")
    private Notification createUpcomingNotification(Context context, ZmanimPreferences settings, ZmanimItem item, PendingIntent contentIntent) {
        CharSequence contentTitle = context.getText(item.titleId);
        CharSequence contentText = item.summary;
        long when = item.time;
        Log.i(TAG, "notify upcoming [" + contentTitle + "] for [" + formatDateTime(when) + "]");

        Notification.Builder builder = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_solar))
                .setOngoing(true)
                .setSmallIcon(R.drawable.stat_notify_time)
                .setWhen(when);
        if (SDK_INT >= JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        if (SDK_INT >= LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_REMINDER);
        }
        Notification notification;
        if (SDK_INT >= JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }

    private void postUpcomingNotification(Context context, ZmanimPreferences settings, Notification notification) {
        NotificationManager nm = getNotificationManager(context);
        nm.notify(ID_NOTIFY_UPCOMING, notification);
    }

    /**
     * Notify upcoming time.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     * @param item
     *         the next item.
     */
    private void notifyUpcoming(Context context, ZmanimPreferences settings, ZmanimItem item) {
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification = createUpcomingNotification(context, settings, item, contentIntent);
        postUpcomingNotification(context, settings, notification);

        long triggerAt = item.time + MINUTE_IN_MILLIS;
        AlarmManager manager = getAlarmManager(context);
        PendingIntent alarmIntent = createUpcomingIntent(context);
        manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
    }

    private PendingIntent createUpcomingIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_UPDATE);

        return PendingIntent.getService(context, ID_ALARM_UPCOMING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Silence the notification at some time in the future.
     *
     * @param context
     *         the context.
     * @param item
     *         the item to show.
     * @param triggerAt
     *         when to silence.
     */
    private void silenceFuture(Context context, ZmanimReminderItem item, long triggerAt) {
        Log.i(TAG, "silence future at [" + formatDateTime(triggerAt) + "]");
        if (item == null) {
            cancelFuture(context, triggerAt);
            return;
        }

        AlarmManager manager = getAlarmManager(context);
        PendingIntent alarmIntent = createSilenceIntent(context, item);
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    /**
     * Create the intent to silence notifications.
     *
     * @param context
     *         the context.
     * @return the pending intent.
     */
    private PendingIntent createSilenceIntent(Context context, ZmanimReminderItem item) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_SILENCE);

        intent.putExtra(EXTRA_REMINDER_ID, item.getId());
        intent.putExtra(EXTRA_REMINDER_TITLE, item.getTitle());
        intent.putExtra(EXTRA_REMINDER_TEXT, item.getText());
        intent.putExtra(EXTRA_REMINDER_TIME, item.getTime());

        return PendingIntent.getService(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Replace the current notification with a silent notification.
     *
     * @param context
     *         the context.
     * @param settings
     *         the preferences.
     * @param item
     *         the reminder item.
     */
    private void silence(Context context, ZmanimPreferences settings, ZmanimReminderItem item) {
        Log.i(TAG, "silence now [" + item.getTitle() + "] for [" + formatDateTime(item.getTime()) + "]");
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification = createSilenceNotification(context, settings, item, contentIntent);
        postSilenceNotification(context, notification);
    }

    private Notification createSilenceNotification(Context context, ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        return createReminderNotification(context, settings, item, contentIntent, true);
    }

    private void postSilenceNotification(Context context, Notification notification) {
        NotificationManager nm = getNotificationManager(context);
        nm.cancel(ID_NOTIFY); // Kill the notification so that the sound stops playing.
        nm.notify(ID_NOTIFY, notification);
    }

    /**
     * Get the notification manager.
     *
     * @param context
     *         the context.
     * @return the manager.
     */
    protected NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Get the alarm manager.
     *
     * @param context
     *         the context.
     * @return the manager.
     */
    protected AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}

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
package net.sf.times.remind;

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
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.util.Log;

import net.sf.times.R;
import net.sf.times.ZmanimActivity;
import net.sf.times.ZmanimAdapter;
import net.sf.times.ZmanimApplication;
import net.sf.times.ZmanimItem;
import net.sf.times.ZmanimPopulater;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.SimpleZmanimPreferences;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Notification.DEFAULT_VIBRATE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static java.lang.System.currentTimeMillis;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;
import static net.sf.times.ZmanimItem.NEVER;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.ROSH_HASHANA;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHAVUOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHEMINI_ATZERES;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SIMCHAS_TORAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SUCCOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

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

    private static final String CHANNEL_REMINDER = "reminder";
    private static final String CHANNEL_REMINDER_ALARM = "reminder_alarm";
    private static final String CHANNEL_UPCOMING = "upcoming";

    private final Context context;
    private SimpleDateFormat dateFormat;
    /** The adapter. */
    private ZmanimAdapter adapter;

    /**
     * Constructs a new service.
     *
     * @param context The context.
     */
    public ZmanimReminder(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    /**
     * Setup the first reminder for today.
     */
    public void remind() {
        ZmanimPreferences settings = new SimpleZmanimPreferences(context);
        remind(settings);
    }

    /**
     * Setup the first reminder for today.
     *
     * @param settings the preferences.
     */
    public void remind(ZmanimPreferences settings) {
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

        remind(settings, populater, adapter);
    }

    /**
     * Setup the first reminder for the week.
     *
     * @param settings the preferences.
     * @param adapter  the populated adapter.
     */
    private void remind(ZmanimPreferences settings, ZmanimPopulater<ZmanimAdapter> populater, ZmanimAdapter adapter) {
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
                if ((when != NEVER) && allowReminder(settings, item, jcal)) {
                    if (nextDay && (latest < when) && (was <= when) && (when <= soon)) {
                        notifyNow(settings, item);
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
                    if ((when != NEVER) && (now <= when) && (when < whenUpcoming)) {
                        itemUpcoming = item;
                        whenUpcoming = when;
                    }
                }
            }
        }
        if (itemFirst != null) {
            item = itemFirst;
            Log.i(TAG, "notify at [" + formatDateTime(whenFirst) + "] for [" + formatDateTime(item.time) + "]");
            notifyFuture(item, whenFirst);
        }
        if (itemUpcoming != null) {
            notifyUpcoming(settings, itemUpcoming);
        }
    }

    /**
     * Cancel all reminders.
     */
    public void cancel() {
        Log.i(TAG, "cancel");
        PendingIntent alarmIntent = createAlarmIntent(null);
        PendingIntent upcomingIntent = createUpcomingIntent();

        AlarmManager alarms = getAlarmManager();
        alarms.cancel(alarmIntent);
        alarms.cancel(upcomingIntent);

        NotificationManager nm = getNotificationManager();
        nm.cancel(ID_NOTIFY);
        nm.cancel(ID_NOTIFY_UPCOMING);
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the zmanim item to notify about.
     */
    public void notifyNow(ZmanimPreferences settings, ZmanimItem item) {
        CharSequence contentTitle = context.getText(item.titleId);
        CharSequence contentText = item.summary;
        long when = item.time;
        ZmanimReminderItem reminderItem = new ZmanimReminderItem(item.titleId, contentTitle, contentText, when);

        notifyNow(settings, reminderItem);
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    public void notifyNow(ZmanimPreferences settings, ZmanimReminderItem item) {
        Log.i(TAG, "notify now [" + item.title + "] for [" + formatDateTime(item.time) + "]");
        PendingIntent contentIntent = createActivityIntent();

        Notification notification = createReminderNotification(settings, item, contentIntent);
        postReminderNotification(settings, notification);

        silenceFuture(item, currentTimeMillis() + STOP_NOTIFICATION_AFTER);
    }

    /**
     * Set alarm manager to alert us for the next reminder.
     *
     * @param item      the zmanim item to notify about.
     * @param triggerAt the upcoming reminder.
     */
    public void notifyFuture(ZmanimItem item, long triggerAt) {
        CharSequence contentTitle = context.getText(item.titleId);
        long when = item.time;

        Log.i(TAG, "notify future [" + contentTitle + "] at [" + formatDateTime(triggerAt) + "] for [" + formatDateTime(when) + "]");

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createAlarmIntent(item);
        manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
    }

    /**
     * Create the intent for when user clicks on the notification to launch the main activity.
     *
     * @return the pending intent.
     */
    private PendingIntent createActivityIntent() {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        if (intent == null) {
            Log.w(TAG, "Launch activity not found!");
            intent = new Intent(context, ZmanimActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createAlarmIntent(ZmanimItem item) {
        Intent intent = new Intent(context, getReceiverClass());
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

        return PendingIntent.getBroadcast(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Create the intent to cancel notifications.
     *
     * @return the pending intent.
     */
    private PendingIntent createCancelIntent() {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_CANCEL);

        return PendingIntent.getBroadcast(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                cancel();
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
                        notifyNow(settings, reminderItem);
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
                        silence(settings, reminderItem);
                    }
                    update = true;
                } else {
                    cancel();
                }
                break;
        }
        if (update) {
            remind();
        }
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time the time to format.
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
     * @param time the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }

    private Notification createReminderNotification(ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        return createReminderNotification(settings, item, contentIntent, false);
    }

    private Notification.Builder createNotificationBuilder(CharSequence contentTitle,
                                                           CharSequence contentText,
                                                           long when,
                                                           PendingIntent contentIntent,
                                                           String channelId) {
        Notification.Builder builder;
        if (SDK_INT >= O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setContentIntent(contentIntent)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_solar))
                .setSmallIcon(R.drawable.stat_notify_time)
                .setWhen(when);
        if (SDK_INT >= JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        if (SDK_INT >= M) {
            builder.setCategory(Notification.CATEGORY_REMINDER);
        } else if (SDK_INT >= LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_ALARM);
        }

        return builder;
    }

    private Notification createReminderNotification(ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent, boolean silent) {
        final CharSequence contentTitle = item.title;
        final CharSequence contentText = item.text;
        final long when = item.time;
        final int audioStreamType = settings.getReminderStream();
        final boolean alarm = audioStreamType == AudioManager.STREAM_ALARM;
        final Uri sound = silent ? null : settings.getReminderRingtone();

        final Notification.Builder builder = createNotificationBuilder(contentTitle,
                contentText,
                when,
                contentIntent,
                CHANNEL_REMINDER);
        if (!silent) {
            builder.setDefaults(DEFAULT_VIBRATE);
            builder.setLights(LED_COLOR, LED_ON, LED_OFF);
        }
        builder.setSound(sound, audioStreamType);
        if (SDK_INT >= JELLY_BEAN) {
            if (SDK_INT >= M) {
                builder.setCategory(alarm ? Notification.CATEGORY_ALARM : Notification.CATEGORY_REMINDER);
            }
            return builder.build();
        }
        return builder.getNotification();
    }

    @SuppressLint("Wakelock")
    private void postReminderNotification(ZmanimPreferences settings, Notification notification) {
        // Wake up the device to notify the user.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        wake.acquire(5000L);// enough time to also hear an alarm tone

        NotificationManager nm = getNotificationManager();
        if (SDK_INT >= O) {
            initChannels(nm);
        }
        nm.notify(ID_NOTIFY, notification);

        // This was the last notification.
        final long now = currentTimeMillis();
        settings.setLatestReminder(now);
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param item the item that should be reminded.
     * @param jcal the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private boolean allowReminder(ZmanimPreferences settings, ZmanimItem item, JewishCalendar jcal) {
        return allowReminder(settings, item.titleId, jcal);
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param settings the preferences with reminder day flags.
     * @param itemId   the item that should be reminded.
     * @param jcal     the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private boolean allowReminder(ZmanimPreferences settings, int itemId, JewishCalendar jcal) {
        int dayOfWeek = jcal.getDayOfWeek();
        int holidayIndex = jcal.getYomTovIndex();

        switch (holidayIndex) {
            case PESACH:
            case SHAVUOS:
            case ROSH_HASHANA:
            case YOM_KIPPUR:
            case SUCCOS:
            case SHEMINI_ATZERES:
            case SIMCHAS_TORAH:
                dayOfWeek = SATURDAY;
                break;
        }

        switch (dayOfWeek) {
            case SUNDAY:
                return settings.isReminderSunday(itemId);
            case MONDAY:
                return settings.isReminderMonday(itemId);
            case TUESDAY:
                return settings.isReminderTuesday(itemId);
            case WEDNESDAY:
                return settings.isReminderWednesday(itemId);
            case THURSDAY:
                return settings.isReminderThursday(itemId);
            case FRIDAY:
                return settings.isReminderFriday(itemId);
            case SATURDAY:
                return settings.isReminderSaturday(itemId);
        }

        return true;
    }

    /**
     * Set alarm manager to cancel alert reminders.
     *
     * @param triggerAt when to stop.
     */
    private void cancelFuture(long triggerAt) {
        Log.i(TAG, "cancel future at [" + formatDateTime(triggerAt) + "]");

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createCancelIntent();
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    private Notification createUpcomingNotification(ZmanimPreferences settings, ZmanimItem item, PendingIntent contentIntent) {
        final CharSequence contentTitle = context.getText(item.titleId);
        final CharSequence contentText = item.summary;
        final long when = item.time;
        Log.i(TAG, "notify upcoming [" + contentTitle + "] for [" + formatDateTime(when) + "]");

        final Notification.Builder builder = createNotificationBuilder(contentTitle,
                contentText,
                when,
                contentIntent,
                CHANNEL_UPCOMING);
        builder.setOngoing(true);

        if (SDK_INT < JELLY_BEAN) {
            return builder.getNotification();
        }
        return builder.build();
    }

    private void postUpcomingNotification(ZmanimPreferences settings, Notification notification) {
        NotificationManager nm = getNotificationManager();
        if (SDK_INT >= O) {
            initChannels(nm);
        }
        nm.notify(ID_NOTIFY_UPCOMING, notification);
    }

    /**
     * Notify upcoming time.
     *
     * @param settings the preferences.
     * @param item     the next item.
     */
    public void notifyUpcoming(ZmanimPreferences settings, ZmanimItem item) {
        PendingIntent contentIntent = createActivityIntent();

        Notification notification = createUpcomingNotification(settings, item, contentIntent);
        postUpcomingNotification(settings, notification);

        long triggerAt = item.time + MINUTE_IN_MILLIS;
        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createUpcomingIntent();
        manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
    }

    private PendingIntent createUpcomingIntent() {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_UPDATE);

        return PendingIntent.getBroadcast(context, ID_ALARM_UPCOMING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Silence the notification at some time in the future.
     *
     * @param item      the item to show.
     * @param triggerAt when to silence.
     */
    private void silenceFuture(ZmanimReminderItem item, long triggerAt) {
        Log.i(TAG, "silence future at [" + formatDateTime(triggerAt) + "]");
        if (item == null) {
            cancelFuture(triggerAt);
            return;
        }

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createSilenceIntent(item);
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    /**
     * Create the intent to silence notifications.
     *
     * @return the pending intent.
     */
    private PendingIntent createSilenceIntent(ZmanimReminderItem item) {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_SILENCE);

        intent.putExtra(EXTRA_REMINDER_ID, item.id);
        intent.putExtra(EXTRA_REMINDER_TITLE, item.title);
        intent.putExtra(EXTRA_REMINDER_TEXT, item.text);
        intent.putExtra(EXTRA_REMINDER_TIME, item.time);

        return PendingIntent.getBroadcast(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Replace the current notification with a silent notification.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    private void silence(ZmanimPreferences settings, ZmanimReminderItem item) {
        Log.i(TAG, "silence now [" + item.title + "] for [" + formatDateTime(item.time) + "]");
        PendingIntent contentIntent = createActivityIntent();

        Notification notification = createSilenceNotification(settings, item, contentIntent);
        postSilenceNotification(notification);
    }

    private Notification createSilenceNotification(ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        return createReminderNotification(settings, item, contentIntent, true);
    }

    private void postSilenceNotification(Notification notification) {
        NotificationManager nm = getNotificationManager();
        if (SDK_INT >= O) {
            initChannels(nm);
        }
        nm.cancel(ID_NOTIFY); // Kill the notification so that the sound stops playing.
        nm.notify(ID_NOTIFY, notification);
    }

    /**
     * Get the notification manager.
     *
     * @return the manager.
     */
    protected NotificationManager getNotificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Get the alarm manager.
     *
     * @return the manager.
     */
    protected AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @TargetApi(O)
    private void initChannels(NotificationManager nm) {
        android.app.NotificationChannel channel;

        channel = nm.getNotificationChannel(CHANNEL_REMINDER);
        if (channel == null) {
            channel = new android.app.NotificationChannel(CHANNEL_REMINDER, context.getString(R.string.reminder), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_volume_title));
            channel.enableLights(true);
            channel.setLightColor(LED_COLOR);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            Uri sound = RingtoneManager.getDefaultUri(TYPE_NOTIFICATION);
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build();
            channel.setSound(sound, audioAttributes);

            nm.createNotificationChannel(channel);
        }

        nm.deleteNotificationChannel(CHANNEL_REMINDER_ALARM);

        channel = nm.getNotificationChannel(CHANNEL_UPCOMING);
        if (channel == null) {
            channel = new android.app.NotificationChannel(CHANNEL_UPCOMING, context.getString(R.string.notification_upcoming_title), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notification_upcoming_title));
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);// Silent

            nm.createNotificationChannel(channel);
        }
    }

    /**
     * Get the broadcast receiver that will then start the reminder service.
     *
     * @return the receiver class.
     */
    private Class<? extends BroadcastReceiver> getReceiverClass() {
        return ZmanimReminderReceiver.class;
    }
}

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
package com.github.times.remind;

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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.times.R;
import com.github.times.ZmanimActivity;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimApplication;
import com.github.times.ZmanimItem;
import com.github.times.ZmanimPopulater;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;

import timber.log.Timber;

import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_LOCALE_CHANGED;
import static android.content.Intent.ACTION_MY_PACKAGE_REPLACED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.times.ZmanimHelper.formatDateTime;
import static com.github.times.ZmanimItem.NEVER;
import static java.lang.System.currentTimeMillis;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
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

    /**
     * Id for reminder notifications.<br>
     * Newer notifications will override current notifications.
     */
    private static final int ID_NOTIFY = 1;
    /**
     * Id for alarms.
     */
    private static final int ID_ALARM_REMINDER = 2;
    /**
     * Id for upcoming time notification.<br>
     * Newer notifications will override current notifications.
     */
    private static final int ID_NOTIFY_UPCOMING = 3;
    /**
     * Id for alarms for upcoming time notification.
     */
    private static final int ID_ALARM_UPCOMING = 4;
    /**
     * Id for cancelling alarms.
     */
    private static final int ID_ALARM_CANCEL = 5;
    /**
     * Id for silent alarms.
     */
    private static final int ID_ALARM_SILENT = 6;

    private static final long WAS_DELTA = 30 * SECOND_IN_MILLIS;
    private static final long SOON_DELTA = 30 * SECOND_IN_MILLIS;
    /**
     * The number of days to check forwards for a reminder.
     */
    private static final int DAYS_FORWARD = 30;

    /* Yellow represents the sun or a candle flame. */
    private static final int LED_COLOR = Color.YELLOW;
    private static final int LED_ON = 750;
    private static final int LED_OFF = 500;

    /**
     * Action to remind.
     */
    public static final String ACTION_REMIND = "com.github.times.action.REMIND";
    /**
     * Action to update reminders.
     */
    public static final String ACTION_UPDATE = "com.github.times.action.UPDATE";
    /**
     * Action to cancel reminders.
     */
    public static final String ACTION_CANCEL = "com.github.times.action.CANCEL";
    /**
     * Action to silence reminders.
     */
    public static final String ACTION_SILENCE = "com.github.times.action.SILENCE";

    /**
     * How much time to wait for the notification sound once entered into a day not allowed to disturb.
     */
    private static final long STOP_NOTIFICATION_AFTER = MINUTE_IN_MILLIS * 2;

    private static final String CHANNEL_ALARM = "channel_alarm";
    private static final String CHANNEL_REMINDER = "channel_reminder";
    private static final String CHANNEL_REMINDER_ALARM = "reminder_alarm";
    private static final String CHANNEL_REMINDER_OLD = "reminder";
    private static final String CHANNEL_UPCOMING = "upcoming";

    private static final String WAKE_TAG = "ZmanimReminder:wake";

    private final Context context;
    private Bitmap largeIconSolar;
    private Bitmap largeIconReminder;

    /**
     * Constructs a new reminder worker.
     *
     * @param context The context.
     */
    public ZmanimReminder(@NonNull Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    /**
     * Setup the first reminder for today.
     */
    public void remind() {
        final Context context = getContext();
        ZmanimPreferences settings = new SimpleZmanimPreferences(context);
        remind(settings);
    }

    /**
     * Setup the first reminder for today.
     *
     * @param settings the preferences.
     */
    public void remind(ZmanimPreferences settings) {
        final Context context = getContext();
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

        ZmanimAdapter adapter = new ZmanimAdapter(context, settings);
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
        Timber.i("remind latest [%s]", formatDateTime(latest));

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
                if ((item == null) || item.isEmptyOrElapsed()) {
                    continue;
                }

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
            Timber.i("notify at [%s] for [%s]", formatDateTime(whenFirst), formatDateTime(item.time));
            notifyFuture(item, whenFirst);
        }
        if (itemUpcoming != null) {
            notifyUpcoming(itemUpcoming);
        }
    }

    /**
     * Cancel all reminders.
     */
    public void cancel() {
        Timber.i("cancel");
        final Context context = getContext();
        PendingIntent alarmIntent = createAlarmIntent(context, (ZmanimItem) null);
        PendingIntent upcomingIntent = createUpcomingIntent(context);

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
        final Context context = getContext();
        CharSequence contentTitle = item.title;
        CharSequence contentText = context.getText(R.string.reminder);
        ZmanimReminderItem reminderItem = new ZmanimReminderItem(item.titleId, contentTitle, contentText, item.time);

        notifyNow(settings, reminderItem);
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    @SuppressLint("Wakelock")
    public void notifyNow(ZmanimPreferences settings, ZmanimReminderItem item) {
        Timber.i("notify now [%s] for [%s]", item.title, formatDateTime(item.time));
        final Context context = getContext();
        final long now = currentTimeMillis();

        // Wake up the device to notify the user.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            WakeLock wake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKE_TAG);
            wake.acquire(5000L);// enough time to also hear a tone
        }

        switch (settings.getReminderType()) {
            case RingtoneManager.TYPE_ALARM:
                alarmNow(context, settings, item, now + STOP_NOTIFICATION_AFTER);
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
            default:
                PendingIntent contentIntent = createActivityIntent(context);
                Notification notification = createReminderNotification(context, settings, item, contentIntent);
                showReminderNotification(notification);
                silenceFuture(context, item, now + STOP_NOTIFICATION_AFTER);
                break;
        }

        // This was the last notification.
        settings.setLatestReminder(now);
    }

    /**
     * Set alarm manager to alert us for the next reminder.
     *
     * @param item      the zmanim item to notify about.
     * @param triggerAt the upcoming reminder.
     */
    public void notifyFuture(ZmanimItem item, long triggerAt) {
        final Context context = getContext();
        CharSequence contentTitle = item.title;
        long when = item.time;

        Timber.i("notify future [%s] at [%s] for [%s]", contentTitle, formatDateTime(triggerAt), formatDateTime(when));

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createAlarmIntent(context, item);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent displayIntent = createActivityIntent(context);
            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerAt, displayIntent), alarmIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
        }
    }

    /**
     * Create the intent for when user clicks on the notification to launch the main activity.
     *
     * @return the pending intent.
     */
    private PendingIntent createActivityIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        if (intent == null) {
            Timber.w("Launch activity not found!");
            intent = new Intent(context, ZmanimActivity.class);
        }
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createAlarmIntent(Context context, ZmanimItem item) {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_REMIND);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        putReminderItem(item, intent);
        return PendingIntent.getBroadcast(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createAlarmIntent(Context context, ZmanimReminderItem item) {
        Intent intent = createAlarmActivity(context, item, item.time + STOP_NOTIFICATION_AFTER);
        putReminderItem(item, intent);
        return PendingIntent.getActivity(context, ID_ALARM_REMINDER, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Intent createAlarmServiceIntent(Context context, ZmanimReminderItem item, long silenceWhen) {
        Intent intent = new Intent(context, ZmanimReminderService.class);
        intent.setAction(ACTION_REMIND);
        putReminderItem(item, intent);
        intent.putExtra(ZmanimReminderService.EXTRA_SILENCE_TIME, silenceWhen);
        return intent;
    }

    /**
     * Create the intent to cancel notifications.
     *
     * @return the pending intent.
     */
    private PendingIntent createCancelIntent(Context context) {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_CANCEL);

        return PendingIntent.getBroadcast(context, ID_ALARM_CANCEL, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void process(@Nullable Intent intent) {
        final Context context = getContext();
        Timber.v("process %s [%s]", intent, formatDateTime(currentTimeMillis()));
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
            case ACTION_BOOT_COMPLETED:
            case ACTION_DATE_CHANGED:
            case ACTION_LOCALE_CHANGED:
            case ACTION_TIMEZONE_CHANGED:
            case ACTION_TIME_CHANGED:
            case ACTION_MY_PACKAGE_REPLACED:
            case ACTION_UPDATE:
                update = true;
                break;
            case ACTION_CANCEL:
                cancel();
                break;
            case ACTION_REMIND:
                extras = intent.getExtras();
                if (extras != null) {
                    ZmanimReminderItem reminderItem = ZmanimReminderItem.from(context, extras);
                    if (reminderItem != null) {
                        notifyNow(settings, reminderItem);
                    }
                    update = true;
                }
                break;
            case ACTION_SILENCE:
                extras = intent.getExtras();
                if (extras != null) {
                    ZmanimReminderItem reminderItem = ZmanimReminderItem.from(context, extras);
                    if (reminderItem != null) {
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

    private Notification createReminderNotification(Context context, ZmanimPreferences settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        return createReminderNotification(context, settings, item, contentIntent, false);
    }

    private NotificationCompat.Builder createNotificationBuilder(
        Context context,
        CharSequence contentTitle,
        CharSequence contentText,
        long when,
        PendingIntent contentIntent,
        String channelId
    ) {
        Bitmap largeIconSolar = this.largeIconSolar;
        if (largeIconSolar == null) {
            largeIconSolar = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_solar);
            this.largeIconSolar = largeIconSolar;
        }

        return new NotificationCompat.Builder(context, channelId)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setContentText(contentText)
            .setContentTitle(contentTitle)
            .setLargeIcon(largeIconSolar)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.stat_notify_time)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(when);
    }

    private Notification createReminderNotification(
        Context context,
        ZmanimPreferences settings,
        ZmanimReminderItem item,
        PendingIntent contentIntent,
        boolean silent
    ) {
        final CharSequence contentTitle = item.title;
        final CharSequence contentText = item.text;
        final long when = item.time;
        final int audioStreamType = settings.getReminderStream();
        final boolean alarm = audioStreamType == AudioManager.STREAM_ALARM;
        final Uri sound = silent ? null : settings.getReminderRingtone();
        final String channel = silent ? CHANNEL_ALARM : CHANNEL_REMINDER;

        final NotificationCompat.Builder builder = createNotificationBuilder(
            context,
            contentTitle,
            contentText,
            when,
            contentIntent,
            channel
        )
            .setAutoCancel(true)
            .setCategory(alarm ? NotificationCompat.CATEGORY_ALARM : NotificationCompat.CATEGORY_REMINDER)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(sound, audioStreamType);
        if (!silent) {
            builder.setDefaults(DEFAULT_VIBRATE)
                .setLights(LED_COLOR, LED_ON, LED_OFF);
        }
        if (alarm) {
            builder.setFullScreenIntent(contentIntent, true);
        }

        // Dynamically generate the large icon.
        Bitmap largeIconReminder = this.largeIconReminder;
        if (largeIconReminder == null) {
            final Resources res = context.getResources();
            int largeIconWidth = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int largeIconHeight = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            Rect largeIconRect = new Rect(0, 0, largeIconWidth, largeIconHeight);
            Bitmap largeIcon = Bitmap.createBitmap(largeIconWidth, largeIconHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(largeIcon);
            Drawable layerBottom = ContextCompat.getDrawable(context, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.drawable.ic_alarm_black : R.drawable.ic_alarm_white);
            if (layerBottom != null) {
                layerBottom.setBounds(largeIconRect);
                layerBottom.draw(canvas);
            }
            Bitmap layerTop = largeIconSolar;
            if (layerTop == null) {
                layerTop = BitmapFactory.decodeResource(res, R.mipmap.ic_solar);
                this.largeIconSolar = layerTop;
            }
            canvas.drawBitmap(layerTop, null, largeIconRect, null);
            largeIconReminder = largeIcon;
            this.largeIconReminder = largeIconReminder;
        }
        builder.setLargeIcon(largeIconReminder);

        return builder.build();
    }

    private void showReminderNotification(Notification notification) {
        NotificationManager nm = getNotificationManager();
        initNotifications(nm);
        nm.notify(ID_NOTIFY, notification);
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
            case CHANUKAH:
                if (dayOfWeek == SATURDAY) {
                    return settings.isReminderSunday(itemId);
                }
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
        Timber.i("cancel future at [%s]", formatDateTime(triggerAt));

        final Context context = getContext();
        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createCancelIntent(context);
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    private Notification createUpcomingNotification(Context context, ZmanimItem item, PendingIntent contentIntent) {
        final CharSequence contentTitle = item.title;
        final CharSequence contentText = item.summary;
        final long when = item.time;

        return createNotificationBuilder(context, contentTitle, contentText, when, contentIntent, CHANNEL_UPCOMING)
            .setOngoing(true)
            .build();
    }

    private void showUpcomingNotification(Notification notification) {
        NotificationManager nm = getNotificationManager();
        initNotifications(nm);
        nm.notify(ID_NOTIFY_UPCOMING, notification);
    }

    /**
     * Notify upcoming time.
     *
     * @param item the next item.
     */
    public void notifyUpcoming(ZmanimItem item) {
        CharSequence contentTitle = item.title;
        long when = item.time;
        long triggerAt = item.time;

        Timber.i("notify upcoming [%s] at [%s] for [%s]", contentTitle, formatDateTime(triggerAt), formatDateTime(when));

        final Context context = getContext();
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification = createUpcomingNotification(context, item, contentIntent);
        showUpcomingNotification(notification);

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createUpcomingIntent(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent displayIntent = createActivityIntent(context);
            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerAt, displayIntent), alarmIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
        }
    }

    private PendingIntent createUpcomingIntent(Context context) {
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
    private void silenceFuture(Context context, ZmanimReminderItem item, long triggerAt) {
        Timber.i("silence future at [%s]", formatDateTime(triggerAt));
        if (item == null) {
            cancelFuture(triggerAt);
            return;
        }

        AlarmManager manager = getAlarmManager();
        PendingIntent alarmIntent = createSilenceIntent(context, item);
        manager.set(AlarmManager.RTC, triggerAt, alarmIntent);
    }

    /**
     * Create the intent to silence notifications.
     *
     * @return the pending intent.
     */
    private PendingIntent createSilenceIntent(Context context, ZmanimReminderItem item) {
        Intent intent = new Intent(context, getReceiverClass());
        intent.setAction(ACTION_SILENCE);
        item.put(intent);

        return PendingIntent.getBroadcast(context, ID_ALARM_SILENT, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Replace the current notification with a silent notification.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    private void silence(ZmanimPreferences settings, ZmanimReminderItem item) {
        Timber.i("silence now [%s] for [%s]", item.title, formatDateTime(item.time));
        final Context context = getContext();
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification = createSilenceNotification(context, settings, item, contentIntent);
        postSilenceNotification(notification);
    }

    private Notification createSilenceNotification(
        Context context,
        ZmanimPreferences settings,
        ZmanimReminderItem item,
        PendingIntent contentIntent
    ) {
        return createReminderNotification(context, settings, item, contentIntent, true);
    }

    private void postSilenceNotification(Notification notification) {
        NotificationManager nm = getNotificationManager();
        initNotifications(nm);
        nm.cancel(ID_NOTIFY); // Kill the notification so that the sound stops playing.
        nm.notify(ID_NOTIFY, notification);
    }

    /**
     * Get the notification manager.
     *
     * @return the manager.
     */
    protected NotificationManager getNotificationManager() {
        final Context context = getContext();
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Get the alarm manager.
     *
     * @return the manager.
     */
    protected AlarmManager getAlarmManager() {
        final Context context = getContext();
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initChannels(NotificationManager nm) {
        final Context context = getContext();
        android.app.NotificationChannel channel;
        String channelName;

        nm.deleteNotificationChannel(CHANNEL_REMINDER_ALARM);
        nm.deleteNotificationChannel(CHANNEL_REMINDER_OLD);

        channelName = CHANNEL_ALARM;
        channel = nm.getNotificationChannel(channelName);
        if (channel == null) {
            channel = new android.app.NotificationChannel(channelName, context.getString(R.string.reminder), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_volume_title));
            channel.enableLights(true);
            channel.setLightColor(LED_COLOR);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null);// Silent

            nm.createNotificationChannel(channel);
        }

        channelName = CHANNEL_REMINDER;
        channel = nm.getNotificationChannel(channelName);
        if (channel == null) {
            channel = new android.app.NotificationChannel(channelName, context.getString(R.string.reminder), NotificationManager.IMPORTANCE_HIGH);
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

        channelName = CHANNEL_UPCOMING;
        channel = nm.getNotificationChannel(channelName);
        if (channel == null) {
            channel = new android.app.NotificationChannel(channelName, context.getString(R.string.notification_upcoming_title), NotificationManager.IMPORTANCE_DEFAULT);
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

    /**
     * Alarm screen now.
     *
     * @param item        the reminder item.
     * @param silenceWhen when to silence the alarm, in milliseconds.
     */
    public void alarmNow(Context context, ZmanimPreferences settings, ZmanimReminderItem item, long silenceWhen) {
        Timber.i("alarm now [%s] for [%s]", item.title, formatDateTime(item.time));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startAlarmService(context, item, silenceWhen);
            silenceFuture(context, item, silenceWhen);
        } else {
            startAlarmActivity(context, item, silenceWhen);
        }
    }

    private Intent createAlarmActivity(Context context, ZmanimReminderItem item, long silenceWhen) {
        Intent intent = new Intent(context, AlarmActivity.class);
        putReminderItem(item, intent);
        intent.putExtra(AlarmActivity.EXTRA_SILENCE_TIME, silenceWhen);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION);
        return intent;
    }

    private void putReminderItem(@Nullable ZmanimItem item, Intent intent) {
        if (item != null) {
            final Context context = getContext();
            ZmanimReminderItem reminderItem = ZmanimReminderItem.from(context, item);
            putReminderItem(reminderItem, intent);
        }
    }

    private void putReminderItem(@Nullable ZmanimReminderItem reminderItem, Intent intent) {
        if (reminderItem != null) {
            reminderItem.put(intent);
        }
    }

    private void startAlarmActivity(Context context, ZmanimReminderItem item, long silenceWhen) {
        Intent intent = createAlarmActivity(context, item, silenceWhen);
        context.startActivity(intent);

    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void startAlarmService(Context context, ZmanimReminderItem item, long silenceWhen) {
        Intent intent = createAlarmServiceIntent(context, item, silenceWhen);
        context.startForegroundService(intent);
    }

    public Notification createAlarmServiceNotification(Context context, ZmanimPreferences settings, ZmanimReminderItem item) {
        PendingIntent contentIntent = createAlarmIntent(context, item);
        return createReminderNotification(context, settings, item, contentIntent, true);
    }

    private void initNotifications(NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannels(nm);
        }
    }

    public void initNotifications() {
        NotificationManager nm = getNotificationManager();
        initNotifications(nm);
    }
}

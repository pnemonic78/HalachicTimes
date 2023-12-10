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
package com.github.times.remind

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.github.times.BuildConfig
import com.github.times.CandleData
import com.github.times.R
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimActivity
import com.github.times.ZmanimAdapter
import com.github.times.ZmanimApplication
import com.github.times.ZmanimDays.SHABBATH
import com.github.times.ZmanimHelper.formatDateTime
import com.github.times.ZmanimItem
import com.github.times.ZmanimPopulater
import com.github.times.isNullOrEmptyOrElapsed
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.times.remind.ZmanimReminderItem.Companion.from
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.util.Calendar
import timber.log.Timber

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
class ZmanimReminder(private val context: Context) {

    private var largeIconSolar: Bitmap? = null
    private var largeIconReminder: Bitmap? = null

    private val notificationManager: NotificationManager?
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Setup the first reminder for today.
     */
    fun remind() {
        val settings = createPreferences()
        remind(settings)
    }

    /**
     * Setup the first reminder for today.
     *
     * @param settings the preferences.
     */
    fun remind(settings: ZmanimPreferences) {
        val app = context.applicationContext as ZmanimApplication
        val locations = app.locations
        // Have we been destroyed?
        val gloc = locations.geoLocation ?: return

        val populater = ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, settings)
        populater.setCalendar(System.currentTimeMillis())
        populater.setGeoLocation(gloc)
        populater.isInIsrael = locations.isInIsrael
        val adapter = ZmanimAdapter<ZmanViewHolder>(context, settings, null)
        remind(settings, populater, adapter)
    }

    /**
     * Setup the first reminder for the week.
     *
     * @param settings the preferences.
     * @param populater  the populater.
     * @param adapter  the populated adapter.
     */
    private fun remind(
        settings: ZmanimPreferences,
        populater: ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>,
        adapter: ZmanimAdapter<ZmanViewHolder>
    ) {
        val latest = settings.latestReminder
        Timber.i("remind latest [%s]", formatDateTime(latest))
        val gcal = Calendar.getInstance()
        val now = gcal.timeInMillis
        val was = now - WAS_DELTA
        val soon = now + SOON_DELTA
        var item: ZmanimItem?
        var itemFirst: ZmanimItem? = null
        var whenRemind: Long
        var whenFirst = Long.MAX_VALUE
        var nextDay = true
        var count: Int
        val upcomingNotification = settings.isUpcomingNotification
        var itemUpcoming: ZmanimItem? = null
        var whenUpcoming = Long.MAX_VALUE
        val jcal = JewishCalendar(gcal).apply {
            inIsrael = populater.isInIsrael
        }
        val cal = populater.calendar.calendar
        var candles: CandleData

        // Find the first reminder in the upcoming week.
        var day = 1
        while (nextDay && day <= DAYS_FORWARD) {
            if (day > 1) {
                gcal.add(Calendar.DAY_OF_MONTH, 1)
                jcal.setDate(gcal)
                cal.add(Calendar.DAY_OF_MONTH, 1)
                populater.setCalendar(cal)
            }
            populater.populate(adapter, false)
            candles = adapter.candles
            count = adapter.itemCount
            for (i in 0 until count) {
                item = adapter.getItem(i)
                if (item.isNullOrEmptyOrElapsed()) continue

                // Is the zman to be reminded?
                whenRemind = settings.getReminder(item.titleId, item.time)
                if (whenRemind != ZmanimItem.NEVER && allowReminder(
                        settings,
                        item,
                        jcal,
                        candles
                    )
                ) {
                    if (nextDay && latest < whenRemind && was <= whenRemind && whenRemind <= soon) {
                        notifyNow(settings, item)
                        nextDay = false
                    }
                    if (now < whenRemind && whenRemind < whenFirst) {
                        itemFirst = item
                        whenFirst = whenRemind
                    }
                }

                // Is the zman to be notified?
                if (upcomingNotification) {
                    whenRemind = item.time
                    if (whenRemind != ZmanimItem.NEVER && now <= whenRemind && whenRemind < whenUpcoming) {
                        itemUpcoming = item
                        whenUpcoming = whenRemind
                    }
                }
            }
            day++
        }
        if (itemFirst != null) {
            item = itemFirst
            Timber.i(
                "notify at [%s] for [%s]",
                formatDateTime(whenFirst),
                formatDateTime(item.time)
            )
            notifyFuture(item, whenFirst)
        }
        itemUpcoming?.let { notifyUpcoming(it) }
    }

    /**
     * Cancel all reminders.
     */
    fun cancel() {
        Timber.i("cancel")
        cancelAlarm()
        cancelNotification()
        cancelUpcoming()
    }

    private fun cancelAlarm() {
        val service = createAlarmServiceIntent(null, ZmanimItem.NEVER)
        context.stopService(service)
    }

    private fun cancelNotification() {
        Timber.i("cancelNotification")
        alarmManager?.let { alarms ->
            val alarmIntent = createAlarmIntent(null as ZmanimItem?)
            alarms.cancel(alarmIntent)
        }
        notificationManager?.let { nm ->
            nm.cancel(ID_NOTIFY)
        }
    }

    private fun cancelUpcoming() {
        Timber.i("cancelUpcoming")
        alarmManager?.let { alarms ->
            val upcomingIntent = createUpcomingIntent()
            alarms.cancel(upcomingIntent)
        }
        notificationManager?.let { nm ->
            nm.cancel(ID_NOTIFY_UPCOMING)
        }
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the zmanim item to notify about.
     */
    fun notifyNow(settings: ZmanimPreferences, item: ZmanimItem) {
        val contentTitle = item.title
        val contentText = context.getText(R.string.reminder)
        val reminderItem = ZmanimReminderItem(item.titleId, contentTitle, contentText, item.time)
        notifyNow(settings, reminderItem)
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    @SuppressLint("Wakelock")
    fun notifyNow(settings: ZmanimPreferences, item: ZmanimReminderItem) {
        Timber.i("notify now [%s] for [%s]", item.title, formatDateTime(item.time))
        val now = System.currentTimeMillis()

        // Wake up the device to notify the user.
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (pm != null) {
            val wake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKE_TAG)
            wake.acquire(5000L) // enough time to also hear a tone
        }
        when (settings.reminderType) {
            RingtoneManager.TYPE_ALARM -> alarmNow(item, now + STOP_NOTIFICATION_AFTER)
            RingtoneManager.TYPE_NOTIFICATION -> {
                val contentIntent = createActivityIntent()
                val notification =
                    createReminderNotification(settings, item, contentIntent)
                showReminderNotification(notification)
                silenceFuture(item, now + STOP_NOTIFICATION_AFTER)
            }

            else -> {
                val contentIntent = createActivityIntent()
                val notification =
                    createReminderNotification(settings, item, contentIntent)
                showReminderNotification(notification)
                silenceFuture(item, now + STOP_NOTIFICATION_AFTER)
            }
        }

        // This was the last notification.
        settings.latestReminder = now
    }

    /**
     * Set alarm manager to alert us for the next reminder.
     *
     * @param item      the zmanim item to notify about.
     * @param triggerAt the upcoming reminder.
     */
    fun notifyFuture(item: ZmanimItem, triggerAt: Long) {
        val alarms = alarmManager ?: return
        val contentTitle = item.title
        Timber.i(
            "notify future [%s] at [%s] for [%s]",
            contentTitle,
            formatDateTime(triggerAt),
            formatDateTime(item.time)
        )
        val alarmIntent = createAlarmIntent(item)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarms.canScheduleExactAlarms()) {
                AlarmManagerCompat.setAndAllowWhileIdle(
                    alarms,
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    alarmIntent
                )
                return
            }
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarms,
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            alarmIntent
        )
    }

    /**
     * Create the intent for when user clicks on the notification to launch the main activity.
     *
     * @return the pending intent.
     */
    private fun createActivityIntent(): PendingIntent {
        val pm = context.packageManager
        var intent = pm.getLaunchIntentForPackage(context.packageName)
        if (intent == null) {
            Timber.w("Launch activity not found!")
            intent = Intent(context, ZmanimActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, ID_NOTIFY, intent, FLAGS_UPDATE)
    }

    private fun createAlarmIntent(item: ZmanimItem?): PendingIntent {
        if (isAlarmService) {
            val reminderItem = from(item)
            val now = System.currentTimeMillis()
            val intent =
                createAlarmServiceIntent(reminderItem, now + STOP_NOTIFICATION_AFTER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return PendingIntent.getForegroundService(
                    context,
                    ID_ALARM_REMINDER,
                    intent,
                    FLAGS_UPDATE
                )
            }
        }
        val intent = Intent(context, receiverClass)
            .setAction(ACTION_REMIND)
        putReminderItem(item, intent)
        return PendingIntent.getBroadcast(context, ID_ALARM_REMINDER, intent, FLAGS_UPDATE)
    }

    private fun createAlarmIntent(item: ZmanimReminderItem): PendingIntent {
        val intent = createAlarmActivity(item, item.time + STOP_NOTIFICATION_AFTER)
        putReminderItem(item, intent)
        return PendingIntent.getActivity(
            context,
            ID_ALARM_REMINDER,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmServiceIntent(item: ZmanimReminderItem?, silenceWhen: Long): Intent {
        val intent = Intent(context, ZmanimReminderService::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(ZmanimReminderService.EXTRA_SILENCE_TIME, silenceWhen)
        putReminderItem(item, intent)
        return intent
    }

    /**
     * Create the intent to cancel notifications.
     *
     * @return the pending intent.
     */
    private fun createCancelIntent(): PendingIntent {
        val intent = Intent(context, receiverClass)
            .setAction(ACTION_CANCEL)
        return PendingIntent.getBroadcast(
            context,
            ID_ALARM_CANCEL,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Create the intent to dismiss alarm.
     *
     * @return the pending intent.
     */
    private fun createDismissIntent(): PendingIntent {
        val intent = Intent(context, ZmanimReminderService::class.java)
            .setAction(ACTION_DISMISS)
        return PendingIntent.getService(context, ID_ALARM_DISMISS, intent, FLAGS_UPDATE)
    }

    fun process(intent: Intent?) {
        Timber.i("process %s [%s]", intent, formatDateTime(System.currentTimeMillis()))
        if (intent == null) {
            return
        }
        val action = intent.action ?: return
        var update = false
        val extras: Bundle?
        when (action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_DATE_CHANGED, Intent.ACTION_LOCALE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED, ACTION_UPDATE -> update =
                true

            ACTION_CANCEL -> cancel()
            ACTION_REMIND -> {
                extras = intent.extras
                if (extras != null) {
                    val reminderItem = from(context, extras)
                    if (reminderItem != null) {
                        val settings = createPreferences()
                        notifyNow(settings, reminderItem)
                    }
                    update = true
                }
            }

            ACTION_SILENCE -> {
                extras = intent.extras
                if (extras != null) {
                    val reminderItem = from(context, extras)
                    if (reminderItem != null) {
                        val settings = createPreferences()
                        silence(settings, reminderItem)
                    } else {
                        cancelNotification()
                    }
                    update = true
                } else {
                    cancelNotification()
                }
            }
        }
        if (update) {
            remind()
        }
    }

    private fun createNotificationBuilder(
        contentTitle: CharSequence?,
        contentText: CharSequence?,
        `when`: Long,
        contentIntent: PendingIntent,
        channelId: String
    ): NotificationCompat.Builder {
        var largeIconSolar = largeIconSolar
        if (largeIconSolar == null) {
            largeIconSolar = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_solar)
            this.largeIconSolar = largeIconSolar
        }
        return NotificationCompat.Builder(context, channelId)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setContentText(contentText)
            .setContentTitle(contentTitle)
            .setLargeIcon(largeIconSolar)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.stat_notify_time)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(`when`)
    }

    private fun createReminderNotification(
        settings: ZmanimPreferences,
        item: ZmanimReminderItem,
        contentIntent: PendingIntent,
        silent: Boolean = false
    ): Notification {
        val res = context.resources
        val audioStreamType = settings.reminderStream
        val isAlarm = audioStreamType == AudioManager.STREAM_ALARM
        val sound = if (silent || isAlarmService) null else settings.reminderRingtone
        val channel = if (isAlarm) CHANNEL_ALARM else CHANNEL_REMINDER
        val builder = createNotificationBuilder(
            item.title,
            item.text,
            item.time,
            contentIntent,
            channel
        )
            .setAutoCancel(true)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(sound, audioStreamType)
        if (!silent) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(LED_COLOR, LED_ON, LED_OFF)
            if (isAlarm) {
                val dismissAction = NotificationCompat.Action(
                    R.drawable.ic_dismiss,
                    res.getText(R.string.dismiss),
                    createDismissIntent()
                )
                builder.setFullScreenIntent(contentIntent, true)
                    .addAction(dismissAction)
            }
        }

        // Dynamically generate the large icon.
        var largeIconReminder = largeIconReminder
        if (largeIconReminder == null) {
            val largeIconWidth =
                res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
            val largeIconHeight =
                res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
            val largeIconRect = Rect(0, 0, largeIconWidth, largeIconHeight)
            val largeIcon =
                Bitmap.createBitmap(largeIconWidth, largeIconHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(largeIcon)
            val layerBottom = ContextCompat.getDrawable(
                context,
                com.github.times.common.R.drawable.ic_alarm_black
            )
            if (layerBottom != null) {
                layerBottom.bounds = largeIconRect
                layerBottom.draw(canvas)
            }
            var layerTop = largeIconSolar
            if (layerTop == null) {
                layerTop = BitmapFactory.decodeResource(res, R.mipmap.ic_solar)
                largeIconSolar = layerTop
            }
            if (layerTop != null) {
                canvas.drawBitmap(layerTop, null, largeIconRect, null)
            }
            largeIconReminder = largeIcon
            this.largeIconReminder = largeIconReminder
        }
        builder.setLargeIcon(largeIconReminder)
        return builder.build()
    }

    private fun showReminderNotification(notification: Notification) {
        val nm = notificationManager ?: return
        initNotifications(nm)
        nm.notify(ID_NOTIFY, notification)
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param item the item that should be reminded.
     * @param jcal the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private fun allowReminder(
        settings: ZmanimPreferences,
        item: ZmanimItem,
        jcal: JewishCalendar,
        candles: CandleData
    ): Boolean {
        return allowReminder(settings, item.titleId, jcal, candles)
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param settings the preferences with reminder day flags.
     * @param itemId   the item that should be reminded.
     * @param jcal     the Jewish calendar as of now.
     * @return can the reminder be activated?
     */
    private fun allowReminder(
        settings: ZmanimPreferences,
        itemId: Int,
        jcal: JewishCalendar,
        candles: CandleData
    ): Boolean {
        when (candles.holidayToday) {
            JewishCalendar.PESACH,
            JewishCalendar.SHAVUOS,
            JewishCalendar.ROSH_HASHANA,
            JewishCalendar.YOM_KIPPUR,
            JewishCalendar.SUCCOS,
            JewishCalendar.SHEMINI_ATZERES,
            JewishCalendar.SIMCHAS_TORAH,
            SHABBATH -> return settings.isReminderSaturday(itemId)
        }
        when (jcal.dayOfWeek) {
            Calendar.SUNDAY -> return settings.isReminderSunday(itemId)
            Calendar.MONDAY -> return settings.isReminderMonday(itemId)
            Calendar.TUESDAY -> return settings.isReminderTuesday(itemId)
            Calendar.WEDNESDAY -> return settings.isReminderWednesday(itemId)
            Calendar.THURSDAY -> return settings.isReminderThursday(itemId)
            Calendar.FRIDAY -> return settings.isReminderFriday(itemId)
            Calendar.SATURDAY -> return settings.isReminderSaturday(itemId)
        }
        return true
    }

    /**
     * Set alarm manager to cancel alert reminders.
     *
     * @param triggerAt when to stop.
     */
    private fun cancelFuture(triggerAt: Long) {
        val alarms = alarmManager ?: return
        Timber.i("cancel future at [%s]", formatDateTime(triggerAt))
        val alarmIntent = createCancelIntent()
        alarms[AlarmManager.RTC, triggerAt] = alarmIntent
    }

    private fun createUpcomingNotification(
        item: ZmanimItem,
        contentIntent: PendingIntent
    ): Notification {
        return createNotificationBuilder(
            item.title,
            item.summary,
            item.time,
            contentIntent,
            CHANNEL_UPCOMING
        )
            .setOngoing(true)
            .build()
    }

    private fun showUpcomingNotification(notification: Notification) {
        val nm = notificationManager ?: return
        initNotifications(nm)
        nm.notify(ID_NOTIFY_UPCOMING, notification)
    }

    /**
     * Notify upcoming time.
     *
     * @param item the next item.
     */
    private fun notifyUpcoming(item: ZmanimItem) {
        val alarms = alarmManager ?: return
        val triggerAt = item.time
        Timber.i(
            "notify upcoming [%s] at [%s] for [%s]",
            item.title,
            formatDateTime(triggerAt),
            formatDateTime(item.time)
        )
        val contentIntent = createActivityIntent()
        val notification = createUpcomingNotification(item, contentIntent)
        showUpcomingNotification(notification)
        val alarmIntent = createUpcomingIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarms.canScheduleExactAlarms()) {
                AlarmManagerCompat.setAndAllowWhileIdle(
                    alarms,
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    alarmIntent
                )
                return
            }
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarms,
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            alarmIntent
        )
    }

    private fun createUpcomingIntent(): PendingIntent {
        val intent = Intent(context, receiverClass)
            .setAction(ACTION_UPDATE)
        return PendingIntent.getBroadcast(context, ID_ALARM_UPCOMING, intent, FLAGS_UPDATE)
    }

    /**
     * Silence the notification at some time in the future.
     *
     * @param item      the item to show.
     * @param triggerAt when to silence.
     */
    private fun silenceFuture(item: ZmanimReminderItem?, triggerAt: Long) {
        Timber.i("silence future at [%s]", formatDateTime(triggerAt))
        if (item == null) {
            cancelFuture(triggerAt)
            return
        }
        val alarms = alarmManager ?: return
        val alarmIntent = createSilenceIntent(item)
        alarms[AlarmManager.RTC, triggerAt] = alarmIntent
    }

    /**
     * Create the intent to silence notifications.
     *
     * @return the pending intent.
     */
    private fun createSilenceIntent(item: ZmanimReminderItem): PendingIntent {
        val intent = Intent(context, receiverClass)
            .setAction(ACTION_SILENCE)
        item.put(intent)
        return PendingIntent.getBroadcast(
            context,
            ID_ALARM_SILENT,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Replace the current notification with a silent notification.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    private fun silence(settings: ZmanimPreferences, item: ZmanimReminderItem) {
        Timber.i("silence now [%s] for [%s]", item.title, formatDateTime(item.time))
        val contentIntent = createActivityIntent()
        val notification = createSilenceNotification(settings, item, contentIntent)
        postSilenceNotification(notification)
    }

    private fun createSilenceNotification(
        settings: ZmanimPreferences,
        item: ZmanimReminderItem,
        contentIntent: PendingIntent
    ): Notification {
        return createReminderNotification(settings, item, contentIntent, true)
    }

    private fun postSilenceNotification(notification: Notification) {
        val nm = notificationManager ?: return
        initNotifications(nm)
        // Kill the notification so that its sound stops playing.
        nm.cancel(ID_NOTIFY)
        nm.notify(ID_NOTIFY, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannels(nm: NotificationManager) {
        initChannelAlarm(nm)
        initChannelReminder(nm)
        initChannelUpcoming(nm)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannelAlarm(nm: NotificationManager) {
        nm.deleteNotificationChannel(CHANNEL_ALARM_OLD)
        val channelName = CHANNEL_ALARM
        var channel = nm.getNotificationChannel(channelName)
        if (channel == null) {
            channel = NotificationChannel(
                channelName,
                context.getString(R.string.reminder),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.notification_volume_title)
            channel.enableLights(true)
            channel.lightColor = LED_COLOR
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setSound(null, null) // Silent
            nm.createNotificationChannel(channel)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannelReminder(nm: NotificationManager) {
        nm.deleteNotificationChannel(CHANNEL_REMINDER_OLD)
        val channelName = CHANNEL_REMINDER
        var channel = nm.getNotificationChannel(channelName)
        if (channel == null) {
            channel = NotificationChannel(
                channelName,
                context.getString(R.string.reminder),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.notification_volume_title)
            channel.enableLights(true)
            channel.lightColor = LED_COLOR
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()
            channel.setSound(sound, audioAttributes)
            nm.createNotificationChannel(channel)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannelUpcoming(nm: NotificationManager) {
        val channelName = CHANNEL_UPCOMING
        var channel = nm.getNotificationChannel(channelName)
        if (channel == null) {
            channel = NotificationChannel(
                channelName,
                context.getString(R.string.notification_upcoming_title),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = context.getString(R.string.notification_upcoming_title)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setSound(null, null) // Silent
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * The broadcast receiver that will then start the reminder service.
     */
    private val receiverClass: Class<out BroadcastReceiver>
        get() = ZmanimReminderReceiver::class.java

    /**
     * Alarm screen now.
     *
     * @param item        the reminder item.
     * @param silenceWhen when to silence the alarm, in milliseconds.
     */
    fun alarmNow(item: ZmanimReminderItem, silenceWhen: Long) {
        Timber.i("alarm now [%s] for [%s]", item.title, formatDateTime(item.time))
        if (isAlarmService) {
            startAlarmService(item, silenceWhen)
        } else {
            startAlarmActivity(item, silenceWhen)
        }
    }

    private val isAlarmService: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun createAlarmActivity(
        item: ZmanimReminderItem,
        silenceWhen: Long
    ): Intent {
        val intent = Intent(context, AlarmActivity::class.java)
            .putExtra(AlarmActivity.EXTRA_SILENCE_TIME, silenceWhen)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        putReminderItem(item, intent)
        return intent
    }

    private fun putReminderItem(item: ZmanimItem?, intent: Intent) {
        if (item != null) {
            val reminderItem = from(item)
            putReminderItem(reminderItem, intent)
        }
    }

    private fun putReminderItem(reminderItem: ZmanimReminderItem?, intent: Intent) {
        reminderItem?.put(intent)
    }

    private fun startAlarmActivity(item: ZmanimReminderItem, silenceWhen: Long) {
        val intent = createAlarmActivity(item, silenceWhen)
        context.startActivity(intent)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun startAlarmService(item: ZmanimReminderItem, silenceWhen: Long) {
        val intent = createAlarmServiceIntent(item, silenceWhen)
        context.startForegroundService(intent)
    }

    fun createAlarmServiceNotification(
        settings: ZmanimPreferences,
        item: ZmanimReminderItem
    ): Notification {
        val contentIntent = createAlarmIntent(item)
        return createReminderNotification(settings, item, contentIntent, false)
    }

    private fun initNotifications(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannels(nm)
        }
    }

    fun initNotifications() {
        val nm = notificationManager ?: return
        initNotifications(nm)
    }

    private fun createPreferences(): ZmanimPreferences {
        return SimpleZmanimPreferences(context)
    }

    companion object {
        /**
         * Id for reminder notifications.<br></br>
         * Newer notifications will override current notifications.
         */
        private const val ID_NOTIFY = 1

        /**
         * Id for alarms.
         */
        private const val ID_ALARM_REMINDER = 2

        /**
         * Id for upcoming time notification.<br></br>
         * Newer notifications will override current notifications.
         */
        private const val ID_NOTIFY_UPCOMING = 3

        /**
         * Id for alarms for upcoming time notification.
         */
        private const val ID_ALARM_UPCOMING = 4

        /**
         * Id for cancelling alarms.
         */
        private const val ID_ALARM_CANCEL = 5

        /**
         * Id for silent alarms.
         */
        private const val ID_ALARM_SILENT = 6

        /**
         * Id for dismissing alarms.
         */
        private const val ID_ALARM_DISMISS = 7

        private const val WAS_DELTA = DateUtils.SECOND_IN_MILLIS
        private const val SOON_DELTA = 10 * DateUtils.SECOND_IN_MILLIS

        /**
         * The number of days to check forwards for a reminder.
         */
        private const val DAYS_FORWARD = 30

        /* Yellow represents the sun or a candle flame. */
        private const val LED_COLOR = Color.YELLOW
        private const val LED_ON = 750
        private const val LED_OFF = 500

        /**
         * Action to remind.
         */
        const val ACTION_REMIND = BuildConfig.APPLICATION_ID + ".action.REMIND"

        /**
         * Action to update reminders.
         */
        const val ACTION_UPDATE = BuildConfig.APPLICATION_ID + ".action.UPDATE"

        /**
         * Action to cancel reminders.
         */
        const val ACTION_CANCEL = BuildConfig.APPLICATION_ID + ".action.CANCEL"

        /**
         * Action to silence reminders.
         */
        const val ACTION_SILENCE = BuildConfig.APPLICATION_ID + ".action.SILENCE"

        /**
         * Action to dismiss alarms.
         */
        const val ACTION_DISMISS = BuildConfig.APPLICATION_ID + ".action.DISMISS"

        /**
         * How much time to wait for the notification sound once entered into a day not allowed to disturb.
         */
        // TODO put as user settings.
        private const val STOP_NOTIFICATION_AFTER = DateUtils.MINUTE_IN_MILLIS

        private const val CHANNEL_ALARM = "channel_alarm"
        private const val CHANNEL_REMINDER = "channel_reminder"
        private const val CHANNEL_ALARM_OLD = "reminder_alarm"
        private const val CHANNEL_REMINDER_OLD = "reminder"
        private const val CHANNEL_UPCOMING = "upcoming"

        private const val WAKE_TAG = "ZmanimReminder:wake"

        @TargetApi(Build.VERSION_CODES.TIRAMISU)
        @JvmField
        val PERMISSION_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

        /**
         * Activity id for requesting notification permissions.
         */
        private const val ACTIVITY_PERMISSIONS = 0x6057 // "POST"

        private const val FLAGS_UPDATE =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        @RequiresApi(api = Build.VERSION_CODES.M)
        @JvmStatic
        fun checkPermissions(context: Context, permissions: MutableCollection<String>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val nm =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!nm.areNotificationsEnabled() || PermissionChecker.checkCallingOrSelfPermission(
                        context,
                        PERMISSION_NOTIFICATIONS
                    ) != PermissionChecker.PERMISSION_GRANTED
                ) {
                    permissions.add(PERMISSION_NOTIFICATIONS)
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @JvmStatic
        fun checkPermissions(activity: Activity) {
            val context: Context = activity
            val permissions = mutableSetOf<String>()
            checkPermissions(context, permissions)
            if (permissions.isNotEmpty()) {
                activity.requestPermissions(permissions.toTypedArray(), ACTIVITY_PERMISSIONS)
            }

            // Also check for exact alarm.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarms.canScheduleExactAlarms()) {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + context.packageName)
                    )
                    context.startActivity(intent)
                }
            }
        }
    }
}
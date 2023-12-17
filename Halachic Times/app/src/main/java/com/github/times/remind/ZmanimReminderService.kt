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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.text.format.DateUtils
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.github.times.BuildConfig
import com.github.times.ZmanimHelper.formatDateTime
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.times.remind.ZmanimReminder.Companion.ACTION_CANCEL
import com.github.times.remind.ZmanimReminder.Companion.ACTION_DISMISS
import com.github.times.remind.ZmanimReminder.Companion.ACTION_REMIND
import timber.log.Timber

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
class ZmanimReminderService : Service() {

    private lateinit var settings: ZmanimPreferences
    private lateinit var klaxon: AlarmKlaxon
    private var silenceRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        settings = SimpleZmanimPreferences(context)
        klaxon = AlarmKlaxon(context)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand %s", intent)
        if (intent == null) {
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_REMIND -> handleRemind(intent)
            ACTION_DISMISS -> handleDismiss()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopAlarm()
        val silenceRunnable = this.silenceRunnable
        if (silenceRunnable != null) {
            handler.removeCallbacks(silenceRunnable)
        }
        super.onDestroy()
    }

    private fun startAlarm() {
        klaxon.start()
    }

    private fun stopAlarm() {
        klaxon.stop()
    }

    private fun showNotification(context: Context, item: ZmanimReminderItem, silenceAt: Long) {
        val settings = this.settings
        val reminder = ZmanimReminder(context)
        reminder.initNotifications()
        val notification = reminder.createAlarmServiceNotification(settings, item, silenceAt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                ID_NOTIFY,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(ID_NOTIFY, notification)
        }
    }

    /**
     * Set timer to silence the alert.
     *
     * @param triggerAt when to silence.
     */
    private fun silenceFuture(triggerAt: Long) {
        Timber.i("silence future at [%s]", formatDateTime(triggerAt))
        var silenceRunnable: Runnable? = this.silenceRunnable
        if (silenceRunnable == null) {
            silenceRunnable = Runnable { stopSelf() }
            this.silenceRunnable = silenceRunnable
        }
        val now = System.currentTimeMillis()
        val delayMillis = triggerAt - now
        handler.postDelayed(silenceRunnable, delayMillis)
    }

    companion object {
        /**
         * Extras name to silence to alarm.
         */
        const val EXTRA_SILENCE_TIME = BuildConfig.APPLICATION_ID + ".SILENCE_TIME"

        /**
         * How much time to wait for the notification sound once entered into a day not allowed to disturb.
         */
        private const val STOP_NOTIFICATION_AFTER = ZmanimReminder.STOP_NOTIFICATION_AFTER

        private const val ID_NOTIFY = 0x1111
        private const val BUSY_TIMEOUT = 10 * DateUtils.SECOND_IN_MILLIS

        private var reminderBusy: String? = ""
        private var reminderBusyTime: Long = 0

        fun enqueueWork(context: Context, intent: Intent) {
            val action = intent.action
            if (action.isNullOrEmpty()) return

            // Handler high priority actions immediately.
            if (ACTION_CANCEL == action) {
                WorkManager.getInstance(context).cancelAllWork()
                processReminder(context, intent)
                return
            }
            if (ACTION_REMIND == action) {
                processReminder(context, intent)
                return
            }

            val requestData = ZmanimReminderWorker.toWorkData(intent)
            val workRequest: WorkRequest =
                OneTimeWorkRequest.Builder(ZmanimReminderWorker::class.java)
                    .setInputData(requestData)
                    .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        private fun processReminder(context: Context, intent: Intent) {
            val action = intent.action
            val now = SystemClock.elapsedRealtime()
            if (reminderBusy == action && now - reminderBusyTime < BUSY_TIMEOUT) {
                return
            }
            reminderBusy = action
            reminderBusyTime = now

            val reminder = ZmanimReminder(context)
            reminder.process(intent)
        }
    }

    private fun handleRemind(intent: Intent) {
        val context: Context = this
        val item = ZmanimReminderItem.from(context, intent)
        if (item == null) {
            Timber.w("no item to remind!")
            stopSelf()
            return
        }
        var silenceAt = item.time + STOP_NOTIFICATION_AFTER
        val extras = intent.extras
        if ((extras != null) && extras.containsKey(EXTRA_SILENCE_TIME)) {
            silenceAt = extras.getLong(EXTRA_SILENCE_TIME, silenceAt)
        }
        showNotification(context, item, silenceAt)
        startAlarm()
        silenceFuture(silenceAt)
    }

    private fun handleDismiss() {
        val now = System.currentTimeMillis()
        silenceFuture(now)
    }
}
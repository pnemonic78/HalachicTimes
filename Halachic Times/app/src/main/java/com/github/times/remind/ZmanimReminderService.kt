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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.github.times.BuildConfig
import com.github.times.ZmanimHelper
import com.github.times.ZmanimItem
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.times.remind.ZmanimReminder.ACTION_DISMISS
import com.github.times.remind.ZmanimReminder.ACTION_REMIND
import timber.log.Timber
import java.util.Date

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

    private fun showNotification(item: ZmanimReminderItem) {
        val context: Context = this
        val settings = SimpleZmanimPreferences(context)
        val reminder = ZmanimReminder(context)
        reminder.initNotifications()
        val notification = reminder.createAlarmServiceNotification(context, settings, item)
        startForeground(ID_NOTIFY, notification)
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
            silenceRunnable = Runnable {
                stopSelf()
            }
            this.silenceRunnable = silenceRunnable
        }
        val now = System.currentTimeMillis()
        val delayMillis = triggerAt - now
        handler.postDelayed(silenceRunnable, delayMillis)
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    private fun formatDateTime(time: Long): String? {
        return if (time == ZmanimItem.NEVER) {
            "NEVER"
        } else ZmanimHelper.formatDateTime(Date(time))
    }

    companion object {
        /**
         * Extras name to silence to alarm.
         */
        const val EXTRA_SILENCE_TIME = BuildConfig.APPLICATION_ID + ".SILENCE_TIME"

        private const val ID_NOTIFY = 0x1111
        private const val BUSY_TIMEOUT = 10 * DateUtils.SECOND_IN_MILLIS

        private var reminderBusy: String? = ""
        private var reminderBusyTime: Long = 0

        @JvmStatic
        fun enqueueWork(context: Context, intent: Intent) {
            val action = intent.action
            if (TextUtils.isEmpty(action)) {
                return
            }

            // Handler high priority actions immediately.
            if (ACTION_REMIND == action) {
                processReminder(context, intent)
                return
            }
            val requestData = ZmanimReminderWorker.toWorkData(intent)
            val workRequest: WorkRequest =
                OneTimeWorkRequest.Builder(ZmanimReminderWorker::class.java)
                    .setInputData(requestData)
                    .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)
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
        val item = ZmanimReminderItem.from(this, intent)
        if (item != null) {
            showNotification(item)
            startAlarm()
            val extras = intent.extras
            if ((extras != null) && extras.containsKey(EXTRA_SILENCE_TIME)) {
                val triggerAt = extras.getLong(EXTRA_SILENCE_TIME)
                silenceFuture(triggerAt)
            }
        }
    }

    private fun handleDismiss() {
        stopSelf()
    }
}
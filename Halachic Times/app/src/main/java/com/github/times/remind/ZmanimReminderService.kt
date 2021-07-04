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
import android.os.IBinder
import android.os.SystemClock
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.github.times.remind.ZmanimReminder.ACTION_REMIND

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
class ZmanimReminderService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        val action = intent.action
        if (ACTION_REMIND == action) {
            val item = ZmanimReminderItem.from(this, intent)
            if (item != null) {
                showNotification(item)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun startAlarm() {}

    private fun stopAlarm() {}

    private fun showNotification(item: ZmanimReminderItem) {
//        val notification =
    }

    companion object {
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
}
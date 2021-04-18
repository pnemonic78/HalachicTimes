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

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import static com.github.times.remind.ZmanimReminder.ACTION_REMIND;
import static com.github.times.remind.ZmanimReminderWorker.toWorkData;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderService {

    private static final long BUSY_TIMEOUT = 10 * DateUtils.SECOND_IN_MILLIS;

    private static String reminderBusy = "";
    private static long reminderBusyTime = 0;

    public static void enqueueWork(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        // Handler high priority actions immediately.
        if (ACTION_REMIND.equals(action)) {
            processReminder(context, intent);
            return;
        }

        Data requestData = toWorkData(intent);

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ZmanimReminderWorker.class)
            .setInputData(requestData)
            .build();

        WorkManager.getInstance(context)
            .enqueue(workRequest);
    }

    private static void processReminder(Context context, @NonNull Intent intent) {
        final String action = intent.getAction();
        final long now = SystemClock.elapsedRealtime();
        if (reminderBusy.equals(action) && (now - reminderBusyTime < BUSY_TIMEOUT)) {
            return;
        }
        reminderBusy = action;
        reminderBusyTime = now;

        ZmanimReminder reminder = new ZmanimReminder(context);
        reminder.process(intent);
    }
}

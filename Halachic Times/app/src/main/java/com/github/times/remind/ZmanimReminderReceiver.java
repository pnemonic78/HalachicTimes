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

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.util.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.O;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static java.lang.System.currentTimeMillis;
import static com.github.content.IntentUtils.putExtras;

/**
 * Reminders. Receive alarm events, or date-time events, to update reminders.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ZmanimReminderReceiver";

    private static final int JOB_REMINDER = 0x7E312D; // "rEMIND"

    private SimpleDateFormat dateFormat;

    @Override
    @SuppressWarnings("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive " + intent + " [" + formatDateTime(currentTimeMillis()) + "]");

        // Delegate actions to the service.
        if (VERSION.SDK_INT >= O) {
            startReminderJob(context, intent);
        } else {
            Intent service = new Intent(intent);
            service.setClass(context, ZmanimReminderService.class);
            context.startService(service);
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

    @TargetApi(O)
    private void startReminderJob(Context context, Intent intent) {
        android.app.job.JobScheduler scheduler = (android.app.job.JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            LogUtils.w(TAG, "scheduler required");
            return;
        }
        if (scheduler.getPendingJob(JOB_REMINDER) == null) {
            android.os.PersistableBundle extras = new android.os.PersistableBundle();
            extras.putString(ZmanimReminderJobService.EXTRA_ACTION, intent.getAction());
            putExtras(intent, extras);

            android.app.job.JobInfo job = new android.app.job.JobInfo.Builder(JOB_REMINDER, new ComponentName(context, ZmanimReminderJobService.class))
                    .setExtras(extras)
                    .setPersisted(false)
                    .setRequiresDeviceIdle(false)
                    .setOverrideDeadline(HOUR_IN_MILLIS)
                    .setMinimumLatency(0L)
                    .build();
            scheduler.schedule(job);
        }
    }
}

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

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static java.lang.System.currentTimeMillis;
import static net.sf.times.ZmanimReminderJobService.EXTRA_ACTION;

/**
 * Reminders. Receive alarm events, or date-time events, to update reminders.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ZmanimReminderReceiver";

    private static final int JOB_REMINDER = 1;

    private SimpleDateFormat dateFormat;

    /** No-argument constructor for broadcast receiver. */
    public ZmanimReminderReceiver() {
    }

    @Override
    @SuppressWarnings("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive " + intent + " [" + formatDateTime(currentTimeMillis()) + "]");

        // Delegate actions to the service.
        if (SDK_INT >= O) {
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

    @TargetApi(O)
    private void startReminderJob(Context context, Intent intent) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler.getAllPendingJobs().isEmpty()) {
            PersistableBundle extras = new PersistableBundle();
            extras.putString(EXTRA_ACTION, intent.getAction());

            JobInfo job = new JobInfo.Builder(JOB_REMINDER, new ComponentName(context, ZmanimReminderJobService.class))
                    .setExtras(extras)
                    .setPersisted(false)
                    .setRequiresDeviceIdle(false)
                    .setOverrideDeadline(MINUTE_IN_MILLIS)
                    .build();
            scheduler.schedule(job);
        }
    }
}

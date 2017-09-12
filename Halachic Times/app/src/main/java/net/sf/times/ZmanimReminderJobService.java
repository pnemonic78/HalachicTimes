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
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
@TargetApi(LOLLIPOP)
public class ZmanimReminderJobService extends JobService {

    private static final String TAG = "ZReminderJobService";

    public static final String EXTRA_ACTION = "action";

    private ZmanimReminder reminder;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.v(TAG, "start job");
        PersistableBundle extras = jobParameters.getExtras();
        String action = extras.getString(EXTRA_ACTION);
        Intent intent = new Intent(action);

        if (reminder == null) {
            reminder = new ZmanimReminder(this);
        }
        reminder.process(intent);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.v(TAG, "stop job");
        reminder = null;
        return true;
    }
}

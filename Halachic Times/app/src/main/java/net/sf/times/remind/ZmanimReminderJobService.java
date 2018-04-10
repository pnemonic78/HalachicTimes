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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.sf.app.LocaleCallbacks;
import net.sf.app.LocaleHelper;
import net.sf.preference.LocalePreferences;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static net.sf.content.IntentUtils.readExtras;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
@TargetApi(LOLLIPOP)
public class ZmanimReminderJobService extends android.app.job.JobService {

    private static final String TAG = "ZReminderJobService";

    public static final String EXTRA_ACTION = "android.intent.ACTION";

    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public boolean onStartJob(android.app.job.JobParameters jobParameters) {
        Log.v(TAG, "start job");
        android.os.PersistableBundle extras = jobParameters.getExtras();
        String action = extras.getString(EXTRA_ACTION);
        Intent intent = new Intent(action);
        readExtras(intent, extras);

        final Context context = this;
        ZmanimReminder task = new ZmanimReminder(context);
        task.process(intent);

        return false;
    }

    @Override
    public boolean onStopJob(android.app.job.JobParameters jobParameters) {
        Log.v(TAG, "stop job");
        return false;
    }
}

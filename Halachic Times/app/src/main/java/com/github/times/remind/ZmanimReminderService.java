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

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.preference.LocalePreferences;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import timber.log.Timber;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderService extends JobIntentService {

    private static final int JOB_REMIND = 0x7e312D; // "rEminD"

    private LocaleCallbacks<LocalePreferences> localeCallbacks;
    private ZmanimReminder reminder;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, ZmanimReminderService.class, JOB_REMIND, intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.v("onHandleWork %s", intent);
        ZmanimReminder reminder = this.reminder;
        if (reminder == null) {
            final Context context = this;
            reminder = new ZmanimReminder(context);
            this.reminder = reminder;
        }
        reminder.process(intent);
    }
}

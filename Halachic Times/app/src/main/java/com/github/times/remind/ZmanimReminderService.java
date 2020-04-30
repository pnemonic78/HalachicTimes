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

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.preference.LocalePreferences;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

import static com.github.times.remind.ZmanimReminder.ACTION_REMIND;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderService extends JobIntentService {

    private static final int JOB_REMIND = 0x7e312D; // "rEminD"

    private LocaleCallbacks<LocalePreferences> localeCallbacks;
    private static final AtomicBoolean reminderBusy = new AtomicBoolean(false);

    public static void enqueueWork(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        // Handler high priority actions immediately.
        if (ACTION_REMIND.equals(action)) {
            processReminder(context, intent);
            return;
        }

        enqueueWork(context, ZmanimReminderService.class, JOB_REMIND, intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localeCallbacks.onCreate(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.v("onHandleWork %s", intent);
        processReminder(this, intent);
    }

    private static void processReminder(Context context, @NonNull Intent intent) {
        if (reminderBusy.compareAndSet(false, true)) {
            return;
        }
        try {
            ZmanimReminder reminder = new ZmanimReminder(context);
            reminder.process(intent);
        } finally {
            reminderBusy.set(false);
        }
    }
}

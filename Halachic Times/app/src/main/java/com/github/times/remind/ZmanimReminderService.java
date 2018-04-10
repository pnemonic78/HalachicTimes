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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import net.sf.preference.LocalePreferences;

/**
 * Check for reminders, and manage the notifications.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderService extends IntentService {

    private static final String TAG = "ZmanimReminderService";

    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    /**
     * Constructs a new service.
     *
     * @param name The worker thread name, important only for debugging.
     */
    public ZmanimReminderService(String name) {
        super(name);
    }

    /** Constructs a new service. */
    public ZmanimReminderService() {
        this(TAG);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    protected Context getContext() {
        return this;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        final Context context = getContext();
        ZmanimReminder reminder = new ZmanimReminder(context);
        reminder.process(intent);
    }
}

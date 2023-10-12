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
package com.github.times.util;

import android.util.Log;

import com.github.util.LogTree;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Crashlytics logger tree for Timber.
 *
 * @author Moshe Waisberg
 */
public class CrashlyticsTree extends LogTree {

    private static final Map<Integer, String> priorityChar = new HashMap<>();

    static {
        priorityChar.put(Log.ASSERT, "A");
        priorityChar.put(Log.ERROR, "E");
        priorityChar.put(Log.DEBUG, "D");
        priorityChar.put(Log.INFO, "I");
        priorityChar.put(Log.VERBOSE, "V");
        priorityChar.put(Log.WARN, "W");
    }

    private final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    public CrashlyticsTree(boolean debug) {
        super(debug);
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        super.log(priority, tag, message, t);

        String logMessage = priorityChar.get(priority) + "/" + tag + ": " + message;
        crashlytics.log(logMessage);
        if (t != null) {
            crashlytics.recordException(t);
        }
    }
}

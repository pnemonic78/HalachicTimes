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
package com.github.times.util

import android.util.Log
import com.github.util.LogTree
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Crashlytics logger tree for Timber.
 *
 * @author Moshe Waisberg
 */
class CrashlyticsTree(debug: Boolean) : LogTree(debug) {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)

        val logMessage = priorityChar[priority] + "/" + tag + ": " + message
        crashlytics.log(logMessage)
        if (t != null) {
            crashlytics.recordException(t)
        }
    }

    companion object {
        private val priorityChar: Map<Int, String> = mapOf(
            Log.ASSERT to "A",
            Log.ERROR to "E",
            Log.DEBUG to "D",
            Log.INFO to "I",
            Log.VERBOSE to "V",
            Log.WARN to "W"
        )
    }
}
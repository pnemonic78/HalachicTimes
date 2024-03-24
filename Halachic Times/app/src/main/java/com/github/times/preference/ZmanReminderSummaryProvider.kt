/*
 * Copyright 2023, Moshe Waisberg
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
package com.github.times.preference

import android.content.Context
import androidx.annotation.Keep
import androidx.preference.Preference
import com.github.content.getQuantityString
import com.github.times.R

@Keep
class ZmanReminderSummaryProvider private constructor() :
    Preference.SummaryProvider<ZmanReminderPreference> {

    override fun provideSummary(preference: ZmanReminderPreference): CharSequence {
        return format(preference.context, preference.value)
    }

    fun format(context: Context, value: String?): String {
        if (value.isNullOrEmpty()) return context.getString(R.string.reminder_off)
        val minutes = value.toIntOrNull() ?: return context.getString(R.string.reminder_off)
        return format(context, minutes)
    }

    fun format(context: Context, minutes: Int): String {
        if (minutes == ON_TIME) return context.getString(R.string.reminder_on_time)
        if (minutes == NEVER) return context.getString(R.string.reminder_off)
        if (minutes < 0) return context.getString(R.string.reminder_off)
        return context.getQuantityString(R.plurals.reminder_minutes, minutes, minutes)
    }

    companion object {
        private const val NEVER = Int.MIN_VALUE
        private const val ON_TIME = 0

        /**
         * Retrieve a singleton instance of this simple
         * [androidx.preference.Preference.SummaryProvider] implementation.
         */
        val instance: ZmanReminderSummaryProvider by lazy { ZmanReminderSummaryProvider() }
    }
}

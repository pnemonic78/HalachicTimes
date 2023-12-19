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

import androidx.annotation.Keep
import androidx.preference.Preference
import com.github.preference.NumberPickerPreference
import com.github.times.R

class ReminderSilenceSummaryProvider private constructor() : Preference.SummaryProvider<NumberPickerPreference> {

    override fun provideSummary(preference: NumberPickerPreference): CharSequence? {
        val context = preference.context
        val newValue = preference.value
        if (newValue < 0) return null
        return context.resources.getQuantityString(R.plurals.reminder_silence_minutes, newValue, newValue, "")
    }

    companion object {
        /**
         * Retrieve a singleton instance of this simple
         * [androidx.preference.Preference.SummaryProvider] implementation.
         */
        val instance: ReminderSilenceSummaryProvider by lazy { ReminderSilenceSummaryProvider() }
    }
}
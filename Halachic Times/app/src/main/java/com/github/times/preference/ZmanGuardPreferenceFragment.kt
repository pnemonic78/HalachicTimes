/*
 * Copyright 2025, Moshe Waisberg
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

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.ListPreference
import com.github.times.R
import com.github.times.preference.ZmanimPreferences.Companion.KEY_OPINION_GUARDS
import com.github.times.preference.ZmanimPreferences.Companion.KEY_OPINION_GUARD_ENDS
import com.github.times.preference.ZmanimPreferences.Values.OPINION_4

/**
 * This fragment shows the preferences for gaurds screen.
 */
@Keep
open class ZmanGuardPreferenceFragment : ZmanPreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val endsPreference = findPreference<ListPreference>(KEY_OPINION_GUARD_ENDS)!!

        findPreference<ListPreference>(KEY_OPINION_GUARDS)!!.apply {
            setOnPreferenceChangeListener { _, newValue: Any? ->
                if (newValue == OPINION_4) {
                    endsPreference.setTitle(R.string.guard_fourth)
                    endsPreference.setDialogTitle(R.string.guard_fourth)
                } else {
                    endsPreference.setTitle(R.string.guard_third)
                    endsPreference.setDialogTitle(R.string.guard_third)
                }
                true
            }
        }
    }
}
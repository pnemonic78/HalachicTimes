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
package com.github.times.preference

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.ListPreference
import com.github.preference.NumberPickerPreference
import com.github.times.R

/**
 * This fragment shows the preferences for the Shabbath Ends screen.
 */
@Keep
class ZmanShabbathPreferenceFragment : ZmanPreferenceFragment() {
    private lateinit var afterPreference: ListPreference
    private lateinit var sunsetPreference: ListPreference
    private lateinit var twilightPreference: ListPreference
    private lateinit var nightfallPreference: ListPreference
    private lateinit var minutesPreference: NumberPickerPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        // Must be in reverse order for non-null dependencies.
        minutesPreference =
            findPreference<NumberPickerPreference>(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES)!!.apply {
                setOnPreferenceChangeListener { _, newValue: Any? ->
                    val shabbathAfterId = preferences.toId(afterPreference.value)
                    onMinutesChanged(shabbathAfterId, newValue as Int)
                    true
                }
            }
        sunsetPreference =
            addDefaultOption(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_SUNSET).apply {
                setOnPreferenceChangeListener { _, _: Any? ->
                    onMinutesChanged(R.string.sunset)
                    true
                }
            }
        twilightPreference =
            addDefaultOption(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_TWILIGHT).apply {
                setOnPreferenceChangeListener { _, _: Any? ->
                    onMinutesChanged(R.string.twilight)
                    true
                }
            }
        nightfallPreference =
            addDefaultOption(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_NIGHTFALL).apply {
                setOnPreferenceChangeListener { _, _: Any? ->
                    onMinutesChanged(R.string.nightfall)
                    true
                }
            }
        afterPreference = findPreference<ListPreference>(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER)!!.apply {
            setOnPreferenceChangeListener { _, newValue: Any? ->
                val shabbathAfterId = preferences.toId(newValue?.toString())
                onMinutesChanged(shabbathAfterId)
                true
            }
            val shabbathAfterId = preferences.toId(this.value)
            onMinutesChanged(shabbathAfterId)
        }
    }

    private fun addDefaultOption(key: String): ListPreference {
        val preference = findPreference<ListPreference>(key)!!
        return addDefaultOption(preference)
    }

    private fun addDefaultOption(preference: ListPreference): ListPreference {
        val context = preference.context
        val oldValues = preference.entryValues
        val oldSize = oldValues.size
        val newSize = oldSize + 1
        val newValues = arrayOfNulls<CharSequence>(newSize)
        System.arraycopy(oldValues, 0, newValues, 1, oldSize)
        val defaultValue = context.getText(R.string.opinion_value_none)
        newValues[0] = defaultValue
        preference.entryValues = newValues
        val oldEntries = preference.entries
        val newEntries = arrayOfNulls<CharSequence>(newSize)
        System.arraycopy(oldEntries, 0, newEntries, 1, oldSize)
        val defaultEntry = context.getText(R.string.none)
        newEntries[0] = defaultEntry
        preference.entries = newEntries
        return preference
    }

    private fun getMinutesSummary(shabbathAfterId: Int, minutes: Int): String {
        val shabbathAfterName = getString(shabbathAfterId)
        var opinionValue: String? = null
        var opinionLabel: CharSequence? = null
        if (shabbathAfterId == R.string.sunset) {
            opinionValue = sunsetPreference.value
            opinionLabel = sunsetPreference.entry
        } else if (shabbathAfterId == R.string.twilight) {
            opinionValue = twilightPreference.value
            opinionLabel = twilightPreference.entry
        } else if (shabbathAfterId == R.string.nightfall) {
            opinionValue = nightfallPreference.value
            opinionLabel = nightfallPreference.entry
        }
        return if (opinionLabel.isNullOrEmpty() || ZmanimPreferences.Values.OPINION_NONE == opinionValue) {
            resources.getQuantityString(
                R.plurals.shabbath_ends_summary,
                minutes,
                minutes,
                shabbathAfterName
            )
        } else {
            resources.getQuantityString(
                R.plurals.shabbath_ends_specific_summary,
                minutes,
                minutes,
                shabbathAfterName,
                opinionLabel
            )
        }
    }

    private fun onMinutesChanged(shabbathAfterId: Int, minutes: Int = minutesPreference.value) {
        when (shabbathAfterId) {
            R.string.sunset -> {
                sunsetPreference.isEnabled = true
                twilightPreference.isEnabled = false
                nightfallPreference.isEnabled = false
            }

            R.string.twilight -> {
                sunsetPreference.isEnabled = false
                twilightPreference.isEnabled = true
                nightfallPreference.isEnabled = false
            }

            R.string.nightfall -> {
                sunsetPreference.isEnabled = false
                twilightPreference.isEnabled = false
                nightfallPreference.isEnabled = true
            }

            else -> {
                sunsetPreference.isEnabled = false
                twilightPreference.isEnabled = false
                nightfallPreference.isEnabled = false
            }
        }
        minutesPreference.summary = getMinutesSummary(shabbathAfterId, minutes)
    }
}
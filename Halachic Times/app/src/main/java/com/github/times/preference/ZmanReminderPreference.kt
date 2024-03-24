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

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.github.preference.NumberPickerPreference
import com.github.util.TypedValueUtils

/**
 * Zman reminder preference.
 */
class ZmanReminderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = TypedValueUtils.getAttr(
        context,
        androidx.preference.R.attr.dialogPreferenceStyle,
        android.R.attr.dialogPreferenceStyle
    ),
    @StyleRes defStyleRes: Int = 0
) : NumberPickerPreference(context, attrs, defStyleAttr, defStyleRes) {

    private val entryValues: IntArray

    init {
        val entryValues = values
        this.entryValues = entryValues
        val entries = formatValues(context, entryValues)
        displayedValues = entries
        min = 0
        max = entryValues.lastIndex
        summaryProvider = ZmanReminderSummaryProvider.instance
    }

    override fun getPersistedInt(defaultReturnValue: Int): Int {
        // The original value is a string.
        val defaultReturnValueStr = defaultReturnValue.toString()
        val persistedValue: String? = getPersistedString(defaultReturnValueStr)
        if (persistedValue.isNullOrEmpty()) return NEVER
        return persistedValue.toInt()
    }

    override fun persistInt(value: Int): Boolean {
        return persistString(value.toString())
    }

    override fun notifyChanged() {
        super.notifyChanged()
        notifyDependencyChange(shouldDisableDependents())
    }

    override fun shouldDisableDependents(): Boolean {
        return super.shouldDisableDependents() || isOff
    }

    private var isOff: Boolean = false

    private fun formatValues(context: Context, values: IntArray): Array<String?> {
        val formatter = ZmanReminderSummaryProvider.instance
        return Array(values.size) { i -> formatter.format(context, values[i]) }
    }

    override fun onNeutralButtonClicked() {
        if (callChangeListener(NEVER)) {
            value = NEVER
        }
    }

    override fun setValueImpl(oldValue: Int, newValue: Int): Int {
        isOff = newValue < 0
        return super.setValueImpl(oldValue, newValue)
    }

    companion object {
        private const val NEVER = Int.MIN_VALUE

        // "On Time", 1..60
        private const val SIZE = 1 + 60

        private val values: IntArray = IntArray(SIZE) { it }
    }
}
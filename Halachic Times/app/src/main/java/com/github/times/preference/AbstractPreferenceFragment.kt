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
import androidx.preference.Preference
import com.github.appwidget.AppWidgetUtils.notifyAppWidgetsUpdate
import com.github.preference.AbstractPreferenceFragment
import com.github.times.appwidget.ClockWidget
import com.github.times.appwidget.ZmanimListWidget
import com.github.times.appwidget.ZmanimWidget

/**
 * This fragment shows the preferences for a header.
 */
abstract class AbstractPreferenceFragment : AbstractPreferenceFragment() {
    override fun notifyPreferenceChanged(preference: Preference) {
        super.notifyPreferenceChanged(preference)
        notifyAppWidgets(preference.context)
    }

    protected fun notifyAppWidgets(context: Context) {
        notifyAppWidgetsUpdate(requireContext(), ZmanimWidget::class.java)
        notifyAppWidgetsUpdate(context, ZmanimListWidget::class.java)
        notifyAppWidgetsUpdate(context, ClockWidget::class.java)
    }
}
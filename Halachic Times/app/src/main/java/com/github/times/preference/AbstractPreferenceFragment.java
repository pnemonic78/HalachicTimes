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
package com.github.times.preference;

import android.content.Context;

import com.github.times.appwidget.ClockWidget;
import com.github.times.appwidget.ZmanimListWidget;
import com.github.times.appwidget.ZmanimWidget;

import static com.github.appwidget.AppWidgetUtils.notifyAppWidgetsUpdate;

/**
 * This fragment shows the preferences for a header.
 */
public abstract class AbstractPreferenceFragment extends net.sf.preference.AbstractPreferenceFragment {

    @Override
    protected void notifyPreferenceChanged() {
        super.notifyPreferenceChanged();
        notifyAppWidgets();
    }

    protected void notifyAppWidgets() {
        final Context context = getActivity();
        notifyAppWidgetsUpdate(context, ZmanimWidget.class);
        notifyAppWidgetsUpdate(context, ZmanimListWidget.class);
        notifyAppWidgetsUpdate(context, ClockWidget.class);
    }
}

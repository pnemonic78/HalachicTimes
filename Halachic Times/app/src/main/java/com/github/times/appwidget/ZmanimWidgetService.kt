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
package com.github.times.appwidget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.preference.LocalePreferences

/**
 * Service that provides the list of halachic times (*zmanim*) items for the scrollable widget.
 *
 * @author Moshe Waisberg
 */
class ZmanimWidgetService : RemoteViewsService() {
    private lateinit var localeCallbacks: LocaleCallbacks<LocalePreferences>

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        localeCallbacks.onPreCreate(this)
        super.onCreate()
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val context: Context = this
        return ZmanimWidgetViewsFactory(context, localeCallbacks)
    }
}
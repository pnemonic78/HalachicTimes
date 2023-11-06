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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.github.app.isBrightWallpaper
import com.github.times.R
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shows a scrollable list of halachic times (*zmanim*) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
class ZmanimListWidget : ZmanimWidget() {
    override fun getLayoutId(): Int {
        val themeId = this.theme
        return when {
            themeId == THEME_APPWIDGET_DARK -> R.layout.widget_list
            themeId == THEME_APPWIDGET_LIGHT -> R.layout.widget_list_light
            isBrightWallpaper(context) -> R.layout.widget_list_light
            else -> R.layout.widget_list
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GlobalScope.launch {
            delay(1000)
            notifyAppWidgetViewDataChanged(context)
        }
    }

    override fun populateWidgetTimes(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        activityPendingIntent: PendingIntent,
        viewId: Int,
        day: Long
    ): ZmanimAdapter<ZmanViewHolder>? {
        populateScrollableTimes(context, appWidgetId, views, activityPendingIntent)
        return null
    }

    private fun populateScrollableTimes(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        activityPendingIntent: PendingIntent
    ) {
        views.setPendingIntentTemplate(ID_LIST, activityPendingIntent)
        bindListView(context, appWidgetId, views)
    }

    /**
     * Bind the times to remote list view.
     *
     * @param context     the context.
     * @param appWidgetId the app widget id.
     * @param list        the remote list.
     */
    private fun bindListView(context: Context, appWidgetId: Int, list: RemoteViews) {
        val adapter = Intent(context, ZmanimWidgetService::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
        list.setRemoteAdapter(ID_LIST, adapter)
    }
}
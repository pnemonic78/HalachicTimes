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
package com.github.times.appwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.github.graphics.BitmapUtils.isBrightWallpaper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.StyleRes;

import com.github.times.R;
import com.github.times.ZmanimAdapter;

/**
 * Shows a scrollable list of halachic times (<em>zmanim</em>) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimListWidget extends ZmanimWidget {

    @StyleRes
    private static final int THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark;
    @StyleRes
    private static final int THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light;

    @Override
    protected int getLayoutId() {
        int theme = getTheme();
        if (theme == THEME_APPWIDGET_DARK) {
            return R.layout.widget_list;
        } else if (theme == THEME_APPWIDGET_LIGHT) {
            return R.layout.widget_list_light;
        }
        if (isBrightWallpaper(getContext())) {
            return R.layout.widget_list_light;
        }
        return R.layout.widget_list;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        notifyAppWidgetViewDataChanged(context);
    }

    @Override
    protected ZmanimAdapter populateWidgetTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long day) {
        populateScrollableTimes(context, appWidgetId, views, activityPendingIntent);
        return null;
    }

    protected void populateScrollableTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent) {
        views.setPendingIntentTemplate(android.R.id.list, activityPendingIntent);
        bindListView(context, appWidgetId, views);
    }

    /**
     * Bind the times to remote list view.
     *
     * @param context     the context.
     * @param appWidgetId the app widget id.
     * @param list        the remote list.
     */
    protected void bindListView(Context context, int appWidgetId, RemoteViews list) {
        Intent adapter = new Intent(context, ZmanimWidgetService.class)
            .putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
        adapter.setData(Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME)));
        list.setRemoteAdapter(android.R.id.list, adapter);
    }
}

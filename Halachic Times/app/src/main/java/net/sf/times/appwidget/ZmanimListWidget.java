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
package net.sf.times.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import net.sf.times.R;
import net.sf.times.ZmanimAdapter;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static net.sf.graphics.BitmapUtils.isBright;
import static net.sf.graphics.DrawableUtils.getWallpaperColor;

/**
 * Shows a scrollable list of halachic times (<em>zmanim</em>) for prayers in a
 * widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimListWidget extends ZmanimWidget {

    @Override
    protected int getLayoutId() {
        /*TODO
        if (isBright(getWallpaperColor(getContext()))) {
            return R.layout.widget_list_light;
        }*/
        return R.layout.widget_list;
    }

    @Override
    protected void notifyAppWidgetViewDataChanged(Context context) {
        notifyAppWidgetViewDataChanged11(context);
    }

    @Override
    protected ZmanimAdapter populateWidgetTimes(int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long now) {
        populateScrollableTimes(appWidgetId, views, activityPendingIntent);
        return null;
    }

    protected void populateScrollableTimes(int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent) {
        views.setPendingIntentTemplate(android.R.id.list, activityPendingIntent);
        bindListView(appWidgetId, views);
    }

    /**
     * Bind the times to remote list view.
     *
     * @param appWidgetId
     *         the app widget id.
     * @param list
     *         the remote list.
     */
    protected void bindListView(int appWidgetId, RemoteViews list) {
        final Context context = getContext();
        Intent adapter = new Intent();
        adapter.setClassName(context, ZmanimWidgetService.class.getCanonicalName());
        adapter.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
        adapter.setData(Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME)));
        list.setRemoteAdapter(android.R.id.list, adapter);
    }
}

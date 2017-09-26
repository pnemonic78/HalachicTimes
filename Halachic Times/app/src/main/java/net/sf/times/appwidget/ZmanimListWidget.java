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
import android.widget.RemoteViews;

import net.sf.times.R;
import net.sf.times.ZmanimAdapter;

/**
 * Shows a scrollable list of halachic times (<em>zmanim</em>) for prayers in a
 * widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimListWidget extends ZmanimWidget {

    @Override
    protected int getLayoutId() {
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

}

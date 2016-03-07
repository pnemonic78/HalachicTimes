/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Shows a scrollable list of halachic times (<em>zmanim</em>) for prayers in a
 * widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimListWidget extends ZmanimWidget {

    /**
     * Constructs a new widget.
     */
    public ZmanimListWidget() {
    }

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

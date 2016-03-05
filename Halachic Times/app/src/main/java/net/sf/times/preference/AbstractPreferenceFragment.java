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
package net.sf.times.preference;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import net.sf.times.ZmanimWidget;

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
        Context context = getActivity();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final Class<?> clazz = ZmanimWidget.class;
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;

        Intent intent = new Intent(context, ZmanimWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
}

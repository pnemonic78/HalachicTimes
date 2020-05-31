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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.appwidget.AppWidgetUtils;
import com.github.preference.LocalePreferences;
import com.github.times.ZmanimActivity;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimApplication;
import com.github.times.ZmanimHelper;
import com.github.times.ZmanimItem;
import com.github.times.ZmanimPopulater;
import com.github.times.location.ZmanimLocationListener;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;

import net.sourceforge.zmanim.util.GeoLocation;

import java.util.List;

import timber.log.Timber;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;
import static android.content.Intent.ACTION_WALLPAPER_CHANGED;
import static android.text.TextUtils.isEmpty;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static com.github.appwidget.AppWidgetUtils.notifyAppWidgetsUpdate;
import static com.github.times.location.ZmanimLocationListener.ACTION_LOCATION_CHANGED;
import static java.lang.System.currentTimeMillis;

/**
 * Halachic times (<em>zmanim</em>) widget.
 *
 * @author Moshe Waisberg
 */
public abstract class ZmanimAppWidget extends AppWidgetProvider {

    /**
     * Reminder id for alarms.
     */
    private static final int ID_ALARM_WIDGET = 10;

    /**
     * The context.
     */
    protected Context context;
    /**
     * The preferences.
     */
    private ZmanimPreferences preferences;
    private LocaleCallbacks<LocalePreferences> localeCallbacks;
    protected boolean directionRTL = false;

    /**
     * Get the context.
     *
     * @return the context.
     */
    protected Context getContext() {
        return context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("onReceive %s %s", this, intent);
        this.localeCallbacks = new LocaleHelper<>(context);
        context = localeCallbacks.attachBaseContext(context);
        this.context = context;
        super.onReceive(context, intent);
        this.directionRTL = LocaleUtils.isLocaleRTL(context);

        final String action = intent.getAction();
        if (isEmpty(action)) {
            return;
        }
        switch (action) {
            case ACTION_DATE_CHANGED:
            case ACTION_TIME_CHANGED:
            case ACTION_TIMEZONE_CHANGED:
            case ACTION_WALLPAPER_CHANGED:
                notifyAppWidgets(context);
                break;
            case ACTION_LOCATION_CHANGED: {
                Location location = intent.getParcelableExtra(ZmanimLocationListener.EXTRA_LOCATION);
                if (location != null) {
                    onLocationChanged(location);
                }
                break;
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.v("onUpdate %s", this);
        this.localeCallbacks = new LocaleHelper<>(context);
        context = localeCallbacks.attachBaseContext(context);
        this.context = context;
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.directionRTL = LocaleUtils.isLocaleRTL(context);

        populateTimes(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Populate the list with times.
     *
     * @param context          the context.
     * @param appWidgetManager the widget manager.
     * @param appWidgetIds     the widget ids for which an update is needed.
     */
    protected void populateTimes(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if ((appWidgetIds == null) || (appWidgetIds.length == 0)) {
            return;
        }

        // Pass the activity to ourselves, because starting another activity is not working.
        final int viewId = getIntentViewId();
        Intent activityIntent;
        PendingIntent activityPendingIntent;
        String packageName = context.getPackageName();
        RemoteViews views;
        int layoutId = getLayoutId();
        long day = getDay();
        ZmanimAdapter adapter = null;

        for (int appWidgetId : appWidgetIds) {
            activityIntent = new Intent(context, ZmanimActivity.class);
            activityIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
            activityPendingIntent = PendingIntent.getActivity(context, appWidgetId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views = new RemoteViews(packageName, layoutId);

            adapter = populateWidgetTimes(context, appWidgetId, views, activityPendingIntent, viewId, day);

            try {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (RuntimeException e) {
                // Caused by: android.os.DeadObjectException
            }
        }

        scheduleNext(context, appWidgetIds, adapter);
    }

    /**
     * Populate the list with times.
     *
     * @param context the context.
     */
    protected void populateTimes(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        populateTimes(context, appWidgetManager, getAppWidgetIds(context));
    }

    public void onLocationChanged(Location location) {
        notifyAppWidgets(context);
    }

    /**
     * Schedule an update for the next relevant zman.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     * @param adapter      the adapter with zmanim.
     */
    private void scheduleNext(Context context, int[] appWidgetIds, ZmanimAdapter adapter) {
        if (adapter == null) {
            return;
        }

        int count = adapter.getCount();
        if (count == 0) {
            return;
        }

        long now = currentTimeMillis();
        long when = Long.MAX_VALUE;
        ZmanimItem item;
        long time;

        for (int i = 0; i < count; i++) {
            item = adapter.getItem(i);
            if ((item == null) || item.isEmptyOrElapsed()) {
                continue;
            }
            time = item.time;
            if ((now < time) && (time < when)) {
                when = time;
            }
        }
        if (when < Long.MAX_VALUE) {
            // Let the first visible item linger for another minute.
            scheduleUpdate(context, appWidgetIds, when + MINUTE_IN_MILLIS);
        }
    }

    /**
     * Schedule an update to populate the day's list.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     * @param time         the time to update.
     */
    private void scheduleUpdate(Context context, int[] appWidgetIds, long time) {
        Timber.i("scheduleUpdate [%s]", ZmanimHelper.formatDateTime(time));
        Intent alarmIntent = new Intent(context, getClass());
        alarmIntent.setAction(ACTION_APPWIDGET_UPDATE);
        alarmIntent.putExtra(EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, ID_ALARM_WIDGET, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC, time, alarmPendingIntent);
    }

    /**
     * Bind the times to remote views.
     *
     * @param context         the context.
     * @param list            the remote views.
     * @param adapterToday    the list adapter for today.
     * @param adapterTomorrow the list adapter for tomorrow.
     */
    protected abstract void bindViews(Context context, RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow);

    /**
     * Bind the times to remote views.
     *
     * @param context the context.
     * @param list    the remote views.
     * @param items   the list of items.
     */
    protected abstract void bindViews(Context context, RemoteViews list, List<ZmanimItem> items);

    /**
     * Bind the item to remote views.
     *
     * @param context       the context.
     * @param list          the remote list.
     * @param position      the position index.
     * @param positionTotal the position index relative to all rows.
     * @param item          the zmanim item.
     * @return {@code true} if item was bound to view.
     */
    protected abstract boolean bindView(Context context, RemoteViews list, int position, int positionTotal, @Nullable ZmanimItem item);

    /**
     * Get the layout for the container.
     *
     * @return the layout id.
     */
    protected abstract int getLayoutId();

    /**
     * Get the view for the intent click.
     *
     * @return the view id.
     */
    protected abstract int getIntentViewId();

    protected void notifyAppWidgetViewDataChanged(Context context) {
        AppWidgetUtils.notifyAppWidgetViewDataChanged(context, getClass(), android.R.id.list);
    }

    protected ZmanimPreferences getPreferences() {
        ZmanimPreferences preferences = this.preferences;
        if (preferences == null) {
            preferences = new SimpleZmanimPreferences(context);
            this.preferences = preferences;
        }
        return preferences;
    }

    protected ZmanimAdapter populateWidgetTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long day) {
        return populateStaticTimes(context, appWidgetId, views, activityPendingIntent, viewId, day);
    }

    protected ZmanimAdapter populateStaticTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long day) {
        views.setOnClickPendingIntent(viewId, activityPendingIntent);

        ZmanimLocations locations = getLocations(context);
        GeoLocation gloc = locations.getGeoLocation();
        if (gloc == null) {
            return null;
        }

        ZmanimPreferences preferences = getPreferences();

        ZmanimPopulater populater = new ZmanimPopulater(context, preferences);
        populater.setCalendar(day);
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        ZmanimAdapter adapter = new ZmanimAdapter(context, preferences);
        populater.populate(adapter, true);

        ZmanimAdapter adapterTomorrow = new ZmanimAdapter(context, preferences);
        populater.setCalendar(day + DAY_IN_MILLIS);
        populater.populate(adapterTomorrow, true);

        bindViews(context, views, adapter, adapterTomorrow);

        return adapter;
    }

    protected int[] getAppWidgetIds(Context context) {
        return AppWidgetUtils.getAppWidgetIds(context, getClass());
    }

    @StyleRes
    protected int getTheme() {
        return getPreferences().getAppWidgetTheme();
    }

    private ZmanimLocations getLocations(Context context) {
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        return app.getLocations();
    }

    protected void notifyAppWidgets(Context context) {
        notifyAppWidgetsUpdate(context, getClass());
    }

    protected long getDay() {
        return currentTimeMillis();
    }
}

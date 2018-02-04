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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import net.sf.app.LocaleCallbacks;
import net.sf.app.LocaleHelper;
import net.sf.preference.LocalePreferences;
import net.sf.times.ZmanimActivity;
import net.sf.times.ZmanimAdapter;
import net.sf.times.ZmanimApplication;
import net.sf.times.ZmanimItem;
import net.sf.times.ZmanimPopulater;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.SimpleZmanimPreferences;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.util.GeoLocation;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED;
import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;
import static android.content.Intent.ACTION_WALLPAPER_CHANGED;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static java.lang.System.currentTimeMillis;

/**
 * Halachic times (<em>zmanim</em>) widget.
 *
 * @author Moshe Waisberg
 */
public abstract class ZmanimAppWidget extends AppWidgetProvider implements ZmanimLocationListener {

    /** Reminder id for alarms. */
    private static final int ID_ALARM_WIDGET = 10;

    /** The context. */
    protected Context context;
    /** Provider for locations. */
    private ZmanimLocations locations;
    /** The preferences. */
    private ZmanimPreferences preferences;
    /** The provider name. */
    private ComponentName provider;
    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    private final ContentObserver formatChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            final Context context = getContext();
            notifyAppWidgetViewDataChanged(context);
        }
    };

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
        this.localeCallbacks = new LocaleHelper<>(context);
        context = localeCallbacks.attachBaseContext(context);
        super.onReceive(context, intent);
        this.context = context;

        final String action = intent.getAction();
        if (action == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        switch (action) {
            case ACTION_APPWIDGET_UPDATE:
                resolver.registerContentObserver(Uri.withAppendedPath(Settings.System.CONTENT_URI, Settings.System.TIME_12_24), true, formatChangeObserver);
                break;
            case ACTION_APPWIDGET_DELETED:
                resolver.unregisterContentObserver(formatChangeObserver);
                break;
            case ACTION_DATE_CHANGED:
            case ACTION_TIME_CHANGED:
            case ACTION_TIMEZONE_CHANGED:
            case ACTION_WALLPAPER_CHANGED:
                notifyAppWidgetViewDataChanged(context);
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.localeCallbacks = new LocaleHelper<>(context);
        context = localeCallbacks.attachBaseContext(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        this.context = context;
        if (locations == null) {
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
        }
        locations.start(this);

        populateTimes(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Populate the list with times.
     *
     * @param context
     *         the context.
     * @param appWidgetManager
     *         the widget manager.
     * @param appWidgetIds
     *         the widget ids for which an update is needed.
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
        long now = currentTimeMillis();
        ZmanimAdapter adapter = null;

        for (int appWidgetId : appWidgetIds) {
            activityIntent = new Intent(context, ZmanimActivity.class);
            activityIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
            activityPendingIntent = PendingIntent.getActivity(context, appWidgetId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views = new RemoteViews(packageName, layoutId);

            adapter = populateWidgetTimes(context, appWidgetId, views, activityPendingIntent, viewId, now);

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
     * @param context
     *         the context.
     */
    protected void populateTimes(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        populateTimes(context, appWidgetManager, appWidgetManager.getAppWidgetIds(getProvider()));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (locations != null) {
            locations.stop(this);
        }
        context.unregisterReceiver(this);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        if (locations != null) {
            locations.start(this);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_SET_WALLPAPER);
        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onLocationChanged(Location location) {
        final Context context = getContext();
        notifyAppWidgetViewDataChanged(context);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    @Override
    public boolean isPassive() {
        return true;
    }

    /**
     * Schedule an update for the next relevant zman.
     *
     * @param context
     *         the context.
     * @param appWidgetIds
     *         the widget ids for which an update is needed.
     * @param adapter
     *         the adapter with zmanim.
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
     * @param context
     *         the context.
     * @param appWidgetIds
     *         the widget ids for which an update is needed.
     * @param time
     *         the time to update.
     */
    private void scheduleUpdate(Context context, int[] appWidgetIds, long time) {
        Intent alarmIntent = new Intent(context, ZmanimAppWidget.class);
        alarmIntent.setAction(ACTION_APPWIDGET_UPDATE);
        alarmIntent.putExtra(EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, ID_ALARM_WIDGET, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC, time, alarmPendingIntent);
    }

    /**
     * Bind the times to remote views.
     *
     * @param context
     *         the context.
     * @param list
     *         the remote views.
     * @param adapterToday
     *         the list adapter for today.
     * @param adapterTomorrow
     *         the list adapter for tomorrow.
     */
    protected abstract void bindViews(Context context, RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow);

    /**
     * Bind the item to remote views.
     *
     * @param context
     *         the context.
     * @param list
     *         the remote list.
     * @param position
     *         the position index.
     * @param positionTotal
     *         the position index relative to all rows.
     * @param item
     *         the zmanim item.
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
        populateTimes(context);
    }

    protected void notifyAppWidgetViewDataChanged11(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final Class<?> clazz = getClass();
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, getIntentViewId());
    }

    protected ZmanimAdapter populateWidgetTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long now) {
        return populateStaticTimes(context, appWidgetId, views, activityPendingIntent, viewId, now);
    }

    protected ZmanimAdapter populateStaticTimes(Context context, int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long now) {
        views.setOnClickPendingIntent(viewId, activityPendingIntent);

        if (preferences == null) {
            preferences = new SimpleZmanimPreferences(context);
        }

        ZmanimLocations locations = this.locations;
        if (locations == null) {
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
            locations.start(this);
            this.locations = locations;
        }
        GeoLocation gloc = locations.getGeoLocation();
        if (gloc == null)
            return null;

        ZmanimPopulater populater = new ZmanimPopulater(context, preferences);
        populater.setCalendar(now);
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        ZmanimAdapter adapter = new ZmanimAdapter(context, preferences);
        populater.populate(adapter, true);

        ZmanimAdapter adapterTomorrow = new ZmanimAdapter(context, preferences);
        populater.setCalendar(now + DAY_IN_MILLIS);
        populater.populate(adapterTomorrow, true);

        bindViews(context, views, adapter, adapterTomorrow);

        return adapter;
    }

    protected ComponentName getProvider() {
        if (provider == null) {
            final Class<?> clazz = getClass();
            provider = new ComponentName(context, clazz);
        }
        return provider;
    }
}

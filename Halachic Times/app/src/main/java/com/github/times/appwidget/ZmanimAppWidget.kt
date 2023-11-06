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

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.location.Location
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.appwidget.AppWidgetUtils.getAppWidgetIds
import com.github.appwidget.AppWidgetUtils.notifyAppWidgetViewDataChanged
import com.github.appwidget.AppWidgetUtils.notifyAppWidgetsUpdate
import com.github.os.getParcelableCompat
import com.github.preference.LocalePreferences
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimActivity
import com.github.times.ZmanimAdapter
import com.github.times.ZmanimApplication
import com.github.times.ZmanimHelper.formatDateTime
import com.github.times.ZmanimItem
import com.github.times.ZmanimPopulater
import com.github.times.isNullOrEmptyOrElapsed
import com.github.times.location.ZmanimLocationListener
import com.github.times.location.ZmanimLocations
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.util.LocaleUtils.isLocaleRTL
import java.util.Calendar
import timber.log.Timber

/**
 * Halachic times (*zmanim*) widget.
 *
 * @author Moshe Waisberg
 */
abstract class ZmanimAppWidget : AppWidgetProvider() {
    /**
     * The context.
     */
    protected lateinit var context: Context

    /**
     * The preferences.
     */
    protected val preferences: ZmanimPreferences by lazy { SimpleZmanimPreferences(context) }

    protected var isDirectionRTL = false
        private set

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("onReceive %s %s", this, intent)
        val localeCallbacks: LocaleCallbacks<LocalePreferences> = LocaleHelper(context)
        val contextLocale = localeCallbacks.attachBaseContext(context)
        this.context = contextLocale
        super.onReceive(contextLocale, intent)
        isDirectionRTL = isLocaleRTL(contextLocale)

        val action = intent.action
        if (action.isNullOrEmpty()) return
        when (action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_WALLPAPER_CHANGED -> notifyAppWidgets(contextLocale)

            ZmanimLocationListener.ACTION_LOCATION_CHANGED -> {
                val location = intent.getParcelableCompat(
                    ZmanimLocationListener.EXTRA_LOCATION,
                    Location::class.java
                )
                location?.let { onLocationChanged(contextLocale) }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.v("onUpdate %s", this)
        val localeCallbacks: LocaleCallbacks<LocalePreferences> = LocaleHelper(context)
        val contextLocale = localeCallbacks.attachBaseContext(context)
        this.context = contextLocale
        super.onUpdate(contextLocale, appWidgetManager, appWidgetIds)
        isDirectionRTL = isLocaleRTL(contextLocale)
        populateTimes(contextLocale, appWidgetManager, appWidgetIds)
    }

    /**
     * Populate the list with times.
     *
     * @param context          the context.
     * @param appWidgetManager the widget manager.
     * @param appWidgetIds     the widget ids for which an update is needed.
     */
    private fun populateTimes(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        if (appWidgetIds == null || appWidgetIds.isEmpty()) {
            return
        }

        // Pass the activity to ourselves, because starting another activity is not working.
        val viewId = getIntentViewId()
        var activityIntent: Intent
        var activityPendingIntent: PendingIntent?
        val packageName = context.packageName
        var views: RemoteViews
        val layoutId = getLayoutId()
        val day = day
        var adapter: ZmanimAdapter<ZmanViewHolder>? = null

        for (appWidgetId in appWidgetIds) {
            activityIntent = Intent(context, ZmanimActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activityPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views = RemoteViews(packageName, layoutId)
            adapter =
                populateWidgetTimes(context, appWidgetId, views, activityPendingIntent, viewId, day)
            try {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: RuntimeException) {
                // Caused by: android.os.DeadObjectException
            }
        }
        if (adapter != null) {
            scheduleNext(context, appWidgetIds, adapter)
        }
    }

    /**
     * Populate the list with times.
     *
     * @param context the context.
     */
    protected fun populateTimes(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        populateTimes(context, appWidgetManager, getAppWidgetIds(context))
    }

    private fun onLocationChanged(context: Context) {
        notifyAppWidgets(context)
    }

    /**
     * Schedule an update for the next relevant zman.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     * @param adapter      the adapter with zmanim.
     */
    private fun scheduleNext(
        context: Context,
        appWidgetIds: IntArray,
        adapter: ZmanimAdapter<ZmanViewHolder>
    ) {
        val count = adapter.itemCount
        if (count == 0) {
            return
        }
        val now = System.currentTimeMillis()
        var whenUpdate = Long.MAX_VALUE
        var item: ZmanimItem?
        var time: Long
        for (i in 0 until count) {
            item = adapter.getItem(i)
            if (item.isNullOrEmptyOrElapsed()) {
                continue
            }
            time = item.time
            if (now < time && time < whenUpdate) {
                whenUpdate = time
            }
        }
        if (whenUpdate < Long.MAX_VALUE) {
            // Let the first visible item linger for another minute.
            scheduleUpdate(context, appWidgetIds, whenUpdate + DateUtils.MINUTE_IN_MILLIS)
        }
        scheduleNextDay(context, appWidgetIds)
    }

    /**
     * Schedule an update to populate the day's list.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     * @param time         the time to update.
     * @param id           the pending intent's id.
     */
    private fun schedulePending(context: Context, appWidgetIds: IntArray, time: Long, id: Int) {
        Timber.i("schedulePending [%s]", formatDateTime(time))
        val alarmIntent = Intent(context, javaClass)
            .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm[AlarmManager.RTC, time] = alarmPendingIntent
    }

    /**
     * Schedule an update to populate the day's list.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     * @param time         the time to update.
     */
    private fun scheduleUpdate(context: Context, appWidgetIds: IntArray, time: Long) {
        schedulePending(context, appWidgetIds, time, ID_WIDGET_UPDATE)
    }

    /**
     * Bind the times to remote views.
     *
     * @param context         the context.
     * @param list            the remote views.
     * @param adapterToday    the list adapter for today.
     * @param adapterTomorrow the list adapter for tomorrow.
     */
    protected abstract fun bindViews(
        context: Context,
        list: RemoteViews,
        adapterToday: ZmanimAdapter<ZmanViewHolder>,
        adapterTomorrow: ZmanimAdapter<ZmanViewHolder>
    )

    /**
     * Bind the times to remote views.
     *
     * @param context the context.
     * @param list    the remote views.
     * @param items   the list of items.
     */
    protected abstract fun bindViews(
        context: Context,
        list: RemoteViews,
        items: List<ZmanimItem>
    )

    /**
     * Bind the item to remote views.
     *
     * @param context       the context.
     * @param list          the remote list.
     * @param position      the position index.
     * @param positionTotal the position index relative to all rows.
     * @param item          the zmanim item.
     */
    protected abstract fun bindView(
        context: Context,
        list: RemoteViews,
        position: Int,
        positionTotal: Int,
        item: ZmanimItem
    )

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    @IdRes
    protected abstract fun getIntentViewId(): Int

    protected fun notifyAppWidgetViewDataChanged(context: Context) {
        notifyAppWidgetViewDataChanged(context, javaClass, android.R.id.list)
    }

    protected open fun populateWidgetTimes(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        activityPendingIntent: PendingIntent,
        viewId: Int,
        day: Long
    ): ZmanimAdapter<ZmanViewHolder>? {
        return populateStaticTimes(context, views, activityPendingIntent, viewId, day)
    }

    private fun populateStaticTimes(
        context: Context,
        views: RemoteViews,
        activityPendingIntent: PendingIntent,
        viewId: Int,
        day: Long
    ): ZmanimAdapter<ZmanViewHolder>? {
        views.setOnClickPendingIntent(viewId, activityPendingIntent)
        val locations = getLocations(context)
        val gloc = locations.geoLocation ?: return null
        val preferences = preferences
        val populater: ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>> =
            ZmanimPopulater<ZmanimAdapter<ZmanViewHolder>>(context, preferences).apply {
                this.setCalendar(day)
                this.setGeoLocation(gloc)
                this.isInIsrael = locations.isInIsrael
            }

        val adapter = ZmanimAdapter<ZmanViewHolder>(context, preferences)
        populater.populate(adapter, true)

        val adapterTomorrow = ZmanimAdapter<ZmanViewHolder>(context, preferences)
        populater.setCalendar(day + DateUtils.DAY_IN_MILLIS)
        populater.populate(adapterTomorrow, true)
        bindViews(context, views, adapter, adapterTomorrow)

        return adapter
    }

    private fun getAppWidgetIds(context: Context): IntArray? {
        return getAppWidgetIds(context, javaClass)
    }

    @get:StyleRes
    protected val theme: Int
        get() = preferences.appWidgetTheme

    private fun getLocations(context: Context): ZmanimLocations {
        val app = context.applicationContext as ZmanimApplication
        return app.locations
    }

    private fun notifyAppWidgets(context: Context) {
        notifyAppWidgetsUpdate(context, javaClass)
    }

    protected val day: Long
        get() = System.currentTimeMillis()

    /**
     * Schedule the times to update at midnight, i.e. the next civil day.
     *
     * @param context      the context.
     * @param appWidgetIds the widget ids for which an update is needed.
     */
    private fun scheduleNextDay(context: Context, appWidgetIds: IntArray) {
        val time = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 1
        }.timeInMillis
        schedulePending(context, appWidgetIds, time, ID_WIDGET_MIDNIGHT)
    }

    companion object {
        /**
         * Id to update the widgets.
         */
        private const val ID_WIDGET_UPDATE = 10

        /**
         * Id to update the widgets at midnight.
         */
        private const val ID_WIDGET_MIDNIGHT = 11
    }
}
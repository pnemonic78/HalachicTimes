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
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.github.app.LocaleCallbacks
import com.github.app.isBrightWallpaper
import com.github.math.isOdd
import com.github.preference.LocalePreferences
import com.github.times.R
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimAdapter
import com.github.times.ZmanimApplication
import com.github.times.ZmanimDays.getName
import com.github.times.ZmanimItem
import com.github.times.ZmanimPopulater
import com.github.times.isNullOrEmpty
import com.github.times.location.ZmanimLocations
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.util.LocaleUtils.isLocaleRTL
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.util.Calendar

/**
 * Factory to create views for list widget.
 *
 * @author Moshe Waisberg
 */
class ZmanimWidgetViewsFactory(
    private var context: Context,
    private val localeCallbacks: LocaleCallbacks<LocalePreferences>
) : RemoteViewsFactory {

    private val packageName = context.packageName
    private val preferences: ZmanimPreferences = SimpleZmanimPreferences(context)
    private val items = mutableListOf<ZmanimItem>()
    private val isDirectionRTL: Boolean = isLocaleRTL(context)

    @ColorInt
    private var colorDisabled = Color.DKGRAY

    @ColorInt
    private var colorEnabled = Color.WHITE

    @StyleRes
    private var themeId = com.github.lib.R.style.Theme

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= items.size) {
            return null
        }
        val item = items[position]
        val view: RemoteViews
        if (item.isCategory) {
            view = RemoteViews(packageName, R.layout.widget_date)
            bindViewGrouping(view, item.timeLabel)
        } else {
            view = RemoteViews(packageName, getLayoutItemId(position))
            bindView(view, item)
        }
        return view
    }

    override fun getViewTypeCount(): Int {
        // Has category rows and odd rows.
        return 3
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        // Workaround to clear the asset manager cache.
        context = localeCallbacks.attachBaseContext(context)
        populateAdapter()
    }

    override fun onDestroy() = Unit

    private fun populateAdapter() {
        val context = context
        val locations = getLocations(context)
        val gloc = locations.geoLocation ?: return

        populateResources(context)

        val preferences = preferences
        val time = day
        val adapter = ZmanimAdapter<ZmanViewHolder>(context, preferences, null)
        ZmanimPopulater<ZmanimAdapter<*>>(context, preferences).apply {
            setCalendar(time)
            setGeoLocation(gloc)
            isInIsrael = locations.isInIsrael
            populate(adapter, false)
        }

        val items = mutableListOf<ZmanimItem>()
        var jcal = adapter.jewishCalendar!!
        val itemToday = ZmanimItem(adapter.formatDate(context, jcal))
        itemToday.jewishDate = jcal
        items.add(itemToday)

        val holidayToday = adapter.holidayToday
        val candlesToday = adapter.candlesTodayCount
        val holidayTodayName = getName(context, holidayToday, candlesToday)
        if (holidayTodayName != null) {
            ZmanimItem(holidayTodayName).apply {
                jewishDate = jcal
                items.add(this)
            }
        }

        // Sefirat HaOmer?
        var omer = adapter.dayOfOmerToday
        if (omer >= 1) {
            val omerLabel = adapter.formatOmer(context, omer)
            if (!omerLabel.isNullOrEmpty()) {
                ZmanimItem(omerLabel).apply {
                    jewishDate = jcal
                    items.add(this)
                }
            }
        }
        var itemTomorrow: ZmanimItem? = null
        val count = adapter.itemCount
        if (count > 0) {
            var jewishDate: JewishDate? = null
            var item: ZmanimItem?
            for (i in 0 until count) {
                item = adapter.getItem(i)
                if (item.isNullOrEmpty() || item.jewishDate == null) {
                    continue
                }
                if (jewishDate == null) {
                    jewishDate = item.jewishDate
                } else if (itemTomorrow == null && item.jewishDate != jewishDate) {
                    val zmanCal = adapter.calendar
                    jcal = JewishCalendar(zmanCal.calendar)
                    jcal.forward(Calendar.DATE, 1)
                    itemTomorrow = ZmanimItem(adapter.formatDate(context, jcal)).apply {
                        jewishDate = jcal
                        items.add(this)
                    }
                    val holidayTomorrow = adapter.holidayTomorrow
                    val candlesTomorrow = adapter.candlesCount
                    val holidayTomorrowName = getName(context, holidayTomorrow, candlesTomorrow)
                    if (holidayTomorrowName != null) {
                        ZmanimItem(holidayTomorrowName).apply {
                            jewishDate = jcal
                            items.add(this)
                        }
                    }

                    // Sefirat HaOmer?
                    omer = adapter.dayOfOmerTomorrow
                    if (omer >= 1) {
                        val omerLabel = adapter.formatOmer(context, omer)
                        if (!omerLabel.isNullOrEmpty()) {
                            ZmanimItem(omerLabel).apply {
                                jewishDate = jcal
                                items.add(this)
                            }
                        }
                    }
                }
                items.add(item)
            }
        }
        this.items.clear()
        this.items.addAll(items)
    }

    /**
     * Bind the item to the remote view.
     *
     * @param row      the remote list row.
     * @param item     the zman item.
     */
    private fun bindView(row: RemoteViews, item: ZmanimItem) {
        row.setTextViewText(R.id.title, item.title)
        row.setTextViewText(R.id.time, item.timeLabel)
        if (item.isElapsed) {
            // Using {@code row.setBoolean(id, "setEnabled", enabled)} throws error.
            row.setTextColor(R.id.title, colorDisabled)
            row.setTextColor(R.id.time, colorDisabled)
        } else {
            row.setTextColor(R.id.title, colorEnabled)
            row.setTextColor(R.id.time, colorEnabled)
        }
        // Enable clicking to open the main activity.
        row.setOnClickFillInIntent(R.id.widget_item, Intent())
        bindViewRowSpecial(row, item)
    }

    /**
     * Bind the date group header to a list.
     *
     * @param row      the remote list row.
     * @param label    the formatted Hebrew date label.
     */
    private fun bindViewGrouping(row: RemoteViews, label: CharSequence?) {
        if (label.isNullOrEmpty()) return
        row.setTextViewText(R.id.date_hebrew, label)
        row.setTextColor(R.id.date_hebrew, colorEnabled)
    }

    private fun bindViewRowSpecial(row: RemoteViews, item: ZmanimItem) {
        if (item.titleId == R.string.candles) {
            row.setInt(
                R.id.widget_item,
                "setBackgroundColor",
                ContextCompat.getColor(context, R.color.widget_candles_bg)
            )
        }
    }

    @get:StyleRes
    private val theme: Int
        get() = preferences.appWidgetTheme

    private fun populateResources(context: Context) {
        val themeId = theme
        if ((themeId == 0) || (themeId != this.themeId)) {
            this.themeId = themeId
            val isLight: Boolean = (themeId == THEME_APPWIDGET_LIGHT) || isBrightWallpaper(context)
            if (isLight) {
                colorEnabled = ContextCompat.getColor(context, R.color.widget_text_light)
                colorDisabled = ContextCompat.getColor(context, R.color.widget_text_disabled_light)
            } else {
                colorEnabled = ContextCompat.getColor(context, R.color.widget_text)
                colorDisabled = ContextCompat.getColor(context, R.color.widget_text_disabled)
            }
        }
    }

    private fun getLocations(context: Context): ZmanimLocations {
        val app = context.applicationContext as ZmanimApplication
        return app.locations
    }

    private val day: Long
        get() = System.currentTimeMillis()

    /**
     * Get the layout for the row item.
     *
     * @param position the position index.
     * @return the layout id.
     */
    @LayoutRes
    private fun getLayoutItemId(position: Int): Int {
        return if (position.isOdd) {
            if (isDirectionRTL) {
                R.layout.widget_item_odd_rtl
            } else {
                R.layout.widget_item_odd
            }
        } else if (isDirectionRTL) {
            R.layout.widget_item_rtl
        } else {
            R.layout.widget_item
        }
    }

    companion object {
        @StyleRes
        private val THEME_APPWIDGET_LIGHT = ZmanimAppWidget.THEME_APPWIDGET_LIGHT
    }
}
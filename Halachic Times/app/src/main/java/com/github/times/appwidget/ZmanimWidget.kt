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
import android.graphics.Color
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.github.app.isBrightWallpaper
import com.github.math.isOdd
import com.github.times.R
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimAdapter
import com.github.times.ZmanimDays.getName
import com.github.times.ZmanimItem
import com.github.times.isNullOrEmptyOrElapsed
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.util.Calendar

/**
 * Shows a list of halachic times (*zmanim*) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
open class ZmanimWidget : ZmanimAppWidget() {
    @ColorInt
    private var colorEnabled = Color.WHITE

    @ColorInt
    private var colorOdd = Color.TRANSPARENT

    @StyleRes
    private var themeId = com.github.lib.R.style.Theme

    override fun bindViews(
        context: Context,
        list: RemoteViews,
        adapterToday: ZmanimAdapter<ZmanViewHolder>,
        adapterTomorrow: ZmanimAdapter<ZmanViewHolder>
    ) {
        list.removeAllViews(android.R.id.list)
        populateResources(context)

        val jewishDateToday: JewishDate = adapterToday.jewishCalendar ?: return
        val countToday = adapterToday.itemCount
        val holidayToday = adapterToday.holidayToday
        val candlesToday = adapterToday.candlesTodayCount
        val omerToday = adapterToday.dayOfOmerToday
        val jewishDateTomorrow: JewishDate = adapterTomorrow.jewishCalendar ?: return
        val countTomorrow = adapterTomorrow.itemCount
        val holidayTomorrow = adapterToday.holidayTomorrow
        val candlesTomorrow = adapterToday.candlesCount
        val omerTomorrow = adapterTomorrow.dayOfOmerToday
        val jewishDateOvermorrow = (jewishDateTomorrow.clone() as JewishDate).apply {
            forward(Calendar.DATE, 1)
        }
        val holidayOvermorrow = adapterTomorrow.holidayTomorrow
        val candlesOvermorrow = adapterTomorrow.candlesCount
        val omerOvermorrow = adapterTomorrow.dayOfOmerTomorrow

        val items = mutableListOf<ZmanimItem>()
        // Ignore duplicates.
        val itemsAdded = mutableSetOf<Int>()
        var item: ZmanimItem?
        var position = 0
        while (position < countToday) {
            item = adapterToday.getItem(position)
            if (item.isNullOrEmptyOrElapsed()) {
                position++
                continue
            }
            items.add(item)
            itemsAdded.add(item.titleId)
            position++
        }
        position = 0
        while (position < countTomorrow) {
            item = adapterTomorrow.getItem(position)
            if (item.isNullOrEmptyOrElapsed()) {
                position++
                continue
            }
            if (itemsAdded.contains(item.titleId)) {
                break
            }
            items.add(item)
            position++
        }
        val count = items.size
        if (count == 0) return
        var positionToday = -1
        var positionTomorrow = -1
        var positionOvermorrow = -1
        position = 0
        while (position < count) {
            item = items[position]
            if (positionToday < 0 && jewishDateToday == item.jewishDate) {
                positionToday = position
            }
            if (positionTomorrow < 0 && jewishDateTomorrow == item.jewishDate) {
                positionTomorrow = position
            }
            if (positionOvermorrow < 0 && jewishDateOvermorrow == item.jewishDate) {
                positionOvermorrow = position
            }
            position++
        }
        if (positionOvermorrow >= 0) {
            bindViewsHeader(
                context,
                adapterTomorrow,
                items,
                positionOvermorrow,
                jewishDateOvermorrow,
                holidayOvermorrow,
                candlesOvermorrow,
                omerOvermorrow
            )
        }
        if (positionToday < 0 || positionTomorrow >= 0) {
            bindViewsHeader(
                context,
                adapterTomorrow,
                items,
                positionTomorrow,
                jewishDateTomorrow,
                holidayTomorrow,
                candlesTomorrow,
                omerTomorrow
            )
        }
        if (positionToday >= 0) {
            bindViewsHeader(
                context,
                adapterToday,
                items,
                positionToday,
                jewishDateToday,
                holidayToday,
                candlesToday,
                omerToday
            )
        }
        bindViews(context, list, items)
    }

    private fun bindViewsHeader(
        context: Context,
        adapter: ZmanimAdapter<*>,
        items: MutableList<ZmanimItem>,
        position: Int,
        jewishDate: JewishDate,
        holiday: Int,
        candles: Int,
        omer: Int
    ) {
        var index = position
        val itemToday = ZmanimItem(adapter.formatDate(context, jewishDate))
        itemToday.jewishDate = jewishDate
        items.add(index++, itemToday)

        val holidayTodayName = getName(context, holiday, candles)
        if (holidayTodayName != null) {
            ZmanimItem(holidayTodayName).apply {
                this.jewishDate = jewishDate
                items.add(index++, this)
            }
        }
        if (omer >= 1) {
            val omerLabel = adapter.formatOmer(context, omer)
            if (!omerLabel.isNullOrEmpty()) {
                ZmanimItem(omerLabel).apply {
                    this.jewishDate = jewishDate
                    items.add(index++, this)
                }
            }
        }
    }

    override fun bindViews(context: Context, list: RemoteViews, items: List<ZmanimItem>) {
        val count = items.size
        var item: ZmanimItem
        for (i in 0 until count) {
            item = items[i]
            if (item.isCategory) {
                bindViewGrouping(list, item.timeLabel)
            } else {
                if (item.isEmpty) continue
                bindView(context, list, i, count, item)
            }
        }
    }

    override fun bindView(
        context: Context,
        list: RemoteViews,
        position: Int,
        positionTotal: Int,
        item: ZmanimItem
    ) {
        if (item.isEmpty) {
            return
        }
        val pkg = context.packageName
        val row = RemoteViews(pkg, getLayoutItemId(position))
        row.setTextViewText(R.id.title, item.title)
        row.setTextViewText(R.id.time, item.timeLabel)
        row.setTextColor(R.id.title, colorEnabled)
        row.setTextColor(R.id.time, colorEnabled)
        bindViewRowSpecial(context, row, position, item)
        list.addView(android.R.id.list, row)
    }

    override fun getLayoutId(): Int {
        val theme = theme
        return when {
            theme == THEME_APPWIDGET_DARK -> R.layout.widget_static
            theme == THEME_APPWIDGET_LIGHT -> R.layout.widget_static_light
            isBrightWallpaper(context) -> R.layout.widget_static_light
            else -> R.layout.widget_static
        }
    }

    override fun getIntentViewId(): Int {
        return android.R.id.list
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list  the list.
     * @param label the formatted Hebrew date label.
     */
    private fun bindViewGrouping(list: RemoteViews, label: CharSequence?) {
        if (label.isNullOrEmpty()) return
        val pkg = context.packageName
        val row = RemoteViews(pkg, R.layout.widget_date)
        row.setTextViewText(R.id.date_hebrew, label)
        row.setTextColor(R.id.date_hebrew, colorEnabled)
        list.addView(android.R.id.list, row)
    }

    /**
     * Get the layout for the row item.
     *
     * @param position the position index.
     * @return the layout id.
     */
    @LayoutRes
    protected fun getLayoutItemId(position: Int): Int {
        if (position.isOdd) {
            return if (isDirectionRTL) R.layout.widget_item_odd_rtl else R.layout.widget_item_odd
        }
        return if (isDirectionRTL) R.layout.widget_item_rtl else R.layout.widget_item
    }

    private fun bindViewRowSpecial(
        context: Context,
        row: RemoteViews,
        position: Int,
        item: ZmanimItem
    ) {
        if (item.titleId == R.string.candles) {
            row.setInt(
                R.id.widget_item,
                "setBackgroundColor",
                ContextCompat.getColor(context, R.color.widget_candles_bg)
            )
        } else if (position.isOdd) {
            row.setInt(R.id.widget_item, "setBackgroundColor", colorOdd)
        }
    }

    private fun populateResources(context: Context) {
        val themeId = theme
        if (themeId != this.themeId) {
            this.themeId = themeId
            val isLight: Boolean = (themeId == THEME_APPWIDGET_LIGHT) || isBrightWallpaper(context)
            if (isLight) {
                colorEnabled = ContextCompat.getColor(context, R.color.widget_text_light)
                colorOdd = ContextCompat.getColor(context, R.color.widget_odd_light)
            } else {
                colorEnabled = ContextCompat.getColor(context, R.color.widget_text)
                colorOdd = ContextCompat.getColor(context, R.color.widget_odd)
            }
        }
    }

    companion object {
        @StyleRes
        private val THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark

        @StyleRes
        private val THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light
    }
}
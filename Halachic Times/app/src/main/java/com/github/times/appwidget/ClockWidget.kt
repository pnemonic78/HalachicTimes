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
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.github.app.isBrightWallpaper
import com.github.text.style.TypefaceSpan
import com.github.times.R
import com.github.times.ZmanViewHolder
import com.github.times.ZmanimAdapter
import com.github.times.ZmanimItem
import com.github.times.isNullOrEmptyOrElapsed
import com.github.util.TimeUtils.roundUp
import com.github.util.getDefaultLocale
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Clock widget with hour and title underneath.<br/>
 * Based on the default Android digital clock widget.
 *
 * @author Moshe Waisberg
 */
class ClockWidget : ZmanimAppWidget() {
    private val timeFormat: DateFormat by lazy {
        val time24 = android.text.format.DateFormat.is24HourFormat(context)
        @StringRes val time24Id =
            if (time24) R.string.clock_24_hours_format else R.string.clock_12_hours_format
        val pattern = context.getString(time24Id)
        SimpleDateFormat(pattern, context.getDefaultLocale())
    }

    override fun getLayoutId(): Int {
        val theme = this.theme
        return when {
            theme == THEME_APPWIDGET_DARK -> R.layout.clock_widget
            theme == THEME_APPWIDGET_LIGHT -> R.layout.clock_widget_light
            isBrightWallpaper(context) -> R.layout.clock_widget_light
            else -> R.layout.clock_widget
        }
    }

    override fun getIntentViewId(): Int {
        return R.id.date_gregorian
    }

    override fun bindViews(
        context: Context,
        list: RemoteViews,
        adapterToday: ZmanimAdapter<ZmanViewHolder>,
        adapterTomorrow: ZmanimAdapter<ZmanViewHolder>
    ) {
        var adapter = adapterToday
        var count = adapter.itemCount
        var item: ZmanimItem?
        val items = mutableListOf<ZmanimItem>()
        var position = 0
        while (position < count) {
            item = adapter.getItem(position)
            if (item.isNullOrEmptyOrElapsed()) {
                position++
                continue
            }
            items.add(item)
            break
        }
        if (items.isEmpty()) {
            adapter = adapterTomorrow
            count = adapter.itemCount
            position = 0
            while (position < count) {
                item = adapter.getItem(position)
                if (item.isNullOrEmptyOrElapsed()) {
                    position++
                    continue
                }
                items.add(item)
                break
            }
        }
        bindViews(context, list, items)
    }

    override fun bindViews(context: Context, list: RemoteViews, items: List<ZmanimItem>) {
        if (items.isEmpty()) return
        val item = items[0]
        bindView(context, list, 0, 0, item)
    }

    override fun bindView(
        context: Context,
        list: RemoteViews,
        position: Int,
        positionTotal: Int,
        item: ZmanimItem
    ) {
        val label: CharSequence = if (item.time != ZmanimItem.NEVER)
            timeFormat.format(roundUp(item.time, DateUtils.MINUTE_IN_MILLIS)) else ""
        val spans = SpannableStringBuilder.valueOf(label)
        val indexMinutes = label.indexOf(':')
        if (indexMinutes >= 0) {
            val spanStart = indexMinutes + 1
            val spanEnd = label.length
            spans.setSpan(
                TypefaceSpan(Typeface.SANS_SERIF),
                spanStart,
                spanEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            spans.setSpan(
                StyleSpan(Typeface.BOLD),
                spanStart,
                spanEnd,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        list.setTextViewText(R.id.time, spans)
        list.setTextViewText(R.id.title, item.title)
    }

    companion object {
        @StyleRes
        private val THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark

        @StyleRes
        private val THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light
    }
}
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

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.github.times.R;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimDays;
import com.github.times.ZmanimItem;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

import static com.github.graphics.BitmapUtils.isBrightWallpaper;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidget extends ZmanimAppWidget {

    @StyleRes
    private static final int THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark;
    @StyleRes
    private static final int THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light;

    @ColorInt
    private int colorEnabled = Color.WHITE;
    @StyleRes
    private int themeId = R.style.Theme;

    @Override
    protected void bindViews(Context context, RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow) {
        list.removeAllViews(android.R.id.list);

        populateResources(context);

        ZmanimAdapter adapter = adapterToday;
        int count = adapter.getCount();
        ZmanimItem item;
        JewishCalendar jcal = adapter.getJewishCalendar();
        JewishDate jewishDate = jcal;
        final List<ZmanimItem> items = new ArrayList<>();

        int positionFirst = -1;
        int positionSunset = -1;
        int position = 0;

        if (count > 0) {
            item = adapter.getItem(position);
            if (item != null) {
                if (!item.isEmptyOrElapsed()) {
                    positionFirst = position;
                }
                if (item.jewishDate != null) {
                    jewishDate = item.jewishDate;
                }
            }
        }
        for (position = 1; position < count; position++) {
            item = adapter.getItem(position);
            if ((item == null) || item.isEmpty()) {
                continue;
            }
            if ((positionSunset < 0) && isSunset(item, jewishDate)) {
                positionSunset = position;
            }
            if (item.isEmptyOrElapsed()) {
                continue;
            }
            if (positionFirst < 0) {
                positionFirst = position;
            }
        }

        ZmanimItem itemTomorrow = null;
        int positionTotal = 0;

        // If we have a sunset, then show today's header.
        if (positionFirst < positionSunset) {
            final ZmanimItem itemToday = new ZmanimItem(adapter.formatDate(context, jewishDate));
            itemToday.jewishDate = jewishDate;
            items.add(itemToday);

            int holidayToday = adapter.getHolidayToday();
            int candlesToday = adapter.getCandlesTodayCount();
            CharSequence holidayTodayName = ZmanimDays.getName(context, holidayToday, candlesToday);
            if (holidayTodayName != null) {
                final ZmanimItem itemHoliday = new ZmanimItem(holidayTodayName);
                itemHoliday.jewishDate = jewishDate;
                items.add(itemHoliday);
            }

            int omer = jcal.getDayOfOmer();
            if (omer >= 1) {
                CharSequence omerLabel = adapter.formatOmer(context, omer);
                if (!TextUtils.isEmpty(omerLabel)) {
                    final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                    itemOmer.jewishDate = jcal;
                    items.add(itemOmer);
                }
            }
        }

        for (position = 0; position < count; position++, positionTotal++) {
            item = adapter.getItem(position);
            if ((item == null) || item.isEmptyOrElapsed()) {
                continue;
            }

            items.add(item);

            // Start of the next Hebrew day.
            if ((position >= positionSunset) && (itemTomorrow == null)) {
                jewishDate.forward(Calendar.DATE, 1);
                jcal.forward(Calendar.DATE, 1);

                itemTomorrow = new ZmanimItem(adapter.formatDate(context, jewishDate));
                itemTomorrow.jewishDate = jcal;
                items.add(itemTomorrow);

                int holidayTomorrow = adapter.getHolidayTomorrow();
                int candleCount = adapter.getCandlesCount();
                CharSequence holidayTomorrowName = ZmanimDays.getName(context, holidayTomorrow, candleCount);
                if (holidayTomorrowName != null) {
                    final ZmanimItem labelHoliday = new ZmanimItem(holidayTomorrowName);
                    labelHoliday.jewishDate = jcal;
                    items.add(labelHoliday);
                }

                int omer = jcal.getDayOfOmer();
                if (omer >= 1) {
                    CharSequence omerLabel = adapter.formatOmer(context, omer);
                    if (!TextUtils.isEmpty(omerLabel)) {
                        final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                        itemOmer.jewishDate = jcal;
                        items.add(itemOmer);
                    }
                }
            }
        }

        if (positionFirst < 0) {
            positionFirst = positionTotal;
        }
        if ((adapterTomorrow != null) && (positionFirst >= 0)) {
            adapter = adapterTomorrow;
            count = Math.min(adapter.getCount(), positionFirst);
            itemTomorrow = null;

            if (positionSunset < positionFirst) {
                for (position = 0; position < count; position++) {
                    item = adapter.getItem(position);
                    if ((item == null) || item.isEmptyOrElapsed()) {
                        continue;
                    }
                    if (isSunset(item, jewishDate)) {
                        positionSunset = position;
                        break;
                    }
                }
            }

            for (position = 0; position < count; position++, positionTotal++) {
                item = adapter.getItem(position);
                if ((item == null) || item.isEmptyOrElapsed()) {
                    continue;
                }

                items.add(item);

                // Start of the next Hebrew day.
                if ((position >= positionSunset) && (itemTomorrow == null)) {
                    jewishDate.forward(Calendar.DATE, 1);
                    jcal.forward(Calendar.DATE, 1);

                    itemTomorrow = new ZmanimItem(adapter.formatDate(context, jewishDate));
                    itemTomorrow.jewishDate = jcal;
                    items.add(itemTomorrow);

                    int holidayTomorrow = adapter.getHolidayTomorrow();
                    int candleCount = adapter.getCandlesCount();
                    CharSequence holidayTomorrowName = ZmanimDays.getName(context, holidayTomorrow, candleCount);
                    if (holidayTomorrowName != null) {
                        final ZmanimItem labelHoliday = new ZmanimItem(holidayTomorrowName);
                        labelHoliday.jewishDate = jcal;
                        items.add(labelHoliday);
                    }

                    int omer = jcal.getDayOfOmer();
                    if (omer >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omer);
                        if (!TextUtils.isEmpty(omerLabel)) {
                            final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                            itemOmer.jewishDate = jcal;
                            items.add(itemOmer);
                        }
                    }
                }
            }
        }

        bindViews(context, list, items);

    }

    @Override
    protected void bindViews(Context context, RemoteViews list, List<ZmanimItem> items) {
        final int count = items.size();
        ZmanimItem item;
        for (int i = 0; i < count; i++) {
            item = items.get(i);
            if (item.isCategory()) {
                bindViewGrouping(list, i, item.timeLabel);
            } else {
                bindView(context, list, i, count, item);
            }
        }
    }

    @Override
    protected boolean bindView(Context context, RemoteViews list, int position, int positionTotal, @Nullable ZmanimItem item) {
        if ((item == null) || item.isEmpty()) {
            return false;
        }
        String pkg = context.getPackageName();
        RemoteViews row = new RemoteViews(pkg, getLayoutItemId(position));
        row.setTextViewText(R.id.title, item.title);
        row.setTextViewText(R.id.time, item.timeLabel);
        row.setTextColor(R.id.title, colorEnabled);
        row.setTextColor(R.id.time, colorEnabled);
        bindViewRowSpecial(context, row, position, item);
        list.addView(android.R.id.list, row);
        return true;
    }

    @Override
    protected int getLayoutId() {
        switch (getTheme()) {
            case THEME_APPWIDGET_DARK:
                return R.layout.widget_static;
            case THEME_APPWIDGET_LIGHT:
                return R.layout.widget_static_light;
            default:
                if (isBrightWallpaper(getContext())) {
                    return R.layout.widget_static_light;
                }
                return R.layout.widget_static;
        }
    }

    @Override
    protected int getIntentViewId() {
        return android.R.id.list;
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list     the list.
     * @param position the position index.
     * @param label    the formatted Hebrew date label.
     */
    protected void bindViewGrouping(RemoteViews list, int position, CharSequence label) {
        if ((position < 0) || (label == null)) {
            return;
        }
        final Context context = getContext();
        String pkg = context.getPackageName();
        RemoteViews row = new RemoteViews(pkg, R.layout.widget_date);
        row.setTextViewText(R.id.date_hebrew, label);
        row.setTextColor(R.id.date_hebrew, colorEnabled);
        list.addView(android.R.id.list, row);
    }

    /**
     * Get the layout for the row item.
     *
     * @param position the position index.
     * @return the layout id.
     */
    @LayoutRes
    protected int getLayoutItemId(int position) {
        if ((position & 1) == 1) {
            return directionRTL ? R.layout.widget_item_odd_rtl : R.layout.widget_item_odd;
        }
        return directionRTL ? R.layout.widget_item_rtl : R.layout.widget_item;
    }

    protected void bindViewRowSpecial(Context context, RemoteViews row, int position, ZmanimItem item) {
        if (item.titleId == R.string.candles) {
            row.setInt(R.id.widget_item, "setBackgroundColor", ContextCompat.getColor(context, R.color.widget_candles_bg));
        }
    }

    private void populateResources(Context context) {
        final int themeId = getTheme();
        if (themeId != this.themeId) {
            this.themeId = themeId;

            boolean light;
            switch (themeId) {
                case THEME_APPWIDGET_DARK:
                    light = false;
                    break;
                case THEME_APPWIDGET_LIGHT:
                    light = true;
                    break;
                default:
                    light = isBrightWallpaper(context);
                    break;
            }

            int colorEnabledDark = Color.WHITE;
            int colorEnabledLight = Color.BLACK;
            try {
                colorEnabledDark = ContextCompat.getColor(context, R.color.widget_text);
                colorEnabledLight = ContextCompat.getColor(context, R.color.widget_text_light);
            } catch (Exception e) {
                Timber.e(e);
            }
            this.colorEnabled = light ? colorEnabledLight : colorEnabledDark;
        }
    }

    private boolean isSunset(ZmanimItem item, JewishDate jewishDate) {
        return (item.titleId == R.string.sunset) || ((item.jewishDate != null) && !item.jewishDate.equals(jewishDate));
    }
}

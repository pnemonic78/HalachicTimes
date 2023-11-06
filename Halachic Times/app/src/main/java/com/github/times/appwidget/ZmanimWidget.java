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

import static com.github.graphics.BitmapUtils.isBrightWallpaper;

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
import com.github.times.ZmanViewHolder;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimDays;
import com.github.times.ZmanimItem;
import com.kosherjava.zmanim.hebrewcalendar.JewishDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

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
    private int themeId = com.github.lib.R.style.Theme;

    @Override
    protected void bindViews(Context context, RemoteViews list, ZmanimAdapter<ZmanViewHolder> adapterToday, ZmanimAdapter<ZmanViewHolder> adapterTomorrow) {
        list.removeAllViews(android.R.id.list);

        populateResources(context);

        final JewishDate jewishDateToday = adapterToday.getJewishCalendar();
        final int countToday = adapterToday.getItemCount();
        final int holidayToday = adapterToday.getHolidayToday();
        final int candlesToday = adapterToday.getCandlesTodayCount();
        final int omerToday = adapterToday.getDayOfOmerToday();

        final JewishDate jewishDateTomorrow = adapterTomorrow.getJewishCalendar();
        final int countTomorrow = adapterTomorrow.getItemCount();
        final int holidayTomorrow = adapterToday.getHolidayTomorrow();
        final int candlesTomorrow = adapterToday.getCandlesCount();
        final int omerTomorrow = adapterTomorrow.getDayOfOmerToday();

        final JewishDate jewishDateOvermorrow = ((JewishDate) jewishDateTomorrow.clone());
        jewishDateOvermorrow.forward(Calendar.DATE, 1);
        final int holidayOvermorrow = adapterTomorrow.getHolidayTomorrow();
        final int candlesOvermorrow = adapterTomorrow.getCandlesCount();
        final int omerOvermorrow = adapterTomorrow.getDayOfOmerTomorrow();

        final List<ZmanimItem> items = new ArrayList<>();
        // Ignore duplicates.
        final Set<Integer> itemsAdded = new HashSet<>();
        int position;
        ZmanimItem item;

        for (position = 0; position < countToday; position++) {
            item = adapterToday.getItem(position);
            if ((item == null) || item.isEmptyOrElapsed()) {
                continue;
            }
            items.add(item);
            itemsAdded.add(item.titleId);
        }

        for (position = 0; position < countTomorrow; position++) {
            item = adapterTomorrow.getItem(position);
            if ((item == null) || item.isEmptyOrElapsed()) {
                continue;
            }
            if (itemsAdded.contains(item.titleId)) {
                break;
            }
            items.add(item);
        }

        final int count = items.size();
        if (count == 0) return;

        int positionToday = -1;
        int positionTomorrow = -1;
        int positionOvermorrow = -1;

        for (position = 0; position < count; position++) {
            item = items.get(position);

            if ((positionToday < 0) && jewishDateToday.equals(item.jewishDate)) {
                positionToday = position;
            }
            if ((positionTomorrow < 0) && jewishDateTomorrow.equals(item.jewishDate)) {
                positionTomorrow = position;
            }
            if ((positionOvermorrow < 0) && jewishDateOvermorrow.equals(item.jewishDate)) {
                positionOvermorrow = position;
            }
        }

        if (positionOvermorrow >= 0) {
            bindViewsHeader(context, adapterTomorrow, items, positionOvermorrow, jewishDateOvermorrow, holidayOvermorrow, candlesOvermorrow, omerOvermorrow);
        }
        if ((positionToday < 0) || (positionTomorrow >= 0)) {
            bindViewsHeader(context, adapterTomorrow, items, positionTomorrow, jewishDateTomorrow, holidayTomorrow, candlesTomorrow, omerTomorrow);
        }
        if (positionToday >= 0) {
            bindViewsHeader(context, adapterToday, items, positionToday, jewishDateToday, holidayToday, candlesToday, omerToday);
        }

        bindViews(context, list, items);
    }

    private void bindViewsHeader(
            Context context,
            ZmanimAdapter adapter,
            List<ZmanimItem> items,
            int position,
            JewishDate jewishDate,
            int holiday,
            int candles,
            int omer
    ) {
        int index = position;

        final ZmanimItem itemToday = new ZmanimItem(adapter.formatDate(context, jewishDate));
        itemToday.jewishDate = jewishDate;
        items.add(index++, itemToday);

        CharSequence holidayTodayName = ZmanimDays.getName(context, holiday, candles);
        if (holidayTodayName != null) {
            final ZmanimItem itemHoliday = new ZmanimItem(holidayTodayName);
            itemHoliday.jewishDate = jewishDate;
            items.add(index++, itemHoliday);
        }

        if (omer >= 1) {
            CharSequence omerLabel = adapter.formatOmer(context, omer);
            if (!TextUtils.isEmpty(omerLabel)) {
                final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                itemOmer.jewishDate = jewishDate;
                items.add(index++, itemOmer);
            }
        }
    }

    @Override
    protected void bindViews(Context context, RemoteViews list, List<ZmanimItem> items) {
        final int count = items.size();
        ZmanimItem item;
        for (int i = 0; i < count; i++) {
            item = items.get(i);
            if (item.isCategory()) {
                bindViewGrouping(list, item.timeLabel);
            } else {
                bindView(context, list, i, count, item);
            }
        }
    }

    @Override
    protected void bindView(Context context, RemoteViews list, int position, int positionTotal, @Nullable ZmanimItem item) {
        if ((item == null) || item.isEmpty()) {
            return;
        }
        String pkg = context.getPackageName();
        RemoteViews row = new RemoteViews(pkg, getLayoutItemId(position));
        row.setTextViewText(R.id.title, item.title);
        row.setTextViewText(R.id.time, item.timeLabel);
        row.setTextColor(R.id.title, colorEnabled);
        row.setTextColor(R.id.time, colorEnabled);
        bindViewRowSpecial(context, row, position, item);
        list.addView(android.R.id.list, row);
    }

    @Override
    protected int getLayoutId() {
        int theme = getTheme();
        if (theme == THEME_APPWIDGET_DARK) {
            return R.layout.widget_static;
        }
        if (theme == THEME_APPWIDGET_LIGHT) {
            return R.layout.widget_static_light;
        }
        if (isBrightWallpaper(getContext())) {
            return R.layout.widget_static_light;
        }
        return R.layout.widget_static;
    }

    @Override
    protected int getIntentViewId() {
        return android.R.id.list;
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list     the list.
     * @param label    the formatted Hebrew date label.
     */
    protected void bindViewGrouping(RemoteViews list, CharSequence label) {
        if (TextUtils.isEmpty(label)) {
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
            if (themeId == THEME_APPWIDGET_DARK) {
                light = false;
            } else if (themeId == THEME_APPWIDGET_LIGHT) {
                light = true;
            } else {
                light = isBrightWallpaper(context);
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
}

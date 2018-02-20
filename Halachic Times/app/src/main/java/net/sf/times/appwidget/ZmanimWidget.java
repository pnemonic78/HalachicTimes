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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import net.sf.times.R;
import net.sf.times.ZmanimAdapter;
import net.sf.times.ZmanimItem;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import static net.sf.graphics.BitmapUtils.isBrightWallpaper;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidget extends ZmanimAppWidget {

    @ColorInt
    private int colorEnabled = Color.WHITE;

    @Override
    protected void bindViews(Context context, RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow) {
        list.removeAllViews(android.R.id.list);

        boolean light;
        switch (getTheme()) {
            case R.style.Theme_AppWidget_Dark:
                light = false;
                break;
            case R.style.Theme_AppWidget_Light:
                light = true;
                break;
            default:
                light = !isBrightWallpaper(context);
                break;
        }
        final Resources res = context.getResources();
        this.colorEnabled = light ? res.getColor(R.color.widget_text_light) : res.getColor(R.color.widget_text);

        ZmanimAdapter adapter = adapterToday;
        int count = adapter.getCount();
        ZmanimItem item;
        JewishCalendar jcal = adapter.getJewishCalendar();
        JewishDate jewishDate = jcal;

        int positionFirst = -1;
        int positionSunset = -1;

        if (count > 0) {
            item = adapter.getItem(0);
            if (item != null) {
                if (!item.isEmpty()) {
                    positionFirst = 0;
                }
                if (item.jewishDate != null) {
                    jewishDate = item.jewishDate;
                }
            }
        }
        for (int position = 1; position < count; position++) {
            item = adapter.getItem(position);
            if ((item == null) || item.isEmpty()) {
                continue;
            }
            if (positionFirst < 0) {
                positionFirst = position;
            }
            if ((item.jewishDate != null) && !item.jewishDate.equals(jewishDate)) {
                positionSunset = position - 1;
                break;
            }
        }

        int positionTomorrow = -1;
        int positionTotal = 0;
        CharSequence dateHebrew, groupingText;

        // If we have a sunset, then show today's header.
        if (positionSunset >= positionFirst) {
            dateHebrew = adapter.formatDate(context, jewishDate);
            groupingText = dateHebrew;
            bindViewGrouping(list, 0, groupingText);
        }

        for (int position = 0; position < count; position++, positionTotal++) {
            item = adapter.getItem(position);
            bindView(context, list, position, positionTotal, item);

            // Start of the next Hebrew day.
            if ((position >= positionSunset) && (positionTomorrow < 0)) {
                positionTomorrow = position;
                jewishDate.forward();
                dateHebrew = adapter.formatDate(context, jewishDate);
                groupingText = dateHebrew;
                int omer = jcal.getDayOfOmer();
                if (omer >= 1) {
                    CharSequence omerLabel = adapter.formatOmer(context, omer);
                    if (!TextUtils.isEmpty(omerLabel)) {
                        groupingText = TextUtils.concat(groupingText, "\n", omerLabel);
                    }
                }
                bindViewGrouping(list, position, groupingText);
            }
        }

        if ((adapterTomorrow != null) && (positionFirst >= 0)) {
            adapter = adapterTomorrow;
            count = Math.min(adapter.getCount(), positionFirst);
            positionTomorrow = -1;

            if (positionSunset < positionFirst) {
                for (int position = 0; position < count; position++) {
                    item = adapter.getItem(position);
                    if ((item == null) || item.isEmpty()) {
                        continue;
                    }
                    if ((item.jewishDate != null) && !item.jewishDate.equals(jewishDate)) {
                        positionSunset = position - 1;
                        break;
                    }
                }
            }

            for (int position = 0; position < count; position++, positionTotal++) {
                item = adapter.getItem(position);
                bindView(context, list, position, positionTotal, item);

                // Start of the next Hebrew day.
                if ((position >= positionSunset) && (positionTomorrow < 0)) {
                    positionTomorrow = position;
                    jewishDate.forward();
                    dateHebrew = adapter.formatDate(context, jewishDate);
                    groupingText = dateHebrew;
                    int omer = jcal.getDayOfOmer();
                    if (omer >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omer);
                        if (!TextUtils.isEmpty(omerLabel)) {
                            groupingText = TextUtils.concat(groupingText, "\n", omerLabel);
                        }
                    }
                    bindViewGrouping(list, position, groupingText);
                }
            }
        }
    }

    @Override
    protected boolean bindView(Context context, RemoteViews list, int position, int positionTotal, @Nullable ZmanimItem item) {
        if ((item == null) || item.isEmpty()) {
            return false;
        }
        String pkg = context.getPackageName();
        RemoteViews row = new RemoteViews(pkg, getLayoutItemId(positionTotal));
        row.setTextViewText(android.R.id.title, context.getText(item.titleId));
        row.setTextViewText(R.id.time, item.timeLabel);
        row.setTextColor(android.R.id.title, colorEnabled);
        row.setTextColor(R.id.time, colorEnabled);
        bindViewRowSpecial(context, row, position, item);
        list.addView(android.R.id.list, row);
        return true;
    }

    @Override
    protected int getLayoutId() {
        switch (getTheme()) {
            case R.style.Theme_AppWidget_Dark:
                return R.layout.widget_static;
            case R.style.Theme_AppWidget_Light:
                return R.layout.widget_static_light;
            default:
                if (isBrightWallpaper(getContext())) {
                    return R.layout.widget_static;
                }
                return R.layout.widget_static_light;
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
    protected int getLayoutItemId(int position) {
        return ((position & 1) == 1) ? R.layout.widget_item_odd : R.layout.widget_item;
    }

    protected void bindViewRowSpecial(Context context, RemoteViews row, int position, ZmanimItem item) {
        if (item.titleId == R.string.candles) {
            row.setInt(R.id.widget_item, "setBackgroundColor", context.getResources().getColor(R.color.widget_candles_bg));
        }
    }
}

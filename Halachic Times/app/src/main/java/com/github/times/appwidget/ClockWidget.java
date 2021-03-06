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
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;

import androidx.annotation.StyleRes;

import com.github.text.style.TypefaceSpan;
import com.github.times.R;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimItem;
import com.github.util.LocaleUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static com.github.graphics.BitmapUtils.isBrightWallpaper;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.TimeUtils.roundUp;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 *
 * @author Moshe Waisberg
 */
public class ClockWidget extends ZmanimAppWidget {

    @StyleRes
    private static final int THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark;
    @StyleRes
    private static final int THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light;

    private final ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            final Context context = getContext();
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.clock_24_hours_format : R.string.clock_12_hours_format);
            return new SimpleDateFormat(pattern, LocaleUtils.getDefaultLocale(context));
        }
    };

    @Override
    protected int getLayoutId() {
        switch (getTheme()) {
            case THEME_APPWIDGET_DARK:
                return R.layout.clock_widget;
            case THEME_APPWIDGET_LIGHT:
                return R.layout.clock_widget_light;
            default:
                if (isBrightWallpaper(getContext())) {
                    return R.layout.clock_widget_light;
                }
                return R.layout.clock_widget;
        }
    }

    @Override
    protected int getIntentViewId() {
        return R.id.date_gregorian;
    }

    @Override
    protected void bindViews(Context context, RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow) {
        ZmanimAdapter adapter = adapterToday;
        int count = adapter.getCount();
        ZmanimItem item;
        boolean found = false;
        int positionTotal = 0;
        List<ZmanimItem> items = new ArrayList<>(1);

        for (int position = 0; position < count; position++, positionTotal++) {
            item = adapter.getItem(position);
            if ((item == null) || item.isEmptyOrElapsed()) {
                continue;
            }
            items.add(item);
            found = true;
            break;
        }

        if (!found && (adapterTomorrow != null)) {
            adapter = adapterTomorrow;
            count = adapter.getCount();
            for (int position = 0; position < count; position++, positionTotal++) {
                item = adapter.getItem(position);
                if ((item == null) || item.isEmptyOrElapsed()) {
                    continue;
                }
                items.add(item);
                break;
            }
        }

        bindViews(context, list, items);
    }

    @Override
    protected void bindViews(Context context, RemoteViews list, List<ZmanimItem> items) {
        ZmanimItem item = items.get(0);
        bindView(context, list, 0, 0, item);
    }

    @Override
    protected boolean bindView(Context context, RemoteViews list, int position, int positionTotal, ZmanimItem item) {
        CharSequence label = item.time != NEVER ? getTimeFormat().format(roundUp(item.time, MINUTE_IN_MILLIS)) : "";
        SpannableStringBuilder spans = SpannableStringBuilder.valueOf(label);
        int indexMinutes = TextUtils.indexOf(label, ':');
        if (indexMinutes >= 0) {
            int spanStart = indexMinutes + 1;
            int spanEnd = label.length();
            spans.setSpan(new TypefaceSpan(Typeface.SANS_SERIF), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spans.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        list.setTextViewText(R.id.time, spans);
        list.setTextViewText(R.id.title, item.title);
        return true;
    }

    @Override
    protected void notifyAppWidgetViewDataChanged(Context context) {
        formatter.remove();
        super.notifyAppWidgetViewDataChanged(context);
    }

    /**
     * Get the time formatter.
     *
     * @return the formatter.
     */
    protected DateFormat getTimeFormat() {
        return formatter.get();
    }
}

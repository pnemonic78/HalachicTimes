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

import net.sf.text.style.TypefaceSpan;
import com.github.times.R;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimItem;
import com.github.util.LocaleUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
            case R.style.Theme_AppWidget_Dark:
                return R.layout.clock_widget_light;
            case R.style.Theme_AppWidget_Light:
                return R.layout.clock_widget;
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

        for (int position = 0; position < count; position++, positionTotal++) {
            item = adapter.getItem(position);
            if (item.isEmpty())
                continue;
            bindView(context, list, position, positionTotal, item);
            found = true;
            break;
        }

        if (!found && (adapterTomorrow != null)) {
            adapter = adapterTomorrow;
            count = adapter.getCount();
            for (int position = 0; position < count; position++, positionTotal++) {
                item = adapter.getItem(position);
                if (item.isEmpty())
                    continue;
                bindView(context, list, position, positionTotal, item);
                break;
            }
        }
    }

    @Override
    protected boolean bindView(Context context, RemoteViews list, int position, int positionTotal, ZmanimItem item) {
        CharSequence label = item.time != NEVER ? getTimeFormat().format(roundUp(item.time, MINUTE_IN_MILLIS)) : "";
        SpannableStringBuilder spans = SpannableStringBuilder.valueOf(label);
        int indexMinutes = TextUtils.indexOf(label, ':');
        spans.setSpan(new TypefaceSpan(Typeface.SANS_SERIF), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spans.setSpan(new StyleSpan(Typeface.BOLD), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        list.setTextViewText(R.id.time, spans);
        list.setTextViewText(android.R.id.title, context.getText(item.titleId));
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

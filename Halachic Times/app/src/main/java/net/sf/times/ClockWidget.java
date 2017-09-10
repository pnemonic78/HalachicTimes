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
package net.sf.times;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;

import net.sf.graphics.BitmapUtils;
import net.sf.text.style.TypefaceSpan;
import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.util.LocaleUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static net.sf.times.ZmanimAdapter.NEVER;
import static net.sf.util.TimeUtils.roundUp;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 *
 * @author Moshe
 */
public class ClockWidget extends ZmanimWidget {

    private final ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            Context context = getContext();
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.clock_24_hours_format : R.string.clock_12_hours_format);
            return new SimpleDateFormat(pattern, LocaleUtils.getDefaultLocale(context));
        }
    };

    /**
     * Constructs a new widget.
     */
    public ClockWidget() {
    }

    @Override
    protected int getLayoutId() {
        int bg = BitmapUtils.getWallpaperColor(getContext());
        if ((bg != Color.TRANSPARENT) && BitmapUtils.isBright(bg)) {
            return R.layout.clock_widget_light;
        }

        return R.layout.clock_widget;
    }

    @Override
    protected int getIntentViewId() {
        return R.id.date_gregorian;
    }

    @Override
    protected void bindViews(RemoteViews list, ZmanimAdapter adapterToday, ZmanimAdapter adapterTomorrow) {
        ZmanimAdapter adapter = adapterToday;
        int count = adapter.getCount();
        ZmanimItem item;
        boolean found = false;
        int positionTotal = 0;

        for (int position = 0; position < count; position++, positionTotal++) {
            item = adapter.getItem(position);
            if (item.isEmpty())
                continue;
            bindView(list, position, positionTotal, item);
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
                bindView(list, position, positionTotal, item);
                break;
            }
        }
    }

    @Override
    protected boolean bindView(RemoteViews list, int position, int positionTotal, ZmanimItem item) {
        CharSequence label = item.time != NEVER ? getTimeFormat().format(roundUp(item.time, SECOND_IN_MILLIS)) : "";
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case Intent.ACTION_WALLPAPER_CHANGED:
                notifyAppWidgetViewDataChanged(context);
                break;
        }
    }
}

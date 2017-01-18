/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 *
 * @author Moshe
 */
public class ClockWidget extends ZmanimWidget {

    private DateFormat timeFormat;

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
        DateFormat timeFormat = getTimeFormat();
        CharSequence label = timeFormat.format(item.time);
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
        timeFormat = null;
        super.notifyAppWidgetViewDataChanged(context);
    }

    /**
     * Get the time formatter.
     *
     * @return the formatter.
     */
    protected DateFormat getTimeFormat() {
        if (timeFormat == null) {
            Context context = getContext();
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.clock_24_hours_format : R.string.clock_12_hours_format);
            timeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        }
        return timeFormat;
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

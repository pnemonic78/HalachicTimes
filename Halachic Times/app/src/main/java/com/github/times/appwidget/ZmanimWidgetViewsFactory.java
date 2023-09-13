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
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.github.app.LocaleHelper;
import com.github.times.R;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimApplication;
import com.github.times.ZmanimDays;
import com.github.times.ZmanimItem;
import com.github.times.ZmanimPopulater;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishDate;
import com.kosherjava.zmanim.util.GeoLocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

import static com.github.graphics.BitmapUtils.isBrightWallpaper;
import static java.lang.System.currentTimeMillis;

/**
 * Factory to create views for list widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidgetViewsFactory implements RemoteViewsFactory {

    @StyleRes
    private static final int THEME_APPWIDGET_DARK = R.style.Theme_AppWidget_Dark;
    @StyleRes
    private static final int THEME_APPWIDGET_LIGHT = R.style.Theme_AppWidget_Light;

    /**
     * The context.
     */
    private Context context;
    /**
     * The preferences.
     */
    private ZmanimPreferences preferences;
    /**
     * The adapter.
     */
    private ZmanimAdapter adapter;
    private final List<ZmanimItem> items = new ArrayList<>();
    @ColorInt
    private int colorDisabled = Color.DKGRAY;
    @ColorInt
    private int colorEnabled = Color.WHITE;
    @StyleRes
    private int themeId = com.github.lib.R.style.Theme;
    private final LocaleHelper<?> localeCallbacks;
    private final boolean directionRTL;

    public ZmanimWidgetViewsFactory(Context context, Intent intent) {
        this.localeCallbacks = new LocaleHelper<>(context);
        this.context = localeCallbacks.attachBaseContext(context);
        this.directionRTL = LocaleUtils.isLocaleRTL(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if ((position < 0) || (position >= items.size())) {
            return null;
        }

        final Context context = this.context;
        final String pkg = context.getPackageName();

        ZmanimItem item = items.get(position);
        if (item == null) {
            return null;
        }

        RemoteViews view;
        if (item.isCategory()) {
            view = new RemoteViews(pkg, R.layout.widget_date);
            bindViewGrouping(view, position, item.timeLabel);
        } else {
            view = new RemoteViews(pkg, getLayoutItemId(position));
            bindView(view, position, item);
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        // Has category rows and odd rows.
        return 3;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        this.context = localeCallbacks.attachBaseContext(context);//Workaround to clear the asset manager cache.
        populateAdapter();
    }

    @Override
    public void onDestroy() {
    }

    protected ZmanimPreferences getPreferences() {
        ZmanimPreferences preferences = this.preferences;
        if (preferences == null) {
            preferences = new SimpleZmanimPreferences(context);
            this.preferences = preferences;
        }
        return preferences;
    }

    private void populateAdapter() {
        final Context context = this.context;

        ZmanimLocations locations = getLocations(context);
        GeoLocation gloc = locations.getGeoLocation();
        if (gloc == null) {
            return;
        }

        populateResources(context);

        ZmanimPreferences preferences = getPreferences();
        long day = getDay();

        ZmanimPopulater populater = new ZmanimPopulater(context, preferences);
        populater.setCalendar(day);
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        // Always create new adapter to avoid concurrency bugs.
        ZmanimAdapter adapter = new ZmanimAdapter(context, preferences);
        populater.populate(adapter, false);
        this.adapter = adapter;

        List<ZmanimItem> items = new ArrayList<>();

        JewishCalendar jcal = adapter.getJewishCalendar();
        final ZmanimItem itemToday = new ZmanimItem(adapter.formatDate(context, jcal));
        itemToday.jewishDate = jcal;
        items.add(itemToday);

        int holidayToday = adapter.getHolidayToday();
        int candlesToday = adapter.getCandlesTodayCount();
        CharSequence holidayTodayName = ZmanimDays.getName(context, holidayToday, candlesToday);
        if (holidayTodayName != null) {
            final ZmanimItem itemHoliday = new ZmanimItem(holidayTodayName);
            itemHoliday.jewishDate = jcal;
            items.add(itemHoliday);
        }

        // Sefirat HaOmer?
        int omer = adapter.getDayOfOmerToday();
        if (omer >= 1) {
            CharSequence omerLabel = adapter.formatOmer(context, omer);
            if (!TextUtils.isEmpty(omerLabel)) {
                final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                itemOmer.jewishDate = jcal;
                items.add(itemOmer);
            }
        }

        ZmanimItem itemTomorrow = null;

        final int count = adapter.getCount();
        if (count > 0) {
            JewishDate jewishDate = null;
            ZmanimItem item;
            for (int i = 0; i < count; i++) {
                item = adapter.getItem(i);
                if ((item == null) || item.isEmpty() || (item.jewishDate == null)) {
                    continue;
                }
                if (jewishDate == null) {
                    jewishDate = item.jewishDate;
                } else if ((itemTomorrow == null) && !item.jewishDate.equals(jewishDate)) {
                    ComplexZmanimCalendar zmanCal = adapter.getCalendar();
                    jcal = new JewishCalendar(zmanCal.getCalendar());
                    jcal.forward(Calendar.DATE, 1);

                    itemTomorrow = new ZmanimItem(adapter.formatDate(context, jcal));
                    itemTomorrow.jewishDate = jcal;
                    items.add(itemTomorrow);

                    int holidayTomorrow = adapter.getHolidayTomorrow();
                    int candlesTomorrow = adapter.getCandlesCount();
                    CharSequence holidayTomorrowName = ZmanimDays.getName(context, holidayTomorrow, candlesTomorrow);
                    if (holidayTomorrowName != null) {
                        final ZmanimItem labelHoliday = new ZmanimItem(holidayTomorrowName);
                        labelHoliday.jewishDate = jcal;
                        items.add(labelHoliday);
                    }

                    // Sefirat HaOmer?
                    omer = adapter.getDayOfOmerTomorrow();
                    if (omer >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omer);
                        if (!TextUtils.isEmpty(omerLabel)) {
                            final ZmanimItem itemOmer = new ZmanimItem(omerLabel);
                            itemOmer.jewishDate = jcal;
                            items.add(itemOmer);
                        }
                    }
                }

                items.add(item);
            }
        }

        this.items.clear();
        this.items.addAll(items);
    }

    /**
     * Bind the item to the remote view.
     *
     * @param row      the remote list row.
     * @param position the position index.
     * @param item     the zman item.
     */
    private void bindView(RemoteViews row, int position, ZmanimItem item) {
        row.setTextViewText(R.id.title, item.title);
        row.setTextViewText(R.id.time, item.timeLabel);
        if (item.elapsed) {
            // Using {@code row.setBoolean(id, "setEnabled", enabled)} throws error.
            row.setTextColor(R.id.title, colorDisabled);
            row.setTextColor(R.id.time, colorDisabled);
        } else {
            row.setTextColor(R.id.title, colorEnabled);
            row.setTextColor(R.id.time, colorEnabled);
        }
        // Enable clicking to open the main activity.
        row.setOnClickFillInIntent(R.id.widget_item, new Intent());
        bindViewRowSpecial(row, position, item);
    }

    /**
     * Bind the date group header to a list.
     *
     * @param row      the remote list row.
     * @param position the position index.
     * @param label    the formatted Hebrew date label.
     */
    private void bindViewGrouping(RemoteViews row, int position, CharSequence label) {
        row.setTextViewText(R.id.date_hebrew, label);
        row.setTextColor(R.id.date_hebrew, colorEnabled);
    }

    protected void bindViewRowSpecial(RemoteViews row, int position, ZmanimItem item) {
        if (item.titleId == R.string.candles) {
            row.setInt(R.id.widget_item, "setBackgroundColor", ContextCompat.getColor(context, R.color.widget_candles_bg));
        }
    }

    @StyleRes
    protected int getTheme() {
        return getPreferences().getAppWidgetTheme();
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
            int colorDisabledDark = Color.DKGRAY;
            int colorDisabledLight = Color.DKGRAY;
            try {
                colorEnabledDark = ContextCompat.getColor(context, R.color.widget_text);
                colorEnabledLight = ContextCompat.getColor(context, R.color.widget_text_light);
                colorDisabledDark = ContextCompat.getColor(context, R.color.widget_text_disabled);
                colorDisabledLight = ContextCompat.getColor(context, R.color.widget_text_disabled_light);
            } catch (Exception e) {
                Timber.e(e);
            }
            if (light) {
                this.colorEnabled = colorEnabledLight;
                this.colorDisabled = colorDisabledLight;
            } else {
                this.colorEnabled = colorEnabledDark;
                this.colorDisabled = colorDisabledDark;
            }
        }
    }

    private ZmanimLocations getLocations(Context context) {
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        return app.getLocations();
    }

    private long getDay() {
        return currentTimeMillis();
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
}

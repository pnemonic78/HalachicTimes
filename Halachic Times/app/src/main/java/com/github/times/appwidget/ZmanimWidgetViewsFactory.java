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

import com.github.app.LocaleHelper;
import com.github.times.R;
import com.github.times.ZmanimAdapter;
import com.github.times.ZmanimApplication;
import com.github.times.ZmanimItem;
import com.github.times.ZmanimPopulater;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

import static android.widget.AdapterView.INVALID_POSITION;
import static com.github.graphics.BitmapUtils.isBrightWallpaper;
import static java.lang.System.currentTimeMillis;

/**
 * Factory to create views for list widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidgetViewsFactory implements RemoteViewsFactory {

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
    /**
     * Position index of today's Hebrew day.
     */
    private int positionToday = 0;
    /**
     * Position index of next Hebrew day.
     */
    private int positionTomorrow = -1;
    @ColorInt
    private int colorDisabled = Color.DKGRAY;
    @ColorInt
    private int colorEnabled = Color.WHITE;
    @StyleRes
    private int themeId = R.style.Theme;
    private final LocaleHelper localeCallbacks;
    private boolean directionRTL;
    @LayoutRes
    private int layoutItemId = R.layout.widget_item;

    public ZmanimWidgetViewsFactory(Context context, Intent intent) {
        this.localeCallbacks = new LocaleHelper<>(context);
        this.context = localeCallbacks.attachBaseContext(context);
        this.directionRTL = LocaleUtils.isLocaleRTL(context);
    }

    @Override
    public int getCount() {
        return (positionToday >= 0 ? 1 : 0) + (positionTomorrow > positionToday ? 1 : 0) + ((adapter != null) ? adapter.getCount() : 0);
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
        if (position == INVALID_POSITION) {
            return null;
        }

        final ZmanimAdapter adapter = this.adapter;
        if (adapter == null) {
            return null;
        }

        final Context context = this.context;
        final String pkg = context.getPackageName();
        RemoteViews view;

        if ((position == positionToday) || (position == positionTomorrow)) {
            ComplexZmanimCalendar zmanCal = adapter.getCalendar();
            JewishCalendar jcal = new JewishCalendar(zmanCal.getCalendar());
            if (position == positionTomorrow) {
                jcal.forward(Calendar.DATE, 1);
            }
            CharSequence groupingText = adapter.formatDate(context, jcal);

            // Sefirat HaOmer?
            if (position == positionTomorrow) {
                int omer = jcal.getDayOfOmer();
                if (omer >= 1) {
                    CharSequence omerLabel = adapter.formatOmer(context, omer);
                    if (!TextUtils.isEmpty(omerLabel)) {
                        groupingText = TextUtils.concat(groupingText, "\n", omerLabel);
                    }
                }
            }

            view = new RemoteViews(pkg, R.layout.widget_date);
            bindViewGrouping(view, position, groupingText);
            return view;
        }

        // discount for "today" row.
        if ((positionToday >= 0) && (position >= positionToday)) {
            position--;
        }
        // discount for "tomorrow" row.
        if ((positionTomorrow > 0) && (position >= positionTomorrow)) {
            position--;
        }
        if ((position < 0) || (position >= adapter.getCount())) {
            return null;
        }

        ZmanimItem item = adapter.getItem(position);
        view = new RemoteViews(pkg, layoutItemId);
        bindView(view, position, item);
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1 + ((adapter != null) ? adapter.getViewTypeCount() : 1);
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

        ZmanimPopulater populater = new ZmanimPopulater(context, preferences);
        populater.setCalendar(currentTimeMillis());
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        // Always create new adapter to avoid concurrency bugs.
        ZmanimAdapter adapter = new ZmanimAdapter(context, preferences);
        populater.populate(adapter, false);
        this.adapter = adapter;

        int positionToday = 0;
        int positionTomorrow = -1;
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
                    continue;
                }
                if (!item.jewishDate.equals(jewishDate)) {
                    positionTomorrow = i + 1;
                    break;
                }
            }
        }
        this.positionToday = positionToday;
        this.positionTomorrow = positionTomorrow;
    }

    /**
     * Bind the item to the remote view.
     *
     * @param row      the remote list row.
     * @param position the position index.
     * @param item     the zman item.
     */
    private void bindView(RemoteViews row, int position, ZmanimItem item) {
        row.setTextViewText(android.R.id.title, context.getText(item.titleId));
        row.setTextViewText(R.id.time, item.timeLabel);
        if (item.elapsed) {
            // Using {@code row.setBoolean(id, "setEnabled", enabled)} throws error.
            row.setTextColor(android.R.id.title, colorDisabled);
            row.setTextColor(R.id.time, colorDisabled);
        } else {
            row.setTextColor(android.R.id.title, colorEnabled);
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
        } else {
            row.setInt(R.id.widget_item, "setBackgroundColor", Color.TRANSPARENT);
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
            switch (themeId) {
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
                this.layoutItemId = directionRTL ? R.layout.widget_item_light_rtl : R.layout.widget_item_light;
            } else {
                this.colorEnabled = colorEnabledDark;
                this.colorDisabled = colorDisabledDark;
                this.layoutItemId = directionRTL ? R.layout.widget_item_rtl : R.layout.widget_item;
            }
        }
    }

    private ZmanimLocations getLocations(Context context) {
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        return app.getLocations();
    }
}

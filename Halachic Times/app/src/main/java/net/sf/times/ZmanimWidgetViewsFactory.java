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
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

/**
 * Factory to create views for list widget.
 *
 * @author Moshe
 */
public class ZmanimWidgetViewsFactory implements RemoteViewsFactory, ZmanimLocationListener {

    /** The context. */
    private final Context context;
    /** Provider for locations. */
    private ZmanimLocations locations;
    /** The settings and preferences. */
    private ZmanimPreferences settings;
    /** The adapter. */
    private ZmanimAdapter adapter;
    /** Position index of today's Hebrew day. */
    private int positionToday;
    /** Position index of next Hebrew day. */
    private int positionTomorrow;
    private int colorDisabled = Color.DKGRAY;
    private int colorEnabled = Color.WHITE;

    public ZmanimWidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return (adapter == null) ? 0 : (positionToday >= 0 ? 1 : 0) + adapter.getCount() + (positionTomorrow > 0 ? 1 : 0);
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
        final ZmanimAdapter adapter = this.adapter;
        if (adapter == null) {
            return null;
        }

        Context context = this.context;
        String pkg = context.getPackageName();
        RemoteViews view;

        if ((position == positionToday) || (position == positionTomorrow)) {
            ComplexZmanimCalendar zmanCal = adapter.getCalendar();
            JewishCalendar jewishDate = new JewishCalendar(zmanCal.getCalendar());
            if (position == positionTomorrow) {
                jewishDate.forward();
            }
            CharSequence dateHebrew = adapter.formatDate(context, jewishDate);
            CharSequence groupingText = dateHebrew;

            // Sefirat HaOmer?
            if (position == positionTomorrow) {
                int omer = jewishDate.getDayOfOmer();
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
        position--;
        // discount for "tomorrow" row.
        if ((positionTomorrow > 0) && (position >= positionTomorrow))
            position--;

        if ((position < 0) || (position >= adapter.getCount())) {
            return null;
        }

        ZmanimItem item = adapter.getItem(position);
        view = new RemoteViews(pkg, R.layout.widget_item);
        bindView(view, position, item);
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1 + ((adapter == null) ? 0 : adapter.getViewTypeCount());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        if (locations != null)
            locations.start(this);
    }

    @Override
    public void onDataSetChanged() {
        populateAdapter();
    }

    @Override
    public void onDestroy() {
        if (locations != null)
            locations.stop(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        onDataSetChanged();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        onDataSetChanged();
    }

    @Override
    public void onElevationChanged(Location location) {
        onDataSetChanged();
    }

    @Override
    public boolean isPassive() {
        return true;
    }

    private void populateAdapter() {
        Context context = this.context;

        if (settings == null)
            settings = new ZmanimPreferences(context);

        ZmanimLocations locations = this.locations;
        if (locations == null) {
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
            locations.start(this);
            this.locations = locations;
        }
        GeoLocation gloc = locations.getGeoLocation();
        if (gloc == null)
            return;

        ZmanimPopulater populater = new ZmanimPopulater(context, settings);
        populater.setCalendar(System.currentTimeMillis());
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        // Always create new adapter to avoid concurrency bugs.
        ZmanimAdapter adapter = new ZmanimAdapter(context, settings);
        populater.populate(adapter, false);
        this.adapter = adapter;

        positionToday = 0;
        positionTomorrow = -1;
        ZmanimItem item;
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            item = adapter.getItem(i);
            if (item.titleId == R.string.sunset) {
                positionTomorrow = i + (positionToday >= 0 ? 1 : 0) + 1;
                break;
            }
        }
    }

    /**
     * Bind the item to the remote view.
     *
     * @param row
     *         the remote list row.
     * @param position
     *         the position index.
     * @param item
     *         the zman item.
     */
    private void bindView(RemoteViews row, int position, ZmanimItem item) {
        row.setTextViewText(android.R.id.title, context.getText(item.titleId));
        row.setTextViewText(R.id.time, item.timeLabel);
        // FIXME - the application must notify the widget that "past times" has changed.
        if (item.elapsed) {
            // Using {@code row.setBoolean(id, "setEnabled", enabled)} throws
            // error.
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
     * @param row
     *         the remote list row.
     * @param position
     *         the position index.
     * @param label
     *         the formatted Hebrew date label.
     */
    private void bindViewGrouping(RemoteViews row, int position, CharSequence label) {
        row.setTextViewText(R.id.date_hebrew, label);
    }

    protected void bindViewRowSpecial(RemoteViews row, int position, ZmanimItem item) {
        if (item.titleId == R.string.candles) {
            Context context = this.context;
            row.setInt(R.id.widget_item, "setBackgroundColor", context.getResources().getColor(R.color.widget_candles_bg));
        } else {
            row.setInt(R.id.widget_item, "setBackgroundColor", Color.TRANSPARENT);
        }
    }
}

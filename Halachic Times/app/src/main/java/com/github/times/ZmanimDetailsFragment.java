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
package com.github.times;

import static com.github.view.ViewUtils.applyMaxWidth;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsFragment<A extends ZmanimDetailsAdapter, P extends ZmanimDetailsPopulater<A>> extends ZmanimFragment<A, P> {

    /**
     * The master id.
     */
    private int masterId;

    /**
     * Get the master id for populating the details.
     *
     * @return the master id.
     */
    public int getMasterId() {
        return masterId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected A createAdapter(@NonNull Context context) {
        if ((masterId == 0) || (context == null)) {
            return null;
        }

        return (A) new ZmanimDetailsAdapter(context, preferences);
    }

    @Override
    public A populateTimes(@NonNull Calendar date) {
        return populateTimes(date, masterId);
    }

    /**
     * Populate the list with detailed times.
     *
     * @param date the date.
     * @param id   the time id.
     */
    public A populateTimes(@NonNull Calendar date, int id) {
        masterId = id;
        if (!isAdded()) {
            return null;
        }

        P populater = getPopulater();
        A adapter = null;

        if (populater != null) {
            populater.setItemId(id);
            adapter = super.populateTimes(date);
        }

        final int theme = preferences.getTheme();
        if (theme == R.style.Theme_Zmanim_Dark) {
            setBackgroundColorDark(id, list);
        } else if (theme == R.style.Theme_Zmanim_Light) {
            setBackgroundColorLight(id, list);
       } else if (theme == R.style.Theme_Zmanim_DayNight) {
            final Context context = list.getContext();
            final int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    setBackgroundColorLight(id, list);
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    setBackgroundColorDark(id, list);
                    break;
            }
        } else {
            list.setBackgroundColor(Color.TRANSPARENT);
        }

        return adapter;
    }

    private void setBackgroundColorDark(int id, ViewGroup list) {
        final Context context = getContextImpl();
        Resources res = context.getResources();

        if (id == R.string.dawn) {
            list.setBackgroundColor(res.getColor(R.color.dawn));
        } else if (id == R.string.tallis || id == R.string.tallis_only) {
            list.setBackgroundColor(res.getColor(R.color.tallis));
        } else if (id == R.string.sunrise) {
            list.setBackgroundColor(res.getColor(R.color.sunrise));
        } else if (id == R.string.shema) {
            list.setBackgroundColor(res.getColor(R.color.shema));
        } else if (id == R.string.prayers || id == R.string.eat_chametz || id == R.string.burn_chametz) {
            list.setBackgroundColor(res.getColor(R.color.prayers));
        } else if (id == R.string.midday) {
            list.setBackgroundColor(res.getColor(R.color.midday));
        } else if (id == R.string.earliest_mincha) {
            list.setBackgroundColor(res.getColor(R.color.earliest_mincha));
        } else if (id == R.string.mincha) {
            list.setBackgroundColor(res.getColor(R.color.mincha));
        } else if (id == R.string.plug_hamincha) {
            list.setBackgroundColor(res.getColor(R.color.plug_hamincha));
        } else if (id == R.string.sunset) {
            list.setBackgroundColor(res.getColor(R.color.sunset));
        } else if (id == R.string.twilight) {
            list.setBackgroundColor(res.getColor(R.color.twilight));
        } else if (id == R.string.nightfall || id == R.string.shabbath_ends || id == R.string.festival_ends) {
            list.setBackgroundColor(res.getColor(R.color.nightfall));
        } else if (id == R.string.midnight_guard || id == R.string.midnight || id == R.string.morning_guard) {
            list.setBackgroundColor(res.getColor(R.color.midnight));

            list.setBackgroundColor(Color.TRANSPARENT);
        } else {
            list.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setBackgroundColorLight(int id, ViewGroup list) {
        final Context context = getContextImpl();
        Resources res = context.getResources();

        if (id == R.string.dawn) {
            list.setBackgroundColor(res.getColor(R.color.dawn_light));
        } else if (id == R.string.tallis || id == R.string.tallis_only) {
            list.setBackgroundColor(res.getColor(R.color.tallis_light));
        } else if (id == R.string.sunrise) {
            list.setBackgroundColor(res.getColor(R.color.sunrise_light));
        } else if (id == R.string.shema) {
            list.setBackgroundColor(res.getColor(R.color.shema_light));
        } else if (id == R.string.prayers || id == R.string.eat_chametz || id == R.string.burn_chametz) {
            list.setBackgroundColor(res.getColor(R.color.prayers_light));
        } else if (id == R.string.midday) {
            list.setBackgroundColor(res.getColor(R.color.midday_light));
        } else if (id == R.string.earliest_mincha) {
            list.setBackgroundColor(res.getColor(R.color.earliest_mincha_light));
        } else if (id == R.string.mincha) {
            list.setBackgroundColor(res.getColor(R.color.mincha_light));
        } else if (id == R.string.plug_hamincha) {
            list.setBackgroundColor(res.getColor(R.color.plug_hamincha_light));
        } else if (id == R.string.sunset) {
            list.setBackgroundColor(res.getColor(R.color.sunset_light));
        } else if (id == R.string.twilight) {
            list.setBackgroundColor(res.getColor(R.color.twilight_light));
        } else if (id == R.string.nightfall || id == R.string.shabbath_ends || id == R.string.festival_ends) {
            list.setBackgroundColor(res.getColor(R.color.nightfall_light));
        } else if (id == R.string.midnight_guard || id == R.string.midnight || id == R.string.morning_guard) {
            list.setBackgroundColor(res.getColor(R.color.midnight_light));
        } else {
            list.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void setOnClickListener(View view, ZmanimItem item) {
        // No clicking allowed.
    }

    @Override
    protected void bindViews(final ViewGroup list, A adapter) {
        if (list == null)
            return;
        list.removeAllViews();
        if (adapter == null)
            return;

        final Context context = list.getContext();
        if (context == null)
            return;

        JewishCalendar jcal = adapter.getJewishCalendar();
        if (jcal == null) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        final Calendar gcal = (Calendar) jcal.getGregorianCalendar().clone();
        CharSequence dateHebrew;
        JewishDate jewishDatePrevious = null;
        JewishDate jewishDate;

        final int count = adapter.getCount();
        ZmanimItem item;
        View row;
        final List<View> timeViews = new ArrayList<>(count);

        for (int position = 0; position < count; position++) {
            item = adapter.getItem(position);
            if (item == null) {
                continue;
            }

            if ((jewishDatePrevious == null) || ((item.jewishDate != null) && !jewishDatePrevious.equals(item.jewishDate))) {
                if (item.jewishDate != null) {
                    jewishDate = item.jewishDate;
                } else if (item.isEmptyOrElapsed()) {
                    continue;
                } else {
                    gcal.setTimeInMillis(item.time);
                    jcal.setDate(gcal);
                    jewishDate = jcal;
                }
                jewishDatePrevious = jewishDate;
                dateHebrew = adapter.formatDate(context, jewishDate);
                bindViewGrouping(list, dateHebrew);
            }

            row = adapter.getView(position, null, list);
            timeViews.add(row.findViewById(R.id.time));
            bindView(list, position, row, item);
        }

        list.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                list.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Make all time texts same width.
                try {
                    applyMaxWidth(timeViews);
                } catch (NullPointerException e) {
                    throw new NullPointerException("null object reference for " + context.getString(masterId) + " on " + gcal);
                }
            }
        });
    }

    @Override
    @NonNull
    protected P createPopulater(@NonNull Context context) {
        return (P) new ZmanimDetailsPopulater<A>(context, preferences);
    }
}

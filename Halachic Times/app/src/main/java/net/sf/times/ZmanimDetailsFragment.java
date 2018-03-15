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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

import java.util.Calendar;

/**
 * Shows a list of all opinions for a halachic time (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class ZmanimDetailsFragment<A extends ZmanimDetailsAdapter, P extends ZmanimDetailsPopulater<A>> extends ZmanimFragment<A, P> {

    /** The master id. */
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
    protected A createAdapter(Context context) {
        if ((masterId == 0) || (context == null)) {
            return null;
        }

        return (A) new ZmanimDetailsAdapter(context, preferences);
    }

    @Override
    public A populateTimes(Calendar date) {
        return populateTimes(date, masterId);
    }

    /**
     * Populate the list with detailed times.
     *
     * @param date the date.
     * @param id   the time id.
     */
    public A populateTimes(Calendar date, int id) {
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

        Resources res = getContextImpl().getResources();

        switch (preferences.getTheme()) {
            case R.style.Theme_Zmanim_Dark:
                switch (id) {
                    case R.string.dawn:
                        list.setBackgroundColor(res.getColor(R.color.dawn));
                        break;
                    case R.string.tallis:
                    case R.string.tallis_only:
                        list.setBackgroundColor(res.getColor(R.color.tallis));
                        break;
                    case R.string.sunrise:
                        list.setBackgroundColor(res.getColor(R.color.sunrise));
                        break;
                    case R.string.shema:
                        list.setBackgroundColor(res.getColor(R.color.shema));
                        break;
                    case R.string.prayers:
                    case R.string.eat_chametz:
                    case R.string.burn_chametz:
                        list.setBackgroundColor(res.getColor(R.color.prayers));
                        break;
                    case R.string.midday:
                        list.setBackgroundColor(res.getColor(R.color.midday));
                        break;
                    case R.string.earliest_mincha:
                        list.setBackgroundColor(res.getColor(R.color.earliest_mincha));
                        break;
                    case R.string.mincha:
                        list.setBackgroundColor(res.getColor(R.color.mincha));
                        break;
                    case R.string.plug_hamincha:
                        list.setBackgroundColor(res.getColor(R.color.plug_hamincha));
                        break;
                    case R.string.sunset:
                        list.setBackgroundColor(res.getColor(R.color.sunset));
                        break;
                    case R.string.twilight:
                        list.setBackgroundColor(res.getColor(R.color.twilight));
                        break;
                    case R.string.nightfall:
                    case R.string.shabbath_ends:
                    case R.string.festival_ends:
                        list.setBackgroundColor(res.getColor(R.color.nightfall));
                        break;
                    case R.string.midnight_guard:
                    case R.string.midnight:
                    case R.string.morning_guard:
                        list.setBackgroundColor(res.getColor(R.color.midnight));
                    default:
                        list.setBackgroundColor(Color.TRANSPARENT);
                        break;
                }
                break;
            case R.style.Theme_Zmanim_Light:
                switch (id) {
                    case R.string.dawn:
                        list.setBackgroundColor(res.getColor(R.color.dawn_light));
                        break;
                    case R.string.tallis:
                    case R.string.tallis_only:
                        list.setBackgroundColor(res.getColor(R.color.tallis_light));
                        break;
                    case R.string.sunrise:
                        list.setBackgroundColor(res.getColor(R.color.sunrise_light));
                        break;
                    case R.string.shema:
                        list.setBackgroundColor(res.getColor(R.color.shema_light));
                        break;
                    case R.string.prayers:
                    case R.string.eat_chametz:
                    case R.string.burn_chametz:
                        list.setBackgroundColor(res.getColor(R.color.prayers_light));
                        break;
                    case R.string.midday:
                        list.setBackgroundColor(res.getColor(R.color.midday_light));
                        break;
                    case R.string.earliest_mincha:
                        list.setBackgroundColor(res.getColor(R.color.earliest_mincha_light));
                        break;
                    case R.string.mincha:
                        list.setBackgroundColor(res.getColor(R.color.mincha_light));
                        break;
                    case R.string.plug_hamincha:
                        list.setBackgroundColor(res.getColor(R.color.plug_hamincha_light));
                        break;
                    case R.string.sunset:
                        list.setBackgroundColor(res.getColor(R.color.sunset_light));
                        break;
                    case R.string.twilight:
                        list.setBackgroundColor(res.getColor(R.color.twilight_light));
                        break;
                    case R.string.nightfall:
                    case R.string.shabbath_ends:
                    case R.string.festival_ends:
                        list.setBackgroundColor(res.getColor(R.color.nightfall_light));
                        break;
                    case R.string.midnight_guard:
                    case R.string.midnight:
                    case R.string.morning_guard:
                        list.setBackgroundColor(res.getColor(R.color.midnight_light));
                        break;
                    default:
                        list.setBackgroundColor(Color.TRANSPARENT);
                        break;
                }
                break;
            case R.style.Theme_Zmanim_NoGradient:
            case R.style.Theme_Zmanim_White:
            default:
                list.setBackgroundColor(Color.TRANSPARENT);
                break;
        }

        return adapter;
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

        final Context context = getContextImpl();
        if (context == null)
            return;

        JewishCalendar jcal = adapter.getJewishCalendar();
        if (jcal == null) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        Calendar gcal = (Calendar) jcal.getGregorianCalendar().clone();
        CharSequence dateHebrew;
        JewishDate jewishDatePrevious = null;
        JewishDate jewishDate = jcal;

        final int count = adapter.getCount();
        ZmanimItem item;
        View row;
        final View[] timeViews = new View[count];

        for (int position = 0; position < count; position++) {
            item = adapter.getItem(position);
            if (item == null) {
                continue;
            }

            if ((jewishDatePrevious == null) || ((item.jewishDate != null) && !jewishDatePrevious.equals(item.jewishDate))) {
                if (item.jewishDate != null) {
                    jewishDate = item.jewishDate;
                } else if (item.isEmpty()) {
                    continue;
                } else {
                    gcal.setTimeInMillis(item.time);
                    jcal.setDate(gcal);
                    jewishDate = jcal;
                }
                jewishDatePrevious = jewishDate;
                dateHebrew = adapter.formatDate(context, jewishDate);
                bindViewGrouping(list, position, dateHebrew);
            }

            row = adapter.getView(position, null, list);
            timeViews[position] = row.findViewById(R.id.time);
            bindView(list, position, row, item);
        }

        list.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                list.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // Make all time texts same width.
                int maxWidth = 0;
                for (View view : timeViews) {
                    maxWidth = Math.max(maxWidth, view.getMeasuredWidth());
                }
                for (View view : timeViews) {
                    view.setMinimumWidth(maxWidth);
                }
            }
        });
    }

    @Override
    protected P createPopulater(Context context) {
        if (context == null) {
            return null;
        }
        return (P) new ZmanimDetailsPopulater<A>(context, preferences);
    }
}

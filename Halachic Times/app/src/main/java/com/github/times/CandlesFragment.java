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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.EREV_YOM_KIPPUR;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
public class CandlesFragment extends ZmanimFragment<CandlesAdapter, CandlesPopulater> {

    private static final int[] SHABBAT_CANDLES = {R.id.candle_1, R.id.candle_2};
    private static final int[] YOM_KIPPURIM_CANDLES = {R.id.candle_1};
    private static final int[] CHANNUKA_CANDLES = {R.id.candle_1, R.id.candle_2, R.id.candle_3, R.id.candle_4, R.id.candle_5, R.id.candle_6, R.id.candle_7, R.id.candle_8};

    /**
     * The candles view for Shabbat.
     */
    private ViewGroup candlesShabbat;
    /**
     * The candles view for Channuka.
     */
    private ViewGroup candlesChannuka;
    /**
     * The candles view for Yom Kippurim.
     */
    private ViewGroup candlesKippurim;
    /**
     * The flaming candle animations.
     */
    private CandleView[] animations;
    /**
     * The flaming candle animations.
     */
    private CandleView[] animationsShabbat;
    /**
     * The flaming candle animations.
     */
    private CandleView[] animationsChannuka;
    /**
     * The flaming candle animations.
     */
    private CandleView[] animationsKippurim;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Ignore the list inside of the scroller.
        this.list = (ViewGroup) view;
    }

    @Override
    protected CandlesAdapter createAdapter(Context context) {
        if (context == null) {
            return null;
        }
        return new CandlesAdapter(context, preferences);
    }

    @Override
    protected CandlesPopulater createPopulater(Context context) {
        if (context == null) {
            return null;
        }
        return new CandlesPopulater(context, preferences);
    }

    @Override
    protected void bindViews(ViewGroup list, CandlesAdapter adapter) {
        if (list == null)
            return;
        list.removeAllViews();
        if (adapter == null)
            return;

        final int holiday = adapter.getCandlesHoliday();
        final int candlesCount = adapter.getCandlesCount();
        final boolean animate = preferences.isCandlesAnimated();
        CandleView view;
        ViewGroup group = null;

        switch (holiday) {
            case EREV_YOM_KIPPUR:
            case YOM_KIPPUR:
                group = (ViewGroup) adapter.getView(holiday, candlesKippurim, list);
                if (candlesKippurim == null) {
                    candlesKippurim = group;

                    // assert candlesCount == YOM_KIPPURIM_CANDLES.length;
                    animationsKippurim = new CandleView[candlesCount];
                    for (int i = 0; i < candlesCount; i++) {
                        view = group.findViewById(YOM_KIPPURIM_CANDLES[i]);
                        animationsKippurim[i] = view;
                    }
                }
                list.addView(group);
                animations = animationsKippurim;
                break;
            case CHANUKAH:
                group = (ViewGroup) adapter.getView(holiday, candlesChannuka, list);
                if (candlesChannuka == null) {
                    candlesChannuka = group;

                    // create all candles in case user navigates to future day.
                    final int allCandlesCount = CHANNUKA_CANDLES.length;
                    // assert candlesCount <= allCandlesCount;
                    animationsChannuka = new CandleView[allCandlesCount + 1];
                    for (int i = 0; i < allCandlesCount; i++) {
                        view = group.findViewById(CHANNUKA_CANDLES[i]);
                        animationsChannuka[i] = view;
                    }
                    view = group.findViewById(R.id.candle_shamash);
                    animationsChannuka[allCandlesCount] = view;
                }
                list.addView(group);
                animations = animationsChannuka;
                // Only show relevant candles.
                for (int i = 0; i < candlesCount; i++) {
                    animations[i].setVisibility(View.VISIBLE);
                }
                for (int i = candlesCount; i < CHANNUKA_CANDLES.length; i++) {
                    animations[i].setVisibility(View.INVISIBLE);
                }
                break;
            default:
                if (candlesCount > 0) {
                    group = (ViewGroup) adapter.getView(holiday, candlesShabbat, list);
                    if (candlesShabbat == null) {
                        candlesShabbat = group;

                        // assert candlesCount == SHABBAT_CANDLES.length;
                        animationsShabbat = new CandleView[candlesCount];
                        for (int i = 0; i < candlesCount; i++) {
                            view = group.findViewById(SHABBAT_CANDLES[i]);
                            animationsShabbat[i] = view;
                        }
                    }
                    list.addView(group);
                    animations = animationsShabbat;
                }
                break;
        }

        if (group != null) {
            group.setVisibility(View.VISIBLE);
        }

        setFlickers(animations, animate);
    }

    private void setFlickers(CandleView[] candles, boolean enabled) {
        if (candles == null)
            return;
        for (CandleView candle : candles) {
            if (candle == null)
                continue;
            candle.flicker(enabled);
        }
    }

    @Override
    protected void setOnClickListener(View view, ZmanimItem item) {
        // No clicking allowed.
    }
}
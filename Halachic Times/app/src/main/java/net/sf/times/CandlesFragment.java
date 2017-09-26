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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import net.sf.times.ZmanimAdapter.ZmanimItem;

import java.util.Calendar;
import java.util.Random;

import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
public class CandlesFragment extends ZmanimFragment<CandlesAdapter, CandlesPopulater> implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int[] SHABBAT_CANDLES = {R.id.candle_1, R.id.candle_2};
    private static final int[] YOM_KIPPURIM_CANDLES = {R.id.candle_1};
    private static final int[] CHANNUKA_CANDLES = {R.id.candle_1, R.id.candle_2, R.id.candle_3, R.id.candle_4, R.id.candle_5, R.id.candle_6, R.id.candle_7, R.id.candle_8};

    /** The candles view for Shabbat. */
    private ViewGroup candlesShabbat;
    /** The candles view for Channuka. */
    private ViewGroup candlesChannuka;
    /** The candles view for Yom Kippurim. */
    private ViewGroup candlesKippurim;
    /** The animation scheduler. */
    private final Handler handler = new Handler();
    /** The flaming candle animations. */
    private CandleAnimation[] animations;
    /** The flaming candle animations. */
    private CandleAnimation[] animationsShabbat;
    /** The flaming candle animations. */
    private CandleAnimation[] animationsChannuka;
    /** The flaming candle animations. */
    private CandleAnimation[] animationsKippurim;
    /** Randomizer. */
    private final Random random = new Random();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
        // Ignore the list inside of the scroller.
        this.list = (ViewGroup) view;
    }

    @Override
    public void onDestroyView() {
        getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopAnimation();
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
    public CandlesAdapter populateTimes(Calendar date) {
        stopAnimation();
        return super.populateTimes(date);
    }

    @Override
    protected void bindViews(ViewGroup list, CandlesAdapter adapter) {
        if (list == null)
            return;
        list.removeAllViews();
        if (adapter == null)
            return;

        int holiday = adapter.getCandlesHoliday();
        int candlesCount = adapter.getCandlesCount();
        boolean animate = preferences.isCandlesAnimated();
        ImageView view;
        ViewGroup group = null;

        switch (holiday) {
            case YOM_KIPPUR:
                group = (ViewGroup) adapter.getView(holiday, candlesKippurim, list);
                if (candlesKippurim == null) {
                    candlesKippurim = group;

                    // assert candlesCount == YOM_KIPPURIM_CANDLES.length;
                    animationsKippurim = new CandleAnimation[candlesCount];
                    for (int i = 0; i < candlesCount; i++) {
                        view = group.findViewById(YOM_KIPPURIM_CANDLES[i]);
                        animationsKippurim[i] = new CandleAnimation(handler, view, random);
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
                    animationsChannuka = new CandleAnimation[allCandlesCount + 1];
                    for (int i = 0; i < allCandlesCount; i++) {
                        view = group.findViewById(CHANNUKA_CANDLES[i]);
                        animationsChannuka[i] = new CandleAnimation(handler, view, random);
                    }
                    view = group.findViewById(R.id.candle_shamash);
                    animationsChannuka[allCandlesCount] = new CandleAnimation(handler, view, random);
                }
                list.addView(group);
                animations = animationsChannuka;
                // Only show relevant candles.
                for (int i = 0; i < candlesCount; i++) {
                    animations[i].getView().setVisibility(View.VISIBLE);
                }
                for (int i = candlesCount; i < CHANNUKA_CANDLES.length; i++) {
                    animations[i].getView().setVisibility(View.INVISIBLE);
                }
                break;
            default:
                if (candlesCount > 0) {
                    group = (ViewGroup) adapter.getView(holiday, candlesShabbat, list);
                    if (candlesShabbat == null) {
                        candlesShabbat = group;

                        // assert candlesCount == SHABBAT_CANDLES.length;
                        animationsShabbat = new CandleAnimation[candlesCount];
                        for (int i = 0; i < candlesCount; i++) {
                            view = group.findViewById(SHABBAT_CANDLES[i]);
                            animationsShabbat[i] = new CandleAnimation(handler, view, random);
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

        if (animate && isVisible())
            startAnimation();
    }

    @Override
    protected void setOnClickListener(View view, ZmanimItem item) {
        // No clicking allowed.
    }

    private void stopAnimation() {
        final CandleAnimation[] anims = animations;
        if (anims == null)
            return;
        for (Runnable anim : anims) {
            if (anim == null)
                continue;
            handler.removeCallbacks(anim);
        }
    }

    private void startAnimation() {
        final CandleAnimation[] anims = animations;
        if (anims == null)
            return;
        for (Runnable anim : anims) {
            if (anim == null)
                continue;
            handler.post(anim);
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        if (changedView == list) {
            if (visibility == View.VISIBLE) {
                startAnimation();
            } else {
                stopAnimation();
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        View view = getView();
        onVisibilityChanged(view, view.getVisibility());
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        stopAnimation();
    }
}
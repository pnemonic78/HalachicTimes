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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.compass.R;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.util.Calendar;
import java.util.Random;

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
public class CandlesFragment extends ZmanimFragment<CandlesAdapter, CandlesPopulater> {

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

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XMl attributes.
     * @param defStyle
     *         the default style.
     */
    public CandlesFragment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the XML attributes.
     */
    public CandlesFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructs a new candles view.
     *
     * @param context
     *         the context.
     */
    public CandlesFragment(Context context) {
        super(context);
        init();
    }

    /** Initialise. */
    private void init() {
        // Ignore the list inside of the scroller.
        view = this;
        list = view;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected CandlesAdapter createAdapter() {
        return new CandlesAdapter(context, settings);
    }

    @Override
    protected CandlesPopulater createPopulater() {
        return new CandlesPopulater(context, settings);
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
        boolean animate = settings.isCandlesAnimated();
        ImageView view;
        ViewGroup group = null;

        switch (holiday) {
            case JewishCalendar.YOM_KIPPUR:
                group = (ViewGroup) adapter.getView(holiday, candlesKippurim, list);
                if (candlesKippurim == null) {
                    candlesKippurim = group;

                    // assert candlesCount == YOM_KIPPURIM_CANDLES.length;
                    animationsKippurim = new CandleAnimation[candlesCount];
                    for (int i = 0; i < candlesCount; i++) {
                        view = (ImageView) group.findViewById(YOM_KIPPURIM_CANDLES[i]);
                        animationsKippurim[i] = new CandleAnimation(handler, view, random);
                    }
                }
                list.addView(group);
                animations = animationsKippurim;
                break;
            case JewishCalendar.CHANUKAH:
                group = (ViewGroup) adapter.getView(holiday, candlesChannuka, list);
                if (candlesChannuka == null) {
                    candlesChannuka = group;

                    // create all candles in case user navigates to future day.
                    final int allCandlesCount = CHANNUKA_CANDLES.length;
                    // assert candlesCount <= allCandlesCount;
                    animationsChannuka = new CandleAnimation[allCandlesCount + 1];
                    for (int i = 0; i < allCandlesCount; i++) {
                        view = (ImageView) group.findViewById(CHANNUKA_CANDLES[i]);
                        animationsChannuka[i] = new CandleAnimation(handler, view, random);
                    }
                    view = (ImageView) group.findViewById(R.id.candle_shamash);
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
                            view = (ImageView) group.findViewById(SHABBAT_CANDLES[i]);
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

    @Override
    @TargetApi(Build.VERSION_CODES.FROYO)
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        onVisibilityChanged(visibility);
    }

    protected void onVisibilityChanged(int visibility) {
        if (visibility == VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            onVisibilityChanged(visibility);
        }
    }
}
/*
 * Copyright 2019, Moshe Waisberg
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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe Waisberg
 */
public class CandleView extends ImageView {

    private Handler handler;
    private CandleAnimation animation;

    public CandleView(Context context) {
        super(context);
        init();
    }

    public CandleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CandleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_CENTER);
        setImageResource(R.drawable.candle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler = getHandler();
        animation = new CandleAnimation(getHandler(), this);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == View.VISIBLE) {
            startFlicker();
        } else {
            stopFlicker();
        }
    }

    /**
     * Start the flicker animation.
     */
    public void startFlicker() {
        if (handler != null) {
            handler.post(animation);
        }
    }

    /**
     * Stop the flicker animation.
     */
    public void stopFlicker() {
        if (handler != null) {
            handler.removeCallbacks(animation);
        }
    }

    /**
     * Either start or stop the flicker animation.
     *
     * @param enabled {@code true} to start - else stop.
     */
    public void flicker(boolean enabled) {
        if (enabled) {
            startFlicker();
        } else {
            stopFlicker();
        }
    }
}

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
import android.widget.ImageView;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe Waisberg
 */
public class CandleView extends ImageView {

    private Handler handler;
    private Random random;
    private CandleAnimation animation;

    public CandleView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public CandleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public CandleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public CandleView init(@NonNull Random random) {
        this.random = random;
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler = getHandler();
        animation = new CandleAnimation(getHandler(), this, random);
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
}

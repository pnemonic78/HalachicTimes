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

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe Waisberg
 */
public class CandleAnimation implements Runnable {

    private static final long PERIOD = SECOND_IN_MILLIS / 2L;
    private static final int PERIOD_INT = (int) PERIOD;
    private static final int[] LEVELS = {0, 1, 2, 3, 2, 1, 0, 4, 5, 6, 7, 6, 5, 4};
    private static final int MAX_LEVELS = LEVELS.length;

    private final Handler handler;
    private Drawable candle;
    /**
     * Randomizer.
     */
    private final Random random;
    private static Random randomAll;

    /**
     * Create a new animation.
     *
     * @param handler the timer.
     * @param view    the image view.
     */
    public CandleAnimation(@NonNull Handler handler, @NonNull ImageView view) {
        this(handler, view, null);
    }

    /**
     * Create a new animation.
     *
     * @param handler the timer.
     * @param view    the image view.
     * @param random  the delay randomizer.
     */
    public CandleAnimation(@NonNull Handler handler, @NonNull ImageView view, @Nullable Random random) {
        this.handler = handler;
        this.candle = view.getDrawable();

        Random rand = randomAll;
        if (rand == null) {
            rand = new Random();
            randomAll = rand;
        }
        this.random = rand;
    }

    @Override
    public void run() {
        int level = candle.getLevel();
        level++;
        if (level >= MAX_LEVELS) {
            level = 0;
        }
        candle.setLevel(LEVELS[level]);

        if (random == null) {
            handler.postDelayed(this, PERIOD);
        } else {
            handler.postDelayed(this, random.nextInt(PERIOD_INT));
        }
    }
}

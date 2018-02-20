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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * Flicker animation for 1 candle.
 *
 * @author Moshe W
 */
public class CandleAnimation implements Runnable {

    private static final int LEVELS = 14;
    private static final long PERIOD = SECOND_IN_MILLIS >> 1;
    private static final int PERIOD_INT = (int) PERIOD;

    private final Handler handler;
    private final View view;
    private Drawable candle;
    /** Randomizer. */
    private final Random random;
    private static Drawable[] sprites;

    /**
     * Create a new animation.
     *
     * @param handler the timer.
     * @param view    the image view.
     */
    public CandleAnimation(Handler handler, ImageView view) {
        this(handler, view, null);
    }

    /**
     * Create a new animation.
     *
     * @param handler the timer.
     * @param view    the image view.
     * @param random  the delay randomizer.
     */
    public CandleAnimation(Handler handler, ImageView view, Random random) {
        this.handler = handler;
        if (view == null)
            throw new IllegalArgumentException("view required");
        this.view = view;
        this.random = random;

        // Cache the images to avoid "bitmap size exceeds VM budget".
        if (sprites == null) {
            sprites = new Drawable[LEVELS];

            Resources res = view.getResources();
            Options opts = new Options();
            opts.inDither = false;
            Bitmap bmp0 = BitmapFactory.decodeResource(res, R.drawable.candle_0, opts);
            Bitmap bmp1 = BitmapFactory.decodeResource(res, R.drawable.candle_1, opts);
            Bitmap bmp2 = BitmapFactory.decodeResource(res, R.drawable.candle_2, opts);
            Bitmap bmp3 = BitmapFactory.decodeResource(res, R.drawable.candle_3, opts);
            Bitmap bmp4 = BitmapFactory.decodeResource(res, R.drawable.candle_4, opts);
            Bitmap bmp5 = BitmapFactory.decodeResource(res, R.drawable.candle_5, opts);
            Bitmap bmp6 = BitmapFactory.decodeResource(res, R.drawable.candle_6, opts);
            Bitmap bmp7 = BitmapFactory.decodeResource(res, R.drawable.candle_7, opts);
            sprites[0] = new BitmapDrawable(res, bmp0);
            sprites[1] = new BitmapDrawable(res, bmp1);
            sprites[2] = new BitmapDrawable(res, bmp2);
            sprites[3] = new BitmapDrawable(res, bmp3);
            sprites[4] = sprites[2];
            sprites[5] = sprites[1];
            sprites[6] = sprites[0];
            sprites[7] = new BitmapDrawable(res, bmp4);
            sprites[8] = new BitmapDrawable(res, bmp5);
            sprites[9] = new BitmapDrawable(res, bmp6);
            sprites[10] = new BitmapDrawable(res, bmp7);
            sprites[11] = sprites[9];
            sprites[12] = sprites[8];
            sprites[13] = sprites[7];
        }

        LevelListDrawable candle = new LevelListDrawable();
        for (int i = 0; i < LEVELS; i++)
            candle.addLevel(0, i, sprites[i]);
        view.setImageDrawable(candle);
        this.candle = candle;
    }

    @Override
    public void run() {
        int level = candle.getLevel();
        level++;
        if (level >= LEVELS)
            level = 0;
        candle.setLevel(level);

        if (random == null)
            handler.postDelayed(this, PERIOD);
        else
            handler.postDelayed(this, random.nextInt(PERIOD_INT));
    }

    /**
     * Get the image view animating the candle.
     *
     * @return the view.
     */
    public View getView() {
        return view;
    }
}

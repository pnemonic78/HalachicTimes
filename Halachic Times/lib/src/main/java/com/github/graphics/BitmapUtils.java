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
package com.github.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import static com.github.graphics.DrawableUtils.getWallpaperColor;

/**
 * Bitmap utilities.
 *
 * @author Moshe Waisberg
 */
public class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * Get the dominant color of the image.
     *
     * @param bm the bitmap.
     * @return the color - {@code {@link android.graphics.Color#TRANSPARENT}} otherwise.
     */
    public static int getPixel(Bitmap bm) {
        Bitmap pixel = (bm.getWidth() <= 8) && (bm.getHeight() <= 8) ? bm : Bitmap.createScaledBitmap(bm, 1, 1, true);
        if (!pixel.isRecycled()) {
            int bg = pixel.getPixel(0, 0);
            if (bm != pixel) {
                pixel.recycle();
            }
            return bg;
        }

        return Color.TRANSPARENT;
    }

    /**
     * Is the color bright?
     * <br>
     * Useful for determining whether to use dark color on bright background.
     *
     * @param color the color.
     * @return {@code true} if the color is "bright".
     */
    public static boolean isBright(int color) {
        int a = Color.alpha(color);
        if (a >= 0x80) {
            boolean r = Color.red(color) >= 0xB0;
            boolean g = Color.green(color) >= 0xB0;
            boolean b = Color.blue(color) >= 0xB0;
            return ((r && g) || (r && b) || (g && b));
        }
        return false;
    }

    /**
     * Is the wallpaper bright?
     * <br>
     * Useful for determining whether to use dark color on bright background.
     *
     * @param context the context.
     * @return {@code true} if the wallpaper is "bright".
     */
    public static boolean isBrightWallpaper(Context context) {
        return isBright(getWallpaperColor(context));
    }
}

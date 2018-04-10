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

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import static com.github.graphics.BitmapUtils.getPixel;

/**
 * Drawable utilities.
 *
 * @author Moshe Waisberg
 */
public class DrawableUtils {

    private DrawableUtils() {
    }

    /**
     * Get the dominant color of the wallpaper image.
     *
     * @param context the context.
     * @return the color - {@code {@link android.graphics.Color#TRANSPARENT}} otherwise.
     */
    public static int getWallpaperColor(Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Drawable wallpaper = null;
        try {
            wallpaper = wallpaperManager.peekDrawable();
        } catch (Throwable e) {
            // In case of a bad WallpaperService.
            e.printStackTrace();
        }
        if (wallpaper != null) {
            if (wallpaper instanceof BitmapDrawable) {
                Bitmap bm = ((BitmapDrawable) wallpaper).getBitmap();
                return getPixel(bm);
            }
            if (wallpaper instanceof ColorDrawable) {
                return ((ColorDrawable) wallpaper).getColor();
            }

            Bitmap bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            wallpaper.setBounds(0, 0, 1, 1);
            wallpaper.draw(canvas);
            int bg = getPixel(bm);
            bm.recycle();
            return bg;
        }

        return Color.TRANSPARENT;
    }
}

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
package net.sf.graphics;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Bitmap utilities.
 *
 * @author moshe.w
 */
public class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * Get the dominant color of the wallpaper image.
     *
     * @param context
     *         the context.
     * @return the color - {@code {@link android.graphics.Color#TRANSPARENT}} otherwise.
     */
    public static int getWallpaperColor(Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            Drawable wallpaper = wallpaperManager.getDrawable();
            if ((wallpaper != null) && (wallpaper instanceof BitmapDrawable)) {
                Bitmap bm = ((BitmapDrawable) wallpaper).getBitmap();
                Bitmap pixel = Bitmap.createScaledBitmap(bm, 1, 1, true);
                if (!pixel.isRecycled()) {
                    int bg = pixel.getPixel(0, 0);
                    if (bm != pixel) {
                        pixel.recycle();
                    }
                    return bg;
                }
            }
        } catch (RuntimeException e) {
            // In case of bad WallpaperService.
            e.printStackTrace();
        }

        return Color.TRANSPARENT;
    }

    /**
     * Is the color bright?
     * <br>
     * Useful for determining whether to use dark color on bright background.
     *
     * @param color
     *         the color.
     * @return {@code true} if the color is "bright".
     */
    public static boolean isBright(int color) {
        int a = Color.alpha(color);
        if (a >= 0x80) {
            boolean r = Color.red(color) >= 0xcc;
            boolean g = Color.green(color) >= 0xcc;
            boolean b = Color.blue(color) >= 0xcc;
            return ((r && g) || (r && b) || (g && b));
        }
        return false;
    }
}

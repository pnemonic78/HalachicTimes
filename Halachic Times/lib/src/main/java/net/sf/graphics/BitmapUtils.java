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

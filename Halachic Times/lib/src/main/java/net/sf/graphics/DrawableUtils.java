package net.sf.graphics;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import static net.sf.graphics.BitmapUtils.getPixel;

/**
 * @author moshe on 2017/09/17.
 */

public class DrawableUtils {

    private DrawableUtils() {
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
        Drawable wallpaper = null;
        try {
            wallpaper = wallpaperManager.getDrawable();
        } catch (RuntimeException e) {
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

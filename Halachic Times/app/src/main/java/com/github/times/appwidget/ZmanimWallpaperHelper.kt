package com.github.times.appwidget

import android.annotation.TargetApi
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.github.appwidget.AppWidgetUtils.notifyAppWidgetsUpdate

@TargetApi(Build.VERSION_CODES.O_MR1)
class ZmanimWallpaperHelper(private val context: Context) :
    WallpaperManager.OnColorsChangedListener {

    private val handler = Handler(Looper.getMainLooper()) { _ -> true }

    fun onCreate() {
        registerWallpaperChanged(context)
    }

    fun onDestroy() {
        unregisterWallpaperChanged(context)
    }

    override fun onColorsChanged(colors: WallpaperColors?, which: Int) {
        if (colors != null) {
            notifyWallpaperChanged(context, which)
        }
    }

    private fun registerWallpaperChanged(context: Context) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.addOnColorsChangedListener(this, handler)
    }

    private fun unregisterWallpaperChanged(context: Context) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.removeOnColorsChangedListener(this)
    }

    private fun notifyWallpaperChanged(context: Context, which: Int) {
        if (which.and(WallpaperManager.FLAG_SYSTEM) == WallpaperManager.FLAG_SYSTEM) {
            notifyAppWidgetsUpdate(context, ZmanimWidget::class.java)
            notifyAppWidgetsUpdate(context, ZmanimListWidget::class.java)
            notifyAppWidgetsUpdate(context, ClockWidget::class.java)
        }
    }
}
package com.github.times.appwidget

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.github.app.PERMISSION_WALLPAPER
import com.github.times.R
import timber.log.Timber

/**
 * Shows a configuration activity for app widgets.
 *
 * @author Moshe Waisberg
 */
class ZmanimWidgetConfigure : AppCompatActivity() {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Timber.i("Permission to read wallpaper: %s", isGranted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_configure_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkWallpaperPermission(this)
        }
        val appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
    }

    /**
     * @see com.github.times.preference.AppearancePreferenceFragment
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun checkWallpaperPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Wallpaper colors don't need permissions.
            return
        }
        if (PermissionChecker.checkCallingOrSelfPermission(
                context,
                PERMISSION_WALLPAPER
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WALLPAPER)) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title_widget_zmanim)
                    .setMessage(R.string.appwidget_theme_permission_rationale)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        requestPermission.launch(PERMISSION_WALLPAPER)
                    }
                    .show()
            } else {
                requestPermission.launch(PERMISSION_WALLPAPER)
            }
        }
    }
}
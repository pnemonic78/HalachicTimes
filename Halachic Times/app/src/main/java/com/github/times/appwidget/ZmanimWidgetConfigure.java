package com.github.times.appwidget;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.github.times.R;

/**
 * Shows a configuration activity for app widgets.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidgetConfigure extends Activity {

    private static final String PERMISSION_WALLPAPER = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int REQUEST_WALLPAPER = 0x3A11;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configure_activity);
        checkWallpaperPermission(this);
        setResult(RESULT_OK, getIntent());
    }

    /**
     * @see com.github.times.preference.AppearancePreferenceFragment
     */
    private void checkWallpaperPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_WALLPAPER) != PermissionChecker.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WALLPAPER)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_widget_zmanim)
                            .setMessage(R.string.appwidget_theme_permission_rationale)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{PERMISSION_WALLPAPER}, REQUEST_WALLPAPER);
                                }
                            })
                            .show();
                } else {
                    requestPermissions(new String[]{PERMISSION_WALLPAPER}, REQUEST_WALLPAPER);
                }
            }
        }
    }
}

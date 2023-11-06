package com.github.times.appwidget;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.github.times.R;

import timber.log.Timber;

/**
 * Shows a configuration activity for app widgets.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidgetConfigure extends AppCompatActivity {

    private static final String PERMISSION_WALLPAPER = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        Timber.i("Permission to read wallpaper: %s", isGranted);
    });

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
    private boolean checkWallpaperPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Wallpaper colors don't need permissions.
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_WALLPAPER) != PermissionChecker.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WALLPAPER)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_widget_zmanim)
                            .setMessage(R.string.appwidget_theme_permission_rationale)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermission.launch(PERMISSION_WALLPAPER))
                            .show();
                } else {
                    requestPermission.launch(PERMISSION_WALLPAPER);
                }
                return true;
            }
        }
        return false;
    }
}

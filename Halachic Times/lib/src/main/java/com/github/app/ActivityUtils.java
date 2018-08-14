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
package com.github.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.github.util.LogUtils;

import androidx.annotation.NonNull;

import static android.content.pm.PackageManager.GET_META_DATA;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Activity utilities.
 *
 * @author Moshe Waisberg
 */
public class ActivityUtils {

    private static final String TAG = "ActivityUtils";

    private ActivityUtils() {
    }

    /**
     * Reset the title.
     *
     * @param activity the activity with a title.
     */
    public static void restTitle(Activity activity) {
        try {
            int label = activity.getPackageManager().getActivityInfo(activity.getComponentName(), GET_META_DATA).labelRes;
            if (label != 0) {
                activity.setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "package not found!", e);
        }
    }

    /**
     * Restart the activity.
     *
     * @param activity   the activity.
     * @param savedState saved state from either {@link Activity#onSaveInstanceState(Bundle)}
     *                   or {@link Activity#onSaveInstanceState(Bundle, PersistableBundle)}.
     */
    public static void restartActivity(Activity activity, Bundle savedState) {
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();

        Bundle args = (savedState != null) ? savedState : new Bundle();
        if (extras != null) {
            args.putAll(extras);
        }
        intent.putExtras(args);

        activity.finish();
        activity.startActivity(intent);
    }

    /**
     * Restart the activity.
     *
     * @param activity the activity.
     */
    public static void restartActivity(Activity activity) {
        Bundle savedState = null;
        if (VERSION.SDK_INT >= LOLLIPOP) {
            savedState = new Bundle();
            activity.onSaveInstanceState(savedState, null);
        }
        restartActivity(activity, savedState);
    }

    public static boolean isPermissionGranted(@NonNull String permission, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final int length = Math.min(permissions.length, grantResults.length);
        for (int i = 0; i < length; i++) {
            if (permission.equals(permissions[i]) && (grantResults[i] == PackageManager.PERMISSION_GRANTED)) {
                return true;
            }
        }
        return false;
    }
}

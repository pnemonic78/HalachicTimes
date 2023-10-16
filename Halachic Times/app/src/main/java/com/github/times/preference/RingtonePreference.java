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
package com.github.times.preference;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import com.github.app.ActivityUtils;
import com.github.media.RingtoneManager;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 * Requests a user's permissions to read external media.
 *
 * @author Moshe Waisberg
 */
public class RingtonePreference extends com.github.preference.RingtonePreference {

    public static final String PERMISSION_RINGTONE = RingtoneManager.Companion.getPERMISSION_RINGTONE();

    private Fragment requestPermissionsFragment;
    private int requestPermissionsCode;
    private boolean requestPermissions = false;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RingtonePreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Context context = getContext();
            if (requestPermissions) {
                if (PermissionChecker.checkCallingOrSelfPermission(context, PERMISSION_RINGTONE) != PermissionChecker.PERMISSION_GRANTED) {
                    final Fragment owner = requestPermissionsFragment;
                    final int requestCode = requestPermissionsCode;
                    if (owner != null) {
                        owner.requestPermissions(new String[]{PERMISSION_RINGTONE}, requestCode);
                        return;
                    }
                }
            }
        }
        super.onClick();
    }

    public void setRequestPermissionsCode(Fragment owner, int requestPermissionsCode) {
        this.requestPermissionsFragment = owner;
        this.requestPermissionsCode = requestPermissionsCode;
        this.requestPermissions = true;
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        // Rebuild the list to include allowed tones.
        if (ActivityUtils.isPermissionGranted(PERMISSION_RINGTONE, permissions, grantResults)) {
            setRingtoneType(getRingtoneType(), true);
        }
        // Show the list anyway.
        requestPermissions = false;
        onClick();
    }
}

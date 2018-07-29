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
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

import com.github.app.ActivityUtils;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 *
 * @author Moshe Waisberg
 */
public class RingtonePreference extends com.github.preference.RingtonePreference {

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

    @Override
    protected void onClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Context context = getContext();
            if (requestPermissions && (context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (requestPermissionsCode != 0)) {
                final int requestCode = requestPermissionsCode;
                final Fragment owner = requestPermissionsFragment;
                if (owner != null) {
                    owner.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                    return;
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
        if (ActivityUtils.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, permissions, grantResults)) {
            setRingtoneType(getRingtoneType(), true);
        }
        // Show the list anyway.
        requestPermissions = false;
        onClick();
    }
}

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
package com.github.times.preference

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.github.app.ActivityUtils.isPermissionGranted
import com.github.media.RingtoneManager
import com.github.preference.RingtonePreference
import com.github.util.TypedValueUtils

/**
 * A [androidx.preference.Preference] that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 * Requests a user's permissions to read external media.
 *
 * @author Moshe Waisberg
 */
class RingtonePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = TypedValueUtils.getAttr(
        context,
        androidx.preference.R.attr.dialogPreferenceStyle,
        android.R.attr.ringtonePreferenceStyle
    ),
    @StyleRes defStyleRes: Int = 0
) : RingtonePreference(context, attrs, defStyleAttr, defStyleRes) {
    private var requestPermissionsFragment: Fragment? = null
    private var requestPermissionsCode = 0
    private var requestPermissions = false

    override fun onClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val context = context
            if (requestPermissions) {
                if (PermissionChecker.checkCallingOrSelfPermission(
                        context,
                        PERMISSION_RINGTONE
                    ) != PermissionChecker.PERMISSION_GRANTED
                ) {
                    val owner = requestPermissionsFragment
                    val requestCode = requestPermissionsCode
                    if (owner != null) {
                        owner.requestPermissions(arrayOf(PERMISSION_RINGTONE), requestCode)
                        return
                    }
                }
            }
        }
        super.onClick()
    }

    fun setRequestPermissionsCode(owner: Fragment?, requestPermissionsCode: Int) {
        this.requestPermissionsFragment = owner
        this.requestPermissionsCode = requestPermissionsCode
        this.requestPermissions = true
    }

    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        // Rebuild the list to include allowed tones.
        if (isPermissionGranted(PERMISSION_RINGTONE, permissions, grantResults)) {
            setRingtoneType(ringtoneType, true)
        }
        // Show the list anyway.
        requestPermissions = false
        onClick()
    }

    companion object {
        val PERMISSION_RINGTONE = RingtoneManager.PERMISSION_RINGTONE
    }
}
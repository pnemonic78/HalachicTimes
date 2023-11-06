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

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import com.github.times.R

/**
 * This fragment shows the preferences for the About header.
 */
@Keep
class AboutPreferenceFragment : AbstractPreferenceFragment() {
    override val preferencesXml: Int = R.xml.about_preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<Preference>("about.version")!!.apply {
            val context = this.context
            try {
                summary = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                // Never should happen with our own package!
            }
            validateIntent(this)
        }

        validateIntent("about.issue")
        validateIntent("about.kosherjava")
    }
}
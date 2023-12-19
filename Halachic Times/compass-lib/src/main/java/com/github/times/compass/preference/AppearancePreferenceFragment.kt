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
package com.github.times.compass.preference

import androidx.annotation.Keep
import com.github.preference.AbstractPreferenceFragment
import com.github.times.compass.lib.R

/**
 * This fragment shows the preferences for the Appearance header.
 */
@Keep
class AppearancePreferenceFragment : AbstractPreferenceFragment() {
    override val preferencesXml: Int = R.xml.appearance_preferences
}
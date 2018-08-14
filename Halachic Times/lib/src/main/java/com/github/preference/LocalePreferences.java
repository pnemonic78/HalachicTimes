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
package com.github.preference;

import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * Locale preferences.
 *
 * @author Moshe Waisberg
 */
public interface LocalePreferences {

    /** Preference name for the locale. */
    String KEY_LOCALE = "locale";

    /**
     * Get the locale. If no locale was specified, then the default locale.
     *
     * @return the locale.
     */
    @NonNull
    Locale getLocale();

}

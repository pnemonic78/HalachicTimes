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
package net.sf.preference;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Locale;

import static net.sf.util.LocaleUtils.getDefaultLocale;
import static net.sf.util.LocaleUtils.parseLocale;

/**
 * Locale preferences implementation.
 *
 * @author Moshe Waisberg
 */
public class SimpleLocalePreferences extends SimplePreferences implements LocalePreferences {

    public SimpleLocalePreferences(Context context) {
        super(context);
    }

    public SimpleLocalePreferences(Context context, boolean multiProcess) {
        super(context, multiProcess);
    }

    @NonNull
    @Override
    public Locale getLocale() {
        String value = getPreferences().getString(KEY_LOCALE, null);
        Locale locale = parseLocale(value);
        if (locale != null) {
            return locale;
        }
        return getDefaultLocale(context);
    }
}

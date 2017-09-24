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
import android.text.TextUtils;

import java.util.Locale;

import static net.sf.util.LocaleUtils.getDefaultLocale;

/**
 * Locale preferences implementation.
 *
 * @author moshe on 2017/09/17.
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
        if (!TextUtils.isEmpty(value)) {
            String[] tokens = value.split("_");
            switch (tokens.length) {
                case 1:
                    return new Locale(tokens[0]);
                case 2:
                    return new Locale(tokens[0], tokens[1]);
                case 3:
                    return new Locale(tokens[0], tokens[1], tokens[2]);
            }
        }
        return getDefaultLocale(context);
    }
}

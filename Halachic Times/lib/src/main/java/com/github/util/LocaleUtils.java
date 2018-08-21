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
package com.github.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.text.TextUtils.isEmpty;

/**
 * Locale utilities.
 *
 * @author Moshe Waisberg
 */
public class LocaleUtils {

    private static final String TAG = "LocaleUtils";

    /**
     * ISO 639 language code for "Arabic".
     */
    public static final String ISO639_ARABIC = "ar";
    /**
     * ISO 639 language code for "Hausa".
     */
    public static final String ISO639_HAUSA = "ha";
    /**
     * ISO 639 language code for "Hebrew".
     */
    public static final String ISO639_HEBREW_FORMER = "he";
    /**
     * ISO 639 language code for "Hebrew" (Java compatibility).
     */
    public static final String ISO639_HEBREW = "iw";
    /**
     * ISO 639 language code for "Yiddish" (Java compatibility).
     */
    public static final String ISO639_YIDDISH_FORMER = "ji";
    /**
     * ISO 639 language code for "Persian (Farsi)".
     */
    public static final String ISO639_PERSIAN = "fa";
    /**
     * ISO 639 language code for "Pashto, Pushto".
     */
    public static final String ISO639_PASHTO = "ps";
    /**
     * ISO 639 language code for "Yiddish".
     */
    public static final String ISO639_YIDDISH = "yi";

    private LocaleUtils() {
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL() {
        return isLocaleRTL(Locale.getDefault());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param context the context.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(@NonNull Context context) {
        return isLocaleRTL(context.getResources());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param res the resources.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(@NonNull Resources res) {
        return isLocaleRTL(res.getConfiguration());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param config the configuration.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(@NonNull Configuration config) {
        return isLocaleRTL(getDefaultLocale(config));
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(@NonNull Locale locale) {
        switch (locale.getLanguage()) {
            case ISO639_ARABIC:
            case ISO639_HAUSA:
            case ISO639_HEBREW:
            case ISO639_HEBREW_FORMER:
            case ISO639_PASHTO:
            case ISO639_PERSIAN:
            case ISO639_YIDDISH:
            case ISO639_YIDDISH_FORMER:
                return true;
        }
        return false;
    }

    /**
     * Get the default locale.
     *
     * @param context the context.
     * @return the locale.
     */
    @NonNull
    public static Locale getDefaultLocale(@NonNull Context context) {
        return getDefaultLocale(context.getResources());
    }

    /**
     * Get the default locale.
     *
     * @param res the resources.
     * @return the locale.
     */
    @NonNull
    public static Locale getDefaultLocale(@NonNull Resources res) {
        return getDefaultLocale(res.getConfiguration());
    }

    /**
     * Get the default locale.
     *
     * @param config the configuration with locales.
     * @return the locale.
     */
    @TargetApi(N)
    @NonNull
    public static Locale getDefaultLocale(@NonNull Configuration config) {
        Locale locale = null;
        if (VERSION.SDK_INT >= N) {
            return getDefaultLocale(config.getLocales());
        }
        locale = config.locale;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Get the default locale.
     *
     * @param locales the list of locales.
     * @return the locale.
     */
    @TargetApi(N)
    @NonNull
    public static Locale getDefaultLocale(@NonNull android.os.LocaleList locales) {
        Locale locale = null;
        if (locales.isEmpty()) {
            locales = android.os.LocaleList.getAdjustedDefault();
        }
        if (!locales.isEmpty()) {
            locale = locales.get(0);
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Apply the default locale.
     *
     * @param context the context.
     * @param locale  the locale to set.
     * @return the locale.
     */
    public static Context applyLocale(@NonNull Context context, @NonNull Locale locale) {
        Locale.setDefault(locale);

        Resources res = context.getResources();
        if (res == null) {
            LogUtils.w(TAG, "Context resources missing for applying locale!");
            res = Resources.getSystem();
        }
        final Configuration config = res.getConfiguration();
        if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
        return context;
    }

    /**
     * Sort the locales by their display names.
     *
     * @param values the immutable list of locale values.
     * @return the sorted list of locale names.
     */
    public static Locale[] sortByDisplay(String[] values) {
        return sortByDisplay(values, (Locale) null);
    }

    /**
     * Sort the locales by their display names.
     *
     * @param values  the immutable list of locale values.
     * @param context the display context.
     * @return the sorted list of locale names.
     */
    public static Locale[] sortByDisplay(String[] values, Context context) {
        return sortByDisplay(values, getDefaultLocale(context));
    }

    /**
     * Sort the locales by their display names.
     *
     * @param values the immutable list of locale values.
     * @param locale the display locale.
     * @return the sorted list of locales.
     */
    public static Locale[] sortByDisplay(String[] values, Locale locale) {
        if (values == null) {
            return null;
        }
        final int length = values.length;
        Locale[] locales = new Locale[length];
        for (int i = 0; i < length; i++) {
            locales[i] = parseLocale(values[i]);
        }
        return sortByDisplay(locales, locale);
    }


    /**
     * Sort the locales by their display names.
     *
     * @param locales the list of locales to sort.
     * @return the sorted list of locales.
     */
    public static Locale[] sortByDisplay(Locale[] locales) {
        return sortByDisplay(locales, null);
    }

    /**
     * Sort the locales by their display names.
     *
     * @param locales the list of locales to sort.
     * @param locale  the display locale.
     * @return the sorted list of locales.
     */
    public static Locale[] sortByDisplay(Locale[] locales, Locale locale) {
        if (locales == null) {
            return null;
        }
        Arrays.sort(locales, new LocaleNameComparator(locale));
        return locales;
    }

    /**
     * Parse the locale
     *
     * @param localeValue the locale to parse. For example, {@code fr} for "French", or {@code en_UK} for
     *                    "English (United Kingdom)".
     * @return the locale - empty otherwise.
     */
    @NonNull
    public static Locale parseLocale(@Nullable String localeValue) {
        if (!isEmpty(localeValue)) {
            if (VERSION.SDK_INT >= LOLLIPOP) {
                return Locale.forLanguageTag(localeValue);
            }
            String[] tokens = localeValue.split("_");
            switch (tokens.length) {
                case 1:
                    return new Locale(tokens[0]);
                case 2:
                    return new Locale(tokens[0], tokens[1]);
                default:
                    return new Locale(tokens[0], tokens[1], tokens[2]);
            }
        }
        return new Locale("");
    }

    public static Locale[] unique(String[] values) {
        Map<String, Locale> locales = new HashMap<>();
        final int length = values.length;
        Locale locale;
        for (int i = 0; i < length; i++) {
            locale = parseLocale(values[i]);
            locales.put(locale.toString(), locale);
        }
        return locales.values().toArray(new Locale[locales.size()]);
    }
}

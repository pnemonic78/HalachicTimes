/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Locale utilities.
 *
 * @author Moshe Waisberg
 */
public class LocaleUtils {

    /** ISO 639 language code for "Arabic". */
    public static final String ISO639_ARABIC = "ar";
    /** ISO 639 language code for "Hausa". */
    public static final String ISO639_HAUSA = "ha";
    /** ISO 639 language code for "Hebrew". */
    public static final String ISO639_HEBREW_FORMER = "he";
    /** ISO 639 language code for "Hebrew" (Java compatibility). */
    public static final String ISO639_HEBREW = "iw";
    /** ISO 639 language code for "Yiddish" (Java compatibility). */
    public static final String ISO639_YIDDISH_FORMER = "ji";
    /** ISO 639 language code for "Persian (Farsi)". */
    public static final String ISO639_PERSIAN = "fa";
    /** ISO 639 language code for "Pashto, Pushto". */
    public static final String ISO639_PASHTO = "ps";
    /** ISO 639 language code for "Yiddish". */
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
     * @param context
     *         the context.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(Context context) {
        return isLocaleRTL(context.getResources());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param res
     *         the resources.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(Resources res) {
        return isLocaleRTL(res.getConfiguration());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param config
     *         the configuration.
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(Configuration config) {
        return isLocaleRTL(getDefaultLocale(config));
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return {@code true} if the locale is RTL.
     */
    public static boolean isLocaleRTL(Locale locale) {
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
     * @param context
     *         the context.
     * @return the locale.
     */
    public static Locale getDefaultLocale(Context context) {
        return getDefaultLocale(context.getResources());
    }

    /**
     * Get the default locale.
     *
     * @param res
     *         the resources.
     * @return the locale.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getDefaultLocale(Resources res) {
        return getDefaultLocale(res.getConfiguration());
    }

    /**
     * Get the default locale.
     *
     * @param config
     *         the configuration with locales.
     * @return the locale.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getDefaultLocale(Configuration config) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return config.locale;
        }
        Locale locale = config.getLocales().get(0);
        return locale != null ? locale : Locale.getDefault();
    }
}

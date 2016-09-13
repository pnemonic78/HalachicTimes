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
package net.sf.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.sf.content.ProviderPreferences;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class SimplePreferences {

    protected final Context context;
    protected final SharedPreferences preferences;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public SimplePreferences(Context context) {
        this(context, false);
    }

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     * @param multiProcess
     *         is the preferences used for a multi-process application?
     */
    public SimplePreferences(Context context, boolean multiProcess) {
        this.context = context;
        this.preferences = multiProcess ? new ProviderPreferences(context) : PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the preferences.
     *
     * @return the shared preferences.
     */

    public SharedPreferences getPreferences() {
        return preferences;
    }
}

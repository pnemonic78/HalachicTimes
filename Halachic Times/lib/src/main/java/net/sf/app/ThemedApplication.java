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
package net.sf.app;

import android.app.Application;
import android.content.Context;

import net.sf.preference.ThemedPreferences;

/**
 * Application with a theme.
 *
 * @author Moshe Waisberg
 */
public class ThemedApplication extends Application {

    private ThemedPreferences preferences;

    @Override
    public void onCreate() {
        ThemedPreferences.init(this);
        super.onCreate();
        System.out.println("~!@ app " + getPreferences().getTheme());
        setTheme(getPreferences().getTheme());
    }

    public ThemedPreferences getPreferences() {
        if (preferences == null) {
            preferences = createPreferences(this);
        }
        return preferences;
    }

    protected ThemedPreferences createPreferences(Context context) {
        return new ThemedPreferences(context);
    }
}

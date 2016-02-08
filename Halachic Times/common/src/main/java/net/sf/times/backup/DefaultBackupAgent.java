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
package net.sf.times.backup;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.Build;

/**
 * Default backup agent helper for the application.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class DefaultBackupAgent extends BackupAgentHelper {

    /** A key to uniquely identify the set of backup data. */
    private static final String PREFS_BACKUP_KEY = "prefs";

    @Override
    public void onCreate() {
        super.onCreate();

        String prefsName = getDefaultSharedPreferencesName(this);
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, prefsName);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

    /** Copied from android.preference.PreferenceManager */
    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
}

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
package net.sf.times.preference;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;

import net.sf.times.ZmanimReminder;

/**
 * This fragment shows the preferences for a zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanPreferenceFragment extends AbstractPreferenceFragment {

    public static final String EXTRA_XML = "xml";
    public static final String EXTRA_OPINION = "opinion";
    public static final String EXTRA_REMINDER = "reminder";

    private int xmlId;
    private String reminderKey;
    private ZmanimSettings settings;
    private ZmanimReminder reminder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, "xml", getActivity().getPackageName());
        String opinionKey = args.getString(EXTRA_OPINION);
        this.reminderKey = args.getString(EXTRA_REMINDER);

        super.onCreate(savedInstanceState);

        initList(opinionKey);
        initList(reminderKey);
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();

        super.onListPreferenceChange(preference, newValue);

        if (!oldValue.equals(newValue)) {
            if (preference.getKey().equals(reminderKey) && (activity != null)) {
                if (settings == null)
                    settings = new ZmanimSettings(activity);
                if (reminder == null)
                    reminder = new ZmanimReminder(activity);
                reminder.remind(settings);
            }
        }
    }
}

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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;

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
    private String reminderKeySunday;
    private String reminderKeyMonday;
    private String reminderKeyTuesday;
    private String reminderKeyWednesday;
    private String reminderKeyThursday;
    private String reminderKeyFriday;
    private String reminderKeySaturday;
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
        if (!TextUtils.isEmpty(reminderKey)) {
            this.reminderKeySunday = reminderKey + ZmanimSettings.REMINDER_SUNDAY_SUFFIX;
            this.reminderKeyMonday = reminderKey + ZmanimSettings.REMINDER_MONDAY_SUFFIX;
            this.reminderKeyTuesday = reminderKey + ZmanimSettings.REMINDER_TUESDAY_SUFFIX;
            this.reminderKeyWednesday = reminderKey + ZmanimSettings.REMINDER_WEDNESDAY_SUFFIX;
            this.reminderKeyThursday = reminderKey + ZmanimSettings.REMINDER_THURSDAY_SUFFIX;
            this.reminderKeyFriday = reminderKey + ZmanimSettings.REMINDER_FRIDAY_SUFFIX;
            this.reminderKeySaturday = reminderKey + ZmanimSettings.REMINDER_SATURDAY_SUFFIX;
        }

        super.onCreate(savedInstanceState);

        initList(opinionKey);
        initList(reminderKey);
        initReminderDays(reminderKey);
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String key = preference.getKey();
        String oldValue = preference.getValue();

        super.onListPreferenceChange(preference, newValue);

        if (!oldValue.equals(newValue) && key.equals(reminderKey)) {
            remind();
        }
    }

    protected void initReminderDays(String namePrefix) {
        if (TextUtils.isEmpty(namePrefix)) {
            return;
        }

        initReminderDay(reminderKeySunday);
        initReminderDay(reminderKeyMonday);
        initReminderDay(reminderKeyTuesday);
        initReminderDay(reminderKeyWednesday);
        initReminderDay(reminderKeyThursday);
        initReminderDay(reminderKeyFriday);
        initReminderDay(reminderKeySaturday);
    }

    protected CheckBoxPreference initReminderDay(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Preference pref = findPreference(key);
        if (pref != null) {
            CheckBoxPreference checkBox = (CheckBoxPreference) pref;
            checkBox.setOnPreferenceChangeListener(this);
            return checkBox;
        }
        return null;
    }

    protected boolean onCheckBoxPreferenceChange(CheckBoxPreference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(reminderKeySunday)
                || key.equals(reminderKeyMonday)
                || key.equals(reminderKeyTuesday)
                || key.equals(reminderKeyWednesday)
                || key.equals(reminderKeyThursday)
                || key.equals(reminderKeyFriday)
                || key.equals(reminderKeySaturday)) {
            remind();
        }

        return super.onCheckBoxPreferenceChange(preference, newValue);
    }

    private void remind() {
        if (context != null) {
            if (settings == null)
                settings = new ZmanimSettings(context);
            if (reminder == null)
                reminder = new ZmanimReminder(context);
            reminder.remind(settings);
        }
    }
}

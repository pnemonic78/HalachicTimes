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
package net.sf.times.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;

import net.sf.times.ZmanimReminder;
import net.sf.times.ZmanimReminderService;

import java.util.HashSet;
import java.util.Set;

/**
 * This fragment shows the preferences for a zman screen.
 */
public class ZmanPreferenceFragment extends net.sf.preference.AbstractPreferenceFragment {

    public static final String EXTRA_XML = "xml";
    public static final String EXTRA_OPINION = "opinion";
    public static final String EXTRA_REMINDER = "reminder";

    private int xmlId;
    private final Set<String> opinionKeys = new HashSet<>(1);
    private Preference preferenceReminder;
    private Preference preferenceReminderSunday;
    private Preference preferenceReminderMonday;
    private Preference preferenceReminderTuesday;
    private Preference preferenceReminderWednesday;
    private Preference preferenceReminderThursday;
    private Preference preferenceReminderFriday;
    private Preference preferenceReminderSaturday;
    private ZmanimPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        int indexSlash = xmlName.lastIndexOf('/');
        if (indexSlash >= 0) {
            xmlName = xmlName.substring(indexSlash + 1);
        }
        int indexDot = xmlName.indexOf('.');
        if (indexDot >= 0) {
            xmlName = xmlName.substring(0, indexDot);
        }
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, "xml", getActivity().getPackageName());
        String opinionKey = args.getString(EXTRA_OPINION);
        String reminderKey = args.getString(EXTRA_REMINDER);

        super.onCreate(savedInstanceState);

        opinionKeys.clear();
        if (!TextUtils.isEmpty(opinionKey)) {
            if (opinionKey.indexOf(';') > 0) {
                String[] tokens = opinionKey.split(";");
                int length = tokens.length;
                for (int i = 0; i < length; i++) {
                    initList(tokens[i]);
                    opinionKeys.add(tokens[i]);
                }
            } else {
                initList(opinionKey);
                opinionKeys.add(opinionKey);
            }
        }
        if ((this.preferenceReminder = initList(reminderKey)) == null) {
            this.preferenceReminder = initTime(reminderKey);
        }
        if (preferenceReminder != null) {
            initReminderDays(preferenceReminder);
        }
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }

    protected ZmanimPreferences getPreferences() {
        if (preferences == null) {
            final Context context = getActivity();
            preferences = new SimpleZmanimPreferences(context);
        }
        return preferences;
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();

        super.onListPreferenceChange(preference, newValue);

        if (!oldValue.equals(newValue) && ((preference == preferenceReminder) || opinionKeys.contains(preference.getKey()))) {
            // Explicitly disable dependencies?
            preference.notifyDependencyChange(preference.shouldDisableDependents());

            remind();
        }
    }

    protected void initReminderDays(Preference reminderTime) {
        String namePrefix = reminderTime.getKey();
        this.preferenceReminderSunday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_SUNDAY_SUFFIX);
        this.preferenceReminderMonday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_MONDAY_SUFFIX);
        this.preferenceReminderTuesday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_TUESDAY_SUFFIX);
        this.preferenceReminderWednesday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_WEDNESDAY_SUFFIX);
        this.preferenceReminderThursday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_THURSDAY_SUFFIX);
        this.preferenceReminderFriday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_FRIDAY_SUFFIX);
        this.preferenceReminderSaturday = initReminderDay(namePrefix + ZmanimPreferences.REMINDER_SATURDAY_SUFFIX);
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
        if (preference.equals(preferenceReminderSunday)
                || preference.equals(preferenceReminderMonday)
                || preference.equals(preferenceReminderTuesday)
                || preference.equals(preferenceReminderWednesday)
                || preference.equals(preferenceReminderThursday)
                || preference.equals(preferenceReminderFriday)
                || preference.equals(preferenceReminderSaturday)) {
            remind();
        }

        return super.onCheckBoxPreferenceChange(preference, newValue);
    }

    /**
     * Run the reminder service.
     * Tries to postpone the reminder until after any preferences have changed.
     */
    private void remind() {
        final Context context = getActivity();
        Intent intent = new Intent(context, ZmanimReminderService.class);
        intent.setAction(ZmanimReminder.ACTION_UPDATE);
        context.startService(intent);
    }
}

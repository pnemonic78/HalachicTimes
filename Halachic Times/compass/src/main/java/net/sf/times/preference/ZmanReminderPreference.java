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

import android.content.Context;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Zman reminder preference.
 */
public class ZmanReminderPreference extends ListPreference {

    public ZmanReminderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZmanReminderPreference(Context context) {
        super(context);
    }

    @Override
    public boolean shouldDisableDependents() {
        return super.shouldDisableDependents() || isOff();
    }

    public boolean isOff() {
        return TextUtils.isEmpty(getValue());
    }
}

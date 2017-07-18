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

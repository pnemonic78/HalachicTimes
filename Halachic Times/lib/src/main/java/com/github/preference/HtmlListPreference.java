/*
 * Copyright 2016, Moshe Waisberg
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
package com.github.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;

/**
 * List preference that re-parses the entries as HTML.
 *
 * @author Moshe Waisberg
 */
public class HtmlListPreference extends ListPreference {

    public HtmlListPreference(Context context) {
        super(context);
        init();
    }

    public HtmlListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HtmlListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HtmlListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        CharSequence[] entries = getEntries();
        if (entries != null) {
            setEntries(entries);
        }
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        if (entries != null) {
            int length = entries.length;
            CharSequence entry;
            for (int i = 0; i < length; i++) {
                entry = entries[i];
                if (entry == null) {
                    continue;
                }
                entries[i] = trim(Html.fromHtml(entry.toString()));
            }
        }
        super.setEntries(entries);
    }

    private Spanned trim(Spanned spanned) {
        if (spanned.length() == 0) {
            return spanned;
        }

        Editable e;
        if (spanned instanceof Editable) {
            e = (Editable) spanned;
        } else {
            e = SpannableStringBuilder.valueOf(spanned);
        }
        int length = e.length();
        while ((length > 0) && Character.isWhitespace(e.charAt(0))) {
            e.delete(0, 1);
            length--;
        }
        int length1 = length - 1;
        while ((length > 0) && Character.isWhitespace(e.charAt(length1))) {
            e.delete(length1, length);
            length--;
            length1--;
        }

        return e;
    }
}

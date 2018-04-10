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
package com.github.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.lib.R;

import java.util.Calendar;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Date picker dialog with a "Today" button.
 *
 * @author Moshe Waisberg
 */
public class TodayDatePickerDialog extends DatePickerDialog {

    public TodayDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        init(context);
    }

    public TodayDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme, callBack, year, monthOfYear, dayOfMonth);
        init(context);
    }

    private void init(Context context) {
        setButton(BUTTON_NEUTRAL, context.getText(R.string.today), this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_NEUTRAL) {
            setToday();
            if (VERSION.SDK_INT >= LOLLIPOP)
                onClick(dialog, BUTTON_POSITIVE);
        }
        super.onClick(dialog, which);
    }

    private void setToday() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int monthOfYear = today.get(Calendar.MONTH);
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        updateDate(year, monthOfYear, dayOfMonth);
    }
}

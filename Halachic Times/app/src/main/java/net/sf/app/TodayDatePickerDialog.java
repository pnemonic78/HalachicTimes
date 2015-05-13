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

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.Time;

import net.sf.times.R;

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
        }
        super.onClick(dialog, which);
    }

    private void setToday() {
        // "Time" object is cheaper than "Calendar".
        Time today = new Time();
        today.setToNow();
        int year = today.year;
        int monthOfYear = today.month;
        int dayOfMonth = today.monthDay;
        updateDate(year, monthOfYear, dayOfMonth);
    }
}

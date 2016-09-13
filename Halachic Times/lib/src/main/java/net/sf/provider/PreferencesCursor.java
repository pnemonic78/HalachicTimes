/*
 * Source file of the Power Failure Monitor project.
 * Copyright (c) 2016. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * https://sourceforge.net/projects/power-failure
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.provider;

import android.database.MatrixCursor;

import java.util.List;

/**
 * Cursor wrapper for a single preference value.
 *
 * @author moshe.w
 */
public class PreferencesCursor extends MatrixCursor {

    private static final int INDEX_KEY = 0;
    private static final int INDEX_VALUE = 1;
    private static final int INDEX_TYPE = 2;

    public PreferencesCursor() {
        super(new String[]{Preferences.Columns.KEY, Preferences.Columns.VALUE, Preferences.Columns.TYPE}, 0);
    }

    public PreferencesCursor(String type, String key, Object value) {
        super(new String[]{Preferences.Columns.KEY, Preferences.Columns.VALUE, Preferences.Columns.TYPE}, 1);
        addRow(new Object[]{key, value, type});
    }

    public PreferencesCursor(List<String> types, List<String> keys, List<Object> values) {
        super(new String[]{Preferences.Columns.KEY, Preferences.Columns.VALUE, Preferences.Columns.TYPE}, values.size());
        int count = values.size();
        Object[] row = new Object[3];
        for (int i = 0; i < count; i++) {
            row[INDEX_TYPE] = types.get(i);
            row[INDEX_KEY] = keys.get(i);
            row[INDEX_VALUE] = values.get(i);
            addRow(row);
        }
    }

    @Override
    public int getColumnIndex(String columnName) {
        switch (columnName) {
            case Preferences.Columns.KEY:
                return INDEX_KEY;
            case Preferences.Columns.VALUE:
                return INDEX_VALUE;
            case Preferences.Columns.TYPE:
                return INDEX_TYPE;
        }
        return -1;
    }
}

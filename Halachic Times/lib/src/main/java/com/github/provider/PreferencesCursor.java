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
package com.github.provider;

import android.database.MatrixCursor;

import java.util.List;

/**
 * Cursor wrapper for a single preference value.
 *
 * @author Moshe Waisberg
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

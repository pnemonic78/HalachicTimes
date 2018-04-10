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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.provider.Preferences.AUTHORITY;

/**
 * Content provider that wraps a real {@link android.content.SharedPreferences}.
 *
 * @author Moshe Waisberg
 */
public class PreferencesProvider extends ContentProvider {

    private static final int ALL = 1;
    private static final int BOOLEAN = 2;
    private static final int FLOAT = 3;
    private static final int INT = 4;
    private static final int LONG = 5;
    private static final int STRING = 6;
    private static final int STRING_SET = 7;
    private static final int ALL_KEY = 8;

    private SharedPreferences delegate;
    private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public PreferencesProvider() {
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        delegate = PreferenceManager.getDefaultSharedPreferences(context);

        String authority = AUTHORITY(context);
        uriMatcher.addURI(authority, "", ALL);
        uriMatcher.addURI(authority, "all", ALL);
        uriMatcher.addURI(authority, "all/*", ALL_KEY);
        uriMatcher.addURI(authority, "boolean/*", BOOLEAN);
        uriMatcher.addURI(authority, "float/*", FLOAT);
        uriMatcher.addURI(authority, "int/*", INT);
        uriMatcher.addURI(authority, "integer/*", INT);
        uriMatcher.addURI(authority, "long/*", LONG);
        uriMatcher.addURI(authority, "string/*", STRING);
        uriMatcher.addURI(authority, "stringSet/*", STRING_SET);
        uriMatcher.addURI(authority, "string_set/*", STRING_SET);
        uriMatcher.addURI(authority, "strings/*", STRING_SET);

        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        if (match == ALL) {
            return Preferences.CONTENT_TYPE;
        }
        return Preferences.CONTENT_ITEM_TYPE;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String key = uri.getLastPathSegment();
        int match = uriMatcher.match(uri);
        Object value;
        String type;
        List<String> types;
        List<String> keys;
        List<Object> values;
        Cursor cursor = null;

        switch (match) {
            case ALL:
                Map<String, ?> all = delegate.getAll();
                values = new ArrayList<>(all.size());
                types = new ArrayList<>(all.size());
                keys = new ArrayList<>(all.size());
                for (Map.Entry<String, ?> entry : all.entrySet()) {
                    key = entry.getKey();
                    value = entry.getValue();
                    type = Preferences.ALL;
                    if (value instanceof Boolean) {
                        type = Preferences.BOOLEAN;
                        value = ((Boolean) value) ? 1 : 0;
                    } else if (value instanceof Float) {
                        type = Preferences.FLOAT;
                    } else if (value instanceof Integer) {
                        type = Preferences.INT;
                    } else if (value instanceof Long) {
                        type = Preferences.LONG;
                    } else if (value instanceof CharSequence) {
                        type = Preferences.STRING;
                    } else if (value instanceof Set) {
                        type = Preferences.STRING_SET;
                        value = Preferences.fromStringSet((Set<String>) value);
                    }
                    types.add(type);
                    keys.add(key);
                    values.add(value);
                }
                cursor = new PreferencesCursor(types, keys, values);
                break;
            case ALL_KEY:
                type = Preferences.ALL;
                if (delegate.contains(key)) {
                    value = delegate.getAll().get(key);
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case BOOLEAN:
                type = Preferences.BOOLEAN;
                if (delegate.contains(key)) {
                    value = delegate.getBoolean(key, false) ? 1 : 0;
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case FLOAT:
                type = Preferences.FLOAT;
                if (delegate.contains(key)) {
                    value = delegate.getFloat(key, 0);
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case INT:
                type = Preferences.INT;
                if (delegate.contains(key)) {
                    value = delegate.getInt(key, 0);
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case LONG:
                type = Preferences.LONG;
                if (delegate.contains(key)) {
                    value = delegate.getLong(key, 0);
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case STRING:
                type = Preferences.STRING;
                if (delegate.contains(key)) {
                    value = delegate.getString(key, null);
                    cursor = new PreferencesCursor(type, key, value);
                } else {
                    cursor = new PreferencesCursor();
                }
                break;
            case STRING_SET:
                type = Preferences.STRING_SET;
                if (delegate.contains(key)) {
                    Set<String> set = delegate.getStringSet(key, null);
                    values = new ArrayList<>();
                    if (set != null) {
                        values.addAll(set);
                    }
                    types = new ArrayList<>(values.size());
                    Collections.fill(types, type);
                    keys = new ArrayList<>(values.size());
                    Collections.fill(keys, key);
                    cursor = new PreferencesCursor(types, keys, values);
                } else {
                    cursor = new PreferencesCursor();
                }
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (update(uri, values, null, null) > 0) {
            return uri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int result = 0;

        switch (match) {
            case ALL:
                delegate.edit().clear().apply();
                result = 1;
                break;
            default:
                String key = uri.getLastPathSegment();
                if (delegate.contains(key)) {
                    delegate.edit().remove(key).apply();
                    result = 1;
                }
                break;
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String key = uri.getLastPathSegment();
        int match = uriMatcher.match(uri);
        SharedPreferences.Editor editor = delegate.edit();
        int result = 0;

        switch (match) {
            case ALL:
                Object value;
                for (String k : values.keySet()) {
                    key = k;
                    value = values.get(key);
                    if (value instanceof Boolean) {
                        editor.putBoolean(key, values.getAsBoolean(key));
                        result++;
                    } else if (value instanceof Float) {
                        editor.putFloat(key, values.getAsFloat(key));
                        result++;
                    } else if (value instanceof Integer) {
                        editor.putInt(key, values.getAsInteger(key));
                        result++;
                    } else if (value instanceof Long) {
                        editor.putLong(key, values.getAsLong(key));
                        result++;
                    } else if (value instanceof CharSequence) {
                        editor.putString(key, values.getAsString(key));
                        result++;
                    }
                }
                break;
            case BOOLEAN:
                editor.putBoolean(key, values.getAsBoolean(Preferences.Columns.VALUE));
                result = 1;
                break;
            case FLOAT:
                editor.putFloat(key, values.getAsFloat(Preferences.Columns.VALUE));
                result = 1;
                break;
            case INT:
                editor.putInt(key, values.getAsInteger(Preferences.Columns.VALUE));
                result = 1;
                break;
            case LONG:
                editor.putLong(key, values.getAsLong(Preferences.Columns.VALUE));
                result = 1;
                break;
            case STRING:
                editor.putString(key, values.getAsString(Preferences.Columns.VALUE));
                result = 1;
                break;
            case STRING_SET:
                editor.putStringSet(key, Preferences.toStringSet(values.getAsString(Preferences.Columns.VALUE)));
                result = 1;
                break;
        }

        editor.apply();

        return result;
    }
}

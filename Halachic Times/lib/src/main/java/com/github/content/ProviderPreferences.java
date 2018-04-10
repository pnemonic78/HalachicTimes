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
package com.github.content;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.github.provider.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.github.provider.Preferences.AUTHORITY;
import static com.github.provider.Preferences.CONTENT_URI;
import static com.github.provider.Preferences.fromStringSet;

/**
 * Shared preferences that are sharable across processes by using a content provider.
 *
 * @author Moshe Waisberg
 */
public class ProviderPreferences implements SharedPreferences, SharedPreferences.Editor {

    private static final int INDEX_KEY = 0;
    private static final int INDEX_VALUE = 1;
    private static final int INDEX_TYPE = 2;

    private final Context context;
    private final ContentResolver resolver;
    private final Uri contentUri;
    private final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    public ProviderPreferences(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.contentUri = CONTENT_URI(context);
    }

    @Override
    public Map<String, ?> getAll() {
        Map<String, Object> result = new HashMap<>();
        Uri uri = contentUri.buildUpon().appendPath(Preferences.ALL).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String key;
                do {
                    key = cursor.getString(INDEX_KEY);

                    switch (cursor.getString(INDEX_TYPE)) {
                        case Preferences.BOOLEAN:
                            result.put(key, cursor.getInt(INDEX_VALUE) != 0);
                            break;
                        case Preferences.FLOAT:
                            result.put(key, cursor.getFloat(INDEX_VALUE));
                            break;
                        case Preferences.INT:
                            result.put(key, cursor.getInt(INDEX_VALUE));
                            break;
                        case Preferences.LONG:
                            result.put(key, cursor.getLong(INDEX_VALUE));
                            break;
                        case Preferences.STRING:
                            result.put(key, cursor.getString(INDEX_VALUE));
                            break;
                        case Preferences.STRING_SET:
                            result.put(key, Preferences.toStringSet(cursor.getString(INDEX_VALUE)));
                            break;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        boolean result = defValue;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.BOOLEAN).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(INDEX_VALUE) != 0;
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public float getFloat(String key, float defValue) {
        float result = defValue;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.FLOAT).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getFloat(INDEX_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public int getInt(String key, int defValue) {
        int result = defValue;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.INT).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(INDEX_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public long getLong(String key, long defValue) {
        long result = defValue;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.LONG).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getLong(INDEX_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public String getString(String key, String defValue) {
        String result = defValue;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.STRING).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(INDEX_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> result = defValues;
        Uri uri = contentUri.buildUpon().appendPath(Preferences.STRING_SET).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = new LinkedHashSet<>();
                do {
                    result.add(cursor.getString(INDEX_VALUE));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public boolean contains(String key) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.BOOLEAN).appendEncodedPath(key).build();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        boolean result = false;
        if (cursor != null) {
            result = cursor.getCount() > 0;
            cursor.close();
        }
        return result;
    }

    @Override
    public Editor edit() {
        return this;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // TODO Implemented me!
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // TODO Implemented me!
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.BOOLEAN).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, value)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor putFloat(String key, float value) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.FLOAT).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, value)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor putInt(String key, int value) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.INT).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, value)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor putLong(String key, long value) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.LONG).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, value)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor putString(String key, String value) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.STRING).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, value)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor putStringSet(String key, Set<String> values) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.STRING_SET).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newUpdate(uri)
                .withValue(Preferences.Columns.VALUE, fromStringSet(values))
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor remove(String key) {
        Uri uri = contentUri.buildUpon().appendPath(Preferences.ALL).appendEncodedPath(key).build();
        ContentProviderOperation op = ContentProviderOperation.newDelete(uri)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public Editor clear() {
        Uri uri = contentUri;
        ContentProviderOperation op = ContentProviderOperation.newDelete(uri)
                .build();
        ops.add(op);
        return this;
    }

    @Override
    public boolean commit() {
        try {
            resolver.applyBatch(AUTHORITY(context), ops);
            ops.clear();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void apply() {
        commit();
    }
}

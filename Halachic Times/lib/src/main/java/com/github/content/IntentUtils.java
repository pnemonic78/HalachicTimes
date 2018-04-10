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
package com.github.content;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;

import java.util.concurrent.atomic.AtomicLong;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

/**
 * Time utilities.
 *
 * @author Moshe Waisberg
 */
public class IntentUtils {

    private IntentUtils() {
    }

    @TargetApi(LOLLIPOP_MR1)
    public static void putExtras(Intent src, android.os.PersistableBundle dest) {
        Bundle extras = src.getExtras();
        if ((extras == null) || extras.isEmpty()) {
            return;
        }
        Object value;
        for (String key : extras.keySet()) {
            value = extras.get(key);

            if (value == null) {
                dest.putString(key, null);
            } else if (value instanceof Boolean) {
                dest.putBoolean(key, (Boolean) value);
            } else if (value instanceof CharSequence) {
                dest.putString(key, value.toString());
            } else if (value instanceof Double) {
                dest.putDouble(key, (Double) value);
            } else if (value instanceof Float) {
                dest.putDouble(key, ((Number) value).doubleValue());
            } else if (value instanceof Long) {
                dest.putLong(key, (Long) value);
            } else if (value instanceof AtomicLong) {
                dest.putLong(key, ((AtomicLong) value).longValue());
            } else if (value instanceof Number) {
                dest.putInt(key, ((Number) value).intValue());
            } else if (value instanceof boolean[]) {
                dest.putBooleanArray(key, (boolean[]) value);
            } else if (value instanceof double[]) {
                dest.putDoubleArray(key, (double[]) value);
            } else if (value instanceof int[]) {
                dest.putIntArray(key, (int[]) value);
            } else if (value instanceof long[]) {
                dest.putLongArray(key, (long[]) value);
            } else if (value instanceof String[]) {
                dest.putStringArray(key, (String[]) value);
            }
        }
    }

    @TargetApi(LOLLIPOP_MR1)
    public static void readExtras(Intent dest, android.os.PersistableBundle src) {
        if ((src == null) || src.isEmpty()) {
            return;
        }

        Object value;
        for (String key : src.keySet()) {
            value = src.get(key);

            if (value == null) {
                dest.putExtra(key, (String) null);
            } else if (value instanceof Boolean) {
                dest.putExtra(key, (Boolean) value);
            } else if (value instanceof CharSequence) {
                dest.putExtra(key, value.toString());
            } else if (value instanceof Double) {
                dest.putExtra(key, (Double) value);
            } else if (value instanceof Long) {
                dest.putExtra(key, (Long) value);
            } else if (value instanceof Integer) {
                dest.putExtra(key, (Integer) value);
            } else if (value instanceof boolean[]) {
                dest.putExtra(key, (boolean[]) value);
            } else if (value instanceof double[]) {
                dest.putExtra(key, (double[]) value);
            } else if (value instanceof int[]) {
                dest.putExtra(key, (int[]) value);
            } else if (value instanceof long[]) {
                dest.putExtra(key, (long[]) value);
            } else if (value instanceof String[]) {
                dest.putExtra(key, (String[]) value);
            }
        }
    }
}

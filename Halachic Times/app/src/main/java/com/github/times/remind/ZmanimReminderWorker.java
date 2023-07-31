/*
 * Copyright 2021, Moshe Waisberg
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
package com.github.times.remind;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Background worker for reminders.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderWorker extends Worker {

    private static final String DATA_ACTION = "android.intent.action";
    private static final String DATA_DATA = "android.intent.data";

    /**
     * Constructs a new worker.
     *
     * @param context The context.
     * @param params  Parameters to setup the internal state of this worker.
     */
    public ZmanimReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        final Data data = getInputData();
        Intent intent = toIntent(data);
        if (intent == null) return Result.failure();
        ZmanimReminder reminder = new ZmanimReminder(getApplicationContext());
        reminder.process(intent);
        return Result.success();
    }

    @NonNull
    public static Data toWorkData(@NonNull Intent intent) {
        final Bundle extras = intent.getExtras();
        Data.Builder data = new Data.Builder();

        if ((extras != null) && !extras.isEmpty()) {
            final Map<String, Object> all = new HashMap<>(extras.size());
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value == null) continue;
                if (value instanceof CharSequence) {
                    data.putString(key, value.toString());
                } else if (value instanceof Parcelable) {
                    putParcelable(data, key, (Parcelable) value);
                } else {
                    all.put(key, value);
                }
            }
            data.putAll(all);
        }
        data.putString(DATA_ACTION, intent.getAction());
        data.putString(DATA_DATA, intent.getDataString());

        return data.build();
    }

    private static void putParcelable(Data.Builder data, String key, @NonNull Parcelable parcelable) {
        if (parcelable instanceof Location) {
            LocationData.writeToData(data, key, (Location) parcelable);
        } else {
            Timber.w("Unknown parcelable: %s", parcelable);
        }
    }

    @Nullable
    public static Intent toIntent(@Nullable Data data) {
        if (data == null) return null;
        Bundle extras = new Bundle();

        final String action = data.getString(DATA_ACTION);
        final String dataString = data.getString(DATA_DATA);

        Map<String, Object> all = data.getKeyValueMap();
        Collection<String> keysToRemove = new ArrayList<>();

        for (String key : all.keySet()) {
            Object value = all.get(key);
            if (value == null) continue;

            Location location = LocationData.readFromData(data, key, keysToRemove);
            if (location != null) {
                String locationKey = LocationData.getKey(key);
                extras.putParcelable(locationKey, location);
                continue;
            }

            if (value instanceof String) {
                extras.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                extras.putBoolean(key, (Boolean) value);
            } else if (value instanceof Boolean[]) {
                extras.putBooleanArray(key, convertBooleanArray((Boolean[]) value));
            } else if (value instanceof boolean[]) {
                extras.putBooleanArray(key, (boolean[]) value);
            } else if (value instanceof Bundle) {
                extras.putBundle(key, (Bundle) value);
            } else if (value instanceof Byte) {
                extras.putByte(key, (Byte) value);
            } else if (value instanceof Byte[]) {
                extras.putByteArray(key, convertByteArray((Byte[]) value));
            } else if (value instanceof byte[]) {
                extras.putByteArray(key, (byte[]) value);
            } else if (value instanceof Character) {
                extras.putChar(key, (Character) value);
            } else if (value instanceof Character[]) {
                extras.putCharArray(key, convertCharArray((Character[]) value));
            } else if (value instanceof char[]) {
                extras.putCharArray(key, (char[]) value);
            } else if (value instanceof CharSequence) {
                extras.putCharSequence(key, (CharSequence) value);
            } else if (value instanceof Double) {
                extras.putDouble(key, (Double) value);
            } else if (value instanceof Double[]) {
                extras.putDoubleArray(key, convertDoubleArray((Double[]) value));
            } else if (value instanceof double[]) {
                extras.putDoubleArray(key, (double[]) value);
            } else if (value instanceof Float) {
                extras.putFloat(key, (Float) value);
            } else if (value instanceof Float[]) {
                extras.putFloatArray(key, convertFloatArray((Float[]) value));
            } else if (value instanceof float[]) {
                extras.putFloatArray(key, (float[]) value);
            } else if (value instanceof Integer) {
                extras.putInt(key, (Integer) value);
            } else if (value instanceof Integer[]) {
                extras.putIntArray(key, convertIntArray((Integer[]) value));
            } else if (value instanceof int[]) {
                extras.putIntArray(key, (int[]) value);
            } else if (value instanceof Long) {
                extras.putLong(key, (Long) value);
            } else if (value instanceof Long[]) {
                extras.putLongArray(key, convertLongArray((Long[]) value));
            } else if (value instanceof long[]) {
                extras.putLongArray(key, (long[]) value);
            } else if (value instanceof Short) {
                extras.putShort(key, (Short) value);
            } else if (value instanceof Short[]) {
                extras.putShortArray(key, convertShortArray((Short[]) value));
            } else if (value instanceof short[]) {
                extras.putShortArray(key, (short[]) value);
            } else if (value instanceof String[]) {
                extras.putStringArray(key, (String[]) value);
            } else if (value instanceof Parcelable) {
                extras.putParcelable(key, (Parcelable) value);
            } else if (value instanceof Parcelable[]) {
                extras.putParcelableArray(key, (Parcelable[]) value);
            } else if (value instanceof Serializable) {
                extras.putSerializable(key, (Serializable) value);
            }
        }
        for (String key : keysToRemove) {
            extras.remove(key);
        }
        extras.remove(DATA_ACTION);
        extras.remove(DATA_DATA);

        Intent intent = new Intent()
                .putExtras(extras)
                .setAction(action);
        if (!TextUtils.isEmpty(dataString)) {
            intent.setData(Uri.parse(dataString));
        }
        return intent;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    boolean[] convertBooleanArray(@NonNull Boolean[] value) {
        boolean[] returnValue = new boolean[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    byte[] convertByteArray(@NonNull Byte[] value) {
        byte[] returnValue = new byte[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    char[] convertCharArray(@NonNull Character[] value) {
        char[] returnValue = new char[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    int[] convertIntArray(@NonNull Integer[] value) {
        int[] returnValue = new int[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    long[] convertLongArray(@NonNull Long[] value) {
        long[] returnValue = new long[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    float[] convertFloatArray(@NonNull Float[] value) {
        float[] returnValue = new float[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    double[] convertDoubleArray(@NonNull Double[] value) {
        double[] returnValue = new double[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    static @NonNull
    short[] convertShortArray(@NonNull Short[] value) {
        short[] returnValue = new short[value.length];
        for (int i = 0; i < value.length; ++i) {
            returnValue[i] = value[i];
        }
        return returnValue;
    }
}

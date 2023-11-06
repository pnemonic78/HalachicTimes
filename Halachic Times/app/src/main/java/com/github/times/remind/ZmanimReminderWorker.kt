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
package com.github.times.remind

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.Serializable
import timber.log.Timber

/**
 * Background worker for reminders.
 *
 * @param context The context.
 * @param params  Parameters to setup the internal state of this worker.
 * @author Moshe Waisberg
 */
class ZmanimReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val data = inputData
        val intent = toIntent(data) ?: return Result.failure()
        val reminder = ZmanimReminder(applicationContext)
        reminder.process(intent)
        return Result.success()
    }

    companion object {
        private const val DATA_ACTION = "android.intent.action"
        private const val DATA_DATA = "android.intent.data"

        fun toWorkData(intent: Intent): Data {
            val extras = intent.extras
            val data = Data.Builder()
            if (extras != null && !extras.isEmpty) {
                val all = mutableMapOf<String, Any>()
                for (key in extras.keySet()) {
                    val value = extras[key] ?: continue
                    if (value is CharSequence) {
                        data.putString(key, value.toString())
                    } else if (value is Parcelable) {
                        putParcelable(data, key, value)
                    } else {
                        all[key] = value
                    }
                }
                data.putAll(all)
            }
            data.putString(DATA_ACTION, intent.action)
            data.putString(DATA_DATA, intent.dataString)
            return data.build()
        }

        private fun putParcelable(data: Data.Builder, key: String, parcelable: Parcelable) {
            if (parcelable is Location) {
                LocationData.writeToData(data, key, parcelable)
            } else {
                Timber.w("Unknown parcelable: %s", parcelable)
            }
        }

        fun toIntent(data: Data?): Intent? {
            if (data == null) return null
            val extras = Bundle()
            val action = data.getString(DATA_ACTION)
            val dataString = data.getString(DATA_DATA)
            val all = data.keyValueMap
            val keysToRemove = mutableSetOf<String>()

            for (key in all.keys) {
                val value = all[key] ?: continue
                val location = LocationData.readFromData(data, key, keysToRemove)
                if (location != null) {
                    val locationKey = LocationData.getKey(key)
                    extras.putParcelable(locationKey, location)
                    continue
                }
                if (value is String) {
                    extras.putString(key, value)
                } else if (value is Boolean) {
                    extras.putBoolean(key, value)
                } else if (value is BooleanArray) {
                    extras.putBooleanArray(key, value)
                } else if (value is Bundle) {
                    extras.putBundle(key, value)
                } else if (value is Byte) {
                    extras.putByte(key, value)
                } else if (value is ByteArray) {
                    extras.putByteArray(key, value)
                } else if (value is Char) {
                    extras.putChar(key, value)
                } else if (value is CharArray) {
                    extras.putCharArray(key, value)
                } else if (value is CharSequence) {
                    extras.putCharSequence(key, value)
                } else if (value is Double) {
                    extras.putDouble(key, value)
                } else if (value is DoubleArray) {
                    extras.putDoubleArray(key, value)
                } else if (value is Float) {
                    extras.putFloat(key, value)
                } else if (value is FloatArray) {
                    extras.putFloatArray(key, value)
                } else if (value is Int) {
                    extras.putInt(key, value)
                } else if (value is IntArray) {
                    extras.putIntArray(key, value)
                } else if (value is Long) {
                    extras.putLong(key, value)
                } else if (value is LongArray) {
                    extras.putLongArray(key, value)
                } else if (value is Short) {
                    extras.putShort(key, value)
                } else if (value is ShortArray) {
                    extras.putShortArray(key, value)
                } else if (value is Array<*>) {
                    if (value.isArrayOf<String>()) {
                        extras.putStringArray(key, value as Array<String?>)
                    } else if (value.isArrayOf<Parcelable>()) {
                        extras.putParcelableArray(key, value as Array<Parcelable?>)
                    }
                } else if (value is Parcelable) {
                    extras.putParcelable(key, value)
                } else if (value is Serializable) {
                    extras.putSerializable(key, value)
                }
            }

            for (key in keysToRemove) {
                extras.remove(key)
            }
            extras.remove(DATA_ACTION)
            extras.remove(DATA_DATA)

            val intent = Intent(action)
                .putExtras(extras)
            if (!dataString.isNullOrEmpty()) {
                intent.data = Uri.parse(dataString)
            }
            return intent
        }
    }
}
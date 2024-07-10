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
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.times.BuildConfig
import com.github.times.location.LocationData
import com.github.times.location.ZmanimLocationListener
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_LOCATION_CHANGED
import com.github.times.remind.ZmanimReminder.Companion.ACTION_REMIND
import com.github.times.remind.ZmanimReminder.Companion.ACTION_SILENCE
import com.github.times.remind.ZmanimReminderItem.Companion.EXTRA_ITEM
import java.io.Serializable

/**
 * Background worker for reminders.
 *
 * @param context The context.
 * @param params  Parameters to setup the internal state of this worker.
 * @author Moshe Waisberg
 */
class ZmanimReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val intent = toIntent(inputData)
        val reminder = ZmanimReminder(applicationContext)
        reminder.process(intent)
        return Result.success()
    }

    companion object {
        private const val DATA_ACTION = "android.intent.action"
        private const val DATA_DATA = "android.intent.data"

        private const val WORK_REMINDER_TAG = BuildConfig.APPLICATION_ID + ":reminder"

        fun toWorkData(intent: Intent): Data {
            val data = Data.Builder()
            data.putString(DATA_ACTION, intent.action)
            data.putString(DATA_DATA, intent.dataString)

            val extras = intent.extras
            if (extras != null && !extras.isEmpty) {
                when (intent.action) {
                    ACTION_LOCATION_CHANGED -> putExtrasLocation(extras, data)
                    ACTION_REMIND -> putExtrasRemind(extras, data)
                    ACTION_SILENCE -> putExtrasSilence(extras, data)
                }
            }
            return data.build()
        }

        private fun putExtrasLocation(extras: Bundle, data: Data.Builder) {
            LocationData.from(extras, ZmanimLocationListener.EXTRA_LOCATION)?.also {
                LocationData.put(data, ZmanimLocationListener.EXTRA_LOCATION, it)
            }
        }

        private fun putExtrasRemind(extras: Bundle, data: Data.Builder) {
            ZmanimReminderItemData.from(extras)?.also {
                data.putReminder(EXTRA_ITEM, it)
            }
        }

        private fun putExtrasSilence(extras: Bundle, data: Data.Builder) {
            ZmanimReminderItemData.from(extras)?.also {
                data.putReminder(EXTRA_ITEM, it)
            }
        }

        fun toIntent(data: Data): Intent {
            val extras = Bundle()
            val action = data.getString(DATA_ACTION)
            val dataString = data.getString(DATA_DATA)
            val all = data.keyValueMap
            val keysToRemove = mutableSetOf<String>()

            for (key in all.keys) {
                if (keysToRemove.contains(key)) continue
                val value = all[key] ?: continue

                val location = LocationData.readFromData(data, key, keysToRemove)
                if (location != null) {
                    val locationKey = LocationData.getKey(key) ?: continue
                    extras.putParcelable(locationKey, location)
                    continue
                }

                val reminderItem = ZmanimReminderItemData.readFromData(data, key, keysToRemove)
                if (reminderItem != null) {
                    val reminderItemKey = ZmanimReminderItemData.getKey(key) ?: continue
                    ZmanimReminderItemData.put(extras, reminderItemKey, reminderItem)
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
                        @Suppress("UNCHECKED_CAST")
                        extras.putStringArray(key, value as Array<String?>)
                    } else if (value.isArrayOf<Parcelable>()) {
                        @Suppress("UNCHECKED_CAST")
                        extras.putParcelableArray(key, value as Array<Parcelable?>)
                    }
                } else if (value is Parcelable) {
                    extras.putParcelable(key, value)
                } else if (value is Serializable) {
                    extras.putSerializable(key, value)
                }
            }

            extras.remove(DATA_ACTION)
            extras.remove(DATA_DATA)
            for (key in keysToRemove) {
                extras.remove(key)
            }

            val intent = Intent(action)
                .putExtras(extras)
            if (!dataString.isNullOrEmpty()) {
                intent.data = Uri.parse(dataString)
            }
            return intent
        }

        fun enqueue(context: Context, intent: Intent) {
            val requestData = toWorkData(intent)
            val workRequest = OneTimeWorkRequest.Builder(ZmanimReminderWorker::class.java)
                .setInputData(requestData)
                .addTag(WORK_REMINDER_TAG)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_REMINDER_TAG)
        }
    }
}
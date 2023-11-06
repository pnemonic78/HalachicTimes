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
package com.github.times.remind

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.github.times.BuildConfig
import com.github.times.ZmanimItem
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import timber.log.Timber

/**
 * Reminder item for a notification.
 *
 * @author Moshe Waisberg
 */
class ZmanimReminderItem(
    @JvmField
    val id: Int,
    @JvmField
    val title: CharSequence?,
    @JvmField
    val text: CharSequence?,
    @JvmField
    val time: Long
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        time = parcel.readLong()
    )

    val isEmpty: Boolean
        get() = id == 0 || time <= 0L || title.isNullOrEmpty()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        TextUtils.writeToParcel(title, parcel, flags)
        TextUtils.writeToParcel(text, parcel, flags)
        parcel.writeLong(time)
    }

    fun put(extras: Bundle?) {
        if (extras == null) {
            return
        }
        extras.putInt(EXTRA_ID, id)
        extras.putCharSequence(EXTRA_TITLE, title)
        extras.putCharSequence(EXTRA_TEXT, text)
        extras.putLong(EXTRA_TIME, time)
    }

    fun put(intent: Intent?) {
        if (intent == null) {
            return
        }
        intent.putExtra(EXTRA_ID, id)
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_TEXT, text)
            .putExtra(EXTRA_TIME, time)
    }

    override fun toString(): String {
        return "ZmanimReminderItem{" +
            "title=" + title +
            ", text=" + text +
            ", time=" + time +
            '}'
    }

    companion object {
        /**
         * Extras' name for the reminder id.
         */
        const val EXTRA_ID = BuildConfig.APPLICATION_ID + ".REMINDER_ID"

        /**
         * Extras' name for the reminder title.
         */
        const val EXTRA_TITLE = BuildConfig.APPLICATION_ID + ".REMINDER_TITLE"

        /**
         * Extras' name for the reminder text.
         */
        const val EXTRA_TEXT = BuildConfig.APPLICATION_ID + ".REMINDER_TEXT"

        /**
         * Extras' name for the reminder time.
         */
        const val EXTRA_TIME = BuildConfig.APPLICATION_ID + ".REMINDER_TIME"

        @JvmField
        val CREATOR: Parcelable.Creator<ZmanimReminderItem> =
            object : Parcelable.Creator<ZmanimReminderItem> {
                override fun createFromParcel(source: Parcel): ZmanimReminderItem {
                    return ZmanimReminderItem(source)
                }

                override fun newArray(size: Int): Array<ZmanimReminderItem?> {
                    return arrayOfNulls(size)
                }
            }

        @JvmStatic
        fun from(item: ZmanimItem?): ZmanimReminderItem? {
            return if (item != null) {
                ZmanimReminderItem(item.titleId, item.title, item.summary, item.time)
            } else null
        }

        @JvmStatic
        fun from(context: Context, extras: Bundle?): ZmanimReminderItem? {
            if (extras == null) {
                return null
            }
            if (extras.containsKey(EXTRA_ID)) {
                val id = extras.getInt(EXTRA_ID)
                if (id == 0) {
                    return null
                }
                var contentTitle = extras.getCharSequence(EXTRA_TITLE)
                if (contentTitle.isNullOrEmpty()) {
                    try {
                        contentTitle = context.getText(id)
                    } catch (e: Resources.NotFoundException) {
                        Timber.e(e)
                    }
                }
                val contentText = extras.getCharSequence(EXTRA_TEXT)
                val time = extras.getLong(EXTRA_TIME, System.currentTimeMillis())
                if (contentTitle != null && time >= 0L) {
                    return ZmanimReminderItem(id, contentTitle, contentText, time)
                }
            }
            return null
        }

        @JvmStatic
        fun from(context: Context, intent: Intent?): ZmanimReminderItem? {
            return if (intent != null) from(context, intent.extras) else null
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun ZmanimReminderItem?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }

    return this == null || this.isEmpty
}

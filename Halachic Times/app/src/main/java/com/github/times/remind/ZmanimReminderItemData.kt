package com.github.times.remind

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcel
import android.text.TextUtils
import androidx.work.Data
import timber.log.Timber

object ZmanimReminderItemData {
    private const val DATA_LOCATION = "com.github.times.remind.ZmanimReminderItem"
    private const val DATA_KEY_SUFFIX = "/${DATA_LOCATION}"
    private const val DATA_PREFIX = "${DATA_KEY_SUFFIX}."
    private const val DATA_ID = DATA_PREFIX + "ID"
    private const val DATA_TITLE = DATA_PREFIX + "TITLE"
    private const val DATA_TEXT = DATA_PREFIX + "TEXT"
    private const val DATA_TIME = DATA_PREFIX + "TIME"

    private const val KEY = ZmanimReminderItem.EXTRA_ITEM

    fun put(data: Data.Builder, key: String, item: ZmanimReminderItem) {
        data.putString(key + DATA_KEY_SUFFIX, key)
        data.putString(key + DATA_TITLE, item.title.toString())
        data.putString(key + DATA_TEXT, item.text.toString())
        data.putInt(key + DATA_ID, item.id)
        data.putLong(key + DATA_TIME, item.time)
    }

    fun readFromData(
        data: Data,
        dataKey: String,
        keysToRemove: MutableCollection<String>
    ): ZmanimReminderItem? {
        if (!dataKey.endsWith(DATA_KEY_SUFFIX)) return null
        val value = data.getString(dataKey)
        val key = getKey(dataKey)
        if (key.isNullOrEmpty() || key != value) return null
        keysToRemove.add(dataKey)

        val id = data.getInt(key + DATA_ID, 0)
        val title = data.getString(key + DATA_TITLE)
        val text = data.getString(key + DATA_TEXT)
        val time = data.getLong(key + DATA_TIME, 0L)
        keysToRemove.add(key + DATA_ID)
        keysToRemove.add(key + DATA_TITLE)
        keysToRemove.add(key + DATA_TEXT)
        keysToRemove.add(key + DATA_TIME)
        return ZmanimReminderItem(id, title, text, time)
    }

    fun getKey(key: String?): String? {
        if (key.isNullOrEmpty()) return null
        val index = key.indexOf(DATA_KEY_SUFFIX)
        return if (index > 0) key.substring(0, index) else null
    }

    fun put(bundle: Bundle, key: String, item: ZmanimReminderItem) {
        bundle.putString(key + DATA_KEY_SUFFIX, key)
        bundle.putCharSequence(key + DATA_TITLE, item.title)
        bundle.putCharSequence(key + DATA_TEXT, item.text)
        bundle.putInt(key + DATA_ID, item.id)
        bundle.putLong(key + DATA_TIME, item.time)
    }

    fun put(intent: Intent, key: String, item: ZmanimReminderItem) {
        Bundle().apply {
            put(this, key, item)
            intent.putExtras(this)
        }
    }

    fun put(parcel: Parcel, flags: Int, item: ZmanimReminderItem) {
        parcel.writeInt(item.id)
        TextUtils.writeToParcel(item.title, parcel, flags)
        TextUtils.writeToParcel(item.text, parcel, flags)
        parcel.writeLong(item.time)
    }

    fun from(context: Context?, extras: Bundle?): ZmanimReminderItem? {
        if (extras == null) {
            return null
        }
        val key = extras.getString(KEY + DATA_KEY_SUFFIX)
        if (KEY != key) {
            return null
        }
        val id = extras.getInt(key + DATA_ID)
        if (id == 0) {
            return null
        }
        var contentTitle = extras.getCharSequence(key + DATA_TITLE)
        if (contentTitle.isNullOrEmpty()) {
            try {
                contentTitle = context?.getText(id)
            } catch (e: Resources.NotFoundException) {
                Timber.e(e)
            }
        }
        val contentText = extras.getCharSequence(key + DATA_TEXT)
        val time = extras.getLong(key + DATA_TIME, System.currentTimeMillis())
        if (contentTitle != null && time > 0L) {
            return ZmanimReminderItem(id, contentTitle, contentText, time)
        }
        return null
    }

    fun from(extras: Bundle): ZmanimReminderItem? = from(context = null, extras)

    fun from(context: Context, intent: Intent?): ZmanimReminderItem? {
        return if (intent != null) from(context, intent.extras) else null
    }
}

fun Bundle.put(key: String, item: ZmanimReminderItem) {
    ZmanimReminderItemData.put(this, key, item)
}

fun Data.Builder.putReminder(key: String, item: ZmanimReminderItem) {
    ZmanimReminderItemData.put(this, key, item)
}

fun Intent.put(key: String, item: ZmanimReminderItem?): Intent {
    item?.let { ZmanimReminderItemData.put(this, key, item) }
    return this
}

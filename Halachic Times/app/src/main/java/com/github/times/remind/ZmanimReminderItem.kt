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

import com.github.times.BuildConfig
import com.github.times.ZmanimItem
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Reminder item for a notification.
 *
 * @author Moshe Waisberg
 */
class ZmanimReminderItem(
    val id: Int,
    val title: CharSequence?,
    val text: CharSequence?,
    val time: Long
) {

    val isEmpty: Boolean
        get() = id == 0 || time <= 0L || title.isNullOrEmpty()

    override fun toString(): String {
        return "ZmanimReminderItem{" +
            "title=" + title +
            ", text=" + text +
            ", time=" + time +
            '}'
    }

    companion object {
        /**
         * Extras' name for the reminder item.
         */
        const val EXTRA_ITEM = BuildConfig.APPLICATION_ID + ".REMINDER"

        fun from(item: ZmanimItem?): ZmanimReminderItem? {
            return if (item != null) {
                ZmanimReminderItem(item.titleId, item.title, item.summary, item.time)
            } else null
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

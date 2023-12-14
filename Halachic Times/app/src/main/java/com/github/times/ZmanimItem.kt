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
package com.github.times

import androidx.annotation.StringRes
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

typealias TimeMillis = Long
typealias Zman = TimeMillis?

/**
 * Time row item.
 */
class ZmanimItem @JvmOverloads constructor(
    /**
     * The title id.
     */
    @StringRes
    val titleId: Int,
    /**
     * The title.
     */
    val title: CharSequence?,
    /**
     * The time.
     */
    val time: TimeMillis,
    /**
     * The summary.
     */
    var summary: CharSequence? = null
) : Comparable<ZmanimItem> {

    /**
     * The time label.
     */
    @JvmField
    var timeLabel: CharSequence? = null
    //TODO private set

    /**
     * Has the time elapsed?
     */
    @JvmField
    var isElapsed = false

    /**
     * Emphasize?
     */
    @JvmField
    var isEmphasis = false

    /**
     * Jewish date.
     */
    @JvmField
    var jewishDate: JewishDate? = null

    @JvmField
    var isCategory = false
    //TODO private set

    /**
     * Creates a new row item.
     */
    constructor(titleId: Int, time: TimeMillis) : this(titleId, null, time)

    /**
     * Creates a new row item.
     */
    constructor(titleId: Int, time: TimeMillis, summary: CharSequence?) : this(
        titleId,
        null,
        time,
        summary
    )

    /**
     * Creates a new category item.
     */
    constructor(label: CharSequence?) : this(0, NEVER) {
        timeLabel = label
        isCategory = true
    }

    override fun compareTo(other: ZmanimItem): Int {
        val t1 = time
        val t2 = other.time
        var c = t1.compareTo(t2)
        if (c != 0) return c

        val j1 = jewishDate
        val j2 = other.jewishDate
        c = if (j1 != null && j2 != null) j1.compareTo(j2) else 0
        if (c != 0) return c

        return titleId - other.titleId
    }

    /**
     * Is the item empty?
     *
     * @return `true` if empty.
     */
    val isEmpty: Boolean
        get() = time == NEVER || time < YEAR_1 || timeLabel == null

    /**
     * Is the item empty or elapsed?
     *
     * @return `true` if either elapsed or empty.
     */
    val isEmptyOrElapsed: Boolean
        get() = isElapsed || isEmpty

    override fun toString(): String {
        return "ZmanimItem{" +
            "title=" + (title ?: ("(0x" + Integer.toHexString(titleId) + ")")) +
            ", summary=" + summary +
            ", time=" + timeLabel +
            ", empty=" + isEmptyOrElapsed +
            '}'
    }

    companion object {
        /**
         * Unknown date.
         */
        const val NEVER = TimeMillis.MIN_VALUE

        /**
         * Start date of the Julian calendar.
         */
        const val YEAR_1 = -62135863199554L
    }
}

@OptIn(ExperimentalContracts::class)
fun ZmanimItem?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }

    return this == null || this.isEmpty
}

@OptIn(ExperimentalContracts::class)
fun ZmanimItem?.isNullOrEmptyOrElapsed(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmptyOrElapsed != null)
    }

    return isNullOrEmpty() || isElapsed
}

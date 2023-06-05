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
package com.github.times;

import com.kosherjava.zmanim.hebrewcalendar.JewishDate;

/**
 * Time row item.
 */
public class ZmanimItem implements Comparable<ZmanimItem> {

    /**
     * Unknown date.
     */
    public static final long NEVER = Long.MIN_VALUE;
    /**
     * Start date of the Julian calendar.
     */
    public static final long YEAR_1 = -62135863199554L;

    /**
     * The title id.
     */
    public final int titleId;
    /**
     * The title.
     */
    public final CharSequence title;
    /**
     * The summary.
     */
    public CharSequence summary;
    /**
     * The time.
     */
    public final long time;
    /**
     * The time label.
     */
    public CharSequence timeLabel;
    /**
     * Has the time elapsed?
     */
    public boolean elapsed;
    /**
     * Emphasize?
     */
    public boolean emphasis;
    /**
     * Jewish date.
     */
    public JewishDate jewishDate;
    private boolean category = false;

    /**
     * Creates a new row item.
     */
    public ZmanimItem(int titleId, long time) {
        this(titleId, null, time);
    }

    /**
     * Creates a new row item.
     */
    public ZmanimItem(int titleId, CharSequence title, long time) {
        this.titleId = titleId;
        this.title = title;
        this.time = time;
    }

    /**
     * Creates a new row item.
     */
    public ZmanimItem(int titleId, long time, CharSequence summary) {
        this(titleId, null, time, summary);
    }

    /**
     * Creates a new row item.
     */
    public ZmanimItem(int titleId, CharSequence title, long time, CharSequence summary) {
        this(titleId, title, time);
        this.summary = summary;
    }

    /**
     * Creates a new category item.
     */
    public ZmanimItem(CharSequence label) {
        this(0, NEVER);
        timeLabel = label;
        category = true;
    }

    @Override
    public int compareTo(ZmanimItem that) {
        long t1 = this.time;
        long t2 = that.time;
        if (t1 != t2)
            return (t1 < t2) ? -1 : +1;

        JewishDate j1 = this.jewishDate;
        JewishDate j2 = that.jewishDate;
        if ((j1 != null) && (j2 != null)) {
            return j1.compareTo(j2);
        }

        return this.titleId - that.titleId;
    }

    /**
     * Is the item empty?
     *
     * @return {@code true} if empty.
     */
    public boolean isEmpty() {
        return (time == NEVER) || (time < YEAR_1) || (timeLabel == null);
    }

    /**
     * Is the item empty or elapsed?
     *
     * @return {@code true} if either elapsed or empty.
     */
    public boolean isEmptyOrElapsed() {
        return elapsed || isEmpty();
    }

    public boolean isCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "ZmanimItem{" +
                "title=" + title +
                ", summary=" + summary +
                ", time=" + timeLabel +
                ", empty=" + isEmptyOrElapsed() +
                '}';
    }
}

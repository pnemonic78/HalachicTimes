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
package net.sf.times;

import net.sourceforge.zmanim.hebrewcalendar.JewishDate;

/**
 * Time row item.
 */
public class ZmanimItem implements Comparable<ZmanimItem> {

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    /** The title id. */
    public final int titleId;
    /** The summary. */
    public CharSequence summary;
    /** The time. */
    public final long time;
    /** The time label. */
    public CharSequence timeLabel;
    /** Has the time elapsed? */
    public boolean elapsed;
    /** Emphasize? */
    public boolean emphasis;
    /** Jewish date. */
    public JewishDate jewishDate;

    /** Creates a new row item. */
    public ZmanimItem(int titleId, long time) {
        this.titleId = titleId;
        this.time = time;
    }

    @Override
    public int compareTo(ZmanimItem that) {
        long t1 = this.time;
        long t2 = that.time;
        if (t1 != t2)
            return (t1 < t2) ? -1 : +1;
        return this.titleId - that.titleId;
    }

    /**
     * Is the item empty?
     *
     * @return {@code true} if empty.
     */
    public boolean isEmpty() {
        return elapsed || (time == NEVER) || (timeLabel == null);
    }
}

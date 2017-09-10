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
package net.sf.util;

/**
 * Time utilities.
 *
 * @author moshe on 2017/09/10.
 */
public class TimeUtils {

    private TimeUtils() {
    }

    /**
     * Round-up the time.
     *
     * @param time
     *         the time, in milliseconds.
     * @param granularity
     *         the granularity, in milliseconds.
     * @return the rounded time.
     */
    public static long roundUp(long time, long granularity) {
        if (time >= 0L) {
            return ((time + (granularity / 2)) / granularity) * granularity;
        }
        return ((time - (granularity / 2)) / granularity) * granularity;
    }
}

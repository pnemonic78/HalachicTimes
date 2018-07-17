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
package com.github.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.util.TimeUtils.isSameDay;
import static com.github.util.TimeUtils.roundUp;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test times.
 *
 * @author Moshe Waisberg
 */
public class TimeTests {

    @Test
    public void roundUpSecond() throws Exception {
        assertEquals(0L, roundUp(0, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(111, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(222, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(333, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(444, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(499, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(500, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(555, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(666, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(777, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(888, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(999, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1000, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1111, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1222, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1333, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1444, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1555, SECOND_IN_MILLIS));
        assertEquals(2000L, roundUp(1755, SECOND_IN_MILLIS));
    }

    @Test
    public void roundUpNegativeSecond() throws Exception {
        assertEquals(0L, roundUp(0, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-111, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-222, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-333, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-444, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-499, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-500, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-555, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-666, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-777, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-888, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-999, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1000, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1111, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1222, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1333, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1444, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1555, SECOND_IN_MILLIS));
        assertEquals(-2000L, roundUp(-1755, SECOND_IN_MILLIS));
    }

    @Test
    public void sameDay() throws Exception {
        long time;
        Calendar cal;
        final long now = currentTimeMillis();

        time = now;
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(now);
        assertTrue(isSameDay(cal, time));

        cal.setTimeInMillis(time);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        assertFalse(isSameDay(cal, time));

        cal.setTimeInMillis(time);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(isSameDay(cal, time));

        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        time = cal.getTimeInMillis();
        for (int j = 0; j < 24; j++) {
            for (int i = 0; i < 24; i++) {
                cal.setTimeInMillis(now);
                cal.set(Calendar.HOUR_OF_DAY, i);
                assertTrue("j=" + j + ",i=" + i, isSameDay(cal, time));
            }
            time += HOUR_IN_MILLIS;
        }
        assertFalse(isSameDay(cal, time));
    }
}

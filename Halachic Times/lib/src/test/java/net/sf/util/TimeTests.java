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

import org.junit.Test;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static junit.framework.Assert.assertEquals;
import static net.sf.util.TimeUtils.roundUp;

/**
 * Test times.
 *
 * @author moshe on 2017/09/10.
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
        assertEquals(1000L, roundUp(500, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(555, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(666, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(777, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(888, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(999, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1000, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1111, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1222, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1333, SECOND_IN_MILLIS));
        assertEquals(1000L, roundUp(1444, SECOND_IN_MILLIS));
        assertEquals(2000L, roundUp(1555, SECOND_IN_MILLIS));
    }

    @Test
    public void roundUpNegativeSecond() throws Exception {
        assertEquals(0L, roundUp(0, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-111, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-222, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-333, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-444, SECOND_IN_MILLIS));
        assertEquals(0L, roundUp(-499, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-500, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-555, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-666, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-777, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-888, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-999, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1000, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1111, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1222, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1333, SECOND_IN_MILLIS));
        assertEquals(-1000L, roundUp(-1444, SECOND_IN_MILLIS));
        assertEquals(-2000L, roundUp(-1555, SECOND_IN_MILLIS));
    }
}

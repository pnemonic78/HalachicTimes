/*
 * Copyright 2018, Moshe Waisberg
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
package com.github.io;

import com.github.nio.charset.StandardCharsets;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test streams.
 *
 * @author Moshe Waisberg
 */
public class StreamTests {

    @Test
    public void fully() throws Exception {
        byte[] buf = new byte[1000];
        Arrays.fill(buf, (byte) 123);
        InputStream in = new ByteArrayInputStream(buf);
        InputStream full = StreamUtils.readFully(in);
        assertNotNull(full);
        for (int i = 1000; i > 0; i--) {
            assertEquals(i, full.available());
            assertEquals(123, full.read());
        }
    }

    @Test
    public void stringsUTF8() throws Exception {
        String sample = "\u0590\u0591\u0000\u0010";
        byte[] buf = sample.getBytes(StandardCharsets.UTF_8);
        assertEquals(sample.length() + 2, buf.length);
        InputStream in = new ByteArrayInputStream(buf);
        String s = StreamUtils.toString(in, StandardCharsets.UTF_8);
        assertEquals(sample, s);
        in = new ByteArrayInputStream(buf);
        s = StreamUtils.toString(in);
        assertEquals(sample, s);
    }

    @Test
    public void stringsISO() throws Exception {
        String sample = "ABC\t123\r\n";
        byte[] buf = sample.getBytes(StandardCharsets.UTF_8);
        assertEquals(sample.length(), buf.length);
        InputStream in = new ByteArrayInputStream(buf);
        String s = StreamUtils.toString(in);
        assertEquals(sample, s);
        in = new ByteArrayInputStream(buf);
        s = StreamUtils.toString(in, StandardCharsets.UTF_8);
        assertEquals(sample, s);
        in = new ByteArrayInputStream(buf);
        s = StreamUtils.toString(in, StandardCharsets.US_ASCII);
        assertEquals(sample, s);
        in = new ByteArrayInputStream(buf);
        s = StreamUtils.toString(in, StandardCharsets.ISO_8859_1);
        assertEquals(sample, s);
    }
}

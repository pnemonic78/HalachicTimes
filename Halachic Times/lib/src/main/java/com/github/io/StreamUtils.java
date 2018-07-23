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
package com.github.io;

import com.github.nio.charset.StandardCharsets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Stream utilities.
 *
 * @author Moshe Waisberg
 */
public class StreamUtils {

    private static final int BUFFER_SIZE = 1024;

    private StreamUtils() {
    }

    /**
     * Read all the bytes from the input stream.
     *
     * @param in the input.
     * @return the array of bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream readFully(InputStream in) throws IOException {
        in = new BufferedInputStream(in);
        return readFully(in, in.available());
    }

    /**
     * Read all the bytes from the input stream.
     *
     * @param in   the input.
     * @param size the initial buffer size.
     * @return the array of bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream readFully(InputStream in, int size) throws IOException {
        size = Math.max(size, BUFFER_SIZE);
        RawByteArrayOutputStream out = new RawByteArrayOutputStream(size);
        final byte[] buf = new byte[BUFFER_SIZE];
        int count;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        out.close();
        return new ByteArrayInputStream(out.getByteArray(), 0, out.size());
    }

    /**
     * Convert the stream bytes to a sequence of characters.
     *
     * @param in the input.
     * @return the characters.
     * @throws IOException if an I/O error occurs.
     */
    public static CharSequence toCharSequence(InputStream in) throws IOException {
        return toCharSequence(in, StandardCharsets.UTF_8);
    }

    /**
     * Convert the stream bytes to a sequence of characters.
     *
     * @param in      the input.
     * @param charset the character encoding.
     * @return the characters.
     * @throws IOException if an I/O error occurs.
     */
    public static CharSequence toCharSequence(InputStream in, Charset charset) throws IOException {
        Reader reader = new InputStreamReader(in, charset);
        StringBuilder out = new StringBuilder(in.available());
        final char[] buf = new char[BUFFER_SIZE];
        int count;
        while ((count = reader.read(buf)) >= 0) {
            out.append(buf, 0, count);
        }
        return out;
    }

    /**
     * Convert the stream bytes to a string.
     *
     * @param in the input.
     * @return the string.
     * @throws IOException if an I/O error occurs.
     */
    public static String toString(InputStream in) throws IOException {
        return toCharSequence(in).toString();
    }

    /**
     * Convert the stream bytes to a string.
     *
     * @param in      the input.
     * @param charset the character encoding.
     * @return the string.
     * @throws IOException if an I/O error occurs.
     */
    public static String toString(InputStream in, Charset charset) throws IOException {
        return toCharSequence(in, charset).toString();
    }
}

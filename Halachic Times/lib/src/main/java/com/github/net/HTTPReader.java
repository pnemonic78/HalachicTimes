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
package com.github.net;

import com.github.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static android.text.TextUtils.isEmpty;

/**
 * HTTP reader.
 *
 * @author Moshe Waisberg
 */
public class HTTPReader {

    /**
     * Content type that is XML text.
     */
    public static final String CONTENT_APP_XML = "application/xml";
    /**
     * Content type that is XML text.
     */
    public static final String CONTENT_TEXT_XML = "text/xml";
    /**
     * Content type that is XML text.
     */
    public static final String[] CONTENT_XML = {CONTENT_APP_XML, CONTENT_TEXT_XML};

    /**
     * Creates a new reader.
     */
    private HTTPReader() {
    }

    /**
     * Read bytes from the network.
     *
     * @param url the URL.
     * @return the data - {@code null} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream read(URL url) throws IOException {
        return read(url, (String[]) null);
    }

    /**
     * Read bytes from the network.
     *
     * @param url                 the URL.
     * @param contentTypeExpected the expected content type.
     * @return the data - {@code null} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream read(URL url, String contentTypeExpected) throws IOException {
        return read(url, new String[]{contentTypeExpected});
    }

    /**
     * Read bytes from the network.
     *
     * @param url                  the URL.
     * @param contentTypesExpected the expected content types.
     * @return the data - {@code null} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream read(URL url, String[] contentTypesExpected) throws IOException {
        URLConnection conn = url.openConnection();
        HttpURLConnection hconn = null;
        if (conn instanceof HttpURLConnection) {
            hconn = (HttpURLConnection) conn;
            int code = hconn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK)
                return null;
        }
        String contentType = conn.getContentType();
        if (isEmpty(contentType)) {
            return null;
        }
        if (contentTypesExpected != null) {
            int indexSemi = contentType.indexOf(';');
            if (indexSemi >= 0) {
                contentType = contentType.substring(0, indexSemi);
            }
            boolean hasType = false;
            for (String contentTypeExpected : contentTypesExpected) {
                if (contentType.equals(contentTypeExpected)) {
                    hasType = true;
                    break;
                }
            }
            if (!hasType) {
                return null;
            }
        }

        InputStream data;
        InputStream in = null;
        try {
            in = conn.getInputStream();
            // Do NOT use Content-Length header for an exact buffer size!
            // It is not always reliable / accurate.
            final int bufferSize = Math.max(in.available(), conn.getContentLength());
            data = StreamUtils.readFully(in, bufferSize);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }
            }
            if (hconn != null) {
                hconn.disconnect();
            }
        }
        return data;
    }

}

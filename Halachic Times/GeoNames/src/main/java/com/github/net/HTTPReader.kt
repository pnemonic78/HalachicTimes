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
package com.github.net

import com.github.io.StreamUtils.readFully
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

/**
 * HTTP reader.
 *
 * @author Moshe Waisberg
 */
object HTTPReader {
    /**
     * Content type that is XML text.
     */
    const val CONTENT_APP_XML = "application/xml"

    /**
     * Content type that is XML text.
     */
    const val CONTENT_TEXT_XML = "text/xml"

    /**
     * Content types that are XML text.
     */
    @JvmField
    val CONTENT_XML = arrayOf(CONTENT_APP_XML, CONTENT_TEXT_XML)

    /**
     * Content type that is JSON text.
     */
    const val CONTENT_APP_JSON = "application/json"

    /**
     * Content types that are JSON text.
     */
    @JvmField
    val CONTENT_JSON = arrayOf(CONTENT_APP_JSON)

    /**
     * Read bytes from the network.
     *
     * @param url the URL.
     * @return the data - `null` otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(url: URL): InputStream? {
        return read(url, null as Array<String>?)
    }

    /**
     * Read bytes from the network.
     *
     * @param url                 the URL.
     * @param contentTypeExpected the expected content type.
     * @return the data - `null` otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(url: URL, contentTypeExpected: String): InputStream? {
        return read(url, arrayOf(contentTypeExpected))
    }

    /**
     * Read bytes from the network.
     *
     * @param url                  the URL.
     * @param contentTypesExpected the expected content types.
     * @return the data - `null` otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(url: URL, contentTypesExpected: Array<String>?): InputStream? {
        val conn = url.openConnection() ?: throw IOException(url.toString())
        var http: HttpURLConnection? = null
        if (conn is HttpURLConnection) {
            http = conn
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return null
            }
        }
        var contentType = conn.contentType
        if (contentType.isNullOrEmpty()) {
            http?.disconnect()
            return null
        }
        if (contentTypesExpected != null) {
            val indexSemi = contentType.indexOf(';')
            if (indexSemi >= 0) {
                contentType = contentType.substring(0, indexSemi)
            }
            var hasType = false
            for (contentTypeExpected in contentTypesExpected) {
                if (contentType == contentTypeExpected) {
                    hasType = true
                    break
                }
            }
            if (!hasType) {
                http?.disconnect()
                return null
            }
        }
        var data: InputStream
        try {
            conn.getInputStream().use { input ->
                // Do NOT use Content-Length header for an exact buffer size!
                // It is not always reliable / accurate.
                val dataSize = max(input.available(), conn.contentLength)
                data = readFully(input, dataSize)
            }
        } finally {
            http?.disconnect()
        }
        return data
    }
}
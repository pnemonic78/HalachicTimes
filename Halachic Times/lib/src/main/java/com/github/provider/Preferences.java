/*
 * Copyright 2016, Moshe Waisberg
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
package com.github.provider;

import android.content.Context;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Preferences provider columns.
 *
 * @author Moshe Waisberg
 */
public class Preferences {

    public static String AUTHORITY(Context context) {
        return context.getPackageName() + ".preferences";
    }

    public static Uri CONTENT_URI(Context context) {
        return Uri.parse("content://" + AUTHORITY(context));
    }

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of preferences.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.github.preferences";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single locale.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.github.preference";

    /**
     * All of the preferences.
     */
    public static final String ALL = "all";
    /**
     * The preference is a boolean.
     */
    public static final String BOOLEAN = "boolean";
    /**
     * The preference is a float.
     */
    public static final String FLOAT = "float";
    /**
     * The preference is an integer.
     */
    public static final String INT = "int";
    /**
     * The preference is a long.
     */
    public static final String LONG = "long";
    /**
     * The preference is a string.
     */
    public static final String STRING = "string";
    /**
     * The preference is a set of strings.
     */
    public static final String STRING_SET = "stringSet";

    public static class Columns {
        /**
         * The preference key column.
         * <p>Type: TEXT</p>
         */
        public static final String KEY = "key";
        /**
         * The preference value column.
         * <p>Type: OBJECT</p>
         */
        public static final String VALUE = "value";
        /**
         * The preference type column.
         * <p>Type: TEXT</p>
         */
        public static final String TYPE = "type";
    }

    /**
     * Encode the set of strings.
     *
     * @param set the set of strings.
     * @return the line-feed separated list of strings.
     */
    public static String fromStringSet(Set<String> set) {
        if ((set == null) || set.isEmpty()) {
            return null;
        }
        try {
            StringBuilder value = new StringBuilder();
            int i = 0;
            String charsetName = Charset.defaultCharset().name();
            for (String s : set) {
                if (i > 0) {
                    value.append('\n');
                }
                value.append(URLEncoder.encode(s, charsetName));
                i++;
            }
            return value.toString();
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen - UTF-8 always supported.
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decode a set of strings.
     *
     * @param encoded the line-feed separated list of strings.
     * @return the set of strings.
     */
    public static Set<String> toStringSet(String encoded) {
        if (encoded == null) {
            return null;
        }
        Set<String> set = new LinkedHashSet<>();
        String charsetName = Charset.defaultCharset().name();
        String[] tokens = encoded.split("\n");
        try {
            for (String s : tokens) {
                set.add(URLDecoder.decode(s, charsetName));
            }
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen - UTF-8 always supported.
            e.printStackTrace();
        }

        return set;
    }
}

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
package com.github.times.location;

import android.location.Address;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Handler for parsing the JSON response.
 *
 * @author Moshe Waisberg
 */
public interface AddressResponseJsonParser {
    /**
     * Parse the data to extract addresses.
     *
     * @param data       the JSON data.
     * @param maxResults the maximum number of results.
     * @param locale     the locale.
     * @return the list of addresses.
     * @throws JSONException if a JSON error occurs.
     * @throws IOException   if an I/O error occurs.
     */
    List<Address> parse(InputStream data, int maxResults, Locale locale) throws JSONException, IOException;
}

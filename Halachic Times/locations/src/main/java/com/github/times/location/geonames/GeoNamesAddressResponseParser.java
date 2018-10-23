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
package com.github.times.location.geonames;

import android.location.Address;

import com.github.times.location.AddressResponseParser;
import com.github.times.location.LocationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Handler for parsing the GeoNames response for addresses.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesAddressResponseParser extends AddressResponseParser {

    /**
     * Construct a new elevation parser.
     *
     * @param locale     the addresses' locale.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    protected GeoNamesAddressResponseParser(Locale locale, List<Address> results, int maxResults) {
        super(locale, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
    }
}

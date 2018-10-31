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
import android.net.Uri;

import com.github.json.UriAdapter;
import com.github.times.location.AddressResponseParser;
import com.github.times.location.LocationException;
import com.github.times.location.ZmanimAddress;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.geonames.BoundingBox;
import org.geonames.Timezone;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Handler for parsing the GeoNames response for addresses.
 *
 * @author Moshe Waisberg
 */
public class GeoNamesAddressResponseParser extends AddressResponseParser {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Uri.class, new UriAdapter())
        .registerTypeAdapter(BoundingBox.class, new GeoNamesBoxTypeAdapter())
        .registerTypeAdapter(Timezone.class, new GeoNamesTimezoneAdapter())
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create();

    /**
     * Construct a new address parser.
     *
     * @param locale     the addresses' locale.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    GeoNamesAddressResponseParser(Locale locale, List<Address> results, int maxResults) {
        super(locale, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            GeoNamesResponse response = gson.fromJson(reader, GeoNamesResponse.class);
            handleResponse(response, results, maxResults, locale);
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(GeoNamesResponse response, List<Address> results, int maxResults, Locale locale) {
        final List<Toponym> records = response.records;
        if ((records == null) || records.isEmpty()) {
            return;
        }

        Toponym toponym;
        Address address;

        final int size = Math.min(records.size(), maxResults);
        for (int i = 0; i < size; i++) {
            toponym = records.get(i);
            address = toAddress(toponym, locale);
            if (address != null) {
                results.add(address);
            }
        }
    }

    @Nullable
    private Address toAddress(@NonNull Toponym toponym, Locale locale) {
        ZmanimAddress address = new ZmanimAddress(locale);
        address.setFeatureName(toponym.name);

        address.setLatitude(toponym.latitude);
        address.setLongitude(toponym.longitude);

        address.setAdminArea(toponym.adminName1);
        address.setCountryCode(toponym.countryCode);
        address.setCountryName(toponym.countryName);
        Integer elevation = toponym.elevation;
        if (elevation != null) {
            address.setElevation(elevation);
        }
        address.setSubAdminArea(toponym.adminName2);
        return address;
    }
}

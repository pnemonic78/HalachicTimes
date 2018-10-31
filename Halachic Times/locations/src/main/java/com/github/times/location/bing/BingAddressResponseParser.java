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
package com.github.times.location.bing;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.text.TextUtils.isEmpty;

/**
 * Handler for parsing the JSON response for addresses.
 *
 * @author Moshe Waisberg
 */
public class BingAddressResponseParser extends AddressResponseParser {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Uri.class, new UriAdapter())
        .create();

    /**
     * Construct a new address parser.
     *
     * @param locale     the addresses' locale.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    BingAddressResponseParser(Locale locale, List<Address> results, int maxResults) {
        super(locale, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            BingResponse response = gson.fromJson(reader, BingResponse.class);
            handleResponse(response, results, maxResults, locale);
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(BingResponse response, List<Address> results, int maxResults, Locale locale) {
        if (response.statusCode != BingResponse.STATUS_OK) {
            return;
        }

        final List<BingResponse.ResourceSet> resourceSets = response.resourceSets;
        if ((resourceSets == null) || resourceSets.isEmpty()) {
            return;
        }

        BingResponse.ResourceSet resourceSet = resourceSets.get(0);
        List<BingResource> resources = resourceSet.resources;
        if ((resources == null) || resources.isEmpty()) {
            return;
        }

        BingResource resource;
        Address address;

        final int size = Math.min(resources.size(), maxResults);
        for (int i = 0; i < size; i++) {
            resource = resources.get(i);
            address = toAddress(resource, locale);
            if (address != null) {
                results.add(address);
            }
        }
    }

    @Nullable
    private Address toAddress(@NonNull BingResource resource, Locale locale) {
        Address address = new ZmanimAddress(locale);
        address.setFeatureName(resource.name);

        BingPoint point = resource.point;
        if ((point == null) || (point.coordinates == null) || (point.coordinates.length < 2)) {
            return null;
        }
        address.setLatitude(point.coordinates[0]);
        address.setLongitude(point.coordinates[1]);

        BingAddress bingAddress = resource.address;
        if (bingAddress == null) {
            return null;
        }
        if (!isEmpty(bingAddress.addressLine)) {
            address.setAddressLine(0, bingAddress.addressLine);
        }
        address.setAdminArea(bingAddress.adminDistrict);
        address.setSubAdminArea(bingAddress.adminDistrict2);
        address.setCountryName(bingAddress.countryRegion);
        address.setLocality(bingAddress.locality);
        address.setPostalCode(bingAddress.postalCode);
        String formatted = bingAddress.formattedAddress;
        if ((formatted != null) && formatted.equals(resource.name)) {
            address.setFeatureName(null);
        }
        return address;
    }
}

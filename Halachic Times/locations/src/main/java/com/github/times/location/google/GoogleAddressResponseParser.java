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
package com.github.times.location.google;

import android.location.Address;

import com.github.times.location.AddressResponseParser;
import com.github.times.location.LocationException;
import com.github.times.location.ZmanimAddress;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

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
class GoogleAddressResponseParser extends AddressResponseParser {

    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(AddressComponentType.class, new AddressComponentTypeAdapter())
        .create();

    /**
     * Construct a new address parser.
     *
     * @param locale     the addresses' locale.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    GoogleAddressResponseParser(Locale locale, List<Address> results, int maxResults) {
        super(locale, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            GeocodingResponse response = gson.fromJson(reader, GeocodingResponse.class);
            handleResponse(response, results, maxResults, locale);
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(GeocodingResponse response, List<Address> results, int maxResults, Locale locale) throws LocationException {
        if (!response.successful()) {
            throw new LocationException(response.errorMessage);
        }

        final GeocodingResult[] responseResults = response.results;
        if ((responseResults == null) || (responseResults.length == 0)) {
            return;
        }

        GeocodingResult geocoderResult;
        Address address;

        final int size = Math.min(responseResults.length, maxResults);
        for (int i = 0; i < size; i++) {
            geocoderResult = responseResults[i];
            address = toAddress(geocoderResult, locale);
            if (address != null) {
                results.add(address);
            }
        }
    }

    @Nullable
    private Address toAddress(@NonNull GeocodingResult result, Locale locale) {
        ZmanimAddress address = new ZmanimAddress(locale);
        //address.setFormatted(result.getFormattedAddress());

        LatLng location = result.geometry.location;
        address.setLatitude(location.lat);
        address.setLongitude(location.lng);

        String longName;
        String shortName;
        AddressComponentType addressComponentType;

        AddressComponent[] components = result.addressComponents;
        for (AddressComponent component : components) {
            longName = component.longName;
            shortName = component.shortName;
            addressComponentType = component.types[0];
            if (addressComponentType == null) {
                continue;
            }

            switch (addressComponentType) {
                case ADMINISTRATIVE_AREA_LEVEL_1:
                    address.setAdminArea(isEmpty(shortName) ? longName : shortName);
                    break;
                case ADMINISTRATIVE_AREA_LEVEL_2:
                    address.setSubAdminArea(isEmpty(shortName) ? longName : shortName);
                    break;
                case COUNTRY:
                    address.setCountryCode(shortName);
                    address.setCountryName(longName);
                    break;
                case LOCALITY:
                    address.setLocality(isEmpty(shortName) ? longName : shortName);
                    break;
                case NATURAL_FEATURE:
                    address.setFeatureName(isEmpty(shortName) ? longName : shortName);
                    break;
                case POSTAL_CODE:
                    address.setPostalCode(isEmpty(shortName) ? longName : shortName);
                    break;
                case PREMISE:
                    address.setPremises(isEmpty(shortName) ? longName : shortName);
                    break;
                case ROUTE:
                    address.setAddressLine(2, isEmpty(shortName) ? longName : shortName);
                    break;
                case STREET_ADDRESS:
                    address.setAddressLine(1, isEmpty(shortName) ? longName : shortName);
                    break;
                case STREET_NUMBER:
                    address.setAddressLine(0, isEmpty(shortName) ? longName : shortName);
                    break;
                case SUBLOCALITY:
                    address.setSubLocality(isEmpty(shortName) ? longName : shortName);
                    break;
            }
        }
        return address;
    }
}

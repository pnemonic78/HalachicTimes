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
import java.util.ArrayList;
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

    @Override
    public List<Address> parse(InputStream data, double latitude, double longitude, int maxResults, Locale locale) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            GeocodingResponse response = gson.fromJson(reader, GeocodingResponse.class);
            List<Address> results = new ArrayList<>(maxResults);
            handleResponse(response, results, maxResults, locale);
            return results;
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
    private Address toAddress(@NonNull GeocodingResult response, Locale locale) {
        ZmanimAddress result = new ZmanimAddress(locale);
        //result.setFormatted(result.getFormattedAddress());

        LatLng location = response.geometry.location;
        result.setLatitude(location.lat);
        result.setLongitude(location.lng);

        String longName;
        String shortName;
        AddressComponentType addressComponentType;
        boolean hasResult = false;

        AddressComponent[] components = response.addressComponents;
        for (AddressComponent component : components) {
            longName = component.longName;
            shortName = component.shortName;
            addressComponentType = component.types[0];
            if (addressComponentType == null) {
                continue;
            }

            switch (addressComponentType) {
                case ADMINISTRATIVE_AREA_LEVEL_1:
                    result.setAdminArea(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case ADMINISTRATIVE_AREA_LEVEL_2:
                    result.setSubAdminArea(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case COUNTRY:
                    result.setCountryCode(shortName);
                    result.setCountryName(longName);
                    hasResult = true;
                    break;
                case LOCALITY:
                    result.setLocality(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case NATURAL_FEATURE:
                    result.setFeatureName(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case POSTAL_CODE:
                    result.setPostalCode(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case PREMISE:
                    result.setPremises(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case ROUTE:
                    result.setAddressLine(2, isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case STREET_ADDRESS:
                    result.setAddressLine(1, isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case STREET_NUMBER:
                    result.setAddressLine(0, isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
                case SUBLOCALITY:
                    result.setSubLocality(isEmpty(shortName) ? longName : shortName);
                    hasResult = true;
                    break;
            }
        }
        return hasResult ? result : null;
    }
}

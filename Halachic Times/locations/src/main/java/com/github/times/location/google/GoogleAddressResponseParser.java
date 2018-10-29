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
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import com.google.gson.FieldNamingPolicy;
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
class GoogleAddressResponseParser extends AddressResponseParser {

    private static final String TYPE_ADMIN = "administrative_area_level_1";
    private static final String TYPE_COUNTRY = "country";
    private static final String TYPE_FEATURE = "feature_name";
    private static final String TYPE_LOCALITY = "locality";
    private static final String TYPE_POLITICAL = "political";
    private static final String TYPE_POSTAL_CODE = "postal_code";
    private static final String TYPE_PREMISE = "premise";
    private static final String TYPE_ROUTE = "route";
    private static final String TYPE_STREET = "street_address";
    private static final String TYPE_STREET_NUMBER = "street_number";
    private static final String TYPE_SUBADMIN = "administrative_area_level_2";
    private static final String TYPE_SUBLOCALITY = "sublocality";

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
        Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
        try {
            Reader reader = new InputStreamReader(data);
            GeocodeResponse response = gson.fromJson(reader, GeocodeResponse.class);
            handleResponse(response, results, maxResults, locale);
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(GeocodeResponse response, List<Address> results, int maxResults, Locale locale) {
        if (response.getStatus() != GeocoderStatus.OK) {
            return;
        }

        final List<GeocoderResult> responseResults = response.getResults();
        if ((responseResults == null) || responseResults.isEmpty()) {
            return;
        }

        GeocoderResult geocoderResult;
        Address address;

        final int size = Math.min(responseResults.size(), maxResults);
        for (int i = 0; i < size; i++) {
            geocoderResult = responseResults.get(i);
            address = toAddress(geocoderResult, locale);
            if (address != null) {
                results.add(address);
            }
        }
    }

    @Nullable
    private Address toAddress(@NonNull GeocoderResult result, Locale locale) {
        ZmanimAddress address = new ZmanimAddress(locale);
        //address.setFormatted(result.getFormattedAddress());

        LatLng location = result.getGeometry().getLocation();
        address.setLatitude(location.getLat().doubleValue());
        address.setLongitude(location.getLng().doubleValue());

        String longName;
        String shortName;

        List<GeocoderAddressComponent> components = result.getAddressComponents();
        for (GeocoderAddressComponent component : components) {
            longName = component.getLongName();
            shortName = component.getShortName();

            switch (component.getTypes().get(0)) {
                case TYPE_ADMIN:
                    address.setAdminArea(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_COUNTRY:
                    address.setCountryCode(shortName);
                    address.setCountryName(longName);
                    break;
                case TYPE_FEATURE:
                    address.setFeatureName(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_LOCALITY:
                    address.setLocality(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_POLITICAL:
                    break;
                case TYPE_POSTAL_CODE:
                    address.setPostalCode(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_PREMISE:
                    address.setPremises(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_ROUTE:
                    address.setAddressLine(2, isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_STREET:
                    address.setAddressLine(1, isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_STREET_NUMBER:
                    address.setAddressLine(0, isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_SUBADMIN:
                    address.setSubAdminArea(isEmpty(shortName) ? longName : shortName);
                    break;
                case TYPE_SUBLOCALITY:
                    address.setSubLocality(isEmpty(shortName) ? longName : shortName);
                    break;
            }
        }
        return address;
    }
}

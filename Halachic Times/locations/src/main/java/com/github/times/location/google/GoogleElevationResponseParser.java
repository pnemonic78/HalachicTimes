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

import android.location.Location;

import com.github.times.location.ElevationResponseParser;
import com.github.times.location.LocationException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Handler for parsing an elevation from a Google XML response.
 *
 * @author Moshe Waisberg
 */
class GoogleElevationResponseParser extends ElevationResponseParser {

    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    @Override
    public List<Location> parse(InputStream data, double latitude, double longitude, int maxResults) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            ElevationResponse response = gson.fromJson(reader, ElevationResponse.class);
            List<Location> results = new ArrayList<>(maxResults);
            handleResponse(response, results, maxResults);
            return results;
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(ElevationResponse response, List<Location> results, int maxResults) throws LocationException {
        if (!response.successful()) {
            throw new LocationException(response.errorMessage);
        }

        ElevationResult geocoderResult = response.getResult();
        Location location = toLocation(geocoderResult);
        if (location != null) {
            results.add(location);
        }
    }

    @Nullable
    private Location toLocation(@NonNull ElevationResult response) {
        Location result = new Location(USER_PROVIDER);

        LatLng location = response.location;
        result.setLatitude(location.lat);
        result.setLongitude(location.lng);
        result.setAltitude(response.elevation);
        result.setAccuracy((float) response.resolution);

        return result;
    }
}

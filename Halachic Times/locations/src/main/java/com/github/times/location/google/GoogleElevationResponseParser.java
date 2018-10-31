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
import com.github.util.LogUtils;
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

    private static final String TAG = "GoogleElevationResponseParser";

    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    public GoogleElevationResponseParser(double latitude, double longitude, List<Location> results, int maxResults) {
        super(latitude, longitude, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data);
            ElevationResponse response = gson.fromJson(reader, ElevationResponse.class);
            handleResponse(response, results, maxResults);
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(ElevationResponse response, List<Location> results, int maxResults) {
        if (!response.successful()) {
            LogUtils.e(TAG, response.errorMessage);
            return;
        }

        ElevationResult geocoderResult = response.getResult();
        Location location = toLocation(geocoderResult);
        if (location != null) {
            results.add(location);
        }
    }

    @Nullable
    private Location toLocation(@NonNull ElevationResult result) {
        Location elevated = new Location(USER_PROVIDER);

        LatLng location = result.location;
        elevated.setLatitude(location.lat);
        elevated.setLongitude(location.lng);
        elevated.setAltitude(result.elevation);
        elevated.setAccuracy((float) result.resolution);

        return elevated;
    }
}

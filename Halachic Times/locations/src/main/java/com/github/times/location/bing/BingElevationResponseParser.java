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

import android.location.Location;
import android.net.Uri;

import com.github.json.UriAdapter;
import com.github.nio.charset.StandardCharsets;
import com.github.times.location.ElevationResponseParser;
import com.github.times.location.LocationException;
import com.github.times.location.ZmanimLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
 * Handler for parsing the Bing response for elevations.
 *
 * @author Moshe Waisberg
 */
public class BingElevationResponseParser extends ElevationResponseParser {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Uri.class, new UriAdapter())
        .create();

    @Override
    public List<Location> parse(InputStream data, double latitude, double longitude, int maxResults) throws LocationException, IOException {
        try {
            Reader reader = new InputStreamReader(data, StandardCharsets.UTF_8);
            BingResponse response = gson.fromJson(reader, BingResponse.class);
            List<Location> results = new ArrayList<>(maxResults);
            handleResponse(response, results, latitude, longitude, maxResults);
            return results;
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new LocationException(e);
        }
    }

    private void handleResponse(BingResponse response, List<Location> results, double latitude, double longitude, int maxResults) throws LocationException {
        if (response == null) return;
        if (response.statusCode != BingResponse.STATUS_OK) {
            throw new LocationException(response.statusDescription);
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

        Location location;
        final int size = Math.min(maxResults, resources.size());
        for (BingResource resource : resources) {
            if (resource == null) continue;
            location = toLocation(resource, latitude, longitude);
            if (location != null) {
                results.add(location);
                if (results.size() >= size) {
                    return;
                }
            }
        }
    }

    @Nullable
    private Location toLocation(@NonNull BingResource response, double latitude, double longitude) {
        Location result = new ZmanimLocation(USER_PROVIDER);
        result.setLatitude(latitude);
        result.setLongitude(longitude);

        BingPoint point = response.point;
        if ((point != null) && (point.coordinates != null) && (point.coordinates.length >= 2)) {
            result.setLatitude(point.coordinates[0]);
            result.setLongitude(point.coordinates[1]);
        }

        Double[] elevations = response.elevations;
        if ((elevations == null) || (elevations.length < 1)) {
            return null;
        }
        result.setAltitude(elevations[0]);
        return result;
    }
}

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

import android.location.Location;

import com.github.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Handler for parsing the textual elevation response.
 *
 * @author Moshe Waisberg
 */
public class TextElevationResponseParser extends ElevationResponseParser {

    /**
     * Lowest possible natural elevation on the surface of the earth.
     */
    private static final double ELEVATION_LOWEST_SURFACE = ZmanimLocation.ELEVATION_MIN;
    /**
     * Highest possible natural elevation from the surface of the earth.
     */
    private static final double ELEVATION_SPACE = ZmanimLocation.ELEVATION_MAX;

    @Override
    public List<Location> parse(InputStream data, double latitude, double longitude, int maxResults) throws LocationException, IOException {
        String text = StreamUtils.toString(data);
        if (isEmpty(text)) {
            throw new LocationException("empty elevation");
        }

        List<Location> results = new ArrayList<>(maxResults);
        Location location = toLocation(text, latitude, longitude);
        if (location != null) {
            results.add(location);
        }
        return results;
    }

    @Nullable
    private Location toLocation(@NonNull String response, double latitude, double longitude) throws LocationException {
        try {
            double elevation = Double.parseDouble(response);
            if (elevation <= ELEVATION_LOWEST_SURFACE) {
                Timber.w("elevation too low: %s", response);
                return null;
            }
            if (elevation >= ELEVATION_SPACE) {
                Timber.w("elevation too high: %s", response);
                return null;
            }
            Location result = new Location(USER_PROVIDER);
            result.setTime(System.currentTimeMillis());
            result.setLatitude(latitude);
            result.setLongitude(longitude);
            result.setAltitude(elevation);
            return result;
        } catch (NumberFormatException e) {
            Timber.e(e, "Bad elevation: [" + response + "] at " + latitude + "," + longitude);
            throw new LocationException(e);
        }
    }
}

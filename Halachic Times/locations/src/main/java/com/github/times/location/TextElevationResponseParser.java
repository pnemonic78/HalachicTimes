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
import com.github.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Handler for parsing the textual elevation response.
 *
 * @author Moshe Waisberg
 */
public class TextElevationResponseParser extends ElevationResponseParser {

    private static final String TAG = "TextElevationResponseParser";

    /**
     * Lowest possible natural elevation on the surface of the earth.
     */
    private static final double ELEVATION_LOWEST_SURFACE = -500;

    /**
     * Construct a new elevation parser.
     *
     * @param latitude   the latitude.
     * @param longitude  the longitude.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    public TextElevationResponseParser(double latitude, double longitude, List<Location> results, int maxResults) {
        super(latitude, longitude, results, maxResults);
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        String text = StreamUtils.toString(data);
        double elevation;
        ZmanimLocation elevated;
        try {
            elevation = Double.parseDouble(text);
            if (elevation <= ELEVATION_LOWEST_SURFACE) {
                return;
            }
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(latitude);
            elevated.setLongitude(longitude);
            elevated.setAltitude(elevation);
            results.add(elevated);
        } catch (NumberFormatException nfe) {
            LogUtils.e(TAG, "Bad elevation: [" + text + "] at " + latitude + "," + longitude, nfe);
            throw new LocationException(nfe);
        }
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Handler for parsing elevations.
 *
 * @author Moshe Waisberg
 */
public abstract class ElevationResponseParser {

    protected final double latitude;
    protected final double longitude;
    protected final List<Location> results;
    protected final int maxResults;

    /**
     * Construct a new elevation parser.
     *
     * @param latitude   the latitude.
     * @param longitude  the longitude.
     * @param results    the list of results to populate.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     */
    protected ElevationResponseParser(double latitude, double longitude, List<Location> results, int maxResults) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.results = results;
        this.maxResults = maxResults;
    }

    /**
     * Parse the data to extract elevations.
     *
     * @param data the JSON data.
     * @throws LocationException if a location error occurs.
     * @throws IOException       if an I/O error occurs.
     */
    public abstract void parse(InputStream data) throws LocationException, IOException;
}

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

/**
 * Handler for parsing the JSON response.
 *
 * @author Moshe Waisberg
 */
public interface ElevationResponseJsonParser {
    /**
     * Parse the data to extract an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the JSON data.
     * @return the location.
     * @throws IOException       if an I/O error occurs.
     * @throws LocationException if a location error occurs.
     */
    Location parse(double latitude, double longitude, InputStream data) throws IOException, LocationException;
}

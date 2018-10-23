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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;

/**
 * Handler for parsing an elevation from a Google XML response.
 *
 * @author Moshe Waisberg
 */
class GoogleElevationResponseParser extends ElevationResponseParser {

    private final SAXParser parser;

    public GoogleElevationResponseParser(double latitude, double longitude, List<Location> results, int maxResults, SAXParser parser) {
        super(latitude, longitude, results, maxResults);
        this.parser = parser;
    }

    @Override
    public void parse(InputStream data) throws LocationException, IOException {
        DefaultHandler handler = new GoogleElevationResponseHandler(latitude, longitude, results);
        try {
            parser.parse(data, handler);
        } catch (SAXException e) {
            throw new LocationException(e);
        }
    }
}

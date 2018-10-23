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

import com.github.times.location.DefaultAddressResponseHandler;
import com.github.times.location.ElevationResponseParser;
import com.github.times.location.LocationException;
import com.github.times.location.ZmanimLocation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;

import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Handler for parsing the XML response for an elevation.
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

    private static class GoogleElevationResponseHandler extends DefaultAddressResponseHandler {

        /**
         * Parse state.
         */
        private enum State {
            START, ROOT, STATUS, RESULT, LOCATION, FINISH
        }

        private static final String STATUS_OK = "OK";

        private static final String TAG_ROOT = "ElevationResponse";
        private static final String TAG_STATUS = "status";
        private static final String TAG_RESULT = "result";
        private static final String TAG_LOCATION = "location";
        private static final String TAG_LATITUDE = "lat";
        private static final String TAG_LONGITUDE = "lng";
        private static final String TAG_ELEVATION = "elevation";

        private final double latitude;
        private final double longitude;
        private final List<Location> results;
        private State state = State.START;
        private ZmanimLocation location;

        public GoogleElevationResponseHandler(double latitude, double longitude, List<Location> results) {
            super(null, 1, null);
            this.latitude = latitude;
            this.longitude = longitude;
            this.results = results;
        }

        @Override
        protected void startElement(String uri, String localName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, attributes);

            switch (state) {
                case START:
                    if (TAG_ROOT.equals(localName))
                        state = State.ROOT;
                    else
                        throw new SAXException("Unexpected root element " + localName);
                    break;
                case ROOT:
                    switch (localName) {
                        case TAG_STATUS:
                            state = State.STATUS;
                            break;
                        case TAG_RESULT:
                            state = State.RESULT;
                            break;
                    }
                    break;
                case RESULT:
                    if (TAG_LOCATION.equals(localName)) {
                        location = new ZmanimLocation(USER_PROVIDER);
                        location.setTime(System.currentTimeMillis());
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        state = State.LOCATION;
                    }
                    break;
                case FINISH:
                default:
                    break;
            }
        }

        @Override
        protected void endElement(String uri, String localName, String qName, String text) throws SAXException {
            super.endElement(uri, localName, qName, text);

            switch (state) {
                case ROOT:
                    if (TAG_ROOT.equals(localName))
                        state = State.FINISH;
                    break;
                case STATUS:
                    if (TAG_STATUS.equals(localName))
                        state = State.ROOT;
                    if (!STATUS_OK.equals(text))
                        state = State.FINISH;
                    break;
                case RESULT:
                    switch (localName) {
                        case TAG_ELEVATION:
                            if ((location != null) && (results.size() < maxResults)) {
                                try {
                                    location.setAltitude(Double.parseDouble(text));
                                    results.add(location);
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(text, nfe);
                                }
                            }
                            break;
                        case TAG_RESULT:
                            location = null;
                            state = State.ROOT;
                            break;
                    }
                    break;
                case LOCATION:
                    switch (localName) {
                        case TAG_LATITUDE:
                            if (location != null) {
                                try {
                                    location.setLatitude(Double.parseDouble(text));
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(text, nfe);
                                }
                            }
                            break;
                        case TAG_LONGITUDE:
                            if (location != null) {
                                try {
                                    location.setLongitude(Double.parseDouble(text));
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(text, nfe);
                                }
                            }
                            break;
                        case TAG_LOCATION:
                            state = State.RESULT;
                            break;
                    }
                    break;
                case FINISH:
                default:
                    break;
            }
        }
    }
}

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

import com.github.times.location.DefaultAddressResponseHandler;
import com.github.times.location.ZmanimLocation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.List;

import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Handler for parsing the XML response for an elevation.
 *
 * @author Moshe Waisberg
 */
class BingElevationResponseHandler extends DefaultAddressResponseHandler {

    /**
     * Parse state.
     */
    private enum State {
        START, ROOT, STATUS, RESOURCE_SETS, RESOURCE_SET, RESOURCES, ELEVATION_DATA, ELEVATION, FINISH
    }

    private static final String STATUS_OK = "200";

    private static final String TAG_ROOT = "Response";
    private static final String TAG_STATUS = "StatusCode";
    private static final String TAG_RESOURCE_SETS = "ResourceSets";
    private static final String TAG_RESOURCE_SET = "ResourceSet";
    private static final String TAG_RESOURCES = "Resources";
    private static final String TAG_ELEVATION_DATA = "ElevationData";
    private static final String TAG_ELEVATIONS = "Elevations";
    private static final String TAG_ELEVATION = "int";

    private State state = State.START;
    private final double latitude;
    private final double longitude;
    private final List<ZmanimLocation> results;
    private ZmanimLocation location;

    /**
     * Constructs a new parse handler.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param results   the destination results.
     */
    public BingElevationResponseHandler(double latitude, double longitude, List<ZmanimLocation> results) {
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
                    case TAG_RESOURCE_SETS:
                        state = State.RESOURCE_SETS;
                        break;
                }
                break;
            case RESOURCE_SETS:
                if (TAG_RESOURCE_SET.equals(localName))
                    state = State.RESOURCE_SET;
                break;
            case RESOURCE_SET:
                if (TAG_RESOURCES.equals(localName))
                    state = State.RESOURCES;
                break;
            case RESOURCES:
                if (TAG_ELEVATION_DATA.equals(localName)) {
                    state = State.ELEVATION_DATA;
                    location = new ZmanimLocation(USER_PROVIDER);
                    location.setTime(System.currentTimeMillis());
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                }
                break;
            case ELEVATION_DATA:
                if (TAG_ELEVATIONS.equals(localName))
                    state = State.ELEVATION;
                break;
            case ELEVATION:
                break;
            case FINISH:
                return;
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
            case RESOURCE_SETS:
                if (TAG_RESOURCE_SETS.equals(localName))
                    state = State.ROOT;
                break;
            case RESOURCE_SET:
                if (TAG_RESOURCE_SET.equals(localName))
                    state = State.RESOURCE_SETS;
                break;
            case RESOURCES:
                if (TAG_RESOURCES.equals(localName))
                    state = State.RESOURCE_SET;
                break;
            case ELEVATION_DATA:
                if (TAG_ELEVATION_DATA.equals(localName)) {
                    if (location != null) {
                        if (location.hasAltitude())
                            results.add(location);
                        else
                            state = State.FINISH;
                        location = null;
                    }
                    state = State.RESOURCES;
                }
                break;
            case ELEVATION:
                switch (localName) {
                    case TAG_ELEVATION:
                        if (location != null) {
                            try {
                                location.setAltitude(Double.parseDouble(text));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(text, nfe);
                            }
                        }
                        break;
                    case TAG_ELEVATIONS:
                        state = State.ELEVATION_DATA;
                        break;
                }
                break;
            case FINISH:
            default:
                break;
        }
    }
}

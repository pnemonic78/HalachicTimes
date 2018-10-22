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

import android.location.Address;

import com.github.times.location.DefaultAddressResponseHandler;
import com.github.times.location.ZmanimAddress;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.Locale;

/**
 * Handler for parsing the XML response for addresses.
 *
 * @author Moshe Waisberg
 */
@Deprecated
class BingAddressResponseHandler extends DefaultAddressResponseHandler {

    /**
     * Parse state.
     */
    private enum State {
        START, ROOT, STATUS, RESOURCE_SETS, RESOURCE_SET, RESOURCES, LOCATION, POINT, ADDRESS, FINISH
    }

    private static final String STATUS_OK = "200";

    private static final String TAG_ROOT = "Response";
    private static final String TAG_STATUS = "StatusCode";
    private static final String TAG_RESOURCE_SETS = "ResourceSets";
    private static final String TAG_RESOURCE_SET = "ResourceSet";
    private static final String TAG_RESOURCES = "Resources";
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_NAME = "Name";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_ADDRESS_LINE = "AddressLine";
    private static final String TAG_ADDRESS_DISTRICT = "AdminDistrict";
    private static final String TAG_ADDRESS_DISTRICT2 = "AdminDistrict2";
    private static final String TAG_ADDRESS_COUNTRY = "CountryRegion";
    private static final String TAG_ADDRESS_FORMATTED = "FormattedAddress";
    private static final String TAG_LOCALITY = "Locality";
    private static final String TAG_POINT = "Point";
    private static final String TAG_LATITUDE = "Latitude";
    private static final String TAG_LONGITUDE = "Longitude";

    private State state = State.START;
    private final List<Address> results;
    private final int maxResults;
    private final Locale locale;
    private Address address;

    /**
     * Constructs a new parse handler.
     *
     * @param results    the destination results.
     * @param maxResults the maximum number of results.
     */
    public BingAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        this.results = results;
        this.maxResults = maxResults;
        this.locale = locale;
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
                if (TAG_LOCATION.equals(localName)) {
                    state = State.LOCATION;
                    address = new ZmanimAddress(locale);
                }
                break;
            case LOCATION:
                switch (localName) {
                    case TAG_POINT:
                        state = State.POINT;
                        break;
                    case TAG_ADDRESS:
                        state = State.ADDRESS;
                        break;
                }
                break;
            case POINT:
            case ADDRESS:
            case FINISH:
            default:
                break;
        }
    }

    @Override
    protected void endElement(String uri, String localName, String qName, String text) throws SAXException {
        super.endElement(uri, localName, qName, text);

        String prev;

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
            case LOCATION:
                switch (localName) {
                    case TAG_NAME:
                        if (address != null) {
                            prev = address.getFeatureName();
                            address.setFeatureName((prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_LOCATION:
                        if (address != null) {
                            if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                        state = State.RESOURCES;
                        break;
                }
                break;
            case POINT:
                switch (localName) {
                    case TAG_LATITUDE:
                        if (address != null) {
                            try {
                                address.setLatitude(Double.parseDouble(text));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(text, nfe);
                            }
                        }
                        break;
                    case TAG_LONGITUDE:
                        if (address != null) {
                            try {
                                address.setLongitude(Double.parseDouble(text));
                            } catch (NumberFormatException nfe) {
                                throw new SAXException(text, nfe);
                            }
                        }
                        break;
                }
                if (TAG_POINT.equals(localName))
                    state = State.LOCATION;
                break;
            case ADDRESS:
                switch (localName) {
                    case TAG_ADDRESS_LINE:
                        if (address != null) {
                            prev = address.getAddressLine(0);
                            address.setAddressLine(0, (prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_ADDRESS_DISTRICT:
                        if (address != null) {
                            prev = address.getAdminArea();
                            address.setAdminArea((prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_ADDRESS_DISTRICT2:
                        if (address != null) {
                            prev = address.getSubAdminArea();
                            address.setSubAdminArea((prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_ADDRESS_COUNTRY:
                        if (address != null) {
                            prev = address.getCountryName();
                            address.setCountryName((prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_LOCALITY:
                        if (address != null) {
                            prev = address.getLocality();
                            address.setLocality((prev == null) ? text : prev + text);
                        }
                        break;
                    case TAG_ADDRESS_FORMATTED:
                        if (address != null) {
                            if (text.equals(address.getFeatureName())) {
                                address.setFeatureName(null);
                            }
                        }
                        break;
                    case TAG_ADDRESS:
                        state = State.LOCATION;
                        break;
                }
                break;
            case FINISH:
            default:
                break;
        }
    }
}

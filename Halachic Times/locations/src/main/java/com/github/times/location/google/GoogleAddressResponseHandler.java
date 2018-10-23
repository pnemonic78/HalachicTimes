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

import android.location.Address;

import com.github.times.location.DefaultAddressResponseHandler;
import com.github.times.location.ZmanimAddress;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

/**
 * Handler for parsing the XML response for addresses.
 *
 * @author Moshe Waisberg
 */
class GoogleAddressResponseHandler extends DefaultAddressResponseHandler {

    /**
     * Parse state.
     */
    private enum State {
        START, ROOT, STATUS, RESULT, RESULT_TYPE, ADDRESS, ADDRESS_TYPE, ADDRESS_LONG, ADDRESS_SHORT, GEOMETRY, LOCATION, LATITUDE, LONGITUDE, FINISH
    }

    private static final String STATUS_OK = "OK";

    private static final String TAG_ROOT = "GeocodeResponse";
    private static final String TAG_STATUS = "status";
    private static final String TAG_RESULT = "result";
    private static final String TAG_TYPE = "type";
    private static final String TAG_ADDRESS = "address_component";
    private static final String TAG_LONG_NAME = "long_name";
    private static final String TAG_SHORT_NAME = "short_name";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_LATITUDE = "lat";
    private static final String TAG_LONGITUDE = "lng";

    private static final String TYPE_ADMIN = "administrative_area_level_1";
    private static final String TYPE_COUNTRY = "country";
    private static final String TYPE_FEATURE = "feature_name";
    private static final String TYPE_LOCALITY = "locality";
    private static final String TYPE_POLITICAL = "political";
    private static final String TYPE_POSTAL_CODE = "postal_code";
    private static final String TYPE_PREMISE = "premise";
    private static final String TYPE_ROUTE = "route";
    private static final String TYPE_STREET = "street_address";
    private static final String TYPE_STREET_NUMBER = "street_number";
    private static final String TYPE_SUBADMIN = "administrative_area_level_2";
    private static final String TYPE_SUBLOCALITY = "sublocality";

    private State state = State.START;
    private Address address;
    private String longName;
    private String shortName;
    private String addressType;

    GoogleAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        super(results, maxResults, locale);
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
                switch (localName) {
                    case TAG_TYPE:
                        state = State.RESULT_TYPE;
                        break;
                    case TAG_ADDRESS:
                        state = State.ADDRESS;
                        break;
                    case TAG_GEOMETRY:
                        state = State.GEOMETRY;
                        break;
                }
                break;
            case ADDRESS:
                switch (localName) {
                    case TAG_LONG_NAME:
                        state = State.ADDRESS_LONG;
                        break;
                    case TAG_SHORT_NAME:
                        state = State.ADDRESS_SHORT;
                        break;
                    case TAG_TYPE:
                        state = State.ADDRESS_TYPE;
                        break;
                }
                break;
            case GEOMETRY:
                if (TAG_LOCATION.equals(localName))
                    state = State.LOCATION;
                break;
            case LOCATION:
                switch (localName) {
                    case TAG_LATITUDE:
                        state = State.LATITUDE;
                        break;
                    case TAG_LONGITUDE:
                        state = State.LONGITUDE;
                        break;
                }
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
                if (!STATUS_OK.equals(text)) {
                    state = State.FINISH;
                }
                break;
            case RESULT:
                if (TAG_RESULT.equals(localName)) {
                    if (address != null) {
                        if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                            results.add(address);
                        else
                            state = State.FINISH;
                        address = null;
                    }
                    longName = null;
                    shortName = null;
                    addressType = null;
                    state = State.ROOT;
                }
                break;
            case RESULT_TYPE:
                if (TAG_TYPE.equals(localName))
                    state = State.RESULT;
                if (!TYPE_POLITICAL.equals(text)) {
                    address = new ZmanimAddress(locale);
                }
                break;
            case ADDRESS:
                if (TAG_ADDRESS.equals(localName)) {
                    if (address != null) {
                        if (addressType != null) {
                            switch (addressType) {
                                case TYPE_ADMIN:
                                    address.setAdminArea(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_COUNTRY:
                                    address.setCountryCode(shortName);
                                    address.setCountryName(longName);
                                    break;
                                case TYPE_FEATURE:
                                    address.setFeatureName(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_LOCALITY:
                                    address.setLocality(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_POSTAL_CODE:
                                    address.setPostalCode(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_PREMISE:
                                    address.setPremises(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_ROUTE:
                                case TYPE_STREET:
                                case TYPE_STREET_NUMBER:
                                    address.setAddressLine(address.getMaxAddressLineIndex() + 1, isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_SUBADMIN:
                                    address.setSubAdminArea(isEmpty(shortName) ? longName : shortName);
                                    break;
                                case TYPE_SUBLOCALITY:
                                    address.setSubLocality(isEmpty(shortName) ? longName : shortName);
                                    break;
                            }
                        }
                        longName = null;
                        shortName = null;
                        addressType = null;
                    }
                    state = State.RESULT;
                }
                break;
            case ADDRESS_LONG:
                if (TAG_LONG_NAME.equals(localName)) {
                    longName = text;
                    state = State.ADDRESS;
                }
                break;
            case ADDRESS_SHORT:
                if (TAG_SHORT_NAME.equals(localName)) {
                    shortName = text;
                    state = State.ADDRESS;
                }
                break;
            case ADDRESS_TYPE:
                if (TAG_TYPE.equals(localName)) {
                    if (addressType == null)
                        addressType = text;
                    state = State.ADDRESS;
                }
                if (TYPE_POLITICAL.equals(text))
                    break;
                break;
            case GEOMETRY:
                if (TAG_GEOMETRY.equals(localName))
                    state = State.RESULT;
                break;
            case LOCATION:
                if (TAG_LOCATION.equals(localName))
                    state = State.GEOMETRY;
                break;
            case LATITUDE:
                if (TAG_LATITUDE.equals(localName)) {
                    if (address != null) {
                        try {
                            address.setLatitude(Double.parseDouble(text));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(text, nfe);
                        }
                    }
                    state = State.LOCATION;
                }
                break;
            case LONGITUDE:
                if (TAG_LONGITUDE.equals(localName)) {
                    if (address != null) {
                        try {
                            address.setLongitude(Double.parseDouble(text));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(text, nfe);
                        }
                    }
                    state = State.LOCATION;
                }
                break;
            case FINISH:
                return;
            default:
                break;
        }
    }
}

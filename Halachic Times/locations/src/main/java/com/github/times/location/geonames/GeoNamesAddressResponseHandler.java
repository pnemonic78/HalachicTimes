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
package com.github.times.location.geonames;

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
class GeoNamesAddressResponseHandler extends DefaultAddressResponseHandler {

    /**
     * Parse state.
     */
    private enum State {
        START, ROOT, GEONAME, TOPONYM, TOPONYM_NAME, COUNTRY_CODE, COUNTRY, LATITUDE, LONGITUDE, STREET, STREET_NUMBER, MTFCC, LOCALITY, POSTAL_CODE, ADMIN_CODE, ADMIN, SUBADMIN_CODE, SUBADMIN, FINISH
    }

    private static final String TAG_ROOT = "geonames";
    private static final String TAG_LATITUDE = "lat";
    private static final String TAG_LONGITUDE = "lng";

    private static final String TAG_GEONAME = "geoname";
    private static final String TAG_TOPONYM = "toponymName";
    private static final String TAG_NAME = "name";
    private static final String TAG_CC = "countryCode";
    private static final String TAG_COUNTRY = "countryName";

    private static final String TAG_ADDRESS = "address";
    private static final String TAG_STREET = "street";
    private static final String TAG_MTFCC = "mtfcc";
    private static final String TAG_STREET_NUMBER = "streetNumber";
    private static final String TAG_POSTAL_CODE = "postalcode";
    private static final String TAG_LOCALITY = "placename";
    private static final String TAG_SUBADMIN_CODE = "adminCode2";
    private static final String TAG_SUBADMIN = "adminName2";
    private static final String TAG_ADMIN_CODE = "adminCode1";
    private static final String TAG_ADMIN = "adminName1";

    private State state = State.START;
    private final List<Address> results;
    private final int maxResults;
    private final Locale locale;
    private Address address;

    /**
     * Constructs a new parse handler.
     *
     * @param results
     *         the destination results.
     * @param maxResults
     *         the maximum number of results.
     */
    public GeoNamesAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
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
                    case TAG_GEONAME:
                        state = State.GEONAME;
                        address = new ZmanimAddress(locale);
                        break;
                    case TAG_ADDRESS:
                        state = State.GEONAME;
                        address = new ZmanimAddress(locale);
                        break;
                }
                break;
            case GEONAME:
                switch (localName) {
                    case TAG_TOPONYM:
                        state = State.TOPONYM;
                        break;
                    case TAG_NAME:
                        state = State.TOPONYM_NAME;
                        break;
                    case TAG_LATITUDE:
                        state = State.LATITUDE;
                        break;
                    case TAG_LONGITUDE:
                        state = State.LONGITUDE;
                        break;
                    case TAG_CC:
                        state = State.COUNTRY_CODE;
                        break;
                    case TAG_COUNTRY:
                        state = State.COUNTRY;
                        break;
                    case TAG_STREET:
                        state = State.STREET;
                        break;
                    case TAG_MTFCC:
                        state = State.MTFCC;
                        break;
                    case TAG_STREET_NUMBER:
                        state = State.STREET_NUMBER;
                        break;
                    case TAG_POSTAL_CODE:
                        state = State.POSTAL_CODE;
                        break;
                    case TAG_LOCALITY:
                        state = State.LOCALITY;
                        break;
                    case TAG_ADMIN:
                        state = State.ADMIN;
                        break;
                    case TAG_ADMIN_CODE:
                        state = State.ADMIN_CODE;
                        break;
                    case TAG_SUBADMIN:
                        state = State.SUBADMIN;
                        break;
                    case TAG_SUBADMIN_CODE:
                        state = State.SUBADMIN_CODE;
                        break;
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
            case GEONAME:
                switch (localName) {
                    case TAG_GEONAME:
                        if (address != null) {
                            if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                        state = State.ROOT;
                        break;
                    case TAG_ADDRESS:
                        if (address != null) {
                            if (results.size() < maxResults)
                                results.add(address);
                            else
                                state = State.FINISH;
                            address = null;
                        }
                        state = State.ROOT;
                        break;
                }
                break;
            case ADMIN:
                if (TAG_ADMIN.equals(localName)) {
                    if (address != null) {
                        address.setAdminArea(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case ADMIN_CODE:
                if (TAG_ADMIN_CODE.equals(localName)) {
                    if ((address != null) && (address.getAdminArea() == null)) {
                        address.setAdminArea(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case COUNTRY:
                if (TAG_COUNTRY.equals(localName)) {
                    if (address != null) {
                        address.setCountryName(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case COUNTRY_CODE:
                if (TAG_CC.equals(localName)) {
                    if (address != null) {
                        address.setCountryCode(text);
                        if (!isEmpty(text) && (address.getCountryName() == null)) {
                            Locale countryLocale = new Locale(locale.getLanguage(), text);
                            address.setCountryName(countryLocale.getDisplayCountry(countryLocale));
                        }
                    }
                    state = State.GEONAME;
                }
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
                    state = State.GEONAME;
                }
                break;
            case LOCALITY:
                if (TAG_LOCALITY.equals(localName)) {
                    if (address != null) {
                        address.setLocality(text);
                    }
                    state = State.GEONAME;
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
                    state = State.GEONAME;
                }
                break;
            case MTFCC:
                if (TAG_MTFCC.equals(localName))
                    state = State.GEONAME;
                break;
            case POSTAL_CODE:
                if (TAG_POSTAL_CODE.equals(localName)) {
                    if (address != null) {
                        address.setPostalCode(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case STREET:
                if (TAG_STREET.equals(localName)) {
                    if (address != null) {
                        address.setAddressLine(1, text);
                    }
                    state = State.GEONAME;
                }
                break;
            case STREET_NUMBER:
                if (TAG_STREET_NUMBER.equals(localName)) {
                    if (address != null) {
                        address.setAddressLine(0, text);
                    }
                    state = State.GEONAME;
                }
                break;
            case SUBADMIN:
                if (TAG_SUBADMIN.equals(localName)) {
                    if (address != null) {
                        address.setSubAdminArea(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case SUBADMIN_CODE:
                if (TAG_SUBADMIN_CODE.equals(localName)) {
                    if ((address != null) && (address.getSubAdminArea() == null)) {
                        address.setSubAdminArea(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case TOPONYM:
                if (TAG_TOPONYM.equals(localName)) {
                    if ((address != null) && (address.getFeatureName() == null)) {
                        address.setFeatureName(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case TOPONYM_NAME:
                if (TAG_NAME.equals(localName)) {
                    if (address != null) {
                        address.setFeatureName(text);
                    }
                    state = State.GEONAME;
                }
                break;
            case FINISH:
            default:
                break;
        }
    }
}

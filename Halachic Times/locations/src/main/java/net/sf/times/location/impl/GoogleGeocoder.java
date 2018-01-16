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
package net.sf.times.location.impl;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;

import net.sf.times.location.GeocoderBase;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.util.LocaleUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Google Geocoding API.
 * <p/>
 * <a href="http://code.google.com/apis/maps/documentation/geocoding/">http://
 * code.google.com/apis/maps/documentation/geocoding/</a>
 *
 * @author Moshe Waisberg
 */
public class GoogleGeocoder extends GeocoderBase {

    /** URL that accepts latitude and longitude coordinates as parameters. */
    private static final String URL_LATLNG = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=%f,%f&language=%s&sensor=true";
    /** URL that accepts an address as parameters. */
    private static final String URL_ADDRESS = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&language=%s&sensor=true";
    /** URL that accepts a bounded address as parameters. */
    private static final String URL_ADDRESS_BOUNDED = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&bounds=%f,%f|%f,%f&language=%s&sensor=true";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION = "http://maps.googleapis.com/maps/api/elevation/xml?locations=%f,%f";

    /**
     * Creates a new Google geocoder.
     *
     * @param context
     *         the context.
     */
    public GoogleGeocoder(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new Google geocoder.
     *
     * @param locale
     *         the locale.
     */
    public GoogleGeocoder(Locale locale) {
        super(locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        String queryUrl = String.format(Locale.US, URL_ADDRESS, locationName, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
                                             double upperRightLongitude) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        if (lowerLeftLatitude < LATITUDE_MIN || lowerLeftLatitude > LATITUDE_MAX)
            throw new IllegalArgumentException("lowerLeftLatitude == " + lowerLeftLatitude);
        if (lowerLeftLongitude < LONGITUDE_MIN || lowerLeftLongitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("lowerLeftLongitude == " + lowerLeftLongitude);
        if (upperRightLatitude < LATITUDE_MIN || upperRightLatitude > LATITUDE_MAX)
            throw new IllegalArgumentException("upperRightLatitude == " + upperRightLatitude);
        if (upperRightLongitude < LONGITUDE_MIN || upperRightLongitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("upperRightLongitude == " + upperRightLongitude);
        String queryUrl = String
                .format(Locale.US, URL_ADDRESS_BOUNDED, locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, getLanguage());
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return new GeocodeResponseHandler(results, maxResults, locale);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class GeocodeResponseHandler extends DefaultAddressResponseHandler {

        /** Parse state. */
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
        private static final String TYPE_ROUTE = "route";
        private static final String TYPE_STREET = "street_address";
        private static final String TYPE_STREET_NUMBER = "street_number";
        private static final String TYPE_SUBADMIN = "administrative_area_level_2";
        private static final String TYPE_SUBLOCALITY = "sublocality";

        private State state = State.START;
        private final List<Address> results;
        private final int maxResults;
        private final Locale locale;
        private Address address;
        private String longName;
        private String shortName;
        private String addressType;

        /**
         * Constructs a new parse handler.
         *
         * @param results
         *         the destination results.
         * @param maxResults
         *         the maximum number of results.
         */
        public GeocodeResponseHandler(List<Address> results, int maxResults, Locale locale) {
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
                                        address.setAdminArea(TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_SUBADMIN:
                                        address.setSubAdminArea(TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_COUNTRY:
                                        address.setCountryCode(shortName);
                                        address.setCountryName(longName);
                                        break;
                                    case TYPE_FEATURE:
                                        address.setFeatureName(TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_LOCALITY:
                                        address.setLocality(TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_POSTAL_CODE:
                                        address.setPostalCode(TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_ROUTE:
                                    case TYPE_STREET:
                                    case TYPE_STREET_NUMBER:
                                        address.setAddressLine(address.getMaxAddressLineIndex() + 1, TextUtils.isEmpty(shortName) ? longName : shortName);
                                        break;
                                    case TYPE_SUBLOCALITY:
                                        address.setSubLocality(TextUtils.isEmpty(shortName) ? longName : shortName);
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
                    longName = text;
                    if (TAG_LONG_NAME.equals(localName))
                        state = State.ADDRESS;
                    break;
                case ADDRESS_SHORT:
                    shortName = text;
                    if (TAG_SHORT_NAME.equals(localName))
                        state = State.ADDRESS;
                    break;
                case ADDRESS_TYPE:
                    if (TAG_TYPE.equals(localName))
                        state = State.ADDRESS;
                    if (TYPE_POLITICAL.equals(text))
                        break;
                    if (addressType == null)
                        addressType = text;
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
                    if (address != null) {
                        try {
                            address.setLatitude(Double.parseDouble(text));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(nfe);
                        }
                    }
                    if (TAG_LATITUDE.equals(localName))
                        state = State.LOCATION;
                    break;
                case LONGITUDE:
                    if (address != null) {
                        try {
                            address.setLongitude(Double.parseDouble(text));
                        } catch (NumberFormatException nfe) {
                            throw new SAXException(nfe);
                        }
                    }
                    if (TAG_LONGITUDE.equals(localName))
                        state = State.LOCATION;
                    break;
                case FINISH:
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude);
        return getElevationXMLFromURL(queryUrl);
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return new ElevationResponseHandler(results);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class ElevationResponseHandler extends DefaultAddressResponseHandler {

        /** Parse state. */
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

        private State state = State.START;
        private final List<ZmanimLocation> results;
        private ZmanimLocation location;

        /**
         * Constructs a new parse handler.
         *
         * @param results
         *         the destination results.
         */
        public ElevationResponseHandler(List<ZmanimLocation> results) {
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
                            if (location != null) {
                                try {
                                    location.setAltitude(Double.parseDouble(text));
                                    results.add(location);
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(nfe);
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
                                    throw new SAXException(nfe);
                                }
                            }
                            break;
                        case TAG_LONGITUDE:
                            if (location != null) {
                                try {
                                    location.setLongitude(Double.parseDouble(text));
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(nfe);
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

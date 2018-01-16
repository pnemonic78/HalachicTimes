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
import android.util.Log;

import net.sf.net.HTTPReader;
import net.sf.times.location.BuildConfig;
import net.sf.times.location.GeocoderBase;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.util.LocaleUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * GeoNames WebServices API.
 * <p/>
 * <a
 * href="http://www.geonames.org/export/web-services.html">http://www.geonames
 * .org/export/web-services.html</a>
 *
 * @author Moshe Waisberg
 */
public class GeoNamesGeocoder extends GeocoderBase {

    private static final String TAG = "GeoNamesGeocoder";

    /** GeoNames user name. */
    private static final String USERNAME = BuildConfig.GEONAMES_USERNAME;

    /** URL that accepts latitude and longitude coordinates as parameters. */
    private static final String URL_LATLNG = "http://api.geonames.org/extendedFindNearby?lat=%f&lng=%f&lang=%s&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Shuttle Radar Topography Mission (SRTM) elevation data.
     */
    private static final String URL_ELEVATION_SRTM3 = "http://api.geonames.org/srtm3?lat=%f&lng=%f&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Aster Global Digital Elevation Model data.
     */
    private static final String URL_ELEVATION_AGDEM = "http://api.geonames.org/astergdem?lat=%f&lng=%f&username=%s";

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param context
     *         the context.
     */
    public GeoNamesGeocoder(Context context) {
        this(context, LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public GeoNamesGeocoder(Context context, Locale locale) {
        super(context, locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        if (TextUtils.isEmpty(USERNAME))
            return null;
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), USERNAME);
        return getAddressXMLFromURL(queryUrl, maxResults);
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return new AddressResponseHandler(results, maxResults, locale);
    }

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected static class AddressResponseHandler extends DefaultAddressResponseHandler {

        /** Parse state. */
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
        public AddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
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
                            state = State.ROOT;
                            if (address != null) {
                                if ((results.size() < maxResults) && address.hasLatitude() && address.hasLongitude())
                                    results.add(address);
                                else
                                    state = State.FINISH;
                                address = null;
                            }
                            break;
                        case TAG_ADDRESS:
                            state = State.ROOT;
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
                    if (address != null) {
                        address.setAdminArea(text);
                    }
                    if (TAG_ADMIN.equals(localName))
                        state = State.GEONAME;
                    break;
                case ADMIN_CODE:
                    if ((address != null) && (address.getAdminArea() == null)) {
                        address.setAdminArea(text);
                    }
                    if (TAG_ADMIN_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case COUNTRY:
                    if (address != null) {
                        address.setCountryName(text);
                    }
                    if (TAG_COUNTRY.equals(localName))
                        state = State.GEONAME;
                    break;
                case COUNTRY_CODE:
                    if (address != null) {
                        address.setCountryCode(text);
                        if (address.getCountryName() == null)
                            address.setCountryName(new Locale(locale.getLanguage(), text).getDisplayCountry());
                    }
                    if (TAG_CC.equals(localName))
                        state = State.GEONAME;
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
                        state = State.GEONAME;
                    break;
                case LOCALITY:
                    if (address != null) {
                        address.setLocality(text);
                    }
                    if (TAG_LOCALITY.equals(localName))
                        state = State.GEONAME;
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
                        state = State.GEONAME;
                    break;
                case MTFCC:
                    if (TAG_MTFCC.equals(localName))
                        state = State.GEONAME;
                    break;
                case POSTAL_CODE:
                    if (address != null) {
                        address.setPostalCode(text);
                    }
                    if (TAG_POSTAL_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case STREET:
                    if (address != null) {
                        address.setAddressLine(1, text);
                    }
                    if (TAG_STREET.equals(localName))
                        state = State.GEONAME;
                    break;
                case STREET_NUMBER:
                    if (address != null) {
                        address.setAddressLine(0, text);
                    }
                    if (TAG_STREET_NUMBER.equals(localName))
                        state = State.GEONAME;
                    break;
                case SUBADMIN:
                    if (address != null) {
                        address.setSubAdminArea(text);
                    }
                    if (TAG_SUBADMIN.equals(localName))
                        state = State.GEONAME;
                    break;
                case SUBADMIN_CODE:
                    if ((address != null) && (address.getSubAdminArea() == null)) {
                        address.setSubAdminArea(text);
                    }
                    if (TAG_SUBADMIN_CODE.equals(localName))
                        state = State.GEONAME;
                    break;
                case TOPONYM:
                    if ((address != null) && (address.getFeatureName() == null)) {
                        address.setFeatureName(text);
                    }
                    if (TAG_TOPONYM.equals(localName))
                        state = State.GEONAME;
                    break;
                case TOPONYM_NAME:
                    if (address != null) {
                        address.setFeatureName(text);
                    }
                    if (TAG_NAME.equals(localName))
                        state = State.GEONAME;
                    break;
                case FINISH:
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
        if (TextUtils.isEmpty(USERNAME))
            return null;
        String queryUrl = String.format(Locale.US, URL_ELEVATION_SRTM3, latitude, longitude, USERNAME);
        URL url = new URL(queryUrl);
        byte[] data = HTTPReader.read(url);
        if (data == null)
            return null;
        String text = new String(data);
        double elevation;
        ZmanimLocation elevated;
        try {
            elevation = Double.parseDouble(text);
            if (elevation <= -9999)
                return null;
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(latitude);
            elevated.setLongitude(longitude);
            elevated.setAltitude(elevation);
            return elevated;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Bad elevation: [" + text + "] at " + latitude + "," + longitude, nfe);
        }
        return null;
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return null;
    }

}

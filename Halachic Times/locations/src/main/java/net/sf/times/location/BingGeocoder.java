/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.location;

import android.content.Context;
import android.location.Address;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Microsoft Bing API.
 * <p/>
 * <a href="http://msdn.microsoft.com/en-us/library/ff701710.aspx">http://msdn.
 * microsoft.com/en-us/library/ff701710.aspx</a>
 *
 * @author Moshe Waisberg
 */
public class BingGeocoder extends GeocoderBase {

    /** Bing API key. */
    private static final String API_KEY = BuildConfig.BING_API_KEY;

    /** URL that accepts latitude and longitude coordinates as parameters. */
    private static final String URL_LATLNG = "http://dev.virtualearth.net/REST/v1/Locations/%f,%f?o=xml&c=%s&key=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION = "http://dev.virtualearth.net/REST/v1/Elevation/List?o=xml&points=%f,%f&key=%s";

    /**
     * Creates a new Bing geocoder.
     *
     * @param context
     *         the context.
     */
    public BingGeocoder(Context context) {
        this(context, Locale.getDefault());
    }

    /**
     * Creates a new Bing geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public BingGeocoder(Context context, Locale locale) {
        super(context, locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), API_KEY);
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
        private static final String TAG_ADDRESS_COUNTRY = "CountryRegion";
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
                                    throw new SAXException(nfe);
                                }
                            }
                            break;
                        case TAG_LONGITUDE:
                            if (address != null) {
                                try {
                                    address.setLongitude(Double.parseDouble(text));
                                } catch (NumberFormatException nfe) {
                                    throw new SAXException(nfe);
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

    @Override
    public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY);
        ZmanimLocation location = getElevationXMLFromURL(queryUrl);
        if (location != null) {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }
        return location;
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
                                    throw new SAXException(nfe);
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

}

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

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.github.net.HTTPReader;
import com.github.util.LocaleUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding.
 *
 * @author Moshe Waisberg
 */
public abstract class GeocoderBase {

    /**
     * The "user pick a city" location provider.
     */
    public static final String USER_PROVIDER = "user";

    /**
     * Maximum radius to consider two locations in the same vicinity.
     */
    protected static final float SAME_LOCATION = 250f;// 250 metres.
    /**
     * Maximum radius to consider a location near the same city.
     * <p/>
     * New York city, USA, is <tt>8,683 km<sup>2</sup></tt>, thus radius is
     * about <tt>37.175 km</tt>.<br>
     * Johannesburg/East Rand, ZA, is <tt>2,396 km<sup>2</sup></tt>, thus radius
     * is about <tt>19.527 km</tt>..<br>
     * Cape Town, ZA, is <tt>686 km<sup>2</sup></tt>, thus radius is about
     * <tt>10.449 km</tt>.
     */
    protected static final float SAME_CITY = 15000f;// 15 kilometres.
    /**
     * Maximum radius to consider a location near the same plateau with similar terrain.
     */
    protected static final float SAME_PLATEAU = 150000f;// 150 kilometres.
    /**
     * Maximum radius to consider a location near the same planet.
     */
    protected static final float SAME_PLANET = 6000000f;// 6000 kilometres.

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    protected final Locale locale;
    private AddressResponseParser addressResponseParser;
    private ElevationResponseParser elevationResponseParser;

    /**
     * Creates a new geocoder.
     *
     * @param locale the locale.
     */
    public GeocoderBase(Locale locale) {
        this.locale = locale;
    }

    /**
     * Creates a new geocoder.
     *
     * @param context the context.
     */
    public GeocoderBase(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Returns an array of Addresses that are known to describe the area
     * immediately surrounding the given latitude and longitude.
     *
     * @param latitude   the latitude a point for the search.
     * @param longitude  the longitude a point for the search.
     * @param maxResults maximum number of addresses to return. Smaller numbers (1 to
     *                   5) are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public abstract List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException;

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName a user-supplied description of a location.
     * @param maxResults   max number of addresses to return. Smaller numbers (1 to 5)
     *                     are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        return null;
    }

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * You may specify a bounding box for the search results by including the
     * Latitude and Longitude of the Lower Left point and Upper Right point of
     * the box.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName        a user-supplied description of a location.
     * @param maxResults          max number of addresses to return. Smaller numbers (1 to 5)
     *                            are recommended.
     * @param lowerLeftLatitude   the latitude of the lower left corner of the bounding box.
     * @param lowerLeftLongitude  the longitude of the lower left corner of the bounding box.
     * @param upperRightLatitude  the latitude of the upper right corner of the bounding box.
     * @param upperRightLongitude the longitude of the upper right corner of the bounding box.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
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
        return null;
    }

    /**
     * Get the address by parsing the XML results.
     *
     * @param latitude   the requested latitude.
     * @param longitude  the requested longitude.
     * @param queryUrl   the URL.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    protected List<Address> getJsonAddressesFromURL(double latitude, double longitude, String queryUrl, int maxResults) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = null;
        try {
            data = HTTPReader.read(url, HTTPReader.CONTENT_JSON);
            return parseAddresses(data, latitude, longitude, locale, maxResults);
        } finally {
            if (data != null) {
                try {
                    data.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    /**
     * Parse the JSON response for addresses.
     *
     * @param data       the JSON data.
     * @param latitude   the requested latitude.
     * @param longitude  the requested longitude.
     * @param locale     the locale.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable
    protected List<Address> parseAddresses(InputStream data, double latitude, double longitude, Locale locale, int maxResults) throws IOException {
        // Minimum length for either "<>" or "{}"
        if ((data == null) || (data.available() <= 2)) {
            return null;
        }

        AddressResponseParser parser = getAddressResponseParser();
        return parser.parse(data, latitude, longitude, maxResults, locale);
    }

    /**
     * Create a parser for addresses.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    protected abstract AddressResponseParser createAddressResponseParser() throws LocationException;

    /**
     * Get a parser for addresses.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    protected AddressResponseParser getAddressResponseParser() throws LocationException {
        if (addressResponseParser == null) {
            addressResponseParser = createAddressResponseParser();
        }
        return addressResponseParser;
    }

    /**
     * Get the ISO 639 language code.
     *
     * @return the language code.
     */
    protected String getLanguage() {
        String language = locale.getLanguage();
        if ("in".equals(language))
            return "id";
        if ("iw".equals(language))
            return "he";
        if ("ji".equals(language))
            return "yi";
        return language;
    }

    /**
     * Get the location with elevation.
     *
     * @param latitude  the latitude a point for the search.
     * @param longitude the longitude a point for the search.
     * @return the location - {@code null} otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public abstract Location getElevation(double latitude, double longitude) throws IOException;

    /**
     * Parse the XML response for an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the XML data.
     * @return the location - {@code null} otherwise.
     * @throws LocationException if a location error occurs.
     * @throws IOException       if an I/O error occurs.
     */
    @Nullable
    protected Location parseElevation(double latitude, double longitude, InputStream data) throws LocationException, IOException {
        // Minimum length for either "<>" or "{}"
        if ((data == null) || (data.available() <= 2)) {
            return null;
        }

        ElevationResponseParser parser = getElevationResponseParser();
        List<Location> results = parser.parse(data, latitude, longitude, 1);

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get the elevation by parsing the plain text result.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - {@code null} otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    protected Location getTextElevationFromURL(double latitude, double longitude, String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = null;
        try {
            data = HTTPReader.read(url);
            return parseElevation(latitude, longitude, data);
        } finally {
            if (data != null) {
                try {
                    data.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    /**
     * Get the elevation by parsing the JSON results.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - {@code null} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    protected Location getJsonElevationFromURL(double latitude, double longitude, String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = null;
        try {
            data = HTTPReader.read(url, HTTPReader.CONTENT_JSON);
            return parseElevation(latitude, longitude, data);
        } finally {
            if (data != null) {
                try {
                    data.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    /**
     * Create a handler to parse elevations.
     *
     * @return the handler.
     * @throws LocationException if a location error occurs.
     */
    protected abstract ElevationResponseParser createElevationResponseParser() throws LocationException;

    /**
     * Get a parser for elevations.
     *
     * @return the parser.
     * @throws LocationException if a location error occurs.
     */
    protected ElevationResponseParser getElevationResponseParser() throws LocationException {
        ElevationResponseParser elevationResponseParser = this.elevationResponseParser;
        if (elevationResponseParser == null) {
            elevationResponseParser = createElevationResponseParser();
            this.elevationResponseParser = elevationResponseParser;
        }
        return elevationResponseParser;
    }

    protected static String decodeApiKey(String encoded) {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

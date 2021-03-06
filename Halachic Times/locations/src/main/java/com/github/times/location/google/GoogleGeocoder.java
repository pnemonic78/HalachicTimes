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

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.github.times.location.AddressResponseParser;
import com.github.times.location.BuildConfig;
import com.github.times.location.ElevationResponseParser;
import com.github.times.location.GeocoderBase;
import com.github.times.location.LocationException;
import com.github.util.LocaleUtils;

import java.io.IOException;
import java.net.URLEncoder;
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

    /**
     * URL that accepts latitude and longitude coordinates as parameters.
     */
    private static final String URL_LATLNG = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=%s&key=%s&sensor=true";
    /**
     * URL that accepts an address as parameters.
     */
    private static final String URL_ADDRESS = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&language=%s&key=%s&sensor=true";
    /**
     * URL that accepts a bounded address as parameters.
     */
    private static final String URL_ADDRESS_BOUNDED = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&bounds=%f,%f|%f,%f&language=%s&key=%s&sensor=true";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION = "https://maps.googleapis.com/maps/api/elevation/json?locations=%f,%f&key=%s";

    /**
     * Google API key.
     */
    private static final String API_KEY = BuildConfig.GOOGLE_API_KEY;

    /**
     * Creates a new Google geocoder.
     *
     * @param context the context.
     */
    public GoogleGeocoder(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new Google geocoder.
     *
     * @param locale the locale.
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
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), API_KEY);
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        String queryUrl = String.format(Locale.US, URL_ADDRESS, URLEncoder.encode(locationName), getLanguage(), API_KEY);
        return getJsonAddressesFromURL(0.0, 0.0, queryUrl, maxResults);
    }

    @Override
    public List<Address> getFromLocationName(String locationName, int maxResults,
                                             double lowerLeftLatitude, double lowerLeftLongitude,
                                             double upperRightLatitude, double upperRightLongitude) throws IOException {
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
        String queryUrl = String.format(Locale.US, URL_ADDRESS_BOUNDED, URLEncoder.encode(locationName), lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, getLanguage(), API_KEY);
        double latitude = (lowerLeftLatitude + upperRightLatitude) / 2;
        double longitude = (lowerLeftLongitude + upperRightLongitude) / 2;
        return getJsonAddressesFromURL(latitude, longitude, queryUrl, maxResults);
    }

    @Override
    protected AddressResponseParser createAddressResponseParser() throws LocationException {
        return new GoogleAddressResponseParser();
    }

    @Override
    public Location getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY);
        return getJsonElevationFromURL(latitude, longitude, queryUrl);
    }

    @Override
    protected ElevationResponseParser createElevationResponseParser() throws LocationException {
        return new GoogleElevationResponseParser();
    }
}

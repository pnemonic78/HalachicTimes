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

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.text.TextUtils;

import com.github.times.location.AddressResponseParser;
import com.github.times.location.BuildConfig;
import com.github.times.location.ElevationResponseParser;
import com.github.times.location.GeocoderBase;
import com.github.times.location.LocationException;
import com.github.util.LocaleUtils;

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

    /**
     * URL that accepts latitude and longitude coordinates as parameters.
     */
    private static final String URL_LATLNG = "https://dev.virtualearth.net/REST/v1/Locations/%f,%f?o=json&c=%s&key=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.
     */
    private static final String URL_ELEVATION = "https://dev.virtualearth.net/REST/v1/Elevation/List?o=json&points=%f,%f&key=%s";

    /**
     * Bing API key.
     */
    private static final String API_KEY = BuildConfig.BING_API_KEY;

    /**
     * Creates a new Bing geocoder.
     *
     * @param context the context.
     */
    public BingGeocoder(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new Bing geocoder.
     *
     * @param locale the locale.
     */
    public BingGeocoder(Locale locale) {
        super(locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        if (TextUtils.isEmpty(API_KEY))
            return null;
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), API_KEY);
        return getJsonAddressesFromURL(queryUrl, maxResults);
    }

    @Override
    protected AddressResponseParser createAddressResponseParser() {
        return new BingAddressResponseParser();
    }

    @Override
    public Location getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        if (TextUtils.isEmpty(API_KEY))
            return null;
        String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY);
        return getJsonElevationFromURL(latitude, longitude, queryUrl);
    }

    @Override
    protected ElevationResponseParser createElevationResponseHandler() throws LocationException {
        return new BingElevationResponseParser();
    }
}
